package com.spark.dao.form;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.form.entity.FormObj;
import com.spark.common.bean.form.query.FormObjQuery;
import com.spark.common.bean.form.result.FormObjResult;
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
 * @since 2026/7/15 20:51
 */
public interface FormObjDao extends BaseDao<FormObj> {
    /**
     * 插入数据
     * @param formObj
     * @return
     */
    @Override
    int insert(FormObj formObj);

    /**
     * 根据id修改数据
     * @param formObj
     * @return
     */
    @Override
    int updateById(FormObj formObj);

    /**
     * 根据id删除数据
     * @param formObj
     * @return
     */
    @Override
    int deleteById(FormObj formObj);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryFormObjCount(FormObjQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FormObjResult> queryFormObjList(FormObjQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    FormObjResult queryFormObj(FormObjQuery query);

    /**
     * 按业务对象id逻辑删除表单对象，自动填充修改人审计字段
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
     * 按业务对象id逻辑删除存活表单对象
     * @param objId 业务对象id
     * @param updatedBy 修改人id
     * @return 删除条数
     */
    int deleteByObjId(@Param("objId") Long objId, @Param("updatedBy") Long updatedBy);
}
