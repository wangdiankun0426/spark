package com.spark.dms.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.entity.DocumentEvent;
import com.spark.common.bean.dms.query.DocumentEventQuery;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.dms.result.DocumentEventResult;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.constant.ESIndexName;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.dms.DocumentEventDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.common.enums.DocumentEventStatusEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.dms.service.IDocumentChunkService;
import com.spark.common.utils.StringUtil;
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
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
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
@LogPrint
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
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_EVENT_UPDATE)
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
        result.setObjId(docId);
        return result;
    }

    /**
     * 编辑分块内容
     * 更新ES分块内容后，重置向量化、图谱状态触发重新处理
     * @param docId 文档ID
     * @param chunkIndex 分块序号
     * @param content 分块内容
     * @return 操作结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_CHUNK_EDIT)
    public ResultData<Void> updateChunk(Long docId, Integer chunkIndex, String content) {
        ResultData<Void> result = new ResultData<>();
        if (docId == null || chunkIndex == null || StringUtil.isBlank(content)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        try {
            // 检查索引是否存在
            IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
            if (!indexOps.exists()) {
                result.setErrorCode(ErrorCodeEnum.FILE_ES_INDEX_NOT_EXIST);
                return result;
            }
            // 查询分块
            Map<String, Object> chunkMap = queryChunkByIndex(docId, chunkIndex);
            if (chunkMap == null || chunkMap.get("id") == null) {
                result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
                return result;
            }
            // 更新分块内容
            String chunkId = String.valueOf(chunkMap.get("id"));
            Document document = Document.create();
            document.put("content", content);
            UpdateQuery updateQuery = UpdateQuery.builder(chunkId).withDocument(document).build();
            elasticsearchOperations.update(updateQuery, IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
            // 重置向量化、图谱状态
            resetEventStatus(docId);
            logger.info("updateChunk success, docId={}, chunkIndex={}", docId, chunkIndex);
            result.setCode(ResultData.OK);
        } catch (Exception e) {
            logger.error("updateChunk error, docId={}, chunkIndex={}", docId, chunkIndex, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
        }
        result.setObjId(docId);
        return result;
    }

    /**
     * 按文档ID与分块序号查询分块
     * @param docId 文档ID
     * @param chunkIndex 分块序号
     * @return 分块数据
     */
    private Map<String, Object> queryChunkByIndex(Long docId, Integer chunkIndex) {
        BoolQuery.Builder bqb = QueryBuilders.bool();
        bqb.must(tq -> tq.term(x -> x.field("docId").value(docId)));
        bqb.must(tq -> tq.term(x -> x.field("chunkIndex").value(chunkIndex)));
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder().withQuery(bqb.build()._toQuery());
        SearchHits<Map> hits = elasticsearchOperations.search(queryBuilder.build(), Map.class, IndexCoordinates.of(ESIndexName.DOCUMENT_CHUNK_INDEX_NAME));
        if (hits.hasSearchHits()) {
            return hits.getSearchHit(0).getContent();
        }
        return null;
    }

    /**
     * 重置文档事件向量化、图谱状态为待处理
     * @param docId 文档ID
     */
    private void resetEventStatus(Long docId) {
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setId(docId);
        DocumentResult document = documentDao.queryDocument(documentQuery);
        if (document == null) {
            return;
        }
        DocumentEventQuery eventQuery = new DocumentEventQuery();
        eventQuery.setDocId(docId);
        DocumentEventResult eventResult = documentEventDao.queryDocumentEvent(eventQuery);
        if (eventResult == null) {
            return;
        }
        DocumentEvent updateEvent = new DocumentEvent();
        updateEvent.setId(eventResult.getId());
        updateEvent.setVectorStatus(DocumentEventStatusEnum.PENDING.getValue());
        updateEvent.setVectorRemark("分块内容已编辑，等待重新向量化");
        updateEvent.setGraphStatus(DocumentEventStatusEnum.PENDING.getValue());
        updateEvent.setGraphRemark("分块内容已编辑，等待重新处理");
        documentEventDao.updateDBById(updateEvent);
    }

}
