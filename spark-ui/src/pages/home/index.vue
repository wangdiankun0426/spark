<template>
  <div>
    <el-container>
      <el-aside width="64px" class="home-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :collapse="true"
            mode="vertical"
            class="home-el-menu"
        >
          <div class="home-menu-group">AI</div>
          <el-menu-item index="/home/aichat">
            <el-icon style="font-size: 24px;">
              <Aichat/>
            </el-icon>
            <template #title>AIChat</template>
          </el-menu-item>
          <el-menu-item index="/home/agent">
            <el-icon style="font-size: 24px;">
              <Agent/>
            </el-icon>
            <template #title>Agent</template>
          </el-menu-item>
          <el-menu-item index="/home/workflow">
            <el-icon style="font-size: 24px;">
              <Workflow/>
            </el-icon>
            <template #title>WorkFlow</template>
          </el-menu-item>
          <div class="home-menu-group">流程</div>
          <el-menu-item index="/home/flow/application">
            <el-icon style="font-size: 24px;">
              <FlowApplication/>
            </el-icon>
            <template #title>流程申请</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/myApplication">
            <el-icon style="font-size: 24px;">
              <MyApplication/>
            </el-icon>
            <template #title>我的申请</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/myTodo">
            <el-icon style="font-size: 24px;">
              <MyTodo/>
            </el-icon>
            <template #title>我的待办</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/myDone">
            <el-icon style="font-size: 24px;">
              <MyDone/>
            </el-icon>
            <template #title>我的已办</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/copyMe">
            <el-icon style="font-size: 24px;">
              <CopyMe/>
            </el-icon>
            <template #title>抄送给我</template>
          </el-menu-item>
          <div class="home-menu-group">通用</div>
          <el-menu-item index="/home/contact">
            <el-icon style="font-size: 24px;">
              <Chat/>
            </el-icon>
            <template #title>通讯录</template>
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
import Agent from "@/assets/icons/agent.vue";
import FlowApplication from "@/assets/icons/flowApplication.vue";
import MyApplication from "@/assets/icons/myApplication.vue";
import MyTodo from "@/assets/icons/myTodo.vue";
import MyDone from "@/assets/icons/myDone.vue";
import CopyMe from "@/assets/icons/copyMe.vue";
import Chat from "@/assets/icons/chat.vue";
import Flow from "@/assets/icons/flow.vue";
import Aichat from "@/assets/icons/aichat.vue";
import Workflow from "@/assets/icons/workflow.vue";
const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>

<style lang="scss" scoped>
.home-el-aside {
  padding: 0;
  height: calc(100vh - $nav-height);
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  box-shadow: var(--sidebar-shadow);
  overflow-y: auto;
  overflow-x: hidden;
}
.home-el-menu {
  border: 0;
  padding: 0;
  margin: 0;
  background: transparent !important;
}

.home-menu-group {
  height: 30px;
  line-height: 28px;
  margin-top: $spacing-xs;
  font-size: 11px;
  letter-spacing: 2px;
  text-align: center;
  color: var(--sidebar-menu-group-text);
  user-select: none;
  pointer-events: none;
}

.home-el-menu :deep(.el-menu-item),
.home-el-menu :deep(.el-sub-menu__title) {
  color: var(--sidebar-text);
  height: 44px;
  line-height: 44px;
  font-size: 13px;
  font-weight: 400;
  justify-content: center;
  padding: 0 !important;
}
/*悬停菜单之后*/
.home-el-menu :deep(.el-menu-item:hover),
.home-el-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--sidebar-hover-bg);
}
/*点击菜单之后*/
.home-el-menu :deep(.el-menu-item.is-active),
.home-el-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background-color: var(--sidebar-active-bg);
  color: var(--sidebar-active-text);
  border-bottom: 0;
}
.home-el-menu :deep(.el-menu-item .el-menu-tooltip__trigger),
.home-el-menu :deep(.el-sub-menu__title .el-menu-tooltip__trigger) {
  padding: 0 !important;
  justify-content: center;
}
/* 折叠态弹出层：Element Plus 会将其 teleport 到 body，scoped 选择器命中不到，
   故按 popper-class 写成受控的全局样式，类名仅本菜单使用。
   弹层收窄为 64px 图标条，与左侧栏一级菜单视觉一致，名称由 tooltip 呈现 */
:global(.home-flow-popup) {
  border: none;
  background: var(--sidebar-bg);
}
:global(.home-flow-popup .el-menu--popup) {
  min-width: 64px;
  padding: $spacing-xs 0;
  background: var(--sidebar-bg);
}
:global(.home-flow-popup .el-menu-item) {
  height: 44px;
  line-height: 44px;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--sidebar-text);
}
:global(.home-flow-popup .el-menu-item:hover) {
  background-color: var(--sidebar-hover-bg);
}
:global(.home-flow-popup .el-menu-item.is-active) {
  background-color: var(--sidebar-active-bg);
  color: var(--sidebar-active-text);
}
</style>
