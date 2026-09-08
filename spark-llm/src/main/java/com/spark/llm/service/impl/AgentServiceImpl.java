package com.spark.llm.service.impl;

import com.spark.common.bean.base.SessionHolder;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.bean.llm.entity.Agent;
import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.query.McpQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.query.SkillQuery;
import com.spark.common.bean.llm.result.AgentResult;
import com.spark.common.bean.llm.result.McpResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.bean.llm.result.SkillResult;
import com.spark.common.bean.llm.vo.AgentVO;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.llm.AgentDao;
import com.spark.dao.llm.McpDao;
import com.spark.dao.llm.ModelDao;
import com.spark.dao.llm.SkillDao;
import com.spark.common.enums.AgentToolEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.service.IAgentService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.BaseService;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-17 10:12:34
 */
@Service
public class AgentServiceImpl extends BaseService<AgentQuery, AgentResult> implements IAgentService {
    private final static Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private AgentFactory agentFactory;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private McpDao mcpDao;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private SkillDao skillDao;

    /**
     * 创建智能体
     * @param agentVO 智能体数据
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.AGENT_INSERT)
    public ResultData<Void> createAgent(AgentVO agentVO) {
        ResultData<Void> result = new ResultData<>();
        if (agentVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Agent agent = new Agent();
        BeanUtil.copyProperties(agentVO, agent);
        Long id = super.genObjectId(ObjectTypeEnum.AGENT);
        agent.setId(id);
        int count = agentDao.insertDB(agent);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(agent.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改智能体
     * @param agentVO 智能体数据
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.AGENT_UPDATE)
    public ResultData<Void> updateAgent(AgentVO agentVO) {
        ResultData<Void> result = new ResultData<>();
        if (agentVO == null || agentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AgentQuery agentQuery = new AgentQuery();
        agentQuery.setId(agentVO.getId());
        AgentResult agentResult = agentDao.queryAgent(agentQuery);
        if (agentResult == null) {
            result.setErrorCode(ErrorCodeEnum.AGENT_NOT_EXIST);
            return result;
        }
        Agent agent = new Agent();
        BeanUtil.copyProperties(agentVO, agent);
        int count = agentDao.updateDBById(agent);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 清掉agent缓存
        agentFactory.clearAgentCache(agentVO.getId());
        result.setObjId(agent.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除智能体
     * @param agentVO 智能体数据
     * @return  删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.AGENT_DELETE)
    public ResultData<Void> deleteAgent(AgentVO agentVO) {
        ResultData<Void> result = new ResultData<>();
        if (agentVO == null || agentVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AgentQuery agentQuery = new AgentQuery();
        agentQuery.setId(agentVO.getId());
        AgentResult agentResult = agentDao.queryAgent(agentQuery);
        if (agentResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Agent agent = new Agent();
        agent.setId(agentVO.getId());
        int count = agentDao.deleteDBById(agent);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 清掉agent缓存
        agentFactory.clearAgentCache(agentVO.getId());
        result.setObjId(agent.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询智能体
     * @param query 查询智能体条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<AgentResult>> pageAgentList(AgentQuery query) {
        ResultData<PageResult<AgentResult>> result = new ResultData<>();
        if (query == null) {
            query = new AgentQuery();
        }
        query.setTenantId(SessionHolder.getCurrentTenantId());
        PageResult<AgentResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询智能体详情
     * @param query 查询智能体条件
     * @return 详情
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.AGENT_DETAIL)
    public ResultData<AgentResult> queryAgentDetail(AgentQuery query) {
        ResultData<AgentResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        AgentResult agentResult = agentDao.queryAgent(query);
        if (agentResult == null) {
            result.setErrorCode(ErrorCodeEnum.AGENT_NOT_EXIST);
            return result;
        }
        result.setData(agentResult);
        result.setObjId(agentResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<AgentResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        Map<AgentResult, List<Long>> agentKbIdMap = new HashMap<>();
        Map<AgentResult, List<Long>> agentGraphIdMap = new HashMap<>();
        Map<AgentResult, List<Long>> agentMcpIdMap = new HashMap<>();
        Map<AgentResult, List<Long>> agentSkillIdMap = new HashMap<>();
        Map<AgentResult, Long> agentChatModelIdMap = new HashMap<>();
        list.forEach(agentResult -> {
            agentResult.setStatusName(StatusEnum.indexOf(agentResult.getStatus()).getDesc());
            if (agentResult.getChatModelId() != null) {
                agentChatModelIdMap.put(agentResult, agentResult.getChatModelId());
            }
            List<String> tools = agentFactory.parseTools(agentResult.getTools());
            if (CollectionUtil.isNotEmpty(tools)) {
                String toolsStr = tools.stream().map(tool -> AgentToolEnum.indexOf(tool).getDesc()).collect(Collectors.joining(","));
                agentResult.setToolNames(toolsStr);
            }
            List<Long> kbIds = agentFactory.parseKbIds(agentResult.getKbIds());
            agentKbIdMap.put(agentResult, kbIds);
            List<Long> graphIds = agentFactory.parseGraphIds(agentResult.getGraphIds());
            agentGraphIdMap.put(agentResult, graphIds);
            List<Long> mcpIds = agentFactory.parseMcpIds(agentResult.getMcpIds());
            agentMcpIdMap.put(agentResult, mcpIds);
            List<Long> skillIds = agentFactory.parseSkillIds(agentResult.getSkills());
            agentSkillIdMap.put(agentResult, skillIds);
        });
        supplyChatModelName(agentChatModelIdMap);
        supplyKbNames(agentKbIdMap);
        supplyGraphNames(agentGraphIdMap);
        supplyMcpNames(agentMcpIdMap);
        supplySkillNames(agentSkillIdMap);
    }

    /**
     * 批量补充语言模型名称
     * @param agentChatModelIdMap 智能体与语言模型ID的映射
     */
    private void supplyChatModelName(Map<AgentResult, Long> agentChatModelIdMap) {
        if (agentChatModelIdMap.isEmpty()) {
            return;
        }
        Set<Long> modelIdSet = new HashSet<>(agentChatModelIdMap.values());
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setPage(false);
        modelQuery.setIds(new ArrayList<>(modelIdSet));
        List<ModelResult> modelResults = modelDao.queryModelList(modelQuery);
        if (CollectionUtil.isEmpty(modelResults)) {
            return;
        }
        Map<Long, String> modelNameMap = modelResults.stream().collect(Collectors.toMap(ModelResult::getId, ModelResult::getName));
        agentChatModelIdMap.forEach((agentResult, modelId) -> {
            String modelName = modelNameMap.get(modelId);
            if (StringUtil.isNotBlank(modelName)) {
                agentResult.setChatModelName(modelName);
            }
        });
    }

