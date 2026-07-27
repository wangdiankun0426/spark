package com.spark.web.controller.form;

import com.spark.bean.base.ResultData;
import com.spark.bean.form.query.FormObjQuery;
import com.spark.bean.form.result.FormObjResult;
import com.spark.bean.form.vo.FormObjValueVO;
import com.spark.form.service.IFormObjValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/15 21:11
 */
@RestController
@RequestMapping("form/value")
public class FormValueController {
    @Autowired
    private IFormObjValueService formObjValueService;

    /**
     * 保存表单数据
     * @param valueVO 表单数据
     * @return 创建结果
     */
    @PostMapping("save")
    private ResultData<Void> saveFormObjValues(@RequestBody FormObjValueVO valueVO) {
        return formObjValueService.saveFormObjValues(valueVO);
    }

    /**
     * 查询表单数据
     * @param query 查询参数
     * @return 表单数据详情
     */
    @GetMapping("detail")
    public ResultData<FormObjResult> queryFormObjDetail(FormObjQuery query) {
        return formObjValueService.queryFormObjDetail(query);
    }
}
