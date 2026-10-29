package com.spark.kg.store;

import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.constant.ESIndexName;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.config.es.ESDataScope;
import com.spark.dao.kg.KgEntityDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.llm.model.ModelFactory;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.llm.ModelUsageSceneHolder;
import com.spark.common.utils.StringUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
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
 * 知识图谱实体向量存储
 */
@Component
public class KgVectorStore {
    private final static Logger logger = LoggerFactory.getLogger(KgVectorStore.class);
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private KgEntityDao kgEntityDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    /**
     * 语义召回候选数
     */
    private static final int NUM_CANDIDATES = 200;
    /**
     * 语义召回最低余弦相似度
     */
    private static final float MIN_SIMILARITY = 0.5f;

    /**
     * 向量化实体写入ES
     * @param entity 实体
     */
    public void vectorizeEntity(KgEntity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        // 回查完整实体，调用方可能只传精简对象，审计字段以库中为准
        KgEntityQuery entityQuery = new KgEntityQuery();
        entityQuery.setId(entity.getId());
        KgEntityResult entityResult = kgEntityDao.queryKgEntity(entityQuery);
        if (entityResult == null) {
            return;
        }
        // 实体表无部门列，部门归属继承所属图谱
        Long deptId = null;
        KgGraphQuery graphQuery = new KgGraphQuery();
        graphQuery.setId(entityResult.getGraphId());
        KgGraphResult graphResult = kgGraphDao.queryKgGraph(graphQuery);
        if (graphResult != null) {
            deptId = graphResult.getDeptId();
        }
        try {
            ModelUsageSceneHolder.push(ModelUsageSceneEnum.KG_ENTITY_VECTOR);
            String text = buildEmbeddingText(entityResult.getName(), entityResult.getDescription());
            if (StringUtil.isBlank(text)) {
                return;
            }
            EmbeddingModel embeddingModel = modelFactory.getDefaultEmbeddingModel();
            Response<Embedding> embedResp = embeddingModel.embed(text);
            Map<String, Object> data = new HashMap<>();
            data.put("id", entityResult.getId());
            data.put("tenantId", entityResult.getTenantId());
            data.put("deptId", deptId);
            data.put("createdBy", entityResult.getCreatedBy());
            data.put("createdDt", entityResult.getCreatedDt().getTime());
            data.put("updatedBy", entityResult.getUpdatedBy());
            data.put("updatedDt", entityResult.getUpdatedDt().getTime());
            data.put("graphId", entityResult.getGraphId());
            data.put("name", entityResult.getName());
            data.put("type", entityResult.getType());
            data.put("description", entityResult.getDescription());
            data.put("embedding", embedResp.content().vectorAsList());
            String source = JsonUtil.toString(data);
            IndexQuery query = new IndexQueryBuilder()
                    .withId(entityResult.getId().toString())
                    .withSource(source)
                    .build();
            elasticsearchOperations.index(query, IndexCoordinates.of(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME));
        } catch (Exception e) {
            logger.error("vectorizeEntity error, entityId={}", entity.getId(), e);
        } finally {
            ModelUsageSceneHolder.pop();
        }
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
            elasticsearchOperations.delete(entityId.toString(), IndexCoordinates.of(ESIndexName.KG_ENTITY_VECTOR_INDEX_NAME));
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
            ModelUsageSceneHolder.push(ModelUsageSceneEnum.RETRIEVE_VECTOR);
            EmbeddingModel embeddingModel = modelFactory.getDefaultEmbeddingModel();
            Response<Embedding> embedResp = embeddingModel.embed(question);
            Embedding embedding = embedResp.content();
            Query filter = this.graphIdFilter(graphIds);
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
        } finally {
            ModelUsageSceneHolder.pop();
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
     * 构造图谱范围过滤条件，恒含租户与数据权限
     * @param graphIds 图谱ID列表
     * @return 过滤Query
     */
    private Query graphIdFilter(List<Long> graphIds) {
        List<FieldValue> values = graphIds.stream()
                .map(id -> FieldValue.of(id.longValue()))
                .collect(Collectors.toList());
        Query scopeQuery = ESDataScope.scopeQuery();
        return Query.of(q -> q.bool(b -> {
            b.must(m -> m.terms(t -> t.field("graphId").terms(tv -> tv.value(values))));
            b.must(scopeQuery);
            return b;
        }));
    }
}
