package com.spark.flow.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.flow.entity.FlowInstanceCopy;
import com.spark.common.bean.flow.entity.FlowInstanceDiscuss;
import com.spark.common.bean.flow.query.FlowInstanceCopyQuery;
import com.spark.common.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.common.bean.flow.query.FlowInstanceQuery;
import com.spark.common.bean.flow.query.FlowTemplateNodeQuery;
import com.spark.common.bean.flow.result.FlowInstanceCopyResult;
import com.spark.common.bean.flow.result.FlowInstanceNodeResult;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.flow.result.FlowTemplateNodeResult;
import com.spark.common.bean.flow.vo.FlowInstanceCopyVO;
import com.spark.dao.flow.*;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.FlowInstanceLevelEnum;
import com.spark.common.enums.FlowInstanceStatusEnum;
import com.spark.common.enums.FlowTemplateNodePermissionEnum;
import com.spark.flow.service.IFlowInstanceCopyService;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/27 19:26
 */
@Service
public class FlowInstanceCopyServiceImpl extends BaseService<FlowInstanceCopyQuery, FlowInstanceCopyResult> implements IFlowInstanceCopyService {
    private final static Logger logger = LoggerFactory.getLogger(FlowInstanceCopyServiceImpl.class);
    @Autowired
    private FlowInstanceCopyDao instanceCopyDao;
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowInstanceNodeDao instanceNodeDao;
    @Autowired
    private FlowTemplateNodeDao templateNodeDao;
    @Autowired
    private FlowInstanceDiscussDao instanceDiscussDao;

    /**
     * 抄送流程实例
     * @param copyVO 抄送参数
     * @return 抄送结果
     */
    @Override
    public ResultData<Void> copyInstance(FlowInstanceCopyVO copyVO) {
        ResultData<Void> result = new ResultData<>();
        if (copyVO == null || copyVO.getInstanceId() == null || CollectionUtil.isEmpty(copyVO.getUserIds())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 校验实例存在
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setId(copyVO.getInstanceId());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_EXIST);
            return result;
        }
        // 校验节点权限
        if (copyVO.getNodeId() != null) {
            FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
            instanceNodeQuery.setId(copyVO.getNodeId());
            instanceNodeQuery.setInstanceId(instanceResult.getId());
            instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
            FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
            if (instanceNodeResult == null) {
                result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
                return result;
            }
            FlowTemplateNodeQuery templateNodeQuery = new FlowTemplateNodeQuery();
            templateNodeQuery.setNodeId(instanceNodeResult.getNodeId());
            templateNodeQuery.setTemplateId(instanceResult.getTemplateId());
            templateNodeQuery.setRevId(instanceResult.getTemplateRevId());
            FlowTemplateNodeResult templateNodeResult = templateNodeDao.queryTemplateNode(templateNodeQuery);
            if (templateNodeResult == null) {
                result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
                return result;
            }
            Integer permission = templateNodeResult.getPermission();
            if (permission == null || (permission & FlowTemplateNodePermissionEnum.ALLOW_COPY.getValue()) != FlowTemplateNodePermissionEnum.ALLOW_COPY.getValue()) {
                result.setErrorCode(ErrorCodeEnum.FLOW_INSTANCE_NOT_ALLOW);
                return result;
            }
        }
        // 批量插入抄送记录
        List<Long> userIds = copyVO.getUserIds().stream().distinct().toList();
        int insertCount = 0;
        for (Long userId : userIds) {
            FlowInstanceCopy instanceCopy = new FlowInstanceCopy();
            instanceCopy.setInstanceId(copyVO.getInstanceId());
            instanceCopy.setUserId(userId);
            instanceCopy.setNodeId(copyVO.getNodeId() == null ? null : copyVO.getNodeId().toString());
            int count = instanceCopyDao.insertDB(instanceCopy);
            if (count > 0) {
                insertCount++;
            }
        }
        if (insertCount > 0) {
            // 记录审批讨论
            String userNames = StringUtil.joinList(super.getObjNames(userIds), ",");
            this.saveFlowDiscuss(instanceResult.getId(), copyVO.getNodeId(), FlowInstanceStatusEnum.PROCESSING.getValue(), "抄送给：" + userNames);
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询抄送给我列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<FlowInstanceCopyResult>> pageCopyMyList(FlowInstanceCopyQuery query) {
        ResultData<PageResult<FlowInstanceCopyResult>> result = new ResultData<>();
        if (query == null) {
            query = new FlowInstanceCopyQuery();
        }
        query.setUserId(SessionHolder.getCurrentUserId());
        PageResult<FlowInstanceCopyResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 保存审批记录
     * @param instanceId 流程实例id
     * @param instanceNodeId 流程实例节点id
     * @param status 操作状态
     * @param discuss 内容
     */
    private void saveFlowDiscuss(Long instanceId, Long instanceNodeId, Integer status, String discuss) {
        FlowInstanceDiscuss instanceDiscuss = new FlowInstanceDiscuss();
        instanceDiscuss.setInstanceId(instanceId);
        instanceDiscuss.setInstanceNodeId(instanceNodeId);
        instanceDiscuss.setDiscuss(discuss);
        instanceDiscuss.setAssigneeId(SessionHolder.getCurrentUserId());
        instanceDiscuss.setStatus(status);
        int count = instanceDiscussDao.insertDB(instanceDiscuss);
        if (count < 1) {
            logger.error("saveFlowDiscuss error, insert db fail");
        }
    }

    /**
     * 补充列表
     * @param list 列表
     */

    @Override
    protected void supplyList(List<FlowInstanceCopyResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        List<Long> instanceIds = list.stream().map(FlowInstanceCopyResult::getInstanceId).distinct().toList();
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setIds(instanceIds);
        instanceQuery.setPage(false);
        List<FlowInstanceResult> instanceList = instanceDao.queryInstanceList(instanceQuery);
        Map<Long, FlowInstanceResult> instanceMap = instanceList.stream().collect(Collectors.toMap(FlowInstanceResult::getId, e -> e));
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                FlowInstanceResult instanceResult = instanceMap.get(item.getInstanceId());
                if (instanceResult == null) {
                    return;
                }
                item.setName(instanceResult.getName());
                item.setStatusName(FlowInstanceStatusEnum.indexOf(instanceResult.getStatus()).getDesc());
                item.setLevelName(FlowInstanceLevelEnum.indexOf(instanceResult.getLevel()).getDesc());
                item.setDeptName(this.getObjName(instanceResult.getDeptId()));
                item.setAppByName(this.getObjName(instanceResult.getCreatedBy()));
                item.setCreatedByName(this.getObjName(item.getCreatedBy()));
            });
        }
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    @Override
    protected int queryCount(FlowInstanceCopyQuery query){
        return instanceCopyDao.queryCopyCount(query);
    }

    /**
     * 分页列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<FlowInstanceCopyResult> queryList(FlowInstanceCopyQuery query){
        return instanceCopyDao.queryCopyList(query);
    }

}
