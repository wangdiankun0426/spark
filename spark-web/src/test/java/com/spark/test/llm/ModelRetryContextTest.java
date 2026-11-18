package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.llm.usage.ModelRetryContext;
import com.spark.llm.usage.ModelUsageChatListener;
import com.spark.llm.usage.ModelUsageRecorder;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 09:00:00
 * 模型重试上下文传递测试
 */
public class ModelRetryContextTest {

    /**
     * 验证嵌套模型调用解绑内层计数后恢复外层上下文，全部结束后ThreadLocal被清理
     */
    @Test
    void shouldRestoreNestedContext() {
        ModelRetryContext outer = new ModelRetryContext();
        ModelRetryContext inner = new ModelRetryContext();
        ModelRetryContext.bind(outer);
        ModelRetryContext.bind(inner);
        assertSame(inner, ModelRetryContext.current());
        ModelRetryContext.unbind(inner);
        assertSame(outer, ModelRetryContext.current());
        ModelRetryContext.unbind(outer);
        assertNull(ModelRetryContext.current());
    }

    /**
     * 验证Chat监听器通过attributes跨线程读取最终重试次数，不依赖回调线程ThreadLocal
     */
    @Test
    void shouldReadRetryCountFromListenerAttributes() {
        ModelUsageRecorder recorder = mock(ModelUsageRecorder.class);
        ModelInfo modelInfo = new ModelInfo(1L, "chat", 2L, 1);
        ModelUsageChatListener listener = new ModelUsageChatListener(recorder, modelInfo);
        Map<Object, Object> attributes = new HashMap<>();
        ChatModelRequestContext requestContext = mock(ChatModelRequestContext.class);
        when(requestContext.attributes()).thenReturn(attributes);
        listener.onRequest(requestContext);
        ModelRetryContext retryContext = ModelRetryContext.current();
        retryContext.incrementRetryCount();
        ModelRetryContext.unbind(retryContext);
        ChatModelResponseContext responseContext = mock(ChatModelResponseContext.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        when(responseContext.attributes()).thenReturn(attributes);
        when(responseContext.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.tokenUsage()).thenReturn(null);
        listener.onResponse(responseContext);
        verify(recorder).record(eq(modelInfo), any(), isNull(), isNull(), isNull(), eq(1));
        assertNull(ModelRetryContext.current());
    }
}
