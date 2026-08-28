package com.spark.kb.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kb.entity.RetrieveLog;
import com.spark.bean.kb.query.DocumentQuery;
import com.spark.bean.kb.query.KnowledgeQuery;
import com.spark.bean.kb.query.RetrieveLogQuery;
import com.spark.bean.kb.result.HotQueryResult;
import com.spark.bean.kb.result.KnowledgeResult;
import com.spark.bean.kb.result.PeriodRetrieveCountResult;
import com.spark.bean.kb.result.RetrieveDetailItem;
import com.spark.bean.kb.result.RetrieveDetailResult;
import com.spark.bean.kb.result.RetrieveLogResult;
import com.spark.bean.kb.result.RetrieveStatsResult;
import com.spark.bean.kb.result.UsageStatsResult;
import com.spark.bean.kb.result.UserRankResult;
import com.spark.bean.kb.vo.RetrieveLogVO;
import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.UserResult;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.dao.kb.DocumentDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.kb.RetrieveLogDao;
import com.spark.dao.system.UserDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.kb.service.IRetrieveLogService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 检索日志服务实现
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-28 16:30:00
 */
@Service
public class RetrieveLogServiceImpl extends BaseService<RetrieveLogQuery, RetrieveLogResult> implements IRetrieveLogService {
    private final static Logger logger = LoggerFactory.getLogger(RetrieveLogServiceImpl.class);
    @Autowired
    private RetrieveLogDao retrieveLogDao;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private UserDao userDao;

