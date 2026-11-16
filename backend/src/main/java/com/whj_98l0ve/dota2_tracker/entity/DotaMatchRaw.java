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
    /** DeepSeek AI 对本场比赛败方“到底谁的问题？”的深度复盘分析结果（JSON 或文本） */
    private String aiAnalysis;
    /** DeepSeek AI 对本场比赛胜方“到底谁是天降猛男！”的深度复盘分析结果（JSON 或文本） */
    private String aiMvpAnalysis;
    private LocalDateTime createdAt;
}