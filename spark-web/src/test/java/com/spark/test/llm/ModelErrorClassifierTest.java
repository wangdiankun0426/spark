package com.spark.test.llm;

import com.fasterxml.jackson.core.JsonParseException;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelTimeoutException;
import com.spark.llm.model.error.RetryAfterHttpException;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 11:00:00
 * 模型错误分类器测试
 */
public class ModelErrorClassifierTest {

    /**
     * 验证429及其LangChain4j包装异常归类为RATE_LIMIT，保留状态并允许重试且提示繁忙
     */
    @Test
    void shouldClassifyRateLimit() {
        HttpException httpException = new HttpException(429, "limited");
        RateLimitException rateLimitException = new RateLimitException(httpException);
        RuntimeException error = new RuntimeException(rateLimitException);
        ModelInvocationException exception = this.assertError(error, ModelErrorTypeEnum.RATE_LIMIT, 429, true);
        assertTrue(exception.getUserMessage().contains("繁忙"));
    }

    /**
     * 验证403认证失败归类为AUTH，保留HTTP状态且不允许重试
     */
    @Test
    void shouldClassifyAuthentication() {
        HttpException error = new HttpException(403, "forbidden");
        this.assertError(error, ModelErrorTypeEnum.AUTH, 403, false);
    }

    /**
     * 验证HTTP和Socket超时优先于普通网络IO归类为TIMEOUT，避免误判为NETWORK
     */
    @Test
    void shouldClassifyTimeoutBeforeNetwork() {
        HttpTimeoutException timeoutException = new HttpTimeoutException("timeout");
        RuntimeException error = new RuntimeException(timeoutException);
        this.assertError(error, ModelErrorTypeEnum.TIMEOUT, null, true);
        SocketTimeoutException socketTimeoutException = new SocketTimeoutException("socket timeout");
        RuntimeException socketError = new RuntimeException(socketTimeoutException);
        this.assertError(socketError, ModelErrorTypeEnum.TIMEOUT, null, true);
    }

    /**
     * 验证503供应商服务异常归类为SERVER，保留HTTP状态并允许重试
     */
    @Test
    void shouldClassifyServerError() {
        HttpException error = new HttpException(503, "unavailable");
        this.assertError(error, ModelErrorTypeEnum.SERVER, 503, true);
    }

    /**
     * 验证多层RuntimeException中的连接拒绝异常仍能穿透归类为NETWORK并允许重试
     */
    @Test
    void shouldClassifyNestedNetworkError() {
        ConnectException connectException = new ConnectException("connection refused");
        RuntimeException networkError = new RuntimeException(connectException);
        RuntimeException error = new RuntimeException(networkError);
        this.assertError(error, ModelErrorTypeEnum.NETWORK, null, true);
    }

    /**
     * 验证400和模型不存在等明确请求错误归类为INVALID且不允许重试
     */
    @Test
    void shouldClassifyInvalidRequest() {
        HttpException error = new HttpException(400, "bad request");
        this.assertError(error, ModelErrorTypeEnum.INVALID, 400, false);
        ModelNotFoundException modelNotFoundException = new ModelNotFoundException("model not found");
        this.assertError(modelNotFoundException, ModelErrorTypeEnum.INVALID, null, false);
    }

    /**
     * 验证多层异常中的完整限流短语按大小写不敏感识别为RATE_LIMIT，并允许应用层重试
     */
    @Test
    void shouldClassifyExplicitRateLimitMessage() {
        IllegalStateException cause = new IllegalStateException("Rate_Limit_Exceeded");
        RuntimeException error = new RuntimeException(cause);
        this.assertError(error, ModelErrorTypeEnum.RATE_LIMIT, null, true);
        RuntimeException httpStyleError = new RuntimeException("429 Too Many Requests");
        this.assertError(httpStyleError, ModelErrorTypeEnum.RATE_LIMIT, null, true);
        RuntimeException chineseError = new RuntimeException("当前请求过于频繁，请控制调用速度");
        this.assertError(chineseError, ModelErrorTypeEnum.RATE_LIMIT, null, true);
    }

