<!--
  home 路由在客户端下的容器

  Web 版 pages/home/index.vue 里带着 82px 图标侧栏，客户端侧栏已由外壳承担，
  这里只保留子路由出口，避免出现双份导航。
-->
<template>
  <div class="dsk-home">
    <router-view/>
  </div>
</template>

<style scoped lang="scss">
// 撑满内容区，让页面内 calc(100vh - 58px) 这类写法由外壳统一接管高度
.dsk-home {
  height: 100%;
  overflow: hidden;
}
</style>
