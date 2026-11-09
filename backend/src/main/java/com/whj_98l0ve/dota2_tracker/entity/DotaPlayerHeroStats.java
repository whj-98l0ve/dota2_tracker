package com.whj_98l0ve.dota2_tracker.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dota_player_hero_stats")
public class DotaPlayerHeroStats {
    @TableId
    private Long accountId;
    private Integer heroId;
    private Integer matches;
    private Integer wins;
    private Integer losses;
    private BigDecimal winRate;
    private BigDecimal avgKills;
    private BigDecimal avgDeaths;
    private BigDecimal avgAssists;
    private BigDecimal kda;
    private BigDecimal laneWinRate;
    private BigDecimal laneLossRate;
    private Integer avgGpm;
    private Integer avgXpm;
    private BigDecimal perfScore;
    private Long lastPlayedTime;
    private LocalDateTime updatedAt;
}