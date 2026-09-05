package com.spark.manage.log;

import com.spark.common.bean.log.entity.LogOperate;
import com.spark.common.bean.log.query.LogOperateQuery;
import com.spark.common.bean.log.result.LogOperateResult;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 19:27
 */
public interface ILogOperateService {

    /**
     * 创建操作日志
     * @param logOperate 操作日志数据
     * @return 创建结果
     */
    ResultData<Void> createLogOperate(LogOperate logOperate);

    /**
     * 分页查询
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<PageResult<LogOperateResult>> pageLogOperateList(LogOperateQuery query);

    /**
     * 查询操作类型列表
     * @return 列表
     */
    ResultData<List<Map<String, Object>>> queryLogOperateTypeList();

    /**
     * 分页查询我的操作记录
     * @param query 查询参数
     * @return 列表
     */
    ResultData<PageResult<LogOperateResult>> pageMyLogOperateList(LogOperateQuery query);

    /**
     * 查询操作日志统计
     * @return 列表
     */
    ResultData<List<Map<String, Object>>> queryOperateStatistics();

    /**
     * 查询对象操作记录列表
     * @param query 查询参数
     * @return 列表
     */
    ResultData<List<LogOperateResult>> queryObjLogOperateList(LogOperateQuery query);

}
