package com.spark.config.aspectj.aspect;

import com.spark.common.bean.base.BaseException;
import com.spark.common.constant.AspectOrder;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.common.bean.base.BaseQuery;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.enums.DataScopeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.utils.StringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 2024/3/26 9:36
 * 方法共有
 * 前置通知 @Before(“pointcut()”)、
 * 后置通知 @After(“pointcut()”)、
 * 返回时通知 @AfterReturning(pointcut = “pointcut()”,returning = “object”)、
 * 抛出异常时通知 @AfterThrowing(“pointcut()”)、
 * 环绕通知 @Around(“pointcut()”)
 *
 * @order 设置优先级，值越低优先级越高
 */
@Aspect
@Component
@Order(AspectOrder.DATA_SCOPE)
public class DataScopeAspect {
    private final static Logger logger = LoggerFactory.getLogger(DataScopeAspect.class);

    /**
     * 切点
     * @within(com.fang.cloud.common.aspect.Ump) 扫类
     * @annotation(com.spark.flow.aspectj.annotation.DataScope) 扫方法
     */
    @Pointcut("@annotation(com.spark.config.aspectj.annotation.DataScope)")
    public void pointcut() {}

    /**
     * 方法之前
     */
    @Before(value = ("pointcut()"))
    public void doBefore(JoinPoint joinPoint) {
        // 管理员不过滤权限
        if (SessionHolder.isSysAdmin() || SessionHolder.isOrgAdmin()) {
            return;
        }
        // 获得注解
        DataScope annotation = getAnnotation(joinPoint);
        if (annotation == null) {
            logger.error("annotation is null");
            return;
        }
        String tableAlias = annotation.tableAlias();
        // 查询当前用户的最大数据权限
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            throw new BaseException(ErrorCodeEnum.NOT_LOGIN);
        }
        Integer dataScope = SessionHolder.getCurrentDataScop();
        logger.info("dataScope={}", dataScope);
        StringBuilder sql = new StringBuilder();
        switch (DataScopeEnum.indexOf(dataScope)) {
            case UNKNOWN:
            case ONLY_ONESELF:
                sql.append("and ");
                if (StringUtil.isNotBlank(tableAlias)) {
                    sql.append(tableAlias).append(".");
                }
                sql.append("created_by = ").append(userId);
                break;
            case ONLY_DEPART:
                sql.append("and ");
                if (StringUtil.isNotBlank(tableAlias)) {
                    sql.append(tableAlias).append(".");
                }
                Long deptId = SessionHolder.getCurrentDeptId();
                sql.append("dept_id = ").append(deptId);
                break;
            case DEPART_AND_SUB_DEPART:
                sql.append("and ");
                if (StringUtil.isNotBlank(tableAlias)) {
                    sql.append(tableAlias).append(".");
                }
                String deptIds = SessionHolder.getCurrentDeptIds();
                sql.append("dept_id in (").append(deptIds).append(")");
                break;
            default:
                break;
        }
        if (StringUtil.isNotBlank(sql.toString())) {
            BaseQuery baseQuery = (BaseQuery) joinPoint.getArgs()[0];
            baseQuery.setDataScopeSQL(sql.toString());
            logger.info("scope sql={}", sql);
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DataScope getAnnotation(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(DataScope.class);
        }
        return null;
    }
}
