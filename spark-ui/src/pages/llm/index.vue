<template>
  <div>
    <el-container>
      <el-aside width="140px" class="llm-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            mode="vertical"
            class="llm-el-menu"
        >
          <el-menu-item index="/llm/agent">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;">
              <Agent/>
            </el-icon>
            <template #title>Agent</template>
          </el-menu-item>
          <el-menu-item index="/llm/workflow">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;">
              <Workflow/>
            </el-icon>
            <template #title>WorkFlow</template>
          </el-menu-item>
          <el-menu-item index="/llm/modelMarket">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;">
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
  background: $bg-sidebar;
  color: #ffffff;
  box-shadow: 2px 0 8px rgba(23, 43, 77, 0.12);
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
  color: #ffffff;
  height: 44px;
  line-height: 44px;
  font-size: 13px;
  font-weight: 550;
}
/*悬停一级菜单之后*/
.el-menu-item:hover {
  background-color: #4b7aca;
  padding-left: 18px !important;
}
/*点击一级菜单之后*/
.el-menu-item.is-active {
  background-color: #6086c6;
  border-bottom: 0;
}
:deep(.el-menu-item .el-menu-tooltip__trigger) {
  padding: 0 8px !important;
}
</style>
