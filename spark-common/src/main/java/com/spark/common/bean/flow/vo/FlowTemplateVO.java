package com.spark.common.bean.flow.vo;

import com.spark.common.bean.base.BaseVO;
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
public class FlowTemplateVO extends BaseVO {

    /**
     * 名称
     **/
    private String name;

    /**
     * 表单id
     **/
    private Long formId;

    /**
     * 状态
     **/
    private Integer status;

    /**
     * 流程类型
     **/
    private Integer type;

    /**
     * 备注
     **/
    private String remark;

}
