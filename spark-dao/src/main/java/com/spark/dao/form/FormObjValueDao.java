package com.spark.dao.form;

import com.spark.common.bean.form.entity.FormObjValue;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.result.FormObjValueResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/28 21:01
 */
public interface FormObjValueDao {
    /**
     * 批量插入
     * @param list
     * @return
     */
    int batchInsert(@Param("list") List<FormObjValue> list);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FormObjValueResult> queryFormObjValueList(FormObjValueQuery query);
}
