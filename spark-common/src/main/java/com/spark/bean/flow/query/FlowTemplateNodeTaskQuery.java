package com.spark.bean.flow.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务查询
 */
@Data
public class FlowTemplateNodeTaskQuery extends BaseQuery {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程版本id
     */
    private Long revId;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 执行时机 FlowNodeTaskExecuteEnum
     */
    private Integer executeType;

}
