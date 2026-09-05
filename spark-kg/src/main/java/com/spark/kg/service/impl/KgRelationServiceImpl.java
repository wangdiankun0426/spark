package com.spark.kg.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.kg.entity.KgRelation;
import com.spark.common.bean.kg.entity.RelationEdge;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.query.KgRelationQuery;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.bean.kg.result.KgRelationResult;
import com.spark.common.bean.kg.vo.KgRelationVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.kg.KgEntityDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.kg.KgRelationDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.KgSourceTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.kg.service.IKgRelationService;
import com.spark.llm.store.Neo4jGraphStore;
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

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 16:00:00
 * 知识图谱关系服务实现
 */
@Service
public class KgRelationServiceImpl extends BaseService<KgRelationQuery, KgRelationResult> implements IKgRelationService {
    private final static Logger logger = LoggerFactory.getLogger(KgRelationServiceImpl.class);
    @Autowired
    private KgRelationDao kgRelationDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private KgEntityDao kgEntityDao;
    @Autowired
    private Neo4jGraphStore graphStore;

    /**
     * 新增关系
     * @param kgRelationVO 关系数据
     * @return 新增结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_RELATION_INSERT)
    public ResultData<Void> createKgRelation(KgRelationVO kgRelationVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgRelationVO == null || kgRelationVO.getGraphId() == null || kgRelationVO.getHeadEntityId() == null
                || kgRelationVO.getTailEntityId() == null || StringUtil.isBlank(kgRelationVO.getRelationType())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgGraphQuery graphQuery = new KgGraphQuery();
        graphQuery.setId(kgRelationVO.getGraphId());
        KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(graphQuery);
        if (kgGraphResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_GRAPH_NOT_EXIST);
            return result;
        }
        KgEntityQuery headQuery = new KgEntityQuery();
        headQuery.setId(kgRelationVO.getHeadEntityId());
        KgEntityResult headEntity = kgEntityDao.queryKgEntity(headQuery);
        if (headEntity == null) {
            result.setErrorCode(ErrorCodeEnum.KG_ENTITY_NOT_EXIST);
            return result;
        }
        KgEntityQuery tailQuery = new KgEntityQuery();
        tailQuery.setId(kgRelationVO.getTailEntityId());
        KgEntityResult tailEntity = kgEntityDao.queryKgEntity(tailQuery);
        if (tailEntity == null) {
            result.setErrorCode(ErrorCodeEnum.KG_ENTITY_NOT_EXIST);
            return result;
        }
        if (!Objects.equals(headEntity.getGraphId(), kgRelationVO.getGraphId())
                || !Objects.equals(tailEntity.getGraphId(), kgRelationVO.getGraphId())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgRelation kgRelation = new KgRelation();
        BeanUtil.copyProperties(kgRelationVO, kgRelation);
        if (kgRelation.getWeight() == null) {
            kgRelation.setWeight(1.0);
        }
        if (kgRelation.getStatus() == null) {
            kgRelation.setStatus(StatusEnum.NORMAL.getValue());
        }
        if (kgRelation.getSourceId() == null) {
            kgRelation.setSourceId(SessionHolder.getCurrentUserId());
        }
        int count = kgRelationDao.insertDB(kgRelation);
        if (count < 1) {
            logger.error("createKgRelation error, insert db fail");
            return result;
        }
        Long id = kgRelation.getId();
        // 同步写入 Neo4j 边
        try {
            RelationEdge edge = new RelationEdge();
            edge.setId(id);
            edge.setGraphId(kgRelation.getGraphId());
            edge.setHeadEntityId(kgRelation.getHeadEntityId());
            edge.setTailEntityId(kgRelation.getTailEntityId());
            edge.setRelationType(kgRelation.getRelationType());
            edge.setWeight(kgRelation.getWeight());
            graphStore.upsertRelation(edge);
        } catch (Exception e) {
            logger.error("createKgRelation graphStore error, id={}", id, e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改关系
     * @param kgRelationVO 关系数据
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_RELATION_UPDATE)
    public ResultData<Void> updateKgRelation(KgRelationVO kgRelationVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgRelationVO == null || kgRelationVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgRelationQuery query = new KgRelationQuery();
        query.setId(kgRelationVO.getId());
        KgRelationResult kgRelationResult = kgRelationDao.queryKgRelation(query);
        if (kgRelationResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_RELATION_NOT_EXIST);
            return result;
        }
        Long graphId = kgRelationVO.getGraphId() == null ? kgRelationResult.getGraphId() : kgRelationVO.getGraphId();
        Long headEntityId = kgRelationVO.getHeadEntityId() == null ? kgRelationResult.getHeadEntityId() : kgRelationVO.getHeadEntityId();
        Long tailEntityId = kgRelationVO.getTailEntityId() == null ? kgRelationResult.getTailEntityId() : kgRelationVO.getTailEntityId();
        if (!validateRelationEntities(graphId, headEntityId, tailEntityId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgRelation kgRelation = new KgRelation();
        BeanUtil.copyProperties(kgRelationVO, kgRelation);
        int count = kgRelationDao.updateDBById(kgRelation);
        if (count < 1) {
            logger.error("updateKgRelation error, update db fail");
            return result;
        }
        // 同步更新 Neo4j 边
        try {
            RelationEdge edge = new RelationEdge();
            edge.setId(kgRelationResult.getId());
            edge.setGraphId(graphId);
            edge.setHeadEntityId(headEntityId);
            edge.setTailEntityId(tailEntityId);
            edge.setRelationType(kgRelationVO.getRelationType() == null
                    ? kgRelationResult.getRelationType() : kgRelationVO.getRelationType());
            edge.setWeight(kgRelationVO.getWeight() == null
                    ? kgRelationResult.getWeight() : kgRelationVO.getWeight());
            graphStore.upsertRelation(edge);
        } catch (Exception e) {
            logger.error("updateKgRelation graphStore error, id={}", kgRelationResult.getId(), e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除关系
     * @param kgRelationVO 关系数据
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_RELATION_DELETE)
    public ResultData<Void> deleteKgRelation(KgRelationVO kgRelationVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgRelationVO == null || kgRelationVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgRelationQuery query = new KgRelationQuery();
        query.setId(kgRelationVO.getId());
        KgRelationResult kgRelationResult = kgRelationDao.queryKgRelation(query);
        if (kgRelationResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        KgRelation kgRelation = new KgRelation();
        kgRelation.setId(kgRelationVO.getId());
        int count = kgRelationDao.deleteDBById(kgRelation);
        if (count < 1) {
            logger.error("deleteKgRelation error, delete db fail");
            return result;
        }
        // 同步删除 Neo4j 边
        try {
            graphStore.deleteByRelationId(kgRelationVO.getId());
        } catch (Exception e) {
            logger.error("deleteKgRelation graphStore error, id={}", kgRelationVO.getId(), e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验关系两端实体属于同一图谱
     * @param graphId 图谱 id
     * @param headEntityId 头实体 id
     * @param tailEntityId 尾实体 id
     * @return 是否校验通过
     */
    private boolean validateRelationEntities(Long graphId, Long headEntityId, Long tailEntityId) {
        if (graphId == null || headEntityId == null || tailEntityId == null) {
            return false;
        }
        KgEntityQuery headQuery = new KgEntityQuery();
        headQuery.setId(headEntityId);
        KgEntityResult headEntity = kgEntityDao.queryKgEntity(headQuery);
        KgEntityQuery tailQuery = new KgEntityQuery();
        tailQuery.setId(tailEntityId);
        KgEntityResult tailEntity = kgEntityDao.queryKgEntity(tailQuery);
        return headEntity != null && tailEntity != null
                && Objects.equals(headEntity.getGraphId(), graphId)
                && Objects.equals(tailEntity.getGraphId(), graphId);
    }

