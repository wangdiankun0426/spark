package com.spark.test.llm;

import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.common.llm.ModelUsageSceneHolder;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.workflow.engine.executor.LlmTaskExecutor;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 18:15:00
 * Workflow大模型节点异常传播测试
 */
public class LlmTaskExecutorTest {

    /**
     * 验证Workflow大模型调用返回429时向外抛出标准限流异常，不再返回空文本成功且场景栈正常恢复
     */
    @Test
    void shouldPropagateClassifiedModelError() {
        LlmTaskExecutor executor = new LlmTaskExecutor();
        ModelFactory modelFactory = mock(ModelFactory.class);
        ChatModel chatModel = mock(ChatModel.class);
        ReflectionTestUtils.setField(executor, "modelFactory", modelFactory);
        when(modelFactory.getChatModel(1L)).thenReturn(chatModel);
        when(chatModel.chat(anyString())).thenThrow(new HttpException(429, "limited"));
        Map<String, Object> config = new HashMap<>();
        config.put("modelId", 1L);
        config.put("prompt", "请回答问题");
        ModelUsageSceneEnum originalScene = ModelUsageSceneHolder.current();
        ModelInvocationException exception = assertThrows(ModelInvocationException.class,
                () -> executor.execute("node1", config, Map.of(), Map.of(), Map.of()));
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertEquals(originalScene, ModelUsageSceneHolder.current());
    }
}
