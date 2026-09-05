package com.spark.manage.log;

import com.spark.common.bean.log.entity.LogLogin;
import com.spark.common.bean.log.query.LogLoginQuery;
import com.spark.common.bean.log.result.LogLoginResult;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 15:23
 */
public interface ILogLoginService {

    /**
     * 创建登录日志
     * @param logLogin 登录日志数据
     * @return 创建结果
     */
    ResultData<Void> createLogLogin(LogLogin logLogin);

    /**
     * 分页查询
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<PageResult<LogLoginResult>> pageLogLoginList(LogLoginQuery query);

    /**
     * 分页查询我的登录记录
     * @param query 查询参数
     * @return 列表
     */
    ResultData<PageResult<LogLoginResult>> pageMyLogLoginList(LogLoginQuery query);
}
