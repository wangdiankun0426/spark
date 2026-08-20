package com.spark.web.controller.task;

import com.spark.bean.base.ResultData;
import com.spark.bean.task.query.TaskTemplateParamQuery;
import com.spark.bean.task.result.TaskTemplateParamResult;
import com.spark.bean.task.vo.TaskTemplateParamVO;
import com.spark.task.service.ITaskTemplateParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/20 18:40
 */
@RestController
@RequestMapping("task/template/param")
public class TaskTemplateParamController {
    @Autowired
    private ITaskTemplateParamService taskTemplateParamService;

    /**
     * 查询任务模板参数列表
     * @param query 查询参数
     * @return 参数列表
     */
    @GetMapping("list")
    private ResultData<List<TaskTemplateParamResult>> queryTaskTemplateParamList(TaskTemplateParamQuery query) {
        return taskTemplateParamService.queryTaskTemplateParamList(query);
    }

    /**
     * 创建任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 创建结果
     */
    @PostMapping("create")
    private ResultData<Void> createTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO) {
        return taskTemplateParamService.createTaskTemplateParam(taskTemplateParamVO);
    }

    /**
     * 修改任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 修改结果
     */
    @PostMapping("update")
    private ResultData<Void> updateTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO) {
        return taskTemplateParamService.updateTaskTemplateParam(taskTemplateParamVO);
    }

    /**
     * 删除任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 删除结果
     */
    @PostMapping("delete")
    private ResultData<Void> deleteTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO) {
        return taskTemplateParamService.deleteTaskTemplateParam(taskTemplateParamVO);
    }
}
