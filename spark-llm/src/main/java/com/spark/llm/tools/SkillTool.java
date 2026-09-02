package com.spark.llm.tools;

import com.spark.bean.llm.result.SkillResult;
import com.spark.dao.llm.SkillDao;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/09/02 09:00:00
 * 技能读取工具（按Agent绑定的技能读取全文）
 */
@Component
public class SkillTool {
    private static final Logger logger = LoggerFactory.getLogger(SkillTool.class);
    @Autowired
    private SkillDao skillDao;
    private List<Long> skillIds;

    /**
     * 为Agent创建绑定技能的工具实例
     * @param skillIds 技能ID列表
     * @return 绑定skillIds的工具实例
     */
    public SkillTool forAgent(List<Long> skillIds) {
        SkillTool tool = new SkillTool();
        tool.skillDao = this.skillDao;
        tool.skillIds = skillIds;
        return tool;
    }

    /**
     * 按技能名称读取技能指令全文
     * @param name 技能名称
     * @return 技能指令全文或引导提示
     */
    @Tool(
        name = "read_skill",
        value = "【技能读取工具】参数name：技能名称（必须是已装配技能目录中列出的名称）。当用户请求匹配目录中某项技能时，调用本工具读取技能全文，之后必须严格按技能指令执行；若没有技能与当前问题匹配，不要调用本工具。"
    )
    public String readSkill(String name) {
        logger.info("readSkill name={}, skillIds={}", name, skillIds);
        if (StringUtil.isBlank(name)) {
            return "技能读取失败：技能名称为空。请按装配目录中的名称调用read_skill。";
        }
        if (CollectionUtil.isEmpty(skillIds)) {
            return "技能读取失败：当前智能体未配置技能。";
        }
        SkillResult skill = skillDao.queryEnabledSkillByName(skillIds, name.trim());
        if (skill == null || StringUtil.isBlank(skill.getContent())) {
            return "未找到技能【" + name.trim() + "】或该技能已停用。请核对装配目录中的技能名称后重试，或直接回答用户。";
        }
        return "【技能【" + skill.getName() + "】指令全文】\n"
                + skill.getContent()
                + "\n【技能指令结束。请严格按以上指令执行并完成用户请求，除非指令明确要求，否则不要再次调用read_skill。】";
    }
}
