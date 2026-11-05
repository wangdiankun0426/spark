<template>
  <el-dialog
      :model-value="visible"
      :show-close="false"
      :close-on-click-modal="false"
      :before-close="handleClose"
      class="flow-detail-dialog"
      width="1180px"
      top="6vh"
      append-to-body
  >
    <!-- 头部横幅：浅色轻风格，与个人中心等弹窗统一 -->
    <template #header>
      <div class="fd-head">
        <div class="fd-ico">
          <el-icon><FlowApplication /></el-icon>
        </div>
        <div class="fd-main">
          <div class="fd-title">
            <span class="fd-title-text">{{ headTitle }}</span>
            <span class="fd-chip">{{ levelText }}</span>
          </div>
          <div class="fd-sub">
            <span class="fd-avatar">{{ avatarText }}</span>
            <span>{{ createdByName || '-' }}</span>
            <template v-if="deptName">
              <span class="fd-sep">·</span>
              <span>{{ deptName }}</span>
            </template>
            <template v-if="applyTime">
              <span class="fd-sep">·</span>
              <span>申请于 {{ applyTime }}</span>
            </template>
            <template v-if="currentNodeName">
              <span class="fd-sep">·</span>
              <span>当前环节：<b>{{ currentNodeName }}</b></span>
            </template>
          </div>
        </div>
        <div class="fd-close" @click="handleClose">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </template>

    <el-tabs v-model="tabActive" class="fd-tabs">
      <!-- 表单详情 -->
      <el-tab-pane name="form">
        <template #label>
          <span class="fd-tab-label"><el-icon><Document /></el-icon>表单详情</span>
        </template>
        <div class="fd-pane">
          <!-- 发起流程：仅保留需要录入的实例信息（申请人/部门已在头部） -->
          <div v-if="type === 1" class="fd-instance">
            <el-form ref="titleFormRef" :model="formModel" label-width="86px">
              <el-form-item label="流程标题" prop="name" :rules="titleRules">
                <el-input v-model="instanceName" placeholder="请输入流程标题" clearable />
              </el-form-item>
              <el-row :gutter="24">
                <el-col :span="8">
                  <el-form-item label="紧急程度" prop="level" :rules="levelRules">
                    <el-radio-group v-model="instanceLevel">
                      <el-radio v-for="item in levelOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="备注">
                <el-input v-model="instanceDescription" type="textarea" :rows="2" placeholder="请输入备注" clearable />
              </el-form-item>
            </el-form>
          </div>

          <!-- 自定义表单内容 -->
          <div v-if="hasCustomForm" class="fd-form" :class="{ 'is-readonly': type !== 1 }">
            <div class="fd-cap">
              <b>表单内容</b>
            </div>
            <form-view :disabled="type !== 1" :form="formJson" v-if="visible && tabActive === 'form'" />
          </div>
          <el-empty v-else-if="type !== 1" description="暂无表单内容" :image-size="60" />

          <!-- 流程备注：非发起场景固定在底部，展示申请人填写的备注 -->
          <div v-if="type !== 1" class="fd-remark">
            <div class="fd-cap"><b>备注</b></div>
            <div class="fd-remark-text" :class="{ 'is-empty': !description }">{{ description || '暂无备注' }}</div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 流程图 -->
      <el-tab-pane name="flow">
        <template #label>
          <span class="fd-tab-label">
            <el-icon><Share /></el-icon>流程图
            <span class="fd-tab-n" v-if="nodes.length">{{ nodes.length }}</span>
          </span>
        </template>
        <div class="fd-flow">
          <div class="fd-timeline" v-if="nodes.length > 0">
            <div class="fd-cap"><b>审批记录</b><span>共 {{ nodes.length }} 个环节</span></div>
            <div class="fd-tl-scroll">
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
          </div>
          <div class="fd-canvas">
            <flow-view :bpmJson="bpmJson" :field-options="fieldOptions" v-if="visible && tabActive === 'flow'" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 底部操作条：沿用各页面传入的按钮 -->
    <template #footer>
      <div class="fd-foot">
        <slot name="footer">
          <el-button @click="handleClose">关闭</el-button>
        </slot>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { Close, Document, Share } from '@element-plus/icons-vue'
import FormView from '@/components/FormView'
import FlowView from '@/components/FlowView'
import FlowApplication from '@/assets/icons/flowApplication.vue'

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
  // 弹窗标题：发起场景可传「发起流程」
  title: { type: String, default: '流程详情' },
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

/** 是否配置了自定义表单 */
const hasCustomForm = computed(() => (props.formJson?.widgetList || []).length > 0)

/** 紧急程度选项 */
const levelOptions = [
  { value: 1, label: '一般' },
  { value: 2, label: '重要' },
  { value: 3, label: '紧急' },
]

const titleRules = { required: true, message: '请输入流程标题', trigger: 'blur' }
const levelRules = { required: true, message: '请选择紧急程度', trigger: 'change' }

const levelText = computed(() => {
  const map = { 1: '一般', 2: '重要', 3: '紧急' }
  return map[props.level] || '一般'
})

/** 头像文字：取申请人首字 */
const avatarText = computed(() => (props.createdByName || '流').slice(0, 1))

/** 申请时间：取首个环节的创建时间 */
const applyTime = computed(() => props.nodes?.[0]?.createdDt || '')

/** 当前处理环节：优先取审批中的用户任务节点 */
const currentNodeName = computed(() => {
  const list = props.nodes || []
  const running = list.find(node => node.status === 2) || list.find(node => node.status === 6) || list.find(node => node.status === 1)
  return running?.name || ''
})

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

/** 头部标题：统一展示流程标题（发起场景为模板名，详情场景为流程标题），无则回退到 title prop */
const headTitle = computed(() => props.name || props.title)

/** 每次打开回到表单详情页签 */
watch(() => props.visible, (val) => {
  if (val) tabActive.value = 'form'
})

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
  level: instanceLevel,
})

function handleClose() {
  emit('close')
}
</script>
