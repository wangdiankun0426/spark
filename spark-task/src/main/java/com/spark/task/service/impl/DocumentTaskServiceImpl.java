package com.spark.task.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.entity.DocumentEvent;
import com.spark.common.bean.dms.query.DocumentEventQuery;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.dms.result.DocumentEventResult;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.entity.KgRelation;
import com.spark.common.bean.kg.entity.RelationEdge;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.constant.ESIndexName;
import com.spark.common.utils.*;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.dms.DocumentEventDao;
import com.spark.dao.kg.KgEntityDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.kg.KgRelationDao;
import com.spark.common.enums.DocumentEventStatusEnum;
import com.spark.common.enums.KgAuditStatusEnum;
import com.spark.common.enums.KgSourceTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.store.ESVectorStore;
import com.spark.llm.store.KgEntityVectorService;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.task.service.IDocumentTaskService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午11:24
 */
@Service
public class DocumentTaskServiceImpl implements IDocumentTaskService {
    private final static Logger logger = LoggerFactory.getLogger(DocumentTaskServiceImpl.class);
    /**
     * 默认实体类型 schema
     */
    private static final String DEFAULT_ENTITY_TYPES = "[\"人物\",\"组织\",\"地点\",\"概念\",\"事件\"]";
    /**
     * 默认关系类型 schema
     */
    private static final String DEFAULT_RELATION_TYPES = "[\"相关\",\"属于\",\"位于\",\"参与\",\"产生\"]";
    @Autowired
    private DocumentEventDao documentEventDao;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private ESVectorStore vectorStore;
    @Autowired
    private KgEntityDao kgEntityDao;
    @Autowired
    private KgRelationDao kgRelationDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private Neo4jGraphStore graphStore;
    @Autowired
    private KgEntityVectorService entityVectorService;
    private final Lock contentLock = new ReentrantLock();
    private final Lock indexLock = new ReentrantLock();
    private final Lock chunkLock = new ReentrantLock();
    private final Lock vectorLock = new ReentrantLock();
    private final Lock graphLock = new ReentrantLock();

