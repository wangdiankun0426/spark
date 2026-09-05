package com.spark.workflow.engine.executor;

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
 * @since 2026-08-26 17:00:00
 * 变量操作节点执行器：支持变量赋值、转换、合并等操作
 */
@Component
public class VariableOpExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(VariableOpExecutor.class);

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.VARIABLE_OP.getValue();
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
        String operation = MapUtil.getStringVal(config, "operation");
        if (StringUtil.isBlank(operation)) {
            throw new IllegalArgumentException("变量操作节点未配置操作类型");
        }
        switch (operation) {
            case "set":
                handleSetOperation(nodeId, config, input, valueMap, showValueMap, output);
                break;
            case "merge":
                handleMergeOperation(nodeId, config, input, valueMap, showValueMap, output);
                break;
            case "concat":
                handleConcatOperation(nodeId, config, input, valueMap, showValueMap, output);
                break;
            case "format":
                handleFormatOperation(nodeId, config, input, valueMap, showValueMap, output);
                break;
            case "convert":
                handleConvertOperation(nodeId, config, input, valueMap, showValueMap, output);
                break;
            default:
                throw new IllegalArgumentException("不支持的操作类型: " + operation);
        }
        logger.info("VariableOp node executed, operation={}", operation);
        return output;
    }

    /**
     * 处理赋值操作
     * 将一个或多个变量赋值给输出变量
     */
    private void handleSetOperation(String nodeId, Map<String, Object> config,
                                    Map<String, String> input, Map<String, String> valueMap,
                                    Map<String, String> showValueMap, Map<String, String> output) {
        List<Map<String, String>> variables = (List<Map<String, String>>) config.get("variables");
        if (variables == null || variables.isEmpty()) {
            throw new IllegalArgumentException("赋值操作未配置变量");
        }
        for (Map<String, String> var : variables) {
            String name = var.get("name");
            String valueCode = var.get("value");
            if (StringUtil.isBlank(name)) {
                continue;
            }
            String value = super.generateFlowValue(valueCode, null, valueMap, showValueMap, input);
            output.put(nodeId + name, value != null ? value : "");
        }
    }

    /**
     * 处理合并操作
     * 将多个变量合并为一个字符串
     */
    private void handleMergeOperation(String nodeId, Map<String, Object> config,
                                      Map<String, String> input, Map<String, String> valueMap,
                                      Map<String, String> showValueMap, Map<String, String> output) {
        List<String> sourceCodes = (List<String>) config.get("sources");
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            throw new IllegalArgumentException("合并操作未配置源变量");
        }
        String separator = MapUtil.getStringVal(config, "separator", ",");
        List<String> values = new ArrayList<>();
        for (String code : sourceCodes) {
            String value = super.generateFlowValue(code, null, valueMap, showValueMap, input);
            if (StringUtil.isNotBlank(value)) {
                values.add(value);
            }
        }
        String merged = String.join(separator, values);
        output.put(nodeId + "result", merged);
    }

    /**
     * 处理拼接操作
     * 使用模板拼接变量
     */
    private void handleConcatOperation(String nodeId, Map<String, Object> config,
                                       Map<String, String> input, Map<String, String> valueMap,
                                       Map<String, String> showValueMap, Map<String, String> output) {
        String template = MapUtil.getStringVal(config, "template");
        if (StringUtil.isBlank(template)) {
            throw new IllegalArgumentException("拼接操作未配置模板");
        }
        String result = super.generateFlowValue(template, null, valueMap, showValueMap, input);
        output.put(nodeId + "result", result);
    }

    /**
     * 处理格式化操作
     * 对变量进行格式化处理
     */
    private void handleFormatOperation(String nodeId, Map<String, Object> config,
                                       Map<String, String> input, Map<String, String> valueMap,
                                       Map<String, String> showValueMap, Map<String, String> output) {
        String sourceCode = MapUtil.getStringVal(config, "source");
        String format = MapUtil.getStringVal(config, "format");
        String source = super.generateFlowValue(sourceCode, null, valueMap, showValueMap, input);
        if (source == null) {
            source = "";
        }
        String result;
        if ("uppercase".equals(format)) {
            result = source.toUpperCase();
        } else if ("lowercase".equals(format)) {
            result = source.toLowerCase();
        } else if ("trim".equals(format)) {
            result = source.trim();
        } else if ("number".equals(format)) {
            try {
                double num = Double.parseDouble(source.trim());
                result = String.valueOf(num);
            } catch (NumberFormatException e) {
                result = source;
            }
        } else {
            result = source;
        }

        output.put(nodeId + "result", result);
    }

    /**
     * 处理转换操作
     * 将变量转换为指定类型
     */
    private void handleConvertOperation(String nodeId, Map<String, Object> config,
                                        Map<String, String> input, Map<String, String> valueMap,
                                        Map<String, String> showValueMap, Map<String, String> output) {
        String sourceCode = MapUtil.getStringVal(config, "source");
        String targetType = MapUtil.getStringVal(config, "targetType");
        String source = super.generateFlowValue(sourceCode, null, valueMap, showValueMap, input);
        if (source == null) {
            source = "";
        }
        String result;
        switch (targetType) {
            case "string":
                result = source;
                break;
            case "number":
                try {
                    double num = Double.parseDouble(source.trim());
                    result = String.valueOf(num);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("无法将值转换为数字: " + source);
                }
                break;
            case "boolean":
                result = String.valueOf(Boolean.parseBoolean(source.trim()));
                break;
            case "json":
                if (source.startsWith("{") || source.startsWith("[")) {
                    result = source;
                } else {
                    throw new IllegalArgumentException("无法将值转换为JSON: " + source);
                }
                break;
            default:
                result = source;
        }
        output.put(nodeId + "result", result);
    }
}
