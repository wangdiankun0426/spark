package com.spark.workflow.engine.executor;

import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.WorkflowTemplateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.utils.JsonUtil;
import com.spark.utils.MapUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
 * 代码执行节点执行器：支持SpEL表达式计算
 * 注：生产环境建议使用沙箱（如 GraalVM JS/Ruby 或 Docker 容器）执行不受信任的代码
 */
@Component
public class CodeExecuteExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CodeExecuteExecutor.class);
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.CODE_EXECUTE.getValue();
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
        String codeType = MapUtil.getStringVal(config, "codeType", "spel");
        String code = MapUtil.getStringVal(config, "code");
        if (StringUtil.isBlank(code)) {
            throw new IllegalArgumentException("代码执行节点未配置代码");
        }
        // 解析变量引用
        String resolvedCode = super.generateFlowValue(code, null, valueMap, showValueMap, input);
        try {
            String result = switch (codeType) {
                case "spel" -> executeSpEL(resolvedCode, input, valueMap, showValueMap);
                case "json" -> executeJsonOperation(resolvedCode, config, input, valueMap, showValueMap);
                case "string" -> executeStringOperation(resolvedCode, config, input, valueMap, showValueMap);
                case "math" -> executeMathOperation(resolvedCode, config, input, valueMap, showValueMap);
                default -> throw new IllegalArgumentException("不支持的代码类型: " + codeType);
            };
            output.put(nodeId + "result", result);
            logger.info("CodeExecute node executed, codeType={}, result length={}", codeType, result.length());
        } catch (Exception e) {
            logger.error("CodeExecute execution failed, codeType={}", codeType, e);
            throw new RuntimeException("代码执行失败: " + e.getMessage(), e);
        }
        return output;
    }

    /**
     * 执行 SpEL 表达式
     */
    private String executeSpEL(String expression, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, Object> variables = new HashMap<>();
        if (input != null) {
            variables.putAll(input);
        }
        if (valueMap != null) {
            variables.putAll(valueMap);
        }
        if (showValueMap != null) {
            for (Map.Entry<String, String> entry : showValueMap.entrySet()) {
                variables.put("show_" + entry.getKey(), entry.getValue());
            }
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapPropertyAccessor());
        context.setRootObject(variables);
        Expression parsed = parser.parseExpression(expression);
        Object value = parsed.getValue(context);
        return value != null ? String.valueOf(value) : "";
    }

    /**
     * 执行 JSON 操作
     */
    private String executeJsonOperation(String code, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        String operation = MapUtil.getStringVal(config, "jsonOperation", "parse");
        switch (operation) {
            case "parse":
                // 验证 JSON 格式
                if (JsonUtil.isValidJson(code)) {
                    return code;
                }
                throw new IllegalArgumentException("无效的JSON格式");
            case "extract":
                // 提取 JSON 字段
                String path = MapUtil.getStringVal(config, "jsonPath");
                if (StringUtil.isBlank(path)) {
                    throw new IllegalArgumentException("JSON提取操作未配置路径");
                }
                return extractJsonField(code, path);
            case "merge":
                // 合并 JSON
                String json2 = MapUtil.getStringVal(config, "json2");
                if (StringUtil.isBlank(json2)) {
                    throw new IllegalArgumentException("JSON合并操作未配置第二个JSON");
                }
                return mergeJson(code, json2);
            default:
                throw new IllegalArgumentException("不支持的JSON操作: " + operation);
        }
    }

    /**
     * 执行字符串操作
     */
    private String executeStringOperation(String code, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        String operation = MapUtil.getStringVal(config, "stringOperation", "length");
        switch (operation) {
            case "length":
                return String.valueOf(code.length());
            case "substring":
                int start = MapUtil.getIntegerVal(config, "start", 0);
                int end = MapUtil.getIntegerVal(config, "end", code.length());
                return code.substring(Math.min(start, code.length()), Math.min(end, code.length()));
            case "replace":
                String oldStr = MapUtil.getStringVal(config, "oldStr");
                String newStr = MapUtil.getStringVal(config, "newStr", "");
                if (StringUtil.isBlank(oldStr)) {
                    throw new IllegalArgumentException("字符串替换操作未配置查找字符串");
                }
                return code.replace(oldStr, newStr);
            case "split":
                String separator = MapUtil.getStringVal(config, "separator", ",");
                String[] parts = code.split(separator);
                return String.join("\n", parts);
            case "trim":
                return code.trim();
            case "uppercase":
                return code.toUpperCase();
            case "lowercase":
                return code.toLowerCase();
            default:
                throw new IllegalArgumentException("不支持的字符串操作: " + operation);
        }
    }

    /**
     * 执行数学运算
     */
    private String executeMathOperation(String code, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        String operation = MapUtil.getStringVal(config, "mathOperation", "eval");
        switch (operation) {
            case "eval":
                // 使用 SpEL 计算数学表达式
                return executeSpEL(code, input, valueMap, showValueMap);
            case "abs":
                double absVal = Double.parseDouble(code);
                return String.valueOf(Math.abs(absVal));
            case "round":
                double roundVal = Double.parseDouble(code);
                return String.valueOf(Math.round(roundVal));
            case "ceil":
                double ceilVal = Double.parseDouble(code);
                return String.valueOf(Math.ceil(ceilVal));
            case "floor":
                double floorVal = Double.parseDouble(code);
                return String.valueOf(Math.floor(floorVal));
            case "max":
                String val2 = MapUtil.getStringVal(config, "val2");
                if (StringUtil.isBlank(val2)) {
                    throw new IllegalArgumentException("数学运算max未配置第二个值");
                }
                double d1 = Double.parseDouble(code);
                double d2 = Double.parseDouble(val2);
                return String.valueOf(Math.max(d1, d2));
            case "min":
                String minVal2 = MapUtil.getStringVal(config, "val2");
                if (StringUtil.isBlank(minVal2)) {
                    throw new IllegalArgumentException("数学运算min未配置第二个值");
                }
                double minD1 = Double.parseDouble(code);
                double minD2 = Double.parseDouble(minVal2);
                return String.valueOf(Math.min(minD1, minD2));
            default:
                throw new IllegalArgumentException("不支持的数学运算: " + operation);
        }
    }

    /**
     * 提取 JSON 字段
     */
    private String extractJsonField(String json, String path) {
        try {
            Map<String, Object> map = JsonUtil.toObject(json, Map.class);
            if (map == null) {
                return "";
            }
            String[] keys = path.split("\\.");
            Object current = map;
            for (String key : keys) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(key);
                } else {
                    return "";
                }
            }
            return current != null ? String.valueOf(current) : "";
        } catch (Exception e) {
            throw new RuntimeException("JSON字段提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 合并 JSON
     */
    private String mergeJson(String json1, String json2) {
        try {
            Map<String, Object> map1 = JsonUtil.toObject(json1, Map.class);
            Map<String, Object> map2 = JsonUtil.toObject(json2, Map.class);
            if (map1 == null || map2 == null) {
                throw new IllegalArgumentException("无效的JSON格式");
            }
            map1.putAll(map2);
            return JsonUtil.toString(map1);
        } catch (Exception e) {
            throw new RuntimeException("JSON合并失败: " + e.getMessage(), e);
        }
    }

    /**
     * 支持 SpEL 以裸属性名读取 Map 变量
     */
    private static class MapPropertyAccessor implements PropertyAccessor {
        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            return target instanceof Map && ((Map<?, ?>) target).containsKey(name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) {
            return new TypedValue(((Map<?, ?>) target).get(name));
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) {
        }

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }
    }
}
