<template>
  <div>
    <el-container>
      <el-aside width="64px" class="flow-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :collapse="true"
            mode="vertical"
            class="flow-el-menu"
        >
          <el-menu-item v-if="hasMenu(MENU_CODES.BUS_FLOW)" index="/bus/flowTemplate">
            <el-icon style="font-size: 24px;">
              <flow-application/>
            </el-icon>
            <template #title>流程模板</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="affix-container">
        <!-- 在模板中使用 <router-view> 来显示组件 -->
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>

import {computed} from "vue";
import {useRoute} from "vue-router";
import FlowApplication from "@/assets/icons/flowApplication.vue";
import {hasMenu, MENU_CODES} from "@/utils/menuUtil.js";

const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>

<style scoped lang="scss">
.flow-el-aside {
  padding: 0;
  height: calc(100vh - $nav-height);
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  box-shadow: var(--sidebar-shadow);
  overflow-y: auto;
  overflow-x: hidden;
}
.flow-el-menu {
  border: 0;
  padding: 0;
  margin: 0;
  background: transparent !important;
}
.el-menu-item {
  color: var(--sidebar-text);
  height: 44px;
  line-height: 44px;
  font-size: 13px;
  font-weight: 400;
  justify-content: center;
}
:deep(.el-menu-item) {
  padding: 0 !important;
}
/*悬停一级菜单之后*/
.el-menu-item:hover {
  background-color: var(--sidebar-hover-bg);
}
/*点击一级菜单之后*/
.el-menu-item.is-active {
  background-color: var(--sidebar-active-bg);
  color: var(--sidebar-active-text);
  border-bottom: 0;
}
:deep(.el-menu-item .el-menu-tooltip__trigger) {
  padding: 0 !important;
  justify-content: center;
}
</style>
