package com.spark.llm.model.http;

import com.spark.llm.model.error.RetryAfterHttpException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 18:00:00
 * 保留失败响应重试等待时间的JDK HTTP客户端装饰器
 */
public class RetryAfterHttpClient extends HttpClient {
    /**
     * 被装饰的JDK HTTP客户端
     */
    private final HttpClient delegate;

    public RetryAfterHttpClient(HttpClient delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    /**
     * 解析供应商重试等待响应头
     * @param retryAfter 响应头值
     * @param now 响应接收时间
     * @return 等待毫秒，无法解析时返回null
     */
    public static Long parseRetryAfterMs(String retryAfter, Instant now) {
        if (retryAfter == null || retryAfter.isBlank()) {
            return null;
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        String value = retryAfter.trim();
        boolean delaySeconds = value.chars().allMatch(character -> character >= '0' && character <= '9');
        if (delaySeconds) {
            try {
                long seconds = Long.parseLong(value);
                return Math.multiplyExact(seconds, 1000L);
            } catch (NumberFormatException | ArithmeticException e) {
                return null;
            }
        }
        try {
            ZonedDateTime retryAt = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
            long retryAtMs = retryAt.toInstant().toEpochMilli();
            long delayMs = Math.subtractExact(retryAtMs, now.toEpochMilli());
            return Math.max(0L, delayMs);
        } catch (DateTimeParseException | ArithmeticException e) {
            return null;
        }
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return delegate.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return delegate.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return delegate.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return delegate.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return delegate.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return delegate.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return delegate.authenticator();
    }

    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public Optional<Executor> executor() {
        return delegate.executor();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        HttpResponse<T> response = delegate.send(request, responseBodyHandler);
        return this.inspectResponse(response);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            HttpResponse.BodyHandler<T> responseBodyHandler) {
        CompletableFuture<HttpResponse<T>> responseFuture = delegate.sendAsync(request, responseBodyHandler);
        return responseFuture.thenApply(this::inspectResponse);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            HttpResponse.BodyHandler<T> responseBodyHandler,
                                                            HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        CompletableFuture<HttpResponse<T>> responseFuture = delegate.sendAsync(
                request, responseBodyHandler, pushPromiseHandler);
        return responseFuture.thenApply(this::inspectResponse);
    }

    @Override
    public WebSocket.Builder newWebSocketBuilder() {
        return delegate.newWebSocketBuilder();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public boolean awaitTermination(Duration duration) throws InterruptedException {
        return delegate.awaitTermination(duration);
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public void shutdownNow() {
        delegate.shutdownNow();
    }

    @Override
    public void close() {
        delegate.close();
    }

    /**
     * 在LangChain4j读取失败响应前保留重试等待时间
     * @param response JDK响应
     * @return 无需特殊处理的原始响应
     * @param <T> 响应体类型
     */
    private <T> HttpResponse<T> inspectResponse(HttpResponse<T> response) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return response;
        }
        String retryAfter = response.headers().firstValue("Retry-After").orElse(null);
        Long retryAfterMs = parseRetryAfterMs(retryAfter, Instant.now());
        if (retryAfterMs == null) {
            return response;
        }
        Object body = response.body();
        String message = body instanceof String ? (String) body : null;
        IOException readError = null;
        if (body instanceof InputStream inputStream) {
            try {
                byte[] responseBody = inputStream.readAllBytes();
                message = new String(responseBody, StandardCharsets.UTF_8);
            } catch (IOException e) {
                readError = e;
            }
        }
        RetryAfterHttpException exception = new RetryAfterHttpException(statusCode, message, retryAfterMs);
        if (readError != null) {
            exception.addSuppressed(readError);
        }
        if (body instanceof InputStream inputStream) {
            try {
                inputStream.close();
            } catch (IOException closeError) {
                exception.addSuppressed(closeError);
            }
        }
        throw exception;
    }
}
