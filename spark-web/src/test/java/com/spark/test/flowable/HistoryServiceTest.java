package com.spark.test.flowable;


import org.flowable.engine.HistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/7/6 13:16
 */
@SpringBootTest
public class HistoryServiceTest {

    @Autowired
    private HistoryService historyService;

    @Test
    public void queryHistoricProcessInstance() {
        historyService.createHistoricProcessInstanceQuery()
                .list()
                .forEach(historicProcessInstance -> {
            System.out.println("流程实例ID：" + historicProcessInstance.getId());
            System.out.println("流程定义ID：" + historicProcessInstance.getProcessDefinitionId());
        });
    }

    @Test
    public void queryHistoricActivityInstance() {
        historyService.createHistoricActivityInstanceQuery()
                .processInstanceId("d05b164e-5a27-11f0-9d3d-9a5f411efe29")
                .list()
                .forEach(historicActivityInstance -> {
                    System.out.println("流程实例ID：" + historicActivityInstance.getProcessInstanceId());
                    System.out.println("流程定义ID：" + historicActivityInstance.getProcessDefinitionId());
                    System.out.println("活动ID：" + historicActivityInstance.getActivityId());
                    System.out.println("活动名称：" + historicActivityInstance.getActivityName());
                });
    }
}
