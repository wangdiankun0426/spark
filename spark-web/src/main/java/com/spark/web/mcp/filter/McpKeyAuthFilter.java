package com.spark.web.mcp.filter;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.result.McpKeyResult;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import com.spark.llm.service.IMcpKeyService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
 * @since 2026-10-15 14:32:00
 * MCP服务端密钥鉴权过滤器
 */
public class McpKeyAuthFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(McpKeyAuthFilter.class);
    /**
     * Authorization头前缀
     */
    private final static String BEARER_PREFIX = "Bearer ";
    private final IMcpKeyService mcpKeyService;

    public McpKeyAuthFilter(IMcpKeyService mcpKeyService) {
        this.mcpKeyService = mcpKeyService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 浏览器预检直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, mcp-session-id");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        String authorization = request.getHeader("Authorization");
        String keyValue = null;
        if (StringUtil.isNotBlank(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            keyValue = authorization.substring(BEARER_PREFIX.length());
        }
        ResultData<McpKeyResult> validateResult = mcpKeyService.validateApiKey(keyValue);
        if (validateResult == null || validateResult.getCode() != ResultData.OK || validateResult.getData() == null) {
            logger.warn("mcp key auth fail, uri={}", request.getRequestURI());
            ResultData<Void> result = new ResultData<>();
            result.setErrorCode(ErrorCodeEnum.MCP_KEY_INVALID);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtil.toString(result));
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
