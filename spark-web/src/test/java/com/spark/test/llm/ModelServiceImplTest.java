package com.spark.test.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.vo.ModelVO;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.service.impl.ModelServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
 * @since 2026-11-18 18:10:00
 * 模型配置测试
 */
public class ModelServiceImplTest {
    private ModelServiceImpl modelService;
    private ModelDao modelDao;
    private ModelFactory modelFactory;
    private AgentFactory agentFactory;

    @BeforeEach
    void setUp() {
        modelService = spy(new ModelServiceImpl());
        modelDao = mock(ModelDao.class);
        modelFactory = mock(ModelFactory.class);
        agentFactory = mock(AgentFactory.class);
        ReflectionTestUtils.setField(modelService, "modelDao", modelDao);
        ReflectionTestUtils.setField(modelService, "modelFactory", modelFactory);
        ReflectionTestUtils.setField(modelService, "agentFactory", agentFactory);
        doReturn(1001L).when(modelService).genObjectId(ObjectTypeEnum.MODEL);
        Session session = new Session();
        session.setUserId(101L);
        session.setTenantId(0L);
        session.setAccountType(AccountTypeEnum.SITE_ADMIN.getValue());
        SessionHolder.initLocalSession(session);
    }

    @AfterEach
    void tearDown() {
        SessionHolder.clearLocalSession();
    }

    /**
     * 验证创建模型未配置上下文窗口时允许保存空值，不虚构默认窗口大小
     */
    @Test
    void shouldAllowNullContextLengthOnCreate() {
        when(modelDao.insertDB(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        ResultData<Void> result = modelService.createModel(modelVO);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).insertDB(modelCaptor.capture());
        assertNull(modelCaptor.getValue().getContextLength());
        assertEquals(ResultData.OK, result.getCode());
    }

    /**
     * 验证修改模型可以保存较大正整数上下文窗口，成功后按原逻辑清理模型和智能体缓存
     */
    @Test
    void shouldSavePositiveContextLengthOnUpdate() {
        when(modelDao.queryModel(any())).thenReturn(new ModelResult());
        when(modelDao.updateDBById(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        modelVO.setContextLength(Integer.MAX_VALUE);
        ResultData<Void> result = modelService.updateModel(modelVO);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        assertEquals(Integer.MAX_VALUE, modelCaptor.getValue().getContextLength());
        assertEquals(ResultData.OK, result.getCode());
        verify(modelFactory).clearModelCache(1L);
        verify(agentFactory).clearAllAgentCache();
    }

    /**
     * 验证修改模型清空上下文窗口时DAO收到空值，配合Mapper直接写列避免保存成功后仍保留旧配置
     */
    @Test
    void shouldClearContextLengthOnUpdate() {
        when(modelDao.queryModel(any())).thenReturn(new ModelResult());
        when(modelDao.updateDBById(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        ResultData<Void> result = modelService.updateModel(modelVO);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        assertNull(modelCaptor.getValue().getContextLength());
        assertEquals(ResultData.OK, result.getCode());
    }

    /**
     * 验证创建传入0和更新传入负数时返回无效参数，不写数据库也不清理缓存
     */
    @Test
    void shouldRejectNonPositiveContextLength() {
        ModelVO createVO = new ModelVO();
        createVO.setContextLength(0);
        ResultData<Void> createResult = modelService.createModel(createVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), createResult.getCode());
        ModelVO updateVO = new ModelVO();
        updateVO.setId(1L);
        updateVO.setContextLength(-1);
        ResultData<Void> updateResult = modelService.updateModel(updateVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), updateResult.getCode());
        verify(modelDao, never()).insertDB(any());
        verify(modelDao, never()).updateDBById(any());
        verify(modelFactory, never()).clearModelCache(any());
        verify(agentFactory, never()).clearAllAgentCache();
    }

    /**
     * 验证创建模型时六个有序降级候选不受五项限制，候选完整且保存时清除空格并规范化为逗号分隔字符串
     */
    @Test
    void shouldNormalizeOrderedFallbackChainOnCreate() {
        List<ModelResult> candidateList = new ArrayList<>();
        for (long id = 2L; id <= 7L; id++) {
            ModelResult candidate = this.createModelResult(id, 0L, 1);
            candidateList.add(candidate);
        }
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(candidateList);
        when(modelDao.insertDB(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        modelVO.setType(1);
        modelVO.setFallbackModelIds(" 2, 3,4,5,6,7 ");
        ResultData<Void> result = modelService.createModel(modelVO);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).insertDB(modelCaptor.capture());
        Model savedModel = modelCaptor.getValue();
        String savedFallbackModelIds = savedModel.getFallbackModelIds();
        assertEquals("2,3,4,5,6,7", savedFallbackModelIds);
        int resultCode = result.getCode();
        assertEquals(ResultData.OK, resultCode);
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery candidateQuery = queryCaptor.getValue();
        List<Long> expectedIds = List.of(2L, 3L, 4L, 5L, 6L, 7L);
        List<Long> actualIds = candidateQuery.getIds();
        assertEquals(expectedIds, actualIds);
        Long tenantId = candidateQuery.getTenantId();
        assertEquals(0L, tenantId);
        Integer type = candidateQuery.getType();
        assertEquals(1, type);
        Integer status = candidateQuery.getStatus();
        assertEquals(StatusEnum.NORMAL.getValue(), status);
        Integer deleteFlag = candidateQuery.getDeleteFlag();
        assertEquals(1, deleteFlag);
        boolean page = candidateQuery.isPage();
        assertEquals(false, page);
    }

    /**
     * 验证创建模型时空项、重复、零、负数、非数字、小数、Long溢出及超过256字符的降级链均返回无效参数且不访问数据库
     */
    @Test
    void shouldRejectMalformedFallbackChains() {
        List<String> invalidChains = List.of("1,,2", "1,", ",1", "1,1", "0", "-1", "abc", "1.5", "9223372036854775808");
        for (String invalidChain : invalidChains) {
            ModelVO modelVO = new ModelVO();
            modelVO.setType(1);
            modelVO.setFallbackModelIds(invalidChain);
            ResultData<Void> result = modelService.createModel(modelVO);
            int resultCode = result.getCode();
            assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), resultCode);
        }
        StringBuilder chainBuilder = new StringBuilder();
        for (long id = 1000L; id <= 1100L; id++) {
            if (chainBuilder.length() > 0) {
                chainBuilder.append(",");
            }
            chainBuilder.append(id);
        }
        ModelVO tooLongVO = new ModelVO();
        tooLongVO.setType(1);
        String tooLongChain = chainBuilder.toString();
        tooLongVO.setFallbackModelIds(tooLongChain);
        ResultData<Void> tooLongResult = modelService.createModel(tooLongVO);
        int tooLongResultCode = tooLongResult.getCode();
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), tooLongResultCode);
        verify(modelDao, never()).queryModelList(any());
        verify(modelDao, never()).insertDB(any());
    }

