package com.spark.config.aspectj.aspect;

import com.spark.common.constant.AspectOrder;
import com.spark.config.aspectj.annotation.LogIgnore;
import com.spark.common.utils.JsonUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 日志打印切面，打印方法入参和返回值
 * 优先级最高，确保在其他切面之前执行
 */
@Aspect
@Component
@Order(AspectOrder.LOG_PRINT)
public class LogPrintAspect {
    private final static Logger logger = LoggerFactory.getLogger(LogPrintAspect.class);

    /**
     * 匹配类上有 @LogPrint 注解的方法
     */
    @Around("@within(com.spark.config.aspectj.annotation.LogPrint)")
    public Object printLogForClass(ProceedingJoinPoint joinPoint) throws Throwable {
        return doPrintLog(joinPoint);
    }

    /**
     * 匹配方法上有 @LogPrint 注解的方法
     */
    @Around("@annotation(com.spark.config.aspectj.annotation.LogPrint)")
    public Object printLogForMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return doPrintLog(joinPoint);
    }

    /**
     * 执行日志打印逻辑
     */
    private Object doPrintLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 检查方法上是否有 @LogIgnore 注解，有则跳过
        LogIgnore logIgnore = AnnotationUtils.findAnnotation(method, LogIgnore.class);
        if (logIgnore != null) {
            return joinPoint.proceed();
        }
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String params = null;
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                params = JsonUtil.toString(args);
            }
        } catch (Exception e) {
            params = "[序列化失败]";
        }
        logger.info("{} {} 入参={}", className, methodName, params);
        long start = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long cost = System.currentTimeMillis() - start;
            String returnVal = null;
            try {
                if (result != null) {
                    returnVal = JsonUtil.toString(result);
                }
            } catch (Exception e) {
                returnVal = "[序列化失败]";
            }
            logger.info("{} {} 耗时={}ms, 出参={}", className, methodName, cost, returnVal);
        }
    }
}
