package com.spark.home.service.impl;

import com.spark.chat.service.IChatSpaceService;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.home.query.DashboardQuery;
import com.spark.common.bean.home.result.DashboardAssetResult;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardUsageSummaryResult;
import com.spark.common.bean.home.result.FlowCountResult;
import com.spark.common.bean.flow.query.FlowInstanceQuery;
import com.spark.common.bean.flow.query.FlowTemplateQuery;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.flow.result.FlowTemplateResult;
import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.DateUtil;
import com.spark.flow.service.IFlowInstanceService;
import com.spark.flow.service.IFlowTemplateService;
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
 * 我的工作台统计服务实现，编排各域个人统计方法
 */
@Service
public class HomeServiceImpl implements IHomeService {

    /**
     * 综合推荐榜条数
     */
    private static final int RECOMMEND_LIMIT = 8;

    /**
     * 待办列表条数
     */
    private static final int TODO_LIMIT = 5;

    /**
     * 可申请流程条数
     */
    private static final int APPLY_LIMIT = 8;

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
    private IFlowTemplateService flowTemplateService;
    @Autowired
    private IAgentService agentService;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IWorkflowService workflowService;
    @Autowired
    private IWorkflowInstanceService workflowInstanceService;

    /**
     * 我的用量汇总
     * @param query 查询参数
     * @return 用量汇总
     */
    @Override
    public ResultData<DashboardUsageSummaryResult> queryUsageSummary(DashboardQuery query) {
        ResultData<DashboardUsageSummaryResult> result = new ResultData<>();
        String[] timeRange = this.buildTimeRange(query);
        DashboardUsageSummaryResult summary = modelUsageService.queryMyUsageSummary(timeRange[0], timeRange[1]);
        result.setData(summary);
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
     * 我的待办总数
     * @return 待办数
     */
    @Override
    public ResultData<Integer> countTodo() {
        ResultData<Integer> result = new ResultData<>();
        Integer todoCount = flowInstanceService.countMyTodo();
        result.setData(todoCount);
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
     * 可申请流程模板
     * @return 模板列表
     */
    @Override
    public ResultData<List<DashboardHotResult>> queryApplyTemplates() {
        ResultData<List<DashboardHotResult>> result = new ResultData<>();
        FlowTemplateQuery query = new FlowTemplateQuery();
        query.setPageNo(1);
        query.setPageSize(APPLY_LIMIT);
        ResultData<PageResult<FlowTemplateResult>> data = flowTemplateService.pageTemplateList(query);
        List<DashboardHotResult> list = new ArrayList<>();
        if (data == null || data.getCode() != ResultData.OK || data.getData() == null
                || CollectionUtil.isEmpty(data.getData().getRows())) {
            result.setData(list);
            result.setCode(ResultData.OK);
            return result;
        }
        for (FlowTemplateResult template : data.getData().getRows()) {
            DashboardHotResult item = new DashboardHotResult();
            item.setId(template.getId());
            item.setName(template.getName());
            item.setHotType("flow");
            list.add(item);
        }
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 我的待办前N条
     * @return 待办列表
     */
    @Override
    public ResultData<List<FlowInstanceResult>> queryTodoList() {
        ResultData<List<FlowInstanceResult>> result = new ResultData<>();
        FlowInstanceQuery query = new FlowInstanceQuery();
        query.setPageNo(1);
        query.setPageSize(TODO_LIMIT);
        ResultData<PageResult<FlowInstanceResult>> data = flowInstanceService.pageMyTodoList(query);
        List<FlowInstanceResult> list = new ArrayList<>();
        if (data == null || data.getCode() != ResultData.OK || data.getData() == null
                || CollectionUtil.isEmpty(data.getData().getRows())) {
            result.setData(list);
            result.setCode(ResultData.OK);
            return result;
        }
        List<FlowInstanceResult> rows = data.getData().getRows();
        result.setData(rows);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 综合推荐榜
     * @param query 查询参数
     * @return 推荐榜
     */
    @Override
    public ResultData<List<DashboardHotResult>> queryRecommend(DashboardQuery query) {
        ResultData<List<DashboardHotResult>> result = new ResultData<>();
        List<DashboardHotResult> models = this.queryHotModels(query);
        List<DashboardHotResult> agents = this.queryHotAgents(query);
        List<DashboardHotResult> workflows = this.queryHotWorkflows(query);
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
     * 我的资产数量
     * @return 资产数量
     */
    @Override
    public ResultData<DashboardAssetResult> queryAsset() {
        ResultData<DashboardAssetResult> result = new ResultData<>();
        DashboardAssetResult asset = new DashboardAssetResult();
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setType(ModelTypeEnum.LANGUAGE.getType());
        modelQuery.setStatus(StatusEnum.NORMAL.getValue());
        ResultData<Integer> modelResult = modelService.countMyModels(modelQuery);
        Integer modelCount = modelResult.getData();
        asset.setModelCount(modelCount);
        AgentQuery agentQuery = new AgentQuery();
        agentQuery.setStatus(StatusEnum.NORMAL.getValue());
        ResultData<Integer> agentResult = agentService.countMyAgents(agentQuery);
        Integer agentCount = agentResult.getData();
        asset.setAgentCount(agentCount);
        WfTemplateQuery workflowQuery = new WfTemplateQuery();
        workflowQuery.setStatus(StatusEnum.NORMAL.getValue());
        ResultData<Integer> workflowResult = workflowService.countMyWorkflows(workflowQuery);
        Integer workflowCount = workflowResult.getData();
        asset.setWorkflowCount(workflowCount);
        result.setData(asset);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 热门模型
     * @param query 查询参数
     * @return 榜单
     */
    private List<DashboardHotResult> queryHotModels(DashboardQuery query) {
        String[] timeRange = this.buildTimeRange(query);
        List<DashboardHotResult> list = modelUsageService.queryMyHotModels(timeRange[0], timeRange[1], HOT_LIMIT);
        for (DashboardHotResult item : list) {
            item.setHotType("model");
        }
        return list;
    }

    /**
     * 常用智能体
     * @param query 查询参数
     * @return 榜单
     */
    private List<DashboardHotResult> queryHotAgents(DashboardQuery query) {
        String[] timeRange = this.buildTimeRange(query);
        return chatSpaceService.queryMyHotAgents(timeRange[0], timeRange[1], HOT_LIMIT);
    }

    /**
     * 常用工作流
     * @param query 查询参数
     * @return 榜单
     */
    private List<DashboardHotResult> queryHotWorkflows(DashboardQuery query) {
        String[] timeRange = this.buildTimeRange(query);
        List<DashboardHotResult> list = workflowInstanceService.queryMyHotWorkflows(timeRange[0], timeRange[1], HOT_LIMIT);
        for (DashboardHotResult item : list) {
            item.setHotType("workflow");
        }
        return list;
    }

    /**
     * 构造近N天时间范围，开始为N天前零点，结束为当前
     * @param query 查询参数
     * @return [开始时间, 结束时间]
     */
    private String[] buildTimeRange(DashboardQuery query) {
        int days = query == null || query.getRangeDays() == null ? 30 : query.getRangeDays();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.YYYYMMDD_HHMMSS);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days - 1L).toLocalDate().atStartOfDay();
        return new String[]{start.format(formatter), end.format(formatter)};
    }
}
