package com.spark.common.bean.flow.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务结果
 */
@Data
public class FlowTemplateNodeTaskResult extends BaseResult {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程版本id
     */
    private Long revId;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 任务模板id
     */
    private Long taskTemplateId;

    /**
     * 执行时机 FlowNodeTaskExecuteEnum
     */
    private Integer executeType;

    /**
     * 执行顺序
     */
    private Integer sort;

    /**
     * 任务参数列表
     */
    private List<FlowTemplateNodeTaskParamResult> params;

}
