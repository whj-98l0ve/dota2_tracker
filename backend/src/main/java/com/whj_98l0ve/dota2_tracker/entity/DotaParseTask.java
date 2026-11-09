package com.whj_98l0ve.dota2_tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dota_parse_task")
public class DotaParseTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private Long matchId;
    private String jobId;
    private String status; // PENDING, REQUESTED, SUCCESS, FAILED
    private Integer checkCount;
    private LocalDateTime nextCheckTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}