package com.spark.test.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Mcp;
import com.spark.common.bean.llm.query.McpQuery;
import com.spark.common.bean.llm.result.McpResult;
import com.spark.common.bean.llm.vo.McpVO;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.McpTransportTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.config.aspectj.annotation.LogIgnore;
import com.spark.dao.llm.McpDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.mcp.McpClientManager;
import com.spark.llm.security.LlmApiKeyCipher;
import com.spark.llm.service.impl.McpServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * @since 2026-11-18 16:00:00
 * MCP独立密钥服务测试
 */
public class McpServiceImplTest {
    private McpServiceImpl mcpService;
    private McpDao mcpDao;
    private McpClientManager mcpClientManager;
    private AgentFactory agentFactory;
    private LlmApiKeyCipher llmApiKeyCipher;

    @BeforeEach
    void setUp() {
        mcpService = new McpServiceImpl();
        mcpDao = mock(McpDao.class);
        mcpClientManager = mock(McpClientManager.class);
        agentFactory = mock(AgentFactory.class);
        llmApiKeyCipher = mock(LlmApiKeyCipher.class);
        ReflectionTestUtils.setField(mcpService, "mcpDao", mcpDao);
        ReflectionTestUtils.setField(mcpService, "mcpClientManager", mcpClientManager);
        ReflectionTestUtils.setField(mcpService, "agentFactory", agentFactory);
        ReflectionTestUtils.setField(mcpService, "llmApiKeyCipher", llmApiKeyCipher);
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
     * 验证创建MCP时独立明文密钥只加密一次，数据库保存密文且默认状态为启用
     */
    @Test
    void shouldCreateMcpWithEncryptedSecret() {
        when(llmApiKeyCipher.encrypt("mcp-secret-abcd")).thenReturn("v1:mcp-encrypted");
        when(mcpDao.insertDB(any(Mcp.class))).thenAnswer(invocation -> {
            Mcp mcp = invocation.getArgument(0);
            mcp.setId(21L);
            return 1;
        });
        McpVO mcpVO = this.createValidMcpVO();
        ResultData<Void> result = mcpService.createMcp(mcpVO);
        ArgumentCaptor<Mcp> mcpCaptor = ArgumentCaptor.forClass(Mcp.class);
        verify(mcpDao).insertDB(mcpCaptor.capture());
        assertEquals("v1:mcp-encrypted", mcpCaptor.getValue().getSecretKey());
        assertEquals(StatusEnum.NORMAL.getValue(), mcpCaptor.getValue().getStatus());
        assertEquals(ResultData.OK, result.getCode());
        verify(llmApiKeyCipher).encrypt("mcp-secret-abcd");
    }

    /**
     * 验证创建MCP缺少密钥或密钥超过256字符时返回无效参数，不加密也不写数据库
     */
    @Test
    void shouldRejectMissingOrTooLongSecretOnCreate() {
        McpVO missingSecret = this.createValidMcpVO();
        missingSecret.setSecretKey(" ");
        ResultData<Void> missingResult = mcpService.createMcp(missingSecret);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), missingResult.getCode());
        McpVO longSecret = this.createValidMcpVO();
        longSecret.setSecretKey("a".repeat(257));
        ResultData<Void> longResult = mcpService.createMcp(longSecret);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), longResult.getCode());
        verify(llmApiKeyCipher, never()).encrypt(any());
        verify(mcpDao, never()).insertDB(any());
    }

    /**
     * 验证多字节明文加密后超过512字符时创建返回无效参数，密文不会写库或留下不可解密数据
     */
    @Test
    void shouldRejectCiphertextLongerThanDatabaseColumn() {
        when(llmApiKeyCipher.encrypt(any())).thenReturn("v1:" + "a".repeat(510));
        McpVO mcpVO = this.createValidMcpVO();
        mcpVO.setSecretKey("密".repeat(256));
        ResultData<Void> result = mcpService.createMcp(mcpVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), result.getCode());
        verify(mcpDao, never()).insertDB(any());
    }

    /**
     * 验证历史空密钥MCP编辑时仍未提交新密钥会返回无效参数，不允许保存成功后继续缺少凭据
     */
    @Test
    void shouldRequireSecretWhenExistingMcpSecretIsNull() {
        when(mcpDao.queryMcpConfig(any(McpQuery.class))).thenReturn(new Mcp());
        McpVO mcpVO = new McpVO();
        mcpVO.setId(21L);
        mcpVO.setSecretKey(" ");
        ResultData<Void> result = mcpService.updateMcp(mcpVO);
        assertEquals(ErrorCodeEnum.INVALID_PARAM.getValue(), result.getCode());
        verify(mcpDao, never()).updateDBById(any());
        verify(mcpClientManager, never()).disconnect(any());
        verify(agentFactory, never()).clearAllAgentCache();
    }

    /**
     * 验证编辑MCP留空密钥时不重新加密且不覆盖旧值，成功后断开客户端并清理Agent缓存
     */
    @Test
    void shouldKeepSecretAndClearCachesWhenUpdateKeyIsBlank() {
        Mcp existMcp = new Mcp();
        existMcp.setSecretKey("v1:existing");
        when(mcpDao.queryMcpConfig(any(McpQuery.class))).thenReturn(existMcp);
        when(mcpDao.updateDBById(any(Mcp.class))).thenReturn(1);
        McpVO mcpVO = new McpVO();
        mcpVO.setId(21L);
        mcpVO.setSecretKey(" ");
        ResultData<Void> result = mcpService.updateMcp(mcpVO);
        ArgumentCaptor<Mcp> mcpCaptor = ArgumentCaptor.forClass(Mcp.class);
        verify(mcpDao).updateDBById(mcpCaptor.capture());
        assertNull(mcpCaptor.getValue().getSecretKey());
        assertEquals(ResultData.OK, result.getCode());
        verify(llmApiKeyCipher, never()).encrypt(any());
        verify(mcpClientManager).disconnect(21L);
        verify(agentFactory).clearAllAgentCache();
    }

    /**
     * 验证编辑MCP提交新密钥时保存新密文，并按新配置重建后续客户端和Agent工具
     */
    @Test
    void shouldReplaceSecretAndClearCachesOnUpdate() {
        when(mcpDao.queryMcpConfig(any(McpQuery.class))).thenReturn(new Mcp());
        when(mcpDao.updateDBById(any(Mcp.class))).thenReturn(1);
        when(llmApiKeyCipher.encrypt("new-mcp-secret")).thenReturn("v1:new-mcp-encrypted");
        McpVO mcpVO = new McpVO();
        mcpVO.setId(21L);
        mcpVO.setSecretKey("new-mcp-secret");
        ResultData<Void> result = mcpService.updateMcp(mcpVO);
        ArgumentCaptor<Mcp> mcpCaptor = ArgumentCaptor.forClass(Mcp.class);
        verify(mcpDao).updateDBById(mcpCaptor.capture());
        assertEquals("v1:new-mcp-encrypted", mcpCaptor.getValue().getSecretKey());
        assertEquals(ResultData.OK, result.getCode());
        verify(mcpClientManager).disconnect(21L);
        verify(agentFactory).clearAllAgentCache();
    }

    /**
     * 验证删除MCP成功后同时断开客户端并清理Agent缓存，避免已装配工具继续引用旧配置
     */
    @Test
    void shouldDisconnectAndClearAgentCacheOnDelete() {
        when(mcpDao.queryMcp(any(McpQuery.class))).thenReturn(new McpResult());
        when(mcpDao.deleteDBById(any(Mcp.class))).thenReturn(1);
        McpVO mcpVO = new McpVO();
        mcpVO.setId(21L);
        ResultData<Void> result = mcpService.deleteMcp(mcpVO);
        assertEquals(ResultData.OK, result.getCode());
        verify(mcpClientManager).disconnect(21L);
        verify(agentFactory).clearAllAgentCache();
    }

    /**
     * 验证包含明文密钥的创建和更新方法均跳过LogPrint，避免fastjson2将入参密钥写入日志
     */
    @Test
    void shouldIgnoreSensitiveRequestLogs() throws NoSuchMethodException {
        assertTrue(McpServiceImpl.class.getMethod("createMcp", McpVO.class).isAnnotationPresent(LogIgnore.class));
        assertTrue(McpServiceImpl.class.getMethod("updateMcp", McpVO.class).isAnnotationPresent(LogIgnore.class));
    }

    /**
     * 构造合法的新增MCP参数
     */
    private McpVO createValidMcpVO() {
        McpVO mcpVO = new McpVO();
        mcpVO.setName("test-mcp");
        mcpVO.setTransport(McpTransportTypeEnum.HTTP.getValue());
        mcpVO.setUrl("http://127.0.0.1:9001/mcp");
        mcpVO.setSecretKey("mcp-secret-abcd");
        return mcpVO;
    }
}
