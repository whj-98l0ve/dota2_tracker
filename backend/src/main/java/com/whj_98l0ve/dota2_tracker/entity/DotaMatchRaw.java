package com.whj_98l0ve.dota2_tracker.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dota_match_raw")
public class DotaMatchRaw {
    @TableId
    private Long matchId;
    private Boolean radiantWin;
    private Integer duration;
    private Long startTime;
    private Integer gameMode;
    private Integer lobbyType;
    private String rawData;
    private LocalDateTime createdAt;
}