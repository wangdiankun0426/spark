/**
 * 用户模块 - 管理用户登录状态和信息
 */
import {loginAPI} from "@/api/manage/auth/login.js";
import {localStorageKey} from "@/utils/keyUtils.js";

/**
 * 存储应用的状态数据，相当于组件中的 data。
 * 单一状态树，整个应用只有一个 store
 * 响应式的，当状态改变时，相关的组件会自动更新
 * 所有组件共享同一份状态
 */
const state = {
  userInfo: (() => {
    const userInfoStr = localStorage.getItem(localStorageKey.USERINFO);
    if (userInfoStr) {
      try {
        return JSON.parse(userInfoStr);
      } catch (e) {
        console.error('解析用户信息失败:', e);
        return null;
      }
    }
    return null;
  })(),
  token: localStorage.getItem(localStorageKey.TOKEN) || '',
}

/**
 * 突变 相当于组件中的methods 唯一可以修改 state 的地方，必须是同步函数。
 * 直接修改 state
 * 必须是同步操作
 * 通过 commit 触发
 * 接收 state 作为第一个参数
 */
const mutations = {
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo;
    localStorage.setItem(localStorageKey.USERINFO, JSON.stringify(userInfo));
  },

  SET_TOKEN(state, token) {
    state.token = token;
    localStorage.setItem(localStorageKey.TOKEN, token);
  },

  CLEAR_USER(state) {
    state.userInfo = null;
    state.token = '';
    localStorage.removeItem(localStorageKey.TOKEN);
    localStorage.removeItem(localStorageKey.USERINFO);
  }
}

/**
 * 动作
 * 可以包含异步操作（如 API 调用）
 * 通过 dispatch 触发
 * 接收 context 对象作为第一个参数
 * 通过 context.commit 提交 mutation
 */
const actions = {
  // 登录
  login({ commit }, { loginForm }) {
    return new Promise((resolve, reject) => {
      loginAPI(loginForm).then(res => {
        if (res.code === 200) {
          commit('SET_TOKEN', res.data.sessionId);
        }
        resolve(res)
      }).catch(err => {
        reject(err)
      })
     })
  },
  // 设置用户信息
  setUserInfo({ commit }, { userInfo }) {
    commit('SET_USER_INFO', userInfo);
  },
  // 登出
  logout({ commit }) {
    commit('CLEAR_USER');
  }
}

/**
 * 获取器 从 state 中派生出一些状态，相当于 store 的计算属性
 * 可以理解为 store 的计算属性
 * 会被缓存，只有依赖的状态发生变化时才会重新计算
 * 接收 state 作为第一个参数
 * 可以接收其他 getters 作为第二个参数
 */
const getters = {
  getUserInfo: state => state.userInfo,
  getToken: state => state.token,
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
