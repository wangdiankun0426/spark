<template>
  <div>
    <el-container>
      <el-aside width="180px" class="llm-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            mode="vertical"
            class="llm-el-menu"
        >
          <el-menu-item index="/llm/agent">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;">
              <Agent/>
            </el-icon>
            <template #title>Agent</template>
          </el-menu-item>
          <el-menu-item index="/llm/workflow">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;">
              <Workflow/>
            </el-icon>
            <template #title>WorkFlow</template>
          </el-menu-item>
          <el-menu-item index="/llm/modelMarket">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;">
              <ModelMarket/>
            </el-icon>
            <template #title>模型市场</template>
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
import Agent from "@/assets/icons/agent.vue";
import ModelMarket from "@/assets/icons/modelMarket.vue";
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
.llm-el-aside {
  padding: 0;
  height: calc(100vh - $nav-height);
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  box-shadow: var(--sidebar-shadow);
  overflow-y: auto;
  overflow-x: hidden;
}
.llm-el-menu {
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
  padding-left: 30px !important;
}
/*悬停一级菜单之后*/
.el-menu-item:hover {
  background-color: var(--sidebar-hover-bg);
  padding-left: 28px !important;
}
/*点击一级菜单之后*/
.el-menu-item.is-active {
  background-color: var(--sidebar-active-bg);
  color: var(--sidebar-active-text);
  border-bottom: 0;
}
:deep(.el-menu-item .el-menu-tooltip__trigger) {
  padding: 0 8px !important;
}
</style>
