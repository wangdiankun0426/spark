package com.spark.llm.model;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.ModelUsageSceneHolder;
import com.spark.llm.usage.ModelUsageRecorder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 10:00:00
 * 排序模型（Cohere/Jina/SiliconFlow 兼容协议，POST /v1/rerank）
 */
public class RerankModel {
    private final static Logger logger = LoggerFactory.getLogger(RerankModel.class);
    private final String apiUrl;
    private final String secretKey;
    private final ModelInfo modelInfo;
    private final ModelUsageRecorder recorder;
    private final HttpClient httpClient;

    public RerankModel(String apiUrl, String secretKey, ModelInfo modelInfo, ModelUsageRecorder recorder) {
        this.apiUrl = apiUrl;
        this.secretKey = secretKey;
        this.modelInfo = modelInfo;
        this.recorder = recorder;
        this.httpClient = HttpClient.newBuilder().build();
    }

    /**
     * 重排序
     * @param query 查询文本
     * @param documents 文档列表
     * @param topN 返回前N条
     * @return 重排结果列表（按相关性降序），异常返回 null
     */
    public List<RerankResult> rerank(String query, List<String> documents, int topN) {
        if (query == null || documents == null || documents.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", modelInfo.getModelName());
            body.put("query", query);
            body.put("documents", documents);
            body.put("top_n", topN);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/rerank"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + secretKey)
                    .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toString(body), StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.error("rerank http fail, status={}, body={}", response.statusCode(), response.body());
                return null;
            }
            Map<String, Object> resp = JsonUtil.toMap(response.body());
            List<Map<String, Object>> results = JsonUtil.getMapList(resp, "results");
            if (results == null || results.isEmpty()) {
                return null;
            }
            List<RerankResult> list = new ArrayList<>();
            for (Map<String, Object> item : results) {
                Integer index = MapUtil.getIntegerVal(item, "index", null);
                Double score = MapUtil.getDoubleVal(item, "relevance_score", null);
                if (index == null || score == null) {
                    continue;
                }
                list.add(new RerankResult(index, score));
            }
            this.reportUsage(resp);
            return list;
        } catch (Exception e) {
            logger.error("rerank error, query={}, docsSize={}", query, documents.size(), e);
            return null;
        }
    }

    /**
     * 上报重排调用用量，兼容SiliconFlow/Jina的usage结构与Cohere的meta结构
     * @param resp 重排响应
     */
    private void reportUsage(Map<String, Object> resp) {
        try {
            Integer inputTokens = parseInputTokens(resp);
            Integer outputTokens = inputTokens == null ? null : 0;
            recorder.record(modelInfo,
                    SessionHolder.getCurrentTenantId(),
                    SessionHolder.getCurrentUserId(),
                    SessionHolder.getCurrentDeptId(),
                    ModelUsageSceneHolder.current().getCode(),
                    inputTokens, outputTokens, null);
        } catch (Exception e) {
            logger.warn("rerank usage report error, modelId={}", modelInfo.getModelId(), e);
        }
    }

    /**
     * 兼容解析各厂商重排响应中的输入token数
     * @param resp 重排响应
     * @return 输入token数，厂商未返回时为null
     */
    private Integer parseInputTokens(Map<String, Object> resp) {
        Object usageObj = resp.get("usage");
        if (usageObj instanceof Map) {
            Map<?, ?> usage = (Map<?, ?>) usageObj;
            Integer total = MapUtil.getIntegerVal(usage, "total_tokens", null);
            if (total != null) {
                return total;
            }
            Integer prompt = MapUtil.getIntegerVal(usage, "prompt_tokens", null);
            if (prompt != null) {
                return prompt;
            }
        }
        Object metaObj = resp.get("meta");
        if (metaObj instanceof Map) {
            Map<?, ?> meta = (Map<?, ?>) metaObj;
            Object billedObj = meta.get("billed_units");
            if (billedObj instanceof Map) {
                Integer search = MapUtil.getIntegerVal((Map<?, ?>) billedObj, "search_tokens", null);
                if (search != null) {
                    return search;
                }
            }
            Object tokensObj = meta.get("tokens");
            if (tokensObj instanceof Map) {
                Integer input = MapUtil.getIntegerVal((Map<?, ?>) tokensObj, "input_tokens", null);
                if (input != null) {
                    return input;
                }
            }
        }
        return null;
    }

    /**
     * 重排结果
     */
    @Data
    @AllArgsConstructor
    public static class RerankResult {
        private final int index;
        private final double score;
    }
}
