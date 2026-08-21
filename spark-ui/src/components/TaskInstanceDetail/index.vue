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
    <div class="section-title">任务参数</div>
    <el-table :data="detail.params" size="small" border>
      <el-table-column prop="code" label="参数编码" align="center" />
      <el-table-column prop="value" label="参数值" align="center" />
    </el-table>
    <div class="section-title">产出数据</div>
    <el-table :data="detail.dataList" size="small" border>
      <el-table-column prop="code" label="数据编码" align="center" />
      <el-table-column prop="value" label="数据值" align="center" />
      <el-table-column prop="createdDt" label="产出时间" width="160" align="center" />
    </el-table>
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
 * 加载任务实例详情（含参数与产出数据）
 */
function loadDetail(taskId) {
  queryTaskInstanceDetailAPI({ id: taskId }).then(res => {
    if (res.code !== 200) return
    detail.value = res.data || {}
  })
}
</script>

<style scoped lang="scss">
.section-title {
  margin: $spacing-md 0 $spacing-sm;
  font-weight: bold;
  color: $color-text-primary;
}
</style>
