package com.spark.web.rest.filter;

import com.spark.common.utils.TraceLogUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/10/22 13:01
 * TraceId过滤器
 */
public class TraceIdFilter implements Filter {

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
        try{
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            String trackId = TraceLogUtil.generateTrackId(null);
            resp.addHeader(TraceLogUtil.TRACK_ID_KEY, trackId);
            chain.doFilter(request, response);
        }finally {
            TraceLogUtil.removeTrackId();
        }
    }
}
