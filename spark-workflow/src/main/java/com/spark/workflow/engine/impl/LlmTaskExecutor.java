package com.spark.workflow.engine.impl;

import com.spark.enums.FlowTemplateTypeEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.utils.StringUtil;
import com.spark.workflow.engine.WorkflowNodeExecutor;
import dev.langchain4j.model.chat.ChatModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 */
@Component
public class LlmTaskExecutor implements WorkflowNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LlmTaskExecutor.class);
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(\\w+(?:\\.\\w+)*)\\}\\}");
    @Autowired
    private ModelFactory modelFactory;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() { return FlowTemplateTypeEnum.LLM_TASK.getValue(); }

    /**
     * 执行节点
     * @param config 节点配置（DAG JSON中的config对象）
     * @param input 当前节点输入
     * @param context 全局变量上下文（可读写）
     * @return
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> config, Map<String, Object> input, Map<String, Object> context) {
        Map<String, Object> output = new HashMap<>();
        if (config == null) {
            return output;
        }
        String prompt = (String) config.getOrDefault("prompt", "");
        Long modelId = StringUtil.isNumeric(config.get("modelId")+"") ? Long.parseLong(config.get("modelId")+"") : null;
        String resolvedPrompt = resolveVariables(prompt, context);
        try {
            ChatModel model = modelFactory.getChatModel(modelId);
            String response = model.chat(resolvedPrompt);
            output.put("text", response);
            output.put("prompt", resolvedPrompt);
            logger.info("LLM node executed, response length: {}", response.length());
        } catch (Exception e) {
            logger.error("LLM execution failed", e);
            output.put("text", "");
            output.put("error", e.getMessage());
        }
        return output;
    }

    /**
     * 替换模板变量
     * @param template
     * @param context
     * @return
     */
    private String resolveVariables(String template, Map<String, Object> context) {
        if (template == null) {
            return "";
        }
        Matcher m = VAR_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String varName = m.group(1);
            Object value = resolveNestedVar(varName, context);
            m.appendReplacement(sb, value != null ? Matcher.quoteReplacement(value.toString()) : "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     *
     * @param path
     * @param context
     * @return
     */
    private Object resolveNestedVar(String path, Map<String, Object> context) {
        String[] parts = path.split("\\.");
        Object current = context;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return path;
            }
        }
        return current != null ? current : "";
    }
}
