package com.spark.dao.log;

import com.spark.bean.log.entity.LogLogin;
import com.spark.bean.log.query.LogLoginQuery;
import com.spark.bean.log.result.LogLoginResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 14:56
 */
public interface LogLoginDao extends BaseDao<LogLogin> {
    /**
     * 插入
     * @param logLogin
     * @return
     */
    @Override
    int insert(LogLogin logLogin);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<LogLoginResult> queryLogLoginList(LogLoginQuery query);

    /**
     * 查询条数
     * @param query
     * @return
     */
    int queryLogLoginCount(LogLoginQuery query);
}
