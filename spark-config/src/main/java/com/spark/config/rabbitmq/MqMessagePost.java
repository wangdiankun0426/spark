package com.spark.config.rabbitmq;

import com.spark.common.utils.TraceLogUtil;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/10/23 13:14
 */
public class MqMessagePost implements MessagePostProcessor {
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        MessageProperties messageProperties = message.getMessageProperties();
        messageProperties.setHeader(TraceLogUtil.TRACK_ID_KEY, TraceLogUtil.getTrackId());
        messageProperties.setHeader(TraceLogUtil.USER_ID_KEY, TraceLogUtil.getUserId());
        messageProperties.setHeader(TraceLogUtil.TENANT_ID_KEY, TraceLogUtil.getTenantId());
        messageProperties.setHeader(TraceLogUtil.LOGIN_PLATFORM_KEY, TraceLogUtil.getLoginPlatform());
        return message;
    }
}
