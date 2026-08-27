package com.spark.flow.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.flow.query.FlowInstanceCopyQuery;
import com.spark.bean.flow.result.FlowInstanceCopyResult;
import com.spark.bean.flow.vo.FlowInstanceCopyVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/27 19:25
 */
public interface IFlowInstanceCopyService {

    /**
     * 抄送流程实例
     * @param copyVO 抄送参数
     * @return 抄送结果
     */
    ResultData<Void> copyInstance(FlowInstanceCopyVO copyVO);

    /**
     * 分页查询抄送给我列表
     * @param query 查询参数
     * @return 列表
     */
    ResultData<PageResult<FlowInstanceCopyResult>> pageCopyMyList(FlowInstanceCopyQuery query);
}