    /**
     * 批量补充知识库名称
     * @param agentKbIdMap 智能体与知识库ID列表的映射
     */
    private void supplyKbNames(Map<AgentResult, List<Long>> agentKbIdMap) {
        Set<Long> kbIdSet = new HashSet<>();
        agentKbIdMap.values().forEach(kbIdSet::addAll);
        if (CollectionUtil.isEmpty(kbIdSet)) {
            return;
        }
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        knowledgeQuery.setPage(false);
        knowledgeQuery.setIds(new ArrayList<>(kbIdSet));
        List<KnowledgeResult> knowledgeResults = knowledgeDao.queryKnowledgeList(knowledgeQuery);
        if (CollectionUtil.isEmpty(knowledgeResults)) {
            return;
        }
        Map<Long, String> kbNameMap = knowledgeResults.stream().collect(Collectors.toMap(KnowledgeResult::getId, KnowledgeResult::getName));
        agentKbIdMap.forEach((agentResult, kbIds) -> {
            if (CollectionUtil.isEmpty(kbIds)) {
                return;
            }
            String kbNames = kbIds.stream()
                    .map(kbId -> kbNameMap.getOrDefault(kbId, "未知知识库"))
                    .collect(Collectors.joining(","));
            agentResult.setKbNames(kbNames);
        });
    }

