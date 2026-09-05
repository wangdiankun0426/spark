package com.spark.test.utils;


import com.spark.common.utils.WordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 09:48
 */
@SpringBootTest
public class WordTest {

    @Test
    public void testReadDocxContent() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test.docx";
        String content = WordUtil.readDocxContent(filePath);
        System.out.println(content);
    }

    @Test
    public void testReadDocContent() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test.doc";
        String content = WordUtil.readDocContent(filePath);
        System.out.println(content);
    }
}
