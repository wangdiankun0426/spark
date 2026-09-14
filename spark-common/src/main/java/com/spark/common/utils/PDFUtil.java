package com.spark.common.utils;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/3/21 10:08
 */
@Component
public class PDFUtil {
    private static final Logger logger = LoggerFactory.getLogger(PDFUtil.class);
    @Autowired
    private OCRUtil ocrUtil;

    /**
     * 读取PDF内容
     * @param filePath
     * @return
     */
    public String readPDFContent(String filePath) {
        File file = new File(filePath);
        StringBuilder fullText = new StringBuilder();
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(file));
             PDDocument pdDoc = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(pdDoc);
            int totalPages = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                PdfPage page = pdfDoc.getPage(i);
                boolean hasImage = checkPageHasImage(page);
                if (hasImage) {
                    // DPI 建议 200~300，越高越清晰但越慢
                    BufferedImage pageImage = renderer.renderImageWithDPI(i - 1, 250);
                    String ocrText = ocrUtil.getContent(pageImage);
                    fullText.append(ocrText);
                } else {
                    String pageText = PdfTextExtractor.getTextFromPage(page);
                    fullText.append(pageText);
                }
            }
            return fullText.toString();
        } catch (IOException e) {
            logger.error("readPDFContent error, file path is {}", filePath, e);
            return "";
        }
    }

    /**
     * 判断页面中是否存在图片
     * 原理：遍历页面资源字典中的 XObject，检查其 Subtype 是否为 Image
     */
    private static boolean checkPageHasImage(PdfPage page) {
        PdfResources resources = page.getResources();
        if (resources == null) {
            return false;
        }
        PdfDictionary xObjects = resources.getResource(PdfName.XObject);
        if (xObjects == null) {
            return false;
        }
        for (PdfName name : xObjects.keySet()) {
            PdfStream xObject = xObjects.getAsStream(name);
            if (xObject != null) {
                PdfName subtype = xObject.getAsName(PdfName.Subtype);
                if (PdfName.Image.equals(subtype)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\w1561\\Desktop\\test.pdf";
        String content = new PDFUtil().readPDFContent(filePath);
        System.out.println("pdf读取内容为:");
        System.out.println(content);
    }
}
