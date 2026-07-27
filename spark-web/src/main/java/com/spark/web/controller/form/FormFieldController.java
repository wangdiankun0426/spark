package com.spark.web.controller.form;

import com.spark.bean.base.ResultData;
import com.spark.bean.form.query.FormFieldQuery;
import com.spark.bean.form.result.FormFieldResult;
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
