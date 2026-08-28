package com.spark.utils;

import org.springframework.beans.BeanUtils;

/**
 * Bean 属性拷贝工具类
 * 统一封装对象属性拷贝入口，方便后续更换底层实现
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 18:00:00
 */
public class BeanUtil {

    /**
     * 拷贝对象属性
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }
}