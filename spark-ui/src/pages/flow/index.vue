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
          <el-menu-item v-if="hasMenu(501)" index="/flow/application">
            <el-icon style="font-size: 24px;">
              <flow-application/>
            </el-icon>
            <template #title>流程申请</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(502)" index="/flow/myApplication">
            <el-icon style="font-size: 24px;">
              <my-application/>
            </el-icon>
            <template #title>我的申请</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(503)" index="/flow/myTodo">
            <el-icon style="font-size: 24px;">
              <my-todo/>
            </el-icon>
            <template #title>我的待办</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(504)" index="/flow/myDone">
            <el-icon style="font-size: 24px;">
              <my-done/>
            </el-icon>
            <template #title>我的已办</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(505)" index="/flow/copyMe">
            <el-icon style="font-size: 24px;">
              <copy-me/>
            </el-icon>
            <template #title>抄送给我</template>
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

<script setup name="manage">

import {computed} from "vue";
import {useRoute} from "vue-router";
import FlowApplication from "@/assets/icons/flowApplication.vue";
import MyTodo from "@/assets/icons/myTodo.vue";
import MyDone from "@/assets/icons/myDone.vue";
import CopyMe from "@/assets/icons/copyMe.vue";
import {hasMenu} from "@/utils/menuUtil.js";
import MyApplication from "@/assets/icons/myApplication.vue";

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
