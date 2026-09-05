package com.spark.form.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.form.query.FormVersionQuery;
import com.spark.common.bean.form.result.FormVersionResult;
import com.spark.dao.form.FormVersionDao;
import com.spark.form.service.IFormVersionService;
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 2024/7/3 17:33
 */
@Service
public class FormVersionServiceImpl extends BaseService<FormVersionQuery, FormVersionResult> implements IFormVersionService {
    private final static Logger logger = LoggerFactory.getLogger(FormVersionServiceImpl.class);
    @Autowired
    private FormVersionDao formVersionDao;

    /**
     * 分页查询
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<PageResult<FormVersionResult>> pageFormVersionList(FormVersionQuery query) {
        ResultData<PageResult<FormVersionResult>> result = new ResultData<>();
        if (query == null) {
            query = new FormVersionQuery();
        }
        PageResult<FormVersionResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<FormVersionResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(FormVersionQuery query) {
        return formVersionDao.queryFormVersionCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<FormVersionResult> queryList(FormVersionQuery query) {
        return formVersionDao.queryFormVersionList(query);
    }
}
