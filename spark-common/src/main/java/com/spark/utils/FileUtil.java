package com.spark.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午8:00
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 获取文件后缀
     * @param fileName
     * @return
     */
    public static String getFileExt(String fileName) {
        if (StringUtil.isBlank(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件名而不包括后缀
     * @param fileName
     * @return
     */
    public static String getFileNameWithoutExt(String fileName) {
        if (StringUtil.isBlank(fileName)) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 转换为txt
     * @param filePath 待提取文件路径
     * @return txt文件路径
     */
    public static String convertToTxt(String filePath) {
        if (StringUtil.isBlank(filePath)) {
            return null;
        }
        String txtPath = generateTxtFile(filePath);
        String fileFile = getFileExt(filePath);
        if (StringUtil.isBlank(fileFile)) {
            return txtPath;
        }
        String fileExt = getFileExt(filePath);
        if (StringUtil.isBlank(fileExt)) {
            return null;
        }
        String content = switch (fileExt) {
            case "pdf" -> PDFUtil.readPDFContent(filePath);
            case "doc" -> WordUtil.readDocContent(filePath);
            case "docx" -> WordUtil.readDocxContent(filePath);
            case "ppt" -> PPTUtil.readPPTContent(filePath);
            case "pptx" -> PPTUtil.readPPTXContent(filePath);
            case "xls" -> ExcelUtil.readXlsContent(filePath);
            case "xlsx" -> ExcelUtil.readXlsxContent(filePath);
            default -> "";
        };
        TextUtil.writeToText(txtPath, content);
        return txtPath;
    }

    /**
     * 获取txt文件
     * @param filePath 文件路径
     * @return txt文件路径
     */
    public static String generateTxtFile(String filePath) {
        if (StringUtil.isBlank(filePath)) {
            return null;
        }
        String fileExt = getFileExt(filePath);
        if (StringUtil.isBlank(fileExt)) {
            return null;
        }
        if ("md".equals(fileExt)) {
            return FileUtil.getFileNameWithoutExt(filePath)+".md";
        }
        String txtPath = FileUtil.getFileNameWithoutExt(filePath)+".txt";
        File file = new File(txtPath);
        if (file.exists()) {
            return txtPath;
        }
        return null;
    }
}
