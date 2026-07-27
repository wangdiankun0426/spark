package com.spark.manage;

import com.rabbitmq.client.Channel;
import com.spark.bean.log.entity.LogLogin;
import com.spark.bean.log.entity.LogOperate;
import com.spark.bean.system.vo.MessageVO;
import com.spark.bean.base.ResultData;
import com.spark.constant.MqQueueKey;
import com.spark.manage.log.ILogLoginService;
import com.spark.manage.log.ILogOperateService;
import com.spark.manage.system.IMessageService;
import com.spark.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/3 13:22
 * mq消息消费者
 */
@Component
public class MqConsumer {
    private final static Logger logger = LoggerFactory.getLogger(MqConsumer.class);
    @Autowired
    private IMessageService messageService;
    @Autowired
    private ILogLoginService logLoginService;
    @Autowired
    private ILogOperateService logOperateService;

    /**
     * 处理[系统通知]消息
     *
     * ExchangeTypes
     * DIRECT:根据路由精准匹配队列
     * TOPIC:根据路由 * # 进行模糊匹配
     * FANOUT:不用路由，交换机直接和队列匹配
     * @param msg
     * 声明交换机-路由-队列之间的绑定关系
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqQueueKey.SYSTEM_MESSAGE),
            exchange = @Exchange(value = "spark_exchange", type = ExchangeTypes.DIRECT),
            key = MqQueueKey.SYSTEM_MESSAGE_KEY
    ))
    public void handleSystemMessage(Message message, Channel channel, String msg) {
        try {
            logger.info("rabbitmq [systemMessage] 队列监听到了消息， msg is {}", msg);
            MessageVO messageVO = JsonUtil.toObject(msg, MessageVO.class);
            ResultData<Void> createMsgResult = messageService.createMessage(messageVO);
            logger.info("handle system message createMsgResult is {}", createMsgResult);
            //手动 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 如果不处理异常，消息会重新入队，这里直接对异常进行处理
            logger.info("handle system message error, exception is  ", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                logger.error("handle system message nack error, exception is  ", ex);
            }
        }
    }

    /**
     * 处理[登录日志]消息
     *
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqQueueKey.LOGIN_LOG),
            exchange = @Exchange(value = "spark_exchange", type = ExchangeTypes.DIRECT),
            key = MqQueueKey.LOGIN_LOG_KEY
    ))
    public void handleLoginLog(Message message, Channel channel, String msg) {
        try {
            logger.info("rabbitmq [loginLog] 队列监听到了消息， msg is {}", msg);
            LogLogin logLogin = JsonUtil.toObject(msg, LogLogin.class);
            ResultData<Void> createLogLoginResult = logLoginService.createLogLogin(logLogin);
            logger.info("handle login log createLogLoginResult is {}", createLogLoginResult);
            //手动 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 如果不处理异常，消息会重新入队，这里直接对异常进行处理
            logger.info("handle login log error, exception is  ", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                logger.error("handle login log nack error, exception is  ", ex);
            }
        }
    }

    /**
     * 处理[操作日志]消息
     *
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqQueueKey.OPERATE_LOG),
            exchange = @Exchange(value = "spark_exchange", type = ExchangeTypes.DIRECT),
            key = MqQueueKey.OPERATE_LOG_KEY
    ))
    public void handleOperateLog(Message message, Channel channel, String msg) {
        try {
            logger.info("rabbitmq [operateLog] 队列监听到了消息， msg is {}", msg);
            LogOperate logOperate = JsonUtil.toObject(msg, LogOperate.class);
            ResultData<Void> createLogOperateResult = logOperateService.createLogOperate(logOperate);
            logger.info("handle operate log createLogOperateResult is {}", createLogOperateResult);
            //手动 ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 发生异常 这里直接对异常进行处理 可以先存到数据库在进行补偿
            logger.error("handle operate log error, exception is  ", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                logger.error("handle operate log nack error, exception is  ", ex);
            }
        }
    }
}
