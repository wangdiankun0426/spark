// utils/utils.js
import store from "@/store/index.js";

/**
 * 判断当前登录用户是否为组织管理员
 * 按角色类型判断
 * @returns {boolean}
 */
export function isOrgAdmin() {
    const userInfo = store.getters['user/getUserInfo'];
    if (userInfo === undefined || userInfo === null || userInfo.roleType === undefined || userInfo.roleType === null) {
        return false;
    }
    return (userInfo.roleType & 2) === 2;
}

/**
 * 判断当前登录用户是否为系统管理员
 * 按角色类型判断
 * @returns {boolean}
 */
export function isSysAdmin() {
    const userInfo = store.getters['user/getUserInfo'];
    if (userInfo === undefined || userInfo === null || userInfo.accountType === undefined || userInfo.accountType === null) {
        return false;
    }
    return userInfo.accountType === 2;
}
