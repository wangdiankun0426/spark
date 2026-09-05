package com.spark.web.rest.controller.flow;

import com.spark.common.bean.flow.query.FlowTemplateQuery;
import com.spark.common.bean.flow.result.FlowTemplateResult;
import com.spark.common.bean.flow.vo.FlowTemplateVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.flow.service.IFlowTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-10-29 20:57:44
 */
@RestController
@RequestMapping("flow/template")
public class FlowTemplateController {

    @Autowired
    private IFlowTemplateService templateService;

    /**
     * 创建流程模板
     * @param templateVO 流程模板数据
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createTemplate(FlowTemplateVO templateVO) {
        return templateService.createTemplate(templateVO);
    }

    /**
     * 修改流程模板
     * @param templateVO 流程模板数据
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateTemplate(FlowTemplateVO templateVO) {
        return templateService.updateTemplate(templateVO);
    }

    /**
     * 删除流程模板
     * @param templateVO 删除流程模板数据
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteTemplate(FlowTemplateVO templateVO) {
        return templateService.deleteTemplate(templateVO);
    }

    /**
     * 分页查询流程模板
     * @param query 查询参数
     * @return 查询结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<FlowTemplateResult>> pageTemplateList(FlowTemplateQuery query) {
        return templateService.pageTemplateList(query);
    }

    /**
     * 查询流程模板详情
     * @param query 查询参数
     * @return 查询结果
     */
    @GetMapping("detail")
    public ResultData<FlowTemplateResult> queryTemplateDetail(FlowTemplateQuery query) {
        return templateService.queryTemplateDetail(query);
    }

    /**
     * 显示流程模板详情
     * @param query 显示流程模板详情参数
     * @return 显示结果
     */
    @GetMapping("showDetail")
    public ResultData<FlowTemplateResult> showTemplateDetail(FlowTemplateQuery query) {
        return templateService.showTemplateDetail(query);
    }
}
