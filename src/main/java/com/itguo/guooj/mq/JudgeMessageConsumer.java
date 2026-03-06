package com.itguo.guooj.mq;

import com.itguo.guooj.judge.JudgeService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 判题消息消费者
 */
@Component
@Slf4j
public class JudgeMessageConsumer {

    @Resource
    private JudgeService judgeService;

    /**
     * 监听判题队列,处理判题任务
     * @param questionSubmitId 题目提交ID
     * @param channel RabbitMQ 通道
     * @param deliveryTag 消息标签
     */
    @RabbitListener(queues = "${rabbitmq.queue.judge}", ackMode = "MANUAL")
    public void receiveJudgeMessage(Long questionSubmitId, 
                                   Channel channel, 
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("接收到判题消息: questionSubmitId={}", questionSubmitId);
        
        try {
            // 执行判题
            judgeService.doJudge(questionSubmitId);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("判题完成并确认消息: questionSubmitId={}", questionSubmitId);
            
        } catch (Exception e) {
            log.error("判题失败: questionSubmitId={}", questionSubmitId, e);
            
            try {
                // 判题失败,拒绝消息并重新入队
                // 第三个参数 requeue=true 表示重新入队
                channel.basicNack(deliveryTag, false, false);
                log.warn("消息已拒绝,不重新入队: questionSubmitId={}", questionSubmitId);
            } catch (IOException ioException) {
                log.error("拒绝消息失败: questionSubmitId={}", questionSubmitId, ioException);
            }
        }
    }
}
