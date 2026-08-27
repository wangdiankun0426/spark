package com.spark.workflow.engine;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 10:00:00
 * 超时控制器：节点级超时控制，超时后中断执行线程
 */
@Component
public class TimeoutStrategy {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutStrategy.class);

    /**
     * 默认超时时间（毫秒）：5分钟
     */
    public static final long DEFAULT_TIMEOUT_MS = 5 * 60 * 1000L;

    /**
     * 线程池上限（有界，防止节点执行堆积耗尽线程）
     */
    private static final int MAX_POOL_SIZE = 32;

    /**
     * 线程计数器
     */
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    /**
     * 执行器服务：有界线程池，队列满时由调用线程执行兜底
     */
    private final ExecutorService executor = new ThreadPoolExecutor(
            4, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            r -> {
                Thread t = new Thread(r, "wf-node-exec-" + THREAD_COUNTER.incrementAndGet());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 执行带超时的操作
     * @param action 待执行操作
     * @param timeoutMs 超时时间（毫秒），为空或非正数时使用默认超时
     * @param <T> 返回类型
     * @return 执行结果
     * @throws TimeoutException 超时异常
     * @throws ExecutionException 执行异常
     * @throws InterruptedException 中断异常
     */
    public <T> T executeWithTimeout(Callable<T> action, Long timeoutMs)
            throws TimeoutException, ExecutionException, InterruptedException {
        long effectiveTimeout = (timeoutMs == null || timeoutMs <= 0) ? DEFAULT_TIMEOUT_MS : timeoutMs;

        Future<T> future = executor.submit(action);
        try {
            return future.get(effectiveTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warn("Execution timeout after {}ms", effectiveTimeout);
            throw new TimeoutException("执行超时，超过" + effectiveTimeout + "ms");
        } catch (ExecutionException e) {
            logger.error("Execution failed", e.getCause());
            throw e;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /**
     * 应用销毁时关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
