package com.spark.utils;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/22 21:40
 */
public class MapUtil {


    /**
     * 获取integer类型
     *
     * @param map
     * @param k
     * @return
     */
    public static Integer getIntegerVal(Map map, Object k) {
        return MapUtils.getInteger(map, k, 0);
    }

    /**
     * 获取boolean类型
     *
     * @param map
     * @param k
     * @return
     */
    public static Boolean getBooleanVal(Map map, Object k) {
        return MapUtils.getBoolean(map, k, false);
    }

    /** 获取string类型
     *
     * @param map
     * @param k
     * @return
     */
    public static String getStringVal(Map map, Object k) {
        return MapUtils.getString(map, k, "");
    }

    /**
     * 获取Long类型
     *
     * @param map
     * @param k
     * @return
     */
    public static Long getLongVal(Map map, Object k) {
        return MapUtils.getLong(map, k, 0L);
    }

}
