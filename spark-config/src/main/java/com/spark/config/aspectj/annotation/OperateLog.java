package com.spark.config.aspectj.annotation;

import com.spark.enums.OperateTypeEnum;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 16:33
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 值越小 优先级越高
@Order(-1)
public @interface OperateLog {
    /**
     * 操作类型
     * @return
     */
    OperateTypeEnum operateType();
}
