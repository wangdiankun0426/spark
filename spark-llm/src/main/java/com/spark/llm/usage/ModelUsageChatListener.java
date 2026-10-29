package com.spark.llm.usage;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.dto.UsageSnapshot;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.common.llm.ModelUsageSceneHolder;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 语言模型用量监听器
 * 随模型实例创建；onRequest在业务线程快照会话与场景，onResponse在回调线程凭attributes上报
 */
public class ModelUsageChatListener implements ChatModelListener {

    private final static Logger logger = LoggerFactory.getLogger(ModelUsageChatListener.class);

    /**
     * attributes中会话快照的key，流式回调线程已拿不到ThreadLocal
     */
    private static final Object SNAPSHOT_KEY = new Object();

    private final ModelUsageRecorder recorder;
    private final ModelInfo modelInfo;

    public ModelUsageChatListener(ModelUsageRecorder recorder, ModelInfo modelInfo) {
        this.recorder = recorder;
        this.modelInfo = modelInfo;
    }

    @Override
    public void onRequest(ChatModelRequestContext context) {
        try {
            Long tenantId = SessionHolder.getCurrentTenantId();
            Long userId = SessionHolder.getCurrentUserId();
            Long deptId = SessionHolder.getCurrentDeptId();
            String scene = ModelUsageSceneHolder.current().getValue();
            UsageSnapshot snapshot = new UsageSnapshot(tenantId, userId, deptId, scene);
            context.attributes().put(SNAPSHOT_KEY, snapshot);
        } catch (Exception e) {
            logger.warn("model usage onRequest error, modelId={}", modelInfo.getModelId(), e);
        }
    }

    @Override
    public void onResponse(ChatModelResponseContext context) {
        try {
            UsageSnapshot snapshot = this.resolveSnapshot(context.attributes().get(SNAPSHOT_KEY));
            TokenUsage tokenUsage = context.chatResponse().tokenUsage();
            Integer inputTokens = tokenUsage == null ? null : tokenUsage.inputTokenCount();
            Integer outputTokens = tokenUsage == null ? null : tokenUsage.outputTokenCount();
            Integer totalTokens = tokenUsage == null ? null : tokenUsage.totalTokenCount();
            recorder.record(modelInfo, snapshot, inputTokens, outputTokens, totalTokens);
        } catch (Exception e) {
            logger.warn("model usage onResponse error, modelId={}", modelInfo.getModelId(), e);
        }
    }

    @Override
    public void onError(ChatModelErrorContext context) {
        // 调用失败不产生token消耗，不记录
    }

    /**
     * 解析会话快照，缺失时按系统调用兜底
     * @param value attributes中的快照
     * @return 会话快照
     */
    private UsageSnapshot resolveSnapshot(Object value) {
        if (value instanceof UsageSnapshot) {
            return (UsageSnapshot) value;
        }
        return new UsageSnapshot(null, null, null, ModelUsageSceneEnum.OTHER.getValue());
    }
}
