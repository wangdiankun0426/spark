package com.spark.test.flowable;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.bean.base.BaseException;
import com.spark.enums.ErrorCodeEnum;
import com.spark.utils.StringUtil;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
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
 * @since 2025/7/6 12:40
 */
@SpringBootTest
public class RepositoryServiceTest {

    private String bpmJson = "{\n" +
            "  \"processId\": \"process_1761803767273\",\n" +
            "  \"processName\": \"流程设计\",\n" +
            "  \"elements\": [\n" +
            "    {\n" +
            "      \"id\": \"element_1\",\n" +
            "      \"type\": \"startEvent\",\n" +
            "      \"name\": \"开始\",\n" +
            "      \"x\": 100,\n" +
            "      \"y\": 200\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"element_2\",\n" +
            "      \"type\": \"userTask\",\n" +
            "      \"name\": \"审批任务\",\n" +
            "      \"x\": 250,\n" +
            "      \"y\": 200,\n" +
            "      \"assignee\": \"经理\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"element_3\",\n" +
            "      \"type\": \"exclusiveGateway\",\n" +
            "      \"name\": \"判断\",\n" +
            "      \"x\": 400,\n" +
            "      \"y\": 200,\n" +
            "      \"gatewayName\": \"是否通过\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"element_4\",\n" +
            "      \"type\": \"endEvent\",\n" +
            "      \"name\": \"通过\",\n" +
            "      \"x\": 550,\n" +
            "      \"y\": 150\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"element_5\",\n" +
            "      \"type\": \"endEvent\",\n" +
            "      \"name\": \"拒绝\",\n" +
            "      \"x\": 550,\n" +
            "      \"y\": 250\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"element_6\",\n" +
            "      \"type\": \"endEvent\",\n" +
            "      \"name\": \"结束\",\n" +
            "      \"x\": 700,\n" +
            "      \"y\": 200\n" +
            "    }\n" +
            "  ],\n" +
            "  \"sequences\": [\n" +
            "    {\n" +
            "      \"id\": \"connection_1\",\n" +
            "      \"sourceRef\": \"element_1\",\n" +
            "      \"targetRef\": \"element_2\",\n" +
            "      \"conditionExpression\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"connection_2\",\n" +
            "      \"sourceRef\": \"element_2\",\n" +
            "      \"targetRef\": \"element_3\",\n" +
            "      \"conditionExpression\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"connection_3\",\n" +
            "      \"sourceRef\": \"element_3\",\n" +
            "      \"targetRef\": \"element_4\",\n" +
            "      \"conditionExpression\": \"${score > 80}\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"connection_4\",\n" +
            "      \"sourceRef\": \"element_3\",\n" +
            "      \"targetRef\": \"element_5\",\n" +
            "      \"conditionExpression\": \"${score <= 80}\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"connection_5\",\n" +
            "      \"sourceRef\": \"element_4\",\n" +
            "      \"targetRef\": \"element_6\",\n" +
            "      \"conditionExpression\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"connection_6\",\n" +
            "      \"sourceRef\": \"element_5\",\n" +
            "      \"targetRef\": \"element_6\",\n" +
            "      \"conditionExpression\": \"\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void createDeploymentByModel() {
        String processId = "process_"+System.currentTimeMillis();
        BpmnModel model = convertJsonToBpmnModel(processId, bpmJson);
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(processId+".bpmn20.xml", model)
                .deploy();
        System.out.println("已部署流程模板ID::"+deployment.getParentDeploymentId());
        System.out.println("已部署流程模板名称::"+deployment.getName());
        List<ProcessDefinition> leave = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processId)
                .list();
        leave.forEach(processDefinition -> {
            System.out.println("流程定义ID::"+processDefinition.getId());
            System.out.println("流程定义名称::"+processDefinition.getName());
            System.out.println("流程定义key::"+processDefinition.getKey());
            System.out.println("流程定义版本::"+processDefinition.getVersion());
        });
    }

    /**
     * 转换json为bpmnModel
     * @param processId
     * @param bpmJson
     * @return
     */
    private BpmnModel convertJsonToBpmnModel(String processId, String bpmJson) {
        BpmnModel bpmnModel = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId(processId);
        bpmnModel.addProcess(process);
        JSONObject bpmObject = JSONObject.parseObject(bpmJson);
        JSONArray elements = bpmObject.getJSONArray("elements");
        for (Object element : elements) {
            JSONObject el = (JSONObject) element;
            String id = el.getString("id");
            String type = el.getString("type");
            String name = el.getString("name");
            FlowElement flowElement = null;
            switch (type) {
                case "startEvent":
                    flowElement = new StartEvent();
                    break;
                case "userTask":
                    UserTask userTask = new UserTask();
                    userTask.setAssignee(el.getString("assignee"));
                    flowElement = userTask;
                    break;
                case "exclusiveGateway":
                    ExclusiveGateway gateway = new ExclusiveGateway();
                    gateway.setName(el.getString("gatewayName"));
                    flowElement = gateway;
                    break;
                case "endEvent":
                    flowElement = new EndEvent();
                    break;
                default:
                    throw new BaseException(ErrorCodeEnum.FLOW_NODE_TYPE_UNKNOWN);
            }
            flowElement.setId(id);
            flowElement.setName(name);
            process.addFlowElement(flowElement);
        }
        JSONArray sequences = bpmObject.getJSONArray("sequences");
        for (Object sequence : sequences) {
            JSONObject seq = (JSONObject) sequence;
            String id = seq.getString("id");
            String sourceRef = seq.getString("sourceRef");
            String targetRef = seq.getString("targetRef");
            String condition = seq.getString("conditionExpression");
            SequenceFlow sequenceFlow = new SequenceFlow();
            sequenceFlow.setId(id);
            sequenceFlow.setSourceRef(sourceRef);
            sequenceFlow.setTargetRef(targetRef);
            if (StringUtil.isNotBlank(condition)) {
                sequenceFlow.setConditionExpression(condition);
            }
            process.addFlowElement(sequenceFlow);
        }
        return bpmnModel;
    }
    @Test
    public void createDeployment() {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave.bpmn20.xml")
                .name("学生请假申请流程").key("leave")
                .deploy();
        System.out.println("已部署流程模板ID::"+deployment.getId());
        System.out.println("已部署流程模板名称::"+deployment.getName());
        //已部署流程模板ID::
        //已部署流程模板名称::学生请假申请流程
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("流程定义ID::"+processDefinition.getId());
        System.out.println("流程定义名称::"+processDefinition.getName());
        System.out.println("流程定义key::"+processDefinition.getKey());
        System.out.println("流程定义版本::"+processDefinition.getVersion());
    }

    @Test
    public void queryProcessDefinitionId() {
        String deploymentId = "9a27a549-5a23-11f0-aecd-9a5f411efe29";
        List<ProcessDefinition> leave = repositoryService.createProcessDefinitionQuery()
//                .deploymentId(deploymentId)
                .processDefinitionKey("leave")
                .list();
       leave.forEach(processDefinition -> {
           System.out.println("流程定义ID::"+processDefinition.getId());
           System.out.println("流程定义名称::"+processDefinition.getName());
           //流程定义ID::leave:1:9a3ed6cb-5a23-11f0-aecd-9a5f411efe29
           //流程定义名称::leave
       });

    }

    @Test
    public void queryProcessDefinitionByKey() {
        String key = "process_1762009234538";
        List<ProcessDefinition> leave = repositoryService.createProcessDefinitionQuery()
//                .deploymentId(deploymentId)
                .processDefinitionKey(key)
                .list();
        leave.forEach(processDefinition -> {
            System.out.println("流程定义ID::"+processDefinition.getId());
            System.out.println("流程定义名称::"+processDefinition.getName());
            //流程定义ID::leave:1:9a3ed6cb-5a23-11f0-aecd-9a5f411efe29
            //流程定义名称::leave
        });

    }
}
