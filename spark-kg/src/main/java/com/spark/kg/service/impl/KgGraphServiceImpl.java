package com.spark.kg.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.entity.KgGraph;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.bean.kg.vo.KgGraphVO;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.dao.kg.KgEntityDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.llm.ModelDao;
import com.spark.common.enums.DataScopeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.kg.service.IKgGraphService;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.retrieve.Neo4jRetrieve;
import com.spark.llm.store.KgEntityVectorService;
import com.spark.llm.store.Neo4jGraphStore;
import com.spark.manage.BaseService;
import com.spark.prompt.PromptTemplateLoader;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.alibaba.fastjson2.JSONObject;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 2026-07-22 15:00:00
 * 知识图谱服务实现
 */
@Service
@LogPrint
public class KgGraphServiceImpl extends BaseService<KgGraphQuery, KgGraphResult> implements IKgGraphService {
    private final static Logger logger = LoggerFactory.getLogger(KgGraphServiceImpl.class);
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private KgEntityDao kgEntityDao;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private Neo4jGraphStore graphStore;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private KgEntityVectorService entityVectorService;
    @Autowired
    private Neo4jRetrieve neo4jRetrieve;

    /**
     * 创建知识图谱
     * @param kgGraphVO 图谱数据
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.KG_GRAPH_INSERT)
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
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(id);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改知识图谱
     * @param kgGraphVO 图谱数据
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.KG_GRAPH_UPDATE)
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
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(kgGraph.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除知识图谱
     * @param kgGraphVO 图谱数据
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.KG_GRAPH_DELETE)
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
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 同步清理 Neo4j 图数据
        try {
            graphStore.deleteByGraphId(kgGraphVO.getId());
        } catch (Exception e) {
            logger.error("deleteKgGraph graphStore error, graphId={}", kgGraphVO.getId(), e);
        }
        // 同步清理ES实体向量数据
        try {
            entityVectorService.deleteByGraphId(kgGraphVO.getId());
        } catch (Exception e) {
            logger.error("deleteKgGraph entityVector error, graphId={}", kgGraphVO.getId(), e);
        }
        result.setObjId(kgGraph.getId());
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
        query.setTenantId(SessionHolder.getCurrentTenantId());
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
    @LogOperate(operateType = OperateTypeEnum.KG_GRAPH_DETAIL)
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
        result.setObjId(kgGraphResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按需展开节点关联子图
     * @param nodeId 节点 id
     * @param depth 扩展深度
     * @return 子图数据
     */
    @Override
    public ResultData<Map<String, Object>> expandNodeVisual(Long nodeId, int depth) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        if (nodeId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 解析节点所属图谱并校验访问权限
        KgEntityQuery nodeQuery = new KgEntityQuery();
        nodeQuery.setId(nodeId);
        KgEntityResult nodeEntity = kgEntityDao.queryKgEntity(nodeQuery);
        if (nodeEntity == null || !checkGraphAccess(nodeEntity.getGraphId())) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Map<String, Object> subgraph = graphStore.expandNode(nodeId, depth);
        // 补充节点关联度
        List<KgEntity> nodes = (List<KgEntity>) subgraph.get("nodes");
        if (CollectionUtil.isNotEmpty(nodes)) {
            List<Long> nodeIds = nodes.stream().map(KgEntity::getId).toList();
            Map<Long, Integer> degrees = graphStore.queryNodeDegrees(nodeIds);
            subgraph.put("degrees", degrees);
        }
        result.setData(subgraph);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询图谱统计信息
     * @param graphId 图谱 id
     * @return 统计信息
     */
    @Override
    public ResultData<Map<String, Object>> queryGraphStats(Long graphId) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        if (graphId == null || !checkGraphAccess(graphId)) {
            result.setErrorCode(graphId == null ? ErrorCodeEnum.INVALID_PARAM : ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        Map<String, Object> stats = graphStore.queryGraphStats(graphId);
        // 连通分量数
        try {
            Map<Integer, List<Long>> components = graphStore.findConnectedComponents(graphId);
            stats.put("componentCount", components.size());
        } catch (Exception e) {
            logger.warn("queryGraphStats wcc fail, graphId={}", graphId, e);
            stats.put("componentCount", -1);
        }
        result.setData(stats);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 图谱推理
     * @param question 问题
     * @param graphIds 图谱 id 列表
     * @return 推理结果
     */
    @Override
    public ResultData<String> reason(String question, List<Long> graphIds) {
        ResultData<String> result = new ResultData<>();
        if (StringUtil.isBlank(question) || CollectionUtil.isEmpty(graphIds)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (graphIds.stream().anyMatch(graphId -> !checkGraphAccess(graphId))) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        // 1. 检索相关三元组
        ResultData<List<String>> retrieval = neo4jRetrieve.retrieve(question, graphIds);
        if (retrieval.getCode() != ResultData.OK || CollectionUtil.isEmpty(retrieval.getData())) {
            result.setData("根据当前知识图谱未检索到与问题相关的关系信息，无法进行推理。");
            result.setCode(ResultData.OK);
            return result;
        }
        // 2. 组装推理上下文
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < retrieval.getData().size(); i++) {
            context.append(i + 1).append(". ").append(retrieval.getData().get(i)).append("\n");
        }
        try {
            // 3. 加载推理模板并调用 LLM
            Map<String, Object> variables = new HashMap<>();
            variables.put("question", question);
            variables.put("context", context.toString());
            Prompt prompt = PromptTemplateLoader.create("prompts/kg-reasoning.txt", variables);
            ChatModel chatModel = modelFactory.getDefaultNoThinkChatModel();
            String answer = chatModel.chat(prompt.text());
            logger.info("reason success, question={}, triplets={}", question, retrieval.getData().size());
            result.setData(answer);
        } catch (Exception e) {
            logger.error("reason error, question={}", question, e);
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询两个实体之间的最短路径
     * @param fromEntityId 起始实体 id
     * @param toEntityId 目标实体 id
     * @return 路径数据
     */
    @Override
    public ResultData<Map<String, Object>> findShortestPath(Long fromEntityId, Long toEntityId) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        Long fromGraphId = getAccessibleEntityGraphId(fromEntityId);
        Long toGraphId = getAccessibleEntityGraphId(toEntityId);
        if (fromGraphId == null || !Objects.equals(fromGraphId, toGraphId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        result.setData(graphStore.findShortestPath(fromEntityId, toEntityId));
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验当前用户对图谱的访问权限
     * @param graphId 图谱 id
     * @return 是否有权限
     */
    private boolean checkGraphAccess(Long graphId) {
        if (graphId == null) {
            return false;
        }
        KgGraphQuery query = new KgGraphQuery();
        query.setId(graphId);
        KgGraphResult graph = kgGraphDao.queryKgGraph(query);
        if (graph == null) {
            return false;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        if (SessionHolder.isSysAdmin() || SessionHolder.isOrgAdmin()) {
            return true;
        }
        DataScopeEnum scope = DataScopeEnum.indexOf(SessionHolder.getCurrentDataScop());
        if (scope == DataScopeEnum.ALL_ACCESS) {
            return true;
        }
        if (scope == DataScopeEnum.ONLY_DEPART) {
            return Objects.equals(graph.getDeptId(), SessionHolder.getCurrentDeptId());
        }
        if (scope == DataScopeEnum.DEPART_AND_SUB_DEPART) {
            String deptIds = SessionHolder.getCurrentDeptIds();
            if (StringUtil.isBlank(deptIds) || graph.getDeptId() == null) {
                return false;
            }
            for (String deptId : deptIds.split(",")) {
                if (String.valueOf(graph.getDeptId()).equals(deptId.trim())) {
                    return true;
                }
            }
            return false;
        }
        return Objects.equals(graph.getCreatedBy(), userId);
    }

    /**
     * 校验实体所属图谱并返回图谱 id
     * @param entityId 实体 id
     * @return 图谱 id
     */
    private Long getAccessibleEntityGraphId(Long entityId) {
        if (entityId == null) {
            return null;
        }
        KgEntityQuery query = new KgEntityQuery();
        query.setId(entityId);
        KgEntityResult entity = kgEntityDao.queryKgEntity(query);
        return entity != null && checkGraphAccess(entity.getGraphId()) ? entity.getGraphId() : null;
    }

    /**
     * 解析 LLM 返回的 JSON 对象，兼容代码块包裹
     * @param response LLM 响应文本
     * @return JSON对象
     */
    private JSONObject parseJsonObject(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }
        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n');
            int end = trimmed.lastIndexOf("```");
            if (start > 0 && end > start) {
                trimmed = trimmed.substring(start + 1, end).trim();
            }
        }
        try {
            return JSONObject.parseObject(trimmed);
        } catch (Exception e) {
            logger.error("parseJsonObject fail, response={}", response, e);
            return null;
        }
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
