package com.spark.llm.usage;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.dto.UsageSnapshot;
import com.spark.common.llm.ModelUsageSceneHolder;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
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
 * @since 2026-10-29 17:30:00
 * 排序模型用量包装器，委托真实模型执行，批量打分返回后读取token用量上报
 */
public class ScoringUsageWrapper implements ScoringModel {

    private final static Logger logger = LoggerFactory.getLogger(ScoringUsageWrapper.class);

    private final ScoringModel delegate;
    private final ModelUsageRecorder recorder;
    private final ModelInfo modelInfo;

    public ScoringUsageWrapper(ScoringModel delegate, ModelUsageRecorder recorder, ModelInfo modelInfo) {
        this.delegate = delegate;
        this.recorder = recorder;
        this.modelInfo = modelInfo;
    }

    @Override
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        Response<List<Double>> response = delegate.scoreAll(segments, query);
        this.reportUsage(response.tokenUsage());
        return response;
    }

    /**
     * 上报排序调用用量，排序模型仅产生输入token
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
            logger.warn("scoring usage report error, modelId={}", modelInfo.getModelId(), e);
        }
    }
}
