package com.spark.common.bean.home.query;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 18:00:00
 * 工作台统计查询参数
 */
@Data
public class DashboardQuery {

    /**
     * 统计天数，为空表示不限时间范围查全部
     */
    private Integer rangeDays;
}
