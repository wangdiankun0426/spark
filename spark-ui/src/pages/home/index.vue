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
          <div class="app-sidebar__group">通用</div>
          <el-menu-item index="/home/dashboard">
            <el-icon><Dashboard/></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <el-menu-item index="/home/contact">
            <el-icon><Chat/></el-icon>
            <template #title>通讯录</template>
          </el-menu-item>
          <div class="app-sidebar__group">AI</div>
          <el-menu-item index="/home/aichat">
            <el-icon><AiChat/></el-icon>
            <template #title>AI对话</template>
          </el-menu-item>
          <el-menu-item index="/home/agent">
            <el-icon><Agent/></el-icon>
            <template #title>智能体</template>
          </el-menu-item>
          <el-menu-item index="/home/workflow">
            <el-icon><Workflow/></el-icon>
            <template #title>工作流</template>
          </el-menu-item>
          <div class="app-sidebar__group">流程</div>
          <el-menu-item index="/home/flow/application">
            <el-icon><FlowApplication/></el-icon>
            <template #title>流程申请</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/myApplication">
            <el-icon><MyApplication/></el-icon>
            <template #title>我的申请</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/myTodo">
            <el-icon><MyTodo/></el-icon>
            <template #title>我的待办</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/myDone">
            <el-icon><MyDone/></el-icon>
            <template #title>我的已办</template>
          </el-menu-item>
          <el-menu-item index="/home/flow/copyMe">
            <el-icon><CopyMe/></el-icon>
            <template #title>抄送给我</template>
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
import Dashboard from "@/assets/icons/dashboard.vue";
import Chat from "@/assets/icons/chat.vue";
import AiChat from "@/assets/icons/aichat.vue";
import Agent from "@/assets/icons/agent.vue";
import Workflow from "@/assets/icons/workflow.vue";
import FlowApplication from "@/assets/icons/flowApplication.vue";
import MyApplication from "@/assets/icons/myApplication.vue";
import MyTodo from "@/assets/icons/myTodo.vue";
import MyDone from "@/assets/icons/myDone.vue";
import CopyMe from "@/assets/icons/copyMe.vue";
const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>
