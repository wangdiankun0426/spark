<template>
  <div class="rank-card">
    <div class="rank-card__head">
      <div class="rank-card__title">
        <el-icon class="rank-card__title-icon"><slot name="icon"/></el-icon>
        <span>{{ title }}</span>
      </div>
      <slot name="extra"/>
    </div>
    <div v-if="list.length" class="rank-card__body">
      <div v-for="(item, index) in list" :key="`${item.hotType}-${item.id}-${index}`" class="rank-row">
        <span class="rank-row__no">{{ index + 1 }}</span>
        <div class="rank-row__info">
          <div class="rank-row__name" :title="item.name">{{ item.name }}</div>
          <div v-if="showTypeTag" class="rank-row__sub">{{ typeText(item.hotType) }}</div>
        </div>
        <span class="rank-row__count">{{ item.count }}<em v-if="unit">{{ unit }}</em></span>
      </div>
    </div>
    <el-empty v-else :image-size="72" description="暂无数据" class="rank-card__empty"/>
  </div>
</template>

<script setup>
const props = defineProps({
  title: { type: String, default: '' },
  list: { type: Array, default: () => [] },
  unit: { type: String, default: '次' },
  showTypeTag: { type: Boolean, default: false }
})

function typeText(type) {
  const map = { model: '模型', agent: '智能体', workflow: '工作流', flow: '流程' }
  return map[type] || ''
}
</script>

<style scoped lang="scss">
.rank-card {
  display: flex;
  flex-direction: column;
  height: 320px;
  overflow: hidden;
  padding: $spacing-md;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
  transition: $transition-normal;

  &:hover {
    box-shadow: $shadow-card-hover;
  }

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-color-light;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 15px;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__title-icon {
    font-size: 18px;
    color: $color-primary;
  }

  &__body {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding-top: $spacing-sm;
  }

  &__empty {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: $spacing-md 0;
  }
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 48px;
  padding: 0 6px;
  border-radius: 8px;
  transition: $transition-fast;

  &:hover {
    background-color: $color-primary-light;
  }

  &__no {
    flex: none;
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    font-size: 12.5px;
    font-weight: 700;
    border-radius: 50%;
    color: $color-text-white;
    background: $ds-grad;
  }

  &__info {
    flex: 1;
    min-width: 0;
    line-height: 1.3;
  }

  &__name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13.5px;
    font-weight: 600;
    color: $color-text-primary;
  }

  &__sub {
    margin-top: 1px;
    font-size: 11.5px;
    color: $color-text-placeholder;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__count {
    flex: none;
    font-size: 15px;
    font-weight: 700;
    color: $el-text-color-primary;
    font-variant-numeric: tabular-nums;

    em {
      margin-left: 2px;
      font-size: 11px;
      font-style: normal;
      font-weight: 400;
      color: $color-text-placeholder;
    }
  }
}
</style>
