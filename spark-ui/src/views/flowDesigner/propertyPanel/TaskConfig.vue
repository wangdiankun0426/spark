<template>
  <div class="task-config">
    <el-form-item label="节点任务" label-width="90px">
      <el-button type="primary" size="small" @click="addTask">
        <el-icon><Plus /></el-icon>添加任务
      </el-button>
      <div class="task-tip">节点开始或结束执行时，按任务模板自动创建任务，按顺序依次执行，可拖拽调整执行顺序</div>
    </el-form-item>
    <div
        v-for="(task, index) in nodeTasks"
        :key="index"
        class="task-item"
        :class="{ 'task-dragging': dragIndex === index }"
        draggable="true"
        @dragstart="handleDragStart(index)"
        @dragover.prevent
        @drop="handleDrop(index)"
        @dragend="dragIndex = null"
    >
      <div class="task-item-header">
        <span class="task-item-title"><el-icon><Rank /></el-icon>任务 {{ index + 1 }}</span>
        <el-button type="danger" link size="small" @click="removeTask(task)">删除</el-button>
      </div>
      <el-form-item label="执行时机" label-width="90px">
        <el-select v-model="task.executeType">
          <el-option label="节点开始执行" :value="1" />
          <el-option label="节点结束执行" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务模板" label-width="90px">
        <el-select
            v-model="task.taskTemplateId"
            placeholder="请选择任务模板"
            @change="handleTemplateChange(task)"
        >
          <el-option
              v-for="template in templateList"
              :key="template.id"
              :label="template.name"
              :value="template.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-for="param in task.params" :key="param.code" :label="param.name" label-width="90px">
        <el-input
            v-model="param.value"
            :placeholder="param.type === 2 ? '支持变量，详见变量帮助' : '请输入' + param.name"
        />
      </el-form-item>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { pageTaskTemplateListAPI, listTaskTemplateParamAPI } from '@/api/task/template.js'

defineOptions({ name: 'TaskConfig' })

const props = defineProps({
  // 当前节点
  node: { type: Object, required: true },
  // 全部节点任务配置（直接原地修改，随保存流程提交）
  tasks: { type: Array, required: true }
})

const templateList = ref([])
// 拖拽排序：当前节点任务列表中被拖拽项的索引
const dragIndex = ref(null)

// 当前节点的任务配置
const nodeTasks = computed(() => props.tasks.filter(task => task.nodeId === props.node.id))

/**
 * 查询任务模板列表
 */
const loadTemplateList = () => {
  pageTaskTemplateListAPI({ pageNo: 1, pageSize: 100 }).then(res => {
    if (res.code === 200 && res.data) {
      templateList.value = res.data.rows || []
    }
  }).catch(() => {})
}
loadTemplateList()

/**
 * 添加任务配置
 */
const addTask = () => {
  props.tasks.push({ nodeId: props.node.id, taskTemplateId: '', taskTemplateName: '', executeType: 1, sort: props.tasks.filter(task => task.nodeId === props.node.id).length + 1, params: [] })
}

/**
 * 删除任务配置
 * @param task 任务配置
 */
const removeTask = (task) => {
  const index = props.tasks.indexOf(task)
  if (index > -1) {
    props.tasks.splice(index, 1)
  }
}

/**
 * 拖拽开始
 * @param index 当前节点任务列表中的索引
 */
const handleDragStart = (index) => {
  dragIndex.value = index
}

/**
 * 拖拽放下：将拖拽任务移动到目标位置（操作全局任务配置数组）
 * @param index 当前节点任务列表中的目标索引
 */
const handleDrop = (index) => {
  if (dragIndex.value === null || dragIndex.value === index) {
    dragIndex.value = null
    return
  }
  const fromTask = nodeTasks.value[dragIndex.value]
  const targetTask = nodeTasks.value[index]
  const fromIndex = props.tasks.indexOf(fromTask)
  const targetIndex = props.tasks.indexOf(targetTask)
  if (fromIndex > -1 && targetIndex > -1) {
    props.tasks.splice(fromIndex, 1)
    props.tasks.splice(targetIndex, 0, fromTask)
  }
  dragIndex.value = null
}

/**
 * 任务模板切换后重新加载模板参数
 * @param task 任务配置
 */
const handleTemplateChange = (task) => {
  const template = templateList.value.find(item => item.id === task.taskTemplateId)
  task.taskTemplateName = template ? template.name : ''
  task.params = []
  if (!task.taskTemplateId) {
    return
  }
  listTaskTemplateParamAPI({ templateId: task.taskTemplateId }).then(res => {
    if (res.code === 200 && res.data) {
      task.params = (res.data || []).map(item => ({
        code: item.code,
        name: item.name,
        type: item.type,
        value: ''
      }))
    }
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.task-config {
  width: 100%;
}

.task-tip {
  width: 100%;
  font-size: 12px;
  line-height: 1.5;
  color: $color-text-secondary;
}

.task-item {
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid $border-color-light;
  border-radius: 4px;
  background: #fafafa;
  cursor: move;

  &.task-dragging {
    opacity: 0.5;
    border-style: dashed;
  }

  .task-item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
    font-size: 13px;
    font-weight: 600;
    color: $color-text-primary;

    .task-item-title {
      display: flex;
      align-items: center;
      gap: 4px;
      font-weight: 400;
      color: $color-text-secondary;
    }
  }
}
</style>
