package com.spark.form.util;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.entity.FormObjValue;
import com.spark.common.bean.form.vo.FormFieldVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.FormFieldTypeEnum;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 00:00:00
 * 表单Schema解析与值规范化工具
 */
public final class FormSchemaUtil {
    private static final int CODE_MAX_LENGTH = 36;
    private static final int LABEL_MAX_LENGTH = 36;
    private static final int VALUE_MAX_LENGTH = 65535;
    private static final int SHOW_VALUE_MAX_LENGTH = 65535;
    private static final int OPTION_VALUE_MAX_LENGTH = 64;
    private static final int OPTION_LABEL_MAX_LENGTH = 512;
    private static final Set<String> EVENT_NAMES = Set.of("mounted", "change", "focus", "blur", "click");
    private static final Set<String> EVENT_ACTIONS = Set.of("set-value", "clear", "set-hidden", "set-disabled", "set-required");
    private static final Set<String> CONDITION_OPERATORS = Set.of("eq", "neq", "in", "notIn", "notEmpty", "isEmpty");
    private static final Set<String> REFERENCE_TYPES = Set.of("select-user", "select-dept", "select-role", "select-knowledge", "select-graph", "upload-attachment");
    private static final Pattern FIELD_PLACEHOLDER = Pattern.compile("#\\{([^}]+)}");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    private FormSchemaUtil() {
    }

