package com.spark.web.controller.log;

import com.spark.bean.log.query.LogOperateQuery;
import com.spark.bean.log.result.LogOperateResult;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.manage.log.ILogOperateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 19:26
 */
@RestController
@RequestMapping("log/operate")
public class LogOperateController {
    @Autowired
    private ILogOperateService logOperateService;

    /**
     * 分页查询
     * @param query 查询参数
     * @return  查询结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<LogOperateResult>> pageLogOperateList(LogOperateQuery query) {
        return logOperateService.pageLogOperateList(query);
    }

    /**
     * 查询操作类型列表
     * @return 列表
     */
    @GetMapping("typeList")
    public ResultData<List<Map<String, Object>>> queryLogOperateTypeList() {
        return logOperateService.queryLogOperateTypeList();
    }

    /**
     * 查询操作日志
     * @return 列表
     */
    @GetMapping("pageMyList")
    public ResultData<PageResult<LogOperateResult>> queryMyOperateLog(LogOperateQuery query) {
        return logOperateService.pageMyLogOperateList(query);
    }

    /**
     * 查询操作日志统计
     * @return 列表
     */
    @GetMapping("statistics")
    public ResultData<List<Map<String, Object>>> queryOperateStatistics() {
        return logOperateService.queryOperateStatistics();
    }

    /**
     * 查询对象操作记录列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("objList")
    public ResultData<List<LogOperateResult>> queryObjLogOperateList(LogOperateQuery query) {
        return logOperateService.queryObjLogOperateList(query);
    }
}
