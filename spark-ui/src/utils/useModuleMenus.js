/**
 * 模块侧栏菜单 - 按当前路由所属模块从菜单树派生分组侧栏项
 */
import {computed} from 'vue';
import {useRoute} from 'vue-router';
import {useStore} from 'vuex';
import {resolveIcon} from '@/utils/iconRegistry.js';

/**
 * 派生当前模块的侧栏菜单项，分组目录保留标题、叶子菜单解析图标，隐藏项跳过
 * @returns {{sidebarItems: import('vue').ComputedRef<Array>}}
 */
export function useModuleMenus() {
  const route = useRoute();
  const store = useStore();
  const sidebarItems = computed(() => {
    const menuTree = store.getters['permission/menuTree'] || [];
    const segment = route.path.split('/')[1];
    const modulePath = segment ? '/' + segment : '';
    const mod = menuTree.find(node => node.path === modulePath);
    if (!mod || !mod.children) {
      return [];
    }
    const items = [];
    flattenSidebar(mod.children, items);
    return items;
  });
  return {sidebarItems};
}

/**
 * 递归拍平菜单为侧栏项，无路径目录转为分组标题，空分组与隐藏节点跳过
 * @param nodes 菜单节点数组
 * @param out 收集结果数组
 */
function flattenSidebar(nodes, out) {
  nodes.forEach(node => {
    if (node.hidden) {
      return;
    }
    if (!node.path && node.children && node.children.length) {
      const groupItems = [];
      flattenSidebar(node.children, groupItems);
      if (groupItems.length) {
        out.push({type: 'group', key: node.code, title: node.title});
        out.push(...groupItems);
      }
      return;
    }
    if (node.path) {
      out.push({type: 'item', key: node.code, path: node.path, label: node.title, icon: resolveIcon(node.icon)});
    }
  });
}