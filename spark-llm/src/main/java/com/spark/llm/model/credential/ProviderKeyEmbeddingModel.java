package com.spark.llm.model.credential;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.function.Supplier;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:00:00
 * 按物理调用选择凭据的向量模型
 */
public class ProviderKeyEmbeddingModel implements EmbeddingModel {
    /**
     * 每次向量物理调用重新选择凭据和底层客户端的选择器
     */
    private final Supplier<ProviderKeyModel<EmbeddingModel>> modelSelector;
    /**
     * 处理当前凭据限流冷却的路由器
     */
    private final ProviderKeyRouter providerKeyRouter;
    /**
     * 当前向量模型的固定维度
     */
    private final int dimension;

    public ProviderKeyEmbeddingModel(Supplier<ProviderKeyModel<EmbeddingModel>> modelSelector,
                                     ProviderKeyRouter providerKeyRouter, int dimension) {
        if (modelSelector == null || providerKeyRouter == null || dimension < 1) {
            throw new IllegalArgumentException("providerKey embeddingModel params must be valid");
        }
        this.modelSelector = modelSelector;
        this.providerKeyRouter = providerKeyRouter;
        this.dimension = dimension;
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
        if (segments == null) {
            throw new IllegalArgumentException("segments must not be null");
        }
        ProviderKeyModel<EmbeddingModel> selected = modelSelector.get();
        try {
            EmbeddingModel delegate = selected.getDelegate();
            return delegate.embedAll(segments);
        } catch (RuntimeException e) {
            Long providerId = selected.getProviderId();
            Long providerKeyId = selected.getProviderKeyId();
            providerKeyRouter.handleError(providerId, providerKeyId, e);
            throw e;
        }
    }

    @Override
    public int dimension() {
        return dimension;
    }
}
