// utils/utils.js
import store from "@/store/index.js";

/**
 * 判断当前登录用户是否为管理员
 * 按角色类型判断
 * @returns {boolean}
 */
export function isAdmin() {
    const userInfo = store.getters['user/getUserInfo'];
    if (userInfo === undefined || userInfo.roleType === undefined || userInfo.roleType === null) {
        return false;
    }
    return (userInfo.roleType & 2) === 2 || (userInfo.roleType & 4) === 4;
}
