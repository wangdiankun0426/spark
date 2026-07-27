package com.spark.kb.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.DocumentQuery;
import com.spark.constant.ESIndexName;
import com.spark.enums.ErrorCodeEnum;
import com.spark.kb.service.IDocumentChunkService;
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
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

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

}
