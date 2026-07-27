package com.spark.test;

import com.google.code.kaptcha.Producer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/7 21:25
 */
@SpringBootTest
public class ValidateCodeTest {
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Test
    public void generateCode() {
        System.out.println(captchaProducer.createText());
    }
}
