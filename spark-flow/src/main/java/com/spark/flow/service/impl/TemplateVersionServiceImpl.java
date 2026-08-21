package com.spark.flow.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.bean.base.ResultData;
import com.spark.bean.flow.entity.FlowTemplate;
import com.spark.bean.flow.entity.FlowTemplateMsg;
import com.spark.bean.flow.entity.FlowTemplateNode;
import com.spark.bean.flow.entity.FlowTemplateNodeTask;
import com.spark.bean.flow.entity.FlowTemplateNodeTaskParam;
import com.spark.bean.flow.entity.FlowTemplateSequence;
import com.spark.bean.flow.entity.FlowTemplateVersion;
import com.spark.bean.flow.query.FlowTemplateQuery;
import com.spark.bean.flow.query.FlowTemplateVersionQuery;
import com.spark.bean.flow.result.FlowTemplateResult;
import com.spark.bean.flow.result.FlowTemplateVersionResult;
import com.spark.bean.flow.vo.FlowTemplateVersionVO;
import com.spark.dao.flow.FlowTemplateDao;
import com.spark.dao.flow.FlowTemplateMsgDao;
import com.spark.dao.flow.FlowTemplateNodeDao;
import com.spark.dao.flow.FlowTemplateNodeTaskDao;
import com.spark.dao.flow.FlowTemplateNodeTaskParamDao;
import com.spark.dao.flow.FlowTemplateSequenceDao;
import com.spark.dao.flow.FlowTemplateVersionDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.flow.service.FlowableService;
import com.spark.flow.service.IFlowTemplateVersionService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import com.spark.utils.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-01 13:34:43
 */
@Service
public class TemplateVersionServiceImpl extends BaseService<FlowTemplateVersionQuery, FlowTemplateVersionResult> implements IFlowTemplateVersionService {
    private final static Logger logger = LoggerFactory.getLogger(TemplateVersionServiceImpl.class);
    @Autowired
    private FlowTemplateVersionDao templateversionDao;
    @Autowired
    private FlowTemplateDao templateDao;
    @Autowired
    private FlowableService flowableService;
    @Autowired
    private FlowTemplateSequenceDao templateSequenceDao;
    @Autowired
    private FlowTemplateNodeDao templateNodeDao;
    @Autowired
    private FlowTemplateMsgDao templateMsgDao;
    @Autowired
    private FlowTemplateNodeTaskDao templateNodeTaskDao;
    @Autowired
    private FlowTemplateNodeTaskParamDao templateNodeTaskParamDao;
    @Value("${flow.bpmn.path}")
    private String flowBpmnPath;

