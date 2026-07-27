package com.spark.web.controller.log;

import com.spark.bean.log.query.LogLoginQuery;
import com.spark.bean.log.result.LogLoginResult;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
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

    /**
     * 查询登录日志
     *
     * @param  query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyList")
    public ResultData<PageResult<LogLoginResult>> queryMyLoginLog(LogLoginQuery query) {
        return logLoginService.pageMyLogLoginList(query);
    }
}
