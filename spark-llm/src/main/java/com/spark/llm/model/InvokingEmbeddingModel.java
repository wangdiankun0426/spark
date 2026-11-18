package com.spark.llm.model;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 10:00:00
 * 向量模型统一调用包装
 */
public class InvokingEmbeddingModel implements EmbeddingModel {
    private final EmbeddingModel delegate;
    private final ModelInvoker modelInvoker;
    private final int maxRetries;

    public InvokingEmbeddingModel(EmbeddingModel delegate, ModelInvoker modelInvoker, int maxRetries) {
        this.delegate = delegate;
        this.modelInvoker = modelInvoker;
        this.maxRetries = maxRetries;
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
        if (segments == null) {
            throw new IllegalArgumentException("segments must not be null");
        }
        return modelInvoker.invoke(() -> delegate.embedAll(segments), maxRetries);
    }

    @Override
    public int dimension() {
        return delegate.dimension();
    }
}
