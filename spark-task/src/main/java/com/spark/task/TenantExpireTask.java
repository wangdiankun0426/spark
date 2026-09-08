package com.spark.task;

import com.spark.common.bean.base.ResultData;
import com.spark.manage.sys.ITenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-09 10:00:00
 * 超期租户自动关闭任务
 */
@Service
public class TenantExpireTask {
    private final static Logger logger = LoggerFactory.getLogger(TenantExpireTask.class);
    @Autowired
    private ITenantService tenantService;

    /**
     * 每分钟执行一次
     * 批量关闭截止时间已过的启用租户
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void executeCloseExpiredTenant() {
        logger.info("scheduled executeCloseExpiredTenant start");
        ResultData<Integer> result = tenantService.closeExpiredTenant();
        logger.info("scheduled executeCloseExpiredTenant end, code={}, closeCount={}", result.getCode(), result.getData());
    }
}
