package com.itguo.guooj.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 判题消息生产者
 */
@Component
@Slf4j
public class JudgeMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.judge}")
    private String judgeExchange;

    @Value("${rabbitmq.routing-key.judge}")
    private String judgeRoutingKey;

    /**
     * 发送判题消息
     * @param questionSubmitId 题目提交ID
     */
    public void sendJudgeMessage(Long questionSubmitId) {
        try {
            log.info("发送判题消息: questionSubmitId={}", questionSubmitId);
            rabbitTemplate.convertAndSend(judgeExchange, judgeRoutingKey, questionSubmitId);
        } catch (Exception e) {
            log.error("发送判题消息失败: questionSubmitId={}", questionSubmitId, e);
            throw new RuntimeException("发送判题消息失败", e);
        }
    }
}
