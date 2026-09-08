package com.spark.config.aspectj.annotation;

import com.spark.common.enums.OperateTypeEnum;

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
public @interface LogOperate {
    /**
     * 操作类型
     * @return
     */
    OperateTypeEnum operateType();
}
