package com.spark.common.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 10:08
 */
public class PDFUtil {
    private static final Logger logger = LoggerFactory.getLogger(PDFUtil.class);

    /**
     * 获取pdf文件内容
     * @param filePath
     * @return
     */
    public static String readPDFContent(String filePath) {
        File file = new File(filePath);
        PdfDocument pdfDoc = null;
        try {
            // 加载PDF文档
            pdfDoc = new PdfDocument(new PdfReader(file));
            // 提取文本
            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
            }
            return text.toString();
        } catch (IOException e) {
            logger.error("readPDFContent error, file path is {}", filePath, e);
            return "";
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }
}
