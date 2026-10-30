package com.spark.dao.monitor;

import com.spark.common.bean.monitor.entity.MonitorApiSnapshot;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
import com.spark.common.bean.monitor.result.MonitorApiTrendResult;
import com.spark.common.bean.monitor.result.MonitorUriStatResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 接口请求监控快照DAO
 */
public interface MonitorApiSnapshotDao extends BaseDao<MonitorApiSnapshot> {

    /**
     * 查询接口请求分钟趋势
     * @param query 查询参数
     * @return 分钟趋势
     */
    List<MonitorApiTrendResult> queryApiTrend(MonitorSnapshotQuery query);

    /**
     * 查询时间范围内接口汇总
     * @param query 查询参数
     * @return 汇总
     */
    MonitorApiTrendResult queryApiSummary(MonitorSnapshotQuery query);

    /**
     * 统计URI维度聚合组数
     * @param query 查询参数
     * @return 组数
     */
    int queryUriStatsCount(MonitorSnapshotQuery query);

    /**
     * 分页查询URI维度聚合统计（按调用次数倒排）
     * @param query 查询参数
     * @return URI统计
     */
    List<MonitorUriStatResult> queryUriStats(MonitorSnapshotQuery query);

    /**
     * 物理删除过期快照
     * @param expireTime 过期时间界限
     * @return 删除条数
     */
    int deleteApiExpired(@Param("expireTime") Date expireTime);
}
