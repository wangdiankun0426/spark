package com.spark.web.controller.flow;

import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.bean.flow.vo.FlowInstanceVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.flow.service.IFlowInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 创建流程实例
     * @param instanceVO 流程实例数据
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createInstance(@RequestBody FlowInstanceVO instanceVO) {
        return instanceService.createInstance(instanceVO);
    }

    /**
     * 分页查询我申请的流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyAppliedList")
    public ResultData<PageResult<FlowInstanceResult>> pageMyAppliedList(FlowInstanceQuery query) {
        return instanceService.pageMyAppliedList(query);
    }

    /**
     * 分页查询我待办流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyPendingList")
    public ResultData<PageResult<FlowInstanceResult>> pageMyPendingList(FlowInstanceQuery query) {
        return instanceService.pageMyPendingList(query);
    }

    /**
     * 分页查询我的已办流程实例
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyPendedList")
    public ResultData<PageResult<FlowInstanceResult>> pageMyPendedList(FlowInstanceQuery query) {
        return instanceService.pageMyPendedList(query);
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
    @PostMapping("approval")
    public ResultData<Void> approveInstance(FlowInstanceVO instanceVO) {
        return instanceService.approveInstance(instanceVO);
    }
}
