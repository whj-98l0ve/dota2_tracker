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

    // 队友合拍红黑榜 SQL
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
    List<Map<String, Object>> selectTeammateSynergy(@Param("accountId") Long accountId);

    // 统计各分路位置 (Pos 1 ~ Pos 5) 场次、占比、线优率与胜率
    @Select("""
        SELECT 
            COALESCE(NULLIF(lane_role, 0), 2) AS posId,
            COUNT(1) AS games,
            ROUND(SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS winRate,
            ROUND(SUM(CASE WHEN lane_result = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS laneWinRate
        FROM dota_match_player
        WHERE account_id = #{accountId}
        GROUP BY posId
        ORDER BY games DESC
    """)
    List<Map<String, Object>> selectPositionStats(@Param("accountId") Long accountId);

    // 统计组排对比 (单排 vs 双三排 vs 五黑)
    @Select("""
        SELECT 
            CASE 
                WHEN party_size = 1 THEN 'solo'
                WHEN party_size IN (2, 3) THEN 'party'
                WHEN party_size = 5 THEN 'stack5'
                ELSE 'solo'
            END AS queueType,
            COUNT(1) AS games,
            ROUND(SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS winRate,
            ROUND(SUM(CASE WHEN is_mvp = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS mvpRate
        FROM dota_match_player
        WHERE account_id = #{accountId}
        GROUP BY queueType
    """)
    List<Map<String, Object>> selectPartyStats(@Param("accountId") Long accountId);

    // 统计天辉 vs 夜宴真实本地战绩
    @Select("""
        SELECT 
            is_radiant AS isRadiant,
            COUNT(1) AS games,
            SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) AS wins,
            ROUND(SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS winRate
        FROM dota_match_player
        WHERE account_id = #{accountId}
        GROUP BY is_radiant
    """)
    List<Map<String, Object>> selectFactionStats(@Param("accountId") Long accountId);

    // 统计玩家所有使用过的英雄全维度明细（按场次从高到低排序：场次、胜率、天辉胜率、夜魇胜率、线优/均势/线劣分布、最近一场时间）
    @Select("""
        SELECT 
            hero_id AS heroId,
            COUNT(1) AS matches,
            SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) AS wins,
            SUM(CASE WHEN win = 0 THEN 1 ELSE 0 END) AS losses,
            ROUND(SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS winRate,
            ROUND((SUM(kills) + SUM(assists)) / GREATEST(SUM(deaths), 1), 2) AS kda,
            SUM(CASE WHEN is_radiant = 1 THEN 1 ELSE 0 END) AS radiantMatches,
            SUM(CASE WHEN is_radiant = 1 AND win = 1 THEN 1 ELSE 0 END) AS radiantWins,
            ROUND(COALESCE(SUM(CASE WHEN is_radiant = 1 AND win = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN is_radiant = 1 THEN 1 ELSE 0 END), 0), 0), 1) AS radiantWinRate,
            SUM(CASE WHEN is_radiant = 0 THEN 1 ELSE 0 END) AS direMatches,
            SUM(CASE WHEN is_radiant = 0 AND win = 1 THEN 1 ELSE 0 END) AS direWins,
            ROUND(COALESCE(SUM(CASE WHEN is_radiant = 0 AND win = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN is_radiant = 0 THEN 1 ELSE 0 END), 0), 0), 1) AS direWinRate,
            SUM(CASE WHEN lane_result = 1 THEN 1 ELSE 0 END) AS laneWinCount,
            SUM(CASE WHEN lane_result = 2 OR lane_result IS NULL OR lane_result NOT IN (1, 3) THEN 1 ELSE 0 END) AS laneEvenCount,
            SUM(CASE WHEN lane_result = 3 THEN 1 ELSE 0 END) AS laneLossCount,
            ROUND(SUM(CASE WHEN lane_result = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS laneWinRate,
            ROUND(SUM(CASE WHEN lane_result = 2 OR lane_result IS NULL OR lane_result NOT IN (1, 3) THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS laneEvenRate,
            ROUND(SUM(CASE WHEN lane_result = 3 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1) AS laneLossRate,
            MAX(start_time) AS lastPlayedTime
        FROM dota_match_player
        WHERE account_id = #{accountId}
        GROUP BY hero_id
        ORDER BY matches DESC, winRate DESC, lastPlayedTime DESC
    """)
    List<Map<String, Object>> selectHeroDetailedStats(@Param("accountId") Long accountId);

    // 统计首页核心指标：关注玩家去重后的开黑对局总场数与单排对局总场数（同一场比赛有2/3/5个关注玩家开黑只算1场）
    @Select("""
        SELECT 
            COALESCE(SUM(CASE WHEN is_party = 1 THEN 1 ELSE 0 END), 0) AS totalPartyMatches,
            COALESCE(SUM(CASE WHEN is_party = 0 THEN 1 ELSE 0 END), 0) AS totalSoloMatches
        FROM (
            SELECT 
                mp.match_id,
                CASE 
                    WHEN MAX(COALESCE(mp.party_size, 1)) > 1 OR COUNT(DISTINCT mp.account_id) > 1 THEN 1 
                    ELSE 0 
                END AS is_party
            FROM dota_match_player mp
            JOIN dota_player_summary s ON mp.account_id = s.account_id
            WHERE s.is_tracked = 1
            GROUP BY mp.match_id
        ) t
    """)
    Map<String, Object> selectTrackedMatchTypeTotals();

    // 统计首页群活动：关注玩家每天的开黑场次与单排场次（按 match_id 去重，多人同局开黑只算1场）
    @Select("""
        SELECT 
            match_date AS matchDate,
            COALESCE(SUM(CASE WHEN is_party = 1 THEN 1 ELSE 0 END), 0) AS partyGames,
            COALESCE(SUM(CASE WHEN is_party = 0 THEN 1 ELSE 0 END), 0) AS soloGames
        FROM (
            SELECT 
                mp.match_id,
                DATE_FORMAT(FROM_UNIXTIME(MAX(mp.start_time)), '%Y-%m-%d') AS match_date,
                CASE 
                    WHEN MAX(COALESCE(mp.party_size, 1)) > 1 OR COUNT(DISTINCT mp.account_id) > 1 THEN 1 
                    ELSE 0 
                END AS is_party
            FROM dota_match_player mp
            JOIN dota_player_summary s ON mp.account_id = s.account_id
            WHERE s.is_tracked = 1
            GROUP BY mp.match_id
        ) t
        GROUP BY match_date
        ORDER BY match_date DESC
        LIMIT 30
    """)
    List<Map<String, Object>> selectTrackedDailyActivity();

    // 查询所有关注玩家列表及每个人最近一场比赛时间（供首页“选择你的身份”下拉菜单使用）
    @Select("""
        SELECT 
            s.account_id AS accountId,
            s.personaname AS personaname,
            s.remark AS remark,
            s.avatar AS avatar,
            s.rank_tier AS rankTier,
            s.leaderboard_rank AS leaderboardRank,
            s.total_matches AS totalMatches,
            s.win_rate AS winRate,
            MAX(mp.start_time) AS lastMatchTime
        FROM dota_player_summary s
        LEFT JOIN dota_match_player mp ON s.account_id = mp.account_id
        WHERE s.is_tracked = 1
        GROUP BY s.account_id, s.personaname, s.remark, s.avatar, s.rank_tier, s.leaderboard_rank, s.total_matches, s.win_rate
        ORDER BY lastMatchTime DESC, s.total_matches DESC
    """)
    List<Map<String, Object>> selectTrackedPlayersWithLastMatch();
}
