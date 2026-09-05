package com.spark.test.utils;


import com.spark.common.utils.ExcelUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 11:15
 */
@SpringBootTest
public class ExcelTest {

    @Test
    public void testReadXlsxContent() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test.xlsx";
        String content = ExcelUtil.readXlsxContent(filePath);
        System.out.println(content);
    }

    @Test
    public void testReadXlsContent() {
        String filePath = "C:\\Users\\w1561\\Desktop\\file\\test.xls";
        String content = ExcelUtil.readXlsContent(filePath);
        System.out.println(content);
    }
}
