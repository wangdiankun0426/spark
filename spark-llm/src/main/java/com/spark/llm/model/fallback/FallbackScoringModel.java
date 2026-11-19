package com.spark.llm.model.fallback;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.usage.ModelRetryContext;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 09:00:00
 * 排序模型有序降级外观
 */
public class FallbackScoringModel implements ScoringModel {
    private static final Logger logger = LoggerFactory.getLogger(FallbackScoringModel.class);
    private final List<FallbackModelTarget<ScoringModel>> targets;

    public FallbackScoringModel(List<FallbackModelTarget<ScoringModel>> targets) {
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("fallback scoringModel targets must not be empty");
        }
        this.targets = List.copyOf(targets);
    }

    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        if (segments == null || query == null) {
            throw new IllegalArgumentException("segments and query must not be null");
        }
        ModelRetryContext context = ModelRetryContext.current();
        if (context == null) {
            context = new ModelRetryContext();
        }
        try {
            for (int index = 0; index < targets.size(); index++) {
                FallbackModelTarget<ScoringModel> target = targets.get(index);
                ModelInfo modelInfo = target.getModelInfo();
                Long modelId = modelInfo.getModelId();
                context.selectTarget(modelInfo, index > 0);
                if (ModelRetryContext.current() != context) {
                    ModelRetryContext.bind(context);
                }
                try {
                    ScoringModel delegate = target.getDelegate();
                    return delegate.scoreAll(segments, query);
                } catch (RuntimeException e) {
                    ModelInvocationException exception = ModelErrorClassifier.wrap(e);
                    if (index >= targets.size() - 1 || !ModelFallbackPolicy.shouldFallback(exception)) {
                        throw exception;
                    }
                    FallbackModelTarget<ScoringModel> nextTarget = targets.get(index + 1);
                    ModelInfo nextModelInfo = nextTarget.getModelInfo();
                    Long nextModelId = nextModelInfo.getModelId();
                    logger.warn("model fallback target failed, modelId={}, nextModelId={}, errorType={}",
                            modelId, nextModelId, exception.getErrorType());
                } finally {
                    ModelRetryContext.unbind(context);
                }
            }
        } finally {
            ModelRetryContext.unbind(context);
        }
        throw new IllegalStateException("fallback scoringModel target list is empty");
    }
}
