package com.spark.web.controller.llm;

import com.spark.bean.llm.query.ModelQuery;
import com.spark.bean.llm.result.ModelResult;
import com.spark.bean.llm.vo.ModelVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.llm.service.IModelService;
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
 * @since 2026-05-14 09:49:10
 */
@RestController
@RequestMapping("llm/model")
public class ModelController {

    @Autowired
    private IModelService modelService;

    /**
     * 创建模型
     * @param modelVO  创建模型参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createModel(ModelVO modelVO) {
        return modelService.createModel(modelVO);
    }

    /**
     * 修改模型
     * @param modelVO 修改模型参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateModel(ModelVO modelVO) {
        return modelService.updateModel(modelVO);
    }

    /**
     * 删除模型
     * @param modelVO 删除模型参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteModel(ModelVO modelVO) {
        return modelService.deleteModel(modelVO);
    }

    /**
     * 分页查询模型
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<ModelResult>> pageModelList(ModelQuery query) {
        return modelService.pageModelList(query);
    }

    /**
     * 查询模型详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<ModelResult> queryModelDetail(ModelQuery query) {
        return modelService.queryModelDetail(query);
    }
}
