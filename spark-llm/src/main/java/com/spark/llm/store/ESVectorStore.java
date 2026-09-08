package com.spark.llm.store;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.constant.ESIndexName;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.common.utils.TextChunkUtil;
import com.spark.prompt.PromptTemplateLoader;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 * 知识库向量存储（Small-to-Big 父子分块）
 */
@Component
public class ESVectorStore {
    private final static Logger logger = LoggerFactory.getLogger(ESVectorStore.class);
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    /**
     * 图谱抽取默认分块大小（字符）
     */
    private static final int DEFAULT_CHUNK_SIZE = 800;
    /**
     * 图谱抽取默认分块重叠（字符）
     */
    private static final int DEFAULT_CHUNK_OVERLAP = 100;

    /**
     * 根据向量模型id获取向量模型
     * @param vectorModelId 向量模型id
     * @return
     */
    private EmbeddingModel getEmbeddingModel(Long vectorModelId) {
        return modelFactory.getEmbeddingModel(vectorModelId);
    }

    /**
     * 获取聊天模型
     * @return
     */
    private ChatModel getChatModel() {
        return modelFactory.getDefaultNoThinkChatModel();
    }

    /**
     * 文档分块入库
     * 采用Small-to-Big 父子分块策略中的父分块阶段，仅写入父块到 chunk 索引
     * 分块参数按文档归属类型读取：KNOWLEDGE 读知识库配置，KG_GRAPH 读知识图谱配置
     * @param document 文档
     * @param content 文档内容
     */
    public void addChunk(DocumentResult document, String content) {
        if (StringUtil.isBlank(content) || document == null || document.getPath() == null) {
            logger.warn("addChunk skip, content blank or document null");
            return;
        }
        Integer documentType = document.getDocumentType();
        Long prtId = document.getPrtId();
        Long docId = document.getId();
        int chunkSize;
        int overlap;
        if (ObjectTypeEnum.KG_GRAPH.getValue().equals(documentType)) {
            KgGraphQuery kgQuery = new KgGraphQuery();
            kgQuery.setId(prtId);
            KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(kgQuery);
            if (kgGraphResult == null) {
                logger.error("addChunk kg graph result is null, docId={}, prtId={}", docId, prtId);
                return;
            }
            chunkSize = kgGraphResult.getChunkSize() != null && kgGraphResult.getChunkSize() > 0
                    ? kgGraphResult.getChunkSize() : DEFAULT_CHUNK_SIZE;
            overlap = kgGraphResult.getOverlap() != null && kgGraphResult.getOverlap() >= 0
                    ? kgGraphResult.getOverlap() : DEFAULT_CHUNK_OVERLAP;
        } else {
            KnowledgeQuery kbQuery = new KnowledgeQuery();
            kbQuery.setId(prtId);
            KnowledgeResult knowledgeResult = knowledgeDao.queryKnowledge(kbQuery);
            if (knowledgeResult == null) {
                logger.error("addChunk knowledge result is null, docId={}, prtId={}", docId, prtId);
                return;
            }
            chunkSize = knowledgeResult.getParentChunkSize();
            overlap = knowledgeResult.getParentOverlap();
            // 使用知识库配置的分块策略
            String chunkStrategy = knowledgeResult.getChunkStrategy();
            List<String> parentChunks = TextChunkUtil.handleChunk(content, chunkSize, overlap, chunkStrategy);
            if (CollectionUtil.isEmpty(parentChunks)) {
                logger.warn("addChunk skip, parent chunks empty, docId={}, prtId={}", docId, prtId);
                return;
            }
            saveChunksToES(parentChunks, document);
            logger.info("addChunk success, prtId={}, docId={}, parentChunkSize={}, strategy={}", prtId, docId, parentChunks.size(), chunkStrategy);
            return;
        }
        List<String> parentChunks = TextChunkUtil.handleChunk(content, chunkSize, overlap);
        if (CollectionUtil.isEmpty(parentChunks)) {
            logger.warn("addChunk skip, parent chunks empty, docId={}, prtId={}", docId, prtId);
            return;
        }
        saveChunksToES(parentChunks, document);
        logger.info("addChunk success, prtId={}, docId={}, parentChunkSize={}", prtId, docId, parentChunks.size());
    }

