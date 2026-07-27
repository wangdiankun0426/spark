<template>
  <div>
    <el-container>
      <el-aside width="140px" class="flow-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            mode="vertical"
            class="flow-el-menu"
        >
          <el-menu-item index="/flow/application">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;"><flow-list/></el-icon>
            <template #title>流程申请</template>
          </el-menu-item>
          <el-menu-item index="/flow/myAppliedList">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;"><my-applied-list/></el-icon>
            <template #title>我的申请</template>
          </el-menu-item>
          <el-menu-item index="/flow/myPendingList">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;"><my-pending-list/></el-icon>
            <template #title>我的待办</template>
          </el-menu-item>
          <el-menu-item index="/flow/myPendedList">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;"><my-applied-list/></el-icon>
            <template #title>我的已办</template>
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
import {useRoute, useRouter} from "vue-router";
import FlowList from "@/assets/icons/flowList.vue";
import MyAppliedList from "@/assets/icons/myAppliedList.vue";
import MyPendingList from "@/assets/icons/myPendingList.vue";

const router = useRouter();
const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>

<style scoped lang="scss">
.flow-el-aside {
  padding: 0;
  height: calc(100vh - $nav-height);
  background: $bg-sidebar;
  color: #ffffff;
  box-shadow: 2px 0 8px rgba(23, 43, 77, 0.12);
  overflow-y: auto;
  overflow-x: hidden;
}
.flow-el-menu {
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
