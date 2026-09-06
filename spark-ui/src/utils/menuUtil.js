// 菜单权限工具：按当前用户可访问菜单做前端显隐
import store from "@/store/index.js";

/**
 * 当前用户是否有指定菜单权限
 * @param menuId 菜单id
 * @returns {boolean}
 */
export function hasMenu(menuId) {
  const userInfo = store.getters['user/getUserInfo'];
  const ownMenuIds = userInfo && Array.isArray(userInfo.menuIds) ? userInfo.menuIds : [];
  return ownMenuIds.includes(menuId);
}
