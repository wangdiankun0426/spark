<template>
  <error-page
    code="403"
    title-main="这里暂时"
    title-em="进不去"
    desc="你的账号还没有访问这个页面的权限，需要管理员开通后才能查看。"
    meta-tag="管理员"
  />
</template>

<script setup>
import ErrorPage from './errorPage.vue';
</script>
