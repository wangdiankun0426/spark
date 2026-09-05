package com.spark.config.aspectj.aspect;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.constant.AspectOrder;
import com.spark.config.aspectj.annotation.Debounce;
import com.spark.config.redis.RedisService;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.ErrorCodeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 防抖切面，基于Redis实现，防止重复提交
 * 环绕通知，方法执行前检查防抖标记
 */
@Aspect
@Component
@Order(AspectOrder.DEBOUNCE)
public class DebounceAspect {
    private final static Logger logger = LoggerFactory.getLogger(DebounceAspect.class);
    @Autowired
    private RedisService redisService;

    /**
     * 环绕通知，方法执行前检查防抖标记
     */
    @Around("@annotation(com.spark.config.aspectj.annotation.Debounce)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Debounce annotation = method.getAnnotation(Debounce.class);
        if (annotation == null) {
            return joinPoint.proceed();
        }
        // 获取当前用户ID
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            return joinPoint.proceed();
        }
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String redisKey = ObjectCacheKey.DEBOUNCE_KEY + userId + "_" + className + "." + methodName;
        // 检查是否存在防抖标记
        boolean exists = redisService.hasKey(redisKey);
        if (exists) {
            logger.warn("debounce hit, key={}", redisKey);
            ResultData<Object> result = new ResultData<>();
            result.setErrorCode(ErrorCodeEnum.OPERATE_TOO_FREQUENT);
            return result;
        }
        // 设置防抖标记
        redisService.setStr(redisKey, "1", annotation.value(), annotation.timeUnit());
        return joinPoint.proceed();
    }
}
