package com.spark.test.chat;

import com.spark.chat.service.IChatSpaceService;
import com.spark.chat.service.impl.ChatMsgServiceImpl;
import com.spark.common.bean.chat.entity.ChatMsg;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatMsgVO;
import com.spark.common.bean.llm.result.AgentResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.dao.chat.ChatMsgDao;
import com.spark.dao.llm.AgentDao;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelTimeoutException;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.tools.SearchKnowledge;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
 * @since 2026-11-17 11:00:00
 * 聊天模型错误提示测试
 */
public class ChatMsgServiceImplTest {
    private static final String DEFAULT_MESSAGE = "抱歉，当前Agent对话异常！";

    /**
     * 验证六类已知模型错误即使被外层异常包装也返回各自差异化中文提示
     */
    @Test
    void shouldResolveKnownErrorMessages() {
        ChatMsgServiceImpl service = new ChatMsgServiceImpl();
        for (ModelErrorTypeEnum errorType : ModelErrorTypeEnum.values()) {
            if (errorType == ModelErrorTypeEnum.UNKNOWN) {
                continue;
            }
            ModelInvocationException error = new ModelInvocationException(
                    errorType, null, "technical error", null, errorType.getUserMessage());
            RuntimeException nestedError = new RuntimeException(error);
            String message = ReflectionTestUtils.invokeMethod(
                    service, "buildChatErrorMessage", nestedError, DEFAULT_MESSAGE);
            assertEquals(errorType.getUserMessage(), message);
        }
    }

    /**
     * 验证UNKNOWN、普通本地异常和空异常仍保留原Agent默认提示，不误报为供应商错误
     */
    @Test
    void shouldKeepDefaultMessageForUnknownError() {
        ChatMsgServiceImpl service = new ChatMsgServiceImpl();
        ModelInvocationException unknownError = new ModelInvocationException(ModelErrorTypeEnum.UNKNOWN,
                null, "technical error", null, ModelErrorTypeEnum.UNKNOWN.getUserMessage());
        String unknownMessage = ReflectionTestUtils.invokeMethod(
                service, "buildChatErrorMessage", unknownError, DEFAULT_MESSAGE);
        assertEquals(DEFAULT_MESSAGE, unknownMessage);
        RuntimeException error = new RuntimeException("local error");
        String localMessage = ReflectionTestUtils.invokeMethod(
                service, "buildChatErrorMessage", error, DEFAULT_MESSAGE);
        assertEquals(DEFAULT_MESSAGE, localMessage);
        String nullMessage = ReflectionTestUtils.invokeMethod(
                service, "buildChatErrorMessage", null, DEFAULT_MESSAGE);
        assertEquals(DEFAULT_MESSAGE, nullMessage);
    }

    /**
     * 验证流式空闲超时继续使用带实际秒数的动态提示，不被通用超时文案覆盖
     */
    @Test
    void shouldKeepDynamicTimeoutMessage() {
        ChatMsgServiceImpl service = new ChatMsgServiceImpl();
        ModelTimeoutException error = new ModelTimeoutException(1L, 60000L);
        String message = ReflectionTestUtils.invokeMethod(
                service, "buildChatErrorMessage", error, DEFAULT_MESSAGE);
        assertTrue(message.contains("60 秒"));
    }

