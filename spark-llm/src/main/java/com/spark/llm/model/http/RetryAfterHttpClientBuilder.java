package com.spark.llm.model.http;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpClientBuilderLoader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 18:00:00
 * 为重试等待时间装饰器包装JDK HTTP客户端构建器
 */
public class RetryAfterHttpClientBuilder implements HttpClient.Builder {
    /**
     * 被装饰的JDK HTTP客户端构建器
     */
    private final HttpClient.Builder delegate;

    public RetryAfterHttpClientBuilder(HttpClient.Builder delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    /**
     * 创建保留重试等待时间的LangChain4j HTTP构建器
     * @return LangChain4j HTTP构建器
     */
    public static HttpClientBuilder createHttpClientBuilder() {
        HttpClientBuilder builder = HttpClientBuilderLoader.loadHttpClientBuilder();
        HttpClient.Builder jdkBuilder = HttpClient.newBuilder();
        RetryAfterHttpClientBuilder retryAfterBuilder = new RetryAfterHttpClientBuilder(jdkBuilder);
        try {
            Method method = builder.getClass().getMethod("httpClientBuilder", HttpClient.Builder.class);
            method.invoke(builder, retryAfterBuilder);
            return builder;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("jdk http client builder does not support retry after", e);
        }
    }

    @Override
    public HttpClient.Builder cookieHandler(CookieHandler cookieHandler) {
        delegate.cookieHandler(cookieHandler);
        return this;
    }

    @Override
    public HttpClient.Builder connectTimeout(Duration duration) {
        delegate.connectTimeout(duration);
        return this;
    }

    @Override
    public HttpClient.Builder sslContext(SSLContext sslContext) {
        delegate.sslContext(sslContext);
        return this;
    }

    @Override
    public HttpClient.Builder sslParameters(SSLParameters sslParameters) {
        delegate.sslParameters(sslParameters);
        return this;
    }

    @Override
    public HttpClient.Builder executor(Executor executor) {
        delegate.executor(executor);
        return this;
    }

    @Override
    public HttpClient.Builder followRedirects(HttpClient.Redirect policy) {
        delegate.followRedirects(policy);
        return this;
    }

    @Override
    public HttpClient.Builder version(HttpClient.Version version) {
        delegate.version(version);
        return this;
    }

    @Override
    public HttpClient.Builder priority(int priority) {
        delegate.priority(priority);
        return this;
    }

    @Override
    public HttpClient.Builder proxy(ProxySelector proxySelector) {
        delegate.proxy(proxySelector);
        return this;
    }

    @Override
    public HttpClient.Builder authenticator(Authenticator authenticator) {
        delegate.authenticator(authenticator);
        return this;
    }

    @Override
    public HttpClient.Builder localAddress(InetAddress localAddr) {
        delegate.localAddress(localAddr);
        return this;
    }

    @Override
    public HttpClient build() {
        HttpClient httpClient = delegate.build();
        return new RetryAfterHttpClient(httpClient);
    }
}
