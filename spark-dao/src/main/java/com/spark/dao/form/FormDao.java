package com.spark.dao.form;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.form.entity.Form;
import com.spark.common.bean.form.query.FormQuery;
import com.spark.common.bean.form.result.FormResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
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
     * 查询单条并锁定表单行
     * @param query 查询参数
     * @return 表单
     */
    FormResult queryFormForUpdate(FormQuery query);

    /**
     * 按当前版本原子更新表单版本，自动填充修改人和时间
     * @param id 表单id
     * @param expectedRevId 期望的当前版本id
     * @param revId 新版本id
     * @param revNum 新版本号
     * @return 更新条数
     */
    default int updateFormRevIdCasDB(Long id, Long expectedRevId, Long revId, String revNum) {
        if (id == null || expectedRevId == null || revId == null || revNum == null) {
            return -1;
        }
        Long updatedBy = SessionHolder.getCurrentUserId();
        long updatedTime = System.currentTimeMillis();
        Timestamp updatedDt = new Timestamp(updatedTime);
        return updateFormRevIdCas(id, expectedRevId, revId, revNum, updatedBy, updatedDt);
    }

    /**
     * 按当前版本原子更新表单版本
     * @param id 表单id
     * @param expectedRevId 期望的当前版本id
     * @param revId 新版本id
     * @param revNum 新版本号
     * @param updatedBy 修改人id
     * @param updatedDt 修改时间
     * @return 更新条数
     */
    int updateFormRevIdCas(@Param("id") Long id, @Param("expectedRevId") Long expectedRevId,
                           @Param("revId") Long revId, @Param("revNum") String revNum,
                           @Param("updatedBy") Long updatedBy, @Param("updatedDt") Timestamp updatedDt);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from form")
    Long queryFormMaxId();
}
