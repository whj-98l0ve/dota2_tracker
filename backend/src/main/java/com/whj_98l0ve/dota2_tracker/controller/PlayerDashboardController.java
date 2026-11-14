package com.whj_98l0ve.dota2_tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whj_98l0ve.dota2_tracker.entity.*;
import com.whj_98l0ve.dota2_tracker.mapper.*;
import com.whj_98l0ve.dota2_tracker.service.OpenDotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/player")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PlayerDashboardController {

    private final OpenDotaService openDotaService;
    private final DotaPlayerSummaryMapper summaryMapper;
    private final DotaPlayerHeroStatsMapper heroStatsMapper;
    private final DotaMatchPlayerMapper matchPlayerMapper;
    private final DotaMatchRawMapper matchRawMapper;
    private final DotaParseTaskMapper taskMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 0. 网站首页聚合数据接口（已同步总比赛数、关注玩家去重开黑总场数、最近20天每日开黑/单排趋势、关注玩家身份列表）
     */
    @GetMapping("/home")
    public Map<String, Object> getHomeOverview() {
        long totalSyncedMatches = matchRawMapper.selectCount(null);
        Map<String, Object> typeTotals = matchPlayerMapper.selectTrackedMatchTypeTotals();
        long totalPartyMatches = typeTotals != null && typeTotals.get("totalPartyMatches") != null
                ? ((Number) typeTotals.get("totalPartyMatches")).longValue() : 0L;
        long totalSoloMatches = typeTotals != null && typeTotals.get("totalSoloMatches") != null
                ? ((Number) typeTotals.get("totalSoloMatches")).longValue() : 0L;

        // 构造最近 20 天的连续日期趋势数据（含无比赛日期补 0）
        List<Map<String, Object>> rawDaily = matchPlayerMapper.selectTrackedDailyActivity();
        Map<String, Map<String, Object>> dailyMap = new HashMap<>();
        if (rawDaily != null) {
            for (Map<String, Object> row : rawDaily) {
                String d = String.valueOf(row.get("matchDate"));
                dailyMap.put(d, row);
            }
        }

        List<Map<String, Object>> daily20List = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fullFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter shortFmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 19; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = date.format(fullFmt);
            Map<String, Object> found = dailyMap.get(dateKey);
            int partyGames = found != null && found.get("partyGames") != null
                    ? ((Number) found.get("partyGames")).intValue() : 0;
            int soloGames = found != null && found.get("soloGames") != null
                    ? ((Number) found.get("soloGames")).intValue() : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("date", dateKey);
            item.put("shortDate", date.format(shortFmt));
            item.put("partyGames", partyGames);
            item.put("soloGames", soloGames);
            daily20List.add(item);
        }

        List<Map<String, Object>> trackedPlayers = matchPlayerMapper.selectTrackedPlayersWithLastMatch();

        Map<String, Object> res = new HashMap<>();
        res.put("totalSyncedMatches", totalSyncedMatches);
        res.put("totalPartyMatches", totalPartyMatches);
        res.put("totalSoloMatches", totalSoloMatches);
        res.put("dailyActivity", daily20List);
        res.put("trackedPlayers", trackedPlayers);
        return res;
    }

    /**
     * 1. 录入玩家 ID 并启动 7 天对局同步与解析流
     */
    @PostMapping("/{accountId}/sync")
    public Map<String, Object> triggerSync(@PathVariable Long accountId) {
        return openDotaService.syncPlayerRecentMatches(accountId);
    }

    /**
     * 2. 查询当前录入与解析任务状态 (供前端进度条轮询)
     */
    @GetMapping("/{accountId}/tasks")
    public Map<String, Object> getTaskStatus(@PathVariable Long accountId) {
        List<DotaParseTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .eq(DotaParseTask::getAccountId, accountId)
                .orderByDesc(DotaParseTask::getId));
        long pending = tasks.stream()
                .filter(t -> "PENDING".equals(t.getStatus()) || "READY_TO_FETCH".equals(t.getStatus()) || "REQUESTED".equals(t.getStatus()))
                .count();
        long success = tasks.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();

        Map<String, Object> res = new HashMap<>();
        res.put("total", tasks.size());
        res.put("pending", pending);
        res.put("success", success);
        res.put("tasks", tasks);
        return res;
    }

    /**
     * 3. 看板完整聚合数据接口 (纯本地数据库真实数据聚合)
     */
    @GetMapping("/{accountId}/dashboard")
    public Map<String, Object> getDashboardData(@PathVariable Long accountId) {
        DotaPlayerSummary summary = summaryMapper.selectById(accountId);
        // 查询该玩家使用过的全部英雄全维统计（按使用场次从高到低排序，包含天辉/夜魇胜率、对线三态分布、最近比赛时间）
        List<Map<String, Object>> topHeroes = matchPlayerMapper.selectHeroDetailedStats(accountId);
        List<Map<String, Object>> teammates = matchPlayerMapper.selectTeammateSynergy(accountId);

        // 1. 各分路表现 Pos 1 ~ 5 (纯本地)
        List<Map<String, Object>> positionStats = matchPlayerMapper.selectPositionStats(accountId);

        // 2. 组队模式对比 Solo / Duo / 5-Stack (纯本地)
        List<Map<String, Object>> partyStats = matchPlayerMapper.selectPartyStats(accountId);

        // 3. 阵营胜率分布 (纯本地)
        List<Map<String, Object>> factionStats = matchPlayerMapper.selectFactionStats(accountId);

        // 4. 最近 10 场完整明细
        List<DotaMatchPlayer> recentMatches = matchPlayerMapper.selectList(new LambdaQueryWrapper<DotaMatchPlayer>()
                .eq(DotaMatchPlayer::getAccountId, accountId)
                .orderByDesc(DotaMatchPlayer::getStartTime)
                .last("LIMIT 10"));

        Map<String, Object> res = new HashMap<>();
        res.put("summary", summary);
        res.put("topHeroes", topHeroes);
        res.put("teammates", teammates);
        res.put("positionStats", positionStats);
        res.put("partyStats", partyStats);
        res.put("factionStats", factionStats);
        res.put("recentMatches", recentMatches);
        return res;
    }
