/**
 * 左侧菜单栏收起展开状态，各模块外壳共用同一份状态并持久化
 */
import { ref } from 'vue';
import { localStorageKey } from '@/utils/keyUtils.js';

// 宽度与 variables.scss 的 $sidebar-width / $sidebar-width-mini 保持同值
const railWidth = '82px';
const railWidthMini = '56px';

const railMini = ref(localStorage.getItem(localStorageKey.RAIL_MINI) === '1');

/**
 * 切换收起展开并持久化
 */
export function toggleRail() {
  railMini.value = !railMini.value;
  localStorage.setItem(localStorageKey.RAIL_MINI, railMini.value ? '1' : '0');
}

export { railMini, railWidth, railWidthMini };
