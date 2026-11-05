<template>
  <div>
    <el-container>
      <el-aside :width="railMini ? railWidthMini : railWidth" class="app-sidebar" :class="{ 'app-sidebar--mini': railMini }">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :collapse="railMini"
            :collapse-transition="false"
            mode="vertical"
            class="app-sidebar-menu"
        >
          <div
              v-if="hasMenu(MENU_CODES.LLM_MODEL_USAGE) || hasMenu(MENU_CODES.LLM_MODEL_MARKET) || hasMenu(MENU_CODES.LLM_SKILL) || hasMenu(MENU_CODES.LLM_MCP)"
              class="app-sidebar__group"
          >基础</div>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_MODEL_USAGE)" index="/llm/modelUsage">
            <el-icon><ModelUsage/></el-icon>
            <template #title>模型用量</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_MODEL_MARKET)" index="/llm/modelMarket">
            <el-icon><ModelMarket/></el-icon>
            <template #title>模型市场</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_SKILL)" index="/llm/skill">
            <el-icon><Skill/></el-icon>
            <template #title>技能库</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_MCP)" index="/llm/mcp">
            <el-icon><Mcp/></el-icon>
            <template #title>MCP服务</template>
          </el-menu-item>

          <div
              v-if="hasMenu(MENU_CODES.LLM_AGENT) || hasMenu(MENU_CODES.LLM_WORKFLOW)"
              class="app-sidebar__group"
          >应用</div>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_AGENT)" index="/llm/agent">
            <el-icon><Agent/></el-icon>
            <template #title>智能体</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_WORKFLOW)" index="/llm/workflow">
            <el-icon><Workflow/></el-icon>
            <template #title>工作流</template>
          </el-menu-item>

          <div
              v-if="hasMenu(MENU_CODES.LLM_KNOWLEDGE) || hasMenu(MENU_CODES.LLM_GRAPH)"
              class="app-sidebar__group"
          >增强</div>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_KNOWLEDGE)" index="/llm/knowledge">
            <el-icon><Knowledge/></el-icon>
            <template #title>知识库</template>
          </el-menu-item>
          <el-menu-item v-if="hasMenu(MENU_CODES.LLM_GRAPH)" index="/llm/graph">
            <el-icon><Graph/></el-icon>
            <template #title>知识图谱</template>
          </el-menu-item>
        </el-menu>
        <rail-toggle/>
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
import RailToggle from '@/components/RailToggle.vue';
import {railMini, railWidth, railWidthMini} from '@/utils/railUtil.js';
import {hasMenu, MENU_CODES} from "@/utils/menuUtil.js";
import ModelUsage from "@/assets/icons/modelUsage.vue";
import ModelMarket from "@/assets/icons/modelMarket.vue";
import Skill from "@/assets/icons/skill.vue";
import Mcp from "@/assets/icons/mcp.vue";
import Agent from "@/assets/icons/agent.vue";
import Workflow from "@/assets/icons/workflow.vue";
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
