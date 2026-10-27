package com.spark.web.rest.controller.llm;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.ModelUsageQuery;
import com.spark.common.bean.llm.result.ModelUsageResult;
import com.spark.common.bean.llm.result.ModelUsageSummaryResult;
import com.spark.common.bean.llm.result.ModelUsageTrendResult;
import com.spark.llm.service.IModelUsageService;
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
 * @since 2026/10/27 10:00:00
 */
@RestController
@RequestMapping("llm/modelUsage")
public class ModelUsageController {

    @Autowired
    private IModelUsageService modelUsageService;

    /**
     * 分页查询模型用量明细
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<ModelUsageResult>> pageUsageList(ModelUsageQuery query) {
        return modelUsageService.pageUsageList(query);
    }

    /**
     * 按模型类型汇总用量
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("summary")
    public ResultData<List<ModelUsageSummaryResult>> queryUsageSummary(ModelUsageQuery query) {
        return modelUsageService.queryUsageSummary(query);
    }

    /**
     * 按日期与类型查询用量趋势
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("trend")
    public ResultData<List<ModelUsageTrendResult>> queryUsageTrend(ModelUsageQuery query) {
        return modelUsageService.queryUsageTrend(query);
    }

}
