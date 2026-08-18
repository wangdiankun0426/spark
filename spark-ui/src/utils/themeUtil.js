import {localStorageKey} from "@/utils/keyUtils.js";

/**
 * 主题切换工具
 * 浅色主题（light，白色）为默认；深色主题（dark，蓝色）
 * 通过在 html 根节点挂载 theme-light 类切换，仅影响导航栏与左侧菜单栏配色
 */
const LIGHT_THEME = 'light';

/**
 * 读取本地保存的主题
 * @returns {string} dark | light
 */
export function getTheme() {
  return localStorage.getItem(localStorageKey.THEME_KEY) === 'dark' ? 'dark' : LIGHT_THEME;
}

/**
 * 应用主题到页面根节点
 * @param theme dark | light
 */
export function applyTheme(theme) {
  document.documentElement.classList.toggle('theme-light', theme === LIGHT_THEME);
}

/**
 * 保存并应用主题
 * @param theme dark | light
 */
export function setTheme(theme) {
  localStorage.setItem(localStorageKey.THEME_KEY, theme);
  applyTheme(theme);
}
