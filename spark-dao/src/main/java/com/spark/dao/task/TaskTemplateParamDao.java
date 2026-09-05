package com.spark.dao.task;

import com.spark.common.bean.task.entity.TaskTemplateParam;
import com.spark.common.bean.task.query.TaskTemplateParamQuery;
import com.spark.common.bean.task.result.TaskTemplateParamResult;
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
 * @since 2026-08-20 10:00:00
 * 任务模板参数 DAO
 */
public interface TaskTemplateParamDao extends BaseDao<TaskTemplateParam> {

    /**
     * 查询任务模板参数列表
     * @param query 查询参数
     * @return 参数列表
     */
    List<TaskTemplateParamResult> queryTaskTemplateParamList(TaskTemplateParamQuery query);

    /**
     * 根据模板id逻辑删除参数
     * @param templateId 模板id
     * @param userId 操作人id
     * @return 删除数量
     */
    int deleteByTemplateId(@Param("templateId") Long templateId, @Param("updatedBy") Long userId);
}
