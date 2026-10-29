package com.spark.web.rest.controller.home;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.home.query.DashboardQuery;
import com.spark.common.bean.home.result.DashboardAssetResult;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardUsageSummaryResult;
import com.spark.common.bean.home.result.FlowCountResult;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;
import com.spark.home.service.IHomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 19:00:00
 * 我的工作台统计面板，按卡片独立提供数据
 */
@RestController
@RequestMapping("home")
public class HomeController {

    @Autowired
    private IHomeService homeService;

    /**
     * 我的用量汇总
     * @param query 查询参数
     * @return 用量汇总
     */
    @GetMapping("usageSummary")
    public ResultData<DashboardUsageSummaryResult> usageSummary(DashboardQuery query) {
        return homeService.queryUsageSummary(query);
    }

    /**
     * 我的用量趋势
     * @param query 查询参数
     * @return 趋势列表
     */
    @GetMapping("usageTrend")
    public ResultData<List<ModelUsageTrendResult>> usageTrend(DashboardQuery query) {
        return homeService.queryUsageTrend(query);
    }

    /**
     * 我的待办总数
     * @return 待办数
     */
    @GetMapping("todoTotal")
    public ResultData<Integer> todoTotal() {
        return homeService.countTodo();
    }

    /**
     * 我的待办列表
     * @return 待办列表
     */
    @GetMapping("todoList")
    public ResultData<List<FlowInstanceResult>> todoList() {
        return homeService.queryTodoList();
    }

    /**
     * 我的流程四项数量，待办、已办、申请、抄送
     * @return 流程数量
     */
    @GetMapping("flowCount")
    public ResultData<FlowCountResult> flowCount() {
        return homeService.queryFlowCount();
    }

    /**
     * 可申请流程模板
     * @return 模板列表
     */
    @GetMapping("applyTemplates")
    public ResultData<List<DashboardHotResult>> applyTemplates() {
        return homeService.queryApplyTemplates();
    }

    /**
     * 综合推荐榜
     * @param query 查询参数
     * @return 推荐榜
     */
    @GetMapping("recommend")
    public ResultData<List<DashboardHotResult>> recommend(DashboardQuery query) {
        return homeService.queryRecommend(query);
    }

    /**
     * 我的资产数量
     * @return 资产数量
     */
    @GetMapping("asset")
    public ResultData<DashboardAssetResult> asset() {
        return homeService.queryAsset();
    }
}
