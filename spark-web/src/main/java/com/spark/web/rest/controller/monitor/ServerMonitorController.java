package com.spark.web.rest.controller.monitor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.monitor.entity.MonitorDruidSnapshot;
import com.spark.common.bean.monitor.entity.MonitorHostSnapshot;
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
 * 服务器与JVM监控
 */
@RestController
@RequestMapping("monitor/server")
public class ServerMonitorController {

    @Autowired
    private IMonitorService monitorService;

    /**
     * 查询节点最新主机快照
     * @param query 查询参数
     * @return 主机快照
     */
    @GetMapping("latest")
    private ResultData<MonitorHostSnapshot> queryLatestHost(MonitorSnapshotQuery query) {
        return monitorService.queryLatestHost(query);
    }

    /**
     * 查询主机快照趋势
     * @param query 查询参数
     * @return 快照序列
     */
    @GetMapping("series")
    private ResultData<List<MonitorHostSnapshot>> queryHostSeries(MonitorSnapshotQuery query) {
        return monitorService.queryHostSeries(query);
    }

    /**
     * 查询节点最新慢SQL快照
     * @param query 查询参数
     * @return 慢SQL快照
     */
    @GetMapping("druid")
    private ResultData<MonitorDruidSnapshot> queryLatestDruid(MonitorSnapshotQuery query) {
        return monitorService.queryLatestDruid(query);
    }
}