//curl -X POST http://localhost:8088/api/player/199666669/recalculate gitbash重算
    @PostMapping("/{accountId}/recalculate")
    public Map<String, Object> recalculate(@PathVariable Long accountId) {
        return openDotaService.recalculateAllLocalMatches(accountId);
    }

    /**
     * 4. 查询玩家全量历史战绩 (支持分页)
     */
    @GetMapping("/{accountId}/matches")
    public List<DotaMatchPlayer> getAllMatches(@PathVariable Long accountId) {
        return matchPlayerMapper.selectList(new LambdaQueryWrapper<DotaMatchPlayer>()
                .eq(DotaMatchPlayer::getAccountId, accountId)
                .orderByDesc(DotaMatchPlayer::getStartTime));
    }

    /**
     * 5. 查询单场比赛详情 (包括比赛概况、已解析的完整原始 JSON 节点和所有玩家计算指标)
     */
    @GetMapping("/match/{matchId}")
    public Map<String, Object> getMatchDetail(@PathVariable Long matchId) {
        DotaMatchRaw raw = matchRawMapper.selectById(matchId);
        List<DotaMatchPlayer> players = matchPlayerMapper.selectList(new LambdaQueryWrapper<DotaMatchPlayer>()
                .eq(DotaMatchPlayer::getMatchId, matchId)
                .orderByAsc(DotaMatchPlayer::getPlayerSlot));

        Map<String, Object> res = new HashMap<>();
        res.put("match", raw);
        res.put("players", players);
        // 直接将数据库保存的高精度原始 JSON 解析为结构化对象返回给前端，便于前端多维战术面板直接渲染
        if (raw != null && raw.getRawData() != null && !raw.getRawData().isBlank()) {
            try {
                JsonNode rawJson = objectMapper.readTree(raw.getRawData());
                res.put("rawJson", rawJson);
            } catch (Exception ignored) {
            }
        }
        return res;
    }
}