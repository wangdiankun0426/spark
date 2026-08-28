package com.spark.web.controller.kb;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.RetrieveLogQuery;
import com.spark.bean.kb.result.RetrieveLogResult;
import com.spark.bean.kb.result.RetrieveStatsResult;
import com.spark.bean.kb.result.UsageStatsResult;
import com.spark.bean.kb.vo.RetrieveLogVO;
import com.spark.kb.service.IRetrieveLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 检索日志控制层
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 16:30:00
 */
@RestController
@RequestMapping("kb/retrieveLog")
public class RetrieveLogController {
    @Autowired
    private IRetrieveLogService retrieveLogService;

    /**
     * 分页查询检索日志
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<RetrieveLogResult>> pageRetrieveLogList(RetrieveLogQuery query) {
        return retrieveLogService.pageRetrieveLogList(query);
    }

    /**
     * 查询检索统计
     * @param query 查询参数
     * @return 统计结果
     */
    @GetMapping("stats")
    private ResultData<RetrieveStatsResult> queryRetrieveStats(RetrieveLogQuery query) {
        return retrieveLogService.queryRetrieveStats(query);
    }

    /**
     * 检索反馈
     * @param retrieveLogVO 反馈参数
     * @return 操作结果
     */
    @PostMapping("feedback")
    private ResultData<Void> feedback(RetrieveLogVO retrieveLogVO) {
        return retrieveLogService.feedback(retrieveLogVO);
    }

    /**
     * 查询使用情况统计
     * @return 使用统计结果
     */
    @GetMapping("usageStats")
    private ResultData<UsageStatsResult> queryUsageStats() {
        return retrieveLogService.queryUsageStats();
    }
}