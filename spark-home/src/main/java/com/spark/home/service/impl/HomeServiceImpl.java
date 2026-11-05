package com.spark.home.service.impl;

import com.spark.chat.service.IChatSpaceService;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.home.query.DashboardQuery;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardOverviewResult;
import com.spark.common.bean.home.result.DashboardUsageSummaryResult;
import com.spark.common.bean.home.result.FlowCountResult;
import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.DateUtil;
import com.spark.flow.service.IFlowInstanceService;
import com.spark.home.service.IHomeService;
import com.spark.llm.service.IAgentService;
import com.spark.llm.service.IModelService;
import com.spark.llm.service.IModelUsageService;
import com.spark.workflow.service.IWorkflowInstanceService;
import com.spark.workflow.service.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 19:00:00
 * 工作台统计服务实现
 */
@Service
public class HomeServiceImpl implements IHomeService {

    /**
     * 综合推荐榜条数
     */
    private static final int RECOMMEND_LIMIT = 8;

    /**
     * 热门榜单条数
     */
    private static final int HOT_LIMIT = 5;

    @Autowired
    private IModelUsageService modelUsageService;
    @Autowired
    private IChatSpaceService chatSpaceService;
    @Autowired
    private IFlowInstanceService flowInstanceService;
    @Autowired
    private IAgentService agentService;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IWorkflowService workflowService;
    @Autowired
    private IWorkflowInstanceService workflowInstanceService;

    /**
     * 工作台总览，模型用量与资产数量合并返回
     * @return 总览数据
     */
    @Override
    public ResultData<DashboardOverviewResult> queryOverview() {
        ResultData<DashboardOverviewResult> result = new ResultData<>();
        DashboardOverviewResult overview = new DashboardOverviewResult();
        DashboardUsageSummaryResult summary = modelUsageService.queryMyUsageSummary(null, null);
        overview.setCallCount(summary.getCallCount());
        overview.setInputTokens(summary.getInputTokens());
        overview.setOutputTokens(summary.getOutputTokens());
        overview.setTotalTokens(summary.getTotalTokens());
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setType(ModelTypeEnum.LANGUAGE.getType());
        modelQuery.setStatus(StatusEnum.NORMAL.getValue());
        ResultData<Integer> modelResult = modelService.countMyModels(modelQuery);
        overview.setModelCount(modelResult.getData());
        AgentQuery agentQuery = new AgentQuery();
        agentQuery.setStatus(StatusEnum.NORMAL.getValue());
        ResultData<Integer> agentResult = agentService.countMyAgents(agentQuery);
        overview.setAgentCount(agentResult.getData());
        WfTemplateQuery workflowQuery = new WfTemplateQuery();
        workflowQuery.setStatus(StatusEnum.NORMAL.getValue());
        ResultData<Integer> workflowResult = workflowService.countMyWorkflows(workflowQuery);
        overview.setWorkflowCount(workflowResult.getData());
        result.setData(overview);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 我的用量趋势
     * @param query 查询参数
     * @return 趋势列表
     */
    @Override
    public ResultData<List<ModelUsageTrendResult>> queryUsageTrend(DashboardQuery query) {
        ResultData<List<ModelUsageTrendResult>> result = new ResultData<>();
        String[] timeRange = this.buildTimeRange(query);
        List<ModelUsageTrendResult> trend = modelUsageService.queryMyUsageTrend(timeRange[0], timeRange[1]);
        result.setData(trend);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 我的流程四项数量，待办、已办、申请、抄送
     * @return 流程数量
     */
    @Override
    public ResultData<FlowCountResult> queryFlowCount() {
        ResultData<FlowCountResult> result = new ResultData<>();
        FlowCountResult flowCount = new FlowCountResult();
        Integer todoCount = flowInstanceService.countMyTodo();
        flowCount.setTodoCount(todoCount);
        Integer doneCount = flowInstanceService.countMyDone();
        flowCount.setDoneCount(doneCount);
        Integer applicationCount = flowInstanceService.countMyApplication();
        flowCount.setApplicationCount(applicationCount);
        Integer copyCount = flowInstanceService.countMyCopy();
        flowCount.setCopyCount(copyCount);
        result.setData(flowCount);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 综合推荐榜
     * @return 推荐榜
     */
    @Override
    public ResultData<List<DashboardHotResult>> queryRecommend() {
        ResultData<List<DashboardHotResult>> result = new ResultData<>();
        List<DashboardHotResult> models = this.queryHotModels();
        List<DashboardHotResult> agents = this.queryHotAgents();
        List<DashboardHotResult> workflows = this.queryHotWorkflows();
        List<DashboardHotResult> merged = new ArrayList<>();
        merged.addAll(models);
        merged.addAll(agents);
        merged.addAll(workflows);
        merged.removeIf(item -> item.getName() == null || item.getCount() == null);
        Comparator<DashboardHotResult> comparator = Comparator.comparing(DashboardHotResult::getCount).reversed();
        merged.sort(comparator);
        List<DashboardHotResult> recommend = merged;
        if (merged.size() > RECOMMEND_LIMIT) {
            List<DashboardHotResult> topList = merged.subList(0, RECOMMEND_LIMIT);
            recommend = new ArrayList<>(topList);
        }
        result.setData(recommend);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 热门模型
     * @return 榜单
     */
    private List<DashboardHotResult> queryHotModels() {
        List<DashboardHotResult> list = modelUsageService.queryMyHotModels(HOT_LIMIT);
        if (CollectionUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        for (DashboardHotResult item : list) {
            item.setHotType("model");
        }
        return list;
    }

    /**
     * 常用智能体
     * @return 榜单
     */
    private List<DashboardHotResult> queryHotAgents() {
        List<DashboardHotResult> list = chatSpaceService.queryMyHotAgents(HOT_LIMIT);
        if (CollectionUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list;
    }

    /**
     * 常用工作流
     * @return 榜单
     */
    private List<DashboardHotResult> queryHotWorkflows() {
        List<DashboardHotResult> list = workflowInstanceService.queryMyHotWorkflows(HOT_LIMIT);
        if (CollectionUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        for (DashboardHotResult item : list) {
            item.setHotType("workflow");
        }
        return list;
    }

    /**
     * 构造近N天时间范围，开始为N天前零点，结束为当前；rangeDays 为空时返回 null 占位，Mapper 判空后查全部
     * @param query 查询参数
     * @return [开始时间, 结束时间]，不限范围时两个元素均为 null
     */
    private String[] buildTimeRange(DashboardQuery query) {
        if (query == null || query.getRangeDays() == null) {
            return new String[]{null, null};
        }
        int days = query.getRangeDays();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.YYYYMMDD_HHMMSS);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days - 1L).toLocalDate().atStartOfDay();
        return new String[]{start.format(formatter), end.format(formatter)};
    }
}
