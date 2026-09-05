package com.spark.common.bean.workflow.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow运行实例提交参数
 */
@Data
public class WfInstanceVO extends BaseVO {

    /**
     * 工作流模板ID
     */
    private Long templateId;

}
