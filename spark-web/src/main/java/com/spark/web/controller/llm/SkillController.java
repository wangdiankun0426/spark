package com.spark.web.controller.llm;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.llm.query.SkillQuery;
import com.spark.bean.llm.result.SkillResult;
import com.spark.bean.llm.vo.SkillVO;
import com.spark.llm.service.ISkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/09/02 09:00:00
 */
@RestController
@RequestMapping("llm/skill")
public class SkillController {

    @Autowired
    private ISkillService skillService;

    /**
     * 创建技能
     * @param skillVO 创建参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createSkill(SkillVO skillVO) {
        return skillService.createSkill(skillVO);
    }

    /**
     * 修改技能
     * @param skillVO 修改参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateSkill(SkillVO skillVO) {
        return skillService.updateSkill(skillVO);
    }

    /**
     * 删除技能
     * @param skillVO 删除参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteSkill(SkillVO skillVO) {
        return skillService.deleteSkill(skillVO);
    }

    /**
     * 分页查询技能
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<SkillResult>> pageSkillList(SkillQuery query) {
        return skillService.pageSkillList(query);
    }

    /**
     * 查询技能详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<SkillResult> querySkillDetail(SkillQuery query) {
        return skillService.querySkillDetail(query);
    }

}
