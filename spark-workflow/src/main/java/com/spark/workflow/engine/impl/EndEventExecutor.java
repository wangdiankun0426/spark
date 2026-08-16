package com.spark.workflow.engine.impl;

import com.spark.enums.FlowTemplateTypeEnum;
import com.spark.workflow.engine.WorkflowNodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 */
@Component
public class EndEventExecutor implements WorkflowNodeExecutor {

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return FlowTemplateTypeEnum.END_EVENT.getValue();
    }

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
        if (config != null && config.containsKey("outputs")) {
            List<Map<String, String>> outputs = (List<Map<String, String>>) config.get("outputs");
            if (outputs != null) {
                for (Map<String, String> mapping : outputs) {
                    String name = mapping.get("name");
                    String source = mapping.get("source");
                    if (name != null && source != null) {
                        Object value = resolveValue(source, input, context);
                        output.put(name, value);
                    }
                }
            }
        }
        if (output.isEmpty()) { output.putAll(input); }
        return output;
    }

    private Object resolveValue(String path, Map<String, Object> input, Map<String, Object> context) {
        if (path == null || path.trim().isEmpty()) { return ""; }
        path = path.trim();
        String[] parts = path.split("\\.");

        // 写法1: nodeId.output.fieldName（节点路径，从context中按节点ID取输出）
        if (parts.length >= 3 && "output".equals(parts[1])) {
            Object nodeOutput = context.get(parts[0]);
            if (nodeOutput instanceof Map) {
                Object val = ((Map<?, ?>) nodeOutput).get(parts[2]);
                for (int i = 3; i < parts.length; i++) {
                    if (val instanceof Map) {
                        val = ((Map<?, ?>) val).get(parts[i]);
                    } else {
                        return "";
                    }
                }
                return val != null ? val : "";
            }
            return "";
        }

        // 写法2: fieldName（直接字段名，从input/context中取）
        Object val = input.get(path);
        if (val == null) { val = context.get(path); }
        return val != null ? val : "";
    }
}