    /**
     * 批量补充知识图谱名称
     * @param agentGraphIdMap 智能体与知识图谱ID列表的映射
     */
    private void supplyGraphNames(Map<AgentResult, List<Long>> agentGraphIdMap) {
        Set<Long> graphIdSet = new HashSet<>();
        agentGraphIdMap.values().forEach(graphIdSet::addAll);
        if (CollectionUtil.isEmpty(graphIdSet)) {
            return;
        }
        KgGraphQuery kgGraphQuery = new KgGraphQuery();
        kgGraphQuery.setPage(false);
        kgGraphQuery.setIds(new ArrayList<>(graphIdSet));
        List<KgGraphResult> kgGraphResults = kgGraphDao.queryKgGraphList(kgGraphQuery);
        if (CollectionUtil.isEmpty(kgGraphResults)) {
            return;
        }
        Map<Long, String> graphNameMap = kgGraphResults.stream().collect(Collectors.toMap(KgGraphResult::getId, KgGraphResult::getName));
        agentGraphIdMap.forEach((agentResult, graphIds) -> {
            if (CollectionUtil.isEmpty(graphIds)) {
                return;
            }
            String graphNames = graphIds.stream()
                    .map(graphId -> graphNameMap.getOrDefault(graphId, "未知知识图谱"))
                    .collect(Collectors.joining(","));
            agentResult.setGraphNames(graphNames);
        });
    }

    /**
     * 批量补充MCP服务器名称
     * @param agentMcpIdMap 智能体与MCP服务器ID列表的映射
     */
    private void supplyMcpNames(Map<AgentResult, List<Long>> agentMcpIdMap) {
        Set<Long> mcpIdSet = new HashSet<>();
        agentMcpIdMap.values().forEach(mcpIdSet::addAll);
        if (CollectionUtil.isEmpty(mcpIdSet)) {
            return;
        }
        McpQuery mcpQuery = new McpQuery();
        mcpQuery.setPage(false);
        mcpQuery.setIds(new ArrayList<>(mcpIdSet));
        List<McpResult> mcpResults = mcpDao.queryMcpList(mcpQuery);
        if (CollectionUtil.isEmpty(mcpResults)) {
            return;
        }
        Map<Long, String> mcpNameMap = mcpResults.stream().collect(Collectors.toMap(McpResult::getId, McpResult::getName));
        agentMcpIdMap.forEach((agentResult, mcpIds) -> {
            if (CollectionUtil.isEmpty(mcpIds)) {
                return;
            }
            String mcpNames = mcpIds.stream()
                    .map(mcpId -> mcpNameMap.getOrDefault(mcpId, "未知MCP服务器"))
                    .collect(Collectors.joining(","));
            agentResult.setMcpNames(mcpNames);
        });
    }

    /**
     * 批量补充技能名称
     * @param agentSkillIdMap 智能体与技能ID列表的映射
     */
    private void supplySkillNames(Map<AgentResult, List<Long>> agentSkillIdMap) {
        Set<Long> skillIdSet = new HashSet<>();
        agentSkillIdMap.values().forEach(skillIdSet::addAll);
        if (CollectionUtil.isEmpty(skillIdSet)) {
            return;
        }
        SkillQuery skillQuery = new SkillQuery();
        skillQuery.setPage(false);
        skillQuery.setIds(new ArrayList<>(skillIdSet));
        List<SkillResult> skillResults = skillDao.querySkillList(skillQuery);
        if (CollectionUtil.isEmpty(skillResults)) {
            return;
        }
        Map<Long, String> skillNameMap = skillResults.stream().collect(Collectors.toMap(SkillResult::getId, SkillResult::getName));
        agentSkillIdMap.forEach((agentResult, skillIds) -> {
            if (CollectionUtil.isEmpty(skillIds)) {
                return;
            }
            String skillNames = skillIds.stream()
                    .map(skillId -> skillNameMap.getOrDefault(skillId, "未知技能"))
                    .collect(Collectors.joining(","));
            agentResult.setSkillNames(skillNames);
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(AgentQuery query) {
        return agentDao.queryAgentCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<AgentResult> queryList(AgentQuery query) {
        return agentDao.queryAgentList(query);
    }
}