    /**
     * 保存分块到ES
     * @param parentChunks 分块列表
     * @param document 文档I
     */
    private void saveChunksToES(List<String> parentChunks, DocumentResult document) {
        // 把历史的分片删掉
        this.deleteChunkByDocId(document.getId());
        for (int parentIndex = 0; parentIndex < parentChunks.size(); parentIndex++) {
            String parentContent = parentChunks.get(parentIndex);
            Long chunkId = System.currentTimeMillis();
            Map<String, Object> chunkData = new HashMap<>();
            chunkData.put("id", chunkId);
            chunkData.put("tenantId", document.getTenantId());
            chunkData.put("prtId", document.getPrtId());
            chunkData.put("documentType", document.getDocumentType());
            chunkData.put("docId", document.getId());
            chunkData.put("chunkIndex", parentIndex + 1);
            chunkData.put("content", parentContent);
            IndexQuery parentQuery = new IndexQueryBuilder()
                    .withId(chunkId.toString())
                    .withSource(JsonUtil.toString(chunkData))
                    .build();
            elasticsearchOperations.index(parentQuery, IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
        }
    }

    /**
     * 文档向量化入库
     * 读取已落地的父块，做子分块 + embedding 写入向量索引，按需生成 QA
     * @param document 文档
     */
    public void addVector(DocumentResult document) {
        if (document == null || document.getId() == null || document.getPrtId() == null) {
            logger.warn("addVector skip, docId or prtId null");
            return;
        }
        Long prtId = document.getPrtId();
        Long docId = document.getId();
        Integer documentType = document.getDocumentType();
        KnowledgeQuery kbQuery = new KnowledgeQuery();
        kbQuery.setId(prtId);
        KnowledgeResult knowledgeResult = knowledgeDao.queryKnowledge(kbQuery);
        if (knowledgeResult == null) {
            logger.error("addVector knowledge result is null, docId={}, prtId={}", docId, prtId);
            return;
        }
        List<Map<String, Object>> parentChunks = this.queryParentChunks(docId);
        if (CollectionUtil.isEmpty(parentChunks)) {
            logger.warn("addVector skip, parent chunks empty, docId={}, prtId={}", docId, prtId);
            return;
        }
        EmbeddingModel embeddingModel = getEmbeddingModel(knowledgeResult.getVectorModelId());
        // 把历史的向量、QA删掉
        this.deleteVectorByDocId(docId);
        for (Map<String, Object> parentChunk : parentChunks) {
            Long chunkId = ((Number) parentChunk.get("id")).longValue();
            String parentContent = (String) parentChunk.get("content");
            if (StringUtil.isBlank(parentContent)) {
                continue;
            }
            List<String> childChunks = TextChunkUtil.handleChunk(parentContent, knowledgeResult.getChildChunkSize(), knowledgeResult.getChildOverlap());
            if (CollectionUtil.isEmpty(childChunks)) {
                continue;
            }
            for (int childIndex = 0; childIndex < childChunks.size(); childIndex++) {
                String childContent = childChunks.get(childIndex);
                Long childId = System.currentTimeMillis();
                Response<Embedding> embedResp = embeddingModel.embed(childContent);
                Map<String, Object> childData = new HashMap<>();
                childData.put("id", childId);
                childData.put("tenantId", document.getTenantId());
                childData.put("chunkId", chunkId);
                childData.put("prtId", prtId);
                childData.put("documentType", documentType);
                childData.put("docId", docId);
                childData.put("chunkIndex", childIndex + 1);
                childData.put("content", childContent);
                childData.put("embedding", embedResp.content().vectorAsList());
                IndexQuery childQuery = new IndexQueryBuilder()
                        .withId(childId.toString())
                        .withSource(JsonUtil.toString(childData))
                        .build();
                elasticsearchOperations.index(childQuery, IndexCoordinates.of(ESIndexName.DOCUMENT_VECTOR_INDEX_NAME));
            }
            if (StatusEnum.NORMAL.getValue().equals(knowledgeResult.getEnableQa())) {
                addQA(document, chunkId, parentContent, knowledgeResult.getVectorModelId());
            }
        }
        logger.info("addVector success, prtId={}, docId={}, parentChunkSize={}", prtId, docId, parentChunks.size());
    }

    /**
     * 查询文档的父块列表
     * @param docId 文档ID
     * @return 父块列表
     */
    private List<Map<String, Object>> queryParentChunks(Long docId) {
        try {
            SearchRequest request = SearchRequest.of(b -> b
                    .index(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME)
                    .query(q -> q.term(t -> t.field("docId").value(docId)))
                    .size(10000));
            SearchResponse<Map> response = client.search(request, Map.class);
            List<Map<String, Object>> parents = response.hits().hits().stream()
                    .map(hit -> (Map<String, Object>) hit.source())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return parents;
        } catch (Exception e) {
            logger.error("queryParentChunks error, docId={}", docId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询文档的父块文本列表，供知识图谱任务复用分块结果
     * @param docId 文档ID
     * @return 父块文本列表
     */
    public List<String> queryParentChunkContents(Long docId) {
        List<Map<String, Object>> parentChunks = this.queryParentChunks(docId);
        if (CollectionUtil.isEmpty(parentChunks)) {
            return new ArrayList<>();
        }
        return parentChunks.stream()
                .map(m -> (String) m.get("content"))
                .filter(StringUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 入库QA
     * @param document 文档
     * @param chunkId 文档分块ID
     * @param parentContent 父块内容
     * @param vectorModelId 向量模型id
     */
    private void addQA(DocumentResult document, Long chunkId, String parentContent, Long vectorModelId) {
        try {
            Map<String, Object> variables = Map.of("document", parentContent);
            Prompt prompt = PromptTemplateLoader.create("prompts/qa-prompt.txt", variables);
            ChatModel chatModel = getChatModel();
            String chat = chatModel.chat(prompt.text());
            JSONArray objects = JSON.parseArray(chat);
            if (CollectionUtil.isEmpty(objects)) {
                return;
            }
            EmbeddingModel embeddingModel = getEmbeddingModel(vectorModelId);
            for (Object object : objects) {
                JSONObject jsonObject = (JSONObject) object;
                String question = jsonObject.getString("question");
                String answer = jsonObject.getString("answer");
                if (StringUtil.isBlank(question) || StringUtil.isBlank(answer)) {
                    continue;
                }
                Response<Embedding> qaResp = embeddingModel.embed(question);
                Long qaId = System.currentTimeMillis();
                Map<String, Object> qaData = new HashMap<>();
                qaData.put("id", qaId);
                qaData.put("tenantId", document.getTenantId());
                qaData.put("chunkId", chunkId);
                qaData.put("prtId", document.getPrtId());
                qaData.put("documentType", document.getDocumentType());
                qaData.put("docId", document.getId());
                qaData.put("question", question);
                qaData.put("answer", answer);
                qaData.put("questionEmbedding", qaResp.content().vectorAsList());
                IndexQuery qaQuery = new IndexQueryBuilder()
                        .withId(qaId.toString())
                        .withSource(JsonUtil.toString(qaData))
                        .build();
                elasticsearchOperations.index(qaQuery, IndexCoordinates.of(ESIndexName.DOCUMENT_QA_VECTOR_INDEX_NAME));
            }
        } catch (Exception e) {
            logger.error("indexQA error, chunkId={}", chunkId, e);
        }
    }

    /**
     * 根据文档ID删除历史的分片数据
     * @param docId 文档ID
     */
    private void deleteChunkByDocId(Long docId) {
        if (docId == null) {
            return;
        }
        try {
            Query docIdQuery = Query.of(q -> q.term(t -> t.field("docId").value(docId)));
            client.deleteByQuery(d -> d
                    .index(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME)
                    .query(docIdQuery)
                    .ignoreUnavailable(true)
                    .refresh(true));
            logger.info("deleteChunkByDocId success, docId={}", docId);
        } catch (Exception e) {
            logger.error("deleteChunkByDocId error, docId={}", docId, e);
        }
    }

    /**
     * 根据文档ID删除历史的向量、QA数据
     * @param docId 文档ID
     */
    private void deleteVectorByDocId(Long docId) {
        if (docId == null) {
            return;
        }
        try {
            Query docIdQuery = Query.of(q -> q.term(t -> t.field("docId").value(docId)));
            client.deleteByQuery(d -> d
                    .index(ESIndexName.DOCUMENT_VECTOR_INDEX_NAME)
                    .query(docIdQuery)
                    .ignoreUnavailable(true)
                    .refresh(true));
            client.deleteByQuery(d -> d
                    .index(ESIndexName.DOCUMENT_QA_VECTOR_INDEX_NAME)
                    .query(docIdQuery)
                    .ignoreUnavailable(true)
                    .refresh(true));
            logger.info("deleteVectorByDocId success, docId={}", docId);
        } catch (Exception e) {
            logger.error("deleteVectorByDocId error, docId={}", docId, e);
        }
    }
}
