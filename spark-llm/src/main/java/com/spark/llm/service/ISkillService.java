package com.spark.llm.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.llm.query.SkillQuery;
import com.spark.bean.llm.result.SkillResult;
import com.spark.bean.llm.vo.SkillVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/09/02 09:00:00
 */
public interface ISkillService {

    /**
     * 创建技能
     * @param skillVO 技能数据
     * @return 创建结果
     */
    ResultData<Void> createSkill(SkillVO skillVO);

    /**
     * 修改技能
     * @param skillVO 技能数据
     * @return 修改结果
     */
    ResultData<Void> updateSkill(SkillVO skillVO);

    /**
     * 删除技能
     * @param skillVO 技能数据
     * @return 删除结果
     */
    ResultData<Void> deleteSkill(SkillVO skillVO);

    /**
     * 分页查询技能
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<SkillResult>> pageSkillList(SkillQuery query);

    /**
     * 查询技能详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<SkillResult> querySkillDetail(SkillQuery query);

}
