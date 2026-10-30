<template>
  <div>
    <el-container>
      <el-aside width="50px" class="site-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :collapse="true"
            mode="vertical"
            class="site-el-menu"
        >
          <div class="site-menu-group">数据</div>
          <el-menu-item index="/site/sys/tenant">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><User /></el-icon>
            <template #title>租户管理</template>
          </el-menu-item>
          <div class="site-menu-group">监控</div>
          <el-menu-item index="/site/monitor/server">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><Monitor /></el-icon>
            <template #title>服务器监控</template>
          </el-menu-item>
          <el-menu-item index="/site/monitor/cache">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><Coin /></el-icon>
            <template #title>缓存监控</template>
          </el-menu-item>
          <el-menu-item index="/site/monitor/mq">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><Connection /></el-icon>
            <template #title>消息队列</template>
          </el-menu-item>
          <el-menu-item index="/site/monitor/api">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><DataLine /></el-icon>
            <template #title>接口请求</template>
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

const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>

<style lang="scss" scoped>
.site-el-aside {
  padding: 0;
  box-sizing: border-box;
  height: calc(100vh - $nav-height);
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  box-shadow: var(--sidebar-shadow);
  overflow-y: auto;
  overflow-x: hidden;
}
.site-el-menu {
  border: 0;
  padding: 0;
  margin: 0;
  background: transparent !important;
}
.site-menu-group {
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
.el-menu-item {
  color: var(--sidebar-text);
  height: 44px;
  line-height: 44px;
  font-size: 13px;
  font-weight: 400;
  justify-content: center;
  width: calc(100% - 10px);
  margin: 3px 5px;
  border-radius: $border-radius-md;
}
:deep(.el-menu-item) {
  padding: 0 !important;
}
/*悬停一级菜单*/
.el-menu-item:hover {
  background-color: var(--sidebar-hover-bg);
}
/*点击一级菜单*/
.el-menu-item.is-active {
  background-color: var(--sidebar-active-bg);
  color: var(--sidebar-active-text);
  border-bottom: 0;
}
:deep(.el-menu-item .el-menu-tooltip__trigger) {
  padding: 0 !important;
  justify-content: center;
}
:deep(.el-menu--collapse) {
  width: 50px;
}
</style>
