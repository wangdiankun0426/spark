package com.spark.kb.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.RetrieveLogQuery;
import com.spark.bean.kb.result.RetrieveDetailResult;
import com.spark.bean.kb.result.RetrieveLogResult;
import com.spark.bean.kb.result.RetrieveStatsResult;
import com.spark.bean.kb.result.UsageStatsResult;
import com.spark.bean.kb.vo.RetrieveLogVO;

/**
 * 检索日志服务接口
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 16:30:00
 */
public interface IRetrieveLogService {

    /**
     * 保存检索日志
     * @param kbId 知识库ID
     * @param query 查询内容
     * @param detailResult 检索详情
     * @param costTime 耗时
     */
    ResultData<Void> saveRetrieveLog(Long kbId, String query, RetrieveDetailResult detailResult, long costTime);

    /**
     * 分页查询检索日志
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<RetrieveLogResult>> pageRetrieveLogList(RetrieveLogQuery query);

    /**
     * 查询检索统计
     * @param query 查询条件
     * @return 统计结果
     */
    ResultData<RetrieveStatsResult> queryRetrieveStats(RetrieveLogQuery query);

    /**
     * 用户反馈
     * @param vo 反馈数据
     * @return 操作结果
     */
    ResultData<Void> feedback(RetrieveLogVO vo);

    /**
     * 查询使用情况统计
     * @return 使用统计结果
     */
    ResultData<UsageStatsResult> queryUsageStats();
}