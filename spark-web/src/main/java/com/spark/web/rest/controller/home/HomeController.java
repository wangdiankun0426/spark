package com.spark.web.rest.controller.home;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.home.query.DashboardQuery;
import com.spark.common.bean.home.result.DashboardHotResult;
import com.spark.common.bean.home.result.DashboardOverviewResult;
import com.spark.common.bean.home.result.FlowCountResult;
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
 * 工作台统计服务接口
 */
@RestController
@RequestMapping("home")
public class HomeController {

    @Autowired
    private IHomeService homeService;

    /**
     * 工作台总览，模型用量与资产数量合并返回
     * @return 总览数据
     */
    @GetMapping("overview")
    public ResultData<DashboardOverviewResult> overview() {
        return homeService.queryOverview();
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
     * 我的流程四项数量，待办、已办、申请、抄送
     * @return 流程数量
     */
    @GetMapping("flowCount")
    public ResultData<FlowCountResult> flowCount() {
        return homeService.queryFlowCount();
    }

    /**
     * 综合推荐榜
     * @return 推荐榜
     */
    @GetMapping("recommend")
    public ResultData<List<DashboardHotResult>> recommend() {
        return homeService.queryRecommend();
    }
}