    /**
     * 分页查询关系
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<KgRelationResult>> pageKgRelationList(KgRelationQuery query) {
        ResultData<PageResult<KgRelationResult>> result = new ResultData<>();
        if (query == null) {
            query = new KgRelationQuery();
        }
        PageResult<KgRelationResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询关系详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    public ResultData<KgRelationResult> queryKgRelationDetail(KgRelationQuery query) {
        ResultData<KgRelationResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgRelationResult kgRelationResult = kgRelationDao.queryKgRelation(query);
        if (kgRelationResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_RELATION_NOT_EXIST);
            return result;
        }
        result.setData(kgRelationResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<KgRelationResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(kr -> {
            kr.setStatusName(StatusEnum.indexOf(kr.getStatus()).getDesc());
            if (kr.getSourceType() != null) {
                KgSourceTypeEnum sourceType = KgSourceTypeEnum.indexOf(kr.getSourceType());
                kr.setSourceTypeName(sourceType.getDesc());
            }
        });
        Set<Long> graphIds = new HashSet<>();
        Set<Long> entityIds = new HashSet<>();
        for (KgRelationResult kr : list) {
            if (kr.getGraphId() != null) {
                graphIds.add(kr.getGraphId());
            }
            if (kr.getHeadEntityId() != null) {
                entityIds.add(kr.getHeadEntityId());
            }
            if (kr.getTailEntityId() != null) {
                entityIds.add(kr.getTailEntityId());
            }
        }
        Map<Long, String> graphNameMap = new HashMap<>();
        if (!graphIds.isEmpty()) {
            KgGraphQuery graphQuery = new KgGraphQuery();
            graphQuery.setIds(new ArrayList<>(graphIds));
            List<KgGraphResult> graphList = kgGraphDao.queryKgGraphList(graphQuery);
            for (KgGraphResult graph : graphList) {
                graphNameMap.put(graph.getId(), graph.getName());
            }
        }
        Map<Long, String> entityNameMap = new HashMap<>();
        if (!entityIds.isEmpty()) {
            KgEntityQuery entityQuery = new KgEntityQuery();
            entityQuery.setIds(new ArrayList<>(entityIds));
            List<KgEntityResult> entityList = kgEntityDao.queryKgEntityList(entityQuery);
            for (KgEntityResult entity : entityList) {
                entityNameMap.put(entity.getId(), entity.getName());
            }
        }
        list.forEach(kr -> {
            kr.setGraphName(graphNameMap.get(kr.getGraphId()));
            kr.setHeadEntityName(entityNameMap.get(kr.getHeadEntityId()));
            kr.setTailEntityName(entityNameMap.get(kr.getTailEntityId()));
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(KgRelationQuery query) {
        return kgRelationDao.queryKgRelationCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<KgRelationResult> queryList(KgRelationQuery query) {
        return kgRelationDao.queryKgRelationList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return kgRelationDao.queryKgRelationMaxId();
    }
}
