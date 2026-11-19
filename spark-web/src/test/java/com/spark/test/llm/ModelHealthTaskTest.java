package com.spark.test.llm;

import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.enums.StatusEnum;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.model.health.ModelHealthProber;
import com.spark.task.ModelHealthTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 18:30:00
 * 模型健康定时探活任务测试
 */
public class ModelHealthTaskTest {
    private ModelHealthTask task;
    private ModelDao modelDao;
    private ModelHealthProber prober;

    @BeforeEach
    void setUp() {
        task = new ModelHealthTask();
        modelDao = mock(ModelDao.class);
        prober = mock(ModelHealthProber.class);
        ReflectionTestUtils.setField(task, "modelDao", modelDao);
        ReflectionTestUtils.setField(task, "modelHealthProber", prober);
        when(modelDao.updateDBById(any(Model.class))).thenReturn(1);
    }

    /**
     * 验证探活失败时写入异常状态并刷新最近失败时间，全量复制保留上下文窗口与降级链现值，审计字段置空交BaseDao填充
     */
    @Test
    void shouldWriteAbnormalAndKeepUnconditionalColumnsOnProbeFail() {
        ModelResult model = this.createModel(1L, 1);
        model.setContextLength(8192);
        model.setFallbackModelIds("2,3");
        Timestamp oldFailDt = new Timestamp(1000L);
        model.setLastFailDt(oldFailDt);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(model));
        when(prober.probe(model)).thenReturn(false);
        task.probeAllModels();
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        Model saved = modelCaptor.getValue();
        assertEquals(1L, saved.getId());
        assertEquals(2, saved.getHealthStatus());
        assertNotNull(saved.getLastFailDt());
        assertTrue(saved.getLastFailDt().getTime() > oldFailDt.getTime());
        assertEquals(8192, saved.getContextLength());
        assertEquals("2,3", saved.getFallbackModelIds());
        assertNull(saved.getUpdatedBy());
        assertNull(saved.getUpdatedDt());
    }

    /**
     * 验证探活成功且原状态异常时恢复为正常，最近失败时间保留供面板追溯
     */
    @Test
    void shouldRestoreNormalAndKeepLastFailDtOnProbeSuccess() {
        ModelResult model = this.createModel(1L, 2);
        Timestamp oldFailDt = new Timestamp(1000L);
        model.setLastFailDt(oldFailDt);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(model));
        when(prober.probe(model)).thenReturn(true);
        task.probeAllModels();
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        Model saved = modelCaptor.getValue();
        assertEquals(1, saved.getHealthStatus());
        assertEquals(oldFailDt, saved.getLastFailDt());
    }

    /**
     * 验证探活成功且原状态正常时跳过写库，避免每轮全量无谓更新
     */
    @Test
    void shouldSkipWriteWhenProbeSuccessAndAlreadyNormal() {
        ModelResult model = this.createModel(1L, 1);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(model));
        when(prober.probe(model)).thenReturn(true);
        task.probeAllModels();
        verify(modelDao, never()).updateDBById(any());
    }

    /**
     * 验证单个模型探活抛出运行时异常不中断整轮，后续模型仍正常探活落库
     */
    @Test
    void shouldContinueRoundWhenSingleProbeThrows() {
        ModelResult first = this.createModel(1L, 1);
        ModelResult second = this.createModel(2L, 1);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(first, second));
        when(prober.probe(first)).thenThrow(new RuntimeException("probe crash"));
        when(prober.probe(second)).thenReturn(false);
        task.probeAllModels();
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao, times(1)).updateDBById(modelCaptor.capture());
        Model saved = modelCaptor.getValue();
        assertEquals(2L, saved.getId());
        assertEquals(2, saved.getHealthStatus());
    }

    /**
     * 验证探活查询口径为启用且未删除的全量模型，空列表时不触发探活
     */
    @Test
    void shouldQueryEnabledFullListAndSkipEmpty() {
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of());
        task.probeAllModels();
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery query = queryCaptor.getValue();
        assertEquals(StatusEnum.NORMAL.getValue(), query.getStatus());
        assertEquals(1, query.getDeleteFlag());
        assertEquals(false, query.isPage());
        verify(prober, never()).probe(any());
    }

    /**
     * 验证健康状态写库影响行数为0时仅记录日志，不抛异常中断整轮
     */
    @Test
    void shouldNotThrowWhenUpdateAffectsNoRow() {
        ModelResult model = this.createModel(1L, 1);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(model));
        when(prober.probe(model)).thenReturn(false);
        when(modelDao.updateDBById(any(Model.class))).thenReturn(0);
        task.probeAllModels();
        verify(modelDao).updateDBById(any());
    }

    /**
     * 构造探活任务模型测试数据
     * @param id 模型id
     * @param healthStatus 健康状态
     * @return 模型数据
     */
    private ModelResult createModel(Long id, Integer healthStatus) {
        ModelResult model = new ModelResult();
        model.setId(id);
        model.setTenantId(0L);
        model.setProviderId(10L);
        model.setType(1);
        model.setName("model-" + id);
        model.setStatus(StatusEnum.NORMAL.getValue());
        model.setDeleteFlag(1);
        model.setHealthStatus(healthStatus);
        return model;
    }
}
