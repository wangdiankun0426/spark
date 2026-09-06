package com.spark.manage.log.impl;

import com.spark.common.bean.log.entity.LogLogin;
import com.spark.common.bean.log.query.LogLoginQuery;
import com.spark.common.bean.log.result.LogLoginResult;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.dao.log.LogLoginDao;
import com.spark.common.enums.LoginPlatformEnum;
import com.spark.common.enums.LoginStatusEnum;
import com.spark.common.enums.LoginTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.log.ILogLoginService;
import com.spark.config.redis.RedisService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 15:24
 */
@Service
public class LogLoginServiceImpl extends BaseService<LogLoginQuery,LogLoginResult> implements ILogLoginService {
    private final static Logger logger = LoggerFactory.getLogger(LogLoginServiceImpl.class);
    @Autowired
    private LogLoginDao logLoginDao;
    @Autowired
    private RedisService redisService;

    /**
     * 创建登录日志
     * @param logLogin 登录日志数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createLogLogin(LogLogin logLogin) {
        ResultData<Void> result = new ResultData<>();
        int count = logLoginDao.insertDB(logLogin);
        if (count < 1) {
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
    public ResultData<PageResult<LogLoginResult>> pageLogLoginList(LogLoginQuery query) {
        ResultData<PageResult<LogLoginResult>> result = new ResultData<>();
        if (query == null) {
            query = new LogLoginQuery();
        }
        PageResult<LogLoginResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询 我的登录记录
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<LogLoginResult>> pageMyLogLoginList(LogLoginQuery query) {
        ResultData<PageResult<LogLoginResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null) {
            query = new LogLoginQuery();
        }
        query.setCreatedBy(userId);
        PageResult<LogLoginResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询条数
     * @param query 查询参数
     * @return 条数
     */
    @Override
    protected int queryCount(LogLoginQuery query) {
        return logLoginDao.queryLogLoginCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<LogLoginResult> queryList(LogLoginQuery query) {
        return logLoginDao.queryLogLoginList(query);
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<LogLoginResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        for (LogLoginResult logLoginResult : list) {
            String sessionId = logLoginResult.getSessionId();
            String sessionIdKey = ObjectCacheKey.LOGIN_SESSION + sessionId;
            String value = redisService.getValue(sessionIdKey);
            if (StringUtil.isNotBlank(value)) {
                logLoginResult.setStatus(LoginStatusEnum.ONLINE.getValue());
                logLoginResult.setStatusName(LoginStatusEnum.ONLINE.getDesc());
            } else {
                logLoginResult.setStatus(LoginStatusEnum.NOT_ONLINE.getValue());
                logLoginResult.setStatusName(LoginStatusEnum.NOT_ONLINE.getDesc());
            }
            logLoginResult.setLoginPlatformName(LoginPlatformEnum.indexOf(logLoginResult.getLoginPlatform()).getDesc());
            logLoginResult.setLoginTypeName(LoginTypeEnum.indexOf(logLoginResult.getLoginType()).getDesc());
        }
    }
}
