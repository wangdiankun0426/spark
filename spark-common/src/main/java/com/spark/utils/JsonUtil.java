package com.spark.utils;

import com.alibaba.fastjson2.JSON;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/18 16:03
 */
public class JsonUtil {
    /**
     * 字符串转object
     * @param text
     * @param objectClass
     * @param <T>
     * @return
     */
    public static <T> T toObject(String text, Class<T> objectClass) {
        return JSON.parseObject(text, objectClass);
    }

    /**
     * object转字符串
     * @param object
     * @return
     */
    public static String toString(Object object) {
        return JSON.toJSONString(object);
    }

}
