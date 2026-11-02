// 小程序无法给 page 节点挂类名，主题靠页面根节点绑 themeClass、再由 theme.scss 的 CSS 变量逐层生效
// 小程序组件 prop 拿不到 CSS 变量，本文件是 prop 传实色的唯一出口，色值与 styles/theme.scss 同名变量保持一致，改色两处同改
import {localStorageKey} from '@/utils/keyUtils';

export const THEME_LIGHT = 'light';
export const THEME_DARK = 'dark';

// prop 实色逃生舱，key 与 theme.scss 的 --app-* 语义对应
const themeTokens = {
  light: {
    primary: '#004fc5',
    brandIcon: '#2f7bff',
    onFill: '#ffffff',
    info: '#0052cc',
    success: '#52c41a',
    warning: '#faad14',
    error: '#ff4d4f',
    textPrimary: '#172b4d',
    textSecondary: '#5e6c84',
    textTertiary: '#86909c',
    placeholder: '#a0aec0',
    chevron: '#4a5b74',
    searchIcon: '#9aa7ba',
    border: '#e4eaf4',
    cardBg: '#ffffff',
    searchBg: '#f5f7fa',
    headerBg: '#ffffff',
    pageBase: '#ffffff',
    gradientFill: 'linear-gradient(135deg, #2f7bff 0%, #5b94f5 100%)'
  },
  dark: {
    primary: '#4d8dff',
    brandIcon: '#8fb6ff',
    onFill: '#e9f1ff',
    info: '#6ea8ff',
    success: '#6fcf5b',
    warning: '#f5b544',
    error: '#ff8c8c',
    textPrimary: '#e5edf7',
    textSecondary: '#94a6c0',
    textTertiary: '#8b9cb5',
    placeholder: '#8595ad',
    chevron: '#c0c8d4',
    searchIcon: '#8595ad',
    border: '#2c3b53',
    cardBg: '#1a2537',
    searchBg: '#243044',
    headerBg: '#131d2e',
    pageBase: '#0f172a',
    gradientFill: 'linear-gradient(135deg, #2f63bd 0%, #244d97 100%)'
  }
};

/**
 * 按语义取当前皮肤实色
 * @param name token 名
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getThemeToken(name, isDark) {
  const group = themeTokens[isDark ? THEME_DARK : THEME_LIGHT];
  return group[name];
}

/**
 * 品牌主色，底部导航选中态使用
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getPrimaryColor(isDark) {
  return getThemeToken('primary', isDark);
}

/**
 * 头部返回与操作图标、列表卡片品牌图标实色，暗色下提亮一档
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getHeaderIconColor(isDark) {
  return getThemeToken('brandIcon', isDark);
}

/**
 * 品牌渐变填充上的文字与图标实色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getOnFillColor(isDark) {
  return getThemeToken('onFill', isDark);
}

/**
 * 信息蓝，文字链接、信息色图标
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getInfoColor(isDark) {
  return getThemeToken('info', isDark);
}

/**
 * 警示色实色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getWarningColor(isDark) {
  return getThemeToken('warning', isDark);
}

/**
 * 主要文字色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getTextPrimaryColor(isDark) {
  return getThemeToken('textPrimary', isDark);
}

/**
 * 次级文字色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getSecondaryColor(isDark) {
  return getThemeToken('textSecondary', isDark);
}

/**
 * 分割线实色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getBorderColor(isDark) {
  return getThemeToken('border', isDark);
}

/**
 * 三级弱文字色，未选中导航、辅助箭头
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getTertiaryColor(isDark) {
  return getThemeToken('textTertiary', isDark);
}

/**
 * 行尾箭头实色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getChevronColor(isDark) {
  return getThemeToken('chevron', isDark);
}

/**
 * 搜索框内图标实色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getSearchIconColor(isDark) {
  return getThemeToken('searchIcon', isDark);
}

/**
 * 占位文字色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getPlaceholderColor(isDark) {
  return getThemeToken('placeholder', isDark);
}

/**
 * 弹层底色
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getCardBgColor(isDark) {
  return getThemeToken('cardBg', isDark);
}

/**
 * 品牌渐变填充，组件仅收实色的渐变类 prop 使用，普通样式一律写 var(--app-gradient-fill)
 * @param isDark 是否暗色
 * @returns {string} 渐变
 */
export function getGradientFill(isDark) {
  return getThemeToken('gradientFill', isDark);
}

/**
 * 搜索框底色，选人/选会话弹层共用，组件的内联样式拿不到 CSS 变量
 * @param isDark 是否暗色
 * @returns {string} 色值
 */
export function getSearchBgColor(isDark) {
  return getThemeToken('searchBg', isDark);
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
    backgroundColor: dark ? themeTokens.dark.headerBg : themeTokens.light.headerBg,
    fail: () => {}
  });
  // page 节点的背景色由 wxss 固定，小程序端只能靠这个接口改窗口与下拉区域，避免暗色下露白
  uni.setBackgroundColor({
    backgroundColor: dark ? themeTokens.dark.pageBase : themeTokens.light.pageBase,
    backgroundColorTop: dark ? themeTokens.dark.pageBase : themeTokens.light.pageBase,
    backgroundColorBottom: dark ? themeTokens.dark.pageBase : themeTokens.light.pageBase,
    fail: () => {}
  });
  // #endif
}
