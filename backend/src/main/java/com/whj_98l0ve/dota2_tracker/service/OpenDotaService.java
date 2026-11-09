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
     * 1. 用户触发同步：拉取近 7 天列表，快速判定是否在库与解析状态，构建流水线任务
     */
    public Map<String, Object> syncPlayerRecentMatches(Long accountId) {
        // 1. 同步玩家基本资料
        syncPlayerProfile(accountId);

        // 2. 调用 OpenDota 获取比赛列表
        JsonNode matchesArray = openDotaClient.get("/players/" + accountId + "/matches?date=7");

        int totalFound = 0;
        int alreadyInDb = 0;
        int queuedTasks = 0;

        if (matchesArray != null && matchesArray.isArray()) {
            // 计算精确的 7 天前时间戳（秒）
            long exactSevenDaysAgo = Instant.now().minusSeconds(7L * 24 * 3600).getEpochSecond();

            for (JsonNode m : matchesArray) {
                long matchId = m.path("match_id").asLong();
                long startTime = m.path("start_time").asLong(0);

                // 核心截断：OpenDota 返回列表是倒序的，超出 7 天直接终止后续处理
                if (startTime < exactSevenDaysAgo) {
                    log.info("比赛 {} 开始时间早于 7 天前，截断退出", matchId);
                    break;
                }

                totalFound++;

                // 写入/更新基础战绩（保证前端看板秒级出数据）
                saveBasicMatchPlayer(accountId, matchId, m);

                // 判定 1：原始数据表已有，证明已彻底同步完成，严格跳过！
                if (rawMapper.selectById(matchId) != null) {
                    alreadyInDb++;
                    continue;
                }

                // 判定 2：查询任务表历史记录
                DotaParseTask existingTask = taskMapper.selectOne(new LambdaQueryWrapper<DotaParseTask>()
                        .eq(DotaParseTask::getMatchId, matchId)
                        .last("LIMIT 1"));

                // 正在正常排队或解析中的任务，跳过以防重复处理
                if (existingTask != null && Arrays.asList("PENDING", "READY_TO_FETCH", "REQUESTED").contains(existingTask.getStatus())) {
                    continue;
                }

                // 判定 3：检查官方是否已解析 (version > 0)
                boolean hasParsedByOpenDota = m.has("version") && m.path("version").asInt(0) > 0;
                // 官方已解析的直接准备下载，没解析的先存 PENDING 由后台排队去发 POST，主线程绝不发网络请求
                String status = hasParsedByOpenDota ? "READY_TO_FETCH" : "PENDING";
                LocalDateTime nextCheck = LocalDateTime.now();

                if (existingTask != null) {
                    // 历史失败任务重置激活
                    existingTask.setStatus(status);
                    existingTask.setCheckCount(0);
                    existingTask.setNextCheckTime(nextCheck);
                    taskMapper.updateById(existingTask);
                } else {
                    // 新任务入库
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
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalFound", totalFound);
        result.put("alreadyInDb", alreadyInDb);
        result.put("queuedTasks", queuedTasks);
        return result;
    }
    /**
     * 2. 后台单通道消费：拉取比赛详情 JSON 并存入数据库
     */
    @Scheduled(fixedDelay = 6000)
    public void pollParseTasks() {
        // 1. 每次只发 1 个 POST /request（细水长流，OpenDota 对 POST 容忍度低）
        List<DotaParseTask> pendingTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .eq(DotaParseTask::getStatus, "PENDING")
                .le(DotaParseTask::getNextCheckTime, LocalDateTime.now())
                .last("LIMIT 1"));

        for (DotaParseTask task : pendingTasks) {
            String jobId = triggerOpenDotaParse(task.getMatchId());
            if (jobId == null) {
                // 说明发 POST 时遭遇了临时网络超时，保持 PENDING，20秒后换个时机再发
                log.warn("比赛 {} 提交官方解析网络超时，稍后重新提交", task.getMatchId());
                task.setNextCheckTime(LocalDateTime.now().plusSeconds(20));
                taskMapper.updateById(task);
                continue;
            }
            // 成功拿到 jobId，才正规转为 REQUESTED 并等 2 分钟
            task.setJobId(jobId);
            task.setStatus("REQUESTED");
            task.setCheckCount(0);
            task.setNextCheckTime(LocalDateTime.now().plusMinutes(2)); // 精准等 2 分钟
            taskMapper.updateById(task);
            log.info("比赛 {} 已在后台异步提交官方解析队列, jobId={}", task.getMatchId(), jobId);
        }

        // 2. 每次拉取 2 个已就绪的详情 JSON
        List<DotaParseTask> fetchTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .in(DotaParseTask::getStatus, "READY_TO_FETCH", "REQUESTED")
                .le(DotaParseTask::getNextCheckTime, LocalDateTime.now())
                .last("LIMIT 2"));

        for (DotaParseTask task : fetchTasks) {
            try {
                JsonNode matchRoot = openDotaClient.get("/matches/" + task.getMatchId());

                if (matchRoot == null) {
                    // 网络波动超时，20秒后重试
                    task.setNextCheckTime(LocalDateTime.now().plusSeconds(20));
                    taskMapper.updateById(task);
                    continue;
                }

                // 成功下载大 JSON
                if (matchRoot.has("version") && matchRoot.path("version").asInt(0) > 0) {
                    saveMatchRawAndDetails(task.getMatchId(), matchRoot);
                    task.setStatus("SUCCESS");
                    taskMapper.updateById(task);
                    refreshPlayerMetrics(task.getAccountId());
                    log.info("🎉 比赛 {} 详细 JSON 下载并入库成功！", task.getMatchId());
                    continue;
                }

                // 返回了数据但没录像
                if ("READY_TO_FETCH".equals(task.getStatus())) {
                    task.setStatus("REQUESTED");
                    task.setNextCheckTime(LocalDateTime.now().plusSeconds(30));
                } else {
                    int count = task.getCheckCount() + 1;
                    task.setCheckCount(count);
                    if (count >= 4) {
                        task.setStatus("FAILED");
                        log.warn("比赛 {} 多次轮询确无录像数据，标记为失败", task.getMatchId());
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

    private void saveBasicMatchPlayer(Long accountId, Long matchId, JsonNode m) {
        try {
            int playerSlot = m.path("player_slot").asInt(0);
            boolean isRadiant = playerSlot < 128;
            int radiantWinInt = m.path("radiant_win").asInt(-1);
            boolean radiantWin = radiantWinInt == 1;
            boolean win = (isRadiant && radiantWin) || (!isRadiant && !radiantWin);

            int kills = m.path("kills").asInt(0);
            int deaths = m.path("deaths").asInt(0);
            int assists = m.path("assists").asInt(0);
            BigDecimal kda = (deaths == 0) ? BigDecimal.valueOf(kills + assists) :
                    BigDecimal.valueOf((double) (kills + assists) / deaths).setScale(2, RoundingMode.HALF_UP);

            DotaMatchPlayer detail = new DotaMatchPlayer();
            detail.setMatchId(matchId);
            detail.setAccountId(accountId);
            detail.setPersonaname("我");
            detail.setPlayerSlot(playerSlot);
            detail.setIsRadiant(isRadiant);
            detail.setWin(win);
            detail.setHeroId(m.path("hero_id").asInt(0));
            detail.setKills(kills);
            detail.setDeaths(deaths);
            detail.setAssists(assists);
            detail.setKda(kda);
            detail.setGoldPerMin(m.path("gold_per_min").asInt(0));
            detail.setXpPerMin(m.path("xp_per_min").asInt(0));
            detail.setHeroDamage(m.path("hero_damage").asInt(0));
            detail.setTowerDamage(m.path("tower_damage").asInt(0));
            detail.setLane(m.path("lane").asInt(0));
            detail.setLaneRole(m.path("lane_role").asInt(0));
            detail.setLaneEfficiency(BigDecimal.valueOf(0.50));
            detail.setLaneResult(2);
            detail.setPartySize(m.path("party_size").asInt(1));
            detail.setStartTime(m.path("start_time").asLong(0));

            matchPlayerMapper.upsert(detail);
        } catch (Exception e) {
            log.warn("基础对局数据预写入失败 matchId={}: {}", matchId, e.getMessage());
        }
    }

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

                double efficiency = p.path("lane_efficiency").asDouble(0.5);
                int laneResult = 2;
                if (efficiency >= 0.65) laneResult = 1;
                else if (efficiency <= 0.45) laneResult = 3;

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
                detail.setLane(p.path("lane").asInt(0));
                detail.setLaneRole(p.path("lane_role").asInt(0));
                detail.setLaneEfficiency(BigDecimal.valueOf(efficiency).setScale(2, RoundingMode.HALF_UP));
                detail.setLaneResult(laneResult);
                detail.setPartyId(p.has("party_id") ? p.path("party_id").asInt() : null);
                detail.setPartySize(p.path("party_size").asInt(1));
                detail.setIsMvp(p.path("benchmarks").path("gold_per_min").path("pct").asDouble(0) > 0.95);
                try {
                    detail.setItemsJson(objectMapper.writeValueAsString(items));
                } catch (Exception ignored) {}
                detail.setStartTime(raw.getStartTime());

                matchPlayerMapper.upsert(detail);
            }
        }
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
}