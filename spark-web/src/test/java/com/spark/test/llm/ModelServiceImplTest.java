package com.spark.test.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.vo.ModelVO;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.service.impl.ModelServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
 * 模型上下文窗口配置测试
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
}