    /**
     * 校验Schema并提取字段索引
     * @param formJson 表单Schema
     * @return 字段列表
     */
    public static ResultData<List<FormFieldVO>> analyzeFormFields(String formJson) {
        ResultData<List<FormFieldVO>> result = new ResultData<>();
        ResultData<Map<String, Object>> schemaResult = parseSchema(formJson);
        if (schemaResult.getCode() != ResultData.OK) {
            int code = schemaResult.getCode();
            String message = schemaResult.getMessage();
            result.setCode(code);
            result.setMessage(message);
            return result;
        }
        Map<String, Object> schema = schemaResult.getData();
        List<Map<String, Object>> widgetList = getWidgetList(schema);
        List<FormFieldVO> fieldList = new ArrayList<>();
        for (Map<String, Object> widget : widgetList) {
            String type = MapUtil.getStringVal(widget, "type");
            Map<String, Object> config = JsonUtil.getMap(widget, "config");
            FormFieldVO field = new FormFieldVO();
            field.setType(type);
            String code = MapUtil.getStringVal(config, "code");
            String label = MapUtil.getStringVal(config, "label");
            field.setCode(code);
            field.setLabel(label);
            fieldList.add(field);
        }
        result.setData(fieldList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按Schema校验并规范化提交值
     * @param formJson 表单Schema
     * @param values 原始提交值
     * @return 规范化提交值
     */
    public static ResultData<List<FormObjValue>> normalizeValues(String formJson, List<FormObjValue> values) {
        ResultData<List<FormObjValue>> result = new ResultData<>();
        ResultData<Map<String, Object>> schemaResult = parseSchema(formJson);
        if (schemaResult.getCode() != ResultData.OK) {
            int code = schemaResult.getCode();
            String message = schemaResult.getMessage();
            result.setCode(code);
            result.setMessage(message);
            return result;
        }
        Map<String, FormObjValue> valueMap = new HashMap<>();
        if (values != null) {
            for (FormObjValue value : values) {
                if (value == null || StringUtil.isBlank(value.getCode())) {
                    setInvalid(result, "表单字段编码不能为空");
                    return result;
                }
                String code = value.getCode();
                if (valueMap.put(code, value) != null) {
                    setInvalid(result, "表单字段" + code + "重复提交");
                    return result;
                }
            }
        }
        Map<String, Object> schema = schemaResult.getData();
        List<Map<String, Object>> widgetList = getWidgetList(schema);
        Set<String> knownCodes = new HashSet<>();
        for (Map<String, Object> widget : widgetList) {
            Map<String, Object> config = JsonUtil.getMap(widget, "config");
            String code = MapUtil.getStringVal(config, "code");
            knownCodes.add(code);
        }
        for (String code : valueMap.keySet()) {
            if (!knownCodes.contains(code)) {
                setInvalid(result, "表单字段" + code + "不存在");
                return result;
            }
        }
        List<FormObjValue> normalizedValues = new ArrayList<>();
        Set<String> dynamicHiddenCodes = findEventTargetCodes(widgetList, "set-hidden");
        Set<String> dynamicRequiredCodes = findEventTargetCodes(widgetList, "set-required");
        for (Map<String, Object> widget : widgetList) {
            Map<String, Object> config = JsonUtil.getMap(widget, "config");
            String code = MapUtil.getStringVal(config, "code");
            String label = MapUtil.getStringVal(config, "label");
            boolean dynamicHidden = dynamicHiddenCodes.contains(code);
            boolean hidden = MapUtil.getBooleanVal(config, "hidden", false);
            if (hidden && !dynamicHidden) {
                continue;
            }
            FormObjValue originalValue = valueMap.get(code);
            String rawValue = originalValue == null ? null : originalValue.getValue();
            boolean required = MapUtil.getBooleanVal(config, "required", false);
            if (StringUtil.isBlank(rawValue)) {
                boolean dynamicRequired = dynamicRequiredCodes.contains(code);
                if (required && !dynamicHidden && !dynamicRequired) {
                    setInvalid(result, label + "不能为空");
                    return result;
                }
                continue;
            }
            String type = MapUtil.getStringVal(widget, "type");
            ResultData<FormObjValue> valueResult = normalizeValue(type, config, originalValue, rawValue, label);
            if (valueResult.getCode() != ResultData.OK) {
                int valueCode = valueResult.getCode();
                String valueMessage = valueResult.getMessage();
                result.setCode(valueCode);
                result.setMessage(valueMessage);
                return result;
            }
            FormObjValue normalizedValue = valueResult.getData();
            normalizedValues.add(normalizedValue);
        }
        result.setData(normalizedValues);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 解析并完整校验Schema
     * @param formJson 表单Schema
     * @return Schema对象
     */
    private static ResultData<Map<String, Object>> parseSchema(String formJson) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        if (StringUtil.isBlank(formJson) || !JsonUtil.isValidJson(formJson)) {
            setInvalid(result, "表单Schema格式不正确");
            return result;
        }
        Map<String, Object> schema = JsonUtil.toMap(formJson);
        if (schema == null) {
            setInvalid(result, "表单Schema不能为空");
            return result;
        }
        Object formConfig = schema.get("formConfig");
        if (!(formConfig instanceof Map<?, ?>)) {
            setInvalid(result, "表单配置不能为空");
            return result;
        }
        Object widgetObject = schema.get("widgetList");
        if (!(widgetObject instanceof List<?>)) {
            setInvalid(result, "表单组件列表不能为空");
            return result;
        }
        Set<String> codes = new HashSet<>();
        List<?> widgets = (List<?>) widgetObject;
        for (Object item : widgets) {
            if (!(item instanceof Map<?, ?>)) {
                setInvalid(result, "表单组件配置不正确");
                return result;
            }
            Map<String, Object> widget = asMap(item);
            boolean valid = validateWidget(widget, codes, result);
            if (!valid) {
                return result;
            }
        }
        List<Map<String, Object>> widgetList = getWidgetList(schema);
        for (Map<String, Object> widget : widgetList) {
            Map<String, Object> config = JsonUtil.getMap(widget, "config");
            boolean valid = validateEvents(config, codes, result);
            if (!valid) {
                return result;
            }
        }
        result.setData(schema);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验单个组件及字段编码
     * @param widget 组件配置
     * @param codes 已存在编码
     * @param result 校验结果
     * @return true校验通过
     */
    private static boolean validateWidget(Map<String, Object> widget, Set<String> codes, ResultData<?> result) {
        String type = MapUtil.getStringVal(widget, "type");
        FormFieldTypeEnum fieldType = FormFieldTypeEnum.findByValue(type);
        if (fieldType == null) {
            setInvalid(result, "表单组件类型" + type + "不受支持");
            return false;
        }
        Map<String, Object> config = JsonUtil.getMap(widget, "config");
        if (config == null) {
            setInvalid(result, "表单组件缺少配置");
            return false;
        }
        String code = MapUtil.getStringVal(config, "code");
        if (StringUtil.isBlank(code) || code.length() > CODE_MAX_LENGTH) {
            setInvalid(result, "表单字段编码为空或超过36个字符");
            return false;
        }
        if (!codes.add(code)) {
            setInvalid(result, "表单字段编码" + code + "重复");
            return false;
        }
        String label = MapUtil.getStringVal(config, "label");
        if (StringUtil.isBlank(label) || label.length() > LABEL_MAX_LENGTH) {
            setInvalid(result, "表单字段" + code + "的标题为空或超过36个字符");
            return false;
        }
        boolean validSwitches = validateSwitches(config, code, result);
        if (!validSwitches) {
            return false;
        }
        if ("radio".equals(type) || "select".equals(type)) {
            return validateOptions(config, code, result);
        }
        if ("number".equals(type)) {
            return validateNumberConfig(config, code, result);
        }
        if ("date".equals(type)) {
            String dateType = MapUtil.getStringVal(config, "dateType");
            if (!"date".equals(dateType) && !"datetime".equals(dateType)) {
                setInvalid(result, "表单字段" + code + "的日期类型不正确");
                return false;
            }
        }
        return true;
    }

    /**
     * 校验组件开关配置
     * @param config 组件配置
     * @param code 字段编码
     * @param result 校验结果
     * @return true校验通过
     */
    private static boolean validateSwitches(Map<String, Object> config, String code, ResultData<?> result) {
        Set<String> switchNames = Set.of("hidden", "required", "disabled", "readonly", "multiple", "downloadable");
        for (String switchName : switchNames) {
            Object value = config.get(switchName);
            if (value != null && !(value instanceof Boolean)) {
                setInvalid(result, "表单字段" + code + "的" + switchName + "配置必须是布尔值");
                return false;
            }
        }
        return true;
    }

    /**
     * 校验选项配置
     * @param config 组件配置
     * @param code 字段编码
     * @param result 校验结果
     * @return true校验通过
     */
    private static boolean validateOptions(Map<String, Object> config, String code, ResultData<?> result) {
        Object optionObject = config.get("options");
        if (!(optionObject instanceof List<?>) || ((List<?>) optionObject).isEmpty()) {
            setInvalid(result, "表单字段" + code + "的选项不能为空");
            return false;
        }
        Set<String> optionValues = new HashSet<>();
        List<?> options = (List<?>) optionObject;
        for (Object item : options) {
            if (!(item instanceof Map<?, ?>)) {
                setInvalid(result, "表单字段" + code + "的选项配置不正确");
                return false;
            }
            Map<String, Object> option = asMap(item);
            Object rawValue = option.get("value");
            Object rawLabel = option.get("label");
            if (rawValue == null || rawLabel == null) {
                setInvalid(result, "表单字段" + code + "的选项值和标题不能为空");
                return false;
            }
            if ((!(rawValue instanceof String) && !(rawValue instanceof Number) && !(rawValue instanceof Boolean))
                    || (!(rawLabel instanceof String) && !(rawLabel instanceof Number) && !(rawLabel instanceof Boolean))) {
                setInvalid(result, "表单字段" + code + "的选项值或标题类型不正确");
                return false;
            }
            String optionLabel = String.valueOf(rawLabel);
            String optionValue = String.valueOf(rawValue);
            if (StringUtil.isBlank(optionLabel) || StringUtil.isBlank(optionValue)) {
                setInvalid(result, "表单字段" + code + "的选项值和标题不能为空");
                return false;
            }
            if (optionValue.length() > OPTION_VALUE_MAX_LENGTH || optionLabel.length() > OPTION_LABEL_MAX_LENGTH) {
                setInvalid(result, "表单字段" + code + "的选项值或标题超长");
                return false;
            }
            if (!optionValues.add(optionValue)) {
                setInvalid(result, "表单字段" + code + "的选项值重复");
                return false;
            }
        }
        return true;
    }

    /**
     * 校验数字边界配置
     * @param config 组件配置
     * @param code 字段编码
     * @param result 校验结果
     * @return true校验通过
     */
    private static boolean validateNumberConfig(Map<String, Object> config, String code, ResultData<?> result) {
        Object minObject = config.get("min");
        Object maxObject = config.get("max");
        Object stepObject = config.get("step");
        BigDecimal min = parseDecimal(minObject);
        BigDecimal max = parseDecimal(maxObject);
        BigDecimal step = parseDecimal(stepObject);
        if ((minObject != null && min == null) || (maxObject != null && max == null) || (stepObject != null && step == null)) {
            setInvalid(result, "表单字段" + code + "的数字配置不正确");
            return false;
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            setInvalid(result, "表单字段" + code + "的最小值不能大于最大值");
            return false;
        }
        if (step != null && step.compareTo(BigDecimal.ZERO) <= 0) {
            setInvalid(result, "表单字段" + code + "的步长必须大于0");
            return false;
        }
        return true;
    }

    /**
     * 校验事件及字段引用
     * @param config 组件配置
     * @param codes 全部字段编码
     * @param result 校验结果
     * @return true校验通过
     */
    private static boolean validateEvents(Map<String, Object> config, Set<String> codes, ResultData<?> result) {
        Object eventObject = config.get("events");
        if (eventObject == null) {
            return true;
        }
        if (!(eventObject instanceof Map<?, ?>)) {
            setInvalid(result, "表单事件配置不正确");
            return false;
        }
        Map<String, Object> events = asMap(eventObject);
        for (Map.Entry<String, Object> entry : events.entrySet()) {
            String eventName = entry.getKey();
            if (!EVENT_NAMES.contains(eventName)) {
                setInvalid(result, "表单事件" + eventName + "不受支持");
                return false;
            }
            Object ruleObject = entry.getValue();
            if (!(ruleObject instanceof List<?>)) {
                setInvalid(result, "表单事件" + eventName + "的规则配置不正确");
                return false;
            }
            List<?> rules = (List<?>) ruleObject;
            for (Object item : rules) {
                if (!(item instanceof Map<?, ?>)) {
                    setInvalid(result, "表单事件规则配置不正确");
                    return false;
                }
                Map<String, Object> rule = asMap(item);
                boolean valid = validateEventRule(rule, codes, result);
                if (!valid) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 校验单条事件规则
     * @param rule 事件规则
     * @param codes 全部字段编码
     * @param result 校验结果
     * @return true校验通过
     */
    private static boolean validateEventRule(Map<String, Object> rule, Set<String> codes, ResultData<?> result) {
        String target = MapUtil.getStringVal(rule, "target");
        String action = MapUtil.getStringVal(rule, "action");
        if (StringUtil.isBlank(target) || !codes.contains(target)) {
            setInvalid(result, "表单事件目标字段" + target + "不存在");
            return false;
        }
        if (!EVENT_ACTIONS.contains(action)) {
            setInvalid(result, "表单事件动作" + action + "不受支持");
            return false;
        }
        Object value = rule.get("value");
        if (!"clear".equals(action) && value == null) {
            setInvalid(result, "表单事件动作值不能为空");
            return false;
        }
        Object whenObject = rule.get("when");
        if (whenObject != null && !(whenObject instanceof Map<?, ?>)) {
            setInvalid(result, "表单事件条件配置不正确");
            return false;
        }
        Map<String, Object> when = JsonUtil.getMap(rule, "when");
        if (when != null) {
            String field = MapUtil.getStringVal(when, "field");
            String operator = MapUtil.getStringVal(when, "op");
            if (StringUtil.isBlank(field) || !codes.contains(field)) {
                setInvalid(result, "表单事件条件字段" + field + "不存在");
                return false;
            }
            if (!CONDITION_OPERATORS.contains(operator)) {
                setInvalid(result, "表单事件条件" + operator + "不受支持");
                return false;
            }
        }
        if (value instanceof String) {
            String text = (String) value;
            Matcher matcher = FIELD_PLACEHOLDER.matcher(text);
            while (matcher.find()) {
                String code = matcher.group(1).trim();
                if (!codes.contains(code)) {
                    setInvalid(result, "表单事件引用字段" + code + "不存在");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 查找受指定事件动作影响的字段
     * 动态显隐和必填的最终状态只存在于运行时，服务端不按模板初始值误拦截
     * @param widgetList 组件列表
     * @param action 事件动作
     * @return 动态字段编码
     */
    private static Set<String> findEventTargetCodes(List<Map<String, Object>> widgetList, String action) {
        Set<String> codes = new HashSet<>();
        for (Map<String, Object> widget : widgetList) {
            Map<String, Object> config = JsonUtil.getMap(widget, "config");
            Map<String, Object> events = JsonUtil.getMap(config, "events");
            if (events == null) {
                continue;
            }
            for (String eventName : events.keySet()) {
                List<Map<String, Object>> rules = JsonUtil.getMapList(events, eventName);
                for (Map<String, Object> rule : rules) {
                    String ruleAction = MapUtil.getStringVal(rule, "action");
                    if (action.equals(ruleAction)) {
                        String target = MapUtil.getStringVal(rule, "target");
                        codes.add(target);
                    }
                }
            }
        }
        return codes;
    }

    /**
     * 按字段类型规范化单个值
     * @param type 字段类型
     * @param config 字段配置
     * @param originalValue 原始值
     * @param rawValue 原始业务值
     * @param label 字段标题
     * @return 规范化字段值
     */
    private static ResultData<FormObjValue> normalizeValue(String type, Map<String, Object> config, FormObjValue originalValue, String rawValue, String label) {
        ResultData<FormObjValue> result = new ResultData<>();
        String value = rawValue;
        String showValue = rawValue;
        if ("number".equals(type)) {
            BigDecimal number = parseDecimal(rawValue);
            if (number == null) {
                setInvalid(result, label + "必须是有效数字");
                return result;
            }
            Object minObject = config.get("min");
            Object maxObject = config.get("max");
            BigDecimal min = parseDecimal(minObject);
            BigDecimal max = parseDecimal(maxObject);
            if ((min != null && number.compareTo(min) < 0) || (max != null && number.compareTo(max) > 0)) {
                setInvalid(result, label + "超出允许范围");
                return result;
            }
            value = number.toPlainString();
            showValue = value;
        } else if ("date".equals(type)) {
            boolean valid = isValidDate(config, rawValue);
            if (!valid) {
                setInvalid(result, label + "的日期格式不正确");
                return result;
            }
        } else if ("radio".equals(type) || "select".equals(type)) {
            Map<String, Object> option = findOption(config, rawValue);
            if (option == null) {
                setInvalid(result, label + "的选项值不存在");
                return result;
            }
            Object optionValue = option.get("value");
            Object optionLabel = option.get("label");
            value = String.valueOf(optionValue);
            showValue = String.valueOf(optionLabel);
        } else if (REFERENCE_TYPES.contains(type)) {
            ResultData<String> referenceResult = normalizeReferenceValue(config, rawValue, label);
            if (referenceResult.getCode() != ResultData.OK) {
                int referenceCode = referenceResult.getCode();
                String referenceMessage = referenceResult.getMessage();
                result.setCode(referenceCode);
                result.setMessage(referenceMessage);
                return result;
            }
            value = referenceResult.getData();
            String originalShowValue = originalValue.getShowValue();
            showValue = StringUtil.isBlank(originalShowValue) ? value : originalShowValue;
        }
        if (value.length() > VALUE_MAX_LENGTH) {
            setInvalid(result, label + "的值内容过长");
            return result;
        }
        if (showValue.length() > SHOW_VALUE_MAX_LENGTH) {
            setInvalid(result, label + "的显示值内容过长");
            return result;
        }
        String code = MapUtil.getStringVal(config, "code");
        FormObjValue normalizedValue = new FormObjValue();
        normalizedValue.setCode(code);
        normalizedValue.setType(type);
        normalizedValue.setValue(value);
        normalizedValue.setShowValue(showValue);
        result.setData(normalizedValue);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 规范化引用类型ID串
     * @param config 字段配置
     * @param rawValue 原始ID串
     * @param label 字段标题
     * @return 规范化ID串
     */
    private static ResultData<String> normalizeReferenceValue(Map<String, Object> config, String rawValue, String label) {
        ResultData<String> result = new ResultData<>();
        String[] parts = rawValue.split(",");
        Set<String> ids = new LinkedHashSet<>();
        for (String part : parts) {
            String idText = part.trim();
            if (StringUtil.isBlank(idText)) {
                continue;
            }
            try {
                long id = Long.parseLong(idText);
                if (id <= 0) {
                    setInvalid(result, label + "包含无效ID");
                    return result;
                }
                String idValue = Long.toString(id);
                ids.add(idValue);
            } catch (NumberFormatException e) {
                setInvalid(result, label + "包含无效ID");
                return result;
            }
        }
        if (ids.isEmpty()) {
            setInvalid(result, label + "不能为空");
            return result;
        }
        boolean multiple = MapUtil.getBooleanVal(config, "multiple", false);
        if (!multiple && ids.size() > 1) {
            setInvalid(result, label + "不允许多选");
            return result;
        }
        String value = String.join(",", ids);
        result.setData(value);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验日期值
     * @param config 字段配置
     * @param value 日期值
     * @return true格式正确
     */
    private static boolean isValidDate(Map<String, Object> config, String value) {
        String dateType = MapUtil.getStringVal(config, "dateType");
        try {
            if ("datetime".equals(dateType)) {
                LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            } else {
                LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 查找匹配选项
     * @param config 字段配置
     * @param rawValue 原始值
     * @return 匹配选项
     */
    private static Map<String, Object> findOption(Map<String, Object> config, String rawValue) {
        Object optionObject = config.get("options");
        List<?> options = (List<?>) optionObject;
        for (Object item : options) {
            Map<String, Object> option = asMap(item);
            Object optionValue = option.get("value");
            String optionText = String.valueOf(optionValue);
            if (optionText.equals(rawValue)) {
                return option;
            }
        }
        return null;
    }

    /**
     * 解析精确数字
     * @param value 原始数字
     * @return 数字值
     */
    private static BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (StringUtil.isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取已校验的组件列表
     * @param schema Schema对象
     * @return 组件列表
     */
    private static List<Map<String, Object>> getWidgetList(Map<String, Object> schema) {
        return JsonUtil.getMapList(schema, "widgetList");
    }

    /**
     * 转换动态Map
     * @param value 原始对象
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return (Map<String, Object>) value;
    }

    /**
     * 设置参数错误
     * @param result 返回结果
     * @param message 错误信息
     * @param <T> 数据类型
     */
    private static <T> void setInvalid(ResultData<T> result, String message) {
        result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
        result.setMessage(message);
    }
}
