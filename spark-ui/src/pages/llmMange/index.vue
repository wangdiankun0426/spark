<template>
  <div>
    <el-container>
      <el-aside width="50px" class="llm-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :collapse="true"
            mode="vertical"
            class="llm-el-menu"
        >
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_AGENT)" index="/llm/agent">
            <el-icon style="font-size: 24px;">
              <Agent/>
            </el-icon>
            <template #title>Agent</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_WORKFLOW)" index="/llm/workflow">
            <el-icon style="font-size: 24px;">
              <Workflow/>
            </el-icon>
            <template #title>WorkFlow</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_MODEL_MARKET)" index="/llm/modelMarket">
            <el-icon style="font-size: 24px;">
              <ModelMarket/>
            </el-icon>
            <template #title>模型市场</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_SKILL)" index="/llm/skill">
            <el-icon style="font-size: 24px;">
              <Skill/>
            </el-icon>
            <template #title>技能库</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_MCP)" index="/llm/mcp">
            <el-icon style="font-size: 24px;">
              <Mcp/>
            </el-icon>
            <template #title>MCP服务</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_KNOWLEDGE)" index="/llm/knowledge">
            <el-icon style="font-size: 24px;">
              <Knowledge/>
            </el-icon>
            <template #title>知识库</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_GRAPH)" index="/llm/graph">
            <el-icon style="font-size: 24px;">
              <Graph/>
            </el-icon>
            <template #title>知识图谱</template>
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
import ModelMarket from "@/assets/icons/modelMarket.vue";
import Workflow from "@/assets/icons/workflow.vue";
import Skill from "@/assets/icons/skill.vue";
import {hasMenu, MENU_CODES} from "@/utils/menuUtil.js";
import Mcp from "@/assets/icons/mcp.vue";
import Knowledge from "@/assets/icons/knowledge.vue";
import Graph from "@/assets/icons/graph.vue";

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
:deep(.el-menu--collapse) {
  width: 50px;
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
