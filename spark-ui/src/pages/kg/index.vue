<template>
  <div>
    <el-container>
      <el-aside width="140px" class="kg-el-aside">
        <el-menu
            :default-active="defaultActive"
            :router='true'
            mode="vertical"
            class="kg-el-menu"
        >
          <el-menu-item index="/kg/search">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;"><SearchDocument/></el-icon>
            <template #title>知识检索</template>
          </el-menu-item>
          <el-menu-item index="/kg/graph">
            <el-icon style="font-size:18px;color:#fef0f0;vertical-align: middle;"><Graph/></el-icon>
            <template #title>知识图谱</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <!-- 在模板中使用 <router-view> 来显示组件 -->
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup name="manage">

import {computed} from "vue";
import {useRoute} from "vue-router";
import Knowledge from "@/assets/icons/knowledge.vue";
import SearchDocument from "@/assets/icons/searchDocument.vue";
import Graph from "@/assets/icons/graph.vue";
import { Box, Share } from "@element-plus/icons-vue";

const route = useRoute();

/**
 * 初始化路径
 */
const defaultActive = computed(() => {
  return route.path; //监听路由，控制菜单选择
})

</script>

<style lang="scss" scoped>
.kg-el-aside {
  padding: 0;
  height: calc(100vh - $nav-height);
  background: $bg-sidebar;
  color: #ffffff;
  box-shadow: 2px 0 8px rgba(23, 43, 77, 0.12);
  overflow-y: auto;
  overflow-x: hidden;
}
.kg-el-menu {
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
