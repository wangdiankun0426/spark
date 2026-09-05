package com.spark.web.rest.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/7 14:48
 * 跨域资源共享过滤器
 * 解决浏览器跨域问题
 */
public class CorsFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    /**
     * 过滤器
     * @param request 请求
     * @param response 响应
     * @param chain 过滤器链
     * @throws IOException 抛出IO异常
     * @throws ServletException 抛出Servlet异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String origin = req.getHeader("Origin");
        resp.setHeader("Access-Control-Allow-Origin", origin);
        // --- 设置通用的 CORS 头 ---
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS, HEAD");
        // 预检请求缓存时间
        resp.setHeader("Access-Control-Max-Age", "3600");
        // 允许携带凭证 (Cookie, Authorization headers)
        // !! 关键 !!: 设置此项后，Access-Control-Allow-Origin 不能是 *
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        // --- 处理预检请求 (OPTIONS) ---
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            String requestHeaders = req.getHeader("Access-Control-Request-Headers");
            resp.setHeader("Access-Control-Allow-Headers", requestHeaders);
            // 预检请求处理完毕，直接返回 200 OK
            resp.setStatus(HttpServletResponse.SC_OK);
            // 不再传递给后续 Filter/Servlet
            return;
        }
        resp.setHeader("Access-Control-Allow-Headers", "*");
        // 放行请求给下一个 Filter 或最终的 Servlet (DispatcherServlet)
        chain.doFilter(request, response);
    }
}
