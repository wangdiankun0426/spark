package com.spark.dao.log;

import com.spark.bean.log.entity.LogOperate;
import com.spark.bean.log.query.LogOperateQuery;
import com.spark.bean.log.result.LogOperateResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 17:02
 */
public interface LogOperateDao extends BaseDao<LogOperate> {

    /**
     * 插入
     * @param logOperate
     * @return
     */
    @Override
    int insert(LogOperate logOperate);

    /**
     * 查询条数
     * @param query
     * @return
     */
    int queryLogOperateCount(LogOperateQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<LogOperateResult> queryLogOperateList(LogOperateQuery query);
}
