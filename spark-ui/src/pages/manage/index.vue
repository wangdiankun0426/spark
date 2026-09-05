<template>
  <div>
    <el-container>
      <el-aside width="64px" class="admin-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :collapse="true"
            mode="vertical"
            class="admin-el-menu"
        >
          <el-menu-item index="/manage/sys/user">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/manage/sys/dept">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><OfficeBuilding /></el-icon>
            <template #title>部门管理</template>
          </el-menu-item>
          <el-menu-item index="/manage/sys/role">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><UserFilled /></el-icon>
            <template #title>角色管理</template>
          </el-menu-item>
          <el-menu-item index="/manage/sys/notice">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><Bell /></el-icon>
            <template #title>公告管理</template>
          </el-menu-item>
          <el-menu-item index="/manage/sys/message">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><ChatDotRound /></el-icon>
            <template #title>消息管理</template>
          </el-menu-item>
          <el-menu-item index="/manage/log/login">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><Key /></el-icon>
            <template #title>登录日志</template>
          </el-menu-item>
          <el-menu-item index="/manage/log/operate">
            <el-icon style="font-size:18px;color:var(--sidebar-icon);vertical-align: middle;"><Document /></el-icon>
            <template #title>操作日志</template>
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
.admin-el-aside {
  padding: 0;
  height: calc(100vh - $nav-height);
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  box-shadow: var(--sidebar-shadow);
  overflow-y: auto;
  overflow-x: hidden;
}
.admin-el-menu {
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
</style>
