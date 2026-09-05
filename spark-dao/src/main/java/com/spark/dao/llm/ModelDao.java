package com.spark.dao.llm;

import com.spark.common.bean.llm.entity.Model;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.ModelResult;
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
 * @since 2026-05-14 09:45:43
 */
public interface ModelDao extends BaseDao<Model> {

    /**
     * 插入数据
     * @param model 数据对象
     * @return 影响行数
     */
    @Override
    int insert(Model model);

    /**
     * 删除数据
     * @param model 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(Model model);

    /**
     * 修改数据
     * @param model 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(Model model);

    /**
     * 查询数量
     * @param query 查询条件
     * @return  数量
     */
    int queryModelCount(ModelQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<ModelResult> queryModelList(ModelQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    ModelResult queryModel(ModelQuery query);

    /**
     * 查询模型最大id
     * @return
     */
    @Select("select max(id) from llm_model")
    Long queryModelMaxId();
}
