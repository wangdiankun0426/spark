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
 * @since 2025-11-01 13:34:43
 */
@Data
public class FlowTemplateVersionVO extends BaseVO {

    /**
     * 模板id
     **/
    private Long templateId;

    /**
     * flowable json
     **/
    private String bpmJson;

}
