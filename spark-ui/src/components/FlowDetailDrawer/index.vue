<template>
  <el-drawer
      :model-value="visible"
      title="流程详情"
      direction="rtl"
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
                    :key="index"
                    :type="node.status === 2 ? 'primary' : (node.status === 3 ? 'success' : 'danger')"
                >
                  <p>{{ node.name }}</p>
                  <p>{{ node.createdDt }}</p>
                  <p v-if="node.type === 'userTask'">节点状态：{{ node.statusName }}</p>
                  <p v-if="node.status === 2">待审批人：{{ node.unAssigneeName }}</p>
                  <p v-for="(discuss, index) in node.discusses" :key="index" style="font-size: 12px">
                    <p>{{ discuss.assigneeName }} - {{ discuss.createdDt }}<br>
                      审批意见: {{ discuss.discuss }}
                    </p>
                  </p>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
          <el-col :span="nodes.length > 0 ? 18 : 24">
            <flow-view
                :bpmJson="bpmJson"
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

/** 紧急程度选项 */
const levelOptions = [
  { value: 1, label: '一般' },
  { value: 2, label: '重要' },
  { value: 3, label: '紧急' },
]

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

/** 详情场景（已有申请人）时紧急程度不允许修改，仅发起流程时可选 */
const levelReadonly = computed(() => !!props.createdByName)

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
</style>