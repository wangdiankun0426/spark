package com.spark.llm.usage;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.entity.ModelUsage;
import com.spark.dao.llm.ModelUsageDao;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
 * 模型token用量采集器，调用线程入队、定时批量落库，采集失败不影响模型主链路
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

    /**
     * 无会话系统调用兜底租户id
     */
    private static final Long SYSTEM_TENANT_ID = 0L;

    /**
     * 无会话系统调用兜底用户id
     */
    private static final Long SYSTEM_USER_ID = 101L;

    /**
     * 无会话系统调用兜底部门id
     */
    private static final Long SYSTEM_DEPT_ID = 0L;

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
     * @param tenantId 租户id
     * @param userId 使用人id
     * @param deptId 部门id
     * @param scene 场景编码
     * @param inputTokens 输入token
     * @param outputTokens 输出token
     * @param totalTokens 总token
     */
    public void record(ModelInfo modelInfo, Long tenantId, Long userId, Long deptId, String scene,
                       Integer inputTokens, Integer outputTokens, Integer totalTokens) {
        if (modelInfo == null || modelInfo.getModelId() == null) {
            return;
        }
        try {
            Long tenantIdVal = tenantId == null ? SYSTEM_TENANT_ID : tenantId;
            Long operatorId = userId == null ? SYSTEM_USER_ID : userId;
            ModelUsage usage = new ModelUsage();
            usage.setTenantId(tenantIdVal);
            usage.setUserId(operatorId);
            usage.setModelId(modelInfo.getModelId());
            usage.setModelName(modelInfo.getModelName());
            usage.setProviderId(modelInfo.getProviderId());
            usage.setType(modelInfo.getType());
            usage.setScene(scene);
            usage.setInputTokens(inputTokens);
            usage.setOutputTokens(outputTokens);
            usage.setTotalTokens(this.normalizeTotal(inputTokens, outputTokens, totalTokens));
            usage.setDeptId(deptId == null ? SYSTEM_DEPT_ID : deptId);
            usage.setCreatedBy(operatorId);
            usage.setUpdatedBy(operatorId);
            if (!queue.offer(usage)) {
                this.warnDrop();
            }
        } catch (Exception e) {
            logger.warn("record model usage error, modelId={}", modelInfo.getModelId(), e);
        }
    }

    /**
     * 定时将队列用量批量落库
     */
    @Scheduled(fixedDelay = 10000)
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
}
