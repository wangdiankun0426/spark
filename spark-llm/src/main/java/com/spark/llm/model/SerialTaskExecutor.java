package com.spark.llm.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 09:00:00
 * 请求级串行任务执行器，复用共享线程并保持事件顺序
 */
public class SerialTaskExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(SerialTaskExecutor.class);
    private final Executor delegate;
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private boolean running;

    public SerialTaskExecutor(Executor delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable task) {
        if (task == null) {
            return;
        }
        synchronized (tasks) {
            tasks.offer(task);
            if (running) {
                return;
            }
            running = true;
        }
        try {
            delegate.execute(this::drain);
        } catch (RuntimeException e) {
            synchronized (tasks) {
                running = false;
                tasks.clear();
            }
            throw e;
        }
    }

    /**
     * 串行消费当前请求的任务队列
     */
    private void drain() {
        while (true) {
            Runnable task;
            synchronized (tasks) {
                task = tasks.poll();
                if (task == null) {
                    running = false;
                    return;
                }
            }
            try {
                task.run();
            } catch (RuntimeException e) {
                logger.error("serial task execution error", e);
            }
        }
    }
}
