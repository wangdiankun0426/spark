package com.spark.kb.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.entity.DocumentEvent;
import com.spark.bean.kb.query.DocumentEventQuery;
import com.spark.bean.kb.query.DocumentQuery;
import com.spark.bean.kb.query.KnowledgeQuery;
import com.spark.bean.kb.result.DocumentEventResult;
import com.spark.bean.kb.result.DocumentResult;
import com.spark.bean.kb.result.KnowledgeResult;
import com.spark.constant.ESIndexName;
import com.spark.dao.kb.DocumentDao;
import com.spark.dao.kb.DocumentEventDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.enums.DocumentEventStatusEnum;
import com.spark.enums.ErrorCodeEnum;
import com.spark.kb.service.IDocumentChunkService;
import com.spark.llm.store.ESVectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

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
 * @since 2026/7/16 13:26
 */
@Service
public class DocumentChunkServiceImpl implements IDocumentChunkService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentChunkServiceImpl.class);
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private DocumentEventDao documentEventDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private ESVectorStore esVectorStore;

    /**
     * 分页查询文档分片列表
     * @param query 查询参数
     * @return 文档分块结果
     */
    @Override
    public ResultData<PageResult<Map>> pageDocumentChunkList(DocumentQuery query) {
        ResultData<PageResult<Map>> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 检查索引是否存在
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
        if (!indexOps.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_ES_INDEX_NOT_EXIST);
            return result;
        }
        // 构建查询条件
        BoolQuery.Builder bqb = QueryBuilders.bool();
        // term 精确匹配 ，通常用于非分词字段或 keyword 类型的字段
        bqb.must(tq -> tq.term(x -> x.field("docId").value(query.getId())));
        // 构建排序参数
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"id"));
        // 构建分页参数
        Pageable pageable = PageRequest.of(query.getPageNo() - 1, query.getPageSize());
        // 构建查询字段
        SourceFilter sourceFilter = new SourceFilter() {
            @Override
            public String[] getIncludes() {
                return new String[0];
            }

            // 忽略 embedding 字段
            @Override
            public String[] getExcludes() {
                return new String[]{"embedding"};
            }
        };
        // 构建查询对象
        NativeQueryBuilder searchQueryBuilder = new NativeQueryBuilder()
                .withSort(sort)
                .withPageable(pageable)
                .withQuery(bqb.build()._toQuery())
                .withSourceFilter(sourceFilter);
        // 检查索引中文档数量
        long count = elasticsearchOperations.count(searchQueryBuilder.build(), IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
        if (count == 0) {
            PageResult<Map> pageResult = new PageResult<>();
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        // 执行查询，使用 Map<String, Object> 接收结果
        SearchHits<Map> searchHits = elasticsearchOperations.search(searchQueryBuilder.build(), Map.class, IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
        // 将搜索结果转换为 Page 对象
        List<Map> rows = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        // 设置返回结果
        PageResult<Map> pageResult = new PageResult<>();
        pageResult.setRows(rows);
        pageResult.setTotal((int)count);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询文档分片QA列表
     * @param query 查询参数
     * @return 分块问答结果
     */
    @Override
    public ResultData<PageResult<Map>> pageDocumentChunksQAList(DocumentQuery query) {
        ResultData<PageResult<Map>> result = new ResultData<>();
        if (query == null || query.getId() == null || query.getChunkIndex() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 检查索引是否存在
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ESIndexName.DOCUMENT_QA_VECTOR_INDEX_NAME));
        if (!indexOps.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_ES_INDEX_NOT_EXIST);
            return result;
        }
        // 构建查询条件
        BoolQuery.Builder bqb = QueryBuilders.bool();
        // term 精确匹配 ，通常用于非分词字段或 keyword 类型的字段
        bqb.must(tq -> tq.term(x -> x.field("docId").value(query.getId())));
        bqb.must(tq -> tq.term(x -> x.field("chunkIndex").value(query.getChunkIndex())));
        // 构建排序参数
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"id"));
        // 构建分页参数
        Pageable pageable = PageRequest.of(query.getPageNo() - 1, query.getPageSize());
        // 构建查询字段
        SourceFilter sourceFilter = new SourceFilter() {
            @Override
            public String[] getIncludes() {
                return new String[0];
            }

            // 忽略 embedding 字段
            @Override
            public String[] getExcludes() {
                return new String[]{"questionEmbedding"};
            }
        };
        // 构建查询对象
        NativeQueryBuilder searchQueryBuilder = new NativeQueryBuilder()
                .withSort(sort)
                .withPageable(pageable)
                .withQuery(bqb.build()._toQuery())
                .withSourceFilter(sourceFilter);
        // 检查索引中文档数量
        long count = elasticsearchOperations.count(searchQueryBuilder.build(), IndexCoordinates.of(ESIndexName.DOCUMENT_QA_VECTOR_INDEX_NAME));
        if (count == 0) {
            PageResult<Map> pageResult = new PageResult<>();
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        // 执行查询，使用 Map<String, Object> 接收结果
        SearchHits<Map> searchHits = elasticsearchOperations.search(searchQueryBuilder.build(), Map.class, IndexCoordinates.of(ESIndexName.DOCUMENT_QA_VECTOR_INDEX_NAME));
        // 将搜索结果转换为 Page 对象
        List<Map> rows = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        // 设置返回结果
        PageResult<Map> pageResult = new PageResult<>();
        pageResult.setTotal((int) count);
        pageResult.setRows(rows);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询文档分块统计信息
     * @param docId 文档ID
     * @return 统计信息
     */
    @Override
    public ResultData<Map<String, Object>> getDocumentChunkStats(Long docId) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        if (docId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        try {
            // 查询分块总数
            BoolQuery.Builder bqb = QueryBuilders.bool();
            bqb.must(tq -> tq.term(x -> x.field("docId").value(docId)));
            NativeQueryBuilder countQuery = new NativeQueryBuilder()
                    .withQuery(bqb.build()._toQuery());
            long chunkCount = elasticsearchOperations.count(countQuery.build(), IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
            // 查询文档信息获取分块策略
            DocumentQuery documentQuery = new DocumentQuery();
            documentQuery.setId(docId);
            DocumentResult document = documentDao.queryDocument(documentQuery);
            String chunkStrategy = "paragraph";
            if (document != null && document.getPrtId() != null) {
                KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
                knowledgeQuery.setId(document.getPrtId());
                KnowledgeResult knowledge = knowledgeDao.queryKnowledge(knowledgeQuery);
                if (knowledge != null && knowledge.getChunkStrategy() != null) {
                    chunkStrategy = knowledge.getChunkStrategy();
                }
            }
            // 构建统计信息
            Map<String, Object> stats = new HashMap<>();
            stats.put("chunkCount", chunkCount);
            stats.put("chunkStrategy", chunkStrategy);
            result.setData(stats);
            result.setCode(ResultData.OK);
        } catch (Exception e) {
            logger.error("getDocumentChunkStats error, docId={}", docId, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
        }
        return result;
    }

    /**
     * 重新分块文档
     * @param docId 文档ID
     * @return 操作结果
     */
    @Override
    public ResultData<Void> rechunkDocument(Long docId) {
        ResultData<Void> result = new ResultData<>();
        if (docId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        try {
            // 查询文档信息
            DocumentQuery documentQuery = new DocumentQuery();
            documentQuery.setId(docId);
            DocumentResult document = documentDao.queryDocument(documentQuery);
            if (document == null) {
                result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
                return result;
            }
            // 查询文档事件
            DocumentEventQuery eventQuery = new DocumentEventQuery();
            eventQuery.setDocId(docId);
            DocumentEventResult eventResult = documentEventDao.queryDocumentEvent(eventQuery);
            if (eventResult == null) {
                result.setErrorCode(ErrorCodeEnum.DOCUMENT_EVENT_NOT_EXIST);
                return result;
            }
            // 重置分块、向量化、图谱状态为待处理
            DocumentEvent updateEvent = new DocumentEvent();
            updateEvent.setId(eventResult.getId());
            updateEvent.setChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
            updateEvent.setChunkRemark("手动触发重新分块");
            updateEvent.setVectorStatus(DocumentEventStatusEnum.PENDING.getValue());
            updateEvent.setVectorRemark("等待重新分块完成");
            updateEvent.setGraphStatus(DocumentEventStatusEnum.PENDING.getValue());
            updateEvent.setGraphRemark("等待重新分块完成");
            int count = documentEventDao.updateById(updateEvent);
            if (count < 1) {
                logger.error("rechunkDocument update event fail, docId={}", docId);
                result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
                return result;
            }
            logger.info("rechunkDocument success, docId={}, eventId={}", docId, eventResult.getId());
            result.setCode(ResultData.OK);
        } catch (Exception e) {
            logger.error("rechunkDocument error, docId={}", docId, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
        }
        return result;
    }

}
