package com.spark.llm.usage;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.dto.UsageSnapshot;
import com.spark.common.bean.llm.entity.ModelUsage;
import com.spark.common.constant.DefaultBaseParam;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.dao.llm.ModelUsageDao;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型token用量采集器
 * 调用线程入队、定时批量落库，采集失败不影响模型主链路
 */
@Component
public class ModelUsageRecorder {

    private final static Logger logger = LoggerFactory.getLogger(ModelUsageRecorder.class);

    /**
     * 内存队列容量
     */
    private static final int QUEUE_CAPACITY = 20000;

    /**
     * 单批落库条数
     */
    private static final int BATCH_SIZE = 500;

    /**
     * 队列丢弃告警最小间隔毫秒
     */
    private static final long DROP_WARN_INTERVAL_MS = 60000L;

    @Autowired
    private ModelUsageDao modelUsageDao;

    private final BlockingQueue<ModelUsage> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    /**
     * 上次队列丢弃告警时间
     */
    private final AtomicLong lastDropWarnTime = new AtomicLong(0L);

    /**
     * 采集一条模型用量
     * @param modelInfo 模型元数据
     * @param snapshot 调用瞬间的会话与场景快照
     * @param inputTokens 输入token
     * @param outputTokens 输出token
     * @param totalTokens 总token
     * @param retryCount 重试次数
     * @param fallbackFlag 是否经降级
     */
    public void record(ModelInfo modelInfo, UsageSnapshot snapshot, Integer inputTokens,
                       Integer outputTokens, Integer totalTokens, int retryCount, boolean fallbackFlag) {
        if (modelInfo == null || modelInfo.getModelId() == null) {
            return;
        }
        try {
            ModelUsage usage = this.createBaseUsage(modelInfo, snapshot);
            usage.setInputTokens(inputTokens);
            usage.setOutputTokens(outputTokens);
            usage.setTotalTokens(this.normalizeTotal(inputTokens, outputTokens, totalTokens));
            usage.setRetryCount(retryCount);
            usage.setFallbackFlag(fallbackFlag ? 1 : 0);
            usage.setRateLimitFlag(0);
            this.offerUsage(usage);
        } catch (Exception e) {
            logger.warn("record model usage error, modelId={}", modelInfo.getModelId(), e);
        }
    }

    /**
     * 采集一条被租户限流拒绝的调用，不产生token消耗
     * @param modelInfo 模型元数据
     * @param snapshot 调用瞬间的会话与场景快照
     */
    public void recordRateLimited(ModelInfo modelInfo, UsageSnapshot snapshot) {
        if (modelInfo == null || modelInfo.getModelId() == null) {
            return;
        }
        try {
            ModelUsage usage = this.createBaseUsage(modelInfo, snapshot);
            usage.setRetryCount(0);
            usage.setFallbackFlag(0);
            usage.setRateLimitFlag(1);
            this.offerUsage(usage);
        } catch (Exception e) {
            logger.warn("record rate limited usage error, modelId={}", modelInfo.getModelId(), e);
        }
    }

    /**
     * 将队列用量批量落库
     */
    public void flush() {
        if (queue.isEmpty()) {
            return;
        }
        List<ModelUsage> buffer = new ArrayList<>();
        queue.drainTo(buffer);
        if (buffer.isEmpty()) {
            return;
        }
        for (int from = 0; from < buffer.size(); from += BATCH_SIZE) {
            int to = Math.min(from + BATCH_SIZE, buffer.size());
            List<ModelUsage> batch = buffer.subList(from, to);
            try {
                modelUsageDao.insertBatch(batch);
            } catch (Exception e) {
                logger.error("flush model usage error, batchSize={}", batch.size(), e);
            }
        }
        logger.info("flush model usage success, size={}", buffer.size());
    }

    /**
     * 停机前尽量落库，避免队列数据丢失
     */
    @PreDestroy
    public void onDestroy() {
        try {
            this.flush();
            queue.clear();
        } catch (Exception e) {
            logger.warn("destroy flush model usage error", e);
        }
    }

    /**
     * 总token兜底：为空时取输入输出之和，仍为空返回null
     * @param inputTokens 输入token
     * @param outputTokens 输出token
     * @param totalTokens 总token
     * @return 归一后的总token
     */
    private Integer normalizeTotal(Integer inputTokens, Integer outputTokens, Integer totalTokens) {
        if (totalTokens != null) {
            return totalTokens;
        }
        if (inputTokens == null && outputTokens == null) {
            return null;
        }
        int input = inputTokens == null ? 0 : inputTokens;
        int output = outputTokens == null ? 0 : outputTokens;
        return input + output;
    }

    /**
     * 队列满时限频告警
     */
    private void warnDrop() {
        long now = System.currentTimeMillis();
        long last = lastDropWarnTime.get();
        if (now - last > DROP_WARN_INTERVAL_MS && lastDropWarnTime.compareAndSet(last, now)) {
            logger.warn("model usage queue is full, drop record, queueSize={}", QUEUE_CAPACITY);
        }
    }

    /**
     * 构建用量基础字段，会话缺失按系统调用兜底
     * @param modelInfo 模型元数据
     * @param snapshot 调用瞬间的会话与场景快照
     * @return 用量明细
     */
    private ModelUsage createBaseUsage(ModelInfo modelInfo, UsageSnapshot snapshot) {
        if (snapshot == null) {
            snapshot = new UsageSnapshot(DefaultBaseParam.SYSTEM_TENANT_ID, DefaultBaseParam.SYSTEM_USER_ID,
                    DefaultBaseParam.SYSTEM_DEPT_ID, ModelUsageSceneEnum.OTHER.getValue());
        }
        Long tenantId = snapshot.getTenantId() == null ? DefaultBaseParam.SYSTEM_TENANT_ID : snapshot.getTenantId();
        Long userId = snapshot.getUserId() == null ? DefaultBaseParam.SYSTEM_USER_ID : snapshot.getUserId();
        Long deptId = snapshot.getDeptId() == null ? DefaultBaseParam.SYSTEM_DEPT_ID : snapshot.getDeptId();
        String scene = snapshot.getScene() == null ? ModelUsageSceneEnum.OTHER.getValue() : snapshot.getScene();
        ModelUsage usage = new ModelUsage();
        usage.setTenantId(tenantId);
        usage.setUserId(userId);
        usage.setModelId(modelInfo.getModelId());
        usage.setModelName(modelInfo.getModelName());
        usage.setProviderId(modelInfo.getProviderId());
        usage.setType(modelInfo.getType());
        usage.setScene(scene);
        usage.setDeptId(deptId);
        usage.setCreatedBy(userId);
        usage.setUpdatedBy(userId);
        return usage;
    }

    /**
     * 用量入队，队列满时告警丢弃
     * @param usage 用量明细
     */
    private void offerUsage(ModelUsage usage) {
        if (!queue.offer(usage)) {
            this.warnDrop();
        }
    }
}
