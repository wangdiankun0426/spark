package com.spark.dao.monitor;

import com.spark.common.bean.monitor.entity.MonitorRedisSnapshot;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
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
 * Redis监控快照DAO
 */
public interface MonitorRedisSnapshotDao extends BaseDao<MonitorRedisSnapshot> {

    /**
     * 查询最新Redis快照
     * @return 最新快照
     */
    MonitorRedisSnapshot queryLatestRedis();

    /**
     * 查询Redis快照时间序列
     * @param query 查询参数
     * @return 快照序列
     */
    List<MonitorRedisSnapshot> queryRedisSeries(MonitorSnapshotQuery query);

    /**
     * 物理删除过期快照
     * @param expireTime 过期时间界限
     * @return 删除条数
     */
    int deleteRedisExpired(@Param("expireTime") Date expireTime);
}
