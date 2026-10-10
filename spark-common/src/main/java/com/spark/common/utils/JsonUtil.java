package com.spark.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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
     * 字符串转对象列表
     * @param text json字符串
     * @param elementClass 元素类型
     * @param <T>
     * @return 对象列表
     */
    public static <T> List<T> toList(String text, Class<T> elementClass) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return JSON.parseArray(text, elementClass);
    }

    /**
     * 字符串转动态map
     * @param text json对象字符串
     * @return map
     */
    public static Map<String, Object> toMap(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return JSON.parseObject(text, new TypeReference<>() {
        });
    }

    /**
     * 字符串转动态map列表
     * @param text json对象数组字符串
     * @return map列表
     */
    public static List<Map<String, Object>> toMapList(String text) {
        return Collections.singletonList(toMap(text));
    }

    /**
     * 字符串转动态列表
     * @param text json数组字符串
     * @return 列表
     */
    public static List<Object> toArray(String text) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return JSON.parseArray(text);
    }

    /**
     * 取map中的子对象
     * @param map map
     * @param key 键
     * @return 子对象map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        return (Map<String, Object>) map.get(key);
    }

    /**
     * 取map中的对象数组
     * @param map map
     * @param key 键
     * @return 对象数组
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getMapList(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        return (List<Map<String, Object>>) map.get(key);
    }

    /**
     * 取map中的数组
     * @param map map
     * @param key 键
     * @return 数组
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getArray(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        return (List<Object>) map.get(key);
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
            logger.error("parseMap error, str={}", str, e);
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
            logger.error("parseList error, str={}", str, e);
        }
        return null;
    }
}
