package com.spark.web.rest.controller.flow;

import com.spark.common.bean.flow.query.FlowInstanceAssigneeQuery;
import com.spark.common.bean.flow.query.FlowInstanceCopyQuery;
import com.spark.common.bean.flow.query.FlowInstanceQuery;
import com.spark.common.bean.flow.result.FlowInstanceAssigneeResult;
import com.spark.common.bean.flow.result.FlowInstanceCopyResult;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.flow.vo.FlowInstanceCopyVO;
import com.spark.common.bean.flow.vo.FlowInstanceVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.config.aspectj.annotation.Debounce;
import com.spark.flow.service.IFlowInstanceCopyService;
import com.spark.flow.service.IFlowInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-02 15:47:22
 */
@RestController
@RequestMapping("flow/instance")
public class FlowInstanceController {
    @Autowired
    private IFlowInstanceService instanceService;
    @Autowired
    private IFlowInstanceCopyService instanceCopyService;

    /**
     * 创建流程实例
     * @param instanceVO 流程实例数据
     * @return 创建结果
     */
    @Debounce
    @PostMapping("create")
    public ResultData<Void> createInstance(@RequestBody FlowInstanceVO instanceVO) {
        return instanceService.createInstance(instanceVO);
    }

    /**
     * 分页查询我申请的流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyApplicationList")
    public ResultData<PageResult<FlowInstanceResult>> pageMyApplicationList(FlowInstanceQuery query) {
        return instanceService.pageMyApplicationList(query);
    }

    /**
     * 分页查询我待办流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyTodoList")
    public ResultData<PageResult<FlowInstanceResult>> pageMyTodoList(FlowInstanceQuery query) {
        return instanceService.pageMyTodoList(query);
    }

    /**
     * 分页查询我的已办流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyDoneList")
    public ResultData<PageResult<FlowInstanceResult>> pageMyDoneList(FlowInstanceQuery query) {
        return instanceService.pageMyDoneList(query);
    }

    /**
     * 分页查询全部流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<FlowInstanceResult>> pageInstanceList(FlowInstanceQuery query) {
        return instanceService.pageInstanceList(query);
    }

    /**
     * 查询流程实例详情
     * @param query 查询参数
     * @return 详情
     */
    @GetMapping("showDetail")
    public ResultData<FlowInstanceResult> showInstanceDetail(FlowInstanceQuery query) {
        return instanceService.showInstanceDetail(query);
    }

    /**
     * 审批流程实例
     * @param instanceVO 审批参数
     * @return 响应
     */
    @Debounce
    @PostMapping("approval")
    public ResultData<Void> approveInstance(FlowInstanceVO instanceVO) {
        return instanceService.approveInstance(instanceVO);
    }

    /**
     * 管理员干预审批流程实例
     * @param instanceVO 审批参数
     * @return 响应
     */
    @Debounce
    @PostMapping("adminApproval")
    public ResultData<Void> adminApprovalInstance(@RequestBody FlowInstanceVO instanceVO) {
        return instanceService.adminApprovalInstance(instanceVO);
    }

    /**
     * 查询流程实例当前节点待审批人列表
     * @param query 查询参数
     * @return 审批人列表
     */
    @GetMapping("assigneeList")
    public ResultData<List<FlowInstanceAssigneeResult>> queryInstanceAssigneeList(FlowInstanceAssigneeQuery query) {
        return instanceService.queryInstanceAssigneeList(query);
    }

    /**
     * 管理员替换流程实例审批人
     * @param instanceVO 替换参数
     * @return 响应
     */
    @Debounce
    @PostMapping("replaceAssignee")
    public ResultData<Void> replaceInstanceAssignee(@RequestBody FlowInstanceVO instanceVO) {
        return instanceService.replaceInstanceAssignee(instanceVO);
    }

    /**
     * 催办流程实例
     * @param instanceVO 催办参数
     * @return 响应
     */
    @Debounce
    @PostMapping("urge")
    public ResultData<Void> urgeInstance(FlowInstanceVO instanceVO) {
        return instanceService.urgeInstance(instanceVO);
    }

    /**
     * 转办流程实例
     * @param instanceVO 转办参数
     * @return 响应
     */
    @Debounce
    @PostMapping("transfer")
    public ResultData<Void> transferInstance(FlowInstanceVO instanceVO) {
        return instanceService.transferInstance(instanceVO);
    }

    /**
     * 加签流程实例
     * @param instanceVO 加签参数
     * @return 响应
     */
    @Debounce
    @PostMapping("addSign")
    public ResultData<Void> addSignInstance(FlowInstanceVO instanceVO) {
        return instanceService.addSignInstance(instanceVO);
    }

    /**
     * 撤回流程实例
     * @param instanceVO 撤回参数
     * @return 响应
     */
    @Debounce
    @PostMapping("recall")
    public ResultData<Void> recallInstance(@RequestBody FlowInstanceVO instanceVO) {
        return instanceService.recallInstance(instanceVO);
    }

    /**
     * 抄送流程实例
     * @param copyVO 抄送参数
     * @return 响应
     */
    @Debounce
    @PostMapping("copy")
    public ResultData<Void> copyInstance(@RequestBody FlowInstanceCopyVO copyVO) {
        return instanceCopyService.copyInstance(copyVO);
    }

    /**
     * 分页查询抄送给我列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageCopyMyList")
    public ResultData<PageResult<FlowInstanceCopyResult>> pageCopyMyList(FlowInstanceCopyQuery query) {
        return instanceCopyService.pageCopyMyList(query);
    }
}
