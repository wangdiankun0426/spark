package com.spark.test.utils;


import com.spark.common.utils.EncryptUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 16:09
 */
@SpringBootTest
public class EncryptTest {

    @Test
    public void md5Test(String[] args) {
        System.out.println(EncryptUtil.md5("123456"));
        // 85909d78e0d4c355
        System.out.println(EncryptUtil.des("123456", "!Qaz7410"));
    }
}
