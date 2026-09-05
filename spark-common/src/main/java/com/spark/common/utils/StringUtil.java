package com.spark.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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

    /**
     * 解析字符串
     * @param str
     * @param interval
     * @return
     */
    public static List<String> parseStringFormCode(String str, String interval) {
        List<String> keys = new ArrayList<>();
        if(StringUtils.isBlank(str) || StringUtils.isBlank(interval)) {
            return keys;
        }
        String formVal = str;
        while (true){
            int startIndex = formVal.indexOf(interval + "{");
            int endIndex = formVal.indexOf("}"+interval);
            if(startIndex == -1 || endIndex == -1) {
                break;
            }
            String formKey = formVal.substring(startIndex+2, endIndex);
            keys.add(formKey);
            formVal = formVal.substring(endIndex+2);
        }
        return keys;
    }

    /**
     * 判断是否为数字
     * @param cs
     * @return
     */
    public static boolean isNumeric(CharSequence cs) {
        if (cs == null || cs.isEmpty()) {
            return false;
        } else {
            int sz = cs.length();

            for(int i = 0; i < sz; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }
}
