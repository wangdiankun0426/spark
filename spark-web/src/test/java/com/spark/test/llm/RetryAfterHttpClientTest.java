package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.custom.RerankModel;
import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.factory.ModelFactory;
import com.spark.llm.model.http.RetryAfterHttpClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 18:20:00
 * 供应商重试等待响应头传输测试
 */
public class RetryAfterHttpClientTest {
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    /**
     * 验证Retry-After支持秒数和HTTP日期，过去日期归零，非法、负数和溢出值回退为空
     */
    @Test
    void shouldParseRetryAfterSecondsAndHttpDate() {
        Instant now = Instant.parse("2026-11-18T08:49:37Z");
        assertEquals(2000L, RetryAfterHttpClient.parseRetryAfterMs(" 2 ", now));
        assertEquals(0L, RetryAfterHttpClient.parseRetryAfterMs("0", now));
        String futureDate = DateTimeFormatter.RFC_1123_DATE_TIME.format(now.plusSeconds(3).atZone(ZoneOffset.UTC));
        assertEquals(3000L, RetryAfterHttpClient.parseRetryAfterMs(futureDate, now));
        String pastDate = DateTimeFormatter.RFC_1123_DATE_TIME.format(now.minusSeconds(3).atZone(ZoneOffset.UTC));
        assertEquals(0L, RetryAfterHttpClient.parseRetryAfterMs(pastDate, now));
        assertNull(RetryAfterHttpClient.parseRetryAfterMs(null, now));
        assertNull(RetryAfterHttpClient.parseRetryAfterMs(" ", now));
        assertNull(RetryAfterHttpClient.parseRetryAfterMs("-1", now));
        assertNull(RetryAfterHttpClient.parseRetryAfterMs("1.5", now));
        assertNull(RetryAfterHttpClient.parseRetryAfterMs("invalid", now));
        assertNull(RetryAfterHttpClient.parseRetryAfterMs("99999999999999999999", now));
    }

