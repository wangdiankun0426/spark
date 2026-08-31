package com.spark.web.controller.kg;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kg.query.KgCommunityQuery;
import com.spark.bean.kg.result.KgCommunityResult;
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
