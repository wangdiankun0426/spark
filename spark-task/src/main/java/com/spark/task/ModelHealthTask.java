package com.spark.task;

import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.enums.DeleteFlagEnum;
import com.spark.common.enums.ModelHealthStatusEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.model.health.ModelHealthProber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 18:00:00
 * 模型健康定时探活任务，探活结果落库供默认模型选取与面板展示
 */
@Service
public class ModelHealthTask {
    private static final Logger logger = LoggerFactory.getLogger(ModelHealthTask.class);
    private static final long PROBE_INTERVAL_MS = 60000L;
    private static final long PROBE_INITIAL_DELAY_MS = 30000L;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private ModelHealthProber modelHealthProber;

    /**
     * 周期探活全部启用模型并更新健康状态
     */
    @Scheduled(fixedDelay = PROBE_INTERVAL_MS, initialDelay = PROBE_INITIAL_DELAY_MS)
    public void probeAllModels() {
        ModelQuery query = new ModelQuery();
        query.setStatus(StatusEnum.NORMAL.getValue());
        query.setDeleteFlag(DeleteFlagEnum.NORMAL.getValue());
        query.setPage(false);
        List<ModelResult> modelList = modelDao.queryModelList(query);
        if (CollectionUtil.isEmpty(modelList)) {
            return;
        }
        for (ModelResult modelResult : modelList) {
            try {
                this.probeAndUpdate(modelResult);
            } catch (RuntimeException e) {
                Long modelId = modelResult.getId();
                String errorMessage = e.getMessage();
                logger.warn("model probe round error, modelId={}, errMsg={}", modelId, errorMessage);
            }
        }
    }

    /**
     * 探活单个模型并按状态变化落库，全量复制保留无条件写列现值
     * @param modelResult 模型数据
     */
    private void probeAndUpdate(ModelResult modelResult) {
        if (modelResult == null || modelResult.getId() == null) {
            return;
        }
        Long modelId = modelResult.getId();
        boolean healthy = modelHealthProber.probe(modelResult);
        Integer currentHealth = modelResult.getHealthStatus();
        boolean normal = ModelHealthStatusEnum.NORMAL.getValue().equals(currentHealth);
        if (healthy && normal) {
            return;
        }
        Model model = new Model();
        BeanUtil.copyProperties(modelResult, model);
        // 审计字段交BaseDao填充，探活线程无登录会话
        model.setUpdatedBy(null);
        model.setUpdatedDt(null);
        if (healthy) {
            Integer normalValue = ModelHealthStatusEnum.NORMAL.getValue();
            model.setHealthStatus(normalValue);
        } else {
            Integer abnormalValue = ModelHealthStatusEnum.ABNORMAL.getValue();
            model.setHealthStatus(abnormalValue);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            model.setLastFailDt(now);
        }
        int count = modelDao.updateDBById(model);
        if (count < 1) {
            logger.warn("model health update skipped, modelId={}", modelId);
            return;
        }
        Integer healthStatus = model.getHealthStatus();
        logger.info("model health updated, modelId={}, healthStatus={}", modelId, healthStatus);
    }
}