    /**
     * 验证明确HTTP状态优先于限流文案，400、403和503不会被错误改判为限流
     */
    @Test
    void shouldKeepHttpStatusBeforeRateLimitMessage() {
        this.assertError(new HttpException(400, "too many requests"), ModelErrorTypeEnum.INVALID, 400, false);
        this.assertError(new HttpException(403, "rate limit exceeded"), ModelErrorTypeEnum.AUTH, 403, false);
        this.assertError(new HttpException(503, "too many requests"), ModelErrorTypeEnum.SERVER, 503, true);
    }

    /**
     * 验证嵌套429异常携带Retry-After时保留毫秒值和RATE_LIMIT分类
     */
    @Test
    void shouldPreserveRetryAfterFromNestedHttpError() {
        RetryAfterHttpException httpException = new RetryAfterHttpException(429, "limited", 2000L);
        RateLimitException rateLimitException = new RateLimitException(httpException);
        RuntimeException error = new RuntimeException(rateLimitException);
        ModelInvocationException exception = this.assertError(error, ModelErrorTypeEnum.RATE_LIMIT, 429, true);
        assertEquals(2000L, exception.getRetryAfterMs());
    }

    /**
     * 验证孤立状态数字、泛化额度或普通参数异常不按字符串误分类，统一落入UNKNOWN且不重试
     */
    @Test
    void shouldFallbackUnknownWithoutExplicitRateLimitMessage() {
        RuntimeException error = new RuntimeException("upstream returned 429");
        this.assertError(error, ModelErrorTypeEnum.UNKNOWN, null, false);
        RuntimeException quotaError = new RuntimeException("quota configuration");
        this.assertError(quotaError, ModelErrorTypeEnum.UNKNOWN, null, false);
        IllegalArgumentException argumentException = new IllegalArgumentException("local argument");
        this.assertError(argumentException, ModelErrorTypeEnum.UNKNOWN, null, false);
    }

    /**
     * 验证JSON解析异常虽属于IO体系仍归类为UNKNOWN，避免误报网络故障并触发重试
     */
    @Test
    void shouldNotClassifyJsonErrorAsNetwork() {
        JsonParseException parseException = new JsonParseException("bad json");
        RuntimeException error = new RuntimeException(parseException);
        this.assertError(error, ModelErrorTypeEnum.UNKNOWN, null, false);
    }

    /**
     * 验证已统一的流式超时异常不会被重复包装，仍保留TIMEOUT分类和动态秒数提示
     */
    @Test
    void shouldPreserveStandardTimeoutException() {
        ModelTimeoutException timeoutException = new ModelTimeoutException(1L, 60000L);
        RuntimeException error = new RuntimeException(timeoutException);
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        assertSame(timeoutException, exception);
        assertEquals(ModelErrorTypeEnum.TIMEOUT, exception.getErrorType());
        assertTrue(exception.getUserMessage().contains("60 秒"));
        assertSame(timeoutException, ModelErrorClassifier.find(error));
    }

    /**
     * 校验分类结果与原始异常信息
     * @param error 原始异常
     * @param errorType 预期分类
     * @param httpStatus 预期HTTP状态
     * @param retryable 预期重试标志
     * @return 统一异常
     */
    private ModelInvocationException assertError(Throwable error, ModelErrorTypeEnum errorType,
                                                  Integer httpStatus, boolean retryable) {
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        assertEquals(errorType, exception.getErrorType());
        assertEquals(errorType, ModelErrorClassifier.classify(error));
        assertEquals(httpStatus, exception.getHttpStatus());
        assertEquals(retryable, exception.isRetryable());
        assertSame(error, exception.getCause());
        assertFalse(exception.getUserMessage().isBlank());
        if (httpStatus == null) {
            assertNull(exception.getHttpStatus());
        }
        return exception;
    }
}
