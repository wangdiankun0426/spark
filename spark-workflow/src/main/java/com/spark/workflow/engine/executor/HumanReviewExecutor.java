package com.spark.workflow.engine.executor;

import com.spark.common.bean.workflow.exception.HumanReviewRequiredException;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 18:00:00
 * 人工审核节点执行器：暂停工作流执行，等待人工审核
 */
@Component
public class HumanReviewExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(HumanReviewExecutor.class);

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.HUMAN_REVIEW.getValue();
    }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        String promptCode = MapUtil.getStringVal(config, "prompt");
        String prompt = "请审核";
        if (StringUtil.isNotBlank(promptCode)) {
            String resolvedPrompt = super.generateFlowValue(promptCode, null, valueMap, showValueMap, input);
            if (StringUtil.isNotBlank(resolvedPrompt)) {
                prompt = resolvedPrompt;
            }
        }
        String reviewerIdsCode = MapUtil.getStringVal(config, "reviewerIds");
        String reviewerIds = null;
        if (StringUtil.isNotBlank(reviewerIdsCode)) {
            reviewerIds = super.generateFlowValue(reviewerIdsCode, null, valueMap, showValueMap, input);
        }
        String reviewType = MapUtil.getStringVal(config, "reviewType", "approve");
        boolean requireComment = MapUtil.getBooleanVal(config, "requireComment", false);
        output.put(nodeId + "prompt", prompt);
        output.put(nodeId + "reviewType", reviewType);
        output.put(nodeId + "requireComment", String.valueOf(requireComment));
        if (StringUtil.isNotBlank(reviewerIds)) {
            output.put(nodeId + "reviewerIds", reviewerIds);
        }
        throw new HumanReviewRequiredException(nodeId, prompt, reviewerIds, reviewType, requireComment);
    }
}
