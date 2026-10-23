package com.spark.monitor.task;

import com.spark.common.bean.monitor.entity.MonitorApiSnapshot;
import com.spark.common.bean.monitor.entity.MonitorDruidSnapshot;
import com.spark.common.bean.monitor.entity.MonitorHostSnapshot;
import com.spark.common.bean.monitor.entity.MonitorMqSnapshot;
import com.spark.common.bean.monitor.entity.MonitorRedisSnapshot;
import com.spark.dao.monitor.MonitorApiSnapshotDao;
import com.spark.dao.monitor.MonitorDruidSnapshotDao;
import com.spark.dao.monitor.MonitorHostSnapshotDao;
import com.spark.dao.monitor.MonitorMqSnapshotDao;
import com.spark.dao.monitor.MonitorRedisSnapshotDao;
import com.spark.monitor.collect.ApiMetricsCollector;
import com.spark.monitor.collect.DruidMetricCollector;
import com.spark.monitor.collect.HostMetricCollector;
import com.spark.monitor.collect.MqMetricCollector;
import com.spark.monitor.collect.NodeHolder;
import com.spark.monitor.collect.RedisMetricCollector;
import com.spark.monitor.constant.MonitorConstant;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 监控快照定时采集任务
 */
@Component
public class MonitorSnapshotJob {

    private final static Logger logger = LoggerFactory.getLogger(MonitorSnapshotJob.class);

    @Autowired
    private NodeHolder nodeHolder;
    @Autowired
    private HostMetricCollector hostMetricCollector;
    @Autowired
    private ApiMetricsCollector apiMetricsCollector;
    @Autowired
    private DruidMetricCollector druidMetricCollector;
    @Autowired
    private RedisMetricCollector redisMetricCollector;
    @Autowired
    private MqMetricCollector mqMetricCollector;
    @Autowired
    private MonitorHostSnapshotDao hostSnapshotDao;
    @Autowired
    private MonitorApiSnapshotDao apiSnapshotDao;
    @Autowired
    private MonitorDruidSnapshotDao druidSnapshotDao;
    @Autowired
    private MonitorRedisSnapshotDao redisSnapshotDao;
    @Autowired
    private MonitorMqSnapshotDao mqSnapshotDao;
    @Resource(name = "redisTemplate")
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 每分钟落一轮快照
     */
    @Scheduled(fixedDelay = 60000L, initialDelay = 10000L)
    public void saveSnapshot() {
        String nodeKey = nodeHolder.getNodeKey();
        saveHostSnapshot(nodeKey);
        saveApiSnapshot(nodeKey);
        saveDruidSnapshot(nodeKey);
        saveMiddlewareSnapshot(nodeKey);
    }

    /**
     * 保存本机主机与JVM快照
     * @param nodeKey 节点标识
     */
    private void saveHostSnapshot(String nodeKey) {
        try {
            MonitorHostSnapshot snapshot = hostMetricCollector.buildSnapshot(nodeKey);
            hostSnapshotDao.insertDB(snapshot);
        } catch (Exception e) {
            logger.error("saveHostSnapshot error, nodeKey={}, errMsg={}", nodeKey, e.getMessage(), e);
        }
    }

    /**
     * 刷出本机接口分钟快照
     * @param nodeKey 节点标识
     */
    private void saveApiSnapshot(String nodeKey) {
        try {
            List<MonitorApiSnapshot> snapshotList = apiMetricsCollector.flush(nodeKey);
            for (MonitorApiSnapshot snapshot : snapshotList) {
                apiSnapshotDao.insertDB(snapshot);
            }
        } catch (Exception e) {
            logger.error("saveApiSnapshot error, nodeKey={}, errMsg={}", nodeKey, e.getMessage(), e);
        }
    }

    /**
     * 保存本机Druid慢SQL快照
     * @param nodeKey 节点标识
     */
    private void saveDruidSnapshot(String nodeKey) {
        try {
            String sqlJson = druidMetricCollector.buildSqlJson();
            Timestamp snapshotDt = new Timestamp(System.currentTimeMillis());
            MonitorDruidSnapshot snapshot = new MonitorDruidSnapshot();
            snapshot.setNodeKey(nodeKey);
            snapshot.setSnapshotDt(snapshotDt);
            snapshot.setSqlJson(sqlJson);
            druidSnapshotDao.insertDB(snapshot);
        } catch (Exception e) {
            logger.error("saveDruidSnapshot error, nodeKey={}, errMsg={}", nodeKey, e.getMessage(), e);
        }
    }

    /**
     * 抢到分布式锁的节点才采集共享中间件
     * @param nodeKey 节点标识
     */
    private void saveMiddlewareSnapshot(String nodeKey) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                MonitorConstant.MIDDLEWARE_LOCK_KEY, nodeKey,
                MonitorConstant.MIDDLEWARE_LOCK_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            return;
        }
        saveRedisSnapshot(nodeKey);
        saveMqSnapshot(nodeKey);
    }

    /**
     * 保存Redis快照
     * @param nodeKey 节点标识
     */
    private void saveRedisSnapshot(String nodeKey) {
        try {
            MonitorRedisSnapshot snapshot = redisMetricCollector.buildSnapshot(nodeKey);
            redisSnapshotDao.insertDB(snapshot);
        } catch (Exception e) {
            logger.error("saveRedisSnapshot error, nodeKey={}, errMsg={}", nodeKey, e.getMessage(), e);
        }
    }

    /**
     * 保存消息队列快照
     * @param nodeKey 节点标识
     */
    private void saveMqSnapshot(String nodeKey) {
        try {
            List<MonitorMqSnapshot> snapshotList = mqMetricCollector.buildSnapshots(nodeKey);
            for (MonitorMqSnapshot snapshot : snapshotList) {
                mqSnapshotDao.insertDB(snapshot);
            }
        } catch (Exception e) {
            logger.error("saveMqSnapshot error, nodeKey={}, errMsg={}", nodeKey, e.getMessage(), e);
        }
    }
}
