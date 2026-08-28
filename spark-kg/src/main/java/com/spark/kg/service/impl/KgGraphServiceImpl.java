package com.spark.kg.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.result.DocumentCountResult;
import com.spark.bean.kg.entity.KgGraph;
import com.spark.bean.kg.query.KgGraphQuery;
import com.spark.bean.kg.result.KgEntityCountResult;
import com.spark.bean.kg.result.KgGraphResult;
import com.spark.bean.kg.result.KgRelationCountResult;
import com.spark.bean.kg.vo.KgGraphVO;
import com.spark.bean.llm.query.ModelQuery;
import com.spark.bean.llm.result.ModelResult;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.kb.DocumentDao;
import com.spark.dao.kg.KgEntityDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.kg.KgRelationDao;
import com.spark.dao.llm.ModelDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.kg.service.IKgGraphService;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.utils.BeanUtil;
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
 * @since 2026-07-22 15:00:00
 * 知识图谱服务实现
 */
@Service
public class KgGraphServiceImpl extends BaseService<KgGraphQuery, KgGraphResult> implements IKgGraphService {
    private final static Logger logger = LoggerFactory.getLogger(KgGraphServiceImpl.class);
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private KgEntityDao kgEntityDao;
    @Autowired
    private KgRelationDao kgRelationDao;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private Neo4jGraphStore graphStore;

    /**
     * 创建知识图谱
     * @param kgGraphVO 图谱数据
     * @return 创建结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_GRAPH_INSERT)
    public ResultData<Void> createKgGraph(KgGraphVO kgGraphVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgGraphVO == null || kgGraphVO.getName() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgGraph kgGraph = new KgGraph();
        BeanUtil.copyProperties(kgGraphVO, kgGraph);
        if (kgGraph.getStatus() == null) {
            kgGraph.setStatus(StatusEnum.NORMAL.getValue());
        }
        Long id = super.genObjectId(ObjectTypeEnum.KG_GRAPH);
        kgGraph.setId(id);
        int count = kgGraphDao.insertDB(kgGraph);
        if (count < 1) {
            logger.error("createKgGraph error, insert db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改知识图谱
     * @param kgGraphVO 图谱数据
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_GRAPH_UPDATE)
    public ResultData<Void> updateKgGraph(KgGraphVO kgGraphVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgGraphVO == null || kgGraphVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgGraphQuery query = new KgGraphQuery();
        query.setId(kgGraphVO.getId());
        KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(query);
        if (kgGraphResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_GRAPH_NOT_EXIST);
            return result;
        }
        KgGraph kgGraph = new KgGraph();
        BeanUtil.copyProperties(kgGraphVO, kgGraph);
        int count = kgGraphDao.updateDBById(kgGraph);
        if (count < 1) {
            logger.error("updateKgGraph error, update db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除知识图谱
     * @param kgGraphVO 图谱数据
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.KG_GRAPH_DELETE)
    public ResultData<Void> deleteKgGraph(KgGraphVO kgGraphVO) {
        ResultData<Void> result = new ResultData<>();
        if (kgGraphVO == null || kgGraphVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgGraphQuery query = new KgGraphQuery();
        query.setId(kgGraphVO.getId());
        KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(query);
        if (kgGraphResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        KgGraph kgGraph = new KgGraph();
        kgGraph.setId(kgGraphVO.getId());
        int count = kgGraphDao.deleteDBById(kgGraph);
        if (count < 1) {
            logger.error("deleteKgGraph error, delete db fail");
            return result;
        }
        // 同步清理 Neo4j 图数据
        try {
            graphStore.deleteByGraphId(kgGraphVO.getId());
        } catch (Exception e) {
            logger.error("deleteKgGraph graphStore error, graphId={}", kgGraphVO.getId(), e);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询知识图谱
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<KgGraphResult>> pageKgGraphList(KgGraphQuery query) {
        ResultData<PageResult<KgGraphResult>> result = new ResultData<>();
        if (query == null) {
            query = new KgGraphQuery();
        }
        PageResult<KgGraphResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询知识图谱详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    public ResultData<KgGraphResult> queryKgGraphDetail(KgGraphQuery query) {
        ResultData<KgGraphResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        KgGraphResult kgGraphResult = kgGraphDao.queryKgGraph(query);
        if (kgGraphResult == null) {
            result.setErrorCode(ErrorCodeEnum.KG_GRAPH_NOT_EXIST);
            return result;
        }
        result.setData(kgGraphResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<KgGraphResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(kr -> kr.setStatusName(StatusEnum.indexOf(kr.getStatus()).getDesc()));
        List<Long> graphIds = list.stream()
                .map(KgGraphResult::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Integer> entityCountMap = new HashMap<>();
        Map<Long, Integer> relationCountMap = new HashMap<>();
        Map<Long, Integer> docCountMap = new HashMap<>();
        if (!graphIds.isEmpty()) {
            List<KgEntityCountResult> entityCountList = kgEntityDao.queryKgEntityCountByGraphIds(graphIds);
            for (KgEntityCountResult row : entityCountList) {
                entityCountMap.put(row.getGraphId(), row.getEntityCount());
            }
            List<KgRelationCountResult> relationCountList = kgRelationDao.queryKgRelationCountByGraphIds(graphIds);
            for (KgRelationCountResult row : relationCountList) {
                relationCountMap.put(row.getGraphId(), row.getRelationCount());
            }
            List<DocumentCountResult> docCountList = documentDao.queryDocumentCountByPrtIds(graphIds);
            for (DocumentCountResult row : docCountList) {
                docCountMap.put(row.getPrtId(), row.getDocumentCount());
            }
        }
        list.forEach(kr -> {
            kr.setEntityCount(entityCountMap.getOrDefault(kr.getId(), 0));
            kr.setRelationCount(relationCountMap.getOrDefault(kr.getId(), 0));
            kr.setDocCount(docCountMap.getOrDefault(kr.getId(), 0));
        });
        Set<Long> modelIds = new HashSet<>();
        for (KgGraphResult kr : list) {
            if (kr.getExtractModelId() != null) {
                modelIds.add(kr.getExtractModelId());
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
        list.forEach(kr -> kr.setExtractModelName(modelNameMap.get(kr.getExtractModelId())));
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(KgGraphQuery query) {
        return kgGraphDao.queryKgGraphCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<KgGraphResult> queryList(KgGraphQuery query) {
        return kgGraphDao.queryKgGraphList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return kgGraphDao.queryKgGraphMaxId();
    }
}
