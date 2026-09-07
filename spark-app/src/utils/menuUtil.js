import store from "@/store";
import {getSessionAPI} from "@/api/auth/login.js";

/**
 * 菜单权限标识：
 */
export const MENU_IDS = {
  AI_APP: 20,     // AI应用
  FLOW: 50,       // 流程中心
  CONTACTS: 103,  // 通讯录
  MESSAGE: 104,   // 消息
  MY: 105,        // 个人中心

  // AI应用页签
  AI_AGENT: 201,        // Agent
  AI_WORKFLOW: 202,     // WorkFlow
  AI_MODEL_MARKET: 203, // 模型市场

  // 流程中心页签
  FLOW_MY_APPLY: 502,   // 我的申请
  FLOW_MY_TODO: 503,    // 我的待办
  FLOW_MY_DONE: 504,    // 我的已办
  FLOW_COPY_TO_ME: 505, // 抄送给我
};

let sessionPromise = null;

/**
 * 判断当前用户是否具备指定菜单权限
 * @param menuId 菜单id
 * @returns {boolean}
 */
export function hasMenu(menuId) {
  const userInfo = store.getters['user/getUserInfo'];
  const menuIds = userInfo && Array.isArray(userInfo.menuIds) ? userInfo.menuIds : [];
  return menuIds.indexOf(menuId) !== -1;
}

/**
 * 计算当前用户可见的底部导航项（首页常驻，其余按菜单权限过滤）
 * @returns {Array}
 */
export function visibleTabs() {
  // 底部导航tab项
  const TAB_ITEMS = [
    {name: 'home', icon: 'home-fill', text: '首页', always: true},
    {name: 'flow', icon: 'order', text: '流程', menuId: MENU_IDS.FLOW},
    {name: 'llm', icon: 'grid-fill', text: 'AI+', menuId: MENU_IDS.AI_APP},
    {name: 'message', icon: 'chat-fill', text: '消息', menuId: MENU_IDS.MESSAGE},
    {name: 'my', icon: 'account', text: '我的', menuId: MENU_IDS.MY},
  ];
  return TAB_ITEMS.filter(item => hasMenu(item.menuId) || item.always);
}

/**
 * 确保当前用户会话（含 menuIds）已加载：未加载则拉取 /auth/session 并写入 store
 * @returns {Promise}
 */
export function ensureMenusLoaded() {
  const userInfo = store.getters['user/getUserInfo'];
  if (userInfo && Array.isArray(userInfo.menuIds)) {
    return Promise.resolve();
  }
  if (!sessionPromise) {
    sessionPromise = getSessionAPI().then(res => {
      if (res.code === 200) {
        store.dispatch('user/setUserInfo', {userInfo: res.data});
      }
    }).finally(() => {
      sessionPromise = null;
    });
  }
  return sessionPromise;
}

/**
 * 页面级菜单权限校验：不具备时提示无权限并跳回首页
 * @param menuId 菜单id
 * @returns {Promise<boolean>} 是否放行
 */
export async function checkMenuAccess(menuId) {
  await ensureMenusLoaded();
  if (hasMenu(menuId)) {
    return true;
  }
  uni.reLaunch({url: '/pages/error/noPermission'});
  return false;
}
