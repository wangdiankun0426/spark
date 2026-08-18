package com.spark.kb.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.entity.Knowledge;
import com.spark.bean.kb.query.KnowledgeQuery;
import com.spark.bean.kb.result.DocumentCountResult;
import com.spark.bean.kb.result.KnowledgeResult;
import com.spark.bean.kb.vo.KnowledgeVO;
import com.spark.bean.llm.query.ModelQuery;
import com.spark.bean.llm.result.ModelResult;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.kb.DocumentDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.llm.ModelDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.kb.service.IKnowledgeService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
public class KnowledgeServiceImpl extends BaseService<KnowledgeQuery, KnowledgeResult> implements IKnowledgeService {
    private final static Logger logger = LoggerFactory.getLogger(KnowledgeServiceImpl.class);
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private ModelDao modelDao;

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
        BeanUtils.copyProperties(knowledgeVO, knowledge);
        if (knowledge.getVectorModelId() == null || knowledge.getRerankModelId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 设置默认值
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
        if (knowledge.getEnableQa() == null) {
            knowledge.setEnableQa(StatusEnum.NORMAL.getValue());
        }
        if (knowledge.getRetrieveTopK() == null) {
            knowledge.setRetrieveTopK(15);
        }
        if (knowledge.getMinSimilarity() == null) {
            knowledge.setMinSimilarity(0.40);
        }
        if (knowledge.getStatus() == null) {
            knowledge.setStatus(StatusEnum.NORMAL.getValue());
        }
        Long id = super.genObjectId(ObjectTypeEnum.KNOWLEDGE);
        knowledge.setId(id);
        int count = knowledgeDao.insertDB(knowledge);
        if (count < 1) {
            logger.error("createKnowledge error, insert db fail");
            return result;
        }
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
        BeanUtils.copyProperties(knowledgeVO, knowledge);
        if (knowledge.getVectorModelId() == null || knowledge.getRerankModelId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        int count = knowledgeDao.updateDBById(knowledge);
        if (count < 1) {
            logger.error("updateKnowledge error, update db fail");
            return result;
        }
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
            logger.error("deleteKnowledge error, delete db fail");
            return result;
        }
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
        result.setCode(ResultData.OK);
        return result;
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
        });
        List<Long> kbIds = list.stream()
                .map(KnowledgeResult::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Integer> countMap = new HashMap<>();
        if (!kbIds.isEmpty()) {
            List<DocumentCountResult> countList = documentDao.queryDocumentCountByPrtIds(kbIds);
            for (DocumentCountResult row : countList) {
                countMap.put(row.getPrtId(), row.getDocumentCount());
            }
        }
        list.forEach(kr -> kr.setDocumentCount(countMap.getOrDefault(kr.getId(), 0)));
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
