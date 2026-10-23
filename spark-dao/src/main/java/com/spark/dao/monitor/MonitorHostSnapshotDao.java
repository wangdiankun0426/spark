package com.spark.dao.monitor;

import com.spark.common.bean.monitor.entity.MonitorHostSnapshot;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
import com.spark.common.bean.monitor.result.MonitorNodeResult;
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
 * 主机与JVM监控快照DAO
 */
public interface MonitorHostSnapshotDao extends BaseDao<MonitorHostSnapshot> {

    /**
     * 查询节点清单与最近快照时间
     * @return 节点清单
     */
    List<MonitorNodeResult> queryNodeList();

    /**
     * 查询节点最新主机快照
     * @param nodeKey 节点标识
     * @return 最新快照
     */
    MonitorHostSnapshot queryLatestHost(@Param("nodeKey") String nodeKey);

    /**
     * 查询主机快照时间序列
     * @param query 查询参数
     * @return 快照序列
     */
    List<MonitorHostSnapshot> queryHostSeries(MonitorSnapshotQuery query);

    /**
     * 物理删除过期快照
     * @param expireTime 过期时间界限
     * @return 删除条数
     */
    int deleteHostExpired(@Param("expireTime") Date expireTime);
}
