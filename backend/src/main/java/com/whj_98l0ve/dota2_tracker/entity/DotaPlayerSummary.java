package com.whj_98l0ve.dota2_tracker.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dota_player_summary")
public class DotaPlayerSummary {
    @TableId
    private Long accountId;
    private String personaname;
    /** 管理员为玩家设置的备注名（如群昵称/真名） */
    private String remark;
    private String avatar;
    private Integer rankTier;
    /** 冠绝一世天梯具体排名（如 698 表示冠绝698） */
    private Integer leaderboardRank;
    /** 是否为管理员关注的定时同步玩家（1: 关注, 0: 未关注） */
    private Integer isTracked;
    /** 最近一次向 OpenDota 检查同步的时间 */
    private LocalDateTime lastSyncAt;
    private Integer totalMatches;
    private Integer totalWins;
    private BigDecimal winRate;
    private BigDecimal avgKda;
    private BigDecimal avgKills;
    private BigDecimal avgDeaths;
    private BigDecimal avgAssists;
    private Integer avgGpm;
    private Integer avgXpm;
    private Integer radiantMatches;
    private Integer radiantWins;
    private Integer direMatches;
    private Integer direWins;
    private BigDecimal laneWinRate;
    private BigDecimal soloWinRate;
    private BigDecimal duoTrioWinRate;
    private BigDecimal fiveStackWinRate;
    private LocalDateTime updatedAt;
}