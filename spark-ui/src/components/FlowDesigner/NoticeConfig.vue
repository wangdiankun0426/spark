<template>
  <el-dialog
      title="流程通知"
      v-model="dialogVisible"
      width="60%"
      :close-on-click-modal="false"
  >
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane
          v-for="item in config"
          :key="item.type"
          :label="item.label"
          :name="item.type"
      >
        <el-form label-width="90px">
          <el-form-item label="启用通知">
            <el-switch
                v-model="item.enabled"
                active-text="开启"
                inactive-text="关闭"
            />
          </el-form-item>
          <el-form-item label="通知内容">
            <el-input
                v-model="item.content"
                type="textarea"
                :rows="3"
                placeholder="请输入通知内容，支持模板变量"
                :disabled="!item.enabled"
            />
            <div class="notify-var-hint">
              <span class="notify-var-label">插入变量：</span>
              <el-button
                  v-for="v in contentVars"
                  :key="v.value"
                  size="small"
                  link
                  :disabled="!item.enabled"
                  @click="insertVar(item, 'content', v.value)"
              >
                {{ v.label }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="通知人">
            <el-input
                v-model="item.recipient"
                type="textarea"
                :rows="2"
                placeholder="请输入通知人表达式，支持模板变量"
                :disabled="!item.enabled"
            />
            <div class="notify-var-hint">
              <span class="notify-var-label">插入变量：</span>
              <el-button
                  v-for="v in recipientVars"
                  :key="v.value"
                  size="small"
                  link
                  :disabled="!item.enabled"
                  @click="insertVar(item, 'recipient', v.value)"
              >
                {{ v.label }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  config: { type: Array, required: true }
})

const dialogVisible = ref(false)
const activeTab = ref(2)

// 通知内容可用的模板变量
const contentVars = [
  { label: '申请人', value: '#{base:appUserName}#' },
  { label: '流程名称', value: '#{base:flowName}#' },
]

// 通知人可用的模板变量
const recipientVars = [
  { label: '申请人', value: '#{base:appUserName}#' },
  { label: '当前审批人', value: '#{base:appAssignee}#' },
]

// 在指定字段中插入模板变量
const insertVar = (item, field, variable) => {
  item[field] = (item[field] || '') + variable
}

// 暴露打开方法给父组件
const open = () => {
  activeTab.value = 2
  dialogVisible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.notify-var-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  flex-wrap: wrap;
}
.notify-var-label {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}
</style>