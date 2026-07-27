package com.spark.web.controller.flow;

import com.spark.bean.flow.query.FlowTemplateVersionQuery;
import com.spark.bean.flow.result.FlowTemplateVersionResult;
import com.spark.bean.flow.vo.FlowTemplateVersionVO;
import com.spark.bean.base.ResultData;
import com.spark.flow.service.IFlowTemplateVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-01 13:33:32
 */
@RestController
@RequestMapping("flow/template/version")
public class FlowTemplateVersionController {

    @Autowired
    private IFlowTemplateVersionService templateVersionService;

    /**
     * 创建流程模板版本
     * @param templateVersionVO 模板版本信息
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createTemplateVersion(@RequestBody FlowTemplateVersionVO templateVersionVO) {
        return templateVersionService.createTemplateVersion(templateVersionVO);
    }

    /**
     * 查询流程模板版本详情
     * @param query 查询参数
     * @return 查询结果
     */
    @GetMapping("detail")
    public ResultData<FlowTemplateVersionResult> queryTemplateVersionDetail(FlowTemplateVersionQuery query) {
        return templateVersionService.queryTemplateVersionDetail(query);
    }
}
