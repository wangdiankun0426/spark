package com.spark.web.rest.controller.form;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormVersionResult;
import com.spark.form.service.IFormVersionService;
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
 * @since 2024/7/3 17:28
 */
@RestController
@RequestMapping("form/version")
public class FormVersionController {
    @Autowired
    private IFormVersionService formVersionService;

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<FormVersionResult>> pageFormVersionList(FormVersionQuery query) {
        return formVersionService.pageFormVersionList(query);
    }
}
