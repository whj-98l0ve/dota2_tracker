package com.whj_98l0ve.dota2_tracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whj_98l0ve.dota2_tracker.client.OpenDotaClient;
import com.whj_98l0ve.dota2_tracker.entity.*;
import com.whj_98l0ve.dota2_tracker.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1. 用户触发录入同步：拉取近 7 天列表，筛选并建立高精度录像抓取任务
     */
    public Map<String, Object> syncPlayerRecentMatches(Long accountId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 同步玩家基本资料（只存头像和昵称）
        syncPlayerProfile(accountId);

        // 2. 调用 OpenDota 获取比赛列表
        JsonNode matchesArray = openDotaClient.get("/players/" + accountId + "/matches?date=7");

        if (matchesArray == null || !matchesArray.isArray()) {
            log.warn("⚠️ 获取玩家 [{}] 近期比赛列表网络超时，直接降级读取本地", accountId);
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

            // 超出 7 天截断退出
            if (startTime < exactSevenDaysAgo) {
                break;
            }

            totalFound++;

            // 判定 1：本地数据库已有此高精度比赛，严格跳过！
            if (rawMapper.selectById(matchId) != null) {
                alreadyInDb++;
                continue;
            }

            // 判定 2：查询任务表状态
            DotaParseTask existingTask = taskMapper.selectOne(new LambdaQueryWrapper<DotaParseTask>()
                    .eq(DotaParseTask::getMatchId, matchId)
                    .last("LIMIT 1"));

            if (existingTask != null && Arrays.asList("PENDING", "READY_TO_FETCH", "REQUESTED").contains(existingTask.getStatus())) {
                continue;
            }

            // 判定 3：检查官方是否已解析 (version > 0)
            boolean hasParsedByOpenDota = m.has("version") && m.path("version").asInt(0) > 0;
            String status = hasParsedByOpenDota ? "READY_TO_FETCH" : "PENDING";
            LocalDateTime nextCheck = LocalDateTime.now();

            if (existingTask != null) {
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
        // 1. 发送解析请求 POST /request
        List<DotaParseTask> pendingTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .eq(DotaParseTask::getStatus, "PENDING")
                .le(DotaParseTask::getNextCheckTime, LocalDateTime.now())
                .last("LIMIT 1"));

        for (DotaParseTask task : pendingTasks) {
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

        // 2. 拉取详情大 JSON
        List<DotaParseTask> fetchTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .in(DotaParseTask::getStatus, "READY_TO_FETCH", "REQUESTED")
                .le(DotaParseTask::getNextCheckTime, LocalDateTime.now())
                .last("LIMIT 2"));

        for (DotaParseTask task : fetchTasks) {
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

                    // 重新聚合该玩家在数据库中的分析统计指标
                    refreshPlayerMetrics(task.getAccountId());
                    log.info("🎉 比赛 {} 高精度录像入库完毕，已更新本地指标", task.getMatchId());
                    continue;
                }

                // 无录像或等待解析中
                if ("READY_TO_FETCH".equals(task.getStatus())) {
                    task.setStatus("REQUESTED");
                    task.setNextCheckTime(LocalDateTime.now().plusSeconds(30));
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

            // 3. 计算各条线的经济总和 (用于判定对线 800 经济差)
            int radiantMidGold = sumLaneGold(players, true, 2, laneMap, gold10mMap);
            int direMidGold = sumLaneGold(players, false, 2, laneMap, gold10mMap);

            // 天辉优势路(1+5) vs 夜宴劣势路(3+4)
            int radiantSafeGold = sumLaneGold(players, true, 1, laneMap, gold10mMap);
            int direOffGold = sumLaneGold(players, false, 3, laneMap, gold10mMap);

            // 天辉劣势路(3+4) vs 夜宴优势路(1+5)
            int radiantOffGold = sumLaneGold(players, true, 3, laneMap, gold10mMap);
            int direSafeGold = sumLaneGold(players, false, 1, laneMap, gold10mMap);

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

                // --- 核心 A：精准推导 1~5 号位 ---
                int calculatedPos = 2; // 默认中单
                String key = (isRadiant ? "R_" : "D_") + lane;
                boolean isLaneCore = laneCoreSlotMap.getOrDefault(key, -1) == playerSlot;

                if (lane == 2) {
                    calculatedPos = 2; // 中单 Pos 2
                } else if (lane == 1) {
                    // 优势路：经济多的是 Pos 1，少的是 Pos 5
                    calculatedPos = isLaneCore ? 1 : 5;
                } else if (lane == 3) {
                    // 劣势路：经济多的是 Pos 3，少的是 Pos 4
                    calculatedPos = isLaneCore ? 3 : 4;
                } else {
                    calculatedPos = 4; // 游走辅助
                }

                // --- 核心 B：800 经济差判定对线优劣 ---
                int diff = 0;
                if (lane == 2) {
                    diff = isRadiant ? (radiantMidGold - direMidGold) : (direMidGold - radiantMidGold);
                } else if ((isRadiant && lane == 1) || (!isRadiant && lane == 3)) {
                    diff = isRadiant ? (radiantSafeGold - direOffGold) : (direOffGold - radiantSafeGold);
                } else if ((isRadiant && lane == 3) || (!isRadiant && lane == 1)) {
                    diff = isRadiant ? (radiantOffGold - direSafeGold) : (direSafeGold - radiantOffGold);
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

    private void syncPlayerProfile(Long accountId) {
        try {
            JsonNode root = openDotaClient.get("/players/" + accountId);
            if (root != null && root.has("profile") && !root.get("profile").isNull()) {
                JsonNode profile = root.get("profile");
                DotaPlayerSummary s = summaryMapper.selectById(accountId);
                if (s == null) {
                    s = new DotaPlayerSummary();
                    s.setAccountId(accountId);
                }
                s.setPersonaname(profile.path("personaname").asText("Player#" + accountId));
                s.setAvatar(profile.path("avatarfull").asText(""));
                s.setRankTier(root.path("rank_tier").asInt(0));
                summaryMapper.insertOrUpdate(s);
            }
        } catch (Exception e) {
            log.warn("获取玩家头像资料失败 accountId={}: {}", accountId, e.getMessage());
        }
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

        // 2. 重新刷新该玩家的全部统计聚合指标
        refreshPlayerMetrics(accountId);

        Map<String, Object> res = new HashMap<>();
        res.put("recalculatedMatches", updatedCount);
        res.put("msg", "重算完成，指标已更新！");
        return res;
    }
}