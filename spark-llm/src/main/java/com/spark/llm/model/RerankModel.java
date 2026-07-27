package com.spark.llm.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
    private final String modelName;
    private final HttpClient httpClient;

    public RerankModel(String apiUrl, String secretKey, String modelName) {
        this.apiUrl = apiUrl;
        this.secretKey = secretKey;
        this.modelName = modelName;
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
            body.put("model", modelName);
            body.put("query", query);
            body.put("documents", documents);
            body.put("top_n", topN);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/rerank"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + secretKey)
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body), StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.error("rerank http fail, status={}, body={}", response.statusCode(), response.body());
                return null;
            }
            JSONObject resp = JSON.parseObject(response.body());
            JSONArray results = resp.getJSONArray("results");
            if (results == null || results.isEmpty()) {
                return null;
            }
            List<RerankResult> list = new ArrayList<>();
            for (Object obj : results) {
                JSONObject item = (JSONObject) obj;
                Integer index = item.getInteger("index");
                Double score = item.getDouble("relevance_score");
                if (index == null || score == null) {
                    continue;
                }
                list.add(new RerankResult(index, score));
            }
            return list;
        } catch (Exception e) {
            logger.error("rerank error, query={}, docsSize={}", query, documents.size(), e);
            return null;
        }
    }

    /**
     * 重排结果
     */
    public static class RerankResult {
        private final int index;
        private final double score;

        public RerankResult(int index, double score) {
            this.index = index;
            this.score = score;
        }

        public int getIndex() {
            return index;
        }

        public double getScore() {
            return score;
        }
    }
}