    /**
     * 保存检索日志
     * @param kbId 知识库ID
     * @param query 查询内容
     * @param detailResult 检索详情
     * @param costTime 耗时
     */
    @Override
    public ResultData<Void> saveRetrieveLog(Long kbId, String query, RetrieveDetailResult detailResult, long costTime) {
        ResultData<Void> result = new ResultData<>();
        if (kbId == null || StringUtil.isBlank(query) || detailResult == null) {
            logger.warn("saveRetrieveLog skip, invalid param, kbId={}, query={}", kbId, query);
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        RetrieveLog log = new RetrieveLog();
        log.setKbId(kbId);
        log.setQuery(query);
        log.setStrategy(detailResult.getStrategy());
        log.setQaHit(Boolean.TRUE.equals(detailResult.getQaHit()) ? 1 : 0);
        log.setCostTime(costTime);
        log.setRetrieveCount(0);
        log.setAvgSimilarity(0.0);
        log.setMaxSimilarity(0.0);
        List<RetrieveDetailItem> items = detailResult.getItems();
        if (CollectionUtil.isNotEmpty(items)) {
            log.setRetrieveCount(items.size());
            double totalScore = 0;
            double maxScore = 0;
            for (RetrieveDetailItem item : items) {
                if (item.getScore() != null) {
                    totalScore += item.getScore();
                    maxScore = Math.max(maxScore, item.getScore());
                }
            }
            log.setAvgSimilarity(totalScore / items.size());
            log.setMaxSimilarity(maxScore);
        }
        int count = retrieveLogDao.insertDB(log);
        if (count < 1) {
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询检索日志
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<RetrieveLogResult>> pageRetrieveLogList(RetrieveLogQuery query) {
        ResultData<PageResult<RetrieveLogResult>> result = new ResultData<>();
        if (query == null) {
            query = new RetrieveLogQuery();
        }
        PageResult<RetrieveLogResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询检索统计
     * @param query 查询条件
     * @return 统计结果
     */
    @Override
    public ResultData<RetrieveStatsResult> queryRetrieveStats(RetrieveLogQuery query) {
        ResultData<RetrieveStatsResult> result = new ResultData<>();
        if (query == null) {
            query = new RetrieveLogQuery();
        }
        RetrieveStatsResult stats = retrieveLogDao.queryRetrieveStats(query);
        result.setData(stats);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 检索反馈
     * @param retrieveLogVO 反馈数据
     * @return 操作结果
     */
    @Override
    public ResultData<Void> feedback(RetrieveLogVO retrieveLogVO) {
        ResultData<Void> result = new ResultData<>();
        if (retrieveLogVO == null || retrieveLogVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer feedbackScore = retrieveLogVO.getFeedbackScore();
        if (feedbackScore == null || feedbackScore < 1 || feedbackScore > 5) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        RetrieveLog retrieveLog = new RetrieveLog();
        BeanUtil.copyProperties(retrieveLogVO, retrieveLog);
        int count = retrieveLogDao.updateDBById(retrieveLog);
        if (count < 1) {
            logger.error("feedback error, update db fail, id={}", retrieveLogVO.getId());
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询使用情况统计
     * @return 使用统计结果
     */
    @Override
    public ResultData<UsageStatsResult> queryUsageStats() {
        ResultData<UsageStatsResult> result = new ResultData<>();
        UsageStatsResult stats = new UsageStatsResult();
        // 知识库数量
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        int knowledgeCount = knowledgeDao.queryKnowledgeCount(knowledgeQuery);
        stats.setKnowledgeCount((long) knowledgeCount);
        // 文档数量
        DocumentQuery documentQuery = new DocumentQuery();
        int documentCount = documentDao.queryDocumentCount(documentQuery);
        stats.setDocumentCount((long) documentCount);
        // 时段检索次数
        PeriodRetrieveCountResult periodCount = queryPeriodRetrieveCount();
        if (periodCount != null) {
            stats.setTodayRetrieveCount(periodCount.getTodayCount());
            stats.setWeekRetrieveCount(periodCount.getWeekCount());
            stats.setMonthRetrieveCount(periodCount.getMonthCount());
        }
        // 热门查询
        List<HotQueryResult> hotQueries = retrieveLogDao.queryHotQueries(10);
        stats.setHotQueries(hotQueries);
        // 用户使用排行
        List<UserRankResult> userRankList = retrieveLogDao.queryUserRank(10);
        supplyUserRankList(userRankList);
        stats.setUserRankList(userRankList);
        result.setData(stats);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询时段检索次数
     * @return 时段统计结果
     */
    private PeriodRetrieveCountResult queryPeriodRetrieveCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStartDateTime = today.atStartOfDay();
        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStartDateTime = weekStartDate.atStartOfDay();
        LocalDate monthStartDate = today.withDayOfMonth(1);
        LocalDateTime monthStartDateTime = monthStartDate.atStartOfDay();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String todayStart = todayStartDateTime.format(formatter);
        String weekStart = weekStartDateTime.format(formatter);
        String monthStart = monthStartDateTime.format(formatter);
        return retrieveLogDao.queryRetrieveCountByPeriod(todayStart, weekStart, monthStart);
    }

    /**
     * 补充用户排行用户名称
     * @param userRankList 用户排行列表
     */
    private void supplyUserRankList(List<UserRankResult> userRankList) {
        if (CollectionUtil.isEmpty(userRankList)) {
            return;
        }
        Set<Long> userIds = new HashSet<>();
        for (UserRankResult rank : userRankList) {
            if (rank.getUserId() != null) {
                userIds.add(rank.getUserId());
            }
        }
        if (CollectionUtil.isEmpty(userIds)) {
            return;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setPage(false);
        userQuery.setIds(new ArrayList<>(userIds));
        List<UserResult> userList = userDao.queryUserList(userQuery);
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }
        Map<Long, String> userNameMap = new HashMap<>();
        for (UserResult userResult : userList) {
            userNameMap.put(userResult.getId(), userResult.getName());
        }
        for (UserRankResult rank : userRankList) {
            rank.setUserName(userNameMap.getOrDefault(rank.getUserId(), "未知用户"));
        }
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(RetrieveLogQuery query) {
        return retrieveLogDao.queryRetrieveLogCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<RetrieveLogResult> queryList(RetrieveLogQuery query) {
        return retrieveLogDao.queryRetrieveLogList(query);
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<RetrieveLogResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        // 补充QA命中名称
        for (RetrieveLogResult item : list) {
            if (item.getQaHit() != null) {
                item.setQaHitName(item.getQaHit() == 1 ? "是" : "否");
            }
        }
        // 补充知识库名称
        Set<Long> kbIds = new HashSet<>();
        for (RetrieveLogResult item : list) {
            if (item.getKbId() != null) {
                kbIds.add(item.getKbId());
            }
        }
        if (CollectionUtil.isEmpty(kbIds)) {
            return;
        }
        KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
        knowledgeQuery.setPage(false);
        knowledgeQuery.setIds(new ArrayList<>(kbIds));
        List<KnowledgeResult> knowledgeList = knowledgeDao.queryKnowledgeList(knowledgeQuery);
        if (CollectionUtil.isEmpty(knowledgeList)) {
            return;
        }
        Map<Long, String> knowledgeNameMap = new HashMap<>();
        for (KnowledgeResult knowledgeResult : knowledgeList) {
            knowledgeNameMap.put(knowledgeResult.getId(), knowledgeResult.getName());
        }
        for (RetrieveLogResult item : list) {
            item.setKnowledgeName(knowledgeNameMap.getOrDefault(item.getKbId(), "未知知识库"));
        }
    }
}