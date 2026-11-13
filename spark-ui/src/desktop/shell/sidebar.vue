<!--
  客户端专属侧边导航，菜单由后端菜单树驱动；
  区别只在形态：Web 是纯图标栏，客户端是带文字的常驻导航，底部承载账号卡
-->
<template>
  <aside class="dsk-sidebar">
    <nav class="dsk-sidebar__nav">
      <template v-for="entry in sidebarItems" :key="entry.key">
        <div v-if="entry.type === 'group'" class="dsk-sidebar__group">{{ entry.title }}</div>
        <div
            v-else
            class="dsk-sidebar__item"
            :class="{ 'is-active': activePath === entry.path }"
            @click="go(entry.path)"
        >
          <el-icon class="dsk-sidebar__icon">
            <component :is="entry.icon"/>
          </el-icon>
          <span class="dsk-sidebar__label">{{ entry.label }}</span>
        </div>
      </template>
    </nav>

    <account-card/>
  </aside>
</template>

<script setup>
import {computed} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useModuleMenus} from '@/utils/useModuleMenus.js';
import AccountCard from '../components/accountCard.vue';

const router = useRouter();
const route = useRoute();
const {sidebarItems} = useModuleMenus();

// 子页面归属到最长前缀匹配的菜单项
const activePath = computed(() => {
  const path = route.path;
  let matched = '';
  sidebarItems.value.forEach(entry => {
    if (entry.type !== 'item') {
      return;
    }
    if ((path === entry.path || path.startsWith(entry.path + '/')) && entry.path.length > matched.length) {
      matched = entry.path;
    }
  });
  return matched;
});

function go(path) {
  if (route.path === path) {
    return;
  }
  router.push(path).catch(() => {});
}
</script>

<style scoped lang="scss">
.dsk-sidebar {
  width: 180px;
  flex: none;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  background: $dsk-sidebar;
  border-right: 1px solid $dsk-line;
  overflow: hidden;
}

.dsk-sidebar__nav {
  flex: 1 1 auto;
  min-height: 0;
  padding: 12px 10px 6px;
  overflow-y: auto;
  overflow-x: hidden;
}

.dsk-sidebar__group {
  padding: 12px 10px 6px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: $dsk-group-text;
  user-select: none;
  pointer-events: none;

  &:first-child {
    padding-top: 2px;
  }
}

.dsk-sidebar__item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
  padding: 0 10px;
  margin-bottom: 2px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 650;
  color: $dsk-item-text;
  cursor: pointer;
  user-select: none;
  transition: background 0.16s ease, color 0.16s ease;

  &:hover {
    background: $dsk-item-hover;
    color: $dsk-text;
  }

  &.is-active {
    background: $dsk-item-active-bg;
    color: $dsk-item-active-text;

    .dsk-sidebar__icon {
      opacity: 1;
    }
  }
}

.dsk-sidebar__icon {
  flex: none;
  width: 19px;
  height: 19px;
  font-size: 19px;
  opacity: 0.9;
  transition: opacity 0.16s ease;
}

.dsk-sidebar__label {
  flex: 1 1 auto;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
