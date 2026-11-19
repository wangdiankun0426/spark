package com.spark.test.llm;

import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.model.factory.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
 * 模型工厂默认模型选取策略测试
 */
public class ModelFactoryDefaultModelTest {
    private ModelFactory modelFactory;
    private ModelDao modelDao;

    @BeforeEach
    void setUp() {
        modelFactory = spy(new ModelFactory());
        modelDao = mock(ModelDao.class);
        ReflectionTestUtils.setField(modelFactory, "modelDao", modelDao);
    }

    /**
     * 验证健康模型优先于高权重异常模型，未探活过的健康状态与权重空值视同正常与默认权重100
     */
    @Test
    void shouldSelectHealthyModelWithHighestWeight() {
        ModelResult legacy = this.createModel(1L, null, null);
        ModelResult abnormalHeavy = this.createModel(2L, 2, 9999);
        ModelResult healthyLight = this.createModel(3L, 1, 200);
        List<ModelResult> modelList = List.of(abnormalHeavy, legacy, healthyLight);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(modelList);
        ChatModel chatModel = mock(ChatModel.class);
        doReturn(chatModel).when(modelFactory).getChatModel(anyLong());
        ChatModel actual = modelFactory.getDefaultChatModel();
        assertSame(chatModel, actual);
        verify(modelFactory).getChatModel(3L);
    }

    /**
     * 验证全部模型探活异常时仍按权重降序返回最优模型，保持默认调用可用不抛异常
     */
    @Test
    void shouldSelectByWeightWhenAllAbnormal() {
        ModelResult light = this.createModel(1L, 2, 100);
        ModelResult heavy = this.createModel(2L, 2, 300);
        List<ModelResult> modelList = List.of(light, heavy);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(modelList);
        ChatModel chatModel = mock(ChatModel.class);
        doReturn(chatModel).when(modelFactory).getChatModel(anyLong());
        modelFactory.getDefaultChatModel();
        verify(modelFactory).getChatModel(2L);
    }

    /**
     * 验证健康状态与权重均相同时取id较大的模型，与原id降序排序兜底一致
     */
    @Test
    void shouldSelectLargerIdWhenWeightEqual() {
        ModelResult older = this.createModel(1L, 1, 100);
        ModelResult newer = this.createModel(5L, 1, 100);
        List<ModelResult> modelList = List.of(older, newer);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(modelList);
        ChatModel chatModel = mock(ChatModel.class);
        doReturn(chatModel).when(modelFactory).getChatModel(anyLong());
        modelFactory.getDefaultChatModel();
        verify(modelFactory).getChatModel(5L);
    }

