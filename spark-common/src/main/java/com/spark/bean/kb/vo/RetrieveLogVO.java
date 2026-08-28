package com.spark.bean.kb.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * 检索日志VO
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 16:30:00
 */
@Data
public class RetrieveLogVO extends BaseVO {

    /**
     * 用户反馈评分 1-5
     */
    private Integer feedbackScore;

    /**
     * 用户反馈备注
     */
    private String feedbackRemark;
}