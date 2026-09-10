// 菜单权限工具：按当前用户可访问菜单标识码做前端显隐
import store from "@/store/index.js";

/**
 * 模块
 */
export const MODULE_CODES = {
  LLM: 'module.llm',          // AI管理模块
  BUS: 'module.bus',          // 业务管理模块
}

/**
 * 菜单
 */
export const MENU_CODES = {
  // AI管理
  LLM_AGENT: 'menu.llm.agent',                // Agent
  LLM_WORKFLOW: 'menu.llm.workflow',          // WorkFlow
  LLM_MODEL_MARKET: 'menu.llm.modelMarket',   // 模型市场
  LLM_SKILL: 'menu.llm.skill',                // 技能库
  LLM_MCP: 'menu.llm.mcp',                    // MCP服务
  LLM_KNOWLEDGE: 'menu.llm.knowledge',          // 知识库
  LLM_GRAPH: 'menu.llm.graph',                  // 知识图谱

  // 业务管理
  BUS_FLOW: 'menu.bus.flow',              // 流程模板
};

/**
 * 当前用户是否有指定菜单权限
 * @param menuCode 菜单标识码
 * @returns {boolean}
 */
export function hasMenu(menuCode) {
  const userInfo = store.getters['user/getUserInfo'];
  const ownMenuCodes = userInfo && Array.isArray(userInfo.menuCodes) ? userInfo.menuCodes : [];
  return ownMenuCodes.map(String).includes(String(menuCode));
}
