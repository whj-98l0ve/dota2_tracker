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
    private String avatar;
    private Integer rankTier;
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