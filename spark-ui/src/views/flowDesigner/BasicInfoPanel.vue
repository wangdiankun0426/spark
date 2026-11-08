<template>
  <aside class="basic-info" :class="{ 'is-collapsed': collapsed }">
    <!-- 面板头：标题 + 折叠/展开按钮 -->
    <div class="bi-head">
      <span v-if="!collapsed" class="bi-title">基础信息</span>
      <el-tooltip :content="collapsed ? '展开基础信息' : '收起基础信息'" placement="right">
        <el-button class="bi-toggle" text size="small" @click="collapsed = !collapsed">
          <el-icon>
            <DArrowRight v-if="collapsed" />
            <DArrowLeft v-else />
          </el-icon>
        </el-button>
      </el-tooltip>
    </div>

    <el-scrollbar v-show="!collapsed" class="bi-body">
      <el-form :model="info" label-position="top" class="bi-form">
        <el-form-item label="名称" required>
          <el-input v-model="info.name" placeholder="请输入名称" maxlength="50" clearable />
        </el-form-item>
        <el-form-item label="表单" required>
          <el-select v-model="info.formId" placeholder="请选择流程表单" style="width: 100%">
            <el-option
                v-for="item in formOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="info.type" placeholder="请选择流程类型" style="width: 100%">
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" required>
          <el-switch
              v-model="info.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
              v-model="info.remark"
              type="textarea"
              :rows="5"
              maxlength="200"
              show-word-limit
              resize="none"
              placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
    </el-scrollbar>

    <!-- 折叠态：只留竖排标题，点击可展开 -->
    <div v-if="collapsed" class="bi-collapsed-label" @click="collapsed = false">流程模板基础信息</div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'

defineOptions({ name: 'BasicInfoPanel' })

defineProps({
  /** 模板基础信息（与流程一起提交，字段直连 el-form） */
  info: { type: Object, required: true },
  /** 流程表单下拉项 [{ label, value }] */
  formOptions: { type: Array, default: () => [] }
})

/** 是否折叠：默认收起，把画布空间留给流程 */
const collapsed = ref(true)

/** 流程类型选项（与流程模板列表保持一致） */
const typeOptions = [
  { label: '普通流程', value: 1 },
  { label: '知识库归档流程', value: 2 }
]
</script>

<style scoped lang="scss">
.basic-info {
  width: 250px;
  flex: 0 0 250px;
  display: flex;
  flex-direction: column;
  background: $app-bg-card;
  border-right: 1px solid $border-color;
  overflow: hidden;
  transition: width 0.18s ease, flex-basis 0.18s ease;

  &.is-collapsed {
    width: 44px;
    flex-basis: 44px;
  }
}

.bi-head {
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 8px 0 14px;
  border-bottom: 1px solid $app-border-light;

  .is-collapsed & {
    padding: 0;
    justify-content: center;
  }
}

.bi-title {
  flex: 1;
  font-size: 13px;
  font-weight: 600;
  color: $app-text-primary;
}

.bi-toggle {
  padding: 4px;
  color: $app-text-secondary;

  &:hover {
    color: $el-color-primary;
  }
}

.bi-body {
  flex: 1;
  min-height: 0;
}

// 折叠态竖排标题
.bi-collapsed-label {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 12px;
  line-height: 1.5;
  letter-spacing: 1px;
  color: $app-text-secondary;
  writing-mode: vertical-rl;
  cursor: pointer;
  user-select: none;

  &:hover {
    color: $el-color-primary;
  }
}

.bi-form {
  padding: 14px $spacing-md 20px;

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    padding-bottom: 4px;
    font-size: 12px;
    color: $color-text-secondary;
  }
}
</style>
