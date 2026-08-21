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
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.kg.service.IKgEntityService;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
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
        BeanUtils.copyProperties(kgEntityVO, kgEntity);
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
        BeanUtils.copyProperties(kgEntityVO, kgEntity);
        int count = kgEntityDao.updateDBById(kgEntity);
        if (count < 1) {
            logger.error("updateKgEntity error, update db fail");
            return result;
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
        for (Long mergedId : mergedEntityIds) {
            if (mergedId == null || mergedId.equals(mainEntityId)) {
                continue;
            }
            // 把被合并实体的关系引用转移到主实体
            kgRelationDao.updateHeadEntityId(mergedId, mainEntityId);
            kgRelationDao.updateTailEntityId(mergedId, mainEntityId);
            // 软删除被合并实体
            KgEntity entity = new KgEntity();
            entity.setId(mergedId);
            entity.setStatus(StatusEnum.ABNORMAL.getValue());
            kgEntityDao.updateDBById(entity);
            // Neo4j 同步清理旧节点
            try {
                graphStore.deleteByEntityId(mergedId);
            } catch (Exception e) {
                logger.error("mergeKgEntity graphStore error, mergedId={}", mergedId, e);
            }
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
        list.forEach(ke -> ke.setStatusName(StatusEnum.indexOf(ke.getStatus()).getDesc()));
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
