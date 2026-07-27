package com.spark.test.flowable;


import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/7/6 12:49
 */
@SpringBootTest
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void queryTaskList() {
        String processInstanceId = "840a8263-5a2b-11f0-8500-9a5f411efe29";
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for (Task task : taskList) {
            System.out.println("任务ID:" + task.getId());
            System.out.println("任务名称:" + task.getName());
            System.out.println("任务的创建时间:" + task.getCreateTime());
            System.out.println("任务的办理人:" + task.getAssignee());
            System.out.println("流程实例ID：" + task.getProcessInstanceId());
            //任务ID:80eec7a7-5a24-11f0-8d8b-9a5f411efe29
            //任务名称:班长审批
            //任务的创建时间:Sun Jul 06 12:48:47 CST 2025
            //任务的办理人:wangxiaoer
            //流程实例ID：80ec7db3-5a24-11f0-8d8b-9a5f411efe29
        }
    }

    @Test
    public void setAssignee() {
        taskService.setAssignee("d05c9cf3-5a27-11f0-9d3d-9a5f411efe29", "zhangsan");
    }

    @Test
    public void completeTask() {
        String taskId = "9f94d508-5a2b-11f0-9348-9a5f411efe29";
        String instanceId = "840a8263-5a2b-11f0-8500-9a5f411efe29";
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("days", 3);
//        taskService.setVariables(taskId, variables);
//        taskService.complete(taskId, variables);

        taskService.addComment(taskId,instanceId ,"我同意了 2！");
        taskService.complete(taskId);

        taskService.getTaskComments(taskId).forEach(comment -> {
            System.out.println(comment.getFullMessage());
            System.out.println(comment.getUserId());
            System.out.println(comment.getTime());
            System.out.println(comment.getTaskId());
        });
    }

    @Test
    public void queryTaskInstanceList() {
        taskService.createTaskQuery()
                .taskAssignee("admin")
                .list()
                .forEach(task -> {
                    System.out.println("任务ID:" + task.getId());
                    System.out.println("任务名称:" + task.getName());
                    System.out.println("任务的创建时间:" + task.getCreateTime());
                    System.out.println("任务的办理人:" + task.getAssignee());
                    System.out.println("流程实例ID：" + task.getProcessInstanceId());
                });
    }
}
