package com.spark.kb.retrieve;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.llm.result.RetrieveResult;
import com.spark.common.constant.ESIndexName;
import com.spark.common.kb.IKbRetriever;
import com.spark.config.es.ESDataScopeUtil;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.model.RerankModel;
import com.spark.common.enums.ModelUsageSceneEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.ModelUsageSceneHolder;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/23 14:43
 * 知识库召回服务
 */
@Service
public class KbRetrieve implements IKbRetriever {
    private final static Logger logger = LoggerFactory.getLogger(KbRetrieve.class);
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private ElasticsearchClient client;
    /**
     * 召回数量数量
     */
    private final int topK = 20;
    /**
     * 余弦相似度
     * 高度匹配（相同语义）	0.8 ~ 1.0
     * 中等相似	0.6 ~ 0.8
     * 边缘相关	0.4 ~ 0.6
     * 不相关（可过滤）	< 0.3 ~ 0.4
     */
    private final float similarity = 0.5f;
    /**
     * 集群环境下从各节点中提取N条进行排序
     */
    private final int numCandidates = 200;
    /**
     * rrf参数 每个子查询最多取前
     */
    private final long rankWindowSize = 10L;
    /**
     * rrf参数 融合参数 默认的60
     */
    private final Long rankConstant = 30L;

    /**
     * 获取向量模型
     * @return
     */
    private EmbeddingModel getEmbeddingModel() {
        return modelFactory.getDefaultEmbeddingModel();
    }

    /**
     * 根据向量模型id获取向量模型
     * @param vectorModelId 向量模型id
     * @return
     */
    private EmbeddingModel getEmbeddingModel(Long vectorModelId) {
        return modelFactory.getEmbeddingModel(vectorModelId);
    }

    /**
     * 召回
     * Small-to-Big 检索
     * @param query 查询文本
     * @param kbIds 知识库ID列表
     * @param knowledge 知识库配置
     * @return 检索结果（含给LLM的文本和给前端的参考文档）
     */
    @Override
    public RetrieveResult retrieve(String query, List<Long> kbIds, KnowledgeResult knowledge) {
        if (StringUtil.isBlank(query) || CollectionUtil.isEmpty(kbIds) || knowledge == null) {
            RetrieveResult empty = new RetrieveResult();
            empty.setContents(new ArrayList<>());
            empty.setReferences(new ArrayList<>());
            return empty;
        }
        RetrieveResult qaResult = this.retrieveQA(query, kbIds, knowledge);
        if (CollectionUtil.isNotEmpty(qaResult.getContents())) {
            logger.info("retrieve qa hit, size={}", qaResult.getContents().size());
            return qaResult;
        }
        RetrieveResult vectorResult = retrieveByVector(query, kbIds, knowledge);
        return rerankResult(query, vectorResult, knowledge);
    }

    /**
     * 对向量召回结果进行重排序
     * 调用排序模型对 contents 重排，失败时回退原始顺序
     * @param query 查询文本
     * @param result 向量召回结果
     * @param knowledge 知识库配置
     * @return 重排后的结果
     */
    private RetrieveResult rerankResult(String query, RetrieveResult result, KnowledgeResult knowledge) {
        if (result == null || CollectionUtil.isEmpty(result.getContents()) || knowledge == null) {
            logger.error("result is null or knowledge is null");
            return result;
        }
        Long rerankModelId = knowledge.getRerankModelId();
        if (rerankModelId == null) {
            logger.error("rerankModelId is null");
            return result;
        }
        List<String> contents = result.getContents();
        int topN = knowledge.getRetrieveTopK() == null ? topK : knowledge.getRetrieveTopK();
        if (contents.size() <= 1) {
            logger.info("contents size={}", contents.size());
            return result;
        }
        try {
            ModelUsageSceneHolder.push(ModelUsageSceneEnum.RETRIEVE_RERANK);
            RerankModel rerankModel = modelFactory.getRerankModel(rerankModelId);
            if (rerankModel == null) {
                logger.warn("rerankModel is null, rerankModelId={}", rerankModelId);
                return result;
            }
            List<RerankModel.RerankResult> rerankResults = rerankModel.rerank(query, contents, Math.min(topN, contents.size()));
            if (CollectionUtil.isEmpty(rerankResults)) {
                logger.warn("rerank returns empty, fallback to original order, rerankModelId={}", rerankModelId);
                return result;
            }
            List<String> reranked = new ArrayList<>();
            for (RerankModel.RerankResult rr : rerankResults) {
                int idx = rr.getIndex();
                if (idx >= 0 && idx < contents.size()) {
                    reranked.add(contents.get(idx));
                }
            }
            if (reranked.isEmpty()) {
                return result;
            }
            result.setContents(reranked);
            logger.info("rerank success, originalSize={}, rerankedSize={}", contents.size(), reranked.size());
        } catch (Exception e) {
            logger.error("rerank error, fallback to original order, rerankModelId={}", rerankModelId, e);
        } finally {
            ModelUsageSceneHolder.pop();
        }
        return result;
    }

