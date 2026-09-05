package com.spark.dao;

import com.spark.common.bean.base.BaseEntity;
import com.spark.common.bean.base.SessionHolder;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/25 16:33
 */
public interface BaseDao<T extends BaseEntity> {

    /**
     * 插入数据
     * @param t
     * @return
     */
    default int insertDB(T t) {
        if (t == null) {
            return 0;
        }
        if (t.getCreatedBy() == null) {
            t.setCreatedBy(SessionHolder.getCurrentUserId());
        }
        if (t.getDeptId() == null) {
            t.setDeptId(SessionHolder.getCurrentDeptId());
        }
        if (t.getUpdatedBy() == null) {
            t.setUpdatedBy(SessionHolder.getCurrentUserId());
        }
        if (t.getCreatedDt() == null) {
            t.setCreatedDt(new Timestamp(System.currentTimeMillis()));
        }
        if (t.getUpdatedDt() == null) {
            t.setUpdatedDt(new Timestamp(System.currentTimeMillis()));
        }
        int count = insert(t);
        return count;
    }

    /**
     * 修改数据
     * @param t
     * @return
     */
    default int updateDBById(T t) {
        if (t == null) {
            return 0;
        }
        if (t.getId() == null) {
            return -1;
        }
        if(t.getUpdatedBy() == null) {
            t.setUpdatedBy(SessionHolder.getCurrentUserId());
        }
        if(t.getUpdatedDt() == null) {
            t.setUpdatedDt(new Timestamp(System.currentTimeMillis()));
        }
        int count = updateById(t);
        return count;
    }

    /**
     * 删除数据
     * @param t
     * @return
     */
    default int deleteDBById(T t) {
        if(t == null) {
            return 0;
        }
        if (t.getId() == null) {
            return -1;
        }
        if(t.getUpdatedBy() == null) {
            t.setUpdatedBy(SessionHolder.getCurrentUserId());
        }
        if(t.getUpdatedDt() == null) {
            t.setUpdatedDt(new Timestamp(System.currentTimeMillis()));
        }
        int count = deleteById(t);
        return count;
    }

    /**
     * 插入接口接口
     * @param var
     * @return
     */
    int insert(T var);

    /**
     * 根据id修改数据接口
     * @param var
     * @return
     */
    int updateById(T var);

    /**
     * 根据id删除数据接口
     * @param var
     * @return
     */
    int deleteById(T var);
}
