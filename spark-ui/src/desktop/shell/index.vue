<!--
  客户端独立外壳

  Web 端走 pages/index.vue 的 el-container 结构，客户端在 isDesktop 时改走这里，
  两套外壳互不引用、互不覆盖样式。内容区只是 <router-view />，
  因此后续各业务页可以逐个迁移，未迁移的页面仍复用 Web 组件。
-->
<template>
  <div class="dsk-shell">
    <dsk-topbar/>
    <div class="dsk-shell__body">
      <dsk-sidebar/>
      <main class="dsk-shell__main">
        <router-view/>
      </main>
    </div>
  </div>
</template>

<script setup>
import DskTopbar from './topbar.vue';
import DskSidebar from './sidebar.vue';
</script>

<style scoped lang="scss">
.dsk-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: $dsk-bg;
}

.dsk-shell__body {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
}

.dsk-shell__main {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  background: $dsk-bg;
}
</style>