    /**
     * 验证成功响应即使携带Retry-After也在同步和异步入口原样放行，状态和响应体不受影响
     */
    @Test
    void shouldPassThroughSuccessResponses() throws Exception {
        server.createContext("/success", exchange -> this.respond(exchange, 200, "ok", "10"));
        try (RetryAfterHttpClient client = new RetryAfterHttpClient(HttpClient.newHttpClient())) {
            URI uri = URI.create(this.baseUrl("/success"));
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> syncResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, syncResponse.statusCode());
            assertEquals("ok", syncResponse.body());
            HttpResponse<String> asyncResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
            assertEquals(200, asyncResponse.statusCode());
            assertEquals("ok", asyncResponse.body());
        }
    }

    /**
     * 验证真实同步OpenAI客户端收到429和Retry-After时，统一异常保留状态、响应体和2000毫秒等待时间
     */
    @Test
    void shouldPreserveRetryAfterFromRealSyncOpenAiClient() {
        server.createContext("/v1/chat/completions", exchange -> this.respond(exchange, 429, "limited", "2"));
        ChatModel chatModel = this.createRawChatModel(this.baseUrl("/v1"));
        RuntimeException error = assertThrows(RuntimeException.class, () -> chatModel.chat("hello"));
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertEquals(2000L, exception.getRetryAfterMs());
        assertEquals("limited", exception.getMessage());
    }

    /**
     * 验证真实同步OpenAI客户端收到无Retry-After的429时保持原分类和响应体，等待元数据为空
     */
    @Test
    void shouldKeepOriginalErrorWhenRetryAfterIsMissing() {
        server.createContext("/v1/chat/completions", exchange -> this.respond(exchange, 429, "limited", null));
        ChatModel chatModel = this.createRawChatModel(this.baseUrl("/v1"));
        RuntimeException error = assertThrows(RuntimeException.class, () -> chatModel.chat("hello"));
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertNull(exception.getRetryAfterMs());
        assertEquals("limited", exception.getMessage());
    }

    /**
     * 验证真实流式OpenAI客户端异步收到429和Retry-After时，错误回调异常链仍保留3000毫秒等待时间
     */
    @Test
    void shouldPreserveRetryAfterFromRealStreamingOpenAiClient() {
        server.createContext("/v1/chat/completions", exchange -> this.respond(exchange, 429, "limited", "3"));
        StreamingChatModel streamingChatModel = this.createRawStreamingChatModel(this.baseUrl("/v1"));
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        streamingChatModel.chat("hello", handler);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, timeout(3000)).onError(errorCaptor.capture());
        ModelInvocationException exception = ModelErrorClassifier.wrap(errorCaptor.getValue());
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertEquals(3000L, exception.getRetryAfterMs());
        assertEquals("limited", exception.getMessage());
    }

    /**
     * 验证真实流式OpenAI客户端收到无Retry-After的429时仍走原错误回调，保留响应体且等待元数据为空
     */
    @Test
    void shouldKeepOriginalStreamingErrorWhenRetryAfterIsMissing() {
        server.createContext("/v1/chat/completions", exchange -> this.respond(exchange, 429, "limited", null));
        StreamingChatModel streamingChatModel = this.createRawStreamingChatModel(this.baseUrl("/v1"));
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        streamingChatModel.chat("hello", handler);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, timeout(3000)).onError(errorCaptor.capture());
        ModelInvocationException exception = ModelErrorClassifier.wrap(errorCaptor.getValue());
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertNull(exception.getRetryAfterMs());
        assertEquals("limited", exception.getMessage());
    }

    /**
     * 验证真实Rerank响应返回429和Retry-After时直接保留4000毫秒等待时间，统一分类仍可重试
     */
    @Test
    void shouldPreserveRetryAfterFromRealRerankClient() {
        server.createContext("/rerank", exchange -> this.respond(exchange, 429, "limited", "4"));
        ModelInfo modelInfo = new ModelInfo(1L, "rerank", 2L, 3);
        RerankModel rerankModel = new RerankModel(this.baseUrl(""), "test-key", modelInfo, Duration.ofSeconds(5));
        List<TextSegment> segments = List.of(TextSegment.from("document"));
        RuntimeException error = assertThrows(RuntimeException.class, () -> rerankModel.scoreAll(segments, "query"));
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertEquals(4000L, exception.getRetryAfterMs());
    }

    /**
     * 创建使用本地服务的真实同步客户端
     * @param apiUrl 服务地址
     * @return 同步聊天模型
     */
    private ChatModel createRawChatModel(String apiUrl) {
        ModelFactory modelFactory = new ModelFactory();
        Model model = this.createModel();
        Provider provider = this.createProvider(apiUrl);
        return ReflectionTestUtils.invokeMethod(
                modelFactory, "createRawOpenAiChatModel", model, provider, "test-key", Duration.ofSeconds(5));
    }

    /**
     * 创建使用本地服务的真实流式客户端
     * @param apiUrl 服务地址
     * @return 流式聊天模型
     */
    private StreamingChatModel createRawStreamingChatModel(String apiUrl) {
        ModelFactory modelFactory = new ModelFactory();
        Model model = this.createModel();
        Provider provider = this.createProvider(apiUrl);
        return ReflectionTestUtils.invokeMethod(
                modelFactory, "createRawOpenAiStreamingChatModel", model, provider, "test-key", Duration.ofSeconds(5));
    }

    /**
     * 构造真实客户端模型配置
     * @return 模型配置
     */
    private Model createModel() {
        Model model = new Model();
        model.setName("test-model");
        model.setEnableThinking(-1);
        return model;
    }

    /**
     * 构造真实客户端厂商配置
     * @param apiUrl 服务地址
     * @return 厂商配置
     */
    private Provider createProvider(String apiUrl) {
        Provider provider = new Provider();
        provider.setApiUrl(apiUrl);
        return provider;
    }

    /**
     * 获取本地服务地址
     * @param path 请求路径
     * @return 完整地址
     */
    private String baseUrl(String path) {
        int port = server.getAddress().getPort();
        return "http://127.0.0.1:" + port + path;
    }

    /**
     * 写入本地HTTP响应
     * @param exchange HTTP交换
     * @param status 状态码
     * @param body 响应体
     * @param retryAfter 重试等待响应头
     */
    private void respond(HttpExchange exchange, int status, String body, String retryAfter) throws IOException {
        byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
        if (retryAfter != null) {
            exchange.getResponseHeaders().add("Retry-After", retryAfter);
        }
        exchange.sendResponseHeaders(status, responseBody.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBody);
        } finally {
            exchange.close();
        }
    }
}
