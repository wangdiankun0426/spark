package com.spark.llm.usage;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.dto.UsageSnapshot;
import com.spark.common.llm.ModelUsageSceneHolder;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
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
 * @since 2026/10/27 10:00:00
 * 向量模型用量包装器
 * 委托真实模型执行，批量返回后读取token用量上报；embed(String)经default方法最终走到embedAll，只需拦截一处
 */
public class EmbeddingUsageWrapper implements EmbeddingModel {

    private final static Logger logger = LoggerFactory.getLogger(EmbeddingUsageWrapper.class);

    private final EmbeddingModel delegate;
    private final ModelUsageRecorder recorder;
    private final ModelInfo modelInfo;

    public EmbeddingUsageWrapper(EmbeddingModel delegate, ModelUsageRecorder recorder, ModelInfo modelInfo) {
        this.delegate = delegate;
        this.recorder = recorder;
        this.modelInfo = modelInfo;
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
        Response<List<Embedding>> response = delegate.embedAll(segments);
        this.reportUsage(response.tokenUsage());
        return response;
    }

    @Override
    public int dimension() {
        return delegate.dimension();
    }

    /**
     * 上报向量调用用量
     * @param tokenUsage 第三方返回的token用量
     */
    private void reportUsage(TokenUsage tokenUsage) {
        try {
            Integer inputTokens = tokenUsage == null ? null : tokenUsage.inputTokenCount();
            Integer outputTokens = tokenUsage == null ? null : tokenUsage.outputTokenCount();
            Integer totalTokens = tokenUsage == null ? null : tokenUsage.totalTokenCount();
            UsageSnapshot snapshot = new UsageSnapshot(
                    SessionHolder.getCurrentTenantId(),
                    SessionHolder.getCurrentUserId(),
                    SessionHolder.getCurrentDeptId(),
                    ModelUsageSceneHolder.current().getValue());
            recorder.record(modelInfo, snapshot, inputTokens, outputTokens, totalTokens);
        } catch (Exception e) {
            logger.warn("embedding usage report error, modelId={}", modelInfo.getModelId(), e);
        }
    }
}
