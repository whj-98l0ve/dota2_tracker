package com.whj_98l0ve.dota2_tracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whj_98l0ve.dota2_tracker.client.OpenDotaClient;
import com.whj_98l0ve.dota2_tracker.entity.*;
import com.whj_98l0ve.dota2_tracker.mapper.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenDotaService {

    private final OpenDotaClient openDotaClient;
    private final DotaMatchRawMapper rawMapper;
    private final DotaMatchPlayerMapper matchPlayerMapper;
    private final DotaPlayerSummaryMapper summaryMapper;
    private final DotaPlayerHeroStatsMapper heroStatsMapper;
    private final DotaParseTaskMapper taskMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 启动时自动检查 dota_player_summary 表是否包含 remark 备注列，若旧库缺失则自动补齐
     */
    @PostConstruct
    public void ensureRemarkColumnExists() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dota_player_summary' AND COLUMN_NAME = 'remark'",
                    Integer.class
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE dota_player_summary ADD COLUMN remark VARCHAR(64) DEFAULT NULL COMMENT '管理员备注名称' AFTER personaname");
                log.info("✅ 已自动为 dota_player_summary 表补充 remark 备注字段");
            }
        } catch (Exception e) {
            log.debug("检查/补充 remark 字段跳过: {}", e.getMessage());
        }
    }

    /**
     * 每小时定时巡检的关注玩家错峰队列：
     * 每隔 1 小时将所有 is_tracked = 1 的玩家 ID 放入此队列，再由消费定时器每隔 20 秒取出 1 个玩家单独请求，
     * 严格避免多个关注玩家同时并发请求 OpenDota 导致限流或报错。
     */
    private final Queue<Long> hourlySyncQueue = new ConcurrentLinkedQueue<>();
    private volatile LocalDateTime lastHourlyScheduleTime = null;
    private volatile LocalDateTime nextHourlyScheduleTime = LocalDateTime.now().plusHours(1);

    /**
     * 1. 用户触发录入同步：拉取近 7 天列表，筛选并建立高精度录像抓取任务
     */
    public Map<String, Object> syncPlayerRecentMatches(Long accountId) {
        Map<String, Object> result = new HashMap<>();

        // 若当前处于 10 分钟全局熔断期，直接跳过远程请求降级返回本地数据
        if (openDotaClient.isCircuitBreakerOpen()) {
            log.warn("⛔ [熔断器拦截] 当前处于 OpenDota 熔断冷却期 (剩余 {} 秒)，玩家 [{}] 直接降级读取本地数据",
                    openDotaClient.getCircuitBreakerRemainingSeconds(), accountId);
            result.put("totalFound", 0);
            result.put("alreadyInDb", 0);
            result.put("queuedTasks", 0);
            result.put("fallback", true);
            result.put("msg", "OpenDota 服务熔断保护中，直接从本地数据库呈现");
            return result;
        }

        // 1. 同步玩家基本资料（只存头像和昵称）
        syncPlayerProfile(accountId);

        // 若同步资料时已触发熔断，则不再继续请求比赛列表
        if (openDotaClient.isCircuitBreakerOpen()) {
            result.put("totalFound", 0);
            result.put("alreadyInDb", 0);
            result.put("queuedTasks", 0);
            result.put("fallback", true);
            result.put("msg", "OpenDota 服务熔断保护中，直接从本地数据库呈现");
            return result;
        }

        // 2. 调用 OpenDota 获取比赛列表：优先请求 7 天内比赛记录 (?date=7)，避免全量拉取触发 OpenDota 500 错误；
        //    若 7 天内记录请求也报错或返回非数组（且未触发全局熔断），则自动降级请求 recentMatches (20场) 接口
        boolean usedRecentMatchesFallback = false;
        JsonNode matchesArray = openDotaClient.get("/players/" + accountId + "/matches?date=7");

        if ((matchesArray == null || !matchesArray.isArray() || matchesArray.has("error"))
                && !openDotaClient.isCircuitBreakerOpen()) {
            log.warn("⚠️ 获取玩家 [{}] 7天内比赛记录 (matches?date=7) 报错或为空，自动降级请求 recentMatches 接口...", accountId);
            matchesArray = openDotaClient.get("/players/" + accountId + "/recentMatches");
            usedRecentMatchesFallback = true;
        }

        if (matchesArray == null || !matchesArray.isArray()) {
            log.warn("⚠️ 获取玩家 [{}] 近期比赛列表失败 (matches?date=7 与 recentMatches 均异常)，直接降级读取本地", accountId);
            result.put("totalFound", 0);
            result.put("alreadyInDb", 0);
            result.put("queuedTasks", 0);
            result.put("fallback", true);
            result.put("msg", "远程同步超时，直接从本地数据库呈现");
            return result;
        }

        int totalFound = 0;
        int alreadyInDb = 0;
        int queuedTasks = 0;

        // 计算精确的 7 天前时间戳（秒）
        long exactSevenDaysAgo = Instant.now().minusSeconds(7L * 24 * 3600).getEpochSecond();

        for (JsonNode m : matchesArray) {
            long matchId = m.path("match_id").asLong();
            long startTime = m.path("start_time").asLong(0);

            // 若使用的是 matches?date=7 接口，超出 7 天截断退出；若已降级为 recentMatches，则保留 recentMatches 返回的近期记录
            if (!usedRecentMatchesFallback && startTime > 0 && startTime < exactSevenDaysAgo) {
                break;
            }

            totalFound++;

            // 判定 1：本地数据库已有此高精度比赛，严格跳过！
            if (rawMapper.selectById(matchId) != null) {
                alreadyInDb++;
                continue;
            }

            // 判定 2：检查官方是否已解析 (version > 0)
            boolean hasParsedByOpenDota = m.has("version") && m.path("version").asInt(0) > 0;
            String status = hasParsedByOpenDota ? "READY_TO_FETCH" : "PENDING";
            LocalDateTime nextCheck = LocalDateTime.now();

            // 判定 3：查询任务表状态（可能由开黑队友或上一次同步创建）
            DotaParseTask existingTask = taskMapper.selectOne(new LambdaQueryWrapper<DotaParseTask>()
                    .eq(DotaParseTask::getMatchId, matchId)
                    .last("LIMIT 1"));

            if (existingTask != null) {
                // 若此前任务处于 PENDING/REQUESTED（例如队友先同步时还未解析完），但当前 OpenDota 列表显示已解析完 (version > 0)，
                // 则立即升级为 READY_TO_FETCH 并重置检查时间，避免继续苦等或被饿死
                if (hasParsedByOpenDota && !"READY_TO_FETCH".equals(existingTask.getStatus())) {
                    existingTask.setAccountId(accountId);
                    existingTask.setStatus("READY_TO_FETCH");
                    existingTask.setCheckCount(0);
                    existingTask.setNextCheckTime(nextCheck);
                    taskMapper.updateById(existingTask);
                    queuedTasks++;
                    continue;
                }

                // 若任务已在队列中等待处理，将归属关联更新为当前请求玩家并唤醒检查时间，跳过重复插入
                if (Arrays.asList("PENDING", "READY_TO_FETCH", "REQUESTED").contains(existingTask.getStatus())) {
                    existingTask.setAccountId(accountId);
                    if ("READY_TO_FETCH".equals(existingTask.getStatus())) {
                        existingTask.setNextCheckTime(nextCheck);
                    }
                    taskMapper.updateById(existingTask);
                    queuedTasks++;
                    continue;
                }

                // 其它状态（如 FAILED）则重新激活任务
                existingTask.setAccountId(accountId);
                existingTask.setStatus(status);
                existingTask.setCheckCount(0);
                existingTask.setNextCheckTime(nextCheck);
                taskMapper.updateById(existingTask);
            } else {
                DotaParseTask newTask = new DotaParseTask();
                newTask.setAccountId(accountId);
                newTask.setMatchId(matchId);
                newTask.setStatus(status);
                newTask.setCheckCount(0);
                newTask.setNextCheckTime(nextCheck);
                taskMapper.insert(newTask);
            }

            queuedTasks++;
        }

        // 无论是否有新增排队任务，先基于本地已有的比赛（包括队友已同步过的双排/多排比赛）刷新一次该玩家的统计指标
        refreshPlayerMetrics(accountId);

        result.put("totalFound", totalFound);
        result.put("alreadyInDb", alreadyInDb);
        result.put("queuedTasks", queuedTasks);
        result.put("fallback", false);
        return result;
    }

    /**
     * 2. 后台单通道消费：下载并入库高精度录像 JSON
     */
    @Scheduled(fixedDelay = 6000)
    public void pollParseTasks() {
        // 若处于 10 分钟全局熔断冷却期，暂停后台解析与详情拉取，等待满 10 分钟后自动恢复探测
        if (openDotaClient.isCircuitBreakerOpen()) {
            return;
        }

        // 1. 发送解析请求 POST /request（按 match_id 倒序优先请求最新比赛）
        List<DotaParseTask> pendingTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .eq(DotaParseTask::getStatus, "PENDING")
                .le(DotaParseTask::getNextCheckTime, LocalDateTime.now())
                .orderByDesc(DotaParseTask::getMatchId)
                .last("LIMIT 1"));

        for (DotaParseTask task : pendingTasks) {
            if (openDotaClient.isCircuitBreakerOpen()) {
                return;
            }
            String jobId = triggerOpenDotaParse(task.getMatchId());
            if (jobId == null) {
                task.setNextCheckTime(LocalDateTime.now().plusSeconds(45));
                taskMapper.updateById(task);
                continue;
            }
            task.setJobId(jobId);
            task.setStatus("REQUESTED");
            task.setCheckCount(0);
            task.setNextCheckTime(LocalDateTime.now().plusMinutes(2));
            taskMapper.updateById(task);
            log.info("比赛 {} 已提交官方解析队列, jobId={}", task.getMatchId(), jobId);
        }

        // 2. 拉取详情大 JSON（对已到检查时间的任务按 match_id 倒序拉取，确保优先消费最新比赛，且避免 READY_TO_FETCH 饿死 REQUESTED 任务）
        if (openDotaClient.isCircuitBreakerOpen()) {
            return;
        }
        List<DotaParseTask> fetchTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .in(DotaParseTask::getStatus, "READY_TO_FETCH", "REQUESTED")
                .le(DotaParseTask::getNextCheckTime, LocalDateTime.now())
                .orderByDesc(DotaParseTask::getMatchId)
                .last("LIMIT 2"));

        for (DotaParseTask task : fetchTasks) {
            if (openDotaClient.isCircuitBreakerOpen()) {
                break;
            }
            try {
                JsonNode matchRoot = openDotaClient.get("/matches/" + task.getMatchId());

                if (matchRoot == null) {
                    task.setNextCheckTime(LocalDateTime.now().plusSeconds(30));
                    taskMapper.updateById(task);
                    continue;
                }

                // 核心过滤：只有真正包含录像高精数据 (version > 0) 的，才允许入库！
                if (matchRoot.has("version") && matchRoot.path("version").asInt(0) > 0) {
                    saveMatchRawAndDetails(task.getMatchId(), matchRoot);
                    task.setStatus("SUCCESS");
                    taskMapper.updateById(task);

                    // 重新聚合任务发起玩家及本局中所有已关注玩家的统计指标（解决双排/车队共享比赛只刷新单人指标的问题）
                    refreshTrackedPlayersInMatch(task.getAccountId(), matchRoot);
                    log.info("🎉 比赛 {} 高精度录像入库完毕，已更新本地指标", task.getMatchId());
                    continue;
                }

                // 无录像或等待解析中：若原本误判为 READY_TO_FETCH，则退回 PENDING 触发官方解析请求
                if ("READY_TO_FETCH".equals(task.getStatus())) {
                    task.setStatus("PENDING");
                    task.setNextCheckTime(LocalDateTime.now().plusSeconds(10));
                } else {
                    int count = task.getCheckCount() + 1;
                    task.setCheckCount(count);
                    if (count >= 4) {
                        // 超过 4 次依然没有录像数据，彻底废弃，绝不向数据库写入任何脏数据！
                        task.setStatus("FAILED");
                        log.warn("比赛 {} 官方无完整录像，已废弃该比赛", task.getMatchId());
                    } else {
                        task.setNextCheckTime(LocalDateTime.now().plusMinutes(1));
                    }
                }
                taskMapper.updateById(task);

            } catch (Exception e) {
                log.error("下载比赛详情异常 matchId={}: {}", task.getMatchId(), e.getMessage());
            }
        }
    }

    private String triggerOpenDotaParse(Long matchId) {
        try {
            JsonNode body = openDotaClient.post("/request/" + matchId, null);
            if (body != null && body.has("job")) {
                return body.path("job").path("jobId").asText(null);
            }
        } catch (Exception e) {
            log.warn("对比赛 {} 发起解析请求失败: {}", matchId, e.getMessage());
        }
        return null;
    }

    /**
     * 3. 将高精比赛详情落库并打标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMatchRawAndDetails(Long matchId, JsonNode matchRoot) {
        DotaMatchRaw raw = new DotaMatchRaw();
        raw.setMatchId(matchId);
        raw.setRadiantWin(matchRoot.path("radiant_win").asBoolean());
        raw.setDuration(matchRoot.path("duration").asInt());
        raw.setStartTime(matchRoot.path("start_time").asLong());
        raw.setGameMode(matchRoot.path("game_mode").asInt());
        raw.setLobbyType(matchRoot.path("lobby_type").asInt());
        raw.setRawData(matchRoot.toString());
        rawMapper.insertOrUpdate(raw);

        boolean radiantWin = raw.getRadiantWin();
        JsonNode players = matchRoot.path("players");

        if (players.isArray()) {
            // 1. 提取所有 10 名选手的 10 分钟经济与分路
            Map<Integer, Integer> gold10mMap = new HashMap<>(); // playerSlot -> 10min gold
            Map<Integer, Integer> laneMap = new HashMap<>();     // playerSlot -> lane (1优, 2中, 3劣)

            for (JsonNode p : players) {
                int slot = p.path("player_slot").asInt();
                int gold10m = 0;
                JsonNode goldT = p.path("gold_t");
                if (goldT != null && goldT.isArray() && goldT.size() > 10) {
                    gold10m = goldT.get(10).asInt(0);
                } else {
                    gold10m = p.path("gold_per_min").asInt(0) * 10;
                }
                gold10mMap.put(slot, gold10m);
                laneMap.put(slot, mSafeInt(p, "lane"));
            }

            // 2. 【第一轮循环】：找出同一阵营、同一分路里经济最高的大哥 Slot (核心存入)
            Map<String, Integer> laneCoreGoldMap = new HashMap<>();
            Map<String, Integer> laneCoreSlotMap = new HashMap<>();

            for (JsonNode p : players) {
                int slot = p.path("player_slot").asInt();
                boolean isR = slot < 128;
                int lane = laneMap.getOrDefault(slot, 0);
                int gold10 = gold10mMap.getOrDefault(slot, 0);

                String key = (isR ? "R_" : "D_") + lane;
                // 谁在这条线经济最高，谁就是大哥核心
                if (!laneCoreGoldMap.containsKey(key) || gold10 > laneCoreGoldMap.get(key)) {
                    laneCoreGoldMap.put(key, gold10);
                    laneCoreSlotMap.put(key, slot);
                }
            }

            // 3. 计算各条地图分路的 10 分钟经济总和 (OpenDota 中 lane: 1=下路, 2=中路, 3=上路)
            int radiantMidGold = sumLaneGold(players, true, 2, laneMap, gold10mMap);
            int direMidGold = sumLaneGold(players, false, 2, laneMap, gold10mMap);

            // 下路 (lane=1)：天辉优势路(1+5) vs 夜魇劣势路(3+4)
            int radiantBotGold = sumLaneGold(players, true, 1, laneMap, gold10mMap);
            int direBotGold = sumLaneGold(players, false, 1, laneMap, gold10mMap);

            // 上路 (lane=3)：天辉劣势路(3+4) vs 夜魇优势路(1+5)
            int radiantTopGold = sumLaneGold(players, true, 3, laneMap, gold10mMap);
            int direTopGold = sumLaneGold(players, false, 3, laneMap, gold10mMap);

            // 4. 【第二轮循环】：逐个选手计算精准 Pos 1~5 并判定对线胜负，执行入库
            for (JsonNode p : players) {
                long accountId = p.path("account_id").asLong(0);
                int playerSlot = p.path("player_slot").asInt();
                boolean isRadiant = playerSlot < 128;
                boolean win = (isRadiant && radiantWin) || (!isRadiant && !radiantWin);

                int kills = p.path("kills").asInt(0);
                int deaths = p.path("deaths").asInt(0);
                int assists = p.path("assists").asInt(0);
                BigDecimal kda = (deaths == 0) ? BigDecimal.valueOf(kills + assists) :
                        BigDecimal.valueOf((double) (kills + assists) / deaths).setScale(2, RoundingMode.HALF_UP);

                int lane = laneMap.getOrDefault(playerSlot, 0);

                // --- 核心 A：结合阵营与地图上下路精准推导 1~5 号位 ---
                // 天辉下路(lane=1)和夜魇上路(lane=3)为优势路；天辉上路(lane=3)和夜魇下路(lane=1)为劣势路
                int calculatedPos = 2; // 默认中单
                String key = (isRadiant ? "R_" : "D_") + lane;
                boolean isLaneCore = laneCoreSlotMap.getOrDefault(key, -1) == playerSlot;
                boolean isSafeLane = (isRadiant && lane == 1) || (!isRadiant && lane == 3);
                boolean isOffLane = (isRadiant && lane == 3) || (!isRadiant && lane == 1);

                if (lane == 2) {
                    calculatedPos = 2; // 中单 Pos 2
                } else if (isSafeLane) {
                    // 优势路：经济多的是 Pos 1，少的是 Pos 5
                    calculatedPos = isLaneCore ? 1 : 5;
                } else if (isOffLane) {
                    // 劣势路：经济多的是 Pos 3，少的是 Pos 4
                    calculatedPos = isLaneCore ? 3 : 4;
                } else {
                    calculatedPos = 4; // 游走辅助
                }

                // --- 核心 B：同路正面对位 800 经济差判定对线优劣 ---
                int diff = 0;
                if (lane == 2) {
                    diff = isRadiant ? (radiantMidGold - direMidGold) : (direMidGold - radiantMidGold);
                } else if (lane == 1) {
                    // 下路正面对决：天辉下路 vs 夜魇下路
                    diff = isRadiant ? (radiantBotGold - direBotGold) : (direBotGold - radiantBotGold);
                } else if (lane == 3) {
                    // 上路正面对决：天辉上路 vs 夜魇上路
                    diff = isRadiant ? (radiantTopGold - direTopGold) : (direTopGold - radiantTopGold);
                }

                int laneResult = 2; // 默认均势
                if (diff >= 800) {
                    laneResult = 1; // 真实线优
                } else if (diff <= -800) {
                    laneResult = 3; // 真实线劣
                } else {
                    laneResult = 2; // 均势
                }

                List<Integer> items = new ArrayList<>();
                for (int i = 0; i <= 5; i++) {
                    int item = p.path("item_" + i).asInt(0);
                    if (item > 0) items.add(item);
                }

                DotaMatchPlayer detail = new DotaMatchPlayer();
                detail.setMatchId(matchId);
                detail.setAccountId(accountId);
                detail.setPersonaname(p.path("personaname").asText("路人/匿名"));
                detail.setPlayerSlot(playerSlot);
                detail.setIsRadiant(isRadiant);
                detail.setWin(win);
                detail.setHeroId(p.path("hero_id").asInt());
                detail.setKills(kills);
                detail.setDeaths(deaths);
                detail.setAssists(assists);
                detail.setKda(kda);
                detail.setGoldPerMin(p.path("gold_per_min").asInt(0));
                detail.setXpPerMin(p.path("xp_per_min").asInt(0));
                detail.setHeroDamage(p.path("hero_damage").asInt(0));
                detail.setTowerDamage(p.path("tower_damage").asInt(0));
                detail.setLane(lane);

                // 注意这里：必须传入推导出来的 calculatedPos (1~5)，而不是 OpenDota 的原始 lane_role！
                detail.setLaneRole(calculatedPos);

                detail.setLaneEfficiency(BigDecimal.valueOf(p.path("lane_efficiency").asDouble(0.5)).setScale(2, RoundingMode.HALF_UP));
                detail.setLaneResult(laneResult);
                detail.setPartyId(p.has("party_id") ? p.path("party_id").asInt() : null);
                detail.setPartySize(p.path("party_size").asInt(1));

                boolean isMvp = false;
                if (p.has("benchmarks") && p.path("benchmarks").has("gold_per_min")) {
                    isMvp = p.path("benchmarks").path("gold_per_min").path("pct").asDouble(0) > 0.90;
                }
                detail.setIsMvp(isMvp);

                try {
                    detail.setItemsJson(objectMapper.writeValueAsString(items));
                } catch (Exception ignored) {}
                detail.setStartTime(raw.getStartTime());

                matchPlayerMapper.upsert(detail);
            }
        }
    }

    // 辅助方法：快速统计某阵营特定分路（例如天辉 lane=1 的 1+5号位）的 10 分钟经济总和
    private int sumLaneGold(JsonNode players, boolean radiant, int targetLane, Map<Integer, Integer> laneMap, Map<Integer, Integer> gold10mMap) {
        int total = 0;
        for (JsonNode p : players) {
            int slot = p.path("player_slot").asInt();
            boolean isR = slot < 128;
            if (isR == radiant && laneMap.getOrDefault(slot, 0) == targetLane) {
                total += gold10mMap.getOrDefault(slot, 0);
            }
        }
        return total;
    }

    private int mSafeInt(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt(0) : 0;
    }

    public void refreshPlayerMetrics(Long accountId) {
        if (accountId == null || accountId <= 0) return;
        summaryMapper.refreshPlayerSummary(accountId);
        heroStatsMapper.refreshPlayerHeroStats(accountId);
    }

    /**
     * 刷新本局比赛中所有已在本地建档玩家的统计指标（确保双排/车队队友同步受益）
     */
    private void refreshTrackedPlayersInMatch(Long taskAccountId, JsonNode matchRoot) {
        Set<Long> accountsToRefresh = new HashSet<>();
        if (taskAccountId != null && taskAccountId > 0) {
            accountsToRefresh.add(taskAccountId);
        }
        JsonNode players = matchRoot.path("players");
        if (players.isArray()) {
            for (JsonNode p : players) {
                long accId = p.path("account_id").asLong(0);
                if (accId > 0 && summaryMapper.selectById(accId) != null) {
                    accountsToRefresh.add(accId);
                }
            }
        }
        for (Long accId : accountsToRefresh) {
            refreshPlayerMetrics(accId);
        }
    }

    private void syncPlayerProfile(Long accountId) {
        try {
            JsonNode root = openDotaClient.get("/players/" + accountId);
            if (root != null && root.has("profile") && !root.get("profile").isNull()) {
                JsonNode profile = root.get("profile");
                DotaPlayerSummary s = summaryMapper.selectById(accountId);
                if (s == null) {
                    s = new DotaPlayerSummary();
                    s.setAccountId(accountId);
                    s.setIsTracked(0);
                }
                s.setPersonaname(profile.path("personaname").asText("Player#" + accountId));
                s.setAvatar(profile.path("avatarfull").asText(""));
                s.setRankTier(root.path("rank_tier").asInt(0));
                // 提取冠绝一世天梯排名（如 leaderboard_rank: 698 -> 冠绝698）
                int lbRank = (root.has("leaderboard_rank") && !root.get("leaderboard_rank").isNull())
                        ? root.path("leaderboard_rank").asInt(0) : 0;
                s.setLeaderboardRank(lbRank > 0 ? lbRank : null);
                s.setLastSyncAt(LocalDateTime.now());
                summaryMapper.insertOrUpdate(s);
            }
        } catch (Exception e) {
            log.warn("获取玩家头像资料失败 accountId={}: {}", accountId, e.getMessage());
        }
    }

    /**
     * 4. 每 1 小时将所有已关注玩家 (is_tracked = 1) 排入错峰同步队列（绝不并发一起发起请求）
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void scheduleHourlyTrackedPlayersCheck() {
        lastHourlyScheduleTime = LocalDateTime.now();
        nextHourlyScheduleTime = lastHourlyScheduleTime.plusHours(1);

        List<DotaPlayerSummary> trackedList = summaryMapper.selectList(
                new LambdaQueryWrapper<DotaPlayerSummary>().eq(DotaPlayerSummary::getIsTracked, 1)
        );
        if (trackedList == null || trackedList.isEmpty()) {
            return;
        }
        int added = 0;
        for (DotaPlayerSummary p : trackedList) {
            if (p.getAccountId() != null && !hourlySyncQueue.contains(p.getAccountId())) {
                hourlySyncQueue.offer(p.getAccountId());
                added++;
            }
        }
        log.info("⏰ [每小时定时巡检] 已将 {} 名关注玩家加入错峰检查队列，将逐个间隔请求 OpenDota...", added);
    }

    /**
     * 5. 错峰消费关注玩家巡检队列：每 20 秒仅取 1 个关注玩家向 OpenDota 检查是否有新比赛，避免并发请求引发报错
     */
    @Scheduled(fixedDelay = 20000, initialDelay = 20000)
    public void consumeTrackedPlayerSyncQueue() {
        // 若处于 10 分钟全局熔断冷却期，暂不从队列中弹出玩家，等待 10 分钟期满后再取出玩家发起恢复探测
        if (openDotaClient.isCircuitBreakerOpen()) {
            return;
        }

        Long accountId = hourlySyncQueue.poll();
        if (accountId == null) {
            return;
        }
        DotaPlayerSummary summary = summaryMapper.selectById(accountId);
        if (summary != null && Integer.valueOf(1).equals(summary.getIsTracked())) {
            log.info("🔍 [关注玩家错峰巡检] 正在检查玩家 [{}] 是否有新比赛 (队列剩余: {})...", accountId, hourlySyncQueue.size());
            Map<String, Object> syncRes = syncPlayerRecentMatches(accountId);
            // 若本次检查恰好触发了熔断导致失败，将该玩家重新放回队列尾部，等 10 分钟熔断恢复后再重新检查
            if (Boolean.TRUE.equals(syncRes.get("fallback")) && openDotaClient.isCircuitBreakerOpen()) {
                if (!hourlySyncQueue.contains(accountId)) {
                    hourlySyncQueue.offer(accountId);
                }
                log.warn("⏸️ [关注玩家错峰巡检] 玩家 [{}] 检查期间触发全局熔断，已放回巡检队列等待 10 分钟后重试", accountId);
                return;
            }
            log.info("✅ [关注玩家错峰巡检] 玩家 [{}] 检查完毕: 发现近期比赛={}, 已在库={}, 新增入队={}",
                    accountId, syncRes.get("totalFound"), syncRes.get("alreadyInDb"), syncRes.get("queuedTasks"));
        }
    }

    /**
     * 管理员接口：获取所有被关注的玩家列表及定时巡检状态
     */
    public Map<String, Object> getTrackedPlayersStatus() {
        List<DotaPlayerSummary> trackedPlayers = summaryMapper.selectList(
                new LambdaQueryWrapper<DotaPlayerSummary>()
                        .eq(DotaPlayerSummary::getIsTracked, 1)
                        .orderByDesc(DotaPlayerSummary::getUpdatedAt)
        );
        Map<String, Object> res = new HashMap<>();
        res.put("players", trackedPlayers);
        res.put("queueSize", hourlySyncQueue.size());
        res.put("queuedIds", new ArrayList<>(hourlySyncQueue));
        res.put("lastHourlyScheduleTime", lastHourlyScheduleTime);
        res.put("nextHourlyScheduleTime", nextHourlyScheduleTime);
        res.put("circuitBreakerOpen", openDotaClient.isCircuitBreakerOpen());
        res.put("circuitBreakerRemainingSeconds", openDotaClient.getCircuitBreakerRemainingSeconds());
        return res;
    }

    /**
     * 管理员接口：新增关注的玩家 ID，并立即触发一次同步或加入错峰队列
     */
    public Map<String, Object> addTrackedPlayer(Long accountId) {
        return addTrackedPlayer(accountId, null);
    }

    public Map<String, Object> addTrackedPlayer(Long accountId, String remark) {
        String cleanRemark = (remark != null && !remark.trim().isEmpty()) ? remark.trim() : null;
        DotaPlayerSummary existing = summaryMapper.selectById(accountId);
        if (existing == null) {
            existing = new DotaPlayerSummary();
            existing.setAccountId(accountId);
            existing.setPersonaname("Player#" + accountId);
            existing.setRemark(cleanRemark);
            existing.setRankTier(0);
            existing.setTotalMatches(0);
            existing.setTotalWins(0);
            existing.setIsTracked(1);
            existing.setLastSyncAt(LocalDateTime.now());
            summaryMapper.insert(existing);
        } else {
            existing.setIsTracked(1);
            if (cleanRemark != null) {
                existing.setRemark(cleanRemark);
            }
            existing.setLastSyncAt(LocalDateTime.now());
            summaryMapper.updateById(existing);
        }
        // 同步该玩家基本资料与近期比赛
        Map<String, Object> syncRes = syncPlayerRecentMatches(accountId);
        // 确保 is_tracked 与 remark 保持最新
        DotaPlayerSummary updated = summaryMapper.selectById(accountId);
        if (updated != null) {
            boolean needUpdate = false;
            if (!Integer.valueOf(1).equals(updated.getIsTracked())) {
                updated.setIsTracked(1);
                needUpdate = true;
            }
            if (cleanRemark != null && !cleanRemark.equals(updated.getRemark())) {
                updated.setRemark(cleanRemark);
                needUpdate = true;
            }
            if (needUpdate) {
                summaryMapper.updateById(updated);
            }
        }
        Map<String, Object> res = new HashMap<>(syncRes);
        res.put("player", summaryMapper.selectById(accountId));
        return res;
    }

    /**
     * 管理员接口：设置或修改关注玩家的备注名称
     */
    public boolean updateTrackedPlayerRemark(Long accountId, String remark) {
        DotaPlayerSummary existing = summaryMapper.selectById(accountId);
        if (existing == null) {
            return false;
        }
        String cleanRemark = (remark != null && !remark.trim().isEmpty()) ? remark.trim() : null;
        summaryMapper.updatePlayerRemark(accountId, cleanRemark);
        return true;
    }

    /**
     * 管理员接口：取消关注某个玩家 ID
     */
    public boolean removeTrackedPlayer(Long accountId) {
        hourlySyncQueue.remove(accountId);
        DotaPlayerSummary existing = summaryMapper.selectById(accountId);
        if (existing != null) {
            existing.setIsTracked(0);
            summaryMapper.updateById(existing);
            return true;
        }
        return false;
    }

    /**
     * 管理员接口：将所有关注玩家加入错峰队列（逐个间隔 20 秒检查，不并发）
     */
    public int triggerStaggeredCheckAllTracked() {
        scheduleHourlyTrackedPlayersCheck();
        return hourlySyncQueue.size();
    }


    /**
     * 一键重算本地所有历史比赛的对线结果（基于 dota_match_raw 已存 JSON，零网络请求）
     */
    public Map<String, Object> recalculateAllLocalMatches(Long accountId) {
        // 1. 查询本地所有已下载成功的原始比赛 JSON
        List<DotaMatchRaw> rawList = rawMapper.selectList(null);
        int updatedCount = 0;

        for (DotaMatchRaw raw : rawList) {
            try {
                if (raw.getRawData() == null || raw.getRawData().isBlank()) {
                    continue;
                }
                JsonNode matchRoot = objectMapper.readTree(raw.getRawData());

                // 复用最新的 800 经济差逻辑重写该比赛所有选手数据
                saveMatchRawAndDetails(raw.getMatchId(), matchRoot);
                updatedCount++;
            } catch (Exception e) {
                log.error("重算比赛 {} 失败: {}", raw.getMatchId(), e.getMessage());
            }
        }

        // 2. 重新刷新所有已建档玩家及指定玩家的统计聚合指标
        refreshPlayerMetrics(accountId);
        List<DotaPlayerSummary> allPlayers = summaryMapper.selectList(null);
        if (allPlayers != null) {
            for (DotaPlayerSummary s : allPlayers) {
                if (s.getAccountId() != null && !s.getAccountId().equals(accountId)) {
                    refreshPlayerMetrics(s.getAccountId());
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("recalculatedMatches", updatedCount);
        res.put("msg", "重算完成，指标已更新！");
        return res;
    }
}