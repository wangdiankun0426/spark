package com.spark.monitor.task;

import com.spark.dao.monitor.MonitorApiSnapshotDao;
import com.spark.dao.monitor.MonitorDruidSnapshotDao;
import com.spark.dao.monitor.MonitorHostSnapshotDao;
import com.spark.dao.monitor.MonitorMqSnapshotDao;
import com.spark.dao.monitor.MonitorRedisSnapshotDao;
import com.spark.monitor.constant.MonitorConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 监控快照过期清理任务
 */
@Component
public class MonitorCleanJob {

    private final static Logger logger = LoggerFactory.getLogger(MonitorCleanJob.class);

    @Autowired
    private MonitorHostSnapshotDao hostSnapshotDao;
    @Autowired
    private MonitorApiSnapshotDao apiSnapshotDao;
    @Autowired
    private MonitorRedisSnapshotDao redisSnapshotDao;
    @Autowired
    private MonitorMqSnapshotDao mqSnapshotDao;
    @Autowired
    private MonitorDruidSnapshotDao druidSnapshotDao;

    /**
     * 每天凌晨3点物理删除过期快照
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpired() {
        Timestamp hostExpireTime = expireTime(MonitorConstant.HOST_RETAIN_DAYS);
        Timestamp apiExpireTime = expireTime(MonitorConstant.API_RETAIN_DAYS);
        Timestamp redisExpireTime = expireTime(MonitorConstant.REDIS_RETAIN_DAYS);
        Timestamp mqExpireTime = expireTime(MonitorConstant.MQ_RETAIN_DAYS);
        Timestamp druidExpireTime = expireTime(MonitorConstant.DRUID_RETAIN_DAYS);
        int hostCount = hostSnapshotDao.deleteHostExpired(hostExpireTime);
        int apiCount = apiSnapshotDao.deleteApiExpired(apiExpireTime);
        int redisCount = redisSnapshotDao.deleteRedisExpired(redisExpireTime);
        int mqCount = mqSnapshotDao.deleteMqExpired(mqExpireTime);
        int druidCount = druidSnapshotDao.deleteDruidExpired(druidExpireTime);
        logger.info("cleanExpired finished, hostCount={}, apiCount={}, redisCount={}, mqCount={}, druidCount={}",
                hostCount, apiCount, redisCount, mqCount, druidCount);
    }

    /**
     * 计算过期时间界限
     * @param retainDays 保留天数
     * @return 界限时间
     */
    private Timestamp expireTime(int retainDays) {
        return Timestamp.valueOf(LocalDateTime.now().minusDays(retainDays));
    }
}
