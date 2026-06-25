package com.study.langchain4jspringboot.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * RabbitMQ 消息体 —— 文档处理任务。
 *
 * @param taskId    任务唯一标识
 * @param sourceType 来源类型：FILE / URL
 * @param source     文件路径或 URL
 * @param fileName   原始文件名
 * @param createdAt  创建时间
 * @author kisssheep
 */
public record DocumentTaskMessage(
        String taskId,
        String sourceType,
        String source,
        String fileName,
        Instant createdAt
) implements Serializable {
}
