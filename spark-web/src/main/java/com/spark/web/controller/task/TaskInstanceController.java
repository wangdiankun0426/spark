package com.spark.web.controller.task;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.task.query.TaskInstanceQuery;
import com.spark.bean.task.result.TaskInstanceResult;
import com.spark.task.service.ITaskInstanceService;
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
 * @since 2026-08-18 15:20:00
 * 通用定时任务实例控制器
 */
@RestController
@RequestMapping("task/instance")
public class TaskInstanceController {
    @Autowired
    private ITaskInstanceService taskInstanceService;

    /**
     * 分页查询任务实例列表
     * @param query
     * @return 任务实例分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<TaskInstanceResult>> pageTaskInstanceList(TaskInstanceQuery query) {
        return taskInstanceService.pageTaskInstanceList(query);
    }

    /**
     * 查询任务实例详情
     * @param query 查询参数
     * @return 任务实例详情
     */
    @GetMapping("detail")
    private ResultData<TaskInstanceResult> queryTaskInstanceDetail(TaskInstanceQuery query) {
        return taskInstanceService.queryTaskInstanceDetail(query);
    }
}
