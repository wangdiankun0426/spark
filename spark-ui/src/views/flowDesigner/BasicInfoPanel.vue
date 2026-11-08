<template>
  <aside class="basic-info">
    <el-scrollbar class="bi-body">
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
  </aside>
</template>

<script setup>
defineOptions({ name: 'BasicInfoPanel' })

defineProps({
  /** 模板基础信息（与流程一起提交，字段直连 el-form） */
  info: { type: Object, required: true },
  /** 流程表单下拉项 [{ label, value }] */
  formOptions: { type: Array, default: () => [] }
})

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
}

.bi-body {
  flex: 1;
  min-height: 0;
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
