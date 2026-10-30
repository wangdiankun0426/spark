package com.spark.web.rest.controller.monitor;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
import com.spark.common.bean.monitor.result.MonitorApiTrendResult;
import com.spark.common.bean.monitor.result.MonitorUriStatResult;
import com.spark.monitor.service.IMonitorService;
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
 * @since 2026-10-23 10:00:00
 * 接口请求监控
 */
@RestController
@RequestMapping("monitor/api")
public class ApiMonitorController {

    @Autowired
    private IMonitorService monitorService;

    /**
     * 查询接口请求汇总
     * @param query 查询参数
     * @return 汇总
     */
    @GetMapping("summary")
    private ResultData<MonitorApiTrendResult> queryApiSummary(MonitorSnapshotQuery query) {
        return monitorService.queryApiSummary(query);
    }

    /**
     * 查询接口请求分钟趋势
     * @param query 查询参数
     * @return 分钟趋势
     */
    @GetMapping("trend")
    private ResultData<List<MonitorApiTrendResult>> queryApiTrend(MonitorSnapshotQuery query) {
        return monitorService.queryApiTrend(query);
    }

    /**
     * 分页查询URI维度统计（全量聚合，按调用次数倒排）
     * @param query 查询参数
     * @return URI统计分页
     */
    @GetMapping("uriStats")
    private ResultData<PageResult<MonitorUriStatResult>> queryUriStats(MonitorSnapshotQuery query) {
        return monitorService.queryUriStats(query);
    }
}
