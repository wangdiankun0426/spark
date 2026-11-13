package com.spark.config.aspectj.aspect;

import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.constant.AspectOrder;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.RequiresPermi;
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
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-13 16:00:00
 * 操作权限切面，按权限串校验当前用户，无权限直接拦截
 */
@Aspect
@Component
@Order(AspectOrder.ROLE_PERMISSION)
public class RequiresPermiAspect {
    private final static Logger logger = LoggerFactory.getLogger(RequiresPermiAspect.class);
    /**
     * 站点管理员通配权限串
     */
    private static final String ALL_PERMISSION = "*:*:*";

    /**
     * 切点
     */
    @Pointcut("@annotation(com.spark.config.aspectj.annotation.RequiresPermi)")
    public void pointcut() {}

    /**
     * 方法之前校验操作权限
     */
    @Before(value = "pointcut()")
    public void doBefore(JoinPoint joinPoint) {
        RequiresPermi annotation = getAnnotation(joinPoint);
        if (annotation == null) {
            return;
        }
        String perms = annotation.value();
        if (StringUtil.isBlank(perms)) {
            throw new BaseException(ErrorCodeEnum.NO_PERMISSION);
        }
        // 未登录快速失败
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            throw new BaseException(ErrorCodeEnum.NOT_LOGIN);
        }
        // 系统管理员放行
        if (SessionHolder.isSysAdmin()) {
            return;
        }
        Set<String> permissions = SessionHolder.getCurrentPermissions();
        boolean allowed = hasPermission(permissions, perms);
        if (allowed) {
            return;
        }
        logger.warn("permission denied, userId={}, perms={}", userId, perms);
        throw new BaseException(ErrorCodeEnum.NO_PERMISSION);
    }

    /**
     * 判断权限集合是否命中目标权限串或通配权限
     * @param permissions 权限集合
     * @param perms 目标权限串
     * @return 命中返回true
     */
    private boolean hasPermission(Set<String> permissions, String perms) {
        if (CollectionUtil.isEmpty(permissions)) {
            return false;
        }
        if (permissions.contains(ALL_PERMISSION)) {
            return true;
        }
        return permissions.contains(perms);
    }

    /**
     * 获取方法上的操作权限注解
     * @param joinPoint 连接点
     * @return 注解
     */
    private RequiresPermi getAnnotation(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(RequiresPermi.class);
        }
        return null;
    }
}