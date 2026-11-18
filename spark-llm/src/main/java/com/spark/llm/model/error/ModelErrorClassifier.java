package com.spark.llm.model.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spark.common.enums.ModelErrorTypeEnum;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.exception.InvalidRequestException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.exception.UnresolvedModelServerException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 11:00:00
 * 模型错误分类器，仅依据异常类型和HTTP状态归一化错误
 */
public final class ModelErrorClassifier {

    private ModelErrorClassifier() {
    }

    /**
     * 分类模型调用异常
     * @param error 原始异常
     * @return 标准错误分类
     */
    public static ModelErrorTypeEnum classify(Throwable error) {
        ModelInvocationException exception = wrap(error);
        return exception.getErrorType();
    }

    /**
     * 包装为统一模型异常
     * @param error 原始异常
     * @return 统一模型异常
     */
    public static ModelInvocationException wrap(Throwable error) {
        ModelInvocationException existing = find(error);
        if (existing != null) {
            return existing;
        }
        HttpException httpException = findHttpException(error);
        Integer httpStatus = null;
        ModelErrorTypeEnum errorType = ModelErrorTypeEnum.UNKNOWN;
        if (httpException != null) {
            httpStatus = httpException.statusCode();
            errorType = classifyHttpStatus(httpStatus);
        }
        if (errorType == ModelErrorTypeEnum.UNKNOWN) {
            errorType = classifyByType(error);
        }
        String message = error != null ? error.getMessage() : null;
        return new ModelInvocationException(errorType, httpStatus, message, error, errorType.getUserMessage());
    }

    /**
     * 从异常链查找统一模型异常
     * @param error 原始异常
     * @return 统一模型异常，不存在时返回null
     */
    public static ModelInvocationException find(Throwable error) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = error;
        while (current != null && visited.add(current)) {
            if (current instanceof ModelInvocationException exception) {
                return exception;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 解析用户提示
     * @param error 原始异常
     * @param defaultMessage 默认提示
     * @return 用户提示
     */
    public static String resolveUserMessage(Throwable error, String defaultMessage) {
        ModelInvocationException exception = find(error);
        if (exception == null || exception.getErrorType() == ModelErrorTypeEnum.UNKNOWN) {
            return defaultMessage;
        }
        return exception.getUserMessage();
    }

    /**
     * 查找异常链中的HTTP异常
     * @param error 原始异常
     * @return HTTP异常
     */
    private static HttpException findHttpException(Throwable error) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = error;
        while (current != null && visited.add(current)) {
            if (current instanceof HttpException exception) {
                return exception;
            }
            current = current.getCause();
        }
        return null;
    }

    /**
     * 按HTTP状态分类
     * @param statusCode HTTP状态
     * @return 标准错误分类
     */
    private static ModelErrorTypeEnum classifyHttpStatus(int statusCode) {
        if (statusCode == 429) {
            return ModelErrorTypeEnum.RATE_LIMIT;
        }
        if (statusCode == 401 || statusCode == 403) {
            return ModelErrorTypeEnum.AUTH;
        }
        if (statusCode == 408) {
            return ModelErrorTypeEnum.TIMEOUT;
        }
        if (statusCode >= 500 && statusCode <= 599) {
            return ModelErrorTypeEnum.SERVER;
        }
        if (statusCode >= 400 && statusCode <= 499) {
            return ModelErrorTypeEnum.INVALID;
        }
        return ModelErrorTypeEnum.UNKNOWN;
    }

    /**
     * 按明确异常类型分类
     * @param error 原始异常
     * @return 标准错误分类
     */
    private static ModelErrorTypeEnum classifyByType(Throwable error) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = error;
        while (current != null && visited.add(current)) {
            if (current instanceof ModelTimeoutException || current instanceof TimeoutException
                    || current instanceof HttpTimeoutException || current instanceof SocketTimeoutException
                    || current instanceof java.util.concurrent.TimeoutException) {
                return ModelErrorTypeEnum.TIMEOUT;
            }
            if (current instanceof RateLimitException) {
                return ModelErrorTypeEnum.RATE_LIMIT;
            }
            if (current instanceof AuthenticationException) {
                return ModelErrorTypeEnum.AUTH;
            }
            if (current instanceof InternalServerException) {
                return ModelErrorTypeEnum.SERVER;
            }
            if (current instanceof InvalidRequestException || current instanceof ModelNotFoundException) {
                return ModelErrorTypeEnum.INVALID;
            }
            if (current instanceof UnresolvedModelServerException || current instanceof UnresolvedAddressException) {
                return ModelErrorTypeEnum.NETWORK;
            }
            if (current instanceof JsonProcessingException) {
                return ModelErrorTypeEnum.UNKNOWN;
            }
            if (current instanceof IOException) {
                return ModelErrorTypeEnum.NETWORK;
            }
            current = current.getCause();
        }
        return ModelErrorTypeEnum.UNKNOWN;
    }
}
