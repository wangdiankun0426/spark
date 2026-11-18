package com.spark.llm.model.invocation;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 10:00:00
 * 排序模型统一调用包装
 */
public class InvokingScoringModel implements ScoringModel {
    private final ScoringModel delegate;
    private final ModelInvoker modelInvoker;
    private final int maxRetries;

    public InvokingScoringModel(ScoringModel delegate, ModelInvoker modelInvoker, int maxRetries) {
        this.delegate = delegate;
        this.modelInvoker = modelInvoker;
        this.maxRetries = maxRetries;
    }

    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        if (segments == null || query == null) {
            throw new IllegalArgumentException("segments and query must not be null");
        }
        return modelInvoker.invoke(() -> delegate.scoreAll(segments, query), maxRetries);
    }
}
