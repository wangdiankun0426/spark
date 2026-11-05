<template>
  <!-- 分组小标题：品牌渐变竖条 + 14px 粗体，全站唯一定版规格，外层间距由页面 section-head 控制 -->
  <text class="section-title" :class="{'is-danger': danger}"><slot/></text>
</template>

<script setup>
defineProps({
  // 错误语义，文字与竖条切换错误色
  danger: {type: Boolean, default: false}
})
</script>

<style scoped lang="scss">
.section-title {
  position: relative;
  padding-left: 11px;
  font-size: $font-size-base;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: $app-text-primary;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 13px;
  border-radius: $radius-bar;
  background-image: $app-gradient-brand;
}

.section-title.is-danger {
  color: $app-text-error;
}

.section-title.is-danger::before {
  background-image: none;
  background-color: $app-error-solid;
}
</style>
