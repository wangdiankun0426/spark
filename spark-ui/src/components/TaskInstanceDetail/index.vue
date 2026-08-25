<template>
  <el-dialog
      v-model="visible"
      title="任务实例详情"
      width="700px"
      destroy-on-close
  >
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="编号">{{ detail.id }}</el-descriptions-item>
      <el-descriptions-item label="任务类型">{{ detail.taskTypeName }}</el-descriptions-item>
      <el-descriptions-item label="任务状态">{{ detail.statusName }}</el-descriptions-item>
      <el-descriptions-item label="业务对象类型">{{ detail.objTypeName }}</el-descriptions-item>
      <el-descriptions-item label="业务对象id">{{ detail.objId }}</el-descriptions-item>
      <el-descriptions-item label="任务组id">{{ detail.setId }}</el-descriptions-item>
      <el-descriptions-item label="执行顺序">{{ detail.sort }}</el-descriptions-item>
      <el-descriptions-item label="下次执行时间">{{ detail.taskTime }}</el-descriptions-item>
      <el-descriptions-item label="重复间隔(小时)">{{ detail.intervalHours }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detail.createdDt }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
    </el-descriptions>
    <div class="section-title">任务入参</div>
    <pre class="json-block">{{ formatJson(detail.inputJson) }}</pre>
    <div class="section-title">任务出参</div>
    <pre class="json-block">{{ formatJson(detail.outputJson) }}</pre>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { queryTaskInstanceDetailAPI } from '@/api/task/instance'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  taskId: { type: [Number, String], default: undefined }
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const detail = ref({})

/**
 * 弹窗打开且 taskId 存在时加载任务实例详情
 */
watch(
  () => [props.modelValue, props.taskId],
  ([isOpen, taskId]) => {
    if (isOpen && taskId) {
      loadDetail(taskId)
    }
    if (!isOpen) {
      detail.value = {}
    }
  }
)

/**
 * 加载任务实例详情
 */
function loadDetail(taskId) {
  queryTaskInstanceDetailAPI({ id: taskId }).then(res => {
    if (res.code !== 200) return
    detail.value = res.data || {}
  })
}

/**
 * 格式化JSON字符串
 * @param jsonStr JSON字符串
 * @returns 格式化后的JSON字符串
 */
function formatJson(jsonStr) {
  if (!jsonStr) return '暂无数据'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}
</script>

<style scoped lang="scss">
.section-title {
  margin: $spacing-md 0 $spacing-sm;
  font-weight: bold;
  color: $color-text-primary;
}
.json-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
