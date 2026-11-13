<template>
  <el-menu
      :default-active="defaultActive"
      :router="true"
      :collapse="collapse"
      :collapse-transition="false"
      mode="vertical"
      class="app-sidebar-menu"
  >
    <template v-for="entry in items" :key="entry.key">
      <div v-if="entry.type === 'group'" class="app-sidebar__group">{{ entry.title }}</div>
      <el-menu-item v-else :index="entry.path">
        <el-icon><component :is="entry.icon"/></el-icon>
        <template #title>{{ entry.label }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import {computed} from 'vue';
import {useRoute} from 'vue-router';

defineProps({
  items: {type: Array, default: () => []},
  collapse: {type: Boolean, default: false}
});

const route = useRoute();

const defaultActive = computed(() => route.path);
</script>