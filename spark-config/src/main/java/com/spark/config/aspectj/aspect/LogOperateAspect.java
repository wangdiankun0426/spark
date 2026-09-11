package com.spark.config.aspectj.aspect;

import com.spark.common.constant.AspectOrder;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.utils.JsonUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @since 2024/4/3 16:34
 */
@Aspect
@Component
@Order(AspectOrder.OPERATE_LOG)
public class LogOperateAspect {
    private final static Logger logger  = LoggerFactory.getLogger(LogOperateAspect.class);
    @Autowired
    private MqProducer mqProducer;

    /**
     *切点
     */
    @Pointcut("@annotation(com.spark.config.aspectj.annotation.LogOperate)")
    public void pointcut() {}

    /**
     * 环绕通知
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around(value = "pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            throw new BaseException(ErrorCodeEnum.NOT_LOGIN);
        }
        long start = System.currentTimeMillis();
        LogOperate annotation = this.getAnnotation(joinPoint);
        if (annotation == null) {
            // 获取不到注解时直接放行,不能返回null导致接口无响应
            return joinPoint.proceed();
        }
        com.spark.common.bean.log.entity.LogOperate logOperate = new com.spark.common.bean.log.entity.LogOperate();
        logOperate.setCreatedBy(userId);
        logOperate.setUpdatedBy(userId);
        logOperate.setType(annotation.operateType().getValue());
        try {
            // 执行方法
            Object obj = joinPoint.proceed();
            // 方法执行完之后
            ResultData result = (ResultData) obj;
            logOperate.setCode(result.getCode());
            logOperate.setObjId(result.getObjId());
            if (logOperate.getObjId() == null) {
                logOperate.setObjId(0L);
            }
            if (result.getCode() == ResultData.OK) {
                logOperate.setRemark("请求成功");
            } else {
                logOperate.setRemark(ErrorCodeEnum.indexOf(result.getCode()).getDesc());
            }
            // 将操作对象id置空,不给前端返回
            result.setObjId(null);
            return obj;
        } catch (BaseException e) {
            // 业务异常:记录业务错误码后继续抛出,交由全局异常处理器返回给前端
            logger.warn("OperateLogAspect business exception: {}", e.getMessage());
            logOperate.setCode(e.getCode() == null ? ErrorCodeEnum.SYSTEM_ERROR.getValue() : e.getCode());
            logOperate.setRemark(e.getMessage());
            logOperate.setObjId(0L);
            throw e;
        } catch (Throwable e) {
            // 方法抛出未知异常之后:记录系统错误码后继续抛出,交由全局异常处理器返回给前端
            logger.error("OperateLogAspect error", e);
            logOperate.setCode(ErrorCodeEnum.SYSTEM_ERROR.getValue());
            logOperate.setRemark(e.getMessage());
            logOperate.setObjId(0L);
            throw e;
        } finally {
            // 无论成功失败都要落操作日志
            long end = System.currentTimeMillis();
            logOperate.setConsume((int)(end-start));
            logOperate.setTenantId(SessionHolder.getCurrentTenantId());
            try {
                mqProducer.sendOperateLogMq(JsonUtil.toString(logOperate));
            } catch (Exception e) {
                // 日志发送失败不能影响业务结果
                logger.error("OperateLogAspect send operate log mq fail", e);
            }
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private LogOperate getAnnotation(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(LogOperate.class);
        }
        return null;
    }
}
