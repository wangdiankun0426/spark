package com.spark.config.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqProducerConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqProducerConfig.class);

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setBeforePublishPostProcessors(new MqMessagePost());
        // 设置 ConfirmCallback：消息是否成功到达 Exchange
        rabbitTemplate.setConfirmCallback((data, ack, cause) -> {
            if (ack) {
                logger.info("消息已成功到达 RabbitMQ Exchange: {}", data);
            } else {
                logger.error("消息发送失败，原因: {}，data: {}", cause, data);
                // 可在此处记录日志、重试、存 DB 补偿等
            }
        });
        rabbitTemplate.setMandatory(true);
        // 设置 ReturnCallback：消息是否成功路由到 Queue（当 mandatory=true 时）
        rabbitTemplate.setReturnsCallback(returned -> {
            // 通常是因为 routingKey 错误或没有绑定队列
            logger.error("消息未路由到队列，被退回: exchange={}, routingKey={}, message={}, replyCode={}, replyText={}", returned.getExchange(), returned.getRoutingKey(), new String(returned.getMessage().getBody()), returned.getReplyCode(), returned.getReplyText());
        });
        return rabbitTemplate;
    }
}