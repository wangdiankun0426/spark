package com.spark.common.bean.workflow.exception;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 * 人工审核所需异常
 */
public class HumanReviewRequiredException extends RuntimeException {
    /**
     * 节点ID
     */
    private final String nodeId;

    /**
     * 审核提示信息
     */
    private final String prompt;

    /**
     * 审核人ID列表
     */
    private final String reviewerIds;

    public HumanReviewRequiredException(String nodeId, String prompt, String reviewerIds) {
        super("需要人工审核: " + prompt);
        this.nodeId = nodeId;
        this.prompt = prompt;
        this.reviewerIds = reviewerIds;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getReviewerIds() {
        return reviewerIds;
    }
}
