package com.spark.config.aspectj.aspect;

import com.spark.utils.TraceLogUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/10/23 13:21
 */
@Aspect
@Component
@Order(Integer.MIN_VALUE+101)
public class RabbitListenerCustomAspect {

    /**
     *切点
     */
    @Before("@annotation(rabbitListener)")
    public void doBefore(JoinPoint point, RabbitListener rabbitListener) {
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (!(arg instanceof Message message)) {
                break;
            }
            MessageProperties messageProperties = message.getMessageProperties();
            Map<String, Object> headers = messageProperties.getHeaders();
            String trackId = (String)headers.get(TraceLogUtil.TRACK_ID_KEY);
            String userId = (String)headers.get(TraceLogUtil.USER_ID_KEY);
            TraceLogUtil.generateTrackId(trackId);
            TraceLogUtil.cacheTrackUserId(userId != null ? Long.valueOf(userId) : null);
        }
    }
}
