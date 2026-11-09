package com.whj_98l0ve.dota2_tracker;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whj_98l0ve.dota2_tracker.entity.DotaMatchRaw;
import com.whj_98l0ve.dota2_tracker.entity.DotaParseTask;
import com.whj_98l0ve.dota2_tracker.mapper.DotaMatchRawMapper;
import com.whj_98l0ve.dota2_tracker.mapper.DotaParseTaskMapper;
import com.whj_98l0ve.dota2_tracker.service.OpenDotaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class Dota2TrackerApplicationTests {

    @Autowired
    private OpenDotaService openDotaService;

    @Autowired
    private DotaParseTaskMapper taskMapper;

    @Autowired
    private DotaMatchRawMapper rawMapper;

    @Test
    @DisplayName("全链路测试：静默等待后台把剩余排队任务全部解析并拉取入库")
    void testSyncPlayerRecentMatchesAndCompleteParse() throws InterruptedException {
        Long testAccountId = 182595756L;

        // 1. 发起同步（增量判定）
        System.out.println("====== [阶段 1] 触发近 7 天比赛同步与检查 ======");
        Map<String, Object> result = openDotaService.syncPlayerRecentMatches(testAccountId);
        assertNotNull(result);

        int totalFound = (int) result.get("totalFound");
        int alreadyInDb = (int) result.get("alreadyInDb");
        int queuedTasks = (int) result.get("queuedTasks");

        System.out.println("近 7 天总比赛数: " + totalFound);
        System.out.println("数据库已存在（跳过）: " + alreadyInDb);
        System.out.println("本次新进入队列数: " + queuedTasks);

        // 2. 检查任务表中该玩家是否还有未完结的任务
        Long activeTasks = taskMapper.selectCount(new LambdaQueryWrapper<DotaParseTask>()
                .eq(DotaParseTask::getAccountId, testAccountId)
                .in(DotaParseTask::getStatus, "PENDING", "READY_TO_FETCH", "REQUESTED"));

        System.out.println(">>> 任务表中当前正在排队/等待中的任务总数: " + activeTasks);

        if (activeTasks == 0) {
            System.out.println("🎉 所有比赛均已解析完毕且数据完整在库，无需等待！");
            return;
        }

        // 3. 静默轮询等待后台 scheduling 线程单通道消费
        System.out.println("====== [阶段 2] 保持进程存活，等待后台线程下载入库 ======");
        int maxLoop = 120; // 36 * 10秒 = 6分钟（给足 OpenDota 解析与排队重试时间）

        for (int i = 1; i <= maxLoop; i++) {
            Thread.sleep(10000); // 主线程每 10 秒查一次任务表状态

            Long remaining = taskMapper.selectCount(new LambdaQueryWrapper<DotaParseTask>()
                    .eq(DotaParseTask::getAccountId, testAccountId)
                    .in(DotaParseTask::getStatus, "PENDING", "READY_TO_FETCH", "REQUESTED"));

            System.out.printf(">>> [%d/%d] 后台处理中... 当前该玩家剩余未完成任务: %d 条%n", i, maxLoop, remaining);

            if (remaining == 0) {
                System.out.println("🎉 恭喜！后台已将该批次所有排队比赛处理完毕！");
                break;
            }
        }

        // 4. 最终校验落库结果
        System.out.println("====== [阶段 3] 校验 ODS 原始表入库数量 ======");
        List<DotaParseTask> successTasks = taskMapper.selectList(new LambdaQueryWrapper<DotaParseTask>()
                .eq(DotaParseTask::getAccountId, testAccountId)
                .eq(DotaParseTask::getStatus, "SUCCESS"));

        System.out.println("成功完成深度解析的任务数: " + successTasks.size());
        for (DotaParseTask task : successTasks) {
            DotaMatchRaw raw = rawMapper.selectById(task.getMatchId());
            assertNotNull(raw, "ODS 原始表必须存在 matchId=" + task.getMatchId());
            assertNotNull(raw.getRawData());
        }
    }
}