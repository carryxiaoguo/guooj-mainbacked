package com.itguo.guooj.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.judge}")
    private String judgeQueue;

    @Value("${rabbitmq.exchange.judge}")
    private String judgeExchange;

    @Value("${rabbitmq.routing-key.judge}")
    private String judgeRoutingKey;

    /**
     * 判题队列
     */
    @Bean
    public Queue judgeQueue() {
        // durable: 持久化队列
        return new Queue(judgeQueue, true);
    }

    /**
     * 判题交换机
     */
    @Bean
    public DirectExchange judgeExchange() {
        return new DirectExchange(judgeExchange, true, false);
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding judgeBinding() {
        return BindingBuilder
                .bind(judgeQueue())
                .to(judgeExchange())
                .with(judgeRoutingKey);
    }

    /**
     * 消息转换器 - 使用 JSON 格式
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
