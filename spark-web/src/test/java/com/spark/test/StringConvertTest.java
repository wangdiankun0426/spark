package com.spark.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/15 16:03
 */
@SpringBootTest
public class StringConvertTest {

    /**
     * 驼峰命名的字符串转为下划线小写的方式
     */
    @Test
    public void test() {
        String name = "msgType";
        StringBuilder result = new StringBuilder();
        // 循环处理
        for (int i = 0; i < name.length(); i++) {
            String s = name.substring(i, i + 1);
            // 在大写字母前添加下划线
            if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                result.append("_");
            }
            // 其他字符直接转成小写
            result.append(s.toLowerCase());
        }
        // msg_type
        System.out.println(result.toString());
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串
     */
    @Test
    public void test1() {
        String name = "msg_type";
        StringBuilder result = new StringBuilder();
        if (name == null || name.isEmpty() || !name.contains("_")) {
            // 没必要转换
            System.out.println(name);
            return;
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 首字母大写
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        StringBuilder ret = new StringBuilder(result.substring(0, 1).toLowerCase());
        ret.append(result.substring(1, result.toString().length()));
        // msgType
        System.out.println(ret.toString());
    }
}
