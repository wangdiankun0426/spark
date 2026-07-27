package com.spark.test.utils;


import com.spark.utils.PDFUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 12:00
 */
@SpringBootTest
public class PDFTest {

    @Test
    public void testPDF() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test_p.pdf";
        String content = PDFUtil.readPDFContent(filePath);
        System.out.println("pdf读取内容为:"+content);
    }
}
