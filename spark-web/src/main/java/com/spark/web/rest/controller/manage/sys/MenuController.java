package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.manage.sys.IMenuService;
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
 * @since 2026-09-06 11:40:00
 */
@RestController
@RequestMapping("sys/menu")
public class MenuController {
    @Autowired
    private IMenuService menuService;

    /**
     * 查询菜单列表
     * @return 查询结果
     */
    @GetMapping("list")
    private ResultData<List<Menu>> listMenu() {
        return menuService.listMenu();
    }
}
