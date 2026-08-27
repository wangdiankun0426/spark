package com.spark.workflow.engine;

import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 15:00:00
 * 工作流变量空间
 * 参考：Dify VariablePool
 * 统一管理全局变量和节点输出，支持多种变量引用方式：
 * - ${globalVar} - 全局变量
 * - ${nodeId.outputKey} - 节点输出
 * - ${nodeId} - 节点完整输出（JSON）
 */
public class WorkflowVariableSpace {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowVariableSpace.class);

    /**
     * 变量引用正则：${xxx} 或 ${xxx.yyy}
     */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 全局变量（用户输入）
     */
    private final Map<String, String> globalVars = new HashMap<>();

    /**
     * 节点输出：nodeId -> outputMap
     */
    private final Map<String, Map<String, String>> nodeOutputs = new HashMap<>();

    /**
     * 表单值
     */
    private final Map<String, String> formValues = new HashMap<>();

    /**
     * 表单显示值
     */
    private final Map<String, String> formDisplayValues = new HashMap<>();

    /**
     * 构造函数
     */
    public WorkflowVariableSpace() {
    }

    /**
     * 构造函数
     * @param globalVars 全局变量
     * @param formValues 表单值
     * @param formDisplayValues 表单显示值
     */
    public WorkflowVariableSpace(Map<String, String> globalVars, Map<String, String> formValues, Map<String, String> formDisplayValues) {
        if (globalVars != null) {
            this.globalVars.putAll(globalVars);
        }
        if (formValues != null) {
            this.formValues.putAll(formValues);
        }
        if (formDisplayValues != null) {
            this.formDisplayValues.putAll(formDisplayValues);
        }
    }

    /**
     * 设置全局变量
     * @param key 变量名
     * @param value 变量值
     */
    public void setGlobalVar(String key, String value) {
        globalVars.put(key, value);
    }

    /**
     * 批量设置全局变量
     * @param vars 变量Map
     */
    public void setGlobalVars(Map<String, String> vars) {
        if (vars != null) {
            globalVars.putAll(vars);
        }
    }

    /**
     * 获取全局变量
     * @param key 变量名
     * @return 变量值
     */
    public String getGlobalVar(String key) {
        return globalVars.get(key);
    }

    /**
     * 设置节点输出
     * @param nodeId 节点ID
     * @param output 节点输出
     */
    public void setNodeOutput(String nodeId, Map<String, String> output) {
        nodeOutputs.put(nodeId, output);
    }

    /**
     * 获取节点输出
     * @param nodeId 节点ID
     * @return 节点输出
     */
    public Map<String, String> getNodeOutput(String nodeId) {
        return nodeOutputs.get(nodeId);
    }

    /**
     * 获取节点输出的某个值
     * @param nodeId 节点ID
     * @param key 输出键
     * @return 输出值
     */
    public String getNodeOutputValue(String nodeId, String key) {
        Map<String, String> output = nodeOutputs.get(nodeId);
        if (output == null) {
            return null;
        }
        return output.get(key);
    }

    /**
     * 设置表单值
     * @param key 表单字段
     * @param value 字段值
     */
    public void setFormValue(String key, String value) {
        formValues.put(key, value);
    }

    /**
     * 设置表单显示值
     * @param key 表单字段
     * @param value 显示值
     */
    public void setFormDisplayValue(String key, String value) {
        formDisplayValues.put(key, value);
    }

    /**
     * 解析变量表达式
     * 支持：
     * - ${globalVar} - 全局变量
     * - ${nodeId.outputKey} - 节点输出
     * - ${form:fieldName} - 表单值
     * - ${show:fieldName} - 表单显示值
     * @param expression 表达式
     * @return 解析后的值
     */
    public String resolve(String expression) {
        if (StringUtil.isBlank(expression)) {
            return expression;
        }

        // 如果是纯变量引用 ${xxx}，直接返回值
        Matcher matcher = VAR_PATTERN.matcher(expression);
        if (matcher.matches()) {
            String varPath = matcher.group(1);
            return resolveVariable(varPath);
        }

        // 如果包含变量引用，进行替换
        StringBuffer sb = new StringBuffer();
        matcher.reset();
        while (matcher.find()) {
            String varPath = matcher.group(1);
            String value = resolveVariable(varPath);
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 解析变量路径
     * @param varPath 变量路径
     * @return 变量值
     */
    private String resolveVariable(String varPath) {
        if (StringUtil.isBlank(varPath)) {
            return null;
        }

        // 表单值：form:fieldName
        if (varPath.startsWith("form:")) {
            String fieldName = varPath.substring(5);
            return formValues.get(fieldName);
        }

        // 表单显示值：show:fieldName
        if (varPath.startsWith("show:")) {
            String fieldName = varPath.substring(5);
            return formDisplayValues.get(fieldName);
        }

        // 节点输出：nodeId.outputKey
        int dotIndex = varPath.indexOf('.');
        if (dotIndex > 0) {
            String nodeId = varPath.substring(0, dotIndex);
            String outputKey = varPath.substring(dotIndex + 1);
            return getNodeOutputValue(nodeId, outputKey);
        }

        // 全局变量或节点ID
        // 优先查找全局变量
        String globalValue = globalVars.get(varPath);
        if (globalValue != null) {
            return globalValue;
        }

        // 尝试查找节点输出（返回JSON格式）
        Map<String, String> nodeOutput = nodeOutputs.get(varPath);
        if (nodeOutput != null && !nodeOutput.isEmpty()) {
            // 返回节点输出的text字段或第一个值
            String textValue = nodeOutput.get(varPath + "text");
            if (textValue != null) {
                return textValue;
            }
            return nodeOutput.values().iterator().next();
        }

        // 最后尝试表单值
        String formValue = formValues.get(varPath);
        if (formValue != null) {
            return formValue;
        }

        logger.debug("Variable not found: {}", varPath);
        return null;
    }

    /**
     * 合并所有变量为一个Map（用于SpEL求值等场景）
     * @return 合并后的变量Map
     */
    public Map<String, Object> mergeAll() {
        Map<String, Object> merged = new HashMap<>();

        // 添加全局变量
        for (Map.Entry<String, String> entry : globalVars.entrySet()) {
            merged.put(entry.getKey(), parseNumberValue(entry.getValue()));
        }

        // 添加表单值
        for (Map.Entry<String, String> entry : formValues.entrySet()) {
            merged.put(entry.getKey(), parseNumberValue(entry.getValue()));
        }

        // 添加表单显示值
        for (Map.Entry<String, String> entry : formDisplayValues.entrySet()) {
            merged.put("show_" + entry.getKey(), parseNumberValue(entry.getValue()));
        }

        // 添加节点输出（扁平化）
        for (Map.Entry<String, Map<String, String>> entry : nodeOutputs.entrySet()) {
            String nodeId = entry.getKey();
            for (Map.Entry<String, String> outputEntry : entry.getValue().entrySet()) {
                merged.put(nodeId + "." + outputEntry.getKey(), parseNumberValue(outputEntry.getValue()));
            }
        }

        return merged;
    }

    /**
     * 获取所有节点输出
     * @return 节点输出Map
     */
    public Map<String, Map<String, String>> getAllNodeOutputs() {
        return new HashMap<>(nodeOutputs);
    }

    /**
     * 获取所有全局变量
     * @return 全局变量Map
     */
    public Map<String, String> getAllGlobalVars() {
        return new HashMap<>(globalVars);
    }

    /**
     * 获取所有表单值
     * @return 表单值Map
     */
    public Map<String, String> getAllFormValues() {
        return new HashMap<>(formValues);
    }

    /**
     * 获取所有表单显示值
     * @return 表单显示值Map
     */
    public Map<String, String> getAllFormDisplayValues() {
        return new HashMap<>(formDisplayValues);
    }

    /**
     * 将字符串解析为数值类型，解析失败返回原字符串
     * @param value 变量值
     * @return 数值或原字符串
     */
    private Object parseNumberValue(String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public String toString() {
        return "WorkflowVariableSpace{" +
                "globalVars=" + globalVars.size() +
                ", nodeOutputs=" + nodeOutputs.size() +
                ", formValues=" + formValues.size() +
                '}';
    }
}
