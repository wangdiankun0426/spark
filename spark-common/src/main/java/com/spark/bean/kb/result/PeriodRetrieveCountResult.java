package com.spark.bean.kb.result;

import lombok.Data;

/**
 * 时段检索次数结果
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 17:00:00
 */
@Data
public class PeriodRetrieveCountResult {

    /**
     * 今日检索次数
     */
    private Long todayCount;

    /**
     * 本周检索次数
     */
    private Long weekCount;

    /**
     * 本月检索次数
     */
    private Long monthCount;
}