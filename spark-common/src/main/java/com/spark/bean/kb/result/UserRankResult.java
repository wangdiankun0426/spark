package com.spark.bean.kb.result;

import lombok.Data;

/**
 * 用户使用排行结果
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
public class UserRankResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 检索次数
     */
    private Long retrieveCount;
}