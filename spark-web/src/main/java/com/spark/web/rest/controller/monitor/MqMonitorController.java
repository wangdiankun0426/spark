package com.spark.web.rest.controller.monitor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.monitor.entity.MonitorMqSnapshot;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
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
 * 消息队列监控
 */
@RestController
@RequestMapping("monitor/mq")
public class MqMonitorController {

    @Autowired
    private IMonitorService monitorService;

    /**
     * 查询各队列最新快照
     * @return 队列快照列表
     */
    @GetMapping("latest")
    private ResultData<List<MonitorMqSnapshot>> queryLatestMqList() {
        return monitorService.queryLatestMqList();
    }

    /**
     * 查询队列消息积压趋势
     * @param query 查询参数
     * @return 快照序列
     */
    @GetMapping("series")
    private ResultData<List<MonitorMqSnapshot>> queryMqSeries(MonitorSnapshotQuery query) {
        return monitorService.queryMqSeries(query);
    }
}
