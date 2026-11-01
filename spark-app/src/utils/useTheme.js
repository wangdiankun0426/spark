import {computed} from 'vue';
import {useStore} from 'vuex';
import {applyStatusBarTheme, getThemeClass} from '@/utils/themeUtil';

/**
 * 页面根节点绑 themeClass 就能拿到当前皮肤的 CSS 变量
 * @returns {{theme, isDark, themeClass, toggleTheme, setTheme}}
 */
export function useTheme() {
  const store = useStore();
  const theme = computed(() => store.getters['theme/getTheme']);
  const isDark = computed(() => store.getters['theme/isDark']);
  const themeClass = computed(() => getThemeClass(theme.value));

  // 每个页面创建时校正一次状态栏图标颜色，切换主题后由 store 同步
  applyStatusBarTheme(theme.value);

  function toggleTheme() {
    store.dispatch('theme/toggleTheme');
  }

  function setTheme(value) {
    store.dispatch('theme/setTheme', value);
  }

  return {theme, isDark, themeClass, toggleTheme, setTheme};
}