    /**
     * 召回QA
     * @param query 查询文本
     * @param kbIds 知识库ID列表
     * @param knowledge 知识库配置
     * @return 检索结果（contents为answer列表，references含docId）
     */
    private RetrieveResult retrieveQA(String query, List<Long> kbIds, KnowledgeResult knowledge) {
        logger.info("retrieveQa query={}, kbIds={}", query, kbIds);
        RetrieveResult result = new RetrieveResult();
        result.setContents(new ArrayList<>());
        result.setReferences(new ArrayList<>());
        try {
            ModelUsageSceneHolder.push(ModelUsageSceneEnum.RETRIEVE_VECTOR);
            int retrieveTopK = knowledge.getRetrieveTopK() == null ? topK : knowledge.getRetrieveTopK();
            float minSimilarity = knowledge.getMinSimilarity() == null ? similarity : knowledge.getMinSimilarity().floatValue();
            EmbeddingModel embeddingModel = getEmbeddingModel(knowledge.getVectorModelId());
            Response<Embedding> embedResp = embeddingModel.embed(query);
            Embedding embedding = embedResp.content();
            Query filter = this.kbIdFilter(kbIds);
            SearchRequest knnRequest = SearchRequest.of(b -> b
                    .index(ESIndexName.DOCUMENT_QA_VECTOR_INDEX_NAME)
                    .knn(k -> k
                            .field("questionEmbedding")
                            .queryVector(embedding.vectorAsList())
                            .k(retrieveTopK)
                            .numCandidates(numCandidates)
                            .similarity(minSimilarity)
                            .filter(filter))
                    .size(retrieveTopK)
            );
            SearchResponse<Map> response = client.search(knnRequest, Map.class);
            List<String> contents = new ArrayList<>();
            Set<Long> docIds = new HashSet<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map map = hit.source();
                if (map == null) {
                    continue;
                }
                String answer = (String) map.get("answer");
                if (StringUtil.isBlank(answer)) {
                    continue;
                }
                contents.add(answer);
                Long docId = ((Number) map.get("docId")).longValue();
                docIds.add(docId);
            }
            result.setContents(contents);
            result.setReferences(new ArrayList<>(docIds));
        } catch (Exception e) {
            logger.error("retrieveQa error", e);
        } finally {
            ModelUsageSceneHolder.pop();
        }
        return result;
    }

    /**
     * 向量+BM25混合召回（RRF融合），按chunkId聚合后取父块文本
     * @param query 查询文本
     * @param kbIds 知识库ID列表
     * @param knowledge 知识库配置
     * @return 检索结果（contents为父块文本列表，references含docId）
     */
    private RetrieveResult retrieveByVector(String query, List<Long> kbIds, KnowledgeResult knowledge) {
        RetrieveResult result = new RetrieveResult();
        result.setContents(new ArrayList<>());
        result.setReferences(new ArrayList<>());
        try {
            ModelUsageSceneHolder.push(ModelUsageSceneEnum.RETRIEVE_VECTOR);
            EmbeddingModel embeddingModel = getEmbeddingModel(knowledge.getVectorModelId());
            Response<Embedding> embedResp = embeddingModel.embed(query);
            Embedding embedding = embedResp.content();
            Query filter = this.kbIdFilter(kbIds);
            Integer retrieveTopK = knowledge.getRetrieveTopK() == null ? topK : knowledge.getRetrieveTopK();
            float minSimilarity = knowledge.getMinSimilarity() == null ? similarity : knowledge.getMinSimilarity().floatValue();
            // 1.执行BM25查询
            SearchRequest bm25Request = SearchRequest.of(b -> b
                    .index(ESIndexName.DOCUMENT_VECTOR_INDEX_NAME)
                    .query(q -> q.bool(bb -> bb
                            .must(m -> m.match(mm -> mm.field("content").query(query)))
                            .filter(filter)))
                    .size(Math.toIntExact(rankWindowSize))
            );
            SearchResponse<Map> bm25Response = client.search(bm25Request, Map.class);
            List<Hit<Map>>  bm25Hits = bm25Response.hits().hits();
            // 2.执行 KNN 查询
            SearchRequest knnRequest = SearchRequest.of(b -> b
                    .index(ESIndexName.DOCUMENT_VECTOR_INDEX_NAME)
                    .knn(k -> k
                            .field("embedding")
                            .queryVector(embedding.vectorAsList())
                            .k(retrieveTopK)
                            .numCandidates(numCandidates)
                            .similarity(minSimilarity)
                            .filter(filter))
                    .size(Math.toIntExact(rankWindowSize))
            );
            SearchResponse<Map> knnResponse = client.search(knnRequest, Map.class);
            List<Hit<Map>> knnHits = knnResponse.hits().hits();
            // 3.构建排名映射：sumChunkId -> rank (从1开始)
            Map<String, Integer> bm25Ranks = new HashMap<>();
            for (int i = 0; i < bm25Hits.size(); i++) {
                bm25Ranks.put(bm25Hits.get(i).id(), i + 1);
            }
            Map<String, Integer> knnRanks = new HashMap<>();
            for (int i = 0; i < knnHits.size(); i++) {
                knnRanks.put(knnHits.get(i).id(), i + 1);
            }
            // 4.收集所有sumChunkId
            Set<String> sumChunkIds = new HashSet<>();
            sumChunkIds.addAll(bm25Ranks.keySet());
            sumChunkIds.addAll(knnRanks.keySet());
            // 5.计算RRF分数
            Map<Long, Double> rrfScores = new HashMap<>();
            Set<Long> docIds = new HashSet<>();
            for (String sumChunkId : sumChunkIds) {
                double score = 0.0;
                if (bm25Ranks.containsKey(sumChunkId)) {
                    score += 1.0 / (rankConstant + bm25Ranks.get(sumChunkId));
                }
                if (knnRanks.containsKey(sumChunkId)) {
                    score += 1.0 / (rankConstant + knnRanks.get(sumChunkId));
                }
                Hit<Map> hit = bm25Ranks.containsKey(sumChunkId) ? findHit(bm25Hits, sumChunkId) : findHit(knnHits, sumChunkId);
                if (hit != null && hit.source() != null && hit.source().get("chunkId") != null) {
                    Long chunkId = ((Number) hit.source().get("chunkId")).longValue();
                    rrfScores.merge(chunkId, score, Math::max);
                    Long docId = ((Number) hit.source().get("docId")).longValue();
                    docIds.add(docId);
                }
            }
            List<Long> chunkIds = rrfScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList();
            if (CollectionUtil.isEmpty(chunkIds)) {
                return result;
            }
            List<String> contents = this.queryChunkContents(chunkIds);
            result.setContents(contents);
            result.setReferences(new ArrayList<>(docIds));
            return result;
        } catch (Exception e) {
            logger.error("retrieveByVector error", e);
            return result;
        } finally {
            ModelUsageSceneHolder.pop();
        }
    }

    /**
     * 查父块文本，合并相邻父块
     * @param parentIds 父块ID列表
     * @return 父块文本列表
     */
    private List<String> queryChunkContents(List<Long> parentIds) {
        try {
            List<FieldValue> values = parentIds.stream()
                    .map(id -> FieldValue.of(id.longValue()))
                    .collect(Collectors.toList());
            Query scopeQuery = ESDataScopeUtil.scopeQuery();
            SearchRequest request = SearchRequest.of(b -> b
                    .index(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME)
                    .query(q -> q.bool(bb -> {
                        bb.must(m -> m.terms(t -> t.field("id").terms(tv -> tv.value(values))));
                        bb.must(scopeQuery);
                        return bb;
                    }))
                    .size(parentIds.size())
            );
            SearchResponse<Map> response = client.search(request, Map.class);
            List<Map<String, Object>> parents = response.hits().hits().stream()
                    .map(hit -> (Map<String, Object>) hit.source())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (CollectionUtil.isEmpty(parents)) {
                return new ArrayList<>();
            }
            parents.sort((a, b) -> {
                Long docA = ((Number) a.get("docId")).longValue();
                Long docB = ((Number) b.get("docId")).longValue();
                if (!docA.equals(docB)) {
                    return docA.compareTo(docB);
                }
                Integer idxA = (Integer) a.get("chunkIndex");
                Integer idxB = (Integer) b.get("chunkIndex");
                return idxA.compareTo(idxB);
            });
            List<String> merged = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            Long currentDocId = null;
            int lastIndex = -1;
            for (Map<String, Object> parent : parents) {
                Long docId = ((Number) parent.get("docId")).longValue();
                int chunkIndex = (Integer) parent.get("chunkIndex");
                String content = (String) parent.get("content");
                if (currentDocId == null) {
                    currentDocId = docId;
                    lastIndex = chunkIndex;
                    current.append(content);
                } else if (docId.equals(currentDocId) && chunkIndex == lastIndex + 1) {
                    current.append("\n").append(content);
                    lastIndex = chunkIndex;
                } else {
                    merged.add(current.toString());
                    currentDocId = docId;
                    lastIndex = chunkIndex;
                    current = new StringBuilder(content);
                }
            }
            if (current.length() > 0) {
                merged.add(current.toString());
            }
            return merged;
        } catch (Exception e) {
            logger.error("queryParentContents error", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从hit列表中查找指定id的hit
     * @param hits hit列表
     * @param id 文档id
     * @return hit
     */
    private Hit<Map> findHit(List<Hit<Map>> hits, String id) {
        for (Hit<Map> hit : hits) {
            if (hit.id().equals(id)) {
                return hit;
            }
        }
        return null;
    }

    /**
     * 构造知识库召回过滤条件，恒含租户与数据权限
     * @param kbIds 知识库ID列表
     * @return 过滤Query
     */
    private Query kbIdFilter(List<Long> kbIds) {
        List<FieldValue> values = kbIds.stream()
                .map(id -> FieldValue.of(id.longValue()))
                .collect(Collectors.toList());
        Query scopeQuery = ESDataScopeUtil.scopeQuery();
        return Query.of(q -> q.bool(b -> {
            b.must(scopeQuery);
            b.must(m -> m.terms(t -> t.field("prtId").terms(tv -> tv.value(values))));
            return b;
        }));
    }
}
