package com.spark.bean.flow.result;

import com.spark.bean.base.BaseResult;
import com.spark.bean.form.result.FormObjValueResult;
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
public class FlowInstanceResult extends BaseResult {

    /**
     * 流程模板id
     **/
    private Long templateId;

    /**
     * 流程模板版本id
     **/
    private Long templateRevId;

    /**
     * 表单id
     **/
    private Long formId;

    /**
     * 表单版本id
     **/
    private Long formRevId;

    /**
     * 模板id
     **/
    private String processId;

    /**
     * flowable 流程实例id
     **/
    private String flowableInstanceId;

    /**
     * 流程实例状态
     **/
    private Integer status;

    /**
     * 紧急程度 FlowInstanceLevelEnum
     **/
    private Integer level;

    /**
     * 紧急程度名称
     **/
    private String levelName;

    /**
     * 申请部门id
     **/
    private Long deptId;

    /**
     * 申请部门名称
     **/
    private String deptName;

    /**
     * 流程实例名称
     **/
    private String name;

    /**
     * 流程实例描述
     **/
    private String description;

    /**
     * 状态名称
     **/
    private String statusName;

    /**
     * 表单json
     **/
    private String formJson;

    /**
     * 流程定义json
     **/
    private String bpmJson;

    /**
     * 表单值列表
     **/
    private List<FormObjValueResult> values;

    /**
     * 节点列表
     **/
    private List<FlowInstanceNodeResult> nodes;

    /**
     * 待审批节点id
     */
    private Long nodeId;

    /**
     * 节点权限
     */
    private Integer permission;
}
