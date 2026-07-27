package com.spark.test.flowable;


import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/7/6 12:43
 */
@SpringBootTest
public class RuntimeServiceTest {


    @Autowired
    private RuntimeService runtimeService;

    @Test
    public void startProcessInstance() {
        String processDefinitionKey = "process_1762009234538";
        Map<String, Object> variables = new HashMap<>();
        variables.put("field1762008617581", 1);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey,variables);
        System.out.println("流程实例ID：" + processInstance.getId());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
        //流程实例ID：80ec7db3-5a24-11f0-8d8b-9a5f411efe29
        //流程定义ID：leave:1:9a3ed6cb-5a23-11f0-aecd-9a5f411efe29
    }

    /**
     * 删除流程实例
     * 触发 PROCESS_CANCELLED 事件
     */
    @Test
    public void dropProcessInstance() {
        runtimeService.deleteProcessInstance("f348b373-b7a5-11f0-b507-00ff54fdd5fc", "后悔了 不要了");
    }

    /**
     * 挂起流程实例
     */
    @Test
    public void suspendProcessInstance() {
        runtimeService.suspendProcessInstanceById("b79ed4e4-b7f7-11f0-8ef4-00ff54fdd5fc");
    }

    /**
     * 激活流程实例
     */
    @Test
    public void activateProcessInstance() {
        runtimeService.activateProcessInstanceById("b79ed4e4-b7f7-11f0-8ef4-00ff54fdd5fc");
    }
}
