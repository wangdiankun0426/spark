package com.spark.llm.store;

import com.spark.bean.kg.entity.KgEntity;
import com.spark.constant.ESIndexName;
import com.spark.llm.model.ModelFactory;
import com.spark.utils.CollectionUtil;
import com.spark.utils.JsonUtil;
import com.spark.utils.StringUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 16:00:00
 * 知识图谱实体向量索引服务，将实体描述向量化写入ES，支持语义检索定位入口实体
 */
@Component
public class KgEntityVectorService {
    private final static Logger logger = LoggerFactory.getLogger(KgEntityVectorService.class);
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private ModelFactory modelFactory;
    /**
     * 向量维度
     */
    private static final int VECTOR_DIMENSIONS = 1024;
    /**
     * 语义召回候选数
     */
    private static final int NUM_CANDIDATES = 200;
    /**
     * 语义召回最低余弦相似度
     */
    private static final float MIN_SIMILARITY = 0.5f;

    /**
     * 启动时创建实体向量索引
     */
    @PostConstruct
    public void initIndex() {
        try {
            boolean exists = client.indices()
                    .exists(e -> e.index(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME))
                    .value();
            if (exists) {
                logger.info("KgEntityVectorService index already exists");
                return;
            }
            CreateIndexRequest request = CreateIndexRequest.of(b -> b
                    .index(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME)
                    .mappings(m -> m
                            .properties("id", p -> p.long_(l -> l))
                            .properties("graphId", p -> p.long_(l -> l))
                            .properties("name", p -> p.keyword(k -> k))
                            .properties("type", p -> p.keyword(k -> k))
                            .properties("description", p -> p.text(t -> t))
                            .properties("embedding", p -> p.denseVector(d -> d
                                    .dims(VECTOR_DIMENSIONS)
                                    .index(true)
                                    .similarity("cosine")))));
            client.indices().create(request);
            logger.info("KgEntityVectorService index created successfully");
        } catch (Exception e) {
            logger.error("KgEntityVectorService initIndex error", e);
        }
    }

    /**
     * 向量化实体写入ES
     * @param entity 实体
     */
    public void vectorizeEntity(KgEntity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        try {
            String text = buildEmbeddingText(entity.getName(), entity.getDescription());
            if (StringUtil.isBlank(text)) {
                return;
            }
            EmbeddingModel embeddingModel = modelFactory.getDefaultEmbeddingModel();
            Response<Embedding> embedResp = embeddingModel.embed(text);
            Map<String, Object> data = new HashMap<>();
            data.put("id", entity.getId());
            data.put("graphId", entity.getGraphId());
            data.put("name", entity.getName());
            data.put("type", entity.getType());
            data.put("description", entity.getDescription());
            data.put("embedding", embedResp.content().vectorAsList());
            String source = JsonUtil.toString(data);
            IndexQuery query = new IndexQueryBuilder()
                    .withId(entity.getId().toString())
                    .withSource(source)
                    .build();
            elasticsearchOperations.index(query, IndexCoordinates.of(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME));
        } catch (Exception e) {
            logger.error("vectorizeEntity error, entityId={}", entity.getId(), e);
        }
    }

    /**
     * 批量向量化实体
     * @param entities 实体列表
     */
    public void vectorizeEntities(List<KgEntity> entities) {
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }
        for (KgEntity entity : entities) {
            this.vectorizeEntity(entity);
        }
        logger.info("vectorizeEntities success, count={}", entities.size());
    }

    /**
     * 删除单个实体的向量数据
     * @param entityId 实体ID
     */
    public void deleteEntityVector(Long entityId) {
        if (entityId == null) {
            return;
        }
        try {
            elasticsearchOperations.delete(entityId.toString(),
                    IndexCoordinates.of(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME));
        } catch (Exception e) {
            logger.error("deleteEntityVector error, entityId={}", entityId, e);
        }
    }

    /**
     * 按图谱ID删除全部实体向量数据
     * @param graphId 图谱ID
     */
    public void deleteByGraphId(Long graphId) {
        if (graphId == null) {
            return;
        }
        try {
            Query query = Query.of(q -> q.term(t -> t.field("graphId").value(graphId)));
            client.deleteByQuery(d -> d
                    .index(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME)
                    .query(query)
                    .ignoreUnavailable(true)
                    .refresh(true));
            logger.info("deleteByGraphId success, graphId={}", graphId);
        } catch (Exception e) {
            logger.error("deleteByGraphId error, graphId={}", graphId, e);
        }
    }

    /**
     * 语义检索实体
     * @param question 用户问题
     * @param graphIds 图谱ID列表
     * @param topK 召回数量
     * @return 实体列表
     */
    public List<KgEntity> semanticSearch(String question, List<Long> graphIds, int topK) {
        List<KgEntity> list = new ArrayList<>();
        if (StringUtil.isBlank(question)) {
            return list;
        }
        if (CollectionUtil.isEmpty(graphIds)) {
            return list;
        }
        if (topK <= 0) {
            return list;
        }
        try {
            EmbeddingModel embeddingModel = modelFactory.getDefaultEmbeddingModel();
            Response<Embedding> embedResp = embeddingModel.embed(question);
            Embedding embedding = embedResp.content();
            Query filter = graphIdFilter(graphIds);
            SearchRequest request = SearchRequest.of(b -> b
                    .index(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME)
                    .knn(k -> k
                            .field("embedding")
                            .queryVector(embedding.vectorAsList())
                            .k(topK)
                            .numCandidates(NUM_CANDIDATES)
                            .similarity(MIN_SIMILARITY)
                            .filter(filter))
                    .size(topK));
            SearchResponse<Map> response = client.search(request, Map.class);
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source == null || source.get("id") == null) {
                    continue;
                }
                KgEntity entity = new KgEntity();
                entity.setId(((Number) source.get("id")).longValue());
                entity.setGraphId(source.get("graphId") != null ? ((Number) source.get("graphId")).longValue() : null);
                entity.setName((String) source.get("name"));
                entity.setType((String) source.get("type"));
                entity.setDescription((String) source.get("description"));
                list.add(entity);
            }
            logger.info("semanticSearch success, question={}, hit={}", question, list.size());
        } catch (Exception e) {
            logger.error("semanticSearch error, question={}", question, e);
        }
        return list;
    }

    /**
     * 构建向量化文本
     * @param name 实体名称
     * @param description 实体描述
     * @return 向量化文本
     */
    private String buildEmbeddingText(String name, String description) {
        if (StringUtil.isNotBlank(name) && StringUtil.isNotBlank(description)) {
            return name + "：" + description;
        }
        return StringUtil.isNotBlank(name) ? name : description;
    }

    /**
     * 构造图谱ID过滤条件
     * @param graphIds 图谱ID列表
     * @return 过滤Query
     */
    private Query graphIdFilter(List<Long> graphIds) {
        List<FieldValue> values = graphIds.stream()
                .map(id -> FieldValue.of(id.longValue()))
                .collect(Collectors.toList());
        return Query.of(q -> q.terms(t -> t.field("graphId").terms(tv -> tv.value(values))));
    }
}
