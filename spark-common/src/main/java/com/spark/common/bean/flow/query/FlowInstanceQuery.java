package com.spark.common.bean.flow.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-02 15:47:22
 */
@Data
public class FlowInstanceQuery extends BaseQuery {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程模板版本id
     */
    private Long templateRevId;

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 表单版本id
     */
    private Long formRevId;

    /**
     * 模板id
     */
    private String processId;

    /**
     * flowable 流程实例id
     **/
    private String flowableInstanceId;

    /**
     * flowable 批量流程实例id
     **/
    private List<String> flowableInstanceIds;

    /**
     * 流程状态
     */
    private Integer status;

    /**
     * 流程状态集合：用于「进行中」这类跨多个状态的分组筛选
     */
    private List<Integer> statusList;

    /**
     * 紧急程度：1-一般 2-重要 3-紧急
     */
    private Integer level;

    /**
     * 流程实例名称(模糊查询)
     */
    private String name;
}