    /**
     * 验证修改模型时降级链包含主模型自身会被拒绝，不查询候选也不执行更新
     */
    @Test
    void shouldRejectSelfFallbackOnUpdate() {
        ModelResult originalModel = this.createModelResult(1L, 8L, 1);
        when(modelDao.queryModel(any())).thenReturn(originalModel);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        modelVO.setFallbackModelIds("1,2");
        ResultData<Void> result = modelService.updateModel(modelVO);
        int resultCode = result.getCode();
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), resultCode);
        verify(modelDao, never()).queryModelList(any());
        verify(modelDao, never()).updateDBById(any());
    }

    /**
     * 验证修改模型同时切换类型时按原租户和新类型校验候选，成功后保存规范化降级链
     */
    @Test
    void shouldValidateFallbackWithOriginalTenantAndEffectiveType() {
        ModelResult originalModel = this.createModelResult(1L, 8L, 1);
        when(modelDao.queryModel(any())).thenReturn(originalModel);
        List<ModelResult> candidateList = new ArrayList<>();
        ModelResult firstCandidate = this.createModelResult(2L, 8L, 2);
        candidateList.add(firstCandidate);
        ModelResult secondCandidate = this.createModelResult(3L, 8L, 2);
        candidateList.add(secondCandidate);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(candidateList);
        when(modelDao.updateDBById(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        modelVO.setType(2);
        modelVO.setFallbackModelIds(" 2,3 ");
        ResultData<Void> result = modelService.updateModel(modelVO);
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery candidateQuery = queryCaptor.getValue();
        Long tenantId = candidateQuery.getTenantId();
        assertEquals(8L, tenantId);
        Integer type = candidateQuery.getType();
        assertEquals(2, type);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        Model savedModel = modelCaptor.getValue();
        String savedFallbackModelIds = savedModel.getFallbackModelIds();
        assertEquals("2,3", savedFallbackModelIds);
        int resultCode = result.getCode();
        assertEquals(ResultData.OK, resultCode);
    }

    /**
     * 验证修改模型未传类型时沿用原类型查询候选，任一候选因租户、类型、状态或删除标记不符而未返回时拒绝更新
     */
    @Test
    void shouldRejectMissingFallbackCandidateWithOriginalType() {
        ModelResult originalModel = this.createModelResult(1L, 8L, 1);
        when(modelDao.queryModel(any())).thenReturn(originalModel);
        ModelResult availableCandidate = this.createModelResult(2L, 8L, 1);
        List<ModelResult> candidateList = List.of(availableCandidate);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(candidateList);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        modelVO.setFallbackModelIds("2,3");
        ResultData<Void> result = modelService.updateModel(modelVO);
        int resultCode = result.getCode();
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), resultCode);
        ArgumentCaptor<ModelQuery> queryCaptor = ArgumentCaptor.forClass(ModelQuery.class);
        verify(modelDao).queryModelList(queryCaptor.capture());
        ModelQuery candidateQuery = queryCaptor.getValue();
        Long tenantId = candidateQuery.getTenantId();
        assertEquals(8L, tenantId);
        Integer type = candidateQuery.getType();
        assertEquals(1, type);
        verify(modelDao, never()).updateDBById(any());
        verify(modelFactory, never()).clearModelCache(any());
        verify(agentFactory, never()).clearAllAgentCache();
    }

    /**
     * 验证创建模型配置降级链但模型类型无效时直接拒绝，避免跨类型候选通过校验
     */
    @Test
    void shouldRejectFallbackWithoutValidType() {
        ModelVO modelVO = new ModelVO();
        modelVO.setFallbackModelIds("2");
        ResultData<Void> result = modelService.createModel(modelVO);
        int resultCode = result.getCode();
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), resultCode);
        verify(modelDao, never()).queryModelList(any());
        verify(modelDao, never()).insertDB(any());
    }

    /**
     * 验证候选查询按id绕过通用删除条件时，Java仍会拒绝逻辑删除的备用模型且不执行更新
     */
    @Test
    void shouldRejectDeletedFallbackCandidate() {
        ModelResult originalModel = this.createModelResult(1L, 8L, 1);
        when(modelDao.queryModel(any())).thenReturn(originalModel);
        ModelResult deletedCandidate = this.createModelResult(2L, 8L, 1);
        deletedCandidate.setDeleteFlag(-1);
        when(modelDao.queryModelList(any(ModelQuery.class))).thenReturn(List.of(deletedCandidate));
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        modelVO.setFallbackModelIds("2");
        ResultData<Void> result = modelService.updateModel(modelVO);
        int resultCode = result.getCode();
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), resultCode);
        verify(modelDao, never()).updateDBById(any());
        verify(modelFactory, never()).clearModelCache(any());
    }

    /**
     * 验证修改模型未传降级链时DAO收到null，配合Mapper无条件写字段可清空原配置
     */
    @Test
    void shouldClearFallbackChainOnUpdate() {
        ModelResult originalModel = this.createModelResult(1L, 8L, 1);
        when(modelDao.queryModel(any())).thenReturn(originalModel);
        when(modelDao.updateDBById(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        ResultData<Void> result = modelService.updateModel(modelVO);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        Model savedModel = modelCaptor.getValue();
        String savedFallbackModelIds = savedModel.getFallbackModelIds();
        assertNull(savedFallbackModelIds);
        int resultCode = result.getCode();
        assertEquals(ResultData.OK, resultCode);
        verify(modelDao, never()).queryModelList(any());
    }

    /**
     * 验证创建权重-1与修改权重10000均返回无效参数，不写数据库也不清理缓存
     */
    @Test
    void shouldRejectOutOfRangeWeight() {
        ModelVO createVO = new ModelVO();
        createVO.setWeight(-1);
        ResultData<Void> createResult = modelService.createModel(createVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), createResult.getCode());
        ModelVO updateVO = new ModelVO();
        updateVO.setId(1L);
        updateVO.setWeight(10000);
        ResultData<Void> updateResult = modelService.updateModel(updateVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), updateResult.getCode());
        verify(modelDao, never()).insertDB(any());
        verify(modelDao, never()).updateDBById(any());
    }

    /**
     * 验证创建边界权重0与9999均保存成功，DAO收到原始权重值
     */
    @Test
    void shouldSaveBoundaryWeightOnCreate() {
        when(modelDao.insertDB(any(Model.class))).thenReturn(1);
        ModelVO zeroVO = new ModelVO();
        zeroVO.setWeight(0);
        ResultData<Void> zeroResult = modelService.createModel(zeroVO);
        assertEquals(ResultData.OK, zeroResult.getCode());
        ModelVO maxVO = new ModelVO();
        maxVO.setWeight(9999);
        ResultData<Void> maxResult = modelService.createModel(maxVO);
        assertEquals(ResultData.OK, maxResult.getCode());
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao, times(2)).insertDB(modelCaptor.capture());
        List<Model> savedModels = modelCaptor.getAllValues();
        assertEquals(0, savedModels.get(0).getWeight());
        assertEquals(9999, savedModels.get(1).getWeight());
    }

    /**
     * 验证修改未传权重时放行且DAO收到空值，健康列不被管理端编辑触碰
     */
    @Test
    void shouldAllowNullWeightOnUpdate() {
        when(modelDao.queryModel(any())).thenReturn(new ModelResult());
        when(modelDao.updateDBById(any(Model.class))).thenReturn(1);
        ModelVO modelVO = new ModelVO();
        modelVO.setId(1L);
        ResultData<Void> result = modelService.updateModel(modelVO);
        assertEquals(ResultData.OK, result.getCode());
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(modelDao).updateDBById(modelCaptor.capture());
        Model savedModel = modelCaptor.getValue();
        assertNull(savedModel.getWeight());
        assertNull(savedModel.getHealthStatus());
        assertNull(savedModel.getLastFailDt());
    }

    /**
     * 构造模型测试数据
     * @param id 模型id
     * @param tenantId 租户id
     * @param type 模型类型
     * @return 模型数据
     */
    private ModelResult createModelResult(Long id, Long tenantId, Integer type) {
        ModelResult modelResult = new ModelResult();
        modelResult.setId(id);
        modelResult.setTenantId(tenantId);
        modelResult.setType(type);
        modelResult.setStatus(StatusEnum.NORMAL.getValue());
        modelResult.setDeleteFlag(1);
        return modelResult;
    }
}
