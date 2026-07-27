package com.spark.web.controller;

import com.spark.bean.base.ResultData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/2 8:21
 */
@RestController
public class IndexController {

    /**
     * 添加系统欢迎接口
     * @return 系统欢迎信息
     */
    @GetMapping
    public ResultData<Void> index() {
        ResultData<Void> result = new ResultData<>();
        result.setMessage("Welcome to use spark.ms server v1.0.0 !");
        result.setCode(ResultData.OK);
        return result;
    }
}
