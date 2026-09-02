package com.spark.llm.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.llm.entity.Skill;
import com.spark.bean.llm.query.SkillQuery;
import com.spark.bean.llm.result.SkillResult;
import com.spark.bean.llm.vo.SkillVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.dao.llm.SkillDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.StatusEnum;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.service.ISkillService;
import com.spark.manage.BaseService;
import com.spark.utils.BeanUtil;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class SkillServiceImpl extends BaseService<SkillQuery, SkillResult> implements ISkillService {
    private final static Logger logger = LoggerFactory.getLogger(SkillServiceImpl.class);
    @Autowired
    private SkillDao skillDao;
    @Autowired
    private AgentFactory agentFactory;

    /**
     * 创建技能
     * @param skillVO 技能数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createSkill(SkillVO skillVO) {
        ResultData<Void> result = new ResultData<>();
        if (skillVO == null || StringUtil.isBlank(skillVO.getName())
                || StringUtil.isBlank(skillVO.getDescription()) || StringUtil.isBlank(skillVO.getContent())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (skillDao.querySkillByName(skillVO.getName().trim()) != null) {
            result.setErrorCode(ErrorCodeEnum.SKILL_NAME_EXIST);
            return result;
        }
        Skill skill = new Skill();
        BeanUtil.copyProperties(skillVO, skill);
        if (skill.getStatus() == null) {
            skill.setStatus(StatusEnum.NORMAL.getValue());
        }
        int count = skillDao.insertDB(skill);
        if (count < 1) {
            logger.error("createSkill error, insert db fail");
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改技能
     * @param skillVO 技能数据
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateSkill(SkillVO skillVO) {
        ResultData<Void> result = new ResultData<>();
        if (skillVO == null || skillVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        SkillQuery query = new SkillQuery();
        query.setId(skillVO.getId());
        SkillResult existResult = skillDao.querySkill(query);
        if (existResult == null) {
            result.setErrorCode(ErrorCodeEnum.SKILL_NOT_EXIST);
            return result;
        }
        if (StringUtil.isNotBlank(skillVO.getName())) {
            SkillResult nameResult = skillDao.querySkillByName(skillVO.getName().trim());
            if (nameResult != null && !nameResult.getId().equals(skillVO.getId())) {
                result.setErrorCode(ErrorCodeEnum.SKILL_NAME_EXIST);
                return result;
            }
        }
        Skill skill = new Skill();
        BeanUtil.copyProperties(skillVO, skill);
        int count = skillDao.updateDBById(skill);
        if (count < 1) {
            logger.error("updateSkill error, update db fail");
            return result;
        }
        // 目录是装配期快照，技能变更后需重建绑定该技能的Agent缓存
        agentFactory.clearAgentsBySkill(skillVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除技能
     * @param skillVO 技能数据
     * @return 删除结果
     */
    @Override
    public ResultData<Void> deleteSkill(SkillVO skillVO) {
        ResultData<Void> result = new ResultData<>();
        if (skillVO == null || skillVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        SkillQuery query = new SkillQuery();
        query.setId(skillVO.getId());
        SkillResult existResult = skillDao.querySkill(query);
        if (existResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Skill skill = new Skill();
        skill.setId(skillVO.getId());
        int count = skillDao.deleteDBById(skill);
        if (count < 1) {
            logger.error("deleteSkill error, delete db fail");
            return result;
        }
        // 目录是装配期快照，技能变更后需重建绑定该技能的Agent缓存
        agentFactory.clearAgentsBySkill(skillVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询技能
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<SkillResult>> pageSkillList(SkillQuery query) {
        ResultData<PageResult<SkillResult>> result = new ResultData<>();
        if (query == null) {
            query = new SkillQuery();
        }
        PageResult<SkillResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询技能详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    public ResultData<SkillResult> querySkillDetail(SkillQuery query) {
        ResultData<SkillResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        SkillResult skillResult = skillDao.querySkill(query);
        if (skillResult == null) {
            result.setErrorCode(ErrorCodeEnum.SKILL_NOT_EXIST);
            return result;
        }
        skillResult.setStatusName(StatusEnum.indexOf(skillResult.getStatus()).getDesc());
        result.setData(skillResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<SkillResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        list.forEach(skillResult -> skillResult.setStatusName(StatusEnum.indexOf(skillResult.getStatus()).getDesc()));
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(SkillQuery query) {
        return skillDao.querySkillCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<SkillResult> queryList(SkillQuery query) {
        return skillDao.querySkillList(query);
    }
}
