package com.spark.common.utils;

import com.spark.common.bean.base.ResultData;
import com.spark.common.enums.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/6 17:50
 * 文本文件工具类
 */
public class TextUtil {
    private final static Logger logger = LoggerFactory.getLogger(TextUtil.class);

    /**
     * 读取text中的字符串
     * @param filePath 文本文件路径
     * @param withFormat 是否保留换行
     * @return 文本内容
     */
    public static ResultData<String> getFromText(String filePath, boolean withFormat) {
        ResultData<String> result = new ResultData<>();
        if (StringUtil.isBlank(filePath)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        StringBuilder textContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String tempContent;
            while ((tempContent = reader.readLine()) != null) {
                textContent.append(tempContent);
                if (withFormat) {
                    textContent.append("\n");
                }
            }
        } catch (IOException e) {
            logger.error("getFromText error, filePath={}", filePath, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        String text = textContent.toString();
        result.setData(text);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 将字符串写入text中
     * @param filePath 文本文件路径
     * @param content 要写入的文本
     * @return 写入结果
     */
    public static ResultData<Void> writeToText(String filePath, String content) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(filePath) || StringUtil.isBlank(content)) {
            logger.error("writeToText error, params not complete");
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        File file = new File(filePath);
        if (file.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_EXIST);
            return result;
        }
        boolean created = false;
        try {
            created = file.createNewFile();
            if (!created) {
                result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
                return result;
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
        } catch (IOException e) {
            logger.error("writeToText error, filePath={}", filePath, e);
            if (created) {
                boolean deleted = file.delete();
                if (!deleted) {
                    logger.warn("writeToText cleanup incomplete, filePath={}", filePath);
                }
            }
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }
}
