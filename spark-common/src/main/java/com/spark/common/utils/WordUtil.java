package com.spark.common.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/22 下午4:20
 */
public class WordUtil {
    private static final Logger logger = LoggerFactory.getLogger(WordUtil.class);

    /**
     * 读取 .docx 文件的内容
     * @param filePath 文件路径
     * @return 文件内容
     * @throws IOException 如果读取文件时发生错误
     */
    public static String readDocxContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XWPFDocument document = new XWPFDocument(fis);
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                content.append(paragraph.getText());
                content.append("\n");
            }
        } catch (IOException e) {
            logger.error("readDocx error, file path is {}", filePath);
        }
        return content.toString();
    }

    /**
     * 读取 .doc 文件的内容
     * @param filePath 文件路径
     * @return 文件内容
     * @throws IOException 如果读取文件时发生错误
     */
    public static String readDocContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            String[] paragraphs = extractor.getParagraphText();
            for (String paragraph : paragraphs) {
                content.append(paragraph);
                content.append("\n");
            }
        } catch (IOException e) {
            logger.error("readDoc error, file path is {}", filePath);
        }
        return content.toString();
    }

    }
