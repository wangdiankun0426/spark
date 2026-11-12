<template>
  <!-- 桌面客户端：使用独立外壳，与 Web 端 el-container 结构完全分开 -->
  <desktop-shell v-if="isDesktop"/>
  <div v-else>
    <el-container>
      <el-header class="el-header">
        <navigation/>
      </el-header>
<!--      <el-watermark :font="font" :content="['spark.ai']">-->
        <el-main>
          <!-- 在模板中使用 <router-view> 来显示组件 -->
          <router-view></router-view>
        </el-main>
<!--      </el-watermark>-->
    </el-container>
  </div>
</template>

<script setup>
import navigation from '@/navigation';
import {ref} from "vue";
import {isDesktop} from '@desktop/env.js';
import DesktopShell from '@desktop/shell/index.vue';

const font = ref('rgba(0, 0, 0, .15)');

</script>

<style lang="scss" scoped>
.el-container {
  display: block !important;
}

.el-header {
  padding: 0;
  margin: 0;
  height: $nav-height;
  font-size: 14px;
  overflow: visible;
  box-shadow: $nav-shadow;
  position: relative;
  z-index: 100;
}

.el-main {
  padding: 0;
  height: calc(100vh - #{$nav-height});
  background-color: $bg-page;
  overflow: hidden;
}
</style>
