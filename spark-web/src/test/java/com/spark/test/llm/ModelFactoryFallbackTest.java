package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.llm.ProviderDao;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.model.fallback.FallbackModelTarget;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.usage.ModelUsageRecorder;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 10:00:00
 * 模型工厂降级链装配与缓存测试
 */
public class ModelFactoryFallbackTest {

    /**
     * 验证工厂构建期不选Key，重复自身和跨租户候选被跳过且不递归展开备用链，备用缓存清理后主外观同步重建
     */
    @Test
    void shouldBuildDirectFallbackChainWithoutSelectingKeyAndClearDependentFacade() {
        ModelFactory modelFactory = new ModelFactory();
        ModelDao modelDao = mock(ModelDao.class);
        ProviderDao providerDao = mock(ProviderDao.class);
        ProviderKeyRouter providerKeyRouter = mock(ProviderKeyRouter.class);
        ModelInvoker modelInvoker = mock(ModelInvoker.class);
        ModelUsageRecorder modelUsageRecorder = mock(ModelUsageRecorder.class);
        ProviderCircuitBreaker providerCircuitBreaker = new ProviderCircuitBreaker();
        ReflectionTestUtils.setField(modelFactory, "modelDao", modelDao);
        ReflectionTestUtils.setField(modelFactory, "providerDao", providerDao);
        ReflectionTestUtils.setField(modelFactory, "providerKeyRouter", providerKeyRouter);
        ReflectionTestUtils.setField(modelFactory, "modelInvoker", modelInvoker);
        ReflectionTestUtils.setField(modelFactory, "modelUsageRecorder", modelUsageRecorder);
        ReflectionTestUtils.setField(modelFactory, "providerCircuitBreaker", providerCircuitBreaker);
        ModelResult root = this.createModel(1L, 0L, 10L, 1, "2,2,1,3");
        ModelResult fallback = this.createModel(2L, 0L, 20L, 1, "3");
        ModelResult invalidCandidate = this.createModel(3L, 1L, 30L, 1, null);
        Map<Long, ModelResult> models = Map.of(1L, root, 2L, fallback, 3L, invalidCandidate);
        when(modelDao.queryModel(any(ModelQuery.class))).thenAnswer(invocation -> {
            ModelQuery query = invocation.getArgument(0);
            Long modelId = query.getId();
            return models.get(modelId);
        });
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenAnswer(invocation -> {
            ProviderQuery query = invocation.getArgument(0);
            Long providerId = query.getId();
            ProviderResult provider = new ProviderResult();
            provider.setId(providerId);
            provider.setName("provider-" + providerId);
            provider.setApiUrl("http://127.0.0.1:9000");
            return provider;
        });
        when(modelInvoker.resolveTimeout(any())).thenReturn(1000L);
        when(modelInvoker.resolveMaxRetries(any())).thenReturn(0);
        when(modelInvoker.wrap(any(ChatModel.class), anyInt())).thenAnswer(invocation -> invocation.getArgument(0));
        ChatModel firstFacade = modelFactory.getChatModel(1L);
        List<?> firstTargets = this.readTargets(firstFacade);
        assertEquals(2, firstTargets.size());
        FallbackModelTarget<?> firstTarget = (FallbackModelTarget<?>) firstTargets.get(0);
        ModelInfo firstModelInfo = firstTarget.getModelInfo();
        assertEquals(1L, firstModelInfo.getModelId());
        FallbackModelTarget<?> secondTarget = (FallbackModelTarget<?>) firstTargets.get(1);
        ModelInfo secondModelInfo = secondTarget.getModelInfo();
        assertEquals(2L, secondModelInfo.getModelId());
        verify(providerKeyRouter, never()).select(any());
        modelFactory.clearModelCache(2L);
        ChatModel secondFacade = modelFactory.getChatModel(1L);
        assertNotSame(firstFacade, secondFacade);
        List<?> secondTargets = this.readTargets(secondFacade);
        assertEquals(2, secondTargets.size());
    }

    /**
     * 验证厂商下已没有有效模型时仍清空聊天外观缓存并重置该厂商熔断状态，避免已删除厂商的备用模型继续被旧降级链引用
     */
    @Test
    void shouldClearFacadeWhenProviderHasNoModels() {
        ModelFactory modelFactory = new ModelFactory();
        ModelDao modelDao = mock(ModelDao.class);
        ProviderKeyRouter providerKeyRouter = mock(ProviderKeyRouter.class);
        ProviderCircuitBreaker providerCircuitBreaker = mock(ProviderCircuitBreaker.class);
        ReflectionTestUtils.setField(modelFactory, "modelDao", modelDao);
        ReflectionTestUtils.setField(modelFactory, "providerKeyRouter", providerKeyRouter);
        ReflectionTestUtils.setField(modelFactory, "providerCircuitBreaker", providerCircuitBreaker);
        ChatModel cachedModel = mock(ChatModel.class);
        Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
        chatModelCache.put(1L, cachedModel);
        ReflectionTestUtils.setField(modelFactory, "chatModelCache", chatModelCache);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of());
        modelFactory.clearModelCacheByProvider(10L);
        assertEquals(0, chatModelCache.size());
        verify(providerKeyRouter).clearProvider(10L);
        verify(providerCircuitBreaker).reset(10L);
    }

    /**
     * 构造有效模型配置
     */
    private ModelResult createModel(Long id, Long tenantId, Long providerId, Integer type, String fallbackModelIds) {
        ModelResult model = new ModelResult();
        model.setId(id);
        model.setTenantId(tenantId);
        model.setProviderId(providerId);
        model.setType(type);
        model.setName("model-" + id);
        model.setFallbackModelIds(fallbackModelIds);
        model.setStatus(1);
        model.setDeleteFlag(1);
        return model;
    }

    /**
     * 读取降级外观目标列表
     */
    private List<?> readTargets(ChatModel model) {
        Object targets = ReflectionTestUtils.getField(model, "targets");
        return (List<?>) targets;
    }
}
