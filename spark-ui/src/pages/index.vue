<template>
  <div>
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
  overflow: hidden;
  // 导航栏向下投影，区分内容区
  box-shadow: 0 2px 8px 0 rgba(23, 43, 77, 0.15);
  position: relative;
  z-index: 100;
}

.el-main {
  padding: 0;
  height: 100vh;
  background-color: $bg-page;
}
</style>