    /**
     * 验证Agent构建阶段发生限流异常时不向WebSocket容器抛出，只落库和推送一次繁忙提示并清理引用
     */
    @Test
    void shouldHandleAgentBuildError() throws Exception {
        ChatMsgServiceImpl service = new ChatMsgServiceImpl();
        ChatMsgDao chatMsgDao = mock(ChatMsgDao.class);
        AgentFactory agentFactory = mock(AgentFactory.class);
        AgentDao agentDao = mock(AgentDao.class);
        SearchKnowledge searchKnowledge = mock(SearchKnowledge.class);
        IChatSpaceService chatSpaceService = mock(IChatSpaceService.class);
        ReflectionTestUtils.setField(service, "chatMsgDao", chatMsgDao);
        ReflectionTestUtils.setField(service, "agentFactory", agentFactory);
        ReflectionTestUtils.setField(service, "agentDao", agentDao);
        ReflectionTestUtils.setField(service, "searchKnowledge", searchKnowledge);
        ReflectionTestUtils.setField(service, "chatSpaceService", chatSpaceService);
        ChatMsgVO chatMsgVO = this.createChatMsgVO();
        ChatSpaceResult participant = new ChatSpaceResult();
        participant.setReceiverId(30L);
        when(chatSpaceService.queryParticipantSpace(10L, 20L)).thenReturn(participant);
        AgentResult agentResult = new AgentResult();
        agentResult.setChatModelId(40L);
        when(agentDao.queryAgent(any())).thenReturn(agentResult);
        ModelInvocationException error = new ModelInvocationException(ModelErrorTypeEnum.RATE_LIMIT,
                429, "limited", null, ModelErrorTypeEnum.RATE_LIMIT.getUserMessage());
        when(agentFactory.buildForRequest(any(), any())).thenThrow(error);
        when(chatMsgDao.insertDB(any(ChatMsg.class))).thenReturn(1);
        Session webSocketSession = mock(Session.class);
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(webSocketSession.isOpen()).thenReturn(true);
        when(webSocketSession.getBasicRemote()).thenReturn(basicRemote);
        assertDoesNotThrow(() -> service.createAgentChatMsg(chatMsgVO, List.of(webSocketSession)));
        ArgumentCaptor<ChatMsg> messageCaptor = ArgumentCaptor.forClass(ChatMsg.class);
        verify(chatMsgDao).insertDB(messageCaptor.capture());
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT.getUserMessage(), messageCaptor.getValue().getMessage());
        verify(searchKnowledge).clearReferences(10L);
        verify(basicRemote).sendText(anyString());
    }

    /**
     * 验证普通模型构建阶段发生认证异常时不进入流式调用，只落库和推送一次认证提示且不清理Agent引用
     */
    @Test
    void shouldHandleModelBuildError() throws Exception {
        ChatMsgServiceImpl service = new ChatMsgServiceImpl();
        ChatMsgDao chatMsgDao = mock(ChatMsgDao.class);
        ModelFactory modelFactory = mock(ModelFactory.class);
        ModelDao modelDao = mock(ModelDao.class);
        SearchKnowledge searchKnowledge = mock(SearchKnowledge.class);
        IChatSpaceService chatSpaceService = mock(IChatSpaceService.class);
        ReflectionTestUtils.setField(service, "chatMsgDao", chatMsgDao);
        ReflectionTestUtils.setField(service, "modelFactory", modelFactory);
        ReflectionTestUtils.setField(service, "modelDao", modelDao);
        ReflectionTestUtils.setField(service, "searchKnowledge", searchKnowledge);
        ReflectionTestUtils.setField(service, "chatSpaceService", chatSpaceService);
        ChatMsgVO chatMsgVO = this.createChatMsgVO();
        ChatSpaceResult participant = new ChatSpaceResult();
        participant.setReceiverId(30L);
        when(chatSpaceService.queryParticipantSpace(10L, 20L)).thenReturn(participant);
        ModelResult modelResult = new ModelResult();
        modelResult.setId(30L);
        when(modelDao.queryModel(any())).thenReturn(modelResult);
        ModelInvocationException error = new ModelInvocationException(ModelErrorTypeEnum.AUTH,
                401, "unauthorized", null, ModelErrorTypeEnum.AUTH.getUserMessage());
        when(modelFactory.getStreamingChatModel(30L)).thenThrow(error);
        when(chatMsgDao.insertDB(any(ChatMsg.class))).thenReturn(1);
        Session webSocketSession = mock(Session.class);
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(webSocketSession.isOpen()).thenReturn(true);
        when(webSocketSession.getBasicRemote()).thenReturn(basicRemote);
        assertDoesNotThrow(() -> service.createModelChatMsg(chatMsgVO, List.of(webSocketSession)));
        ArgumentCaptor<ChatMsg> messageCaptor = ArgumentCaptor.forClass(ChatMsg.class);
        verify(chatMsgDao).insertDB(messageCaptor.capture());
        assertEquals(ModelErrorTypeEnum.AUTH.getUserMessage(), messageCaptor.getValue().getMessage());
        verify(searchKnowledge, never()).clearReferences(any());
        verify(basicRemote).sendText(anyString());
    }

    /**
     * 构造构建异常测试消息
     * @return 聊天消息参数
     */
    private ChatMsgVO createChatMsgVO() {
        ChatMsgVO chatMsgVO = new ChatMsgVO();
        chatMsgVO.setSpaceId(10L);
        chatMsgVO.setSenderId(20L);
        chatMsgVO.setReceiverId(30L);
        chatMsgVO.setMessage("test question");
        return chatMsgVO;
    }
}
