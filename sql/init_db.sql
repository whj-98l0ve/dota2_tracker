-- ============================================================================
-- Dota2 Tracker (毒瘤榜.GG) 数据库初始化建表脚本
-- 适用版本: MySQL 5.7+ / MySQL 8.0+
-- 执行方式: mysql -u root -p < sql/init_db.sql
-- ============================================================================

-- 1. 创建数据库（使用 utf8mb4 字符集，支持 Emoji 昵称与多语言字符）
CREATE DATABASE IF NOT EXISTS `dota2_tracker`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `dota2_tracker`;

SET NAMES utf8mb4;

-- ============================================================================
-- 2. 比赛原始高精度录像 JSON 存档表 (dota_match_raw)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `dota_match_raw` (
  `match_id` bigint NOT NULL COMMENT 'OpenDota 比赛唯一编号',
  `radiant_win` tinyint(1) DEFAULT '0' COMMENT '天辉是否获胜 (1:天辉胜, 0:夜魇胜)',
  `duration` int DEFAULT '0' COMMENT '比赛持续时长(秒)',
  `start_time` bigint DEFAULT '0' COMMENT '比赛开始时间戳(秒)',
  `game_mode` int DEFAULT '0' COMMENT '游戏模式代码 (如 22:全英雄选择)',
  `lobby_type` int DEFAULT '0' COMMENT '房间类型代码 (如 7:天梯匹配)',
  `raw_data` longtext NOT NULL COMMENT 'OpenDota 解析后的完整高精度比赛原始 JSON',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  PRIMARY KEY (`match_id`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛原始高精度JSON存档表';

-- ============================================================================
-- 3. 比赛选手单场表现明细表 (dota_match_player)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `dota_match_player` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `match_id` bigint NOT NULL COMMENT '比赛ID',
  `account_id` bigint NOT NULL COMMENT 'Steam32 玩家ID (匿名玩家为 0)',
  `personaname` varchar(64) DEFAULT NULL COMMENT '比赛当时的玩家昵称',
  `player_slot` int NOT NULL COMMENT '选手槽位 (0~4为天辉, 128~132为夜魇)',
  `is_radiant` tinyint(1) NOT NULL COMMENT '是否天辉阵营 (1:天辉, 0:夜魇)',
  `win` tinyint(1) NOT NULL COMMENT '本局是否获胜 (1:胜, 0:负)',
  `hero_id` int NOT NULL COMMENT '使用英雄ID',
  `kills` int DEFAULT '0' COMMENT '击杀数',
  `deaths` int DEFAULT '0' COMMENT '死亡数',
  `assists` int DEFAULT '0' COMMENT '助攻数',
  `kda` decimal(6,2) DEFAULT '0.00' COMMENT '单场KDA计算值',
  `gold_per_min` int DEFAULT '0' COMMENT '分均经济 (GPM)',
  `xp_per_min` int DEFAULT '0' COMMENT '分均经验 (XPM)',
  `hero_damage` int DEFAULT '0' COMMENT '英雄伤害',
  `tower_damage` int DEFAULT '0' COMMENT '建筑伤害',
  `lane` int DEFAULT '0' COMMENT '地图分路 (1:下路, 2:中路, 3:上路)',
  `lane_role` int DEFAULT '0' COMMENT '推导出的1~5号位角色 (1~5)',
  `lane_efficiency` decimal(5,2) DEFAULT '0.00' COMMENT '10分钟对线效率',
  `lane_result` tinyint DEFAULT '2' COMMENT '10分钟对线结果 (1:线优, 2:均势, 3:线劣)',
  `party_id` int DEFAULT NULL COMMENT '组队队伍ID',
  `party_size` int DEFAULT '1' COMMENT '组队人数 (1:单排, 2~3:双三排, 5:五黑)',
  `is_mvp` tinyint(1) DEFAULT '0' COMMENT '是否本局核心高光MVP (1:是, 0:否)',
  `items_json` varchar(256) DEFAULT '[]' COMMENT '终局六格主物品ID列表 JSON',
  `start_time` bigint NOT NULL COMMENT '比赛开始时间戳(秒)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_account` (`match_id`,`account_id`),
  KEY `idx_account_time` (`account_id`,`start_time`),
  KEY `idx_match_radiant` (`match_id`,`is_radiant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛选手单场表现明细表';

-- ============================================================================
-- 4. 录像解析异步任务队列表 (dota_parse_task)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `dota_parse_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` bigint NOT NULL COMMENT '触发该任务的关注玩家ID',
  `match_id` bigint NOT NULL COMMENT '待解析/拉取的比赛ID',
  `job_id` varchar(64) DEFAULT NULL COMMENT 'OpenDota 解析任务 JobId',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态: PENDING / READY_TO_FETCH / REQUESTED / SUCCESS / FAILED',
  `check_count` int DEFAULT '0' COMMENT '轮询检查次数',
  `next_check_time` datetime NOT NULL COMMENT '下一次轮询执行时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_match` (`account_id`,`match_id`),
  KEY `idx_status_next_check` (`status`,`next_check_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OpenDota录像解析与拉取任务队列表';

-- ============================================================================
-- 5. 玩家常用英雄聚合统计表 (dota_player_hero_stats)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `dota_player_hero_stats` (
  `account_id` bigint NOT NULL COMMENT 'Steam32 玩家ID',
  `hero_id` int NOT NULL COMMENT '英雄ID',
  `matches` int NOT NULL DEFAULT '0' COMMENT '使用总场次',
  `wins` int NOT NULL DEFAULT '0' COMMENT '获胜场次',
  `losses` int NOT NULL DEFAULT '0' COMMENT '失败场次',
  `win_rate` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '胜率百分比',
  `avg_kills` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '场均击杀',
  `avg_deaths` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '场均死亡',
  `avg_assists` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '场均助攻',
  `kda` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '综合KDA',
  `lane_win_rate` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '线优率百分比',
  `lane_loss_rate` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '线劣率百分比',
  `avg_gpm` int NOT NULL DEFAULT '0' COMMENT '场均GPM',
  `avg_xpm` int NOT NULL DEFAULT '0' COMMENT '场均XPM',
  `perf_score` decimal(3,1) NOT NULL DEFAULT '0.0' COMMENT '英雄战力评分',
  `last_played_time` bigint DEFAULT NULL COMMENT '最近一场使用该英雄的时间戳(秒)',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`,`hero_id`),
  KEY `idx_account_matches` (`account_id`,`matches`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家英雄池统计汇总表';

-- ============================================================================
-- 6. 玩家综合档案与定时巡检关注表 (dota_player_summary)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `dota_player_summary` (
  `account_id` bigint NOT NULL COMMENT 'Steam32 玩家ID',
  `personaname` varchar(64) DEFAULT NULL COMMENT 'Steam 昵称',
  `avatar` varchar(512) DEFAULT NULL COMMENT 'Steam 头像完整 URL',
  `rank_tier` int DEFAULT '0' COMMENT 'OpenDota 段位代码 (如 75:超凡5星, 80:冠绝一世)',
  `leaderboard_rank` int DEFAULT NULL COMMENT '冠绝一世天梯具体排名 (如 698 表示冠绝698)',
  `is_tracked` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否納入管理员每小时自动巡检关注列表 (1:关注, 0:未关注)',
  `last_sync_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次向 OpenDota 检查同步的时间',
  `total_matches` int DEFAULT '0' COMMENT '本地已入库总场次',
  `total_wins` int DEFAULT '0' COMMENT '本地已入库总胜场',
  `win_rate` decimal(5,1) DEFAULT '0.0' COMMENT '总胜率百分比',
  `avg_kda` decimal(5,2) DEFAULT '0.00' COMMENT '综合场均KDA',
  `avg_kills` decimal(4,1) DEFAULT '0.0' COMMENT '场均击杀',
  `avg_deaths` decimal(4,1) DEFAULT '0.0' COMMENT '场均死亡',
  `avg_assists` decimal(4,1) DEFAULT '0.0' COMMENT '场均助攻',
  `avg_gpm` int DEFAULT '0' COMMENT '场均GPM',
  `avg_xpm` int DEFAULT '0' COMMENT '场均XPM',
  `radiant_matches` int DEFAULT '0' COMMENT '天辉场次',
  `radiant_wins` int DEFAULT '0' COMMENT '天辉胜场',
  `dire_matches` int DEFAULT '0' COMMENT '夜魇场次',
  `dire_wins` int DEFAULT '0' COMMENT '夜魇胜场',
  `lane_win_rate` decimal(5,1) DEFAULT '0.0' COMMENT '综合线优率百分比',
  `solo_win_rate` decimal(5,1) DEFAULT '0.0' COMMENT '单排胜率百分比',
  `duo_trio_win_rate` decimal(5,1) DEFAULT '0.0' COMMENT '双三排胜率百分比',
  `five_stack_win_rate` decimal(5,1) DEFAULT '0.0' COMMENT '五排车队胜率百分比',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`),
  KEY `idx_is_tracked` (`is_tracked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家综合战绩汇总与关注配置表';
