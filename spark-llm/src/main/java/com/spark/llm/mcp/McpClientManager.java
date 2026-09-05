package com.spark.llm.mcp;

import com.spark.common.bean.llm.entity.Mcp;
import com.spark.common.bean.llm.entity.Provider;
import com.spark.common.bean.llm.query.ProviderQuery;
import com.spark.common.bean.llm.result.ProviderResult;
import com.spark.dao.llm.ProviderDao;
import com.spark.common.enums.McpTransportTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 * MCP客户端管理器，负责维护MCP服务器连接，按服务器ID缓存客户端
 */
@Component
public class McpClientManager {
    private final static Logger logger = LoggerFactory.getLogger(McpClientManager.class);
    @Autowired
    private ProviderDao providerDao;
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
            logger.warn("connectToServer: mcp status is ={}", mcp.getStatus());
            return null;
        }
        Long serverId = mcp.getId();
        // 已连接则直接返回
        if (clientCache.containsKey(serverId)) {
            return clientCache.get(serverId);
        }
        try {
            logger.info("connectToServer: serverId is ={}", serverId);
            McpClient client;
            if (McpTransportTypeEnum.STDIO.getValue().equals(mcp.getTransport())) {
                client = createStdioClient(mcp);
            } else if (McpTransportTypeEnum.SSE.getValue().equals(mcp.getTransport())) {
                if (mcp.getProviderId() == null) {
                    logger.error("connectToServer: mcp provider id is null");
                    return null;
                }
                Provider provider = loadProvider(mcp.getProviderId());
                if (provider == null || StringUtil.isBlank(provider.getSecretKey())) {
                    logger.error("connectToServer: provider secret key is null");
                    return null;
                }
                client = createSseClient(mcp, provider);
            } else {
                logger.error("connectToServer: mcp transport type is ={}", mcp.getTransport());
                return null;
            }
            clientCache.put(serverId, client);
            logger.info("connectToServer: serverId is ={}", serverId);
            return client;
        } catch (Exception e) {
            logger.error("connectToServer: ", e);
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
                logger.error("connect MCP server fail，next={}", mcp.getName(), e);
            }
        }
        logger.info("connect success {} size MCP server", clients.size());
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
                logger.info("disconnect MCP server, serverId={}", serverId);
            } catch (Exception e) {
                logger.warn("disconnect MCP server error, serverId={}", serverId, e);
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
                logger.warn("disconnect MCP server error, serverId={}", entry.getKey(), e);
            }
        }
        clientCache.clear();
        logger.info("all MCP server disconnect");
    }

    /**
     * 创建STDIO传输的MCP客户端
     */
    private McpClient createStdioClient(Mcp mcp) {
        List<String> commandList = new ArrayList<>();
        commandList.add(mcp.getCommand());
        List<String> args = JsonUtil.parseList(mcp.getArgs());
        if (CollectionUtil.isNotEmpty(args)) {
            commandList.addAll(args);
        }
        StdioMcpTransport.Builder transportBuilder = new StdioMcpTransport.Builder()
                .command(commandList)
                .logEvents(true);
        Map<String, String> envMap = JsonUtil.parseMap(mcp.getEnv());
        if (MapUtil.isNotEmpty(envMap)) {
            transportBuilder.environment(envMap);
        }
        StdioMcpTransport transport = transportBuilder.build();
        return new DefaultMcpClient.Builder().transport(transport).build();
    }

    /**
     * 创建SSE传输的MCP客户端
     * @param mcp MCP服务器配置
     * @param provider 厂商信息（提供密钥用于鉴权）
     * @return MCP客户端
     */
    private McpClient createSseClient(Mcp mcp, Provider provider) {
        StreamableHttpMcpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url(mcp.getUrl())
                .customHeaders(Map.of("Authorization", "Bearer " + provider.getSecretKey()))
                .logRequests(true)
                .logResponses(true)
                .build();
        return new DefaultMcpClient.Builder().transport(transport).build();
    }

    /**
     * 从数据库加载厂商信息
     * @param providerId 厂商ID
     * @return 厂商信息，不存在返回null
     */
    private Provider loadProvider(Long providerId) {
        if (providerId == null) {
            return null;
        }
        ProviderQuery query = new ProviderQuery();
        query.setId(providerId);
        ProviderResult providerResult = providerDao.queryProvider(query);
        if (providerResult == null) {
            return null;
        }
        Provider provider = new Provider();
        provider.setId(providerResult.getId());
        provider.setName(providerResult.getName());
        provider.setApiUrl(providerResult.getApiUrl());
        provider.setSecretKey(providerResult.getSecretKey());
        provider.setIcon(providerResult.getIcon());
        provider.setDescription(providerResult.getDescription());
        provider.setRemark(providerResult.getRemark());
        provider.setOrderNum(providerResult.getOrderNum());
        return provider;
    }
}
