package com.spark.test.llm;

import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.dao.llm.ProviderDao;
import com.spark.llm.model.credential.ProviderKeyRoute;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.health.ModelHealthProber;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 18:30:00
 * 模型健康探活器测试
 */
public class ModelHealthProberTest {
    private ModelHealthProber prober;
    private ProviderDao providerDao;
    private ProviderKeyRouter providerKeyRouter;

    @BeforeEach
    void setUp() {
        prober = spy(new ModelHealthProber());
        providerDao = mock(ProviderDao.class);
        providerKeyRouter = mock(ProviderKeyRouter.class);
        ReflectionTestUtils.setField(prober, "providerDao", providerDao);
        ReflectionTestUtils.setField(prober, "providerKeyRouter", providerKeyRouter);
        ProviderResult providerResult = new ProviderResult();
        providerResult.setId(10L);
        providerResult.setApiUrl("http://127.0.0.1:9000");
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(providerResult);
        ProviderKeyRoute route = new ProviderKeyRoute(1L, 10L, "encrypted", "sk-test");
        when(providerKeyRouter.select(10L)).thenReturn(route);
    }

    /**
     * 验证语言模型探活构建原始聊天客户端并发送ping，调用无异常时判定健康
     */
    @Test
    void shouldProbeLanguageModelSuccess() {
        ModelResult model = this.createModel(1L, ModelTypeEnum.LANGUAGE.getType());
        ChatModel chatModel = mock(ChatModel.class);
        doReturn(chatModel).when(prober).createProbeChatModel(any(), any(), any(), any());
        boolean healthy = prober.probe(model);
        assertEquals(true, healthy);
        verify(chatModel).chat("ping");
    }

    /**
     * 验证向量模型探活构建原始向量客户端并发送ping，调用无异常时判定健康
     */
    @Test
    void shouldProbeVectorModelSuccess() {
        ModelResult model = this.createModel(1L, ModelTypeEnum.VECTOR.getType());
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        doReturn(embeddingModel).when(prober).createProbeEmbeddingModel(any(), any(), any(), any());
        boolean healthy = prober.probe(model);
        assertEquals(true, healthy);
        verify(embeddingModel).embed("ping");
    }

    /**
     * 验证排序模型探活构建原始排序客户端并发送ping打分，调用无异常时判定健康
     */
    @Test
    void shouldProbeRerankModelSuccess() {
        ModelResult model = this.createModel(1L, ModelTypeEnum.RERANK.getType());
        ScoringModel scoringModel = mock(ScoringModel.class);
        doReturn(scoringModel).when(prober).createProbeScoringModel(any(), any(), any(), any());
        boolean healthy = prober.probe(model);
        assertEquals(true, healthy);
        verify(scoringModel).scoreAll(anyList(), eq("ping"));
    }

    /**
     * 验证ping调用抛出连接异常时探活判定异常，异常不向外传播
     */
    @Test
    void shouldReturnFalseWhenPingThrows() {
        ModelResult model = this.createModel(1L, ModelTypeEnum.LANGUAGE.getType());
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.chat("ping")).thenThrow(new RuntimeException("connect timeout"));
        doReturn(chatModel).when(prober).createProbeChatModel(any(), any(), any(), any());
        boolean healthy = prober.probe(model);
        assertEquals(false, healthy);
    }

    /**
     * 验证厂商无可用凭据时路由抛出限流异常，探活判定异常且不构建客户端
     */
    @Test
    void shouldReturnFalseWhenNoAvailableKey() {
        ModelResult model = this.createModel(1L, ModelTypeEnum.LANGUAGE.getType());
        ModelErrorTypeEnum errorType = ModelErrorTypeEnum.RATE_LIMIT;
        String userMessage = errorType.getUserMessage();
        ModelInvocationException noKeyError = new ModelInvocationException(errorType, 429, "no available providerKey", null, userMessage);
        when(providerKeyRouter.select(10L)).thenThrow(noKeyError);
        boolean healthy = prober.probe(model);
        assertEquals(false, healthy);
        verify(prober, never()).createProbeChatModel(any(), any(), any(), any());
    }

    /**
     * 验证厂商记录缺失时探活判定异常，不再选择凭据
     */
    @Test
    void shouldReturnFalseWhenProviderMissing() {
        ModelResult model = this.createModel(1L, ModelTypeEnum.LANGUAGE.getType());
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(null);
        boolean healthy = prober.probe(model);
        assertEquals(false, healthy);
        verify(providerKeyRouter, never()).select(any());
    }

    /**
     * 验证未知模型类型与空入参直接判定异常，不访问数据库
     */
    @Test
    void shouldReturnFalseForUnknownTypeAndNullModel() {
        ModelResult unknownModel = this.createModel(1L, ModelTypeEnum.UNKNOWN.getType());
        boolean unknownHealthy = prober.probe(unknownModel);
        assertEquals(false, unknownHealthy);
        boolean nullHealthy = prober.probe(null);
        assertEquals(false, nullHealthy);
        ModelResult noProviderModel = this.createModel(1L, ModelTypeEnum.LANGUAGE.getType());
        noProviderModel.setProviderId(null);
        boolean noProviderHealthy = prober.probe(noProviderModel);
        assertEquals(false, noProviderHealthy);
        verify(providerDao, never()).queryProvider(any());
    }

    /**
     * 构造探活模型测试数据
     * @param id 模型id
     * @param type 模型类型
     * @return 模型数据
     */
    private ModelResult createModel(Long id, Integer type) {
        ModelResult model = new ModelResult();
        model.setId(id);
        model.setProviderId(10L);
        model.setType(type);
        model.setName("model-" + id);
        return model;
    }
}