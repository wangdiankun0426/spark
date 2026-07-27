package com.spark.llm.agent;

import com.spark.bean.llm.query.AgentQuery;
import com.spark.bean.llm.result.AgentResult;
import com.spark.bean.llm.result.McpResult;
import com.spark.bean.llm.entity.Agent;
import com.spark.bean.llm.entity.Mcp;
import com.spark.bean.llm.query.McpQuery;
import com.spark.enums.AgentToolEnum;
import com.spark.llm.IAgent;
import com.spark.dao.llm.AgentDao;
import com.spark.dao.llm.McpDao;
import com.spark.llm.mcp.McpClientManager;
import com.spark.llm.memory.RedisChatMemoryStore;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.tools.SearchKnowledge;
import com.spark.llm.tools.SearchKnowledgeGraph;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent工厂类
 * 基于Agent ID构建和缓存Agent实例
 * 
 * @author wangdiankun
 * @since 2026/05/14
 */
@Component
public class AgentFactory {
    private final static Logger logger = LoggerFactory.getLogger(AgentFactory.class);
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private SearchKnowledge searchKnowledge;
    @Autowired
    private SearchKnowledgeGraph searchKnowledgeGraph;
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;
    @Autowired
    private McpClientManager mcpClientManager;
    @Autowired
    private McpDao mcpDao;
    // Agent缓存，key为agentId，value为构建好的IAgent实例
    private final Map<Long, IAgent> agentCache = new ConcurrentHashMap<>();

