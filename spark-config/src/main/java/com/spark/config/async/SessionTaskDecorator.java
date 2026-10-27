package com.spark.config.async;

import com.spark.common.utils.SessionContextUtil;
import org.springframework.core.task.TaskDecorator;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-28 10:00:00
 * 异步线程池任务装饰器，提交线程捕获登录会话与MDC、执行线程恢复，执行完还原或清空
 */
public class SessionTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return SessionContextUtil.wrap(runnable);
    }
}
