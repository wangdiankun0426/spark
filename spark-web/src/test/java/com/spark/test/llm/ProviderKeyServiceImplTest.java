package com.spark.test.llm;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.bean.llm.query.ProviderKeyQuery;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderKeyResult;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.common.bean.llm.vo.ProviderKeyVO;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.dao.llm.ProviderDao;
import com.spark.dao.llm.ProviderKeyDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.security.LlmApiKeyCipher;
import com.spark.llm.service.impl.ProviderKeyServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * @since 2026-11-18 15:30:00
 * 模型厂商密钥服务测试
 */
public class ProviderKeyServiceImplTest {
    private ProviderKeyServiceImpl providerKeyService;
    private ProviderKeyDao providerKeyDao;
    private ProviderDao providerDao;
    private LlmApiKeyCipher llmApiKeyCipher;
    private ModelFactory modelFactory;
    private AgentFactory agentFactory;

    @BeforeEach
    void setUp() {
        providerKeyService = new ProviderKeyServiceImpl();
        providerKeyDao = mock(ProviderKeyDao.class);
        providerDao = mock(ProviderDao.class);
        llmApiKeyCipher = mock(LlmApiKeyCipher.class);
        modelFactory = mock(ModelFactory.class);
        agentFactory = mock(AgentFactory.class);
        ReflectionTestUtils.setField(providerKeyService, "providerKeyDao", providerKeyDao);
        ReflectionTestUtils.setField(providerKeyService, "providerDao", providerDao);
        ReflectionTestUtils.setField(providerKeyService, "llmApiKeyCipher", llmApiKeyCipher);
        ReflectionTestUtils.setField(providerKeyService, "modelFactory", modelFactory);
        ReflectionTestUtils.setField(providerKeyService, "agentFactory", agentFactory);
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
     * 验证创建时先校验父厂商，再保存密文与末四位，成功后清理模型和智能体缓存并返回密钥id
     */
    @Test
    void shouldCreateEncryptedKeyAndClearCaches() {
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(new ProviderResult());
        when(llmApiKeyCipher.encrypt("sk-test-abcd")).thenReturn("v1:encrypted");
        when(llmApiKeyCipher.last4("sk-test-abcd")).thenReturn("abcd");
        when(providerKeyDao.insertDB(any(ProviderKey.class))).thenAnswer(invocation -> {
            ProviderKey providerKey = invocation.getArgument(0);
            providerKey.setId(901L);
            return 1;
        });
        ProviderKeyVO providerKeyVO = new ProviderKeyVO();
        providerKeyVO.setProviderId(11L);
        providerKeyVO.setSecretKey("sk-test-abcd");
        ResultData<Void> result = providerKeyService.createProviderKey(providerKeyVO);
        ArgumentCaptor<ProviderKey> keyCaptor = ArgumentCaptor.forClass(ProviderKey.class);
        verify(providerKeyDao).insertDB(keyCaptor.capture());
        ProviderKey savedKey = keyCaptor.getValue();
        assertEquals("v1:encrypted", savedKey.getSecretKey());
        assertEquals("abcd", savedKey.getKeyLast4());
        assertEquals(StatusEnum.NORMAL.getValue(), savedKey.getStatus());
        assertEquals(ResultData.OK, result.getCode());
        assertEquals(901L, result.getObjId());
        verify(modelFactory).clearModelCacheByProvider(11L);
        verify(agentFactory).clearAllAgentCache();
    }

    /**
     * 验证新建ProviderKey密文超过512字符时返回无效参数，不写库也不清理缓存
     */
    @Test
    void shouldRejectOverlongCiphertextOnCreate() {
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(new ProviderResult());
        when(llmApiKeyCipher.encrypt(any())).thenReturn("v1:" + "a".repeat(510));
        ProviderKeyVO providerKeyVO = new ProviderKeyVO();
        providerKeyVO.setProviderId(11L);
        providerKeyVO.setSecretKey("密".repeat(256));
        ResultData<Void> result = providerKeyService.createProviderKey(providerKeyVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), result.getCode());
        verify(providerKeyDao, never()).insertDB(any());
        verify(modelFactory, never()).clearModelCacheByProvider(any());
        verify(agentFactory, never()).clearAllAgentCache();
    }

