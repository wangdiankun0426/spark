package com.spark.llm.model.factory;

import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.model.credential.ProviderKeyChatModel;
import com.spark.llm.model.credential.ProviderKeyEmbeddingModel;
import com.spark.llm.model.credential.ProviderKeyModel;
import com.spark.llm.model.credential.ProviderKeyModelCacheEntry;
import com.spark.llm.model.credential.ProviderKeyModelCacheKey;
import com.spark.llm.model.credential.ProviderKeyRoute;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.model.credential.ProviderKeyScoringModel;
import com.spark.llm.model.credential.ProviderKeyStreamingChatModel;
import com.spark.llm.model.custom.RerankModel;
import com.spark.llm.model.fallback.FallbackChatModel;
import com.spark.llm.model.fallback.FallbackEmbeddingModel;
import com.spark.llm.model.fallback.FallbackModelTarget;
import com.spark.llm.model.fallback.FallbackScoringModel;
import com.spark.llm.model.fallback.FallbackStreamingChatModel;
import com.spark.llm.model.fallback.ModelFallbackChainParser;
import com.spark.llm.model.http.RetryAfterHttpClientBuilder;
import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.llm.ProviderDao;
import com.spark.common.enums.DeleteFlagEnum;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.utils.CollectionUtil;
import com.spark.llm.usage.EmbeddingUsageWrapper;
import com.spark.llm.usage.ModelUsageChatListener;
import com.spark.llm.usage.ModelUsageRecorder;
import com.spark.llm.usage.ScoringUsageWrapper;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/5/14
 * 模型工厂类 - 根据数据库中的模型配置动态创建 LangChain4j 模型实例
 */
@Component
public class ModelFactory {
    private static final Logger logger = LoggerFactory.getLogger(ModelFactory.class);
    private static final int OPENAI_EMBEDDING_DIMENSION = 1024;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private ProviderDao providerDao;
    @Autowired
    private ModelUsageRecorder modelUsageRecorder;
    @Autowired
    private ModelInvoker modelInvoker;
    @Autowired
    private ProviderKeyRouter providerKeyRouter;
    /**
     * 模型实例缓存，避免重复创建
     */
    private final Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
    private final Map<Long, ScoringModel> rerankModelCache = new ConcurrentHashMap<>();
    private final Map<ProviderKeyModelCacheKey, ProviderKeyModelCacheEntry<ChatModel>> chatKeyModelCache = new ConcurrentHashMap<>();
    private final Map<ProviderKeyModelCacheKey, ProviderKeyModelCacheEntry<StreamingChatModel>> streamingKeyModelCache = new ConcurrentHashMap<>();
    private final Map<ProviderKeyModelCacheKey, ProviderKeyModelCacheEntry<EmbeddingModel>> embeddingKeyModelCache = new ConcurrentHashMap<>();
    private final Map<ProviderKeyModelCacheKey, ProviderKeyModelCacheEntry<ScoringModel>> rerankKeyModelCache = new ConcurrentHashMap<>();
    
