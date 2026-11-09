package com.whj_98l0ve.dota2_tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whj_98l0ve.dota2_tracker.entity.*;
import com.whj_98l0ve.dota2_tracker.mapper.*;
import com.whj_98l0ve.dota2_tracker.service.OpenDotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    private final DotaParseTaskMapper taskMapper;

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
        long pending = tasks.stream().filter(t -> "PENDING".equals(t.getStatus()) || "REQUESTED".equals(t.getStatus())).count();
        long success = tasks.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();

        Map<String, Object> res = new HashMap<>();
        res.put("total", tasks.size());
        res.put("pending", pending);
        res.put("success", success);
        res.put("tasks", tasks);
        return res;
    }

    /**
     * 3. 看板完整聚合数据接口
     */
    @GetMapping("/{accountId}/dashboard")
    public Map<String, Object> getDashboardData(@PathVariable Long accountId) {
        DotaPlayerSummary summary = summaryMapper.selectById(accountId);
        List<DotaPlayerHeroStats> topHeroes = heroStatsMapper.selectTopHeroes(accountId, 15);
        List<Map<String, Object>> teammates = matchPlayerMapper.selectTeammateSynergy(accountId);

        // 最近 10 场明细
        List<DotaMatchPlayer> recentMatches = matchPlayerMapper.selectList(new LambdaQueryWrapper<DotaMatchPlayer>()
                .eq(DotaMatchPlayer::getAccountId, accountId)
                .orderByDesc(DotaMatchPlayer::getStartTime)
                .last("LIMIT 10"));

        Map<String, Object> res = new HashMap<>();
        res.put("summary", summary);
        res.put("topHeroes", topHeroes);
        res.put("teammates", teammates);
        res.put("recentMatches", recentMatches);
        return res;
    }
}