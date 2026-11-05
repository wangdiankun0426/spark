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
          <div class="app-sidebar__group">数据</div>
          <el-menu-item index="/site/sys/tenant">
            <el-icon><User /></el-icon>
            <template #title>租户管理</template>
          </el-menu-item>
          <div class="app-sidebar__group">监控</div>
          <el-menu-item index="/site/monitor/server">
            <el-icon><Monitor /></el-icon>
            <template #title>服务器监控</template>
          </el-menu-item>
          <el-menu-item index="/site/monitor/cache">
            <el-icon><Coin /></el-icon>
            <template #title>缓存监控</template>
          </el-menu-item>
          <el-menu-item index="/site/monitor/mq">
            <el-icon><Connection /></el-icon>
            <template #title>消息队列</template>
          </el-menu-item>
          <el-menu-item index="/site/monitor/api">
            <el-icon><DataLine /></el-icon>
            <template #title>接口请求</template>
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

const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>
