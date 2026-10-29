package com.spark.llm.tools;

import com.spark.common.bean.llm.result.SkillFileResult;
import com.spark.common.bean.llm.result.SkillResult;
import com.spark.dao.llm.SkillDao;
import com.spark.dao.llm.SkillFileDao;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
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
 * 技能读取工具
 */
@Component
public class SkillTool {
    private static final Logger logger = LoggerFactory.getLogger(SkillTool.class);
    @Autowired
    private SkillDao skillDao;
    @Autowired
    private SkillFileDao skillFileDao;
    private List<Long> skillIds;

    /**
     * 为Agent创建绑定技能的工具实例
     * @param skillIds 技能ID列表
     * @return 绑定skillIds的工具实例
     */
    public SkillTool forAgent(List<Long> skillIds) {
        SkillTool tool = new SkillTool();
        tool.skillDao = this.skillDao;
        tool.skillFileDao = this.skillFileDao;
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
        StringBuilder sb = new StringBuilder();
        sb.append("【技能【").append(skill.getName()).append("】指令全文】\n");
        sb.append(skill.getContent());
        // 追加资源文件清单，供模型按需调用read_skill_file读取
        List<SkillFileResult> fileCatalog = skillFileDao.queryFileCatalogBySkillId(skill.getId());
        if (CollectionUtil.isNotEmpty(fileCatalog)) {
            sb.append("\n【资源文件清单】本技能附带以下资源文件，当指令中提及或任务需要时，调用read_skill_file工具传入技能名称与文件路径读取对应文件全文：");
            for (SkillFileResult file : fileCatalog) {
                sb.append("\n- ").append(file.getFilePath()).append("：").append(file.getDescription());
            }
        }
        sb.append("\n【技能指令结束。请严格按以上指令执行并完成用户请求，除非指令明确要求，否则不要再次调用read_skill。】");
        return sb.toString();
    }

    /**
     * 按技能名称与文件路径读取技能文件全文
     * @param name 技能名称
     * @param path 文件路径
     * @return 技能文件全文或引导提示
     */
    @Tool(
        name = "read_skill_file",
        value = "【技能文件读取工具】参数name：技能名称，参数path：文件路径（均必须来自read_skill返回的资源文件清单）。当技能指令需要参考其资源文件时，调用本工具读取文件全文；资源文件清单之外的文件不要调用本工具。"
    )
    public String readSkillFile(String name, String path) {
        logger.info("readSkillFile name={}, path={}, skillIds={}", name, path, skillIds);
        if (StringUtil.isBlank(name) || StringUtil.isBlank(path)) {
            return "技能文件读取失败：技能名称或文件路径为空。请按资源文件清单中的技能名称与文件路径调用read_skill_file。";
        }
        if (CollectionUtil.isEmpty(skillIds)) {
            return "技能文件读取失败：当前智能体未配置技能。";
        }
        SkillResult skill = skillDao.queryEnabledSkillByName(skillIds, name.trim());
        if (skill == null) {
            return "未找到技能【" + name.trim() + "】或该技能已停用。请核对装配目录中的技能名称后重试。";
        }
        SkillFileResult file = skillFileDao.queryFileByPath(skill.getId(), path.trim());
        if (file == null || StringUtil.isBlank(file.getContent())) {
            return "未找到技能【" + name.trim() + "】下的文件【" + path.trim() + "】。请核对资源文件清单中的文件路径后重试。";
        }
        return "【技能【" + skill.getName() + "】文件【" + file.getFilePath() + "】全文】\n"
                + file.getContent()
                + "\n【文件结束。请将以上内容作为参考完成用户请求，除非确有必要，否则不要重复读取同一文件。】";
    }
}
