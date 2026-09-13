package com.spark.web.rest.controller.manage.log;

import com.spark.common.bean.log.query.LogLoginQuery;
import com.spark.common.bean.log.result.LogLoginResult;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.manage.log.ILogLoginService;
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
 * @since 2024/4/3 15:23
 */
@RestController
@RequestMapping("log/login")
public class LogLoginController {
    @Autowired
    private ILogLoginService logLoginService;

    /**
     * 分页查询
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<LogLoginResult>> pageLogLoginList(LogLoginQuery query) {
        return logLoginService.pageLogLoginList(query);
    }

}
