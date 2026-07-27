package com.spark.bean.flow.vo;

import com.spark.bean.base.BaseVO;
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

}
