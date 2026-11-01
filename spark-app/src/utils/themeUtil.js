// 小程序无法给 page 节点挂类名，主题靠页面根节点绑 themeClass、再由 theme.scss 的 CSS 变量逐层生效
import {localStorageKey} from '@/utils/keyUtils';

export const THEME_LIGHT = 'light';
export const THEME_DARK = 'dark';

/**
 * 搜索框底色，选人/选会话弹层共用，组件的内联样式拿不到 CSS 变量
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getSearchBgColor(isDark) {
  return isDark ? '#243044' : '#f5f7fa';
}

/**
 * 未设置或读取异常时按亮色处理
 * @returns {string} light | dark
 */
export function getStoredTheme() {
  try {
    return uni.getStorageSync(localStorageKey.THEME) === THEME_DARK ? THEME_DARK : THEME_LIGHT;
  } catch (e) {
    console.error('getStoredTheme error', e);
    return THEME_LIGHT;
  }
}

export function setStoredTheme(theme) {
  try {
    uni.setStorageSync(localStorageKey.THEME, theme);
  } catch (e) {
    console.error('setStoredTheme error', e);
  }
}

/**
 * 页面根节点绑定的主题类名
 * @returns {string} theme-light | theme-dark
 */
export function getThemeClass(theme) {
  return 'theme-' + (theme === THEME_DARK ? THEME_DARK : THEME_LIGHT);
}

/**
 * H5 端把主题类名同步到 html 根节点，管的是页面根节点之外的底色，小程序端没有这个节点
 * @param theme light | dark
 */
export function applyDocumentTheme(theme) {
  // #ifdef H5
  const dark = theme === THEME_DARK;
  document.documentElement.classList.toggle(THEME_DARK, dark);
  document.documentElement.classList.toggle(THEME_LIGHT, !dark);
  // #endif
  applyStatusBarTheme(theme);
}

/**
 * 小程序端同步状态栏图标颜色
 * 各页面用的是自定义导航，状态栏压在自己的头部上，暗色头部必须配白色图标才看得见
 * @param theme light | dark
 */
export function applyStatusBarTheme(theme) {
  // #ifdef MP-WEIXIN
  const dark = theme === THEME_DARK;
  uni.setNavigationBarColor({
    frontColor: dark ? '#ffffff' : '#000000',
    backgroundColor: dark ? '#131d2e' : '#ffffff',
    fail: () => {}
  });
  // page 节点的背景色由 wxss 固定，小程序端只能靠这个接口改窗口与下拉区域，避免暗色下露白
  uni.setBackgroundColor({
    backgroundColor: dark ? '#0f172a' : '#ffffff',
    backgroundColorTop: dark ? '#0f172a' : '#ffffff',
    backgroundColorBottom: dark ? '#0f172a' : '#ffffff',
    fail: () => {}
  });
  // #endif
}
