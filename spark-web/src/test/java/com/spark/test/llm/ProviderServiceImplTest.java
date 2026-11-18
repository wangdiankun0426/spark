package com.spark.test.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.bean.llm.vo.ProviderVO;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.dao.llm.ProviderDao;
import com.spark.dao.llm.ProviderKeyDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.security.LlmApiKeyCipher;
import com.spark.llm.service.impl.ProviderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:40:00
 * 模型厂商凭据创建与更新测试
 */
public class ProviderServiceImplTest {
    private ProviderServiceImpl providerService;
    private ProviderDao providerDao;
    private ProviderKeyDao providerKeyDao;
    private ModelFactory modelFactory;
    private AgentFactory agentFactory;
    private LlmApiKeyCipher llmApiKeyCipher;

    @BeforeEach
    void setUp() {
        providerService = new ProviderServiceImpl();
        providerDao = mock(ProviderDao.class);
        providerKeyDao = mock(ProviderKeyDao.class);
        modelFactory = mock(ModelFactory.class);
        agentFactory = mock(AgentFactory.class);
        llmApiKeyCipher = mock(LlmApiKeyCipher.class);
        ReflectionTestUtils.setField(providerService, "providerDao", providerDao);
        ReflectionTestUtils.setField(providerService, "providerKeyDao", providerKeyDao);
        ReflectionTestUtils.setField(providerService, "modelFactory", modelFactory);
        ReflectionTestUtils.setField(providerService, "agentFactory", agentFactory);
        ReflectionTestUtils.setField(providerService, "llmApiKeyCipher", llmApiKeyCipher);
        Session session = new Session();
        session.setUserId(101L);
        session.setAccountType(AccountTypeEnum.SITE_ADMIN.getValue());
        SessionHolder.initLocalSession(session);
    }

    @AfterEach
    void tearDown() {
        SessionHolder.clearLocalSession();
    }

    /**
     * 验证创建厂商时明文只加密一次，首行Key保存密文及正确尾四位
     */
    @Test
    void shouldCreateProviderWithInitialEncryptedKey() {
        when(llmApiKeyCipher.encrypt("sk-test-abcd")).thenReturn("v1:encrypted");
        when(llmApiKeyCipher.last4("sk-test-abcd")).thenReturn("abcd");
        when(providerDao.insertDB(any())).thenAnswer(invocation -> {
            Provider provider = invocation.getArgument(0);
            provider.setId(11L);
            return 1;
        });
        when(providerKeyDao.insertDB(any())).thenReturn(1);
        ProviderVO providerVO = new ProviderVO();
        providerVO.setName("provider");
        providerVO.setSecretKey("sk-test-abcd");
        ResultData<Void> result = providerService.createProvider(providerVO);
        ArgumentCaptor<ProviderKey> keyCaptor = ArgumentCaptor.forClass(ProviderKey.class);
        verify(providerKeyDao).insertDB(keyCaptor.capture());
        assertEquals("v1:encrypted", keyCaptor.getValue().getSecretKey());
        assertEquals(11L, keyCaptor.getValue().getProviderId());
        assertEquals("abcd", keyCaptor.getValue().getKeyLast4());
        assertEquals(ResultData.OK, result.getCode());
        verify(llmApiKeyCipher).encrypt("sk-test-abcd");
    }

    /**
     * 验证创建厂商时密文超过数据库长度会返回无效参数，厂商和首行Key均不写入
     */
    @Test
    void shouldRejectOverlongCiphertextOnProviderCreate() {
        when(llmApiKeyCipher.encrypt(any())).thenReturn("v1:" + "a".repeat(510));
        ProviderVO providerVO = new ProviderVO();
        providerVO.setName("provider");
        providerVO.setSecretKey("密".repeat(256));
        ResultData<Void> result = providerService.createProvider(providerVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), result.getCode());
        verify(providerDao, never()).insertDB(any());
        verify(providerKeyDao, never()).insertDB(any());
    }

    /**
     * 验证厂商更新提交新Key或空白Key都直接拒绝，不写库也不清理缓存，密钥轮换统一走Key管理接口
     */
    @Test
    void shouldRejectSecretOnProviderUpdate() {
        ProviderVO providerVO = new ProviderVO();
        providerVO.setId(11L);
        providerVO.setSecretKey("sk-new-wxyz");
        ResultData<Void> result = providerService.updateProvider(providerVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), result.getCode());
        providerVO.setSecretKey(" ");
        ResultData<Void> blankResult = providerService.updateProvider(providerVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), blankResult.getCode());
        verify(providerDao, never()).updateDBById(any());
        verify(modelFactory, never()).clearModelCacheByProvider(any());
        verify(agentFactory, never()).clearAllAgentCache();
    }
}
