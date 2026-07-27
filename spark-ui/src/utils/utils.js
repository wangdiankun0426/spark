// router/utils.js
import store from "@/store/index.js";

export function isAdmin() {
    const userInfo = store.getters['user/getUserInfo'];
    if (userInfo === undefined) {
        return false;
    }
    return userInfo.id === 101;
}