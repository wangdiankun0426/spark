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
            <var-field
                v-model="item.content"
                textarea
                :rows="3"
                placeholder="请输入通知内容，支持模板变量"
                :disabled="!item.enabled"
            />
          </el-form-item>
          <el-form-item label="通知人">
            <var-field
                v-model="item.recipient"
                textarea
                :rows="2"
                placeholder="请输入通知人表达式，支持模板变量"
                :disabled="!item.enabled"
            />
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
import VarField from './var/VarField.vue'

const props = defineProps({
  config: { type: Array, required: true }
})

const dialogVisible = ref(false)
const activeTab = ref(2)

// 暴露打开方法给父组件
const open = () => {
  activeTab.value = 2
  dialogVisible.value = true
}

defineExpose({ open })
</script>