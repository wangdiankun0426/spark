<template>
  <div class="stat-card">
    <div v-if="$slots.icon" class="stat-card__icon" :class="`is-${type}`">
      <slot name="icon"/>
    </div>
    <div class="stat-card__main">
      <div class="stat-card__label">{{ label }}</div>
      <div class="stat-card__value" :class="`is-${type}`">
        {{ value }}
        <span v-if="unit" class="stat-card__unit">{{ unit }}</span>
      </div>
      <div v-if="sub" class="stat-card__sub">{{ sub }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, default: '' },
  value: { type: [String, Number], default: '--' },
  unit: { type: String, default: '' },
  sub: { type: String, default: '' },
  type: { type: String, default: 'primary' }
})
</script>

<style scoped lang="scss">
.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;

  &__icon {
    flex: none;
    width: 46px;
    height: 46px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    background-color: rgba(0, 79, 197, 0.1);
    color: $color-primary;

    &.is-success {
      background-color: rgba(52, 199, 89, 0.12);
      color: var(--el-color-success);
    }

    &.is-warning {
      background-color: rgba(255, 149, 0, 0.12);
      color: var(--el-color-warning);
    }

    &.is-danger {
      background-color: rgba(245, 108, 108, 0.12);
      color: var(--el-color-danger);
    }
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__label {
    color: $color-text-secondary;
    font-size: 13px;
  }

  &__value {
    margin-top: $spacing-sm;
    font-size: 26px;
    font-weight: 600;
    line-height: 1.2;
    color: $color-primary;

    &.is-success {
      color: var(--el-color-success);
    }

    &.is-warning {
      color: var(--el-color-warning);
    }

    &.is-danger {
      color: var(--el-color-danger);
    }
  }

  &__unit {
    margin-left: 4px;
    font-size: 13px;
    font-weight: 400;
    color: $color-text-secondary;
  }

  &__sub {
    margin-top: 4px;
    font-size: 12px;
    color: $color-text-placeholder;
  }
}
</style>
