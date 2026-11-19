package com.spark.llm.usage;

import com.spark.common.bean.llm.dto.ModelInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 09:00:00
 * 逻辑模型调用的重试计数上下文，支持同步嵌套绑定与异步共享
 */
public class ModelRetryContext {
    private static final ThreadLocal<Deque<ModelRetryContext>> CONTEXT_HOLDER = new ThreadLocal<>();
    private final AtomicInteger retryCount = new AtomicInteger();
    private volatile boolean fallback;
    private volatile ModelInfo actualModelInfo;

    /**
     * 增加一次实际重试
     * @return 当前重试次数
     */
    public int incrementRetryCount() {
        return retryCount.incrementAndGet();
    }

    /**
     * 获取当前重试次数
     * @return 重试次数
     */
    public int getRetryCount() {
        return retryCount.get();
    }

    /**
     * 记录当前实际调用目标
     * @param modelInfo 当前目标模型元数据
     * @param fallback 是否属于备用目标
     */
    public void selectTarget(ModelInfo modelInfo, boolean fallback) {
        if (modelInfo == null) {
            throw new IllegalArgumentException("modelInfo must not be null");
        }
        this.actualModelInfo = modelInfo;
        if (fallback) {
            this.fallback = true;
        }
    }

    /**
     * 获取当前实际目标模型元数据
     * @return 模型元数据
     */
    public ModelInfo getActualModelInfo() {
        return actualModelInfo;
    }

    /**
     * 判断是否实际经过备用模型
     * @return 是否降级
     */
    public boolean isFallback() {
        return fallback;
    }

    /**
     * 获取当前线程最近的重试上下文
     * @return 重试上下文
     */
    public static ModelRetryContext current() {
        Deque<ModelRetryContext> contexts = CONTEXT_HOLDER.get();
        if (contexts == null) {
            return null;
        }
        return contexts.peek();
    }

    /**
     * 绑定重试上下文
     * @param context 重试上下文
     */
    public static void bind(ModelRetryContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        Deque<ModelRetryContext> contexts = CONTEXT_HOLDER.get();
        if (contexts == null) {
            contexts = new ArrayDeque<>();
            CONTEXT_HOLDER.set(contexts);
        }
        contexts.push(context);
    }

    /**
     * 按身份解绑重试上下文
     * @param context 待解绑上下文
     */
    public static void unbind(ModelRetryContext context) {
        if (context == null) {
            return;
        }
        Deque<ModelRetryContext> contexts = CONTEXT_HOLDER.get();
        if (contexts == null) {
            return;
        }
        if (contexts.peek() == context) {
            contexts.pop();
        } else {
            contexts.removeFirstOccurrence(context);
        }
        if (contexts.isEmpty()) {
            CONTEXT_HOLDER.remove();
        }
    }
}
