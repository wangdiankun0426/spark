package com.spark.web.controller.workflow;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.bean.workflow.query.WfInstanceQuery;
import com.spark.bean.workflow.result.WfInstanceNodeResult;
import com.spark.bean.workflow.result.WfInstanceResult;
import com.spark.workflow.service.IWorkflowInstanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 * AI工作流实例管理Controller
 */
@RestController
@RequestMapping("workflow/instance")
public class WorkflowInstanceController {

    @Autowired
    private IWorkflowInstanceService instanceService;

    /**
     * 分页查询运行实例
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<WfInstanceResult>> pageInstanceHistory(WfInstanceQuery query) {
        return instanceService.pageInstanceHistory(query);
    }

    /**
     * 查询运行实例详情
     *
     * @param query 查询参数
     * @return 实例详情
     */
    @GetMapping("detail")
    public ResultData<WfInstanceResult> queryInstanceDetail(WfInstanceQuery query) {
        return instanceService.queryInstanceDetail(query);
    }

    /**
     * 查询实例节点执行记录
     *
     * @param query 查询参数
     * @return 节点执行记录列表
     */
    @GetMapping("nodes")
    public ResultData<List<WfInstanceNodeResult>> queryInstanceNodes(WfInstanceNodeQuery query) {
        return instanceService.queryInstanceNodes(query);
    }

    /**
     * 通用工作流调用入口
     * POST /api/workflow/instance/{path}
     */
    @PostMapping("{path}")
    public ResultData<Map<String, Object>> executeWorkflow(@PathVariable String path, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        return instanceService.executeWorkflow(path, params, request);
    }
}
