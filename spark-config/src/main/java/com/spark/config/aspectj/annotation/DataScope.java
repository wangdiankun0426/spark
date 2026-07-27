package com.spark.config.aspectj.annotation;

import java.lang.annotation.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/25 22:37
 * @Target：注解作用的位置，
 * ElementType.PARAMETER表示该注解仅能作用于参数上，
 * ElementType.METHOD表示该注解仅能作用于方法上，
 * ElementType.TYPE表示该注解仅能作用于类上。
 * 作用在参数或者方法上@Target(ElementType.PARAMETER，ElementType.METHOD)
 * @Retention：注解的生命周期，表示注解会被保留到什么阶段，可以选择编译阶段、类加载阶段，或运行阶段
 * 保留的时间范围 (RetentionPolicy)
 * SOURCE源文件保留（如@Override保留在源文件，编译后注解消失）
 * CLASS编译时保留（如lombok生成get/set）
 * RUNTIME运行时保留（如切面记录日志，或验证参数信息等）
 * @Documented：注解信息会被添加到Java文档中
 * @Inherited：子类注解自动继承该注解（更加业务情况选择）
 * 过滤数据权限注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 表别名
     * @return
     */
    String tableAlias() default "";
}
