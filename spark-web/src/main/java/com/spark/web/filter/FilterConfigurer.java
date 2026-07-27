package com.spark.web.filter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/10/22 13:18
 * 添加servlet过滤器
 * 用户请求到返回整体链路如下
 * Client
 *   ↓
 * [Servlet Container]
 *   ↓
 * Filter (通过 FilterRegistrationBean 注册)
 *   ↓
 * DispatcherServlet
 *   ↓
 * HandlerInterceptor.preHandle()
 *   ↓
 * Controller
 *   ↓
 * HandlerInterceptor.postHandle()
 *   ↓
 * View Rendering
 *   ↓
 * HandlerInterceptor.afterCompletion()
 *   ↓
 * Filter (doFilter 中的后置逻辑，如 finally 块)
 *   ↓
 * Response to Client
 */
@Configuration
public class FilterConfigurer {

    /**
     * 添加traceId过滤器
     * @return 过滤器注册bean
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        // 拦截所有请求
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");
        // 优先级高（数字越小越早）
        registration.setOrder(1);
        return registration;
    }

    /**
     * 添加cors过滤器
     * @return 过滤器注册bean
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilter());
        // 拦截所有请求
        registration.addUrlPatterns("/*");
        registration.setName("corsFilter");
        // 优先级低（数字越大越晚）
        registration.setOrder(2);
        return registration;
    }

}