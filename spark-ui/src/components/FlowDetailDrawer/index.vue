<template>
  <el-drawer
      :model-value="visible"
      title="流程详情"
      direction="ltr"
      size="100%"
      :before-close="handleClose"
  >
    <el-tabs v-model="tabActive">
      <el-tab-pane label="表单详情" name="form">
        <div class="instance-info">
          <el-form ref="titleFormRef" :model="formModel" label-width="80px" style="height: 100% !important;">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="申请人" prop="createdByName" :rules="{ required: true, message: '申请人不能为空', trigger: 'blur' }">
                  <el-input :model-value="createdByName" readonly />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="申请部门" prop="deptName" :rules="{ required: true, message: '申请部门不能为空', trigger: 'blur' }">
                  <el-input :model-value="deptName" readonly />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="紧急程度" prop="level" :rules="{ required: true, message: '请选择紧急程度', trigger: 'change' }">
                  <el-select v-model="instanceLevel" placeholder="请选择紧急程度" style="width: 100%;" :disabled="type !== 1">
                    <el-option
                        v-for="item in levelOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="标题" prop="name" :rules="{ required: true, message: '请输入流程标题', trigger: 'blur' }">
              <el-input
                  v-model="instanceName"
                  placeholder="请输入流程标题"
                  clearable
                  :readonly="type !== 1"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                  v-model="instanceDescription"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入备注"
                  clearable
                  :readonly="type !== 1"
              />
            </el-form-item>
          </el-form>
        </div>
        <form-view
            :disabled="type !== 1"
            :form="formJson"
            v-if="visible && tabActive === 'form'"
        />
      </el-tab-pane>
      <el-tab-pane label="流程图" name="flow">
        <el-row :gutter="20">
          <el-col :span="6" v-if="nodes.length > 0">
            <div class="timeline-container">
              <el-timeline>
                <el-timeline-item
                    v-for="(node, index) in nodes"
                    :key="node.id || index"
                    :type="getTimelineType(node.status)"
                    :timestamp="node.createdDt"
                    placement="top"
                >
                  <div class="node-item">
                    <div class="node-item-header">
                      <span class="node-name">{{ node.name }}</span>
                      <el-tag v-if="node.type === 'userTask'" :type="getStatusTagType(node.status)" size="small">
                        {{ node.statusName }}
                      </el-tag>
                    </div>
                    <div v-if="node.status === 2" class="node-item-row">待审批人：{{ node.unAssigneeName || '-' }}</div>
                    <div v-for="discuss in node.discusses" :key="discuss.id || discuss.createdDt" class="discuss-item">
                      <div class="discuss-header">
                        <span class="discuss-name">{{ discuss.assigneeName }}</span>
                        <span class="discuss-status" :class="'discuss-status--' + discuss.status">{{ discuss.statusName }}</span>
                        <span class="discuss-time">{{ discuss.createdDt }}</span>
                      </div>
                      <div v-if="discuss.discuss" class="discuss-content">{{ discuss.discuss }}</div>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
          <el-col :span="nodes.length > 0 ? 18 : 24">
            <flow-view
                :bpmJson="bpmJson"
                :field-options="fieldOptions"
                v-if="visible && tabActive === 'flow'"
            />
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <slot name="footer" />
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import FormView from '@/components/FormView'
import FlowView from '@/components/FlowView'

const props = defineProps({
  visible: { type: Boolean, required: true },
  formJson: { type: Object, default: () => ({}) },
  bpmJson: { type: Object, default: () => ({}) },
  nodes: { type: Array, default: () => [] },
  name: { type: String, default: '' },
  description: { type: String, default: '' },
  createdByName: { type: String, default: '' },
  deptName: { type: String, default: '' },
  level: { type: Number, default: 1 },
  // 1 流程申请 2 我的申请 3 我的待办 4 我的已办
  type: { type: Number, default: 0 },
})

const emit = defineEmits(['close', 'update:name', 'update:description', 'update:level'])

/**
 * 表单字段选项（label/value），供流程图分支条件标签翻译字段编码
 * @type {ComputedRef<{label: *, value: *, type: *}[]>}
 */
const fieldOptions = computed(() => {
  const widgetList = props.formJson?.widgetList || []
  return widgetList
      .filter(widget => widget.config?.code)
      .map(widget => ({ label: widget.config.label, value: widget.config.code, type: widget.type }))
})

/** 紧急程度选项 */
const levelOptions = [
  { value: 1, label: '一般' },
  { value: 2, label: '重要' },
  { value: 3, label: '紧急' },
]

/**
 * 节点状态 -> 时间线节点颜色类型（2审批中/3通过/4驳回/5自动通过/6等待审批）
 * @param status 节点状态
 * @returns {string} 时间线类型
 */
function getTimelineType(status) {
  const typeMap = { 1: 'info', 2: 'primary', 3: 'success', 4: 'danger', 5: 'success', 6: 'warning', 7: 'info', 8: 'info', 9: 'warning' }
  return typeMap[status] || 'info'
}

/**
 * 节点状态 -> 标签颜色类型
 * @param status 节点状态
 * @returns {string} el-tag 类型
 */
function getStatusTagType(status) {
  const typeMap = { 1: 'info', 2: '', 3: 'success', 4: 'danger', 5: 'success', 6: 'warning', 7: 'info', 8: 'info', 9: 'warning' }
  return typeMap[status] || 'info'
}

const tabActive = ref('form')
const titleFormRef = ref(null)

/** 校验标题表单 */
const validateTitle = () => {
  if (!titleFormRef.value) return true
  let valid = true
  titleFormRef.value.validate((v) => { valid = v })
  return valid
}

defineExpose({ validateTitle })

const instanceName = computed({
  get: () => props.name,
  set: (val) => emit('update:name', val)
})

const instanceDescription = computed({
  get: () => props.description,
  set: (val) => emit('update:description', val)
})

const instanceLevel = computed({
  get: () => props.level,
  set: (val) => emit('update:level', val)
})

/** 表单模型，供 el-form 必填校验取值 */
const formModel = reactive({
  name: instanceName,
  description: instanceDescription,
  createdByName: computed(() => props.createdByName),
  deptName: computed(() => props.deptName),
  level: instanceLevel,
})

function handleClose() {
  emit('close')
}
</script>

<style scoped lang="scss">
:deep(.el-drawer__body) {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding-bottom: 0;
}

.instance-info {
  padding: 12px 0 0 0;
  border-bottom: 5px solid #ebeef5;
  margin-bottom: 12px;
}

:deep(.el-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

:deep(.el-tab-pane) {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.bpmn-designer-container) {
  height: calc(100vh - 200px) !important;
}

:deep(.el-form) {
  height: calc(100vh - 200px) !important;
  min-height: 0 !important;
  overflow-y: auto;
  overflow-x: hidden;
}

.timeline-container {
  padding: 5px;
  border-right: 1px solid #ebeef5;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

// 节点信息
.node-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.node-item-header {
  display: flex;
  align-items: center;
  gap: 8px;

  .node-name {
    font-size: 14px;
    font-weight: 600;
    color: $color-text-primary;
    line-height: 1.4;
  }
}

.node-item-row {
  font-size: 12px;
  color: $color-text-secondary;
}

// 审批意见卡片
.discuss-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f5f7fa;
}

.discuss-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;

  .discuss-name {
    font-weight: 400;
    color: $color-text-primary;
  }

  .discuss-time {
    color: $color-text-secondary;
  }

  // 审批结果着色：通过/自动通过绿色，驳回红色，转办/加签灰色，撤回橙色
  .discuss-status--3,
  .discuss-status--5 {
    color: #67c23a;
  }

  .discuss-status--4 {
    color: #f56c6c;
  }

  .discuss-status--7,
  .discuss-status--8 {
    color: #909399;
  }

  .discuss-status--9 {
    color: #e6a23c;
  }
}

.discuss-content {
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>