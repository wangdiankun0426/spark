package com.spark.web.rest.controller.monitor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.monitor.entity.MonitorRedisSnapshot;
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
 * Redis缓存监控
 */
@RestController
@RequestMapping("monitor/redis")
public class RedisMonitorController {

    @Autowired
    private IMonitorService monitorService;

    /**
     * 查询最新Redis快照
     * @return Redis快照
     */
    @GetMapping("latest")
    private ResultData<MonitorRedisSnapshot> queryLatestRedis() {
        return monitorService.queryLatestRedis();
    }

    /**
     * 查询Redis快照趋势
     * @param query 查询参数
     * @return 快照序列
     */
    @GetMapping("series")
    private ResultData<List<MonitorRedisSnapshot>> queryRedisSeries(MonitorSnapshotQuery query) {
        return monitorService.queryRedisSeries(query);
    }
}
