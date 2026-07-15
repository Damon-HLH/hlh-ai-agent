package com.hlh.hlhaiagent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 恋爱报告记录表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "love_report")
public class LoveReport implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对话ID
     */
    @TableField(value = "chat_id")
    private String chatId;

    /**
     * 对话记录（JSON格式存储）
     */
    @TableField(value = "messages")
    private String messages;
}
