package com.spark.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/15 16:19
 */
public class StringUtil {

    /**
     * 判断字符串是否为空或""
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * 判断字符串是否不为空或""
     * @param str
     * @return
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 连接列表成字符串
     * @param list
     * @param separator
     * @return
     */
    public static String joinList(List list, String separator) {
        return StringUtils.join(list, separator);
    }

    /**
     * 列表转为字符串
     * @param list  列表
     * @param separator 列表元素之间的分隔符
     * @return 列表转为的字符串
     */
    public static String join(List<Long> list, String separator) {
        return StringUtils.join(list, separator);
    }
}
