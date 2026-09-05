package com.spark.web.rest.controller.kg;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.query.KgCommunityQuery;
import com.spark.common.bean.kg.result.KgCommunityResult;
import com.spark.kg.service.IKgCommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 19:00:00
 * 图谱社区管理
 */
@RestController
@RequestMapping("kg/community")
public class KgCommunityController {
    @Autowired
    private IKgCommunityService kgCommunityService;

    /**
     * 分页查询社区列表
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<KgCommunityResult>> pageCommunityList(KgCommunityQuery query) {
        return kgCommunityService.pageCommunityList(query);
    }
}