    /**
     * 文档内容任务
     *
     * @return  异步返回结果，包含操作是否成功的状态信息
     */
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<ResultData<Void>> executeContentTask() {
        ResultData<Void> result = new ResultData<>();
        if (contentLock.tryLock()) {
            try {
                logger.info("executeContentTask start");
                DocumentEventQuery eventQuery = new DocumentEventQuery();
                eventQuery.setContentStatus(DocumentEventStatusEnum.PENDING.getValue());
                List<DocumentEventResult> documentEventList = documentEventDao.queryDocumentEventList(eventQuery);
                if (CollectionUtil.isEmpty(documentEventList)) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                Map<Long, DocumentResult> docMap =  this.convertDocumentMap(documentEventList);
                if (docMap == null) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                for (DocumentEventResult documentEventResult : documentEventList) {
                    Long docId = documentEventResult.getDocId();
                    if (!docMap.containsKey(docId)) {
                        DocumentEvent documentEvent = new DocumentEvent();
                        documentEvent.setId(documentEventResult.getId());
                        documentEvent.setContentStatus(DocumentEventStatusEnum.FAIL.getValue());
                        documentEvent.setContentRemark("文档不存在");
                        documentEvent.setUpdatedBy(101L);
                        documentEventDao.updateDBById(documentEvent);
                        continue;
                    }
                    DocumentResult documentResult = docMap.get(docId);
                    String filePath = documentResult.getPath();
                    String txtPath = FileUtil.convertToTxt(filePath);
                    DocumentEvent documentEvent = new DocumentEvent();
                    documentEvent.setId(documentEventResult.getId());
                    if (StringUtil.isBlank(txtPath)) {
                        documentEvent.setContentStatus(DocumentEventStatusEnum.FAIL.getValue());
                        documentEvent.setContentRemark("提取文档内容失败");
                    } else {
                        File file = new File(txtPath);
                        if (!file.exists()) {
                            documentEvent.setContentStatus(DocumentEventStatusEnum.FAIL.getValue());
                            documentEvent.setContentRemark("提取文档内容失败");
                        } else {
                            documentEvent.setContentStatus(DocumentEventStatusEnum.SUCCESS.getValue());
                            documentEvent.setContentRemark("索引创建成功");
                        }
                    }
                    documentEvent.setUpdatedBy(101L);
                    documentEventDao.updateDBById(documentEvent);
                }
                result.setCode(ResultData.OK);
                logger.info("executeContentTask success");
            } catch (Exception e) {
                logger.error("executeContentTask error, e is ", e);
            } finally {
                contentLock.unlock();
            }
        } else {
            logger.warn("executeContentTask is already running, skipping this execution.");
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 创建文档索引任务
     *
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<ResultData<Void>> executeIndexTask() {
        ResultData<Void> result = new ResultData<>();
        if (indexLock.tryLock()) {
            try {
                logger.info("executeIndexTask start");
                DocumentEventQuery eventQuery = new DocumentEventQuery();
                eventQuery.setNoContentStatus(DocumentEventStatusEnum.PENDING.getValue());
                eventQuery.setIndexStatus(DocumentEventStatusEnum.PENDING.getValue());
                List<DocumentEventResult> documentEventList = documentEventDao.queryDocumentEventList(eventQuery);
                if (CollectionUtil.isEmpty(documentEventList)) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                Map<Long, DocumentResult> docMap = this.convertDocumentMap(documentEventList);
                if (docMap == null) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                for (DocumentEventResult documentEventResult : documentEventList) {
                    Long docId = documentEventResult.getDocId();
                    if (!docMap.containsKey(docId)) {
                        DocumentEvent documentEvent = new DocumentEvent();
                        documentEvent.setId(documentEventResult.getId());
                        documentEvent.setIndexStatus(DocumentEventStatusEnum.FAIL.getValue());
                        documentEvent.setIndexRemark("文档不存在");
                        documentEvent.setUpdatedBy(101L);
                        documentEventDao.updateDBById(documentEvent);
                        continue;
                    }
                    DocumentResult documentResult = docMap.get(docId);
                    Map<String, Object> document = new HashMap<>();
                    document.put("id", documentResult.getId());
                    document.put("prtId", documentResult.getPrtId());
                    document.put("documentType", documentResult.getDocumentType());
                    document.put("name", documentResult.getName());
                    document.put("ext", documentResult.getExt());
                    document.put("ownerId", documentResult.getOwnerId());
                    document.put("size", documentResult.getSize());
                    document.put("sizeStr", documentResult.getSizeStr());
                    String content = "";
                    String filePath = documentResult.getPath();
                    String txtPath = FileUtil.generateTxtFile(filePath);
                    ResultData<String> txtData = TextUtil.getFromText(txtPath, true);
                    if (txtData.getCode() == ResultData.OK) {
                        content = txtData.getData();
                    }
                    document.put("content", content);
                    document.put("createdBy", documentResult.getCreatedBy());
                    document.put("createdDt", documentResult.getCreatedDt().getTime());
                    document.put("updatedBy", documentResult.getUpdatedBy());
                    document.put("updatedDt", documentResult.getUpdatedDt().getTime());
                    this.createOrUpdateDocument(docId.toString(), document);
                    DocumentEvent documentEvent = new DocumentEvent();
                    documentEvent.setId(documentEventResult.getId());
                    documentEvent.setIndexStatus(DocumentEventStatusEnum.SUCCESS.getValue());
                    documentEvent.setIndexRemark("索引创建成功");
                    documentEvent.setUpdatedBy(101L);
                    documentEventDao.updateDBById(documentEvent);
                }
                result.setCode(ResultData.OK);
                logger.info("executeIndexTask success");
            } catch (Exception e) {
                logger.error("executeIndexTask error, e is ", e);
            } finally {
                indexLock.unlock();
            }
        } else {
            logger.warn("executeIndexTask is already running, skipping this execution.");
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 文档分块任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<ResultData<Void>> executeChunkTask() {
        ResultData<Void> result = new ResultData<>();
        if (chunkLock.tryLock()) {
            try {
                logger.info("executeChunkTask start");
                DocumentEventQuery eventQuery = new DocumentEventQuery();
                eventQuery.setNoContentStatus(DocumentEventStatusEnum.PENDING.getValue());
                eventQuery.setChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
                List<DocumentEventResult> documentEventList = documentEventDao.queryDocumentEventList(eventQuery);
                if (CollectionUtil.isEmpty(documentEventList)) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                Map<Long, DocumentResult> docMap = this.convertDocumentMap(documentEventList);
                if (docMap == null) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                for (DocumentEventResult documentEventResult : documentEventList) {
                    Long docId = documentEventResult.getDocId();
                    if (!docMap.containsKey(docId)) {
                        DocumentEvent documentEvent = new DocumentEvent();
                        documentEvent.setId(documentEventResult.getId());
                        documentEvent.setChunkStatus(DocumentEventStatusEnum.FAIL.getValue());
                        documentEvent.setChunkRemark("文档不存在");
                        documentEvent.setUpdatedBy(101L);
                        documentEventDao.updateDBById(documentEvent);
                        continue;
                    }
                    DocumentResult documentResult = docMap.get(docId);
                    String content = null;
                    String filePath = documentResult.getPath();
                    String txtPath = FileUtil.generateTxtFile(filePath);
                    ResultData<String> txtData = TextUtil.getFromText(txtPath, true);
                    if (txtData.getCode() == ResultData.OK) {
                        content = txtData.getData();
                    }
                    if (StringUtil.isBlank(content)) {
                        DocumentEvent documentEvent = new DocumentEvent();
                        documentEvent.setId(documentEventResult.getId());
                        documentEvent.setChunkStatus(DocumentEventStatusEnum.FAIL.getValue());
                        documentEvent.setChunkRemark("文档内容不存在");
                        documentEvent.setUpdatedBy(101L);
                        documentEventDao.updateDBById(documentEvent);
                        continue;
                    }
                    vectorStore.addChunk(docId, content, documentResult.getPrtId(), documentResult.getDocumentType());
                    DocumentEvent documentEvent = new DocumentEvent();
                    documentEvent.setId(documentEventResult.getId());
                    documentEvent.setChunkStatus(DocumentEventStatusEnum.SUCCESS.getValue());
                    documentEvent.setChunkRemark("分块成功");
                    documentEvent.setUpdatedBy(101L);
                    documentEventDao.updateDBById(documentEvent);
                }
                result.setCode(ResultData.OK);
                logger.info("executeChunkTask success");
            } catch (Exception e) {
                logger.error("executeChunkTask error, e is ", e);
            } finally {
                chunkLock.unlock();
            }
        } else {
            logger.warn("executeChunkTask is already running, skipping this execution.");
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 文档向量化任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<ResultData<Void>> executeVectorTask() {
        ResultData<Void> result = new ResultData<>();
        if (vectorLock.tryLock()) {
            try {
                logger.info("executeVectorTask start");
                DocumentEventQuery eventQuery = new DocumentEventQuery();
                eventQuery.setNoChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
                eventQuery.setVectorStatus(DocumentEventStatusEnum.PENDING.getValue());
                List<DocumentEventResult> documentEventList = documentEventDao.queryDocumentEventList(eventQuery);
                if (CollectionUtil.isEmpty(documentEventList)) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                Map<Long, DocumentResult> docMap = this.convertDocumentMap(documentEventList);
                if (docMap == null) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                for (DocumentEventResult documentEventResult : documentEventList) {
                    Long docId = documentEventResult.getDocId();
                    if (!docMap.containsKey(docId)) {
                        DocumentEvent documentEvent = new DocumentEvent();
                        documentEvent.setId(documentEventResult.getId());
                        documentEvent.setVectorStatus(DocumentEventStatusEnum.FAIL.getValue());
                        documentEvent.setVectorRemark("文档不存在");
                        documentEvent.setUpdatedBy(101L);
                        documentEventDao.updateDBById(documentEvent);
                        continue;
                    }
                    DocumentResult documentResult = docMap.get(docId);
                    vectorStore.addVector(docId, documentResult.getPrtId(), documentResult.getDocumentType());
                    DocumentEvent documentEvent = new DocumentEvent();
                    documentEvent.setId(documentEventResult.getId());
                    documentEvent.setVectorStatus(DocumentEventStatusEnum.SUCCESS.getValue());
                    documentEvent.setVectorRemark("向量化成功");
                    documentEvent.setUpdatedBy(101L);
                    documentEventDao.updateDBById(documentEvent);
                }
                result.setCode(ResultData.OK);
                logger.info("executeVectorTask success");
            } catch (Exception e) {
                logger.error("executeVectorTask error, e is ", e);
            } finally {
                vectorLock.unlock();
            }
        } else {
            logger.warn("executeVectorTask is already running, skipping this execution.");
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 知识图谱构建任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<ResultData<Void>> executeGraphTask() {
        ResultData<Void> result = new ResultData<>();
        if (graphLock.tryLock()) {
            try {
                logger.info("executeGraphTask start");
                DocumentEventQuery eventQuery = new DocumentEventQuery();
                eventQuery.setNoChunkStatus(DocumentEventStatusEnum.PENDING.getValue());
                eventQuery.setGraphStatus(DocumentEventStatusEnum.PENDING.getValue());
                List<DocumentEventResult> documentEventList = documentEventDao.queryDocumentEventList(eventQuery);
                if (CollectionUtil.isEmpty(documentEventList)) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                Map<Long, DocumentResult> docMap = this.convertDocumentMap(documentEventList);
                if (docMap == null) {
                    result.setCode(ResultData.OK);
                    return CompletableFuture.completedFuture(result);
                }
                for (DocumentEventResult documentEventResult : documentEventList) {
                    Long docId = documentEventResult.getDocId();
                    if (!docMap.containsKey(docId)) {
                        this.updateGraphStatus(documentEventResult.getId(), DocumentEventStatusEnum.FAIL.getValue(), "文档不存在");
                        continue;
                    }
                    DocumentResult documentResult = docMap.get(docId);
                    this.processDocumentEvent(documentEventResult.getId(), documentResult);
                }
                result.setCode(ResultData.OK);
                logger.info("executeGraphTask success");
            } catch (Exception e) {
                logger.error("executeGraphTask error, e is ", e);
            } finally {
                graphLock.unlock();
            }
        } else {
            logger.warn("executeGraphTask is already running, skipping this execution.");
        }
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 处理单个文档事件，分块抽取实体与关系并写入存储
     * @param eventId 文档事件 id
     * @param documentResult 文档信息
     */
    private void processDocumentEvent(Long eventId, DocumentResult documentResult) {
        Long docId = documentResult.getId();
        Long graphId = documentResult.getPrtId();
        if (graphId == null || graphId <= 0) {
            this.updateGraphStatus(eventId, DocumentEventStatusEnum.FAIL.getValue(), "图谱id为空");
            return;
        }
        KgGraphResult kgGraphResult = this.queryKgGraph(graphId);
        if (kgGraphResult == null) {
            this.updateGraphStatus(eventId, DocumentEventStatusEnum.FAIL.getValue(), "图谱不存在");
            return;
        }
        List<String> chunks = vectorStore.queryParentChunkContents(docId);
        if (CollectionUtil.isEmpty(chunks)) {
            this.updateGraphStatus(eventId, DocumentEventStatusEnum.FAIL.getValue(), "分块数据不存在");
            return;
        }
        try {
            // 分块抽取实体，按名称去重累加
            Map<String, KgEntity> entityMap = new LinkedHashMap<>();
            int entityFailCount = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                try {
                    List<KgEntity> chunkEntities = this.extractEntities(chunk, kgGraphResult.getEntityTypes(), kgGraphResult.getExtractModelId());
                    for (KgEntity entity : chunkEntities) {
                        if (entity == null || StringUtil.isBlank(entity.getName())) {
                            continue;
                        }
                        entityMap.putIfAbsent(entity.getName(), entity);
                    }
                } catch (Exception e) {
                    entityFailCount++;
                    logger.error("extractEntities chunk error, docId={}, chunkIndex={}/{}", docId, i, chunks.size(), e);
                }
            }
            if (entityMap.isEmpty() && entityFailCount == chunks.size()) {
                this.updateGraphStatus(eventId, DocumentEventStatusEnum.FAIL.getValue(), "抽取实体失败");
                return;
            }
            // 统一持久化实体，回填 id
            List<KgEntity> entities = new ArrayList<>(entityMap.values());
            this.saveEntities(entities, graphId, docId);
            // 分块抽取关系，按头尾实体+关系类型去重累加
            Map<String, KgRelation> relationMap = new LinkedHashMap<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                try {
                    List<KgRelation> chunkRelations = this.extractRelations(chunk, entities, kgGraphResult.getRelationTypes(), kgGraphResult.getExtractModelId());
                    for (KgRelation relation : chunkRelations) {
                        if (relation == null || relation.getHeadEntityId() == null || relation.getTailEntityId() == null) {
                            continue;
                        }
                        String key = relation.getHeadEntityId() + "_" + relation.getTailEntityId() + "_" + relation.getRelationType();
                        relationMap.putIfAbsent(key, relation);
                    }
                } catch (Exception e) {
                    logger.error("extractRelations chunk error, docId={}, chunkIndex={}/{}", docId, i, chunks.size(), e);
                }
            }
            // 统一持久化关系
            List<KgRelation> relations = new ArrayList<>(relationMap.values());
            this.saveRelations(relations, graphId, docId);
            String remark = String.format("构建知识图谱成功,分块%d,实体%d,关系%d", chunks.size(), entities.size(), relations.size());
            this.updateGraphStatus(eventId, DocumentEventStatusEnum.SUCCESS.getValue(), remark);
        } catch (Exception e) {
            logger.error("processDocumentEvent error, docId={}", docId, e);
            String remark = e.getMessage() == null ? "构建知识图谱异常" : e.getMessage();
            this.updateGraphStatus(eventId, DocumentEventStatusEnum.FAIL.getValue(), remark);
        }
    }

    /**
     * 从文本中抽取实体
     * @param text 原始文本
     * @param entityTypes 实体类型 schema
     * @param extractModelId 抽取模型 id
     * @return 实体列表
     */
    private List<KgEntity> extractEntities(String text, String entityTypes, Long extractModelId) {
        List<KgEntity> list = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return list;
        }
        String types = (entityTypes == null || entityTypes.isBlank()) ? DEFAULT_ENTITY_TYPES : entityTypes;
        Map<String, Object> variables = new HashMap<>();
        variables.put("entityTypes", types);
        variables.put("document", text);
        Prompt prompt = com.spark.prompt.PromptTemplateLoader.create("prompts/kg-entity-extract.txt", variables);
        ChatModel chatModel = this.getChatModel(extractModelId);
        String response = chatModel.chat(prompt.text());
        List<KgEntity> entities = this.parseEntities(response);
        logger.info("extractEntities success, count={}", entities.size());
        return entities;
    }

    /**
     * 从文本中抽取实体间关系
     * @param text 原始文本
     * @param entities 已抽取的实体列表
     * @param relationTypes 关系类型 schema
     * @param extractModelId 抽取模型 id
     * @return 关系列表
     */
    private List<KgRelation> extractRelations(String text, List<KgEntity> entities, String relationTypes, Long extractModelId) {
        List<KgRelation> list = new ArrayList<>();
        if (text == null || text.isBlank() || entities == null || entities.isEmpty()) {
            return list;
        }
        String types = (relationTypes == null || relationTypes.isBlank()) ? DEFAULT_RELATION_TYPES : relationTypes;
        String entityListText = entities.stream()
                .map(e -> e.getName() + "(" + (e.getType() == null ? "未分类" : e.getType()) + ")")
                .collect(Collectors.joining("、"));
        Map<String, Object> variables = new HashMap<>();
        variables.put("relationTypes", types);
        variables.put("entities", entityListText);
        variables.put("document", text);
        Prompt prompt = com.spark.prompt.PromptTemplateLoader.create("prompts/kg-relation-extract.txt", variables);
        ChatModel chatModel = this.getChatModel(extractModelId);
        String response = chatModel.chat(prompt.text());
        Map<String, KgEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(KgEntity::getName, e -> e, (a, b) -> a));
        List<KgRelation> relations = this.parseRelations(response, entityMap);
        logger.info("extractRelations success, count={}", relations.size());
        return relations;
    }

    /**
     * 获取 ChatModel 实例
     * @param extractModelId 抽取模型 id
     * @return ChatModel
     */
    private ChatModel getChatModel(Long extractModelId) {
        if (extractModelId == null) {
            return modelFactory.getDefaultNoThinkChatModel();
        }
        return modelFactory.getChatModel(extractModelId);
    }

    /**
     * 解析 LLM 返回的实体 JSON
     * @param response LLM 响应文本
     * @return 实体列表
     */
    private List<KgEntity> parseEntities(String response) {
        List<KgEntity> list = new ArrayList<>();
        JSONArray array = this.parseJsonArray(response);
        if (array == null) {
            return list;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj == null) {
                continue;
            }
            String name = obj.getString("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            KgEntity entity = new KgEntity();
            entity.setName(name);
            entity.setType(obj.getString("type"));
            entity.setDescription(obj.getString("description"));
            entity.setConfidence(obj.getDouble("confidence"));
            entity.setSourceType(KgSourceTypeEnum.LLM.getValue());
            entity.setAuditStatus(KgAuditStatusEnum.PENDING.getValue());
            entity.setStatus(StatusEnum.NORMAL.getValue());
            list.add(entity);
        }
        return list;
    }

    /**
     * 解析 LLM 返回的关系 JSON
     * @param response LLM 响应文本
     * @param entityMap 实体名称到实体的映射
     * @return 关系列表
     */
    private List<KgRelation> parseRelations(String response, Map<String, KgEntity> entityMap) {
        List<KgRelation> list = new ArrayList<>();
        JSONArray array = this.parseJsonArray(response);
        if (array == null) {
            return list;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj == null) {
                continue;
            }
            String head = obj.getString("head");
            String tail = obj.getString("tail");
            String relationType = obj.getString("relationType");
            if (head == null || tail == null || relationType == null) {
                continue;
            }
            KgEntity headEntity = entityMap.get(head);
            KgEntity tailEntity = entityMap.get(tail);
            if (headEntity == null || tailEntity == null) {
                continue;
            }
            KgRelation relation = new KgRelation();
            relation.setHeadEntityId(headEntity.getId());
            relation.setTailEntityId(tailEntity.getId());
            relation.setRelationType(relationType);
            Double weight = obj.getDouble("weight");
            relation.setWeight(weight == null ? 1.0 : weight);
            relation.setConfidence(obj.getDouble("confidence"));
            relation.setSourceType(KgSourceTypeEnum.LLM.getValue());
            relation.setStatus(StatusEnum.NORMAL.getValue());
            list.add(relation);
        }
        return list;
    }

    /**
     * 解析 JSON 数组，兼容模型可能返回的代码块包裹
     * @param response LLM 响应文本
     * @return JSONArray，解析失败返回 null
     */
    private JSONArray parseJsonArray(String response) {
        if (response == null || response.isBlank()) {
            return null;
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
            return JSON.parseArray(trimmed);
        } catch (Exception e) {
            logger.error("parseJsonArray fail, response={}", response, e);
            return null;
        }
    }

    /**
     * 批量持久化实体到 MySQL 与 Neo4j
     * @param entities 实体列表（执行完成后 id 已回填）
     * @param graphId 图谱 id
     * @param docId 文档 id
     */
    private void saveEntities(List<KgEntity> entities, Long graphId, Long docId) {
        for (KgEntity entity : entities) {
            entity.setGraphId(graphId);
            entity.setSourceId(docId);
            if (entity.getSourceType() == null) {
                entity.setSourceType(KgSourceTypeEnum.LLM.getValue());
            }
            if (entity.getAuditStatus() == null) {
                entity.setAuditStatus(KgAuditStatusEnum.PENDING.getValue());
            }
            entity.setStatus(StatusEnum.NORMAL.getValue());
            entity.setCreatedBy(101L);
            entity.setUpdatedBy(101L);
            int count = kgEntityDao.insertDB(entity);
            if (count < 1) {
                logger.error("insertKgEntity fail, name={}", entity.getName());
                continue;
            }
            try {
                graphStore.upsertEntity(entity);
            } catch (Exception e) {
                logger.error("upsertEntity error, id={}", entity.getId(), e);
            }
            // 同步向量化实体描述写入ES
            try {
                entityVectorService.vectorizeEntity(entity);
            } catch (Exception e) {
                logger.error("vectorizeEntity error, id={}", entity.getId(), e);
            }
        }
        logger.info("saveEntities success, docId={}, count={}", docId, entities.size());
    }

    /**
     * 批量持久化关系到 MySQL 与 Neo4j
     * @param relations 关系列表
     * @param graphId 图谱 id
     * @param docId 文档 id
     */
    private void saveRelations(List<KgRelation> relations, Long graphId, Long docId) {
        for (KgRelation relation : relations) {
            relation.setGraphId(graphId);
            relation.setSourceId(docId);
            if (relation.getSourceType() == null) {
                relation.setSourceType(KgSourceTypeEnum.LLM.getValue());
            }
            relation.setStatus(StatusEnum.NORMAL.getValue());
            relation.setCreatedBy(101L);
            relation.setUpdatedBy(101L);
            int count = kgRelationDao.insertDB(relation);
            if (count < 1) {
                logger.error("insertKgRelation fail, type={}", relation.getRelationType());
                continue;
            }
            try {
                RelationEdge edge = new RelationEdge();
                edge.setId(relation.getId());
                edge.setGraphId(graphId);
                edge.setHeadEntityId(relation.getHeadEntityId());
                edge.setTailEntityId(relation.getTailEntityId());
                edge.setRelationType(relation.getRelationType());
                edge.setWeight(relation.getWeight());
                graphStore.upsertRelation(edge);
            } catch (Exception e) {
                logger.error("upsertRelation error, id={}", relation.getId(), e);
            }
        }
        logger.info("saveRelations success, docId={}, count={}", docId, relations.size());
    }

    /**
     * 查询图谱详情
     * @param graphId 图谱 id
     * @return 图谱详情
     */
    private KgGraphResult queryKgGraph(Long graphId) {
        KgGraphQuery query = new KgGraphQuery();
        query.setId(graphId);
        return kgGraphDao.queryKgGraph(query);
    }

    /**
     * 更新文档事件的图谱构建状态
     * @param eventId 事件 id
     * @param status 状态值
     * @param remark 备注
     */
    private void updateGraphStatus(Long eventId, Integer status, String remark) {
        DocumentEvent documentEvent = new DocumentEvent();
        documentEvent.setId(eventId);
        documentEvent.setGraphStatus(status);
        documentEvent.setGraphRemark(remark);
        documentEvent.setUpdatedBy(101L);
        documentEventDao.updateDBById(documentEvent);
    }

    /**
     * 创建或更新文档
     * @param id 文档ID
     * @param document  文档
     */
    private void createOrUpdateDocument(String id, Map<String, Object> document) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ESIndexName.DOCUMENT_INDEX_NAME));
        if (!indexOps.exists()) {
            logger.error("");
            return;
        }
        // 使用 IndexQuery 设置 ID 和文档内容
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(id)
                .withSource(JsonUtil.toString(document))
                .build();
        String index = elasticsearchOperations.index(indexQuery, IndexCoordinates.of(ESIndexName.DOCUMENT_INDEX_NAME));
        logger.info("createOrUpdateDocument success, index is {}, doc id is {}", index, id);
    }

    /**
     * 获取文档信息Map
     * @param documentEventList 文档事件列表
     * @return 文档信息Map
     */
    private Map<Long, DocumentResult> convertDocumentMap(List<DocumentEventResult> documentEventList) {
        if (CollectionUtil.isEmpty(documentEventList)) {
            return null;
        }
        List<Long> docIds = documentEventList.stream().map(DocumentEventResult::getDocId).collect(Collectors.toList());
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setIds(docIds);
        documentQuery.setPage(false);
        List<DocumentResult> documentResults = documentDao.queryDocumentList(documentQuery);
        if (CollectionUtil.isEmpty(documentResults)) {
            return null;
        }
        return documentResults.stream().collect(Collectors.toMap(DocumentResult::getId, v -> v));
    }

}
