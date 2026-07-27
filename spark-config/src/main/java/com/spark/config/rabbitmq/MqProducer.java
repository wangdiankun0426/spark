package com.spark.config.rabbitmq;

import com.spark.constant.MqQueueKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/3 13:22
 * mq消息生产者
 */
@Component
public class MqProducer {
    private final static Logger logger = LoggerFactory.getLogger(MqProducer.class);
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 发送[系统通知]消息
     * @param msg 消息内容
     */
    public void sendSystemMessageMq(String msg) {
        this.sendMq(MqQueueKey.SYSTEM_MESSAGE_KEY, msg);
    }

    /**
     * 发送[登录日志]消息
     * @param msg 消息内容
     */
    public void sendLoginLogMq(String msg) {
        this.sendMq(MqQueueKey.LOGIN_LOG_KEY, msg);
    }

    /**
     * 发送[操作日志]消息
     * @param msg 消息内容
     */
    public void sendOperateLogMq(String msg) {
        this.sendMq(MqQueueKey.OPERATE_LOG_KEY, msg);
    }

    /**
     * 发送mq消息
     * @param routeKey 路由:发送给绑定此路由的队列消费
     * @param message 消息内容
     */
    private void sendMq(String routeKey, String message) {
        amqpTemplate.convertAndSend("spark_exchange", routeKey, message);
    }
}
