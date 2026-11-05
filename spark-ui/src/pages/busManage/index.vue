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
          <div class="app-sidebar__group">流程</div>
          <el-menu-item v-if="hasMenu(MENU_CODES.BUS_FLOW)" index="/bus/flowTemplate">
            <el-icon><FlowApplication/></el-icon>
            <template #title>流程模板</template>
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
import FlowApplication from "@/assets/icons/flowApplication.vue";

const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>
