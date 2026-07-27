package com.spark.web.controller.form;

import com.spark.bean.form.entity.Form;
import com.spark.bean.form.query.FormQuery;
import com.spark.bean.form.result.FormResult;
import com.spark.bean.form.vo.FormVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.form.service.IFormService;
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
 * @since 2024-07-01 09:11:13
 */
@RestController
@RequestMapping("form")
public class FormController {

    @Autowired
    private IFormService formService;

    /**
     * 创建表单
     * @param formVO 表单参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Form> createForm(FormVO formVO) {
        return formService.createForm(formVO);
    }

    /**
     * 修改表单
     * @param formVO 表单参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateForm(FormVO formVO) {
        return formService.updateForm(formVO);
    }

    /**
     * 删除表单
     * @param formVO 表单参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteForm(FormVO formVO) {
        return formService.deleteForm(formVO);
    }

    /**
     * 分页查询表单
     * @param query 查询参数
     * @return 表单列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<FormResult>> pageFormList(FormQuery query) {
        return formService.pageFormList(query);
    }

    /**
     * 查询表单列表
     * @param query 查询参数
     * @return 表单列表
     */
    @GetMapping("list")
    public ResultData<List<FormResult>> queryFormList(FormQuery query) {
        return formService.queryFormList(query);
    }

    /**
     * 查询表单详情
     * @param query 查询参数
     * @return 表单详情
     */
    @GetMapping("detail")
    public ResultData<FormResult> queryFormDetail(FormQuery query) {
        return formService.queryFormDetail(query);
    }

    /**
     * 查询表单json
     * @param query 查询参数
     * @return 表单json
     */
    @GetMapping("queryJson")
    public ResultData<String> queryFormJson(FormQuery query) {
        return formService.queryFormJson(query);
    }

    /**
     * 保存表单json
     * @param formVO 表单参数
     * @return 保存结果
     */
    @PostMapping("saveJson")
    public ResultData<Void> saveFormJson(@RequestBody FormVO formVO) {
        return formService.saveFormJson(formVO);
    }
}
