package com.whj_98l0ve.dota2_tracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whj_98l0ve.dota2_tracker.entity.DotaPlayerSummary;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DotaPlayerSummaryMapper extends BaseMapper<DotaPlayerSummary> {

    /**
     * 更新关注玩家的备注名称（允许置空）
     */
    @Update("UPDATE dota_player_summary SET remark = #{remark} WHERE account_id = #{accountId}")
    int updatePlayerRemark(@Param("accountId") Long accountId, @Param("remark") String remark);

    @Insert("""
        INSERT INTO dota_player_summary (
            account_id, total_matches, total_wins, win_rate, avg_kda,
            avg_kills, avg_deaths, avg_assists, avg_gpm, avg_xpm,
            radiant_matches, radiant_wins, dire_matches, dire_wins,
            lane_win_rate, solo_win_rate, duo_trio_win_rate, five_stack_win_rate
        )
        SELECT 
            account_id,
            COUNT(1),
            SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END),
            ROUND(SUM(CASE WHEN win = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1),
            ROUND((SUM(kills) + SUM(assists)) / GREATEST(SUM(deaths), 1), 2),
            ROUND(AVG(kills), 1),
            ROUND(AVG(deaths), 1),
            ROUND(AVG(assists), 1),
            ROUND(AVG(gold_per_min), 0),
            ROUND(AVG(xp_per_min), 0),
            SUM(CASE WHEN is_radiant = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN is_radiant = 1 AND win = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN is_radiant = 0 THEN 1 ELSE 0 END),
            SUM(CASE WHEN is_radiant = 0 AND win = 1 THEN 1 ELSE 0 END),
            ROUND(SUM(CASE WHEN lane_result = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(1), 1),
            ROUND(COALESCE(SUM(CASE WHEN party_size = 1 AND win = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN party_size = 1 THEN 1 ELSE 0 END), 0), 0), 1),
            ROUND(COALESCE(SUM(CASE WHEN party_size IN (2,3) AND win = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN party_size IN (2,3) THEN 1 ELSE 0 END), 0), 0), 1),
            ROUND(COALESCE(SUM(CASE WHEN party_size = 5 AND win = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(SUM(CASE WHEN party_size = 5 THEN 1 ELSE 0 END), 0), 0), 1)
        FROM dota_match_player
        WHERE account_id = #{accountId}
        GROUP BY account_id
        ON DUPLICATE KEY UPDATE 
            total_matches = VALUES(total_matches),
            total_wins = VALUES(total_wins),
            win_rate = VALUES(win_rate),
            avg_kda = VALUES(avg_kda),
            avg_kills = VALUES(avg_kills),
            avg_deaths = VALUES(avg_deaths),
            avg_assists = VALUES(avg_assists),
            avg_gpm = VALUES(avg_gpm),
            avg_xpm = VALUES(avg_xpm),
            radiant_matches = VALUES(radiant_matches),
            radiant_wins = VALUES(radiant_wins),
            dire_matches = VALUES(dire_matches),
            dire_wins = VALUES(dire_wins),
            lane_win_rate = VALUES(lane_win_rate),
            solo_win_rate = VALUES(solo_win_rate),
            duo_trio_win_rate = VALUES(duo_trio_win_rate),
            five_stack_win_rate = VALUES(five_stack_win_rate)
    """)
    int refreshPlayerSummary(Long accountId);
}