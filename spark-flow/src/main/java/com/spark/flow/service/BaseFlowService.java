package com.spark.flow.service;

import com.spark.bean.flow.query.FlowInstanceAssigneeQuery;
import com.spark.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.result.FlowInstanceAssigneeResult;
import com.spark.bean.flow.result.FlowInstanceNodeResult;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.constant.FlowParamCode;
import com.spark.dao.flow.FlowInstanceAssigneeDao;
import com.spark.dao.flow.FlowInstanceDao;
import com.spark.dao.flow.FlowInstanceNodeDao;
import com.spark.enums.FlowInstanceStatusEnum;
import com.spark.enums.VariableTypeEnum;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/6 23:18
 */
public class BaseFlowService extends BaseService {
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowInstanceNodeDao instanceNodeDao;
    @Autowired
    private FlowInstanceAssigneeDao instanceAssigneeDao;

    /**
     * 构造流程默认参数
     * @param instanceId
     * @return
     */
    protected Map<String, String> generateFlowDefaultParam(String instanceId) {
        Map<String, String> params = new HashMap<>();
        if (StringUtil.isBlank(instanceId)) {
            return params;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(instanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            return params;
        }
        params.put(FlowParamCode.FLOW_NAME, instanceResult.getName());
        params.put(FlowParamCode.APP_USER, instanceResult.getCreatedBy()+"");
        params.put(FlowParamCode.APP_USER_NAME, super.getObjName(instanceResult.getCreatedBy()));
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setInstanceId(instanceResult.getId());
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult != null) {
            FlowInstanceAssigneeQuery assigneeQuery = new FlowInstanceAssigneeQuery();
            assigneeQuery.setInstanceId(instanceResult.getId());
            assigneeQuery.setInstanceNodeId(instanceNodeResult.getId());
            assigneeQuery.setAssigneeSetId(instanceNodeResult.getAssigneeSetId());
            assigneeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
            List<FlowInstanceAssigneeResult> flowInstanceAssigneeList = instanceAssigneeDao.queryInstanceAssigneeList(assigneeQuery);
            if (CollectionUtil.isNotEmpty(flowInstanceAssigneeList)) {
                List<Long> assigneeIds = flowInstanceAssigneeList.stream().map(FlowInstanceAssigneeResult::getAssigneeId).toList();
                params.put(FlowParamCode.APP_ASSIGNEE, StringUtil.join(assigneeIds, ","));
            }
        }
        return params;
    }

    /**
     * 根据规则生成 流程值
     *
     * @param valueRule
     * @param defParamMap
     * @return
     */
    protected String generateFlowValue(String valueRule, Map<String, String> defParamMap) {
        if(StringUtils.isBlank(valueRule)) {
            return null;
        }
        List<String> generateRules = StringUtil.parseStringFormCode(valueRule,"#");
        for(String rule : generateRules) {
            if(StringUtils.isBlank(rule)) {
                continue;
            }
            if(!rule.contains(":")) {
                continue;
            }
            String[] arr = rule.split(":");
            String type = arr[0];
            String key = arr[1];
            String value = "";
            if (VariableTypeEnum.BASE.getType().equals(type)) {
                //基础数据
                value = defParamMap.get(key);
            } else {
                continue;
            }
            if(value == null) {
                value = "";
            }
            valueRule = valueRule.replace("#{"+rule+"}#", value);
        }
        return valueRule;
    }

}
