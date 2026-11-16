package com.spark.dao.form;

import com.spark.common.bean.base.SessionHolder;
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

    /**
     * 按业务对象id逻辑删除表单对象值，自动填充修改人审计字段
     * @param objId 业务对象id
     * @return 删除条数
     */
    default int deleteDBByObjId(Long objId) {
        if (objId == null) {
            return -1;
        }
        Long updatedBy = SessionHolder.getCurrentUserId();
        return deleteByObjId(objId, updatedBy);
    }

    /**
     * 按业务对象id逻辑删除存活表单对象值
     * @param objId 业务对象id
     * @param updatedBy 修改人id
     * @return 删除条数
     */
    int deleteByObjId(@Param("objId") Long objId, @Param("updatedBy") Long updatedBy);
}
