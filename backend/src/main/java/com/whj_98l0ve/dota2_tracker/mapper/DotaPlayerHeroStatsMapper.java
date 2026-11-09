package com.whj_98l0ve.dota2_tracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whj_98l0ve.dota2_tracker.entity.DotaPlayerHeroStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DotaPlayerHeroStatsMapper extends BaseMapper<DotaPlayerHeroStats> {

    @Insert("""
        INSERT INTO dota_player_hero_stats (
            account_id, hero_id, matches, wins, losses, win_rate,
            avg_kills, avg_deaths, avg_assists, kda,
            lane_win_rate, lane_loss_rate, avg_gpm, avg_xpm,
            perf_score, last_played_time
        )
        SELECT 
            account_id, hero_id,
            COUNT(1),
            SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN win = 0 THEN 1 ELSE 0 END),
            ROUND(SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1),
            ROUND(AVG(kills), 1),
            ROUND(AVG(deaths), 1),
            ROUND(AVG(assists), 1),
            ROUND((SUM(kills) + SUM(assists)) / GREATEST(SUM(deaths), 1), 2),
            ROUND(SUM(CASE WHEN lane_result = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1),
            ROUND(SUM(CASE WHEN lane_result = 3 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1),
            ROUND(AVG(gold_per_min), 0),
            ROUND(AVG(xp_per_min), 0),
            ROUND(LEAST(10.0, 6.0 + (SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 2.0 / COUNT(1)) + ((SUM(kills) + SUM(assists)) / GREATEST(SUM(deaths), 1) * 0.3)), 1),
            MAX(start_time)
        FROM dota_match_player
        WHERE account_id = #{accountId}
        GROUP BY hero_id
        ON DUPLICATE KEY UPDATE 
            matches = VALUES(matches),
            wins = VALUES(wins),
            losses = VALUES(losses),
            win_rate = VALUES(win_rate),
            avg_kills = VALUES(avg_kills),
            avg_deaths = VALUES(avg_deaths),
            avg_assists = VALUES(avg_assists),
            kda = VALUES(kda),
            lane_win_rate = VALUES(lane_win_rate),
            lane_loss_rate = VALUES(lane_loss_rate),
            avg_gpm = VALUES(avg_gpm),
            avg_xpm = VALUES(avg_xpm),
            perf_score = VALUES(perf_score),
            last_played_time = VALUES(last_played_time)
    """)
    int refreshPlayerHeroStats(Long accountId);

    @Select("""
        SELECT * FROM dota_player_hero_stats
        WHERE account_id = #{accountId}
        ORDER BY matches DESC
        LIMIT #{limit}
    """)
    List<DotaPlayerHeroStats> selectTopHeroes(Long accountId, int limit);
}