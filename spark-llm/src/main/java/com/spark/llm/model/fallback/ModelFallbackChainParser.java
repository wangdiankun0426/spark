package com.spark.llm.model.fallback;

import com.spark.common.utils.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 10:00:00
 * 模型降级链解析器
 */
public final class ModelFallbackChainParser {
    private static final int MAX_LENGTH = 256;
    private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile("[1-9]\\d*");
    private static final BigInteger MAX_LONG_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

    private ModelFallbackChainParser() {
    }

    /**
     * 严格解析降级模型id列表
     * @param fallbackModelIds 降级模型id列表
     * @param rootId 主模型id
     * @return 有序模型id列表
     */
    public static List<Long> parseStrict(String fallbackModelIds, Long rootId) {
        List<Long> modelIds = parseChain(fallbackModelIds, rootId);
        if (modelIds == null) {
            throw new IllegalArgumentException("降级模型id列表格式无效");
        }
        return modelIds;
    }

    /**
     * 宽松解析降级模型id列表并忽略无效项
     * @param fallbackModelIds 降级模型id列表
     * @param rootId 主模型id
     * @return 有序模型id列表
     */
    public static List<Long> parseLenient(String fallbackModelIds, Long rootId) {
        if (StringUtil.isBlank(fallbackModelIds) || fallbackModelIds.length() > MAX_LENGTH) {
            return new ArrayList<>();
        }
        String[] items = fallbackModelIds.split(",", -1);
        Set<Long> modelIdSet = new LinkedHashSet<>();
        for (String item : items) {
            String value = item.trim();
            if (!POSITIVE_INTEGER_PATTERN.matcher(value).matches()) {
                continue;
            }
            BigInteger number = new BigInteger(value);
            if (number.compareTo(MAX_LONG_VALUE) > 0) {
                continue;
            }
            Long modelId = number.longValue();
            if (modelId.equals(rootId)) {
                continue;
            }
            modelIdSet.add(modelId);
        }
        return new ArrayList<>(modelIdSet);
    }

    /**
     * 校验降级模型id列表格式
     * @param fallbackModelIds 降级模型id列表
     * @param rootId 主模型id
     * @return 是否有效
     */
    public static boolean isValidStrict(String fallbackModelIds, Long rootId) {
        List<Long> modelIds = parseChain(fallbackModelIds, rootId);
        return modelIds != null;
    }

    /**
     * 规范化有序模型id列表
     * @param modelIds 模型id列表
     * @return 逗号分隔的模型id列表
     */
    public static String normalize(List<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return null;
        }
        Set<Long> modelIdSet = new LinkedHashSet<>();
        for (Long modelId : modelIds) {
            if (modelId == null || modelId <= 0 || !modelIdSet.add(modelId)) {
                throw new IllegalArgumentException("降级模型id列表格式无效");
            }
        }
        String normalized = normalizeValidIds(modelIdSet);
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("降级模型id列表长度不能超过256个字符");
        }
        return normalized;
    }

    /**
     * 按严格规则解析降级模型id列表
     * @param fallbackModelIds 降级模型id列表
     * @param rootId 主模型id
     * @return 有序模型id列表
     */
    private static List<Long> parseChain(String fallbackModelIds, Long rootId) {
        if (StringUtil.isBlank(fallbackModelIds)) {
            return new ArrayList<>();
        }
        if (fallbackModelIds.length() > MAX_LENGTH) {
            return null;
        }
        String[] items = fallbackModelIds.split(",", -1);
        Set<Long> modelIdSet = new LinkedHashSet<>();
        for (String item : items) {
            String value = item.trim();
            if (value.isEmpty() || !POSITIVE_INTEGER_PATTERN.matcher(value).matches()) {
                return null;
            }
            BigInteger number = new BigInteger(value);
            if (number.compareTo(MAX_LONG_VALUE) > 0) {
                return null;
            }
            Long modelId = number.longValue();
            if (modelId.equals(rootId) || !modelIdSet.add(modelId)) {
                return null;
            }
        }
        String normalized = normalizeValidIds(modelIdSet);
        if (normalized.length() > MAX_LENGTH) {
            return null;
        }
        return new ArrayList<>(modelIdSet);
    }

    /**
     * 拼接已校验的模型id列表
     * @param modelIds 模型id集合
     * @return 逗号分隔的模型id列表
     */
    private static String normalizeValidIds(Set<Long> modelIds) {
        StringBuilder normalized = new StringBuilder();
        for (Long modelId : modelIds) {
            if (normalized.length() > 0) {
                normalized.append(",");
            }
            normalized.append(modelId);
        }
        return normalized.toString();
    }
}
