package com.spark.test.utils;


import com.spark.utils.DecryptUtil;
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
public class DecryptTest {

    @Test
    public void desTest() {
        // 123456
        System.out.println(DecryptUtil.des("85909d78e0d4c355", "!Qaz7410"));
    }
}
