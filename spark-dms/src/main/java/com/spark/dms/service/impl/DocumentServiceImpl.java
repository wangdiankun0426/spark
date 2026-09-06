package com.spark.dms.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.dms.entity.Document;
import com.spark.common.bean.dms.entity.DocumentEvent;
import com.spark.common.bean.dms.query.DocumentEventQuery;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.dms.query.DocumentSearchQuery;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.dms.result.DocumentEventResult;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.dms.vo.DocumentVO;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.bean.sys.query.AttachmentQuery;
import com.spark.common.bean.sys.result.AttachmentResult;
import com.spark.common.enums.*;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.common.constant.ESIndexName;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.dms.DocumentEventDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.dms.AttachmentDao;
import com.spark.dms.service.IDocumentService;
import com.spark.llm.retrieve.ESRetrieve;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.FileUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-12-07 19:08:01
 */
@Service
@LogPrint
public class DocumentServiceImpl extends BaseService<DocumentQuery, DocumentResult> implements IDocumentService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private DocumentEventDao documentEventDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private AttachmentDao attachmentDao;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private ESRetrieve esRetrieve;
    @Value("${docs.file.path}")
    private String docsPath;

    /**
     * 根据系统附件归档文档
     * @param attId 附件id
     * @param prtId 父ID
     * @return 归档结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_INSERT)
    public ResultData<Long> fileDocument(Long attId, Long prtId) {
        ResultData<Long> result = new ResultData<>();
        if (attId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AttachmentQuery attachmentQuery = new AttachmentQuery();
        attachmentQuery.setId(attId);
        AttachmentResult attachment = attachmentDao.queryAttachment(attachmentQuery);
        if (attachment == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        if (prtId == null) {
            prtId = 0L;
        }
        String newPath = FileUtil.generateFilePath(docsPath, UUID.randomUUID() + "." + attachment.getExt());
        if (newPath == null) {
            result.setErrorCode(ErrorCodeEnum.FILE_CREATE_FAIL);
            return result;
        }
        boolean bo = FileUtil.copyFile(attachment.getPath(), newPath);
        if (!bo) {
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        // 复制实体文件 pdf格式
        FileUtil.copyCompanionFile(attachment.getPath(), newPath, "pdf");
        // 复制实体文件 txt格式
        FileUtil.copyCompanionFile(attachment.getPath(), newPath, "txt");
        Document document = new Document();
        Long docId = super.genObjectId(ObjectTypeEnum.DOCUMENT);
        document.setId(docId);
        document.setPrtId(prtId);
        ObjectTypeEnum objEnum = super.getObjEnum(prtId);
        document.setDocumentType(objEnum.getValue());
        document.setName(attachment.getName());
        document.setExt(attachment.getExt());
        document.setSize(attachment.getSize());
        document.setPath(newPath);
        document.setOwnerId(attachment.getOwnerId());
        Long userId = SessionHolder.getCurrentUserId() == null ? attachment.getOwnerId() : SessionHolder.getCurrentUserId();
        Long deptId = SessionHolder.getCurrentDeptId() == null ? attachment.getDeptId() : SessionHolder.getCurrentDeptId();
        document.setCreatedBy(userId);
        document.setUpdatedBy(userId);
        document.setDeptId(deptId);
        int count = documentDao.insertDB(document);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        DocumentEvent event = new DocumentEvent();
        event.setDocId(docId);
        event.setContentStatus(DocumentEventStatusEnum.PENDING.getValue());
        event.setIndexStatus(DocumentEventStatusEnum.PENDING.getValue());
        event.setChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
        event.setVectorStatus(DocumentEventStatusEnum.PENDING.getValue());
        if (objEnum != ObjectTypeEnum.KNOWLEDGE) {
            event.setVectorStatus(DocumentEventStatusEnum.NO_EXECUTE.getValue());
        }
        event.setGraphStatus(DocumentEventStatusEnum.PENDING.getValue());
        if (objEnum != ObjectTypeEnum.KG_GRAPH) {
            event.setGraphStatus(DocumentEventStatusEnum.NO_EXECUTE.getValue());
        }
        event.setCreatedBy(userId);
        event.setUpdatedBy(userId);
        count = documentEventDao.insertDB(event);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setData(docId);
        result.setObjId(docId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改文档
     * @param documentVO 修改的文档
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_UPDATE)
    public ResultData<Void> updateDocument(DocumentVO documentVO) {
        ResultData<Void> result = new ResultData<>();
        if (documentVO == null || documentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setId(documentVO.getId());
        DocumentResult documentResult = documentDao.queryDocument(documentQuery);
        if (documentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
            return result;
        }
        Document document = new Document();
        BeanUtil.copyProperties(documentVO, document);
        int count = documentDao.updateDBById(document);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(documentVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除文档
     * @param documentVO 删除的文档
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_DELETE)
    public ResultData<Void> deleteDocument(DocumentVO documentVO) {
        ResultData<Void> result = new ResultData<>();
        if (documentVO == null || documentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setId(documentVO.getId());
        DocumentResult documentResult = documentDao.queryDocument(documentQuery);
        if (documentResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Document document = new Document();
        document.setId(documentVO.getId());
        int count = documentDao.deleteDBById(document);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(documentVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询文档
     * @param query 查询参数
     * @return 文档列表
     */
    @Override
    @DataScope
    public ResultData<PageResult<DocumentResult>> pageDocumentList(DocumentQuery query) {
        ResultData<PageResult<DocumentResult>> result = new ResultData<>();
        if (query == null) {
            query = new DocumentQuery();
        }
        PageResult<DocumentResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询文档详情
     * @param query 查询参数
     * @return 文档详情
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_DETAIL)
    public ResultData<DocumentResult> queryDocumentDetail(DocumentQuery query) {
        ResultData<DocumentResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentResult documentResult = documentDao.queryDocument(query);
        if (documentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
            return result;
        }
        documentResult.setPath(null);
        documentResult.setOwnerName(super.getObjName(documentResult.getOwnerId()));
        super.supplyCreatedByName(documentResult);
        super.supplyUpdatedByName(documentResult);
        Long prtId = documentResult.getPrtId();
        Integer documentType = documentResult.getDocumentType();
        String prtName = this.queryPrtName(documentType, prtId);
        documentResult.setPrtName(prtName);
        result.setData(documentResult);
        result.setObjId(documentResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 搜索文档
     * 根据 searchType 区分全文检索（基于 spark-document 索引的 BM25 + 高亮）和语义检索（基于 spark-store 索引的 KNN 召回）
     * @param query 搜索参数
     * @return 搜索结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_SEARCH)
    public ResultData<PageResult<Map>> searchDocument(DocumentSearchQuery query) {
        ResultData<PageResult<Map>> result = new ResultData<>();
        if (query == null) {
            query = new DocumentSearchQuery();
        }
        if (DocumentSearchTypeEnum.CONTENT.getValue().equals(query.getSearchType())) {
            result = searchDocumentContent(query);
        } else if (DocumentSearchTypeEnum.SEMANTIC.getValue().equals(query.getSearchType())) {
            result = searchDocumentSemantic(query);
        }
        return result;
    }

    /**
     * 下载文档
     * @param query 查询参数
     * @return 文档结果
     */
    @Override
    public ResultData<DocumentResult> downloadDocument(DocumentQuery query) {
        ResultData<DocumentResult> result = new ResultData<>();
        if (query == null || query.getId() == null || StringUtil.isBlank(query.getExt())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentResult documentResult = documentDao.queryDocument(query);
        if (documentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
            return result;
        }
        String filePath = FileUtil.getFileNameWithoutExt(documentResult.getPath())+"."+query.getExt();
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("downloadDocument file not exist, filePath={}",filePath);
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        documentResult.setPath(filePath);
        result.setData(documentResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 批量删除文档
     * @param ids 文档ID列表
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_DELETE)
    public ResultData<Void> batchDeleteDocument(List<Long> ids) {
        ResultData<Void> result = new ResultData<>();
        if (CollectionUtil.isEmpty(ids)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int successCount = 0;
        for (Long id : ids) {
            try {
                Document document = new Document();
                document.setId(id);
                int count = documentDao.deleteDBById(document);
                if (count > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("batchDeleteDocument error, id={}", id, e);
            }
        }
        logger.info("batchDeleteDocument success, total={}, success={}", ids.size(), successCount);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 批量重新处理文档
     * @param ids 文档ID列表
     * @return 处理结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.DOCUMENT_EVENT_UPDATE)
    public ResultData<Void> batchReprocessDocument(List<Long> ids) {
        ResultData<Void> result = new ResultData<>();
        if (CollectionUtil.isEmpty(ids)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int successCount = 0;
        for (Long id : ids) {
            try {
                DocumentEventQuery eventQuery = new DocumentEventQuery();
                eventQuery.setDocId(id);
                DocumentEventResult eventResult = documentEventDao.queryDocumentEvent(eventQuery);
                if (eventResult != null) {
                    DocumentEvent updateEvent = new DocumentEvent();
                    updateEvent.setId(eventResult.getId());
                    updateEvent.setChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
                    updateEvent.setVectorStatus(DocumentEventStatusEnum.PENDING.getValue());
                    updateEvent.setGraphStatus(DocumentEventStatusEnum.PENDING.getValue());
                    documentEventDao.updateById(updateEvent);
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("batchReprocessDocument error, id={}", id, e);
            }
        }
        logger.info("batchReprocessDocument success, total={}, success={}", ids.size(), successCount);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 全文检索
     * @param query 搜索参数
     * @return 搜索结果
     */
    private ResultData<PageResult<Map>> searchDocumentContent(DocumentSearchQuery query) {
        ResultData<PageResult<Map>> result = new ResultData<>();
        if (StringUtil.isBlank(query.getKeyWord())) {
            PageResult<Map> pageResult = new PageResult<>();
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        // 检查索引是否存在
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ESIndexName.DOCUMENT_INDEX_NAME));
        if (!indexOps.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_ES_INDEX_NOT_EXIST);
            return result;
        }
        // 构建查询条件
        Long userId = SessionHolder.getCurrentUserId();
        BoolQuery.Builder bqb = QueryBuilders.bool();
        // term 精确匹配 ，通常用于非分词字段或 keyword 类型的字段
//        bqb.must(tq -> tq.term(x -> x.field("ownerId").value(userId.toString())));
        if (StringUtil.isNotBlank(query.getKeyWord())) {
            BoolQuery.Builder keyWordBqb = QueryBuilders.bool();
            // match_phrase 匹配分词之后的短语且必须按顺序匹配，但可以通过slop来调整允许间隔的长度，默认0 ， match 匹配分词之后的短语，不按顺序匹配
            keyWordBqb.should(mp -> mp.matchPhrase(x -> x.field("content").query(query.getKeyWord()).slop(0).analyzer("standard")));
            // wildcard 模糊查询 支持加通配符
            keyWordBqb.should(mp -> mp.wildcard(x -> x.field("name.keyword").wildcard("*"+query.getKeyWord()+"*")));
            bqb.must(keyWordBqb.build()._toQuery());
        }
        if (query.getDocumentType() != null) {
            bqb.must(tq -> tq.term(x -> x.field("documentType").value(query.getDocumentType().longValue())));
        }
        if (query.getPrtId() != null) {
            bqb.must(tq -> tq.term(x -> x.field("prtId").value(query.getPrtId())));
        }

//        bqb.must(rq -> rq.range(x -> x.term(l -> l.field("createdDt").gte(String.valueOf(query.getCreatedStartTime().getTime())))));
//        bqb.must(rq -> rq.range(x -> x.term(l -> l.field("createdDt").lte(String.valueOf(query.getCreatedEndTime().getTime())))));

        // 构建排序参数
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"id"));
        // 构建分页参数
        Pageable pageable = PageRequest.of(query.getPageNo() - 1, query.getPageSize());
        // 构建高亮设置
        HighlightField highlightField = new HighlightField("content",
                HighlightFieldParameters.builder()
                        .withPreTags("<span style=\"color:red;\">")
                        .withPostTags("</span>")
                        .build());
        HighlightQuery highlightQuery = new HighlightQuery(new Highlight(List.of(highlightField)), Map.class);
        // 构建查询对象
        NativeQueryBuilder searchQueryBuilder = new NativeQueryBuilder()
                .withSort(sort)
                .withPageable(pageable)
                .withQuery(bqb.build()._toQuery())
                .withHighlightQuery(highlightQuery);
        // 检查索引中文档数量
        long count = elasticsearchOperations.count(searchQueryBuilder.build(), IndexCoordinates.of(ESIndexName.DOCUMENT_INDEX_NAME));
        if (count == 0) {
            PageResult<Map> pageResult = new PageResult<>();
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        // 执行查询，使用 Map<String, Object> 接收结果
        SearchHits<Map> searchHits = elasticsearchOperations.search(searchQueryBuilder.build(), Map.class, IndexCoordinates.of(ESIndexName.DOCUMENT_INDEX_NAME));
        // 将搜索结果转换为 Page 对象
        List<Map> rows = searchHits.getSearchHits().stream()
                .map(hit -> {
                    Map map = hit.getContent();
                    map.put("content", null);
                    map.put("ownerName", super.getObjName(Long.valueOf(map.get("ownerId").toString())));
                    // 合并原始内容和高亮片段
                    Map<String, List<String>> highlightFields = hit.getHighlightFields();
                    if (highlightFields.containsKey("content")) {
                        List<String> highlights = highlightFields.get("content");
                        if (!highlights.isEmpty()) {
                            map.put("fragment", String.join(" ", highlights));
                        }
                    }
                    return map;
                })
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
     * 语义检索
     * 通过 chunk 向量 KNN 召回 docId，按当前用户过滤后回查 DB，按相关性顺序返回
     * @param query 搜索参数
     * @return 搜索结果
     */
    private ResultData<PageResult<Map>> searchDocumentSemantic(DocumentSearchQuery query) {
        ResultData<PageResult<Map>> result = new ResultData<>();
        if (StringUtil.isBlank(query.getKeyWord())) {
            PageResult<Map> pageResult = new PageResult<>();
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        // 检查向量索引是否存在
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ESIndexName.DOCUMENT_VECTOR_INDEX_NAME));
        if (!indexOps.exists()) {
            result.setErrorCode(ErrorCodeEnum.FILE_ES_INDEX_NOT_EXIST);
            return result;
        }
        // KNN 召回文档ID列表（已按相关性降序）
        List<Long> docIds = esRetrieve.retrieveDocumentIds(query.getKeyWord(), query.getDocumentType(), query.getPrtId());
        if (CollectionUtil.isEmpty(docIds)) {
            PageResult<Map> pageResult = new PageResult<>();
            result.setData(pageResult);
            result.setCode(ResultData.OK);
            return result;
        }
        // 按 ownerId 查 DB
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setIds(docIds);
        documentQuery.setDocumentType(query.getDocumentType());
        documentQuery.setPrtId(query.getPrtId());
//        documentQuery.setOwnerId(SessionHolder.getCurrentUserId());
        List<DocumentResult> documents = documentDao.queryDocumentList(documentQuery);
        // 按召回顺序（相关性降序）组装结果
        Map<Long, DocumentResult> docMap = documents.stream()
                .collect(Collectors.toMap(DocumentResult::getId, d -> d));
        List<Map> rows = new ArrayList<>();
        for (Long docId : docIds) {
            DocumentResult doc = docMap.get(docId);
            if (doc == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("name", doc.getName());
            map.put("ext", doc.getExt());
            map.put("size", doc.getSize());
            map.put("sizeStr", doc.getSizeStr());
            map.put("ownerId", doc.getOwnerId());
            map.put("ownerName", super.getObjName(doc.getOwnerId()));
            map.put("createdBy", doc.getCreatedBy());
            map.put("createdDt", doc.getCreatedDt());
            rows.add(map);
        }
        PageResult<Map> pageResult = new PageResult<>();
        pageResult.setTotal(rows.size());
        pageResult.setRows(rows);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }
    
    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<DocumentResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        Set<Long> knowledgeIds = new HashSet<>();
        Set<Long> graphIds = new HashSet<>();
        for (DocumentResult documentResult : list) {
            Long prtId = documentResult.getPrtId();
            if (prtId == null || prtId <= 0) {
                continue;
            }
            Integer documentType = documentResult.getDocumentType();
            if (ObjectTypeEnum.KG_GRAPH.getValue().equals(documentType)) {
                graphIds.add(prtId);
            } else {
                knowledgeIds.add(prtId);
            }
        }
        Map<Long, String> prtNameMap = new HashMap<>();
        if (!knowledgeIds.isEmpty()) {
            KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
            knowledgeQuery.setIds(new ArrayList<>(knowledgeIds));
            List<KnowledgeResult> knowledgeResults = knowledgeDao.queryKnowledgeList(knowledgeQuery);
            for (KnowledgeResult kr : knowledgeResults) {
                prtNameMap.put(kr.getId(), kr.getName());
            }
        }
        if (!graphIds.isEmpty()) {
            KgGraphQuery kgGraphQuery = new KgGraphQuery();
            kgGraphQuery.setIds(new ArrayList<>(graphIds));
            List<KgGraphResult> kgGraphResults = kgGraphDao.queryKgGraphList(kgGraphQuery);
            for (KgGraphResult gr : kgGraphResults) {
                prtNameMap.put(gr.getId(), gr.getName());
            }
        }
        for (DocumentResult documentResult : list) {
            documentResult.setPath(null);
            documentResult.setOwnerName(super.getObjName(documentResult.getOwnerId()));
            documentResult.setPrtName(prtNameMap.get(documentResult.getPrtId()));
        }
    }

    /**
     * 根据文档归属类型查询父级名称
     * @param documentType 文档归属类型
     * @param prtId 父级ID
     * @return 父级名称，查询不到返回 null
     */
    private String queryPrtName(Integer documentType, Long prtId) {
        if (prtId == null || prtId <= 0) {
            return null;
        }
        if (ObjectTypeEnum.KG_GRAPH.getValue().equals(documentType)) {
            KgGraphQuery kgGraphQuery = new KgGraphQuery();
            kgGraphQuery.setId(prtId);
            KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(kgGraphQuery);
            return kgGraphResult == null ? null : kgGraphResult.getName();
        }
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        knowledgeQuery.setId(prtId);
        KnowledgeResult knowledgeResult = knowledgeDao.queryKnowledge(knowledgeQuery);
        return knowledgeResult == null ? null : knowledgeResult.getName();
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(DocumentQuery query) {
        return documentDao.queryDocumentCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表数据
     */
    @Override
    protected List<DocumentResult> queryList(DocumentQuery query) {
        return documentDao.queryDocumentList(query);
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return documentDao.queryDocumentMaxId();
    }
}
