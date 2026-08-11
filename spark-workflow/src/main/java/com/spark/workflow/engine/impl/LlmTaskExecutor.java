package com.spark.workflow.engine.impl;

import com.spark.llm.model.ModelFactory;
import com.spark.workflow.engine.WorkflowNodeExecutor;
import dev.langchain4j.model.chat.ChatModel;
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

    @Override
    public String getNodeType() { return "llmTask"; }

    @Override
    public Map<String, Object> execute(Map<String, Object> config, Map<String, Object> input, Map<String, Object> context) {
        Map<String, Object> output = new HashMap<>();
        String prompt = config != null ? (String) config.getOrDefault("prompt", "") : "";
        Long modelId = config != null ? toLong(config.get("modelId")) : null;

        // 替换模板变量 {{varName}}
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

    private String resolveVariables(String template, Map<String, Object> context) {
        if (template == null) return "";
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

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return null; }
    }
}
