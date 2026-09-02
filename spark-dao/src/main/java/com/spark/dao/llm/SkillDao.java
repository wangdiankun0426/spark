package com.spark.dao.llm;

import com.spark.bean.llm.entity.Skill;
import com.spark.bean.llm.query.SkillQuery;
import com.spark.bean.llm.result.SkillResult;
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
 * @since 2026/09/02 09:00:00
 */
public interface SkillDao extends BaseDao<Skill> {

    /**
     * 插入数据
     * @param skill 数据对象
     * @return 影响行数
     */
    @Override
    int insert(Skill skill);

    /**
     * 删除数据
     * @param skill 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(Skill skill);

    /**
     * 修改数据
     * @param skill 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(Skill skill);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int querySkillCount(SkillQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<SkillResult> querySkillList(SkillQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    SkillResult querySkill(SkillQuery query);

    /**
     * 按名称精确查询技能（名称唯一性校验用）
     * @param name 技能名称
     * @return 技能数据
     */
    SkillResult querySkillByName(@Param("name") String name);

    /**
     * 查询启用技能列表（运行期按Agent绑定技能读取目录）
     * @param skillIds 技能ID列表
     * @return 技能列表
     */
    List<SkillResult> queryEnabledSkillList(@Param("skillIds") List<Long> skillIds);

    /**
     * 按名称查询启用技能（运行期read_skill读取全文）
     * @param skillIds 技能ID列表
     * @param name 技能名称
     * @return 技能数据
     */
    SkillResult queryEnabledSkillByName(@Param("skillIds") List<Long> skillIds, @Param("name") String name);
}
