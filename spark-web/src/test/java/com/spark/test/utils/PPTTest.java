package com.spark.test.utils;


import com.spark.utils.PPTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 11:03
 */
@SpringBootTest
public class PPTTest {

    @Test
    public void testReadPPTContent() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test.ppt";
        String content = PPTUtil.readPPTContent(filePath);
        System.out.println(content);
    }

    @Test
    public void testReadPPTXContent() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test.pptx";
        String content = PPTUtil.readPPTXContent(filePath);
        System.out.println(content);
    }
}
