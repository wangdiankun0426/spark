package com.spark.llm.model.health;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.dao.llm.ProviderDao;
import com.spark.llm.model.credential.ProviderKeyRoute;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.model.custom.RerankModel;
import com.spark.llm.model.http.RetryAfterHttpClientBuilder;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 18:00:00
 * 模型健康探活器，直连原始客户端单次轻量调用，不经过重试降级熔断限流外观
 */
@Component
public class ModelHealthProber {
    private static final Logger logger = LoggerFactory.getLogger(ModelHealthProber.class);
    private static final long PROBE_TIMEOUT_MS = 10000L;
    private static final int PROBE_EMBEDDING_DIMENSION = 1024;
    private static final int PROBE_MAX_OUTPUT_TOKENS = 16;
    private static final String PROBE_TEXT = "ping";
    @Autowired
    private ProviderDao providerDao;
    @Autowired
    private ProviderKeyRouter providerKeyRouter;

    /**
     * 对单个模型执行轻量探活
     * @param model 模型数据
     * @return 是否健康
     */
    public boolean probe(ModelResult model) {
        if (model == null || model.getId() == null || model.getProviderId() == null) {
            return false;
        }
        String modelName = model.getName();
        if (modelName == null || modelName.isEmpty()) {
            return false;
        }
        ModelTypeEnum typeEnum = ModelTypeEnum.indexOf(model.getType());
        if (typeEnum != ModelTypeEnum.LANGUAGE && typeEnum != ModelTypeEnum.VECTOR && typeEnum != ModelTypeEnum.RERANK) {
            return false;
        }
        try {
            Long providerId = model.getProviderId();
            ProviderQuery providerQuery = new ProviderQuery();
            providerQuery.setId(providerId);
            ProviderResult providerResult = providerDao.queryProvider(providerQuery);
            if (providerResult == null || providerResult.getApiUrl() == null) {
                logger.warn("model probe provider missing, modelId={}, providerId={}", model.getId(), providerId);
                return false;
            }
            ProviderKeyRoute route = providerKeyRouter.select(providerId);
            String secretKey = route.getSecretKey();
            String apiUrl = providerResult.getApiUrl();
            Duration timeout = Duration.ofMillis(PROBE_TIMEOUT_MS);
            if (typeEnum == ModelTypeEnum.LANGUAGE) {
                ChatModel chatModel = this.createProbeChatModel(model, apiUrl, secretKey, timeout);
                chatModel.chat(PROBE_TEXT);
                return true;
            }
            if (typeEnum == ModelTypeEnum.VECTOR) {
                EmbeddingModel embeddingModel = this.createProbeEmbeddingModel(model, apiUrl, secretKey, timeout);
                embeddingModel.embed(PROBE_TEXT);
                return true;
            }
            ScoringModel scoringModel = this.createProbeScoringModel(model, apiUrl, secretKey, timeout);
            TextSegment segment = TextSegment.from(PROBE_TEXT);
            List<TextSegment> segments = List.of(segment);
            scoringModel.scoreAll(segments, PROBE_TEXT);
            return true;
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            logger.warn("model probe failed, modelId={}, errMsg={}", model.getId(), errorMessage);
            return false;
        }
    }

    /**
     * 构建探活用同步聊天客户端，关闭思考模式避免非流式报错
     * @param model 模型数据
     * @param apiUrl 供应商接口地址
     * @param secretKey 解密后的凭据
     * @param timeout 探活超时
     * @return 聊天客户端
     */
    public ChatModel createProbeChatModel(ModelResult model, String apiUrl, String secretKey, Duration timeout) {
        String modelName = model.getName();
        Map<String, Object> customParameters = Map.of("enable_thinking", false);
        OpenAiChatRequestParameters parameters = OpenAiChatRequestParameters.builder()
                .modelName(modelName)
                .temperature(0.0)
                .maxOutputTokens(PROBE_MAX_OUTPUT_TOKENS)
                .customParameters(customParameters)
                .build();
        HttpClientBuilder httpClientBuilder = RetryAfterHttpClientBuilder.createHttpClientBuilder();
        return OpenAiChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(apiUrl)
                .apiKey(secretKey)
                .defaultRequestParameters(parameters)
                .timeout(timeout)
                .maxRetries(0)
                .build();
    }

    /**
     * 构建探活用向量客户端，维度与编码格式同生产参数
     * @param model 模型数据
     * @param apiUrl 供应商接口地址
     * @param secretKey 解密后的凭据
     * @param timeout 探活超时
     * @return 向量客户端
     */
    public EmbeddingModel createProbeEmbeddingModel(ModelResult model, String apiUrl, String secretKey, Duration timeout) {
        String modelName = model.getName();
        HttpClientBuilder httpClientBuilder = RetryAfterHttpClientBuilder.createHttpClientBuilder();
        return OpenAiEmbeddingModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(apiUrl)
                .apiKey(secretKey)
                .modelName(modelName)
                .dimensions(PROBE_EMBEDDING_DIMENSION)
                .encodingFormat("float")
                .timeout(timeout)
                .maxRetries(0)
                .build();
    }

    /**
     * 构建探活用排序客户端
     * @param model 模型数据
     * @param apiUrl 供应商接口地址
     * @param secretKey 解密后的凭据
     * @param timeout 探活超时
     * @return 排序客户端
     */
    public ScoringModel createProbeScoringModel(ModelResult model, String apiUrl, String secretKey, Duration timeout) {
        Long modelId = model.getId();
        String modelName = model.getName();
        Long providerId = model.getProviderId();
        Integer type = model.getType();
        ModelInfo modelInfo = new ModelInfo(modelId, modelName, providerId, type);
        return new RerankModel(apiUrl, secretKey, modelInfo, timeout);
    }
}