package com.study.langchain4jspringboot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置 —— 文档异步处理的队列与交换机。
 *
 * @author kisssheep
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "document.exchange";
    public static final String QUEUE = "document.process.queue";
    public static final String ROUTING_KEY = "document.process";

    @Bean
    public DirectExchange documentExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue documentProcessQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding documentProcessBinding(DirectExchange documentExchange, Queue documentProcessQueue) {
        return BindingBuilder.bind(documentProcessQueue).to(documentExchange).with(ROUTING_KEY);
    }

    /** JSON 序列化，替代默认的 Java 序列化 */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
