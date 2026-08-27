package com.spark.config.aspectj.annotation;

import java.lang.annotation.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 忽略日志打印注解，加在方法上，配合 @LogPrint 使用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogIgnore {
}
