package com.spark.monitor.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.monitor.entity.MonitorDruidSnapshot;
import com.spark.common.bean.monitor.entity.MonitorHostSnapshot;
import com.spark.common.bean.monitor.entity.MonitorMqSnapshot;
import com.spark.common.bean.monitor.entity.MonitorRedisSnapshot;
import com.spark.common.bean.monitor.query.MonitorSnapshotQuery;
import com.spark.common.bean.monitor.result.MonitorApiTrendResult;
import com.spark.common.bean.monitor.result.MonitorNodeResult;
import com.spark.common.bean.monitor.result.MonitorUriStatResult;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.utils.StringUtil;
import com.spark.dao.monitor.MonitorApiSnapshotDao;
import com.spark.dao.monitor.MonitorDruidSnapshotDao;
import com.spark.dao.monitor.MonitorHostSnapshotDao;
import com.spark.dao.monitor.MonitorMqSnapshotDao;
import com.spark.dao.monitor.MonitorRedisSnapshotDao;
import com.spark.monitor.constant.MonitorConstant;
import com.spark.monitor.service.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * 监控查询服务实现
 */
@Service
public class MonitorServiceImpl implements IMonitorService {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
     * 查询节点清单与在线状态
     * @return 节点清单
     */
    @Override
    public ResultData<List<MonitorNodeResult>> queryNodeList() {
        ResultData<List<MonitorNodeResult>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        List<MonitorNodeResult> nodeList = hostSnapshotDao.queryNodeList();
        long now = System.currentTimeMillis();
        for (MonitorNodeResult node : nodeList) {
            Date lastSnapshotDt = node.getLastSnapshotDt();
            node.setOnline(lastSnapshotDt != null
                    && now - lastSnapshotDt.getTime() <= MonitorConstant.ONLINE_THRESHOLD_MS);
        }
        result.setData(nodeList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询节点最新主机快照
     * @param query 查询参数
     * @return 主机快照
     */
    @Override
    public ResultData<MonitorHostSnapshot> queryLatestHost(MonitorSnapshotQuery query) {
        ResultData<MonitorHostSnapshot> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        if (query == null || StringUtil.isBlank(query.getNodeKey())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        MonitorHostSnapshot snapshot = hostSnapshotDao.queryLatestHost(query.getNodeKey());
        result.setData(snapshot);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询主机快照趋势
     * @param query 查询参数
     * @return 快照序列
     */
    @Override
    public ResultData<List<MonitorHostSnapshot>> queryHostSeries(MonitorSnapshotQuery query) {
        ResultData<List<MonitorHostSnapshot>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query = ensureQuery(query);
        if (StringUtil.isBlank(query.getNodeKey())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        fillDefaultRange(query);
        List<MonitorHostSnapshot> series = hostSnapshotDao.queryHostSeries(query);
        result.setData(series);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询节点最新慢SQL快照
     * @param query 查询参数
     * @return 慢SQL快照
     */
    @Override
    public ResultData<MonitorDruidSnapshot> queryLatestDruid(MonitorSnapshotQuery query) {
        ResultData<MonitorDruidSnapshot> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        if (query == null || StringUtil.isBlank(query.getNodeKey())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        MonitorDruidSnapshot snapshot = druidSnapshotDao.queryLatestDruid(query.getNodeKey());
        result.setData(snapshot);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询最新Redis快照
     * @return Redis快照
     */
    @Override
    public ResultData<MonitorRedisSnapshot> queryLatestRedis() {
        ResultData<MonitorRedisSnapshot> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        MonitorRedisSnapshot snapshot = redisSnapshotDao.queryLatestRedis();
        result.setData(snapshot);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询Redis快照趋势
     * @param query 查询参数
     * @return 快照序列
     */
    @Override
    public ResultData<List<MonitorRedisSnapshot>> queryRedisSeries(MonitorSnapshotQuery query) {
        ResultData<List<MonitorRedisSnapshot>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query = ensureQuery(query);
        fillDefaultRange(query);
        List<MonitorRedisSnapshot> series = redisSnapshotDao.queryRedisSeries(query);
        result.setData(series);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询各队列最新快照
     * @return 队列快照列表
     */
    @Override
    public ResultData<List<MonitorMqSnapshot>> queryLatestMqList() {
        ResultData<List<MonitorMqSnapshot>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        List<MonitorMqSnapshot> snapshotList = mqSnapshotDao.queryLatestMqList();
        result.setData(snapshotList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询队列消息积压趋势
     * @param query 查询参数
     * @return 快照序列
     */
    @Override
    public ResultData<List<MonitorMqSnapshot>> queryMqSeries(MonitorSnapshotQuery query) {
        ResultData<List<MonitorMqSnapshot>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query = ensureQuery(query);
        fillDefaultRange(query);
        List<MonitorMqSnapshot> series = mqSnapshotDao.queryMqSeries(query);
        result.setData(series);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询接口请求汇总
     * @param query 查询参数
     * @return 汇总
     */
    @Override
    public ResultData<MonitorApiTrendResult> queryApiSummary(MonitorSnapshotQuery query) {
        ResultData<MonitorApiTrendResult> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query = ensureQuery(query);
        fillDefaultRange(query);
        MonitorApiTrendResult summary = apiSnapshotDao.queryApiSummary(query);
        result.setData(summary);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询接口请求分钟趋势
     * @param query 查询参数
     * @return 分钟趋势
     */
    @Override
    public ResultData<List<MonitorApiTrendResult>> queryApiTrend(MonitorSnapshotQuery query) {
        ResultData<List<MonitorApiTrendResult>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query = ensureQuery(query);
        fillDefaultRange(query);
        List<MonitorApiTrendResult> trend = apiSnapshotDao.queryApiTrend(query);
        result.setData(trend);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询URI维度统计（不限时间范围，按调用次数倒排）
     * @param query 查询参数
     * @return URI统计分页
     */
    @Override
    public ResultData<PageResult<MonitorUriStatResult>> queryUriStats(MonitorSnapshotQuery query) {
        ResultData<PageResult<MonitorUriStatResult>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query = ensureQuery(query);
        int total = apiSnapshotDao.queryUriStatsCount(query);
        List<MonitorUriStatResult> uriStats = apiSnapshotDao.queryUriStats(query);
        PageResult<MonitorUriStatResult> pageResult = new PageResult<>();
        pageResult.setRows(uriStats);
        pageResult.setTotal(total);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 保证查询对象非空
     * @param query 原始查询
     * @return 查询对象
     */
    private MonitorSnapshotQuery ensureQuery(MonitorSnapshotQuery query) {
        if (query == null) {
            return new MonitorSnapshotQuery();
        }
        return query;
    }

    /**
     * 趋势查询默认近1小时
     * @param query 查询参数
     */
    private void fillDefaultRange(MonitorSnapshotQuery query) {
        if (!StringUtil.isBlank(query.getStartTime()) || !StringUtil.isBlank(query.getEndTime())) {
            return;
        }
        String startTime = LocalDateTime.now().minusHours(1).format(DATE_TIME_FORMATTER);
        String endTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
    }
}
