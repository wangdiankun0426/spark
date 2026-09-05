package com.spark.common.utils;

import com.spark.common.bean.base.ResultData;
import com.spark.common.enums.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

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
        FileReader fr;
        BufferedReader br;
        String text;
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);
            String tempContent;
            StringBuilder textContent = new StringBuilder();
            while ((tempContent = br.readLine()) != null) {
                textContent.append(tempContent);
                if (withFormat) {
                    textContent.append("\n");
                }
            }
            text = textContent.toString();
            result.setCode(ResultData.OK);
            result.setData(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将字符串写入text中
     * @param content 要写入的文本
     */
    public static ResultData<Void> writeToText(String filePath, String content) {
        ResultData<Void> result = new ResultData<>();
        if ( StringUtil.isBlank(filePath) ||  StringUtil.isBlank(content)) {
            logger.error("writeToText error, params no compete !");
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        File file = new File(filePath);
        if (file.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_EXIST);
            return result;
        }
        FileWriter writer = null;
        try {
            boolean bo = file.createNewFile();
            if (!bo) {
                return result;
            }
            writer = new FileWriter(file);
            writer.write(content);
        } catch (IOException e) {
            logger.error("writeToText error, create new file fail , file path is {}", filePath, e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                logger.error("writeToText error, close writer fail" , e);
            }
        }
        result.setCode(ResultData.OK);
        return result;
    }
}
