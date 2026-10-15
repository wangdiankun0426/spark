package com.spark.web.mcp.filter;

import com.spark.llm.service.IMcpKeyService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/15 16:11
 */
@Configuration
public class McpFilterConfig {

    /**
     * 添加MCP密钥鉴权过滤器
     * @param mcpKeyService MCP密钥服务
     * @return 过滤器注册bean
     */
    @Bean
    public FilterRegistrationBean<McpKeyAuthFilter> mcpKeyAuthFilter(IMcpKeyService mcpKeyService) {
        FilterRegistrationBean<McpKeyAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new McpKeyAuthFilter(mcpKeyService));
        // 仅拦截MCP服务端端点
        registration.addUrlPatterns("/mcp", "/mcp/*");
        registration.setName("mcpKeyAuthFilter");
        registration.setOrder(3);
        return registration;
    }
}
