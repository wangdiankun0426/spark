package com.spark.common.constant;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 切面执行顺序常量
 * 值越小优先级越高，越先执行
 */
public class AspectOrder {

    /**
     * 日志打印切面 - 最先执行
     */
    public static final int LOG_PRINT = 0;

    /**
     * 防抖切面 - 在日志之后执行
     */
    public static final int DEBOUNCE = 1;

    /**
     * 操作日志切面
     */
    public static final int OPERATE_LOG = 2;

    /**
     * 数据权限切面
     */
    public static final int DATA_SCOPE = 3;
}
