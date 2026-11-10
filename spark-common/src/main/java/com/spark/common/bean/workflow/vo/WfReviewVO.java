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
 * @since 2026-11-10 10:00:00
 * 工作流人工审核入参
 */
@Data
public class WfReviewVO extends BaseVO {

    /**
     * 运行实例ID
     */
    private Long instanceId;

    /**
     * 审核结果：true通过/false驳回
     */
    private Boolean approved;

    /**
     * 审核意见
     */
    private String comment;
}