    /**
     * 创建流程模板版本
     * @param templateVersionVO   流程模板版本信息
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createTemplateVersion(FlowTemplateVersionVO templateVersionVO) {
        ResultData<Void> result = new ResultData<>();
        if (templateVersionVO == null || templateVersionVO.getTemplateId() == null
                || StringUtil.isBlank(templateVersionVO.getBpmJson())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowTemplateQuery templateQuery = new FlowTemplateQuery();
        templateQuery.setId(templateVersionVO.getTemplateId());
        FlowTemplateResult templateResult = templateDao.queryTemplate(templateQuery);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
            return result;
        }
        String bpmnPath = flowBpmnPath + UUID.randomUUID().toString().replaceAll("-", "") + ".json";
        result = TextUtil.writeToText(bpmnPath, templateVersionVO.getBpmJson());
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        result = flowableService.deploy(templateResult.getProcessId(), templateVersionVO.getBpmJson());
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        FlowTemplateVersion templateVersion = new FlowTemplateVersion();
        BeanUtils.copyProperties(templateVersionVO, templateVersion);
        templateVersion.setProcessId(templateResult.getProcessId());
        int maxCode = templateversionDao.queryMaxRevCode(templateResult.getId());
        int revCode = maxCode+1;
        String revNum = this.convertRevNum(revCode);
        templateVersion.setRevCode(revCode);
        templateVersion.setRevNum(revNum);
        templateVersion.setBpmPath(bpmnPath);
        int count = templateversionDao.insertDB(templateVersion);
        if (count < 1) {
            logger.error("createTemplateVersion error, insert db fail");
            return result;
        }
        FlowTemplate template = new FlowTemplate();
        template.setId(templateResult.getId());
        template.setRevNum(templateVersion.getRevNum());
        template.setRevId(templateVersion.getId());
        count = templateDao.updateDBById(template);
        if (count < 1) {
            logger.error("createTemplateVersion error, update db fail");
            return result;
        }
        List<FlowTemplateNode> templateNodes = new ArrayList<>();
        List<FlowTemplateSequence> templateSequences = new ArrayList<>();
        List<FlowTemplateMsg> templateMsgs = new ArrayList<>();
        List<FlowTemplateNodeTask> templateNodeTasks = new ArrayList<>();
        this.convertBpmJson(templateVersionVO.getBpmJson(), templateNodes, templateSequences, templateMsgs, templateNodeTasks);
        if (CollectionUtil.isNotEmpty(templateNodes)) {
            templateNodeDao.batchInsert(template.getId(), templateVersion.getId(), templateNodes);
        }
        if (CollectionUtil.isNotEmpty(templateSequences)) {
            templateSequenceDao.batchInsert(template.getId(), templateVersion.getId(), templateSequences);
        }
        if (CollectionUtil.isNotEmpty(templateMsgs)) {
            templateMsgDao.batchInsert(template.getId(), templateVersion.getId(), templateMsgs);
        }
        // 节点任务逐条落库
        for (FlowTemplateNodeTask nodeTask : templateNodeTasks) {
            nodeTask.setTemplateId(template.getId());
            nodeTask.setRevId(templateVersion.getId());
            int taskCount = templateNodeTaskDao.insertDB(nodeTask);
            if (taskCount < 1) {
                logger.error("createTemplateVersion error, insert node task fail");
                continue;
            }
            if (CollectionUtil.isNotEmpty(nodeTask.getParams())) {
                templateNodeTaskParamDao.batchInsert(nodeTask.getId(), nodeTask.getParams());
            }
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 转换json 解析模板节点、模板连线、消息通知与节点任务数据
     *
     * @param bpmJson bpmn json
     * @param templateNodes 模板节点
     * @param templateSequences 模板连线
     * @param templateMsgs 消息模板
     * @param templateNodeTasks 节点任务
     */
    private void convertBpmJson(String bpmJson, List<FlowTemplateNode> templateNodes, List<FlowTemplateSequence> templateSequences, List<FlowTemplateMsg> templateMsgs, List<FlowTemplateNodeTask> templateNodeTasks) {
        JSONObject bpmObject = JSONObject.parseObject(bpmJson);
        JSONArray nodes = bpmObject.getJSONArray("nodes");
        for (Object node : nodes) {
            JSONObject el = (JSONObject) node;
            String id = el.getString("id");
            String type = el.getString("type");
            String name = el.getString("name");
            Integer assigneeType = el.getInteger("assigneeType");
            String assignee = el.getString("assignee");
            Integer permission = el.getInteger("permission");
            Integer approveType = el.getInteger("approveType");
            Boolean urgeEnabled = el.getBoolean("urgeEnabled");
            Integer urgeInterval = el.getInteger("urgeInterval");
            FlowTemplateNode templateNode = new FlowTemplateNode();
            templateNode.setNodeId(id);
            templateNode.setType(type);
            templateNode.setName(name);
            templateNode.setAssigneeType(assigneeType);
            templateNode.setAssignee(assignee);
            templateNode.setPermission(permission);
            templateNode.setApproveType(approveType);
            templateNode.setUrgeEnabled(urgeEnabled);
            templateNode.setUrgeInterval(urgeInterval);
            templateNodes.add(templateNode);
        }
        JSONArray sequences = bpmObject.getJSONArray("sequences");
        for (Object sequence : sequences) {
            JSONObject seq = (JSONObject) sequence;
            String id = seq.getString("id");
            String sourceRef = seq.getString("sourceRef");
            String targetRef = seq.getString("targetRef");
            String condition = seq.getString("conditionExpression");
            FlowTemplateSequence templateSequence = new FlowTemplateSequence();
            templateSequence.setSequenceId(id);
            templateSequence.setSourceRef(sourceRef);
            templateSequence.setTargetRef(targetRef);
            templateSequence.setCondition(condition);
            templateSequences.add(templateSequence);
        }
        JSONArray notices = bpmObject.getJSONArray("notices");
        for (Object notice : notices) {
            JSONObject not = (JSONObject) notice;
            Integer type = not.getInteger("type");
            if (type == null) {
                continue;
            }
            FlowTemplateMsg templateMsg = new FlowTemplateMsg();
            templateMsg.setType(type);
            templateMsg.setEnabled(not.getBoolean("enabled"));
            templateMsg.setContent(not.getString("content"));
            templateMsg.setRecipient(not.getString("recipient"));
            templateMsgs.add(templateMsg);
        }
        JSONArray nodeTasks = bpmObject.getJSONArray("nodeTasks");
        if (nodeTasks == null) {
            return;
        }
        for (Object nodeTask : nodeTasks) {
            JSONObject task = (JSONObject) nodeTask;
            Long taskTemplateId = task.getLong("taskTemplateId");
            if (taskTemplateId == null) {
                continue;
            }
            FlowTemplateNodeTask templateNodeTask = new FlowTemplateNodeTask();
            templateNodeTask.setNodeId(task.getString("nodeId"));
            templateNodeTask.setTaskTemplateId(taskTemplateId);
            templateNodeTask.setExecuteType(task.getInteger("executeType"));
            templateNodeTask.setSort(task.getInteger("sort"));
            List<FlowTemplateNodeTaskParam> taskParams = new ArrayList<>();
            JSONArray params = task.getJSONArray("params");
            if (params != null) {
                for (Object param : params) {
                    JSONObject el = (JSONObject) param;
                    if (el.getString("code") == null) {
                        continue;
                    }
                    FlowTemplateNodeTaskParam taskParam = new FlowTemplateNodeTaskParam();
                    taskParam.setName(el.getString("name"));
                    taskParam.setCode(el.getString("code"));
                    taskParam.setType(el.getInteger("type"));
                    taskParam.setValue(el.getString("value"));
                    taskParams.add(taskParam);
                }
            }
            templateNodeTask.setParams(taskParams);
            templateNodeTasks.add(templateNodeTask);
        }
    }

