package com.spark.bean.flow.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-10-29 20:57:44
 */
@Data
public class FlowTemplateResult extends BaseResult {

    /**
     * 名称
     **/
    private String name;

    /**
     * 模板id
     **/
    private String processId;

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 表单版本id
     */
    private Long formRevId;

    /**
     * 版本id
     **/
    private Long revId;

    /**
     * 版本
     **/
    private String revNum;

    /**
     * 状态
     **/
    private Integer status;

    /**
     * 状态名称
     **/
    private String statusName;

    /**
     * 流程类型
     **/
    private Integer type;

    /**
     * 流程类型名称
     **/
    private String typeName;

    /**
     * 备注
     **/
    private String remark;

    /**
     * 表单json
     **/
    private String formJson;

    /**
     * 流程图json
     **/
    private String bpmJson;
}
