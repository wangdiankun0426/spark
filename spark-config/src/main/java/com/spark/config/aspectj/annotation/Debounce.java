package com.spark.config.aspectj.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 防抖注解，防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Debounce {

    /**
     * 防抖时间
     */
    long value() default 1000;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}
