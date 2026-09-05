package com.spark.dao.kb;

import com.spark.common.bean.kb.entity.RetrieveLog;
import com.spark.common.bean.kb.query.RetrieveLogQuery;
import com.spark.common.bean.kb.result.HotQueryResult;
import com.spark.common.bean.kb.result.PeriodRetrieveCountResult;
import com.spark.common.bean.kb.result.RetrieveLogResult;
import com.spark.common.bean.kb.result.RetrieveStatsResult;
import com.spark.common.bean.kb.result.UserRankResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 检索日志DAO
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 16:30:00
 */
public interface RetrieveLogDao extends BaseDao<RetrieveLog> {

    /**
     * 插入数据
     * @param retrieveLog 数据对象
     * @return 影响行数
     */
    @Override
    int insert(RetrieveLog retrieveLog);

    /**
     * 修改数据
     * @param retrieveLog 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(RetrieveLog retrieveLog);

    /**
     * 删除数据
     * @param retrieveLog 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(RetrieveLog retrieveLog);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryRetrieveLogCount(RetrieveLogQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<RetrieveLogResult> queryRetrieveLogList(RetrieveLogQuery query);

    /**
     * 查询检索统计
     * @param query 查询条件
     * @return 统计结果
     */
    RetrieveStatsResult queryRetrieveStats(RetrieveLogQuery query);

    /**
     * 查询热门查询
     * @param limit 返回条数
     * @return 热门查询列表
     */
    List<HotQueryResult> queryHotQueries(@Param("limit") Integer limit);

    /**
     * 查询用户使用排行
     * @param limit 返回条数
     * @return 用户排行列表
     */
    List<UserRankResult> queryUserRank(@Param("limit") Integer limit);

    /**
     * 查询时段检索次数
     * @param todayStart 今日起始时间
     * @param weekStart 本周起始时间
     * @param monthStart 本月起始时间
     * @return 时段统计结果
     */
    PeriodRetrieveCountResult queryRetrieveCountByPeriod(@Param("todayStart") String todayStart,
                                                         @Param("weekStart") String weekStart,
                                                         @Param("monthStart") String monthStart);
}