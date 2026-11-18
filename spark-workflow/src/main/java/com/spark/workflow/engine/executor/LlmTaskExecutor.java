package com.spark.workflow.engine.executor;

import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.common.llm.ModelUsageSceneHolder;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.manage.BaseService;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 * LLM节点执行器：提示词支持 form/formTxt/node 三类 #{type:键}# 变量引用
 * 文档内容经前置文档解析节点输出，由 node 变量引入提示词
 */
@Component
public class LlmTaskExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LlmTaskExecutor.class);
    @Autowired
    private ModelFactory modelFactory;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() { return WorkflowTemplateTypeEnum.LLM_TASK.getValue(); }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        Long modelId = MapUtil.getLongVal(config, "modelId");
        if (modelId == null) {
            throw new IllegalArgumentException("LLM节点未配置大模型");
        }
        String promptStr = MapUtil.getStringVal(config, "prompt");
        String prompt = super.generateFlowValue(promptStr, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(prompt)) {
            throw new IllegalArgumentException("LLM节点系统提示词参数解析失败");
        }
        ModelUsageSceneHolder.push(ModelUsageSceneEnum.WORKFLOW_LLM);
        try {
            ChatModel model = modelFactory.getChatModel(modelId);
            String response = model.chat(prompt);
            output.put(nodeId+"text", response);
            boolean validJson = JsonUtil.isValidJson(response);
            if (validJson) {
                HashMap<String, Object> map = JsonUtil.toObject(response, HashMap.class);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String text = String.valueOf(value);
                    output.put(nodeId+key, text);
                }
            }
            logger.info("llm node executed");
        } catch (Exception e) {
            logger.error("llm execution failed", e);
            throw ModelErrorClassifier.wrap(e);
        } finally {
            ModelUsageSceneHolder.pop();
        }
        return output;
    }

}
