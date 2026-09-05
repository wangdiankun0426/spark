package com.spark.kg.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.query.KgCommunityQuery;
import com.spark.common.bean.kg.result.KgCommunityResult;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 19:00:00
 * 图谱社区服务接口
 */
public interface IKgCommunityService {

    /**
     * 分页查询社区列表
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<KgCommunityResult>> pageCommunityList(KgCommunityQuery query);
}
