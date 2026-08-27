package com.spark.config.aspectj.aspect;

import com.spark.constant.AspectOrder;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.bean.log.entity.LogOperate;
import com.spark.bean.base.BaseException;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.enums.ErrorCodeEnum;
import com.spark.utils.JsonUtil;
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
public class OperateLogAspect {
    private final static Logger logger  = LoggerFactory.getLogger(OperateLogAspect.class);
    @Autowired
    private MqProducer mqProducer;

    /**
     *切点
     */
    @Pointcut("@annotation(com.spark.config.aspectj.annotation.OperateLog)")
    public void pointcut() {}

    /**
     * 环绕通知
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around(value = "pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            throw new BaseException(ErrorCodeEnum.NOT_LOGIN);
        }
        long start = System.currentTimeMillis();
        OperateLog annotation = this.getAnnotation(joinPoint);
        if (annotation == null) {
            return null;
        }
        LogOperate logOperate = new LogOperate();
        logOperate.setCreatedBy(userId);
        logOperate.setUpdatedBy(userId);
        logOperate.setType(annotation.operateType().getValue());
        Object obj = null;
        try {
            // 执行方法
            obj = joinPoint.proceed();
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
        } catch (Throwable e) {
            // 方法抛出异常之后
            logger.error("OperateLogAspect error", e);
            logOperate.setCode(ErrorCodeEnum.SYSTEM_ERROR.getValue());
            logOperate.setRemark(e.getMessage());
        }
        long end = System.currentTimeMillis();
        logOperate.setConsume((int)(end-start));
        mqProducer.sendOperateLogMq(JsonUtil.toString(logOperate));
        // 方法里面抛出了异常 此处的obj为null
        if (obj == null) {
            throw new BaseException(ErrorCodeEnum.SYSTEM_ERROR);
        }
        return obj;
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OperateLog getAnnotation(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(OperateLog.class);
        }
        return null;
    }
}
