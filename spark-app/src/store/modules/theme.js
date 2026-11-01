// 亮色为默认值，切换即写本地缓存并同步 H5 根节点
import {THEME_DARK, THEME_LIGHT, getStoredTheme, setStoredTheme, applyDocumentTheme} from '@/utils/themeUtil';

const state = {
  theme: getStoredTheme()
};

const mutations = {
  SET_THEME(state, theme) {
    state.theme = theme === THEME_DARK ? THEME_DARK : THEME_LIGHT;
    setStoredTheme(state.theme);
    applyDocumentTheme(state.theme);
  }
};

const actions = {
  toggleTheme({commit, state}) {
    commit('SET_THEME', state.theme === THEME_DARK ? THEME_LIGHT : THEME_DARK);
  },
  setTheme({commit}, theme) {
    commit('SET_THEME', theme);
  },
  // state 初始化已读过缓存，这里补一次是为了把主题应用出去
  initTheme({commit, state}) {
    commit('SET_THEME', state.theme);
  }
};

const getters = {
  getTheme: state => state.theme,
  isDark: state => state.theme === THEME_DARK
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
};
