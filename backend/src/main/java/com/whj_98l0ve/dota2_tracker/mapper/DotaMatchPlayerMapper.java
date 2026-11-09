package com.whj_98l0ve.dota2_tracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whj_98l0ve.dota2_tracker.entity.DotaMatchPlayer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DotaMatchPlayerMapper extends BaseMapper<DotaMatchPlayer> {

    /**
     * 解决唯一索引 uk_match_account 冲突问题：存在则更新，不存在则插入
     */
    @Insert("""
        INSERT INTO dota_match_player (
            match_id, account_id, personaname, player_slot, is_radiant, win, 
            hero_id, kills, deaths, assists, kda, gold_per_min, xp_per_min, 
            hero_damage, tower_damage, lane, lane_role, lane_efficiency, lane_result, 
            party_id, party_size, is_mvp, items_json, start_time
        ) VALUES (
            #{p.matchId}, #{p.accountId}, #{p.personaname}, #{p.playerSlot}, #{p.isRadiant}, #{p.win}, 
            #{p.heroId}, #{p.kills}, #{p.deaths}, #{p.assists}, #{p.kda}, #{p.goldPerMin}, #{p.xpPerMin}, 
            #{p.heroDamage}, #{p.towerDamage}, #{p.lane}, #{p.laneRole}, #{p.laneEfficiency}, #{p.laneResult}, 
            #{p.partyId}, #{p.partySize}, #{p.isMvp}, #{p.itemsJson}, #{p.startTime}
        )
        ON DUPLICATE KEY UPDATE
            personaname = VALUES(personaname),
            player_slot = VALUES(player_slot),
            is_radiant = VALUES(is_radiant),
            win = VALUES(win),
            hero_id = VALUES(hero_id),
            kills = VALUES(kills),
            deaths = VALUES(deaths),
            assists = VALUES(assists),
            kda = VALUES(kda),
            gold_per_min = VALUES(gold_per_min),
            xp_per_min = VALUES(xp_per_min),
            hero_damage = VALUES(hero_damage),
            tower_damage = VALUES(tower_damage),
            lane = VALUES(lane),
            lane_role = VALUES(lane_role),
            lane_efficiency = VALUES(lane_efficiency),
            lane_result = VALUES(lane_result),
            party_id = VALUES(party_id),
            party_size = VALUES(party_size),
            is_mvp = VALUES(is_mvp),
            items_json = VALUES(items_json),
            start_time = VALUES(start_time)
    """)
    int upsert(@Param("p") DotaMatchPlayer p);

    // 队友合拍红黑榜 SQL 自连接查询
    @Select("""
        SELECT 
            b.account_id AS teammateId,
            b.personaname AS teammateName,
            COUNT(1) AS matchesTogether,
            SUM(CASE WHEN b.win = 1 THEN 1 ELSE 0 END) AS winsTogether,
            ROUND(SUM(CASE WHEN b.win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS winRate
        FROM dota_match_player a
        JOIN dota_match_player b 
          ON a.match_id = b.match_id 
         AND a.is_radiant = b.is_radiant
         AND a.account_id != b.account_id
        WHERE a.account_id = #{accountId} 
          AND b.account_id > 0
        GROUP BY b.account_id, b.personaname
        HAVING matchesTogether >= 2
        ORDER BY winRate DESC, matchesTogether DESC
    """)

    List<Map<String, Object>> selectTeammateSynergy( Long accountId);
}