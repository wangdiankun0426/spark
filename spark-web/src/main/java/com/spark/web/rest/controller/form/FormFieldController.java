package com.spark.web.rest.controller.form;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormFieldQuery;
import com.spark.common.bean.form.result.FormFieldResult;
import com.spark.form.service.IFormFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/1 23:46
 */
@RestController
@RequestMapping("form/field")
public class FormFieldController {

    @Autowired
    private IFormFieldService formFieldService;

    /**
     * 查询表单字段列表
     * @param query 查询参数
     * @return 表单字段列表
     */
    @GetMapping("list")
    public ResultData<List<FormFieldResult>> queryFormFieldList(FormFieldQuery query) {
        return formFieldService.queryFormFieldList(query);
    }
}
