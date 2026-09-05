package com.spark.common.utils;

import org.apache.commons.collections4.MapUtils;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 获取integer类型，值为空时返回默认值
     *
     * @param map
     * @param k
     * @param defaultValue 默认值
     * @return
     */
    public static Integer getIntegerVal(Map map, Object k, Integer defaultValue) {
        Integer value = MapUtils.getInteger(map, k, null);
        return value == null ? defaultValue : value;
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

    /**
     * 获取boolean类型，值为空时返回默认值
     *
     * @param map
     * @param k
     * @param defaultValue 默认值
     * @return
     */
    public static Boolean getBooleanVal(Map map, Object k, Boolean defaultValue) {
        Boolean value = MapUtils.getBoolean(map, k, null);
        return value == null ? defaultValue : value;
    }

    /** 获取string类型，值为集合时拼接为逗号分隔字符串（兼容前端多选组件提交的数组）
     *
     * @param map
     * @param k
     * @return
     */
    public static String getStringVal(Map map, Object k) {
        return convertToString(MapUtils.getObject(map, k));
    }

    /** 获取string类型，值为空时返回默认值，值为集合时拼接为逗号分隔字符串
     *
     * @param map
     * @param k
     * @param defaultValue 默认值
     * @return
     */
    public static String getStringVal(Map map, Object k, String defaultValue) {
        String value = convertToString(MapUtils.getObject(map, k));
        return StringUtil.isBlank(value) ? defaultValue : value;
    }

    /**
     * 将配置值转换为字符串，集合值拼接为逗号分隔字符串
     * @param value 配置值
     * @return 字符串
     */
    private static String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return String.valueOf(value);
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


    /**
     * 判断map是否为空
     * @param map
     * @return
     */
    public static boolean isEmpty(Map map) {
        return MapUtils.isEmpty(map);
    }

    /**
     * 判断map是否不为空
     * @param map
     * @return
     */
    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

}
