package com.spark.llm.model;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.scoring.ScoringModel;
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
 * 排序模型（Cohere/Jina/SiliconFlow 兼容协议，POST /v1/rerank），实现 langchain4j ScoringModel
 */
public class RerankModel implements ScoringModel {
    private final static Logger logger = LoggerFactory.getLogger(RerankModel.class);
    private final String apiUrl;
    private final String secretKey;
    private final ModelInfo modelInfo;
    private final HttpClient httpClient;

    public RerankModel(String apiUrl, String secretKey, ModelInfo modelInfo) {
        this.apiUrl = apiUrl;
        this.secretKey = secretKey;
        this.modelInfo = modelInfo;
        this.httpClient = HttpClient.newBuilder().build();
    }

    /**
     * 批量打分，返回分数顺序与入参文本一一对齐
     * @param segments 待打分文本
     * @param query 查询文本
     * @return 带token用量的分数列表
     */
    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        if (query == null || segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("query or segments is empty");
        }
        try {
            List<String> documents = new ArrayList<>();
            for (TextSegment segment : segments) {
                documents.add(segment.text());
            }
            Map<String, Object> body = new HashMap<>();
            body.put("model", modelInfo.getModelName());
            body.put("query", query);
            body.put("documents", documents);
            body.put("top_n", documents.size());
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
                throw new RuntimeException("rerank http fail, status=" + response.statusCode());
            }
            Map<String, Object> resp = JsonUtil.toMap(response.body());
            List<Map<String, Object>> results = JsonUtil.getMapList(resp, "results");
            if (results == null || results.isEmpty()) {
                throw new RuntimeException("rerank results empty");
            }
            // 按厂商返回的原始下标回填，未覆盖位置保留0分
            List<Double> scores = new ArrayList<>(documents.size());
            for (int i = 0; i < documents.size(); i++) {
                scores.add(0d);
            }
            for (Map<String, Object> item : results) {
                Integer index = MapUtil.getIntegerVal(item, "index", null);
                Double score = MapUtil.getDoubleVal(item, "relevance_score", null);
                if (index != null && score != null && index >= 0 && index < scores.size()) {
                    scores.set(index, score);
                }
            }
            TokenUsage tokenUsage = this.buildTokenUsage(resp);
            if (tokenUsage == null) {
                return new Response<>(scores);
            }
            return new Response<>(scores, tokenUsage, null);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("rerank error, query={}, docsSize={}", query, segments.size(), e);
            throw new RuntimeException("rerank error", e);
        }
    }

    /**
     * 从响应解析输入token用量
     * @param resp 重排响应
     * @return token用量，厂商未返回时为null
     */
    private TokenUsage buildTokenUsage(Map<String, Object> resp) {
        Integer inputTokens = this.parseInputTokens(resp);
        if (inputTokens == null) {
            return null;
        }
        return new TokenUsage(inputTokens);
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
}
