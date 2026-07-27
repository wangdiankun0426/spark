package com.spark.dao.form;

import com.spark.bean.form.entity.FormVersion;
import com.spark.bean.form.query.FormVersionQuery;
import com.spark.bean.form.result.FormVersionResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 13:50
 */
public interface FormVersionDao extends BaseDao<FormVersion> {

    /**
     * 插入数据
     * @param formVersion
     * @return
     */
    @Override
    int insert(FormVersion formVersion);

    /**
     * 查询单条
     * @param versionQuery
     * @return
     */
    FormVersionResult queryFormVersion(FormVersionQuery versionQuery);

    /**
     * 查询最大版本号
     * @param formId
     * @return
     */
    @Select("select IFNULL(max(rev_code), 0) from form_version where delete_flag = 1 and form_id = #{formId}")
    int queryMaxFormVersionCode(@Param("formId") Long formId);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryFormVersionCount(FormVersionQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FormVersionResult> queryFormVersionList(FormVersionQuery query);
}
