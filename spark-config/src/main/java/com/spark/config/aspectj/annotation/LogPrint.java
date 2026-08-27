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
 * 日志打印注解，加在类或方法上，打印方法入参和返回值
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogPrint {
}
