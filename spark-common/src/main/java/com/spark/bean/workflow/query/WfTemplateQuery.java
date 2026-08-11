package com.spark.bean.workflow.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 */
@Data
public class WfTemplateQuery extends BaseQuery {

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 状态：-1关闭/1开启
     */
    private Integer status;

}
