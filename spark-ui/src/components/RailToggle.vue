<template>
  <div class="app-sidebar__foot">
    <div
        class="app-sidebar__mini-btn"
        :title="railMini ? '展开菜单' : '收起菜单'"
        @click="toggleRail"
    >
      <el-icon>
        <Expand v-if="railMini"/>
        <Fold v-else/>
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { Expand, Fold } from '@element-plus/icons-vue';
import { railMini, toggleRail } from '@/utils/railUtil.js';
</script>
