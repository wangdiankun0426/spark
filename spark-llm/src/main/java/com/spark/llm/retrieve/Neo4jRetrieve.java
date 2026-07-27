package com.spark.llm.retrieve;

import com.alibaba.fastjson2.JSON;
import com.spark.bean.base.ResultData;
import com.spark.bean.kg.entity.KgEntity;
import com.spark.bean.kg.entity.RelationEdge;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.prompt.PromptTemplateLoader;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/23 14:44
 * neo4j召回服务
 */
@Service
public class Neo4jRetrieve {
    private final static Logger logger = LoggerFactory.getLogger(Neo4jRetrieve.class);
    /**
     * 单个关键词命中的最大实体数
     */
    private static final int MAX_ENTITY_HIT = 5;
    /**
     * 单跳子图扩展的最大返回三元组数
     */
    private static final int MAX_TRIPLETS = 20;
    @Autowired
    private Neo4jGraphStore graphStore;
    @Autowired
    private ModelFactory modelFactory;

    /**
     * 按问题检索知识图谱子图，返回三元组文本
     * @param question 用户问题
     * @param graphIds 图谱 id 列表
     * @return 三元组文本列表
     */
    public ResultData<List<String>> retrieve(String question, List<Long> graphIds) {
        ResultData<List<String>> result = new ResultData<>();
        if (StringUtil.isBlank(question) || CollectionUtil.isEmpty(graphIds)) {
            result.setData(new ArrayList<>());
            result.setCode(ResultData.OK);
            return result;
        }
        List<String> triplets = new ArrayList<>();
        try {
            Set<Long> matchedEntityIds = new LinkedHashSet<>();
            // 先用 LLM 从问题中抽取实体名，再用实体名走 Neo4j 模糊匹配
            List<String> entityNames = this.extractEntitiesByLlm(question);
            if (CollectionUtil.isEmpty(entityNames)) {
                // LLM 抽取失败时回退：用整句做 CONTAINS
                logger.warn("LLM 抽取实体为空,回退整句匹配, question={}", question);
                entityNames = Collections.singletonList(question.trim());
            }
            for (Long graphId : graphIds) {
                for (String name : entityNames) {
                    this.matchEntities(graphId, name, matchedEntityIds);
                }
            }
            if (matchedEntityIds.isEmpty()) {
                result.setData(triplets);
                result.setCode(ResultData.OK);
                return result;
            }
            // 对命中的实体扩展 1 跳子图，生成三元组
            this.buildTriplets(matchedEntityIds, triplets);
        } catch (Exception e) {
            logger.error("retrieve error, question={}", question, e);
        }
        result.setData(triplets);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 调用 LLM 从问题中抽取实体名称
     * @param question 用户问题
     * @return 实体名称列表（去重、保留顺序），失败返回空列表
     */
    private List<String> extractEntitiesByLlm(String question) {
        try {
            ChatModel chatModel = modelFactory.getDefaultNoThinkChatModel();
            Map<String, Object> variables = new HashMap<>();
            variables.put("question", question);
            Prompt prompt = PromptTemplateLoader.create("prompts/kg-entity-extract-from-question.txt", variables);
            String response = chatModel.chat(prompt.text());
            logger.info("extractEntitiesByLlm question={}, response={}", question, response);
            List<String> names = parseStringArray(response);
            // 去重保留顺序
            LinkedHashSet<String> set = new LinkedHashSet<>();
            for (String name : names) {
                String trimmed = name == null ? "" : name.trim();
                if (StringUtil.isNotBlank(trimmed)) {
                    set.add(trimmed);
                }
            }
            return new ArrayList<>(set);
        } catch (Exception e) {
            logger.error("extractEntitiesByLlm error, question={}", question, e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析 LLM 返回的字符串数组，兼容代码块包裹
     * @param response LLM 响应文本
     * @return 字符串列表，解析失败返回空列表
     */
    private List<String> parseStringArray(String response) {
        if (response == null || response.isBlank()) {
            return new ArrayList<>();
        }
        String trimmed = response.trim();
        // 兼容 ```json ... ``` 代码块包裹
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n');
            int end = trimmed.lastIndexOf("```");
            if (start > 0 && end > start) {
                trimmed = trimmed.substring(start + 1, end).trim();
            }
        }
        try {
            return JSON.parseArray(trimmed, String.class);
        } catch (Exception e) {
            logger.error("parseStringArray fail, response={}", response, e);
            return new ArrayList<>();
        }
    }

    /**
     * 在图谱中按实体名称模糊匹配（直接走 Neo4j）
     * @param graphId 图谱 id
     * @param name 实体名称
     * @param matchedEntityIds 命中实体 id 集合
     */
    private void matchEntities(Long graphId, String name, Set<Long> matchedEntityIds) {
        List<KgEntity> entities = graphStore.queryEntityByName(graphId, name);
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }
        int limit = Math.min(entities.size(), MAX_ENTITY_HIT);
        for (int i = 0; i < limit; i++) {
            matchedEntityIds.add(entities.get(i).getId());
        }
    }

    /**
     * 对命中的实体扩展 1 跳子图并生成三元组文本
     * @param entityIds 实体 id 集合
     * @param triplets 三元组文本输出列表
     */
    private void buildTriplets(Set<Long> entityIds, List<String> triplets) {
        Set<String> dedup = new HashSet<>();
        for (Long entityId : entityIds) {
            Map<String, Object> subgraph = graphStore.querySubgraph(entityId, 2);
            List<KgEntity> nodes = (List<KgEntity>) subgraph.get("nodes");
            List<RelationEdge> edges = (List<RelationEdge>) subgraph.get("edges");
            if (CollectionUtil.isEmpty(nodes)) {
                continue;
            }
            Map<Long, KgEntity> nodeMap = new HashMap<>();
            for (KgEntity node : nodes) {
                nodeMap.put(node.getId(), node);
            }
            if (CollectionUtil.isNotEmpty(edges)) {
                for (RelationEdge edge : edges) {
                    KgEntity head = nodeMap.get(edge.getHeadEntityId());
                    KgEntity tail = nodeMap.get(edge.getTailEntityId());
                    if (head == null || tail == null) {
                        continue;
                    }
                    String triplet = formatTriplet(head.getName(), edge.getRelationType(), tail.getName());
                    if (dedup.add(triplet)) {
                        triplets.add(triplet);
                    }
                    if (triplets.size() >= MAX_TRIPLETS) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * 格式化三元组文本
     * @param head 头实体名称
     * @param relation 关系类型
     * @param tail 尾实体名称
     * @return 三元组文本
     */
    private String formatTriplet(String head, String relation, String tail) {
        String rel = relation == null ? "相关" : relation;
        return head + " -[" + rel + "]-> " + tail;
    }

}
