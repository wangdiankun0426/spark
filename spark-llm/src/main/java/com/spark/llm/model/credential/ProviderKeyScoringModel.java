package com.spark.llm.model.credential;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;

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
 * 按物理调用选择凭据的排序模型
 */
public class ProviderKeyScoringModel implements ScoringModel {
    /**
     * 每次排序物理调用重新选择凭据和底层客户端的选择器
     */
    private final Supplier<ProviderKeyModel<ScoringModel>> modelSelector;
    /**
     * 处理当前凭据限流冷却的路由器
     */
    private final ProviderKeyRouter providerKeyRouter;

    public ProviderKeyScoringModel(Supplier<ProviderKeyModel<ScoringModel>> modelSelector,
                                   ProviderKeyRouter providerKeyRouter) {
        if (modelSelector == null || providerKeyRouter == null) {
            throw new IllegalArgumentException("providerKey scoringModel params must not be null");
        }
        this.modelSelector = modelSelector;
        this.providerKeyRouter = providerKeyRouter;
    }

    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        if (segments == null || query == null) {
            throw new IllegalArgumentException("segments and query must not be null");
        }
        ProviderKeyModel<ScoringModel> selected = modelSelector.get();
        try {
            ScoringModel delegate = selected.getDelegate();
            return delegate.scoreAll(segments, query);
        } catch (RuntimeException e) {
            Long providerId = selected.getProviderId();
            Long providerKeyId = selected.getProviderKeyId();
            providerKeyRouter.handleError(providerId, providerKeyId, e);
            throw e;
        }
    }
}
