<template>
  <el-drawer
      v-model="drawerVisible"
      title="切换租户"
      direction="ltr"
      size="420px"
      :close-on-click-modal="true"
      @open="loadTenantList"
  >
    <div v-loading="loading" class="tenant-switch-list">
      <div v-if="tenantList.length === 0" class="tenant-switch-empty">
        <el-empty description="您尚未加入可用的租户，请联系管理员开通" :image-size="60"/>
      </div>
      <div
          v-for="item in tenantList"
          :key="item.id"
          class="tenant-switch-item"
          :class="{ 'is-current': isCurrent(item.id) }"
      >
        <div class="tenant-switch-info">
          <div class="tenant-switch-name">
            {{ item.name }}
            <el-tag v-if="isCurrent(item.id)">当前租户</el-tag>
          </div>
          <div class="tenant-switch-meta">
            <span>截止时间：{{ item.deadline || '不限' }} </span>
          </div>
        </div>
        <el-button
            v-if="!isCurrent(item.id)"
            type="primary"
            size="small"
            @click="handleSwitch(item)"
        >切换</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import {queryMyTenantListAPI, switchTenantAPI} from '@/api/manage/sys/myTenant.js';
import {ElMessage} from 'element-plus';
import {computed, ref} from 'vue';

const props = defineProps({
  // 抽屉显隐
  modelValue: { type: Boolean, default: false },
  // 当前默认租户id
  currentTenantId: { type: [Number, String], default: undefined },
});

const emit = defineEmits(['update:modelValue']);

const drawerVisible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val),
});

const loading = ref(false);
const tenantList = ref([]);

/**
 * 判断是否为当前默认租户
 * @param id 租户id
 * @returns {boolean}
 */
function isCurrent(id) {
  return Number(id) === Number(props.currentTenantId);
}

/**
 * 加载当前用户已加入且可用的租户
 */
function loadTenantList() {
  loading.value = true;
  queryMyTenantListAPI().then(res => {
    if (res.code !== 200) {
      return;
    }
    tenantList.value = res.data || [];
  }).finally(() => {
    loading.value = false;
  })
}

/**
 * 切换默认租户
 * @param item 租户数据
 */
function handleSwitch(item) {
  switchTenantAPI({ id: item.id }).then(res => {
    if (res.code !== 200) {
      return;
    }
    drawerVisible.value = false;
    ElMessage.success('已切换至「' + item.name + '」');
    setTimeout(() => {
      window.location.reload();
    }, 300);
  })
}
</script>

<style scoped lang="scss">
.tenant-switch-list {
  .tenant-switch-empty {
    padding-top: 60px;
  }
  .tenant-switch-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md;
    margin-bottom: $spacing-sm;
    border: 1px solid $border-color-light;
    border-radius: $border-radius-sm;
    transition: $transition-fast;
    &.is-current {
      border-color: $color-primary;
      background: $color-primary-soft;
    }
    .tenant-switch-info {
      min-width: 0;
      .tenant-switch-name {
        font-size: 14px;
        color: $color-text-primary;
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        .el-tag {
          margin-left: 0;
        }
      }
      .tenant-switch-meta {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        margin-top: $spacing-xs;
        font-size: 12px;
        color: $color-text-secondary;
      }
    }
  }
}
</style>
