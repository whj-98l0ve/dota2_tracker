package com.whj_98l0ve.dota2_tracker.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DeepSeek AI 提示词动态配置实体（支持在管理员后台实时修改并持久化）
 */
@Data
@TableName("dota_ai_prompt_config")
public class DotaAiPromptConfig {
    /** 配置键：blame（到底谁的问题？） / mvp（谁是天降猛男！） */
    @TableId
    private String promptKey;
    /** 提示词模板内容（支持 {{winningTeam}} 与 {{losingTeam}} 占位符） */
    private String promptContent;
    /** 最近更新时间 */
    private LocalDateTime updatedAt;
}
