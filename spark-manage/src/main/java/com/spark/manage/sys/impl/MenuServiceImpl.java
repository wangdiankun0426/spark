package com.spark.manage.sys.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.sys.MenuDao;
import com.spark.manage.sys.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
@LogPrint
public class MenuServiceImpl implements IMenuService {
    @Autowired
    private MenuDao menuDao;

    /**
     * 查询菜单列表
     * @return 查询结果
     */
    @Override
    public ResultData<List<Menu>> listMenu() {
        ResultData<List<Menu>> result = new ResultData<>();
        List<Menu> menuList = menuDao.queryMenuList();
        result.setData(menuList);
        result.setCode(ResultData.OK);
        return result;
    }
}
