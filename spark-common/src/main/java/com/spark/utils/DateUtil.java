package com.spark.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/13 14:46
 */
public class DateUtil {
    private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);
    public static final String YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前时间
     * @param format
     * @return
     */
    public static String getCurrentTime(String format) {
        if( StringUtil.isBlank(format)) {
            format = YYYYMMDD_HHMMSS;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    /**
     * 获取几小时之后的时间
     * @param date
     * @param hours
     * @return
     */
    public static Date offsetHour(Date date, Integer hours) {
        return new Date(date.getTime() + hours * 60 * 60 * 1000);
    }
}
