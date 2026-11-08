<template>
  <error-page
    code="404"
    title-main="这个页面"
    title-em="走丢"
    title-after="了"
    desc="链接可能已经失效，或者地址里错了一个字符。请检查地址后重试。"
    meta-hint="确认地址无误请联系管理员"
  />
</template>

<script setup>
import ErrorPage from './errorPage.vue';
</script>
