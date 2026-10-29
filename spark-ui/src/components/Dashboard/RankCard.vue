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
        <span class="rank-row__no" :class="rankClass(index)">{{ index + 1 }}</span>
        <span class="rank-row__name" :title="item.name">{{ item.name }}</span>
        <el-tag v-if="showTypeTag" size="small" effect="plain" class="rank-row__tag">{{ typeText(item.hotType) }}</el-tag>
        <span class="rank-row__bar">
          <i :style="{ width: barWidth(item.count) }"/>
        </span>
        <span class="rank-row__count">{{ item.count }}<em>{{ unit }}</em></span>
      </div>
    </div>
    <el-empty v-else :image-size="72" description="暂无数据" class="rank-card__empty"/>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  list: { type: Array, default: () => [] },
  unit: { type: String, default: '次' },
  showTypeTag: { type: Boolean, default: false }
})

const maxCount = computed(() => {
  if (!props.list.length) return 1
  return Math.max(...props.list.map(item => item.count || 0), 1)
})

function rankClass(index) {
  if (index === 0) return 'is-first'
  if (index === 1) return 'is-second'
  if (index === 2) return 'is-third'
  return ''
}

function barWidth(count) {
  return `${Math.max(8, Math.round((count || 0) / maxCount.value * 100))}%`
}

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
  gap: $spacing-sm;
  height: 38px;

  &__no {
    flex: none;
    width: 20px;
    height: 20px;
    line-height: 20px;
    text-align: center;
    font-size: 12px;
    font-weight: 600;
    border-radius: 4px;
    color: $color-text-secondary;
    background-color: $border-color-light;

    &.is-first {
      color: #fff;
      background-color: #f5b544;
    }

    &.is-second {
      color: #fff;
      background-color: #ff9500;
    }

    &.is-third {
      color: #fff;
      background-color: $color-primary;
    }
  }

  &__name {
    flex: none;
    max-width: 150px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: $color-text-primary;
  }

  &__tag {
    flex: none;
    transform: scale(0.9);
  }

  &__bar {
    flex: 1;
    height: 6px;
    border-radius: 3px;
    background-color: $border-color-light;
    overflow: hidden;

    i {
      display: block;
      height: 100%;
      border-radius: 3px;
      background: linear-gradient(90deg, $color-primary, #4d8ef0);
    }
  }

  &__count {
    flex: none;
    min-width: 48px;
    text-align: right;
    font-size: 13px;
    font-weight: 600;
    color: $color-primary;

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
