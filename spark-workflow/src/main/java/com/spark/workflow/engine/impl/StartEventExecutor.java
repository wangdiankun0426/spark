package com.spark.workflow.engine.impl;

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
public class StartEventExecutor implements WorkflowNodeExecutor {

    @Override
    public String getNodeType() { return "startEvent"; }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> config, Map<String, Object> input, Map<String, Object> context) {
        Map<String, Object> output = new HashMap<>();
        // 将context中的输入参数按inputs定义筛选
        if (config != null && config.containsKey("inputs")) {
            List<Map<String, Object>> inputs = (List<Map<String, Object>>) config.get("inputs");
            if (inputs != null) {
                for (Map<String, Object> param : inputs) {
                    String name = (String) param.get("name");
                    if (context.containsKey(name)) {
                        output.put(name, context.get(name));
                    } else if (param.containsKey("default") && param.get("default") != null) {
                        output.put(name, param.get("default"));
                    }
                }
            }
        }
        // 合并所有context到output
        if (output.isEmpty()) { output.putAll(context); }
        return output;
    }
}
