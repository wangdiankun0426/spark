package com.spark.manage.log.impl;

import com.spark.common.bean.log.entity.LogOperate;
import com.spark.common.bean.log.query.LogOperateQuery;
import com.spark.common.bean.log.result.LogOperateResult;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.dao.log.LogOperateDao;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.log.ILogOperateService;
import com.spark.common.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 19:27
 */
@Service
public class LogOperateServiceImpl extends BaseService<LogOperateQuery, LogOperateResult> implements ILogOperateService {
    private final static Logger logger = LoggerFactory.getLogger(LogOperateServiceImpl.class);
    @Autowired
    private LogOperateDao logOperateDao;

    /**
     * 创建操作日志
     * @param logOperate 操作日志数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createLogOperate(LogOperate logOperate) {
        ResultData<Void> result = new ResultData<>();
        int count = logOperateDao.insertDB(logOperate);
        if (count < 1) {
            logger.error("createLogOperate create db fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<LogOperateResult>> pageLogOperateList(LogOperateQuery query) {
        ResultData<PageResult<LogOperateResult>> result = new ResultData<>();
        if (query == null) {
            query = new LogOperateQuery();
        }
        query.setTenantId(SessionHolder.getCurrentTenantId());
        PageResult<LogOperateResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询操作类型列表
     * @return 列表
     */
    @Override
    public ResultData<List<Map<String, Object>>> queryLogOperateTypeList() {
        ResultData<List<Map<String, Object>>> result = new ResultData<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (OperateTypeEnum value : OperateTypeEnum.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", value.getValue());
            map.put("label", value.getDesc());
            list.add(map);
        }
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询我的操作记录
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<LogOperateResult>> pageMyLogOperateList(LogOperateQuery query) {
        ResultData<PageResult<LogOperateResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null) {
            query = new LogOperateQuery();
        }
        query.setCreatedBy(userId);
        PageResult<LogOperateResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询操作日志统计
     * @return 列表
     */
    @Override
    public ResultData<List<Map<String, Object>>> queryOperateStatistics() {
        ResultData<List<Map<String, Object>>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        LogOperateQuery query = new LogOperateQuery();
        query.setCreatedBy(userId);
        query.setPage(false);
        List<LogOperateResult> list = logOperateDao.queryLogOperateList(query);
        if (CollectionUtil.isEmpty(list)) {
            result.setCode(ResultData.OK);
            return result;
        }
        Map<Integer, Integer> countMap = list.stream().collect(Collectors.toMap(LogOperateResult::getType, v -> 1, Integer::sum));
        Map<Integer, Integer> successMap = list.stream().filter(v -> ResultData.OK == v.getCode()).collect(Collectors.toMap(LogOperateResult::getType, v -> 1, Integer::sum));
        Map<Integer, Integer> errorMap = list.stream().filter(v -> ResultData.OK != v.getCode()).collect(Collectors.toMap(LogOperateResult::getType, v -> 1, Integer::sum));

        List<Map.Entry<Integer,Integer>> countList = new ArrayList<>(countMap.entrySet());
        countList.sort(Map.Entry.comparingByValue());

        for (Map.Entry<Integer, Integer> entry : countList) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", OperateTypeEnum.indexOf(entry.getKey()).getDesc());
            map.put("successValue", successMap.getOrDefault(entry.getKey(), 0));
            map.put("errorValue", errorMap.getOrDefault(entry.getKey(), 0));
            mapList.add(map);
        }
        result.setData(mapList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询对象操作记录列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<List<LogOperateResult>> queryObjLogOperateList(LogOperateQuery query) {
        ResultData<List<LogOperateResult>> result = new ResultData<>();
        if (query == null || query.getObjId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        query.setCode(ResultData.OK);
        query.setPage(false);
        List<LogOperateResult> list = logOperateDao.queryLogOperateList(query);
        this.supplyList(list);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<LogOperateResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        for (LogOperateResult logOperateResult : list) {
            logOperateResult.setTypeName(OperateTypeEnum.indexOf(logOperateResult.getType()).getDesc());
        }
    }

    /**
     * 查询条数
     * @param query 查询参数
     * @return 条数
     */
    @Override
    protected int queryCount(LogOperateQuery query) {
        return logOperateDao.queryLogOperateCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<LogOperateResult> queryList(LogOperateQuery query) {
        return logOperateDao.queryLogOperateList(query);
    }
}
