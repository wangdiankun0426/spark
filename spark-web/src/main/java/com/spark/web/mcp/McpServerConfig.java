package com.spark.web.mcp;

import com.spark.llm.mcp.McpServerToolExecutor;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:30:00
 * MCP服务端装配，注册Streamable HTTP端点
 */
@Configuration
public class McpServerConfig {
    /**
     * MCP服务端端点路径
     */
    private final static String MCP_ENDPOINT = "/mcp";

    /**
     * 注册传输层，提取Authorization头存入传输上下文供工具鉴权
     * @return 传输层
     */
    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransportProvider() {
        return HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint(MCP_ENDPOINT)
                .contextExtractor(request -> {
                    Map<String, Object> contextMap = new HashMap<>();
                    String authorization = request.getHeader("Authorization");
                    if (authorization != null) {
                        contextMap.put(McpServerToolExecutor.CONTEXT_AUTHORIZATION, authorization);
                    }
                    return McpTransportContext.create(contextMap);
                })
                .build();
    }

    /**
     * 装配MCP服务端，注册对外工具
     * @param transportProvider 传输层
     * @param mcpServerToolExecutor 工具执行器
     * @return MCP服务端
     */
    @Bean
    public McpSyncServer mcpSyncServer(HttpServletStreamableServerTransportProvider transportProvider,
                                       McpServerToolExecutor mcpServerToolExecutor) {
        return McpServer.sync(transportProvider)
                .serverInfo("spark-mcp-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(mcpServerToolExecutor.toolSpecifications())
                .build();
    }

    /**
     * 将传输层注册为servlet，独立于DispatcherServlet不走登录拦截器
     * @param transportProvider 传输层
     * @return servlet注册bean
     */
    @Bean
    public ServletRegistrationBean<HttpServletStreamableServerTransportProvider> mcpServlet(HttpServletStreamableServerTransportProvider transportProvider) {
        ServletRegistrationBean<HttpServletStreamableServerTransportProvider> registration = new ServletRegistrationBean<>();
        registration.setServlet(transportProvider);
        registration.addUrlMappings(MCP_ENDPOINT);
        registration.setName("mcpServlet");
        return registration;
    }
}