    /**
     * 验证默认模型查询口径收口为启用且全量，停用模型与分页截断不再参与选取
     */
    @Test
    void shouldQueryEnabledFullList() {
        ModelResult model = this.createModel(1L, 1, 100);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(model));
        ChatModel chatModel = mock(ChatModel.class);
        doReturn(chatModel).when(modelFactory).getChatModel(anyLong());
        modelFactory.getDefaultChatModel();
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery query = queryCaptor.getValue();
        assertEquals(ModelTypeEnum.LANGUAGE.getType(), query.getType());
        assertEquals(StatusEnum.NORMAL.getValue(), query.getStatus());
        assertEquals(false, query.isPage());
    }

    /**
     * 验证默认流式聊天模型与同步聊天模型执行同一选取规则，健康且权重最高者胜出
     */
    @Test
    void shouldSelectDefaultStreamingBySameRule() {
        ModelResult abnormal = this.createModel(1L, 2, 9999);
        ModelResult healthy = this.createModel(2L, 1, 100);
        List<ModelResult> modelList = List.of(abnormal, healthy);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(modelList);
        StreamingChatModel streamingChatModel = mock(StreamingChatModel.class);
        doReturn(streamingChatModel).when(modelFactory).getStreamingChatModel(anyLong());
        StreamingChatModel actual = modelFactory.getDefaultStreamingChatModel();
        assertSame(streamingChatModel, actual);
        verify(modelFactory).getStreamingChatModel(2L);
    }

    /**
     * 验证关闭思考变体按enableThinking=-1过滤，无候选时回退默认聊天模型查询且回退查询不带思考过滤
     */
    @Test
    void shouldFilterNoThinkVariantAndFallbackToDefault() {
        ModelResult thinkingModel = this.createModel(9L, 1, 100);
        List<ModelResult> defaultList = List.of(thinkingModel);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of()).thenReturn(defaultList);
        ChatModel chatModel = mock(ChatModel.class);
        doReturn(chatModel).when(modelFactory).getChatModel(anyLong());
        ChatModel actual = modelFactory.getDefaultNoThinkChatModel();
        assertSame(chatModel, actual);
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao, times(2)).queryModelList(queryCaptor.capture());
        List<ModelQuery> queries = queryCaptor.getAllValues();
        ModelQuery variantQuery = queries.get(0);
        assertEquals(StatusEnum.ABNORMAL.getValue(), variantQuery.getEnableThinking());
        assertEquals(StatusEnum.NORMAL.getValue(), variantQuery.getStatus());
        ModelQuery fallbackQuery = queries.get(1);
        assertNull(fallbackQuery.getEnableThinking());
        verify(modelFactory).getChatModel(9L);
    }

    /**
     * 验证开启思考流式变体按enableThinking=1过滤并执行健康权重选取
     */
    @Test
    void shouldFilterThinkingStreamingVariant() {
        ModelResult thinkingModel = this.createModel(7L, 1, 500);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(thinkingModel));
        StreamingChatModel streamingChatModel = mock(StreamingChatModel.class);
        doReturn(streamingChatModel).when(modelFactory).getStreamingChatModel(anyLong());
        StreamingChatModel actual = modelFactory.getDefaultThinkingStreamingChatModel();
        assertSame(streamingChatModel, actual);
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery query = queryCaptor.getValue();
        assertEquals(StatusEnum.NORMAL.getValue(), query.getEnableThinking());
        assertEquals(StatusEnum.NORMAL.getValue(), query.getStatus());
        verify(modelFactory).getStreamingChatModel(7L);
    }

    /**
     * 验证默认向量模型执行同一健康权重选取规则，查询类型为向量
     */
    @Test
    void shouldSelectDefaultEmbeddingBySameRule() {
        ModelResult abnormal = this.createModel(1L, 2, 9999);
        abnormal.setType(ModelTypeEnum.VECTOR.getType());
        ModelResult healthy = this.createModel(2L, 1, 100);
        healthy.setType(ModelTypeEnum.VECTOR.getType());
        List<ModelResult> modelList = List.of(abnormal, healthy);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(modelList);
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        doReturn(embeddingModel).when(modelFactory).getEmbeddingModel(anyLong());
        EmbeddingModel actual = modelFactory.getDefaultEmbeddingModel();
        assertSame(embeddingModel, actual);
        verify(modelFactory).getEmbeddingModel(2L);
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery query = queryCaptor.getValue();
        assertEquals(ModelTypeEnum.VECTOR.getType(), query.getType());
    }

    /**
     * 验证无启用模型时聊天与向量默认获取均抛出非法状态异常，与原行为一致
     */
    @Test
    void shouldThrowWhenNoEnabledModel() {
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> modelFactory.getDefaultChatModel());
        assertThrows(IllegalStateException.class, () -> modelFactory.getDefaultEmbeddingModel());
    }

    /**
     * 构造默认选取模型测试数据
     * @param id 模型id
     * @param healthStatus 健康状态
     * @param weight 权重
     * @return 模型数据
     */
    private ModelResult createModel(Long id, Integer healthStatus, Integer weight) {
        ModelResult model = new ModelResult();
        model.setId(id);
        model.setType(ModelTypeEnum.LANGUAGE.getType());
        model.setName("model-" + id);
        model.setStatus(StatusEnum.NORMAL.getValue());
        model.setDeleteFlag(1);
        model.setHealthStatus(healthStatus);
        model.setWeight(weight);
        return model;
    }
}