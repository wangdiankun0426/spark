package com.spark.test.llm;

import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.model.credential.ProviderKeyModel;
import com.spark.llm.model.credential.ProviderKeyRoute;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.model.factory.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 16:30:00
 * 模型凭据客户端缓存测试
 */
public class ModelFactoryCacheTest {

    /**
     * 验证数据库模型包含上下文窗口时加载到运行时元数据，避免手工映射丢失该配置
     */
    @Test
    void shouldLoadContextLengthIntoRuntimeModel() {
        ModelFactory modelFactory = new ModelFactory();
        ModelDao modelDao = mock(ModelDao.class);
        ReflectionTestUtils.setField(modelFactory, "modelDao", modelDao);
        ModelResult modelResult = new ModelResult();
        modelResult.setId(1L);
        modelResult.setContextLength(128000);
        when(modelDao.queryModel(any())).thenReturn(modelResult);
        Model model = ReflectionTestUtils.invokeMethod(modelFactory, "loadModel", 1L);
        assertEquals(128000, model.getContextLength());
    }

    /**
     * 验证同步Chat同模型同Key密文不变时复用客户端，密文变化后立即创建新客户端
     */
    @Test
    void shouldReuseAndRebuildChatClientByCiphertextVersion() {
        ModelFactory modelFactory = new ModelFactory();
        ProviderKeyRouter router = mock(ProviderKeyRouter.class);
        ReflectionTestUtils.setField(modelFactory, "providerKeyRouter", router);
        ProviderKeyRoute routeA = new ProviderKeyRoute(21L, 10L, "v1:cipher-a", "plain-a");
        ProviderKeyRoute routeB = new ProviderKeyRoute(21L, 10L, "v1:cipher-b", "plain-b");
        when(router.select(10L)).thenReturn(routeA, routeA, routeB);
        Model model = this.createModel();
        Provider provider = this.createProvider();
        Duration timeout = Duration.ofSeconds(1);
        ProviderKeyModel<ChatModel> first = ReflectionTestUtils.invokeMethod(
                modelFactory, "selectChatModel", model, provider, timeout);
        ProviderKeyModel<ChatModel> second = ReflectionTestUtils.invokeMethod(
                modelFactory, "selectChatModel", model, provider, timeout);
        ProviderKeyModel<ChatModel> third = ReflectionTestUtils.invokeMethod(
                modelFactory, "selectChatModel", model, provider, timeout);
        assertSame(first.getDelegate(), second.getDelegate());
        assertNotSame(second.getDelegate(), third.getDelegate());
    }

    /**
     * 验证流式Chat同模型同Key密文不变时复用客户端，密文变化后不会继续使用旧密钥客户端
     */
    @Test
    void shouldReuseAndRebuildStreamingClientByCiphertextVersion() {
        ModelFactory modelFactory = new ModelFactory();
        ProviderKeyRouter router = mock(ProviderKeyRouter.class);
        ReflectionTestUtils.setField(modelFactory, "providerKeyRouter", router);
        ProviderKeyRoute routeA = new ProviderKeyRoute(21L, 10L, "v1:cipher-a", "plain-a");
        ProviderKeyRoute routeB = new ProviderKeyRoute(21L, 10L, "v1:cipher-b", "plain-b");
        when(router.select(10L)).thenReturn(routeA, routeA, routeB);
        Model model = this.createModel();
        Provider provider = this.createProvider();
        Duration timeout = Duration.ofSeconds(1);
        ProviderKeyModel<StreamingChatModel> first = ReflectionTestUtils.invokeMethod(
                modelFactory, "selectStreamingChatModel", model, provider, timeout);
        ProviderKeyModel<StreamingChatModel> second = ReflectionTestUtils.invokeMethod(
                modelFactory, "selectStreamingChatModel", model, provider, timeout);
        ProviderKeyModel<StreamingChatModel> third = ReflectionTestUtils.invokeMethod(
                modelFactory, "selectStreamingChatModel", model, provider, timeout);
        assertSame(first.getDelegate(), second.getDelegate());
        assertNotSame(second.getDelegate(), third.getDelegate());
    }

    /**
     * 构造缓存测试模型配置
     */
    private Model createModel() {
        Model model = new Model();
        model.setId(1L);
        model.setProviderId(10L);
        model.setName("test-model");
        return model;
    }

    /**
     * 构造缓存测试厂商配置
     */
    private Provider createProvider() {
        Provider provider = new Provider();
        provider.setId(10L);
        provider.setName("test-provider");
        provider.setApiUrl("http://127.0.0.1:9000");
        return provider;
    }
}
