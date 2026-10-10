package com.spark.dao.llm;

import com.spark.common.bean.llm.entity.SkillFile;
import com.spark.common.bean.llm.result.SkillFileResult;
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
 * @since 2026/10/10 10:00:00
 */
public interface SkillFileDao extends BaseDao<SkillFile> {

    /**
     * 插入数据
     * @param skillFile 数据对象
     * @return 影响行数
     */
    @Override
    int insert(SkillFile skillFile);

    /**
     * 删除数据
     * @param skillFile 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(SkillFile skillFile);

    /**
     * 修改数据
     * @param skillFile 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(SkillFile skillFile);

    /**
     * 查询技能文件清单
     * @param skillId 技能id
     * @return 文件清单
     */
    List<SkillFileResult> queryFileCatalogBySkillId(@Param("skillId") Long skillId);

    /**
     * 查询技能文件列表
     * @param skillId 技能id
     * @return 文件列表
     */
    List<SkillFileResult> queryFileListBySkillId(@Param("skillId") Long skillId);

    /**
     * 按路径查询技能文件
     * @param skillId 技能id
     * @param filePath 文件路径
     * @return 文件数据
     */
    SkillFileResult queryFileByPath(@Param("skillId") Long skillId, @Param("filePath") String filePath);

    /**
     * 按技能逻辑删除全部文件
     * @param skillId 技能id
     * @param updatedBy 修改人id
     * @return 影响行数
     */
    int deleteBySkillId(@Param("skillId") Long skillId, @Param("updatedBy") Long updatedBy);
}
