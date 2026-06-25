package com.study.langchain4jspringboot.messaging;

import com.study.langchain4jspringboot.dto.DocumentTaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.study.langchain4jspringboot.config.RabbitMQConfig.EXCHANGE;
import static com.study.langchain4jspringboot.config.RabbitMQConfig.ROUTING_KEY;

/**
 * 文档处理消息生产者 —— 将文档处理任务投递到 RabbitMQ。
 *
 * @author kisssheep
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessingProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送文档处理任务到异步队列。
     *
     * @param message 任务消息
     */
    public void send(DocumentTaskMessage message) {
        log.info("Sending document processing task: taskId={}, source={}", message.taskId(), message.fileName());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        log.info("Task {} sent to queue", message.taskId());
    }
}
