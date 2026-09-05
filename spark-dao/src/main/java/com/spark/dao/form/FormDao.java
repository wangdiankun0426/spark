package com.spark.dao.form;

import com.spark.common.bean.form.entity.Form;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.result.FormResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Select;

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
public interface FormDao extends BaseDao<Form> {
    /**
     * 插入数据
     * @param form
     * @return
     */
    @Override
    int insert(Form form);

    /**
     * 删除数据
     * @param form
     * @return
     */
    @Override
    int deleteById(Form form);

    /**
     * 修改数据
     * @param form
     * @return
     */
    @Override
    int updateById(Form form);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryFormCount(FormQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FormResult> queryFormList(FormQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    FormResult queryForm(FormQuery query);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from form")
    Long queryFormMaxId();
}
