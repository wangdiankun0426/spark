package com.spark.dao.monitor;

import com.spark.common.bean.monitor.entity.MonitorDruidSnapshot;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * Druid慢SQL监控快照DAO
 */
public interface MonitorDruidSnapshotDao extends BaseDao<MonitorDruidSnapshot> {

    /**
     * 查询节点最新慢SQL快照
     * @param nodeKey 节点标识
     * @return 最新快照
     */
    MonitorDruidSnapshot queryLatestDruid(@Param("nodeKey") String nodeKey);

    /**
     * 物理删除过期快照
     * @param expireTime 过期时间界限
     * @return 删除条数
     */
    int deleteDruidExpired(@Param("expireTime") Date expireTime);
}
