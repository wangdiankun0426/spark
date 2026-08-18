<template>
  <div>
    <el-container>
      <el-aside width="180px" class="admin-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            :default-openeds="['1','2','3','4','5','6','7','8','9']"
            mode="vertical"
            class="admin-el-menu"
          >
          <el-sub-menu index="1">
            <template #title>
              <el-icon><User /></el-icon>
              <span style="color: var(--sidebar-group-text)">系统数据</span>
            </template>
            <el-menu-item index="/manage/system/user">
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/system/dept">
              <span>部门管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/system/role">
              <span>角色管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/system/notice">
              <span>公告管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Ai /></el-icon>
              <span style="color: var(--sidebar-group-text)">AI管理</span>
            </template>
            <el-menu-item index="/manage/llm/providerManage">
              <span>厂商管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/llm/modelManage">
              <span>模型管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/llm/mcpManage">
              <span>MCP管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/llm/agentManage">
              <span>Agent管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="3">
            <template #title>
              <el-icon><Collection /></el-icon>
              <span style="color: var(--sidebar-group-text)">知识库管理</span>
            </template>
            <el-menu-item index="/manage/kb/knowledge">
              <span>知识库</span>
            </el-menu-item>
            <el-menu-item index="/manage/kb/document">
              <span>知识文档</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="4">
            <template #title>
              <el-icon><GraphV2 /></el-icon>
              <span style="color: var(--sidebar-group-text)">知识图谱管理</span>
            </template>
            <el-menu-item index="/manage/kg/graph">
              <span>知识图谱</span>
            </el-menu-item>
            <el-menu-item index="/manage/kg/entity">
              <span>实体管理</span>
            </el-menu-item>
            <el-menu-item index="/manage/kg/relation">
              <span>关系管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="5">
            <template #title>
              <el-icon><Share /></el-icon>
              <span style="color: var(--sidebar-group-text)">工作流管理</span>
            </template>
            <el-menu-item index="/manage/workflow/template">
              <span>流程模板</span>
            </el-menu-item>
            <el-menu-item index="/manage/workflow/instance">
              <span>运行记录</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="6">
            <template #title>
              <el-icon><Document /></el-icon>
              <span style="color: var(--sidebar-group-text)">表单管理</span>
            </template>
            <el-menu-item index="/manage/form/template">
              <span>表单模版</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="7">
            <template #title>
              <el-icon><Share /></el-icon>
              <span style="color: var(--sidebar-group-text)">流程管理</span>
            </template>
            <el-menu-item index="/manage/flow/template">
              <span>流程模板</span>
            </el-menu-item>
            <el-menu-item index="/manage/flow/instance">
              <span>流程实例</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="8">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span style="color: var(--sidebar-group-text)">对话管理</span>
            </template>
            <el-menu-item index="/manage/chat/msgManage">
              <span>聊天记录</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="9">
            <template #title>
              <el-icon><Notebook /></el-icon>
              <span style="color: var(--sidebar-group-text)">日志管理</span>
            </template>
            <el-menu-item index="/manage/log/login">
              <span>登录日志</span>
            </el-menu-item>
            <el-menu-item index="/manage/log/operate">
              <span>操作日志</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-main>
        <!-- 在模板中使用 <router-view> 来显示组件 -->
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>

import {computed} from "vue";
import {useRoute} from "vue-router";
import {User, Document, Share, Collection} from "@element-plus/icons-vue";
import Ai from "@/assets/icons/ai.vue";
import GraphV2 from "@/assets/icons/graphV2.vue";

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
// 子菜单标题
:deep(.el-menu:not(.el-menu--collapse) .el-sub-menu__title) {
  background-color: var(--sidebar-group-bg) !important;
  color: var(--sidebar-group-text) !important;
  height: 40px !important; // 将默认高达50/56的一级子菜单压扁
  line-height: 40px !important;
  font-size: 13px !important;
  font-weight: 560;
  transition: $transition-fast;
  padding: 5px 5px 5px 16px;

  &:hover {
    background-color: var(--sidebar-group-hover-bg) !important;
  }
}

// 压缩子菜单下的图标间距
:deep(.el-sub-menu__title .el-icon) {
  font-size: 16px !important;
  //margin-right: 8px;
}

// 子菜单展开区域透明
:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.el-menu-item {
  font-size: 13px; // 缩小字体
  font-weight: 400;
  height: 36px;    // 减小行高
  line-height: 40px;
  color: var(--sidebar-item-text) !important;
  transition: $transition-fast;
  padding-left: 46px !important;
}

// 菜单项中的图标样式
.el-menu-item .el-icon {
  color: var(--sidebar-icon-color);
  margin-right: 5px;
  font-size: 14px;
}

// 悬停二级菜单
.el-menu-item:hover {
  background-color: var(--sidebar-item-hover-bg) !important;
  color: var(--sidebar-item-hover-text) !important;
  padding-left: 44px !important;
}

// 点击二级菜单激活
.el-menu-item.is-active {
  background-color: var(--sidebar-item-active-bg) !important;
  color: var(--sidebar-item-active-text) !important;
  border-right: 3px solid var(--sidebar-item-active-border);
  font-weight: 600;
}
</style>

