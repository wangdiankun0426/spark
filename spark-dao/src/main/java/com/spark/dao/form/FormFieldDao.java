package com.spark.dao.form;

import com.spark.common.bean.form.entity.FormField;
import com.spark.common.bean.form.query.FormFieldQuery;
import com.spark.common.bean.form.result.FormFieldResult;
import com.spark.common.bean.form.vo.FormFieldVO;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 16:50
 */
public interface FormFieldDao extends BaseDao<FormField> {

    /**
     * 批量插入
     * @param formId
     * @param revId
     * @param createdBy
     * @param fieldList
     * @return
     */
    int batchInsert(@Param("formId") Long formId, @Param("revId") Long revId, @Param("createdBy")Long createdBy, @Param("fieldList") List<FormFieldVO> fieldList);

    /**
     * 查询表单字段列表
     * @param query
     * @return
     */
    List<FormFieldResult> queryFormFieldList(FormFieldQuery query);

    /**
     * 查询单条表单字段
     * @param query
     * @return
     */
    FormFieldResult queryFormField(FormFieldQuery query);
}