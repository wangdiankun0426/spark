package com.spark.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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
    private final static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

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

    /**
     * 检查字符串是否为json
     * @param str
     * @return
     */
    public static boolean isValidJson(String str) {
        return JSON.isValidObject(str);
    }

    /**
     * 字符串转map
     * @param str
     * @return
     */
    public static Map<String, String> parseMap(String str) {
        if (StringUtil.isBlank(str)) {
            return null;
        }
        try {
            return JSON.parseObject(str, new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("parseMap str error, str={}", str, e);
        }
        return null;
    }

    /**
     * 字符串转list
     * @param str
     * @return
     */
    public static List<String> parseList(String str) {
        if (StringUtil.isBlank(str)) {
            return null;
        }
        try {
            return JSON.parseObject(str, new TypeReference<>() {
            });
        } catch (Exception e) {
            logger.error("parseList str error, str={}", str, e);
        }
        return null;
    }
}