    /**
     * 验证更新密钥为空白时保留原密钥，只修改状态，并按数据库真实厂商id清理缓存
     */
    @Test
    void shouldKeepSecretWhenUpdateKeyIsBlank() {
        ProviderKeyResult existingKey = new ProviderKeyResult();
        existingKey.setId(88L);
        existingKey.setProviderId(11L);
        when(providerKeyDao.queryProviderKey(any(ProviderKeyQuery.class))).thenReturn(existingKey);
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(new ProviderResult());
        when(providerKeyDao.updateDBById(any(ProviderKey.class))).thenReturn(1);
        ProviderKeyVO providerKeyVO = new ProviderKeyVO();
        providerKeyVO.setId(88L);
        providerKeyVO.setProviderId(99L);
        providerKeyVO.setSecretKey(" ");
        providerKeyVO.setStatus(StatusEnum.ABNORMAL.getValue());
        ResultData<Void> result = providerKeyService.updateProviderKey(providerKeyVO);
        ArgumentCaptor<ProviderKey> keyCaptor = ArgumentCaptor.forClass(ProviderKey.class);
        verify(providerKeyDao).updateDBById(keyCaptor.capture());
        ProviderKey updatedKey = keyCaptor.getValue();
        assertNull(updatedKey.getSecretKey());
        assertNull(updatedKey.getKeyLast4());
        assertEquals(StatusEnum.ABNORMAL.getValue(), updatedKey.getStatus());
        assertEquals(ResultData.OK, result.getCode());
        verify(llmApiKeyCipher, never()).encrypt(any());
        verify(modelFactory).clearModelCacheByProvider(11L);
        verify(agentFactory).clearAllAgentCache();
    }

    /**
     * 验证更新新密钥时重新生成密文与末四位，冷却时间保持空值供Mapper清空旧冷却
     */
    @Test
    void shouldReplaceSecretAndResetCooldown() {
        ProviderKeyResult existingKey = new ProviderKeyResult();
        existingKey.setId(88L);
        existingKey.setProviderId(11L);
        when(providerKeyDao.queryProviderKey(any(ProviderKeyQuery.class))).thenReturn(existingKey);
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(new ProviderResult());
        when(providerKeyDao.updateDBById(any(ProviderKey.class))).thenReturn(1);
        when(llmApiKeyCipher.encrypt("sk-new-wxyz")).thenReturn("v1:new-encrypted");
        when(llmApiKeyCipher.last4("sk-new-wxyz")).thenReturn("wxyz");
        ProviderKeyVO providerKeyVO = new ProviderKeyVO();
        providerKeyVO.setId(88L);
        providerKeyVO.setSecretKey("sk-new-wxyz");
        ResultData<Void> result = providerKeyService.updateProviderKey(providerKeyVO);
        ArgumentCaptor<ProviderKey> keyCaptor = ArgumentCaptor.forClass(ProviderKey.class);
        verify(providerKeyDao).updateDBById(keyCaptor.capture());
        ProviderKey updatedKey = keyCaptor.getValue();
        assertEquals("v1:new-encrypted", updatedKey.getSecretKey());
        assertEquals("wxyz", updatedKey.getKeyLast4());
        assertNull(updatedKey.getCooldownUntil());
        assertEquals(ResultData.OK, result.getCode());
    }

    /**
     * 验证普通租户用户修改他租户密钥时父厂商查询被强制限定当前租户，查询不到父厂商即拒绝写入
     */
    @Test
    void shouldRejectKeyOutsideCurrentTenant() {
        SessionHolder.setAccountType(AccountTypeEnum.COMMON.getValue());
        SessionHolder.setCurrentTenantId(103L);
        ProviderKeyResult existingKey = new ProviderKeyResult();
        existingKey.setId(88L);
        existingKey.setProviderId(11L);
        when(providerKeyDao.queryProviderKey(any(ProviderKeyQuery.class))).thenReturn(existingKey);
        when(providerDao.queryProvider(any(ProviderQuery.class))).thenReturn(null);
        ProviderKeyVO providerKeyVO = new ProviderKeyVO();
        providerKeyVO.setId(88L);
        ResultData<Void> result = providerKeyService.updateProviderKey(providerKeyVO);
        ArgumentCaptor<ProviderQuery> providerQueryCaptor = ArgumentCaptor.forClass(ProviderQuery.class);
        verify(providerDao).queryProvider(providerQueryCaptor.capture());
        assertEquals(103L, providerQueryCaptor.getValue().getTenantId());
        assertEquals(ErrorCodeEnum.PROVIDER_NOT_EXIST.getValue(), result.getCode());
        verify(providerKeyDao, never()).updateDBById(any(ProviderKey.class));
        verify(modelFactory, never()).clearModelCacheByProvider(any());
    }

    /**
     * 验证分页未传厂商id时直接返回无效参数，不触发父厂商、数量或列表查询
     */
    @Test
    void shouldRequireProviderIdForPage() {
        ProviderKeyQuery query = new ProviderKeyQuery();
        ResultData<PageResult<ProviderKeyResult>> result = providerKeyService.pageProviderKeyList(query);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), result.getCode());
        verify(providerDao, never()).queryProvider(any(ProviderQuery.class));
        verify(providerKeyDao, never()).queryProviderKeyCount(any(ProviderKeyQuery.class));
        verify(providerKeyDao, never()).queryProviderKeyList(any(ProviderKeyQuery.class));
    }

}
