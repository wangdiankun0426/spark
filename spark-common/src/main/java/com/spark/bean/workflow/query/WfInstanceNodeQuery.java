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
 * AI工作流实例节点运行记录查询参数
 */
@Data
public class WfInstanceNodeQuery extends BaseQuery {

    /**
     * 运行实例ID
     */
    private Long instanceId;

}
