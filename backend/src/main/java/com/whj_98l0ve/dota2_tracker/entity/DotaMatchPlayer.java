package com.whj_98l0ve.dota2_tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dota_match_player")
public class DotaMatchPlayer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long matchId;
    private Long accountId;
    private String personaname;
    private Integer playerSlot;
    private Boolean isRadiant;
    private Boolean win;
    private Integer heroId;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private BigDecimal kda;
    private Integer goldPerMin;
    private Integer xpPerMin;
    private Integer heroDamage;
    private Integer towerDamage;
    private Integer lane;
    private Integer laneRole;
    private BigDecimal laneEfficiency;
    private Integer laneResult; // 1优 2均 3劣
    private Integer partyId;
    private Integer partySize;
    private Boolean isMvp;
    private String itemsJson;
    private Long startTime;
    private LocalDateTime createdAt;
}