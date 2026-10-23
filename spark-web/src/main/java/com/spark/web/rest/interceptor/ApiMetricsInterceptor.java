package com.spark.web.rest.interceptor;

import com.spark.monitor.collect.ApiMetricsCollector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 接口请求指标采集拦截器
 */
@Component
public class ApiMetricsInterceptor implements HandlerInterceptor {

    /**
     * 请求起始时间属性
     */
    private static final String START_TIME_ATTR = "apiMetricsStartTime";

    @Autowired
    private ApiMetricsCollector apiMetricsCollector;

    /**
     * 记录请求起始时间
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @return 始终放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.nanoTime());
        return true;
    }

    /**
     * 请求完成后按URI模板采集耗时与状态码
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @param ex 异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern == null) {
            return;
        }
        String uri = pattern.toString();
        if (uri.startsWith("/monitor")) {
            return;
        }
        Object startTimeObj = request.getAttribute(START_TIME_ATTR);
        if (!(startTimeObj instanceof Long startTime)) {
            return;
        }
        int costMs = (int) Math.max((System.nanoTime() - startTime) / 1_000_000L, 1L);
        apiMetricsCollector.record(uri, request.getMethod(), response.getStatus(), costMs);
    }
}