    /**
     * 查询流程模板版本详情
     * @param query 查询参数
     * @return 查询结果
     */
    @Override
    public ResultData<FlowTemplateVersionResult> queryTemplateVersionDetail(FlowTemplateVersionQuery query) {
        ResultData<FlowTemplateVersionResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowTemplateVersionResult templateVersionResult = templateversionDao.queryTemplateVersion(query);
        if (templateVersionResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        FlowTemplateQuery templateQuery = new FlowTemplateQuery();
        templateQuery.setId(templateVersionResult.getTemplateId());
        FlowTemplateResult templateResult = templateDao.queryTemplate(templateQuery);
        if (templateResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
            return result;
        }
        templateVersionResult.setFormId(templateResult.getFormId());
        String bpmPath = templateVersionResult.getBpmPath();
        if (StringUtil.isNotBlank(bpmPath)) {
            ResultData<String> fromText = TextUtil.getFromText(bpmPath, false);
            templateVersionResult.setBpmJson(fromText.getData());
        }
        templateVersionResult.setBpmPath(null);
        result.setData(templateVersionResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<FlowTemplateVersionResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        // todo 补充其他数据

    }

    /**
     * 将revCode转成revNum
     * @param revCode 版本code
     * @return 版本号
     */
    private String convertRevNum(int revCode) {
        return String.format("%.1f", revCode/10.0);
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(FlowTemplateVersionQuery query) {
        return templateversionDao.queryTemplateVersionCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<FlowTemplateVersionResult> queryList(FlowTemplateVersionQuery query) {
        return templateversionDao.queryTemplateVersionList(query);
    }
}
