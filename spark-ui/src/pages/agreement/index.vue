<template>
  <div class="agreement-page">
    <div class="agreement-body">
      <div class="doc-meta">
        <h1 class="doc-name">{{ doc.title }}</h1>
        <div class="doc-desc">更新日期：{{ doc.updateDate }}　版本：{{ doc.version }}</div>
      </div>
      <div v-for="(section, index) in doc.sections" :key="index" class="doc-section">
        <h2 class="section-heading">{{ section.heading }}</h2>
        <p
            v-for="(paragraph, pIndex) in section.paragraphs"
            :key="pIndex"
            class="section-paragraph"
        >{{ paragraph }}</p>
      </div>
      <div class="doc-footer">本文件更新日期：{{ doc.updateDate }}，自发布之日起生效。</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { agreementMap } from '@/utils/agreementDoc'

const route = useRoute()

const doc = computed(() => agreementMap[route.query.type] || agreementMap.service)
</script>

<style scoped lang="scss">
.agreement-page {
  height: 100vh;
  overflow-y: auto;
  background-color: $bg-card;
}

.agreement-body {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: 48px $spacing-md $spacing-xl;
  box-sizing: border-box;
}

.doc-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-md 0;
}

.doc-name {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
  color: $color-text-primary;
}

.doc-desc {
  margin-top: $spacing-sm;
  font-size: 13px;
  color: $color-text-secondary;
}

.doc-section {
  margin-top: $spacing-lg;
}

.section-heading {
  margin: 0 0 $spacing-sm;
  font-size: 16px;
  font-weight: 700;
  line-height: $line-height-base;
  color: $color-text-primary;
}

.section-paragraph {
  margin: 0 0 $spacing-sm;
  font-size: 14px;
  line-height: 1.9;
  text-align: justify;
  color: $color-text-primary;
}

.doc-footer {
  padding: $spacing-lg 0;
  font-size: 13px;
  text-align: center;
  color: $color-text-secondary;
}
</style>
