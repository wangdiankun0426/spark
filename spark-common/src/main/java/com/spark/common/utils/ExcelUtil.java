package com.spark.common.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/22 下午4:31
 */
public class ExcelUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 读取xls文件内容
     * @param filePath
     * @return
     */
    public static String readXlsContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new HSSFWorkbook(fis);
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                content.append(cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    content.append(cell.getDateCellValue());
                                } else {
                                    content.append(cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                content.append(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                content.append(cell.getCellFormula());
                                break;
                            default:
                                break;
                        }
                        content.append("\t");
                    }
                    content.append("\n");
                }
            }
        } catch (IOException e) {
            logger.error("readXlsContent error, file path is {}", filePath, e);
        }
        return content.toString();
    }

    /**
     * 读取xlsx文件内容
     * @param filePath
     * @return
     */
    public static String readXlsxContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fis);
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                content.append(cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    content.append(cell.getDateCellValue());
                                } else {
                                    content.append(cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                content.append(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                content.append(cell.getCellFormula());
                                break;
                            default:
                                break;
                        }
                        content.append("\t");
                    }
                    content.append("\n");
                }
            }
        } catch (IOException e) {
            logger.error("readXlsxContent error, file path is {}", filePath, e);
        }
        return content.toString();
    }

    }
