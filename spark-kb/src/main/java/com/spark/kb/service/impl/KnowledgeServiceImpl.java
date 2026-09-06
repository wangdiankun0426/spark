package com.spark.kb.service.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kb.entity.Knowledge;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.kb.query.RetrieveTestQuery;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.kb.result.RetrieveItemResult;
import com.spark.common.bean.kb.result.RetrieveTestResult;
import com.spark.common.bean.kb.vo.KnowledgeVO;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.kb.result.RetrieveDetailItem;
import com.spark.common.bean.kb.result.RetrieveDetailResult;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.llm.ModelDao;
import com.spark.common.enums.ChunkStrategyEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.kb.service.IKnowledgeService;
import com.spark.kb.service.IRetrieveLogService;
import com.spark.llm.retrieve.ESRetrieve;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 */
@Service
@LogPrint
public class KnowledgeServiceImpl extends BaseService<KnowledgeQuery, KnowledgeResult> implements IKnowledgeService {
    private final static Logger logger = LoggerFactory.getLogger(KnowledgeServiceImpl.class);
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private ESRetrieve esRetrieve;
    @Autowired
    private IRetrieveLogService retrieveLogService;

    /**
     * 创建知识库
     * @param knowledgeVO 知识库数据
     * @return 创建结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KNOWLEDGE_INSERT)
    public ResultData<Void> createKnowledge(KnowledgeVO knowledgeVO) {
        ResultData<Void> result = new ResultData<>();
        if (knowledgeVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Knowledge knowledge = new Knowledge();
        BeanUtil.copyProperties(knowledgeVO, knowledge);
        if (knowledge.getVectorModelId() == null || knowledge.getRerankModelId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (knowledge.getParentChunkSize() == null) {
            knowledge.setParentChunkSize(800);
        }
        if (knowledge.getChildChunkSize() == null) {
            knowledge.setChildChunkSize(200);
        }
        if (knowledge.getParentOverlap() == null) {
            knowledge.setParentOverlap(100);
        }
        if (knowledge.getChildOverlap() == null) {
            knowledge.setChildOverlap(20);
        }
        if (StringUtil.isBlank(knowledge.getChunkStrategy())) {
            knowledge.setChunkStrategy(ChunkStrategyEnum.PARAGRAPH.getCode());
        }
        if (knowledge.getEnableQa() == null) {
            knowledge.setEnableQa(StatusEnum.ABNORMAL.getValue());
        }
        if (knowledge.getRetrieveTopK() == null) {
            knowledge.setRetrieveTopK(15);
        }
        if (knowledge.getMinSimilarity() == null) {
            knowledge.setMinSimilarity(0.40);
        }
        if (knowledge.getStatus() == null) {
            knowledge.setStatus(StatusEnum.ABNORMAL.getValue());
        }
        Long id = super.genObjectId(ObjectTypeEnum.KNOWLEDGE);
        knowledge.setId(id);
        int count = knowledgeDao.insertDB(knowledge);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(knowledge.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改知识库
     * @param knowledgeVO 知识库数据
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KNOWLEDGE_UPDATE)
    public ResultData<Void> updateKnowledge(KnowledgeVO knowledgeVO) {
        ResultData<Void> result = new ResultData<>();
        if (knowledgeVO == null || knowledgeVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        knowledgeQuery.setId(knowledgeVO.getId());
        KnowledgeResult knowledgeResult = knowledgeDao.queryKnowledge(knowledgeQuery);
        if (knowledgeResult == null) {
            result.setErrorCode(ErrorCodeEnum.KNOWLEDGE_NOT_EXIST);
            return result;
        }
        Knowledge knowledge = new Knowledge();
        BeanUtil.copyProperties(knowledgeVO, knowledge);
        if (knowledge.getVectorModelId() == null || knowledge.getRerankModelId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int count = knowledgeDao.updateDBById(knowledge);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(knowledge.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除知识库
     * @param knowledgeVO 知识库数据
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KNOWLEDGE_DELETE)
    public ResultData<Void> deleteKnowledge(KnowledgeVO knowledgeVO) {
        ResultData<Void> result = new ResultData<>();
        if (knowledgeVO == null || knowledgeVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        knowledgeQuery.setId(knowledgeVO.getId());
        KnowledgeResult knowledgeResult = knowledgeDao.queryKnowledge(knowledgeQuery);
        if (knowledgeResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Knowledge knowledge = new Knowledge();
        knowledge.setId(knowledgeVO.getId());
        int count = knowledgeDao.deleteDBById(knowledge);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(knowledge.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询知识库
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<KnowledgeResult>> pageKnowledgeList(KnowledgeQuery query) {
        ResultData<PageResult<KnowledgeResult>> result = new ResultData<>();
        if (query == null) {
            query = new KnowledgeQuery();
        }
        PageResult<KnowledgeResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询知识库详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KNOWLEDGE_DETAIL)
    public ResultData<KnowledgeResult> queryKnowledgeDetail(KnowledgeQuery query) {
        ResultData<KnowledgeResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KnowledgeResult knowledgeResult = knowledgeDao.queryKnowledge(query);
        if (knowledgeResult == null) {
            result.setErrorCode(ErrorCodeEnum.KNOWLEDGE_NOT_EXIST);
            return result;
        }
        result.setData(knowledgeResult);
        result.setObjId(knowledgeResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 检索测试
     * @param query 检索测试查询条件
     * @return 检索测试结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KNOWLEDGE_RETRIEVE_TEST)
    public ResultData<RetrieveTestResult> testRetrieve(RetrieveTestQuery query) {
        ResultData<RetrieveTestResult> result = new ResultData<>();
        if (query == null || query.getKbId() == null || StringUtil.isBlank(query.getQuery())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 查询知识库配置
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        knowledgeQuery.setId(query.getKbId());
        KnowledgeResult knowledge = knowledgeDao.queryKnowledge(knowledgeQuery);
        if (knowledge == null) {
            result.setErrorCode(ErrorCodeEnum.KNOWLEDGE_NOT_EXIST);
            return result;
        }
        // 覆盖知识库配置参数
        if (query.getTopK() != null) {
            knowledge.setRetrieveTopK(query.getTopK());
        }
        if (query.getMinSimilarity() != null) {
            knowledge.setMinSimilarity(query.getMinSimilarity());
        }
        if (query.getEnableQa() != null) {
            knowledge.setEnableQa(query.getEnableQa());
        }
        // 执行检索测试
        long startTime = System.currentTimeMillis();
        List<Long> kbIds = new ArrayList<>();
        kbIds.add(query.getKbId());
        RetrieveDetailResult detailResult = esRetrieve.retrieveDetail(query.getQuery(), kbIds, knowledge);
        long costTime = System.currentTimeMillis() - startTime;
        // 构建测试结果
        RetrieveTestResult testResult = new RetrieveTestResult();
        testResult.setQuery(query.getQuery());
        testResult.setCostTime(costTime);
        testResult.setStrategy(detailResult.getStrategy());
        testResult.setQaHit(detailResult.getQaHit());
        testResult.setRetrieveCount(0);
        // 构建检索项结果
        List<RetrieveItemResult> items = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(detailResult.getItems())) {
            testResult.setRetrieveCount(detailResult.getItems().size());
            for (int i = 0; i < detailResult.getItems().size(); i++) {
                RetrieveDetailItem detailItem = detailResult.getItems().get(i);
                RetrieveItemResult item = new RetrieveItemResult();
                item.setDocId(detailItem.getDocId());
                item.setChunkId(detailItem.getChunkId());
                item.setChunkIndex(detailItem.getChunkIndex());
                item.setContent(detailItem.getContent());
                item.setScore(detailItem.getScore());
                item.setRank(i + 1);
                item.setSourceType(detailItem.getSourceType());
                items.add(item);
            }
        }
        // 记录检索日志
        ResultData<Void> saveData = retrieveLogService.saveRetrieveLog(query.getKbId(), query.getQuery(), detailResult, costTime);
        BaseAssert.assertTrue(saveData);
        // 补充文档信息
        supplyDocName(items);
        testResult.setItems(items);
        result.setData(testResult);
        result.setObjId(knowledge.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充检索结果文档名称
     * @param items 检索结果列表
     */
    private void supplyDocName(List<RetrieveItemResult> items) {
        if (CollectionUtil.isEmpty(items)) {
            return;
        }
        Set<Long> docIds = items.stream()
                .map(RetrieveItemResult::getDocId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(docIds)) {
            return;
        }
        DocumentQuery docQuery = new DocumentQuery();
        docQuery.setPage(false);
        docQuery.setIds(new ArrayList<>(docIds));
        List<DocumentResult> docList = documentDao.queryDocumentList(docQuery);
        if (CollectionUtil.isEmpty(docList)) {
            return;
        }
        Map<Long, String> docNameMap = new HashMap<>();
        for (DocumentResult docResult : docList) {
            docNameMap.put(docResult.getId(), docResult.getName());
        }
        for (RetrieveItemResult item : items) {
            item.setDocName(docNameMap.get(item.getDocId()));
        }
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<KnowledgeResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(knowledgeResult -> {
            knowledgeResult.setStatusName(StatusEnum.indexOf(knowledgeResult.getStatus()).getDesc());
            knowledgeResult.setEnableQaName(StatusEnum.indexOf(knowledgeResult.getEnableQa()).getDesc());
            knowledgeResult.setChunkStrategyName(ChunkStrategyEnum.indexOf(knowledgeResult.getChunkStrategy()).getDesc());
        });
        Set<Long> modelIds = new HashSet<>();
        for (KnowledgeResult kr : list) {
            if (kr.getVectorModelId() != null) {
                modelIds.add(kr.getVectorModelId());
            }
            if (kr.getRerankModelId() != null) {
                modelIds.add(kr.getRerankModelId());
            }
        }
        Map<Long, String> modelNameMap = new HashMap<>();
        if (!modelIds.isEmpty()) {
            ModelQuery modelQuery = new ModelQuery();
            modelQuery.setIds(new ArrayList<>(modelIds));
            List<ModelResult> modelList = modelDao.queryModelList(modelQuery);
            for (ModelResult model : modelList) {
                modelNameMap.put(model.getId(), model.getName());
            }
        }
        list.forEach(kr -> {
            kr.setVectorModelName(modelNameMap.get(kr.getVectorModelId()));
            kr.setRerankModelName(modelNameMap.get(kr.getRerankModelId()));
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(KnowledgeQuery query) {
        return knowledgeDao.queryKnowledgeCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<KnowledgeResult> queryList(KnowledgeQuery query) {
        return knowledgeDao.queryKnowledgeList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return knowledgeDao.queryKnowledgeMaxId();
    }
}
