<template>
  <div>
    <el-container>
      <el-aside :width="railMini ? railWidthMini : railWidth" class="app-sidebar" :class="{ 'app-sidebar--mini': railMini }">
        <rail-toggle/>
        <sidebar-menu :items="sidebarItems" :collapse="railMini"/>
      </el-aside>

      <el-main class="affix-container">
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import RailToggle from '@/components/RailToggle/index.vue';
import SidebarMenu from '@/components/SidebarMenu/index.vue';
import {railMini, railWidth, railWidthMini} from '@/utils/railUtil.js';
import {useModuleMenus} from '@/utils/useModuleMenus.js';

const {sidebarItems} = useModuleMenus();
</script>