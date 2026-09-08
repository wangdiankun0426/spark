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
     * 角色权限切面 - 权限校验，紧随日志打印执行，先于防抖与操作日志
     */
    public static final int ROLE_PERMISSION = 1;

    /**
     * 防抖切面 - 在权限校验之后执行
     */
    public static final int DEBOUNCE = 2;

    /**
     * 操作日志切面
     */
    public static final int OPERATE_LOG = 3;

    /**
     * 数据权限切面
     */
    public static final int DATA_SCOPE = 4;
}