    /**
     * 获取默认的聊天模型
     * @return ChatModel 实例
     */
    public ChatModel getDefaultChatModel() {
        ModelQuery query = new ModelQuery();
        query.setType(ModelTypeEnum.LANGUAGE.getType());
        List<ModelResult> modelList = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(modelList)) {
            throw new IllegalStateException("No language model found in database");
        }
        Long modelId = modelList.get(0).getId();
        return getChatModel(modelId);
    }

    /**
     * 获取默认的流式聊天模型
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getDefaultStreamingChatModel() {
        ModelQuery query = new ModelQuery();
        query.setType(ModelTypeEnum.LANGUAGE.getType());
        List<ModelResult> modelList = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(modelList)) {
            throw new IllegalStateException("No language model found in database");
        }
        // 取第一个有效的模型
        Long modelId = modelList.get(0).getId();
        return getStreamingChatModel(modelId);
    }

    /**
     * 获取默认关闭思考模式的聊天模型
     * 优先取 enableThinking=-1 的语言模型，找不到则回退到默认聊天模型
     * @return ChatModel 实例
     */
    public ChatModel getDefaultNoThinkChatModel() {
        ModelQuery query = new ModelQuery();
        query.setType(ModelTypeEnum.LANGUAGE.getType());
        query.setEnableThinking(StatusEnum.ABNORMAL.getValue());
        List<ModelResult> modelList = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(modelList)) {
            logger.warn("no thinking-enabled language model found, fallback to default chat model");
            return getDefaultChatModel();
        }
        Long modelId = modelList.get(0).getId();
        return getChatModel(modelId);
    }

    /**
     * 获取默认开启思考模式的流式聊天模型
     * 优先取 enableThinking=1 的语言模型，找不到则回退到默认流式聊天模型
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getDefaultThinkingStreamingChatModel() {
        ModelQuery query = new ModelQuery();
        query.setType(ModelTypeEnum.LANGUAGE.getType());
        query.setEnableThinking(StatusEnum.NORMAL.getValue());
        List<ModelResult> modelList = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(modelList)) {
            logger.warn("no thinking-enabled language model found, fallback to default streaming chat model");
            return getDefaultStreamingChatModel();
        }
        Long modelId = modelList.get(0).getId();
        return getStreamingChatModel(modelId);
    }
    
    /**
     * 获取默认的向量模型
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel getDefaultEmbeddingModel() {
        ModelQuery query = new ModelQuery();
        query.setType(ModelTypeEnum.VECTOR.getType());
        List<ModelResult> modelList = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(modelList)) {
            throw new IllegalStateException("No embedding model found in database");
        }
        // 取第一个有效的模型
        Long modelId = modelList.get(0).getId();
        return getEmbeddingModel(modelId);
    }
    
    /**
     * 根据模型ID获取聊天模型
     * @param modelId 模型ID
     * @return ChatModel 实例
     */
    public ChatModel getChatModel(Long modelId) {
        if (modelId == null) {
            return getDefaultChatModel();
        }
        return chatModelCache.computeIfAbsent(modelId, id -> {
            Model model = loadModel(id);
            return createChatModel(model);
        });
    }
    
    /**
     * 根据模型ID获取流式聊天模型
     * @param modelId 模型ID
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getStreamingChatModel(Long modelId) {
        if (modelId == null) {
            return getDefaultStreamingChatModel();
        }
        return streamingChatModelCache.computeIfAbsent(modelId, id -> {
            Model model = loadModel(id);
            return createStreamingChatModel(model);
        });
    }
    
    /**
     * 根据模型ID获取向量模型
     * @param modelId 模型ID
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel getEmbeddingModel(Long modelId) {
        if (modelId == null) {
            return getDefaultEmbeddingModel();
        }
        return embeddingModelCache.computeIfAbsent(modelId, id -> {
            Model model = loadModel(id);
            return createEmbeddingModel(model);
        });
    }

    /**
     * 根据模型ID获取排序模型
     * @param modelId 模型ID
     * @return ScoringModel 实例（带用量采集包装）
     */
    public ScoringModel getRerankModel(Long modelId) {
        if (modelId == null) {
            return null;
        }
        return rerankModelCache.computeIfAbsent(modelId, id -> {
            Model model = loadModel(id);
            return createRerankModel(model);
        });
    }
    
    /**
     * 清除模型缓存
     * @param modelId 模型ID
     */
    public void clearModelCache(Long modelId) {
        if (modelId == null) {
            return;
        }
        chatModelCache.clear();
        streamingChatModelCache.clear();
        embeddingModelCache.clear();
        rerankModelCache.clear();
        chatKeyModelCache.keySet().removeIf(key -> modelId.equals(key.getModelId()));
        streamingKeyModelCache.keySet().removeIf(key -> modelId.equals(key.getModelId()));
        embeddingKeyModelCache.keySet().removeIf(key -> modelId.equals(key.getModelId()));
        rerankKeyModelCache.keySet().removeIf(key -> modelId.equals(key.getModelId()));
    }

    /**
     * 清除指定厂商下所有模型的缓存
     * @param providerId 厂商ID
     */
    public void clearModelCacheByProvider(Long providerId) {
        if (providerId == null) {
            return;
        }
        providerKeyRouter.clearProvider(providerId);
        chatModelCache.clear();
        streamingChatModelCache.clear();
        embeddingModelCache.clear();
        rerankModelCache.clear();
        ModelQuery query = new ModelQuery();
        query.setProviderId(providerId);
        query.setPage(false);
        List<ModelResult> models = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        for (ModelResult m : models) {
            clearModelCache(m.getId());
        }
    }
    
    /**
     * 从数据库加载模型信息
     */
    private Model loadModel(Long modelId) {
        ModelQuery query = new ModelQuery();
        query.setId(modelId);
        ModelResult modelResult = modelDao.queryModel(query);
        if (modelResult == null || !DeleteFlagEnum.NORMAL.getValue().equals(modelResult.getDeleteFlag())) {
            throw new IllegalArgumentException("Model not found with id: " + modelId);
        }
        Model model = new Model();
        model.setId(modelResult.getId());
        model.setTenantId(modelResult.getTenantId());
        model.setProviderId(modelResult.getProviderId());
        model.setType(modelResult.getType());
        model.setName(modelResult.getName());
        model.setDescription(modelResult.getDescription());
        model.setRemark(modelResult.getRemark());
        model.setEnableThinking(modelResult.getEnableThinking());
        model.setTemperature(modelResult.getTemperature());
        model.setTimeoutMs(modelResult.getTimeoutMs());
        model.setMaxRetries(modelResult.getMaxRetries());
        model.setContextLength(modelResult.getContextLength());
        model.setFallbackModelIds(modelResult.getFallbackModelIds());
        model.setStatus(modelResult.getStatus());
        return model;
    }
    
    /**
     * 创建聊天模型降级外观
     */
    private ChatModel createChatModel(Model model) {
        ModelTypeEnum typeEnum = ModelTypeEnum.indexOf(model.getType());
        if (Objects.requireNonNull(typeEnum) == ModelTypeEnum.LANGUAGE) {
            List<FallbackModelTarget<ChatModel>> targets = this.createFallbackTargets(model, this::createChatTarget);
            ModelInfo modelInfo = this.buildModelInfo(model);
            ChatModelListener usageListener = new ModelUsageChatListener(modelUsageRecorder, modelInfo);
            List<ChatModelListener> listeners = List.of(usageListener);
            return new FallbackChatModel(targets, listeners);
        }
        throw new UnsupportedOperationException("Unsupported model type: " + typeEnum.getDesc());
    }

    /**
     * 创建流式聊天模型降级外观
     */
    private StreamingChatModel createStreamingChatModel(Model model) {
        ModelTypeEnum typeEnum = ModelTypeEnum.indexOf(model.getType());
        if (Objects.requireNonNull(typeEnum) == ModelTypeEnum.LANGUAGE) {
            List<FallbackModelTarget<StreamingChatModel>> targets = this.createFallbackTargets(
                    model, this::createStreamingTarget);
            ModelInfo modelInfo = this.buildModelInfo(model);
            ChatModelListener usageListener = new ModelUsageChatListener(modelUsageRecorder, modelInfo);
            List<ChatModelListener> listeners = List.of(usageListener);
            return new FallbackStreamingChatModel(targets, listeners);
        }
        throw new UnsupportedOperationException("Unsupported model type: " + typeEnum.getDesc());
    }

    /**
     * 创建向量模型降级外观
     */
    private EmbeddingModel createEmbeddingModel(Model model) {
        ModelTypeEnum typeEnum = ModelTypeEnum.indexOf(model.getType());
        if (Objects.requireNonNull(typeEnum) == ModelTypeEnum.VECTOR) {
            List<FallbackModelTarget<EmbeddingModel>> targets = this.createFallbackTargets(
                    model, this::createEmbeddingTarget);
            FallbackEmbeddingModel fallbackModel = new FallbackEmbeddingModel(targets);
            ModelInfo modelInfo = this.buildModelInfo(model);
            return new EmbeddingUsageWrapper(fallbackModel, modelUsageRecorder, modelInfo);
        }
        throw new UnsupportedOperationException("Unsupported model type: " + typeEnum.getDesc());
    }

    /**
     * 创建排序模型降级外观
     */
    private ScoringModel createRerankModel(Model model) {
        ModelTypeEnum typeEnum = ModelTypeEnum.indexOf(model.getType());
        if (Objects.requireNonNull(typeEnum) == ModelTypeEnum.RERANK) {
            List<FallbackModelTarget<ScoringModel>> targets = this.createFallbackTargets(model, this::createRerankTarget);
            FallbackScoringModel fallbackModel = new FallbackScoringModel(targets);
            ModelInfo modelInfo = this.buildModelInfo(model);
            return new ScoringUsageWrapper(fallbackModel, modelUsageRecorder, modelInfo);
        }
        throw new UnsupportedOperationException("Unsupported model type: " + typeEnum.getDesc());
    }

    /**
     * 创建单个同步聊天目标
     */
    private FallbackModelTarget<ChatModel> createChatTarget(Model model) {
        Provider provider = loadProvider(model.getProviderId());
        long timeoutMs = modelInvoker.resolveTimeout(model.getTimeoutMs());
        int maxRetries = modelInvoker.resolveMaxRetries(model.getMaxRetries());
        Duration timeout = Duration.ofMillis(timeoutMs);
        logger.info("create chatModel, modelId={}, model={}, provider={}, enableThinking={}, temperature={}, timeoutMs={}, maxRetries={}",
                model.getId(), model.getName(), provider.getName(), model.getEnableThinking(), model.getTemperature(), timeoutMs, maxRetries);
        ModelInfo modelInfo = this.buildModelInfo(model);
        OpenAiChatRequestParameters defaultParameters = this.buildOpenAiChatRequestParameters(model);
        ChatModel providerKeyModel = new ProviderKeyChatModel(
                () -> this.selectChatModel(model, provider, timeout), providerKeyRouter, defaultParameters, List.of());
        ChatModel invokingModel = modelInvoker.wrap(providerKeyModel, maxRetries);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建单个流式聊天目标
     */
    private FallbackModelTarget<StreamingChatModel> createStreamingTarget(Model model) {
        Provider provider = loadProvider(model.getProviderId());
        long timeoutMs = modelInvoker.resolveTimeout(model.getTimeoutMs());
        int maxRetries = modelInvoker.resolveMaxRetries(model.getMaxRetries());
        Duration timeout = Duration.ofMillis(timeoutMs);
        logger.info("create streamingChatModel, modelId={}, model={}, provider={}, enableThinking={}, temperature={}, timeoutMs={}, maxRetries={}",
                model.getId(), model.getName(), provider.getName(), model.getEnableThinking(), model.getTemperature(), timeoutMs, maxRetries);
        ModelInfo modelInfo = this.buildModelInfo(model);
        OpenAiChatRequestParameters defaultParameters = this.buildOpenAiChatRequestParameters(model);
        StreamingChatModel providerKeyModel = new ProviderKeyStreamingChatModel(
                () -> this.selectStreamingChatModel(model, provider, timeout), providerKeyRouter, defaultParameters, List.of());
        Long modelId = model.getId();
        StreamingChatModel invokingModel = modelInvoker.wrap(providerKeyModel, modelId, timeoutMs, maxRetries);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建单个向量目标
     */
    private FallbackModelTarget<EmbeddingModel> createEmbeddingTarget(Model model) {
        Provider provider = loadProvider(model.getProviderId());
        long timeoutMs = modelInvoker.resolveTimeout(model.getTimeoutMs());
        int maxRetries = modelInvoker.resolveMaxRetries(model.getMaxRetries());
        Duration timeout = Duration.ofMillis(timeoutMs);
        logger.info("create embeddingModel, modelId={}, model={}, provider={}, timeoutMs={}, maxRetries={}",
                model.getId(), model.getName(), provider.getName(), timeoutMs, maxRetries);
        ModelInfo modelInfo = this.buildModelInfo(model);
        EmbeddingModel providerKeyModel = new ProviderKeyEmbeddingModel(
                () -> this.selectEmbeddingModel(model, provider, timeout), providerKeyRouter, OPENAI_EMBEDDING_DIMENSION);
        EmbeddingModel invokingModel = modelInvoker.wrap(providerKeyModel, maxRetries);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建单个排序目标
     */
    private FallbackModelTarget<ScoringModel> createRerankTarget(Model model) {
        Provider provider = loadProvider(model.getProviderId());
        long timeoutMs = modelInvoker.resolveTimeout(model.getTimeoutMs());
        int maxRetries = modelInvoker.resolveMaxRetries(model.getMaxRetries());
        Duration timeout = Duration.ofMillis(timeoutMs);
        logger.info("create rerankModel, modelId={}, model={}, provider={}, timeoutMs={}, maxRetries={}",
                model.getId(), model.getName(), provider.getName(), timeoutMs, maxRetries);
        ModelInfo modelInfo = this.buildModelInfo(model);
        ScoringModel providerKeyModel = new ProviderKeyScoringModel(
                () -> this.selectRerankModel(model, provider, modelInfo, timeout), providerKeyRouter);
        ScoringModel invokingModel = modelInvoker.wrap(providerKeyModel, maxRetries);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 装配根模型和直接备用模型目标
     */
    private <T> List<FallbackModelTarget<T>> createFallbackTargets(
            Model model, Function<Model, FallbackModelTarget<T>> targetFactory) {
        List<FallbackModelTarget<T>> targets = new ArrayList<>();
        FallbackModelTarget<T> rootTarget = targetFactory.apply(model);
        targets.add(rootTarget);
        Long rootModelId = model.getId();
        String fallbackModelIds = model.getFallbackModelIds();
        List<Long> fallbackIds = ModelFallbackChainParser.parseLenient(fallbackModelIds, rootModelId);
        Long rootTenantId = model.getTenantId();
        Integer rootType = model.getType();
        for (Long fallbackId : fallbackIds) {
            try {
                Model fallbackModel = this.loadModel(fallbackId);
                Long fallbackTenantId = fallbackModel.getTenantId();
                Integer fallbackType = fallbackModel.getType();
                Integer fallbackStatus = fallbackModel.getStatus();
                if (!Objects.equals(rootTenantId, fallbackTenantId) || !Objects.equals(rootType, fallbackType)
                        || !StatusEnum.NORMAL.getValue().equals(fallbackStatus)) {
                    logger.warn("model fallback candidate unavailable, modelId={}, fallbackModelId={}", rootModelId, fallbackId);
                    continue;
                }
                FallbackModelTarget<T> fallbackTarget = targetFactory.apply(fallbackModel);
                targets.add(fallbackTarget);
            } catch (RuntimeException e) {
                String errorMessage = e.getMessage();
                logger.warn("model fallback candidate skipped, modelId={}, fallbackModelId={}, errMsg={}",
                        rootModelId, fallbackId, errorMessage);
            }
        }
        return targets;
    }

    /**
     * 构造OpenAI聊天默认请求参数
     */
    private OpenAiChatRequestParameters buildOpenAiChatRequestParameters(Model model) {
        Double temperature = model.getTemperature() != null ? model.getTemperature() : 0.1;
        String modelName = model.getName();
        if (StatusEnum.NORMAL.getValue().equals(model.getEnableThinking())) {
            return OpenAiChatRequestParameters.builder()
                    .modelName(modelName)
                    .temperature(temperature)
                    .customParameters(Map.of("enable_thinking", true))
                    .build();
        }
        return OpenAiChatRequestParameters.builder()
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    /**
     * 选择同步聊天客户端
     */
    private ProviderKeyModel<ChatModel> selectChatModel(Model model, Provider provider, Duration timeout) {
        Long providerId = provider.getId();
        ProviderKeyRoute route = providerKeyRouter.select(providerId);
        Long modelId = model.getId();
        Long providerKeyId = route.getProviderKeyId();
        ProviderKeyModelCacheKey cacheKey = new ProviderKeyModelCacheKey(modelId, providerKeyId);
        String encryptedKey = route.getEncryptedKey();
        String secretKey = route.getSecretKey();
        ProviderKeyModelCacheEntry<ChatModel> entry = chatKeyModelCache.compute(cacheKey, (key, current) -> {
            if (current != null && Objects.equals(current.getEncryptedKey(), encryptedKey)) {
                return current;
            }
            ChatModel rawModel = this.createRawOpenAiChatModel(model, provider, secretKey, timeout);
            return new ProviderKeyModelCacheEntry<>(encryptedKey, rawModel);
        });
        ChatModel delegate = entry.getDelegate();
        return new ProviderKeyModel<>(providerId, providerKeyId, delegate);
    }

    /**
     * 选择流式聊天客户端
     */
    private ProviderKeyModel<StreamingChatModel> selectStreamingChatModel(Model model, Provider provider, Duration timeout) {
        Long providerId = provider.getId();
        ProviderKeyRoute route = providerKeyRouter.select(providerId);
        Long modelId = model.getId();
        Long providerKeyId = route.getProviderKeyId();
        ProviderKeyModelCacheKey cacheKey = new ProviderKeyModelCacheKey(modelId, providerKeyId);
        String encryptedKey = route.getEncryptedKey();
        String secretKey = route.getSecretKey();
        ProviderKeyModelCacheEntry<StreamingChatModel> entry = streamingKeyModelCache.compute(cacheKey, (key, current) -> {
            if (current != null && Objects.equals(current.getEncryptedKey(), encryptedKey)) {
                return current;
            }
            StreamingChatModel rawModel = this.createRawOpenAiStreamingChatModel(model, provider, secretKey, timeout);
            return new ProviderKeyModelCacheEntry<>(encryptedKey, rawModel);
        });
        StreamingChatModel delegate = entry.getDelegate();
        return new ProviderKeyModel<>(providerId, providerKeyId, delegate);
    }

    /**
     * 选择向量客户端
     */
    private ProviderKeyModel<EmbeddingModel> selectEmbeddingModel(Model model, Provider provider, Duration timeout) {
        Long providerId = provider.getId();
        ProviderKeyRoute route = providerKeyRouter.select(providerId);
        Long modelId = model.getId();
        Long providerKeyId = route.getProviderKeyId();
        ProviderKeyModelCacheKey cacheKey = new ProviderKeyModelCacheKey(modelId, providerKeyId);
        String encryptedKey = route.getEncryptedKey();
        String secretKey = route.getSecretKey();
        ProviderKeyModelCacheEntry<EmbeddingModel> entry = embeddingKeyModelCache.compute(cacheKey, (key, current) -> {
            if (current != null && Objects.equals(current.getEncryptedKey(), encryptedKey)) {
                return current;
            }
            EmbeddingModel rawModel = this.createRawOpenAiEmbeddingModel(model, provider, secretKey, timeout);
            return new ProviderKeyModelCacheEntry<>(encryptedKey, rawModel);
        });
        EmbeddingModel delegate = entry.getDelegate();
        return new ProviderKeyModel<>(providerId, providerKeyId, delegate);
    }

    /**
     * 选择排序客户端
     */
    private ProviderKeyModel<ScoringModel> selectRerankModel(Model model, Provider provider,
                                                             ModelInfo modelInfo, Duration timeout) {
        Long providerId = provider.getId();
        ProviderKeyRoute route = providerKeyRouter.select(providerId);
        Long modelId = model.getId();
        Long providerKeyId = route.getProviderKeyId();
        ProviderKeyModelCacheKey cacheKey = new ProviderKeyModelCacheKey(modelId, providerKeyId);
        String encryptedKey = route.getEncryptedKey();
        String secretKey = route.getSecretKey();
        ProviderKeyModelCacheEntry<ScoringModel> entry = rerankKeyModelCache.compute(cacheKey, (key, current) -> {
            if (current != null && Objects.equals(current.getEncryptedKey(), encryptedKey)) {
                return current;
            }
            String apiUrl = provider.getApiUrl();
            ScoringModel rawModel = new RerankModel(apiUrl, secretKey, modelInfo, timeout);
            return new ProviderKeyModelCacheEntry<>(encryptedKey, rawModel);
        });
        ScoringModel delegate = entry.getDelegate();
        return new ProviderKeyModel<>(providerId, providerKeyId, delegate);
    }

    /**
     * 创建不带监听器的原始同步聊天客户端
     */
    private ChatModel createRawOpenAiChatModel(Model model, Provider provider, String secretKey, Duration timeout) {
        OpenAiChatRequestParameters defaultParameters = this.buildOpenAiChatRequestParameters(model);
        HttpClientBuilder httpClientBuilder = RetryAfterHttpClientBuilder.createHttpClientBuilder();
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(provider.getApiUrl())
                .apiKey(secretKey)
                .defaultRequestParameters(defaultParameters)
                .timeout(timeout)
                .maxRetries(0);
        if (StatusEnum.NORMAL.getValue().equals(model.getEnableThinking())) {
            builder.returnThinking(true);
        }
        return builder.build();
    }

    /**
     * 创建不带监听器的原始流式聊天客户端
     */
    private StreamingChatModel createRawOpenAiStreamingChatModel(Model model, Provider provider,
                                                                 String secretKey, Duration timeout) {
        OpenAiChatRequestParameters defaultParameters = this.buildOpenAiChatRequestParameters(model);
        HttpClientBuilder httpClientBuilder = RetryAfterHttpClientBuilder.createHttpClientBuilder();
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(provider.getApiUrl())
                .apiKey(secretKey)
                .defaultRequestParameters(defaultParameters)
                .timeout(timeout);
        if (StatusEnum.NORMAL.getValue().equals(model.getEnableThinking())) {
            builder.returnThinking(true);
        }
        return builder.build();
    }

    /**
     * 创建原始向量客户端
     */
    private EmbeddingModel createRawOpenAiEmbeddingModel(Model model, Provider provider,
                                                         String secretKey, Duration timeout) {
        HttpClientBuilder httpClientBuilder = RetryAfterHttpClientBuilder.createHttpClientBuilder();
        return OpenAiEmbeddingModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(provider.getApiUrl())
                .apiKey(secretKey)
                .modelName(model.getName())
                .dimensions(OPENAI_EMBEDDING_DIMENSION)
                .encodingFormat("float")
                .timeout(timeout)
                .maxRetries(0)
                .build();
    }

    /**
     * 构造用量采集用模型元数据
     * @param model 模型配置
     * @return 模型元数据
     */
    private ModelInfo buildModelInfo(Model model) {
        return new ModelInfo(model.getId(), model.getName(), model.getProviderId(), model.getType());
    }

    /**
     * 从数据库加载模型提供商信息
     */
    private Provider loadProvider(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID cannot be null");
        }
        ProviderQuery query = new ProviderQuery();
        query.setId(providerId);
        ProviderResult providerResult = providerDao.queryProvider(query);
        if (providerResult == null) {
            throw new IllegalArgumentException("Model provider not found with id: " + providerId);
        }
        Provider provider = new Provider();
        provider.setId(providerResult.getId());
        provider.setName(providerResult.getName());
        provider.setApiUrl(providerResult.getApiUrl());
        provider.setIcon(providerResult.getIcon());
        provider.setDescription(providerResult.getDescription());
        provider.setRemark(providerResult.getRemark());
        provider.setOrderNum(providerResult.getOrderNum());
        return provider;
    }
}
