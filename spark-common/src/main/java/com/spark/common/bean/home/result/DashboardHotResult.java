package com.spark.common.bean.home.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 18:00:00
 * 工作台热门榜单行
 */
@Data
public class DashboardHotResult {

    /**
     * 对象id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 次数
     */
    private Integer count;

    /**
     * 热度类型 model/agent/workflow/flow
     */
    private String hotType;
}
