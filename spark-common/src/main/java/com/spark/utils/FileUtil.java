package com.spark.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    /**
     * 构造文件存储路径
     * @param basePath 配置的根路径
     * @param fileName 文件名
     * @return 完整文件路径，目录创建失败时返回 null
     */
    public static String generateFilePath(String basePath, String fileName) {
        if (StringUtil.isBlank(basePath) || StringUtil.isBlank(fileName)) {
            logger.error("basePath or fileName is blank");
            return null;
        }
        LocalDate now = LocalDate.now();
        String dirPath = appendSeparator(basePath)
                + now.format(DateTimeFormatter.ofPattern("yyyyMM"))
                + File.separator
                + now.format(DateTimeFormatter.ofPattern("dd"));
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            logger.error("mkdir fail, dirPath={}", dirPath);
            return null;
        }
        return dirPath + File.separator + fileName;
    }

    /**
     * 路径末尾补充分隔符
     * @param path 路径
     * @return 以分隔符结尾的路径
     */
    private static String appendSeparator(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            return path;
        }
        return path + File.separator;
    }

    /**
     * 复制源文件同名的伴随文件
     * @param srcPath 源文件路径
     * @param newPath 新文件路径
     * @param ext 伴随文件后缀
     */
    public static void copyCompanionFile(String srcPath, String newPath, String ext) {
        String fileExt = getFileExt(srcPath);
        if (ext.equalsIgnoreCase(fileExt)) {
            return;
        }
        String srcCompanion = getFileNameWithoutExt(srcPath) + "." + ext;
        String newCompanion = getFileNameWithoutExt(newPath) + "." + ext;
        File srcFile = new File(srcCompanion);
        if (!srcFile.exists()) {
            return;
        }
        copyFile(srcCompanion, newCompanion);
    }

    /**
     * 复制文件
     * @param srcPath 源文件路径
     * @param destPath 目标文件路径
     * @return 是否成功
     */
    public static boolean copyFile(String srcPath, String destPath) {
        if (StringUtil.isBlank(srcPath)) {
            logger.error("srcPath is blank");
            return false;
        }
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            logger.error("file not exist, path={}", srcPath);
            return false;
        }
        try {
            Files.copy(srcFile.toPath(), new File(destPath).toPath());
        } catch (IOException e) {
            logger.error("srcPath={}, destPath={}", srcPath, destPath, e);
            return false;
        }
        return true;
    }

    /**
     * 删除目录及其子文件
     * @param dir 目录
     */
    public static void deleteDir(File dir) {
        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

}
