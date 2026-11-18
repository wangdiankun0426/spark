package com.spark.llm.mcp;

import com.spark.common.bean.llm.entity.Mcp;
import com.spark.common.enums.McpTransportTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.StringUtil;
import com.spark.llm.security.LlmApiKeyCipher;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 15:30:00
 * MCP客户端管理器
 * 负责维护MCP服务器连接，按服务器ID缓存客户端
 */
@Component
public class McpClientManager {
    private final static Logger logger = LoggerFactory.getLogger(McpClientManager.class);
    @Autowired
    private LlmApiKeyCipher llmApiKeyCipher;
    /**
     * MCP客户端缓存：key=服务器ID, value=MCP客户端
     */
    private final Map<Long, McpClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 初始化并连接到MCP服务器
     * @param mcp MCP服务器配置
     * @return MCP客户端
     */
    public McpClient connectToServer(Mcp mcp) {
        if (mcp == null || mcp.getId() == null) {
            return null;
        }
        if (!StatusEnum.NORMAL.getValue().equals(mcp.getStatus())) {
            logger.warn("connectToServer mcp status not normal, status={}", mcp.getStatus());
            return null;
        }
        Long serverId = mcp.getId();
        // 已连接则直接返回
        if (clientCache.containsKey(serverId)) {
            return clientCache.get(serverId);
        }
        try {
            logger.info("connectToServer serverId={}", serverId);
            McpClient client;
            if (McpTransportTypeEnum.HTTP.getValue().equals(mcp.getTransport())) {
                if (StringUtil.isBlank(mcp.getSecretKey())) {
                    logger.error("connectToServer mcp secret key is null");
                    return null;
                }
                String secretKey = llmApiKeyCipher.decrypt(mcp.getSecretKey());
                client = createHttpClient(mcp, secretKey);
            } else {
                logger.error("connectToServer mcp transport type unsupported, transport={}", mcp.getTransport());
                return null;
            }
            clientCache.put(serverId, client);
            logger.info("connectToServer serverId={}", serverId);
            return client;
        } catch (Exception e) {
            logger.error("connectToServer error", e);
            throw new RuntimeException("连接MCP服务器失败: " + mcp.getName(), e);
        }
    }

    /**
     * 批量连接到多个MCP服务器
     * @param mcps MCP服务器配置列表
     * @return MCP客户端列表
     */
    public List<McpClient> connectToServers(List<Mcp> mcps) {
        List<McpClient> clients = new ArrayList<>();
        if (mcps == null || mcps.isEmpty()) {
            return clients;
        }
        for (Mcp mcp : mcps) {
            try {
                McpClient client = connectToServer(mcp);
                if (client != null) {
                    clients.add(client);
                }
            } catch (Exception e) {
                logger.error("connect mcp server fail, next={}", mcp.getName(), e);
            }
        }
        logger.info("connect mcp server success, size={}", clients.size());
        return clients;
    }

    /**
     * 断开指定MCP服务器连接
     * @param serverId 服务器ID
     */
    public void disconnect(Long serverId) {
        if (serverId == null) {
            return;
        }
        McpClient client = clientCache.remove(serverId);
        if (client != null) {
            try {
                client.close();
                logger.info("disconnect mcp server, serverId={}", serverId);
            } catch (Exception e) {
                logger.warn("disconnect mcp server error, serverId={}", serverId, e);
            }
        }
    }

    /**
     * 断开所有MCP服务器连接
     */
    @PreDestroy
    public void disconnectAll() {
        for (Map.Entry<Long, McpClient> entry : clientCache.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                logger.warn("disconnect mcp server error, serverId={}", entry.getKey(), e);
            }
        }
        clientCache.clear();
        logger.info("all mcp server disconnected");
    }

    /**
     * 创建HTTP传输的MCP客户端
     * @param mcp MCP服务器配置
     * @param secretKey 解密后的独立密钥
     * @return MCP客户端
     */
    private McpClient createHttpClient(Mcp mcp, String secretKey) {
        StreamableHttpMcpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url(mcp.getUrl())
                .customHeaders(Map.of("Authorization", "Bearer " + secretKey))
                .logRequests(false)
                .logResponses(true)
                .build();
        return clientBuilder(mcp, transport).build();
    }

    /**
     * 构造MCP客户端构建器，应用连接与工具执行超时配置
     * @param mcp MCP服务器配置
     * @param transport 传输层
     * @return 客户端构建器
     */
    private DefaultMcpClient.Builder clientBuilder(Mcp mcp, McpTransport transport) {
        long timeoutSeconds = mcp.getTimeout() != null ? mcp.getTimeout() : 30L;
        return new DefaultMcpClient.Builder()
                .transport(transport)
                .initializationTimeout(Duration.ofSeconds(timeoutSeconds))
                .toolExecutionTimeout(Duration.ofSeconds(timeoutSeconds));
    }
}
