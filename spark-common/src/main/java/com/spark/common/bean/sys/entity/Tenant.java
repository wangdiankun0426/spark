package com.spark.common.bean.sys.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-08 10:00:00
 * 租户
 */
@Data
public class Tenant extends BaseEntity {

    /**
     * 租户名
     */
    private String name;

    /**
     * 状态 StatusEnum
     */
    private Integer status;

    /**
     * 截止时间
     */
    private Timestamp deadline;

    /**
     * 账号数量
     */
    private Integer accountCount;
}
