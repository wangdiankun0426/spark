package com.spark.test.chat;

import com.spark.chat.service.impl.ChatMsgServiceImpl;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.ModelInvocationException;
import com.spark.llm.model.ModelTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