    /**
     * 根据Agent ID构建Agent实例（带缓存）
     * 如果该Agent已缓存，则直接返回；否则从数据库加载配置（含绑定的语言模型）并构建
     *
     * @param agentId Agent的ID
     * @return IAgent实例
     */
    public IAgent build(Long agentId) {
        if (agentId == null) {
            throw new IllegalStateException("agentId must not be null");
        }
        // 检查缓存
        IAgent cachedAgent = agentCache.get(agentId);
        if (cachedAgent != null) {
            logger.info("从缓存获取Agent, agentId: {}", agentId);
            return cachedAgent;
        }
        // 从数据库加载Agent配置
        Agent agent = this.loadAgentFromDb(agentId);
        if (agent == null) {
            throw new IllegalStateException("Agent not found with id: " + agentId);
        }
        Long chatModelId = agent.getChatModelId();
        if (chatModelId == null) {
            throw new IllegalStateException("Agent未配置语言模型, agentId: " + agentId);
        }
        int maxMessages = agent.getMaxMessages() != null ? agent.getMaxMessages() : 10;
        // 构建Agent，记忆通过RedisChatMemoryStore持久化，按memoryId（spaceId）隔离
        AiServices<IAgent> builder = AiServices.builder(IAgent.class)
                .streamingChatModel(modelFactory.getStreamingChatModel(chatModelId))
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(maxMessages)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build());
        // 设置系统提示词
        if (StringUtil.isNotBlank(agent.getSystemPrompt())) {
            builder.systemMessageProvider(sm -> agent.getSystemPrompt());
        } else {
            // 如果没有配置系统提示词，使用默认的工具优先提示词
            String defaultPrompt = "你是一个智能助手。当用户提出问题时，请优先使用可用的工具来获取准确信息。如果工具可以提供相关信息，必须先调用工具，不要仅凭自己的知识直接回答。";
            builder.systemMessageProvider(sm -> defaultPrompt);
        }
        //  添加工具
        List<String> toolList = this.parseTools(agent.getTools());
        if (CollectionUtil.isNotEmpty(toolList)) {
            List<Object> toolObjects = new ArrayList<>();
            for (String tool : toolList) {
                if (AgentToolEnum.SEARCH_KB.getType().equals(tool)) {
                    List<Long> kbIds = parseKbIds(agent.getKbIds());
                    toolObjects.add(searchKnowledge.forAgent(kbIds));
                } else if (AgentToolEnum.SEARCH_KG.getType().equals(tool)) {
                    List<Long> graphIds = parseGraphIds(agent.getGraphIds());
                    toolObjects.add(searchKnowledgeGraph.forAgent(graphIds));
                } else {
                    logger.warn("未知的工具：{}", tool);
                }
            }
            builder.tools(toolObjects);
        }
        // 注入MCP工具
        List<McpClient> mcpClients = this.loadMcpClients(agent.getMcpIds());
        if (CollectionUtil.isNotEmpty(mcpClients)) {
            McpToolProvider mcpToolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClients)
                    .failIfOneServerFails(false)
                    .build();
            builder.toolProvider(mcpToolProvider);
        }
        IAgent newAgent = builder.build();
        // 存入缓存
        agentCache.put(agentId, newAgent);
        logger.info("Agent构建完成并缓存, agentId: {}, 名称：{}, 描述：{}", agentId, agent.getName(), agent.getDescription());
        return newAgent;
    }
    
    /**
     * 从数据库加载Agent配置
     */
    private Agent loadAgentFromDb(Long agentId) {
        AgentQuery query = new AgentQuery();
        query.setId(agentId);
        AgentResult result = agentDao.queryAgent(query);
        if (result == null) {
            return null;
        }
        Agent agent = new Agent();
        agent.setId(result.getId());
        agent.setName(result.getName());
        agent.setChatModelId(result.getChatModelId());
        agent.setSystemPrompt(result.getSystemPrompt());
        agent.setMaxMessages(result.getMaxMessages());
        agent.setTools(result.getTools());
        agent.setDescription(result.getDescription());
        agent.setKbIds(result.getKbIds());
        agent.setGraphIds(result.getGraphIds());
        agent.setMcpIds(result.getMcpIds());
        return agent;
    }
    
    /**
     * 清除指定Agent的缓存
     * 当Agent配置修改后调用此方法，下次使用时会重新加载
     *
     * @param agentId Agent的ID
     */
    public void clearAgentCache(Long agentId) {
        if (agentId == null) {
            return;
        }
        agentCache.remove(agentId);
        logger.info("Agent缓存已清除, agentId: {}", agentId);
    }
    
    /**
     * 解析工具字符串为列表
     */
    public List<String> parseTools(String toolsStr) {
        if (StringUtil.isBlank(toolsStr)) {
            return new ArrayList<>();
        }
        List<String> tools = new ArrayList<>();
        String[] toolArray = toolsStr.split(",");
        for (String tool : toolArray) {
            String trimmed = tool.trim();
            if (StringUtil.isNotBlank(trimmed)) {
                tools.add(trimmed);
            }
        }
        return tools;
    }

    /**
     * 解析知识库ID列表字符串为Long列表
     * @param kbIdsStr 知识库ID列表字符串（CSV格式）
     * @return 知识库ID列表
     */
    public List<Long> parseKbIds(String kbIdsStr) {
        List<Long> kbIds = new ArrayList<>();
        if (StringUtil.isBlank(kbIdsStr)) {
            return kbIds;
        }
        String[] idArray = kbIdsStr.split(",");
        for (String id : idArray) {
            String trimmed = id.trim();
            if (StringUtil.isNotBlank(trimmed)) {
                try {
                    kbIds.add(Long.parseLong(trimmed));
                } catch (NumberFormatException e) {
                    logger.warn("无效的知识库ID：{}", trimmed);
                }
            }
        }
        return kbIds;
    }

    /**
     * 解析知识图谱ID列表字符串为Long列表
     * @param graphIdsStr 知识图谱ID列表字符串（CSV格式）
     * @return 知识图谱ID列表
     */
    public List<Long> parseGraphIds(String graphIdsStr) {
        List<Long> graphIds = new ArrayList<>();
        if (StringUtil.isBlank(graphIdsStr)) {
            return graphIds;
        }
        String[] idArray = graphIdsStr.split(",");
        for (String id : idArray) {
            String trimmed = id.trim();
            if (StringUtil.isNotBlank(trimmed)) {
                try {
                    graphIds.add(Long.parseLong(trimmed));
                } catch (NumberFormatException e) {
                    logger.warn("无效的知识图谱ID：{}", trimmed);
                }
            }
        }
        return graphIds;
    }

    /**
     * 解析MCP服务器ID列表字符串为Long列表
     * @param McpIdsStr MCP服务器ID列表字符串（CSV格式）
     * @return MCP服务器ID列表
     */
    public List<Long> parseMcpIds(String McpIdsStr) {
        List<Long> McpIds = new ArrayList<>();
        if (StringUtil.isBlank(McpIdsStr)) {
            return McpIds;
        }
        String[] idArray = McpIdsStr.split(",");
        for (String id : idArray) {
            String trimmed = id.trim();
            if (StringUtil.isNotBlank(trimmed)) {
                try {
                    McpIds.add(Long.parseLong(trimmed));
                } catch (NumberFormatException e) {
                    logger.warn("无效的MCP服务器ID：{}", trimmed);
                }
            }
        }
        return McpIds;
    }

    /**
     * 加载MCP服务器并建立连接
     * @param McpIdsStr MCP服务器ID列表字符串
     * @return MCP客户端列表
     */
    private List<McpClient> loadMcpClients(String McpIdsStr) {
        List<Long> McpIds = parseMcpIds(McpIdsStr);
        if (CollectionUtil.isEmpty(McpIds)) {
            return new ArrayList<>();
        }
        McpQuery query = new McpQuery();
        query.setIds(McpIds);
        query.setPage(false);
        List<McpResult> mcpResults = mcpDao.queryMcpList(query);
        if (CollectionUtil.isEmpty(mcpResults)) {
            return new ArrayList<>();
        }
        List<Mcp> mcpList = new ArrayList<>();
        for (McpResult mcpResult : mcpResults) {
            Mcp mcp = new Mcp();
            mcp.setId(mcpResult.getId());
            mcp.setName(mcpResult.getName());
            mcp.setTransport(mcpResult.getTransport());
            mcp.setProviderId(mcpResult.getProviderId());
            mcp.setCommand(mcpResult.getCommand());
            mcp.setArgs(mcpResult.getArgs());
            mcp.setUrl(mcpResult.getUrl());
            mcp.setEnv(mcpResult.getEnv());
            mcp.setStatus(mcpResult.getStatus());
            mcpList.add(mcp);
        }
        return mcpClientManager.connectToServers(mcpList);
    }
}
