package com.spark.monitor.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.monitor.entity.MonitorDruidSnapshot;
import com.spark.common.bean.monitor.entity.MonitorHostSnapshot;
import com.spark.common.bean.monitor.entity.MonitorMqSnapshot;
import com.spark.common.bean.monitor.entity.MonitorRedisSnapshot;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
import com.spark.common.bean.monitor.result.MonitorApiTrendResult;
import com.spark.common.bean.monitor.result.MonitorNodeResult;
import com.spark.common.bean.monitor.result.MonitorUriStatResult;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 监控查询服务
 */
public interface IMonitorService {

    /**
     * 查询节点清单与在线状态
     * @return 节点清单
     */
    ResultData<List<MonitorNodeResult>> queryNodeList();

    /**
     * 查询节点最新主机快照
     * @param query 查询参数
     * @return 主机快照
     */
    ResultData<MonitorHostSnapshot> queryLatestHost(MonitorSnapshotQuery query);

    /**
     * 查询主机快照趋势
     * @param query 查询参数
     * @return 快照序列
     */
    ResultData<List<MonitorHostSnapshot>> queryHostSeries(MonitorSnapshotQuery query);

    /**
     * 查询节点最新慢SQL快照
     * @param query 查询参数
     * @return 慢SQL快照
     */
    ResultData<MonitorDruidSnapshot> queryLatestDruid(MonitorSnapshotQuery query);

    /**
     * 查询最新Redis快照
     * @return Redis快照
     */
    ResultData<MonitorRedisSnapshot> queryLatestRedis();

    /**
     * 查询Redis快照趋势
     * @param query 查询参数
     * @return 快照序列
     */
    ResultData<List<MonitorRedisSnapshot>> queryRedisSeries(MonitorSnapshotQuery query);

    /**
     * 查询各队列最新快照
     * @return 队列快照列表
     */
    ResultData<List<MonitorMqSnapshot>> queryLatestMqList();

    /**
     * 查询队列消息积压趋势
     * @param query 查询参数
     * @return 快照序列
     */
    ResultData<List<MonitorMqSnapshot>> queryMqSeries(MonitorSnapshotQuery query);

    /**
     * 查询接口请求汇总
     * @param query 查询参数
     * @return 汇总
     */
    ResultData<MonitorApiTrendResult> queryApiSummary(MonitorSnapshotQuery query);

    /**
     * 查询接口请求分钟趋势
     * @param query 查询参数
     * @return 分钟趋势
     */
    ResultData<List<MonitorApiTrendResult>> queryApiTrend(MonitorSnapshotQuery query);

    /**
     * 分页查询URI维度统计（全量聚合，按调用次数倒排）
     * @param query 查询参数
     * @return URI统计分页
     */
    ResultData<PageResult<MonitorUriStatResult>> queryUriStats(MonitorSnapshotQuery query);
}
