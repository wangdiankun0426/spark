package com.spark.bean.flow.query;

import com.spark.bean.base.BaseQuery;
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
     * 流程实例名称(模糊查询)
     */
    private String name;
}
