package com.spark.kg.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.kg.entity.KgEntity;
import com.spark.bean.kg.query.KgEntityQuery;
import com.spark.bean.kg.query.KgGraphQuery;
import com.spark.bean.kg.result.KgEntityResult;
import com.spark.bean.kg.result.KgGraphResult;
import com.spark.bean.kg.vo.KgEntityVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.kg.KgEntityDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.kg.KgRelationDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.KgAuditStatusEnum;
import com.spark.enums.KgSourceTypeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.kg.service.IKgEntityService;
import com.spark.llm.store.KgEntityVectorService;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 15:30:00
 * 知识图谱实体服务实现
 */
@Service
public class KgEntityServiceImpl extends BaseService<KgEntityQuery, KgEntityResult> implements IKgEntityService {
    private final static Logger logger = LoggerFactory.getLogger(KgEntityServiceImpl.class);
    @Autowired
    private KgEntityDao kgEntityDao;
    @Autowired
    private KgRelationDao kgRelationDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private Neo4jGraphStore graphStore;
    @Autowired
    private KgEntityVectorService entityVectorService;

    /**
     * 新增实体
     * @param kgEntityVO 实体数据
     * @return 新增结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_ENTITY_INSERT)
    public ResultData<Void> createKgEntity(KgEntityVO kgEntityVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgEntityVO == null || kgEntityVO.getGraphId() == null || StringUtil.isBlank(kgEntityVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgGraphQuery graphQuery = new KgGraphQuery();
        graphQuery.setId(kgEntityVO.getGraphId());
        KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(graphQuery);
        if (kgGraphResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_GRAPH_NOT_EXIST);
            return result;
        }
        KgEntity kgEntity = new KgEntity();
        BeanUtil.copyProperties(kgEntityVO, kgEntity);
        if (kgEntity.getStatus() == null) {
            kgEntity.setStatus(StatusEnum.NORMAL.getValue());
        }
        if (kgEntity.getSourceId() == null) {
            kgEntity.setSourceId(SessionHolder.getCurrentUserId());
        }
        int count = kgEntityDao.insertDB(kgEntity);
        if (count < 1) {
            logger.error("createKgEntity error, insert db fail");
            return result;
        }
        Long id = kgEntity.getId();
        // 同步写入 Neo4j 节点
        try {
            KgEntity node = new KgEntity();
            node.setId(id);
            node.setGraphId(kgEntity.getGraphId());
            node.setName(kgEntity.getName());
            node.setType(kgEntity.getType());
            node.setDescription(kgEntity.getDescription());
            graphStore.upsertEntity(node);
        } catch (Exception e) {
            logger.error("createKgEntity graphStore error, id={}", id, e);
        }
        // 同步向量化实体写入ES
        try {
            entityVectorService.vectorizeEntity(kgEntity);
        } catch (Exception e) {
            logger.error("createKgEntity vectorize error, id={}", id, e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改实体
     * @param kgEntityVO 实体数据
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_ENTITY_UPDATE)
    public ResultData<Void> updateKgEntity(KgEntityVO kgEntityVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgEntityVO == null || kgEntityVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgEntityQuery query = new KgEntityQuery();
        query.setId(kgEntityVO.getId());
        KgEntityResult kgEntityResult = kgEntityDao.queryKgEntity(query);
        if (kgEntityResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_ENTITY_NOT_EXIST);
            return result;
        }
        KgEntity kgEntity = new KgEntity();
        BeanUtil.copyProperties(kgEntityVO, kgEntity);
        int count = kgEntityDao.updateDBById(kgEntity);
        if (count < 1) {
            logger.error("updateKgEntity error, update db fail");
            return result;
        }
        // 同步更新 Neo4j 节点属性
        try {
            KgEntityResult latest = kgEntityDao.queryKgEntity(query);
            if (latest != null) {
                KgEntity node = new KgEntity();
                node.setId(latest.getId());
                node.setGraphId(latest.getGraphId());
                node.setName(latest.getName());
                node.setType(latest.getType());
                node.setDescription(latest.getDescription());
                graphStore.upsertEntity(node);
            }
        } catch (Exception e) {
            logger.error("updateKgEntity graphStore error, id={}", kgEntityVO.getId(), e);
        }
        // 同步重新向量化实体
        try {
            KgEntityResult latest = kgEntityDao.queryKgEntity(query);
            if (latest != null) {
                KgEntity latestEntity = new KgEntity();
                latestEntity.setId(latest.getId());
                latestEntity.setGraphId(latest.getGraphId());
                latestEntity.setName(latest.getName());
                latestEntity.setType(latest.getType());
                latestEntity.setDescription(latest.getDescription());
                entityVectorService.vectorizeEntity(latestEntity);
            }
        } catch (Exception e) {
            logger.error("updateKgEntity vectorize error, id={}", kgEntityVO.getId(), e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除实体
     * @param kgEntityVO 实体数据
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_ENTITY_DELETE)
    public ResultData<Void> deleteKgEntity(KgEntityVO kgEntityVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgEntityVO == null || kgEntityVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgEntityQuery query = new KgEntityQuery();
        query.setId(kgEntityVO.getId());
        KgEntityResult kgEntityResult = kgEntityDao.queryKgEntity(query);
        if (kgEntityResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        KgEntity kgEntity = new KgEntity();
        kgEntity.setId(kgEntityVO.getId());
        int count = kgEntityDao.deleteDBById(kgEntity);
        if (count < 1) {
            logger.error("deleteKgEntity error, delete db fail");
            return result;
        }
        // 同步删除 Neo4j 节点及其关联边
        try {
            graphStore.deleteByEntityId(kgEntityVO.getId());
        } catch (Exception e) {
            logger.error("deleteKgEntity graphStore error, id={}", kgEntityVO.getId(), e);
        }
        // 同步删除ES实体向量
        try {
            entityVectorService.deleteEntityVector(kgEntityVO.getId());
        } catch (Exception e) {
            logger.error("deleteKgEntity vectorize error, id={}", kgEntityVO.getId(), e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询实体
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<KgEntityResult>> pageKgEntityList(KgEntityQuery query) {
        ResultData<PageResult<KgEntityResult>> result = new ResultData<>();
        if (query == null) {
            query = new KgEntityQuery();
        }
        PageResult<KgEntityResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询实体详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    public ResultData<KgEntityResult> queryKgEntityDetail(KgEntityQuery query) {
        ResultData<KgEntityResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgEntityResult kgEntityResult = kgEntityDao.queryKgEntity(query);
        if (kgEntityResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_ENTITY_NOT_EXIST);
            return result;
        }
        result.setData(kgEntityResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 合并实体（消歧）
     * @param mainEntityId 主实体 id
     * @param mergedEntityIds 被合并的实体 id 列表
     * @return 合并结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_ENTITY_MERGE)
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> mergeKgEntity(Long mainEntityId, List<Long> mergedEntityIds) {
        ResultData<Void> result = new ResultData<>();
        if (mainEntityId == null || CollectionUtil.isEmpty(mergedEntityIds)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgEntityQuery mainQuery = new KgEntityQuery();
        mainQuery.setId(mainEntityId);
        KgEntityResult mainEntity = kgEntityDao.queryKgEntity(mainQuery);
        if (mainEntity == null) {
            result.setErrorCode(ErrorCodeEnum.KG_ENTITY_NOT_EXIST);
            return result;
        }
        // 去重并排除主实体自身
        Set<Long> mergedIds = new HashSet<>();
        for (Long mergedId : mergedEntityIds) {
            if (mergedId != null && !mergedId.equals(mainEntityId)) {
                mergedIds.add(mergedId);
            }
        }
        if (mergedIds.isEmpty()) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 预校验被合并实体与主实体属于同一图谱
        for (Long mergedId : mergedIds) {
            KgEntityQuery mergedQuery = new KgEntityQuery();
            mergedQuery.setId(mergedId);
            KgEntityResult mergedEntity = kgEntityDao.queryKgEntity(mergedQuery);
            if (mergedEntity == null || !mainEntity.getGraphId().equals(mergedEntity.getGraphId())) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
        }
        for (Long mergedId : mergedIds) {
            // 把被合并实体的关系引用转移到主实体
            kgRelationDao.updateHeadEntityId(mergedId, mainEntityId);
            kgRelationDao.updateTailEntityId(mergedId, mainEntityId);
            // 软删除被合并实体
            KgEntity entity = new KgEntity();
            entity.setId(mergedId);
            entity.setStatus(StatusEnum.ABNORMAL.getValue());
            kgEntityDao.updateDBById(entity);
            // Neo4j 迁移关系并删除旧节点，保持双库一致
            try {
                graphStore.migrateRelations(mainEntityId, mergedId);
            } catch (Exception e) {
                logger.error("mergeKgEntity graphStore error, mergedId={}", mergedId, e);
            }
            // ES 同步清理旧实体向量
            try {
                entityVectorService.deleteEntityVector(mergedId);
            } catch (Exception e) {
                logger.error("mergeKgEntity vectorize error, mergedId={}", mergedId, e);
            }
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 审核实体
     * @param entityId 实体ID
     * @param auditStatus 审核状态
     * @return 审核结果
     */
    @Override
    public ResultData<Void> auditKgEntity(Long entityId, Integer auditStatus) {
        ResultData<Void> result = new ResultData<>();
        if (entityId == null || auditStatus == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgEntityQuery query = new KgEntityQuery();
        query.setId(entityId);
        KgEntityResult entityResult = kgEntityDao.queryKgEntity(query);
        if (entityResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_ENTITY_NOT_EXIST);
            return result;
        }
        KgEntity entity = new KgEntity();
        entity.setId(entityId);
        entity.setAuditStatus(auditStatus);
        int count = kgEntityDao.updateDBById(entity);
        if (count < 1) {
            logger.error("auditKgEntity error, update db fail, entityId={}", entityId);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 批量审核实体
     * @param entityIds 实体ID列表
     * @param auditStatus 审核状态
     * @return 审核结果
     */
    @Override
    public ResultData<Void> batchAuditKgEntity(List<Long> entityIds, Integer auditStatus) {
        ResultData<Void> result = new ResultData<>();
        if (CollectionUtil.isEmpty(entityIds) || auditStatus == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        for (Long entityId : entityIds) {
            if (entityId == null) {
                continue;
            }
            KgEntity entity = new KgEntity();
            entity.setId(entityId);
            entity.setAuditStatus(auditStatus);
            kgEntityDao.updateDBById(entity);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<KgEntityResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(ke -> {
            ke.setStatusName(StatusEnum.indexOf(ke.getStatus()).getDesc());
            if (ke.getSourceType() != null) {
                KgSourceTypeEnum sourceType = KgSourceTypeEnum.indexOf(ke.getSourceType());
                ke.setSourceTypeName(sourceType.getDesc());
            }
            if (ke.getAuditStatus() != null) {
                KgAuditStatusEnum auditStatus = KgAuditStatusEnum.indexOf(ke.getAuditStatus());
                ke.setAuditStatusName(auditStatus.getDesc());
            }
        });
        Set<Long> graphIds = new HashSet<>();
        for (KgEntityResult ke : list) {
            if (ke.getGraphId() != null) {
                graphIds.add(ke.getGraphId());
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
        list.forEach(ke -> ke.setGraphName(graphNameMap.get(ke.getGraphId())));
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(KgEntityQuery query) {
        return kgEntityDao.queryKgEntityCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<KgEntityResult> queryList(KgEntityQuery query) {
        return kgEntityDao.queryKgEntityList(query);
    }
}
