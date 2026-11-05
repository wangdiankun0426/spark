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
          <el-menu-item index="/org/sys/user">
            <el-icon>
              <User />
            </el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/org/sys/dept">
            <el-icon>
              <OfficeBuilding />
            </el-icon>
            <template #title>部门管理</template>
          </el-menu-item>
          <el-menu-item index="/org/sys/role">
            <el-icon>
              <Role />
            </el-icon>
            <template #title>角色管理</template>
          </el-menu-item>
          <el-menu-item index="/org/sys/message">
            <el-icon>
              <Bell />
            </el-icon>
            <template #title>消息管理</template>
          </el-menu-item>
          <div class="app-sidebar__group">监控</div>
          <el-menu-item index="/org/log/login">
            <el-icon>
              <Key />
            </el-icon>
            <template #title>登录日志</template>
          </el-menu-item>
          <el-menu-item index="/org/log/operate">
            <el-icon>
              <Document />
            </el-icon>
            <template #title>操作日志</template>
          </el-menu-item>
          <div class="app-sidebar__group">配置</div>
          <el-menu-item index="/org/sys/config">
            <el-icon>
              <SetUp />
            </el-icon>
            <template #title>系统配置</template>
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
import {SetUp} from "@element-plus/icons-vue";
import Role from "@/assets/icons/role.vue";

const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>
