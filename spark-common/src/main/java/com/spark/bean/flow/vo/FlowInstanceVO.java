package com.spark.bean.flow.vo;

import com.spark.bean.base.BaseVO;
import com.spark.bean.form.entity.FormObjValue;
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
public class FlowInstanceVO extends BaseVO {

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
     * 表单值
     **/
    private List<FormObjValue> values;

    /**
     * 状态 FlowInstanceStatusEnum
     **/
    private Integer status;

    /**
     * 待审批节点id
     */
    private Long nodeId;

    /**
     * 审批意见
     */
    private String discuss;
}
