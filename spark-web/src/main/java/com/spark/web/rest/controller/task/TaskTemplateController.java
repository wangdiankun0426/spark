package com.spark.web.rest.controller.task;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.task.query.TaskTemplateQuery;
import com.spark.common.bean.task.result.TaskTemplateResult;
import com.spark.common.bean.task.vo.TaskTemplateVO;
import com.spark.task.service.ITaskTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板控制器
 */
@RestController
@RequestMapping("task/template")
public class TaskTemplateController {
    @Autowired
    private ITaskTemplateService taskTemplateService;

    /**
     * 创建任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 创建结果
     */
    @PostMapping("create")
    private ResultData<Void> createTaskTemplate(TaskTemplateVO taskTemplateVO) {
        return taskTemplateService.createTaskTemplate(taskTemplateVO);
    }

    /**
     * 修改任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 修改结果
     */
    @PostMapping("update")
    private ResultData<Void> updateTaskTemplate(TaskTemplateVO taskTemplateVO) {
        return taskTemplateService.updateTaskTemplate(taskTemplateVO);
    }

    /**
     * 删除任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 删除结果
     */
    @PostMapping("delete")
    private ResultData<Void> deleteTaskTemplate(TaskTemplateVO taskTemplateVO) {
        return taskTemplateService.deleteTaskTemplate(taskTemplateVO);
    }

    /**
     * 分页查询任务模板列表
     * @param query 查询参数
     * @return 任务模板分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<TaskTemplateResult>> pageTaskTemplateList(TaskTemplateQuery query) {
        return taskTemplateService.pageTaskTemplateList(query);
    }

    /**
     * 查询任务模板详情
     * @param query 查询参数
     * @return 任务模板
     */
    @GetMapping("detail")
    private ResultData<TaskTemplateResult> detailTaskTemplate(TaskTemplateQuery query) {
        return taskTemplateService.detailTaskTemplate(query);
    }
}
