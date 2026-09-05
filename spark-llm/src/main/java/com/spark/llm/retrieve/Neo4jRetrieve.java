package com.spark.llm.retrieve;

import com.alibaba.fastjson2.JSON;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.entity.RelationEdge;
import com.spark.config.redis.RedisService;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.store.KgEntityVectorService;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.prompt.PromptTemplateLoader;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.EncryptUtil;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/23 14:44
 * neo4j召回服务：关键词与语义向量双路召回，RRF 融合后扩展子图生成三元组
 */
@Service
public class Neo4jRetrieve {
    private final static Logger logger = LoggerFactory.getLogger(Neo4jRetrieve.class);
    /**
     * 单个关键词命中的最大实体数
     */
    private static final int MAX_ENTITY_HIT = 5;
    /**
     * 语义向量召回实体数
     */
    private static final int MAX_SEMANTIC_HIT = 10;
    /**
     * 融合后参与子图扩展的最大实体数
     */
    private static final int MAX_FUSED_ENTITY = 8;
    /**
     * 单跳子图扩展的最大返回三元组数
     */
    private static final int MAX_TRIPLETS = 20;
    /**
     * RRF 融合参数
     */
    private static final double RRF_CONSTANT = 60.0;
    /**
     * 检索缓存key前缀
     */
    private static final String CACHE_PREFIX = ObjectCacheKey.KG_RETRIEVE_CACHE;
    /**
     * 检索缓存时长
     */
    private static final long CACHE_TTL_SECONDS = 30 * 60L;
    @Autowired
    private Neo4jGraphStore graphStore;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private KgEntityVectorService entityVectorService;
    @Autowired
    private RedisService redisService;

    /**
     * 按问题检索知识图谱子图，返回三元组文本
     * 检索流程：缓存查询 -> 关键词召回 + 语义召回 -> RRF融合 -> 子图扩展 -> 缓存写入
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
        // 1. 缓存命中直接返回
        List<String> cached = this.getCachedResult(question, graphIds);
        if (cached != null) {
            logger.info("retrieve cache hit, question={}", question);
            result.setData(cached);
            result.setCode(ResultData.OK);
            return result;
        }
        List<String> triplets = new ArrayList<>();
        try {
            // 2. 关键词路径：LLM 抽取实体名 -> Neo4j 全文索引匹配
            Set<Long> keywordEntityIds = new LinkedHashSet<>();
            List<String> entityNames = this.extractEntitiesByLlm(question);
            if (CollectionUtil.isEmpty(entityNames)) {
                // LLM 抽取失败时回退：用整句做 CONTAINS
                logger.warn("LLM 抽取实体为空,回退整句匹配, question={}", question);
                entityNames = Collections.singletonList(question.trim());
            }
            for (Long graphId : graphIds) {
                for (String name : entityNames) {
                    this.matchEntities(graphId, name, keywordEntityIds);
                }
            }
            // 3. 语义路径：问题向量 -> ES KNN 召回实体
            List<Long> semanticEntityIds = new ArrayList<>();
            try {
                List<KgEntity> semanticEntities = entityVectorService.semanticSearch(question, graphIds, MAX_SEMANTIC_HIT);
                for (KgEntity entity : semanticEntities) {
                    semanticEntityIds.add(entity.getId());
                }
            } catch (Exception e) {
                logger.warn("semantic search fail, fallback to keyword only, question={}", question, e);
            }
            // 4. RRF 融合两路召回实体
            List<Long> fusedEntityIds = this.rrfFuse(keywordEntityIds, semanticEntityIds);
            if (fusedEntityIds.isEmpty()) {
                result.setData(triplets);
                result.setCode(ResultData.OK);
                return result;
            }
            // 5. 对融合命中的实体扩展子图，生成三元组
            this.buildTriplets(fusedEntityIds, triplets);
        } catch (Exception e) {
            logger.error("retrieve error, question={}", question, e);
        }
        // 6. 写入缓存（含空结果，防止穿透）
        this.cacheResult(question, graphIds, triplets);
        result.setData(triplets);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * RRF 融合两路召回结果
     * @param keywordEntityIds 关键词召回实体
     * @param semanticEntityIds 语义召回实体
     * @return 融合排序后的实体 id 列表
     */
    private List<Long> rrfFuse(Collection<Long> keywordEntityIds, List<Long> semanticEntityIds) {
        Map<Long, Double> scoreMap = new HashMap<>();
        int rank = 1;
        for (Long entityId : keywordEntityIds) {
            scoreMap.merge(entityId, 1.0 / (RRF_CONSTANT + rank), Double::sum);
            rank++;
        }
        rank = 1;
        for (Long entityId : semanticEntityIds) {
            scoreMap.merge(entityId, 1.0 / (RRF_CONSTANT + rank), Double::sum);
            rank++;
        }
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(MAX_FUSED_ENTITY)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 构建检索缓存key
     * @param question 用户问题
     * @param graphIds 图谱 id 列表
     * @return 缓存key
     */
    private String buildCacheKey(String question, List<Long> graphIds) {
        List<Long> sortedIds = new ArrayList<>(graphIds);
        Collections.sort(sortedIds);
        return CACHE_PREFIX + EncryptUtil.md5(question.trim() + "|" + sortedIds);
    }

    /**
     * 读取缓存的检索结果
     * @param question 用户问题
     * @param graphIds 图谱 id 列表
     * @return 三元组列表
     */
    private List<String> getCachedResult(String question, List<Long> graphIds) {
        try {
            String cached = redisService.getValue(buildCacheKey(question, graphIds));
            if (StringUtil.isNotBlank(cached)) {
                return JSON.parseArray(cached, String.class);
            }
        } catch (Exception e) {
            logger.warn("getCachedResult fail, question={}", question, e);
        }
        return null;
    }

    /**
     * 写入检索缓存
     * @param question 用户问题
     * @param graphIds 图谱 id 列表
     * @param triplets 三元组列表
     */
    private void cacheResult(String question, List<Long> graphIds, List<String> triplets) {
        try {
            redisService.setStr(buildCacheKey(question, graphIds), JSON.toJSONString(triplets), CACHE_TTL_SECONDS);
        } catch (Exception e) {
            logger.warn("cacheResult fail, question={}", question, e);
        }
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
    private void buildTriplets(Collection<Long> entityIds, List<String> triplets) {
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
