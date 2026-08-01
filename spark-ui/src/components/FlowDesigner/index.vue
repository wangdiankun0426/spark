<template>
  <div class="bpmn-designer-container">
    <div class="designer-body">
      <div class="palette">
        <div class="palette-header">
          <el-icon><Menu /></el-icon>
          <span>流程组件</span>
        </div>
        <el-collapse :model-value="['events', 'tasks', 'gateways']" accordion>
          <el-collapse-item name="events">
            <template #title>
              <div class="collapse-title">基础节点</div>
            </template>
            <div class="palette-item" draggable="true" @dragstart="dragStart($event, 'startEvent')">
              <el-icon class="icon-start"><VideoPlay /></el-icon> <span>开始</span>
            </div>
            <div class="palette-item" draggable="true" @dragstart="dragStart($event, 'endEvent')">
              <el-icon class="icon-end"><CircleClose /></el-icon> <span>结束</span>
            </div>
          </el-collapse-item>
          <el-collapse-item name="tasks">
            <template #title>
              <div class="collapse-title">任务节点</div>
            </template>
            <div class="palette-item" draggable="true" @dragstart="dragStart($event, 'userTask')">
              <el-icon class="icon-task"><User /></el-icon> <span>用户任务</span>
            </div>
          </el-collapse-item>
          <el-collapse-item name="gateways">
            <template #title>
              <div class="collapse-title">控制网关</div>
            </template>
            <div class="palette-item" draggable="true" @dragstart="dragStart($event, 'exclusiveGateway')">
              <el-icon class="icon-gateway"><Share /></el-icon> <span>排他网关</span>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <div class="designer-right">
        <!-- 头部按钮 -->
        <div class="designer-header">
          <div>
            <el-button type="primary" @click="saveProcess">
              <el-icon><Folder /></el-icon>保存流程
            </el-button>
            <el-button type="danger" @click="showClearConfirm = true">
              <el-icon><Delete /></el-icon>清空
            </el-button>
            <el-button type="warning" @click="notificationConfigRef.open()">
              <el-icon><Bell /></el-icon>流程通知
            </el-button>
          </div>
        </div>
        <!-- 节点画布 -->
        <flow-canvas
            ref="canvasRef"
            :nodes="nodes"
            :sequences="sequences"
            :selected-node="selectedNode"
            :selected-sequence="selectedSequence"
            :canvas-width="canvasWidth"
            :canvas-height="canvasHeight"
            @select-node="selectNode"
            @select-sequence="selectSequence"
            @edit-node="editNode"
            @delete-node="deleteNode"
            @edit-sequence="editSequence"
            @delete-sequence="deleteSequence"
            @drop="handleCanvasDrop"
            @start-sequence="handleCanvasStartSequence"
        />
      </div>

      <!-- 右侧属性面板 -->
      <div class="attr-panel">
      <!-- 节点属性面板 -->
      <template v-if="selectedNode">
        <div class="panel-title">节点配置</div>
        <el-form :model="selectedNode" label-width="auto">
          <el-form-item label="节点ID">
            <el-input v-model="selectedNode.id" readonly></el-input>
          </el-form-item>
          <el-form-item label="节点名称">
            <el-input v-model="selectedNode.name"></el-input>
          </el-form-item>

          <assignee-selector
              v-if="selectedNode.type === 'userTask'"
              :node="selectedNode"
              :field-options="fieldOptions"
          />
          <permission-config
              v-if="selectedNode.type === 'userTask'"
              :node="selectedNode"
          />
        </el-form>
      </template>

      <!-- 连线属性面板 -->
      <template v-else-if="selectedSequence">
        <div class="panel-title">连线配置</div>
        <el-form :model="selectedSequence" label-width="auto">
          <el-form-item label="连线ID">
            <el-input v-model="selectedSequence.id" readonly></el-input>
          </el-form-item>
          <el-form-item label="连线名称">
            <el-input v-model="selectedSequence.name" readonly></el-input>
          </el-form-item>
          <el-form-item label="源节点">
            <el-input :value="getSourceNodeName(selectedSequence.sourceRef)" readonly></el-input>
          </el-form-item>
          <el-form-item label="目标节点">
            <el-input :value="getTargetNodeName(selectedSequence.targetRef)" readonly></el-input>
          </el-form-item>
          <el-form-item label="分支条件" v-if="isConnectionFromGateway(selectedSequence)">
            <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
              <!-- 字段选择器 -->
              <el-select
                  v-model="conditionField"
                  placeholder="选择字段"
                  style="width: 100%;"
                  @change="updateConditionExpression()"
              >
                <el-option
                    v-for="item in fieldOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                    :disabled="item.disabled"
                />
              </el-select>
              <!-- 操作符选择器 -->
              <el-select
                  v-model="conditionOperator"
                  placeholder="选择操作符"
                  style="width: 100%;"
                  @change="updateConditionExpression()"
              >
                <el-option
                    v-for="item in operatorOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                    :disabled="item.disabled"
                />
              </el-select>
              <!-- 值输入框 -->
              <el-input
                  v-model="conditionValue"
                  placeholder="请输入值"
                  style="width: 100%;"
                  @input="updateConditionExpression()"
              ></el-input>
            </div>
            <!-- 显示最终生成的表达式 -->
            <div style="margin-top: 10px; font-size: 12px; color: #666;">
              生成表达式: {{ formatConditionExpression() }}
            </div>
          </el-form-item>

          <el-form-item v-else>
            <span style="color: #999;">非网关分支，无需设置条件</span>
          </el-form-item>
        </el-form>
      </template>

      <el-empty v-else description="请选择节点或连线进行配置" :image-size="60" />
    </div>
  </div>

    <!-- 清空确认对话框 -->
    <el-dialog
        title="确认清空"
        v-model="showClearConfirm"
        width="30%"
        :show-close="false"
    >
      <p>确定要清空整个流程图吗？此操作不可撤销。</p>
      <template #footer>
        <el-button @click="showClearConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmClear">确定</el-button>
      </template>
    </el-dialog>

    <!-- 流程通知 -->
    <NoticeConfig ref="notificationConfigRef" :config="noticeConfig" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {useRoute} from "vue-router";
import {createTemplateVersionAPI, queryTemplateVersionDetailAPI} from "@/api/flow/templateVersion.js";
import {Bell, CircleClose, Delete, Folder, Share, VideoPlay} from "@element-plus/icons-vue";
import {queryFormFieldListAPI} from "@/api/form/formField.js";
import AssigneeSelector from './AssigneeSelector.vue'
import PermissionConfig from './PermissionConfig.vue'
import FlowCanvas from './FlowCanvas.vue'
import NoticeConfig from './NoticeConfig.vue'

const templateId = ref(0);
const revId = ref(0);
const fieldOptions = ref([]);
const operatorOptions = [
  {
    label: "等于",
    value: "==",
  },
  {
    label: "大于",
    value: ">",
  },
  {
    label: "小于",
    value: "<",
  },
  {
    label: "大于等于",
    value: ">=",
  },
  {
    label: "小于等于",
    value: "<=",
  },
  {
    label: "不等于",
    value: "!=",
  },
]
// 审批人类型选项
const assigneeTypeOptions = [
  { label: '流程发起人', value: '1' },
  { label: '系统自动通过', value: '2' },
  { label: '指定用户', value: '3' },
  { label: '指定部门', value: '4' },
  { label: '指定角色', value: '5' },
  { label: '表单数据', value: '6' }
]
onMounted(() => {
  const params = useRoute().params;
  templateId.value = Number(params.id);
  revId.value = Number(params.revId);
  const query = {
    id: revId.value
  }
  queryTemplateVersionDetailAPI(query).then(res => {
    if (res.code === 200) {
      if (res.data.bpmJson !== undefined) {
        const bpmJson = JSON.parse(res.data.bpmJson);
        nodes.value = bpmJson.nodes || [];
        sequences.value = bpmJson.sequences || [];
        noticeConfig.value = bpmJson.notices ? bpmJson.notices : noticeConfig.value;
        calcCanvasSize();
      }
      getFormFieldList(res.data.formId);
    }
  }).catch(() => {})
})

/**
 * 获取表单字段列表
 **/
const getFormFieldList = (formId) => {
  const query = {
    formId: formId
  };
  queryFormFieldListAPI(query).then(res => {
    if (res.code === 200 && res.data !== undefined) {
      fieldOptions.value = res.data.map(item => {
        return {
          label: item.label,
          value: item.code,
          type: item.type
        }
      });
    }
  }).catch(() => {})
}


// 响应式状态
const nodes = ref([])
const sequences = ref([])
const selectedNode = ref(null)
const selectedSequence = ref(null)
const canvasRef = ref(null)
const canvasWidth = ref(2000)
const canvasHeight = ref(1000)
const showClearConfirm = ref(false)

// 流程通知
const notificationConfigRef = ref(null)
const noticeConfig = ref([
  { type: 2, label: '待办通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，请及时审批！', recipient: '#{base:appAssignee}#' },
  { type: 3, label: '完结通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，已审批完结！', recipient: '#{base:appUser}#' },
  { type: 4, label: '驳回通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，已被驳回！', recipient: '#{base:appUser}#' }
])

/**
 * 根据节点位置计算画布尺寸
 */
const calcCanvasSize = () => {
  const PADDING = 200;
  const nodeWidths = {
    exclusiveGateway: 80,
    startEvent: 48,
    endEvent: 48,
  };
  const nodeHeights = {
    exclusiveGateway: 50,
    startEvent: 48,
    endEvent: 48,
  };
  const defaultNodeWidth = 110;
  const defaultNodeHeight = 72;

  let maxX = 0, maxY = 0;

  if (nodes.value.length > 0) {
    nodes.value.forEach(node => {
      const w = nodeWidths[node.type] || defaultNodeWidth;
      const h = nodeHeights[node.type] || defaultNodeHeight;
      maxX = Math.max(maxX, node.x + w);
      maxY = Math.max(maxY, node.y + h);
    });
  }
  canvasWidth.value = Math.max(maxX + PADDING, 800);
  canvasHeight.value = Math.max(maxY + PADDING, 600);
};

// 节点变化时重新计算画布尺寸
watch(() => nodes.value.length, () => {
  calcCanvasSize();
});

// 控制按钮隐藏延迟
const elementHideTimers = ref({})
const sequenceHideTimers = ref({})

// 节点配置
const nodeConfig = {
  startEvent: { icon: 'video-play', label: '开始', color: '#52c41a', bgColor: '#f6ffed' },
  endEvent: { icon: 'circle-close', label: '结束', color: '#f5222d', bgColor: '#fff1f0' },
  userTask: { icon: 'user', label: '用户任务', color: '#1890ff', bgColor: '#e6f7ff' },
  exclusiveGateway: { icon: 'share', label: '排他网关', color: '#fa8c16', bgColor: '#fffbe6' }
}

// 拖拽开始
const dragStart = (event, type) => {
  event.dataTransfer.setData("node-type", type)
  event.dataTransfer.setData("node-name", nodeConfig[type]?.label || type)
}

// 放置节点
const handleCanvasDrop = ({ nodeType, x, y }) => {
  if (!nodeType) {
    return;
  }
  const nodeName = nodeConfig[nodeType]?.label || nodeType
  const width = nodeType === 'exclusiveGateway' ? 80 : (nodeType.includes('Event') ? 48 : 110)
  const height = nodeType === 'exclusiveGateway' ? 50 : (nodeType.includes('Event') ? 48 : 72)
  const newNode = {
    id: `node_${Date.now()}`,
    type: nodeType,
    x: x - width / 2,
    y: y - height / 2,
    name: nodeName,
    assigneeType: nodeType === 'userTask' ? '' : undefined,
    assignee: nodeType === 'userTask' ? '' : undefined,
    assigneeLabel: nodeType === 'userTask' ? '' : undefined,
    permission: nodeType === 'userTask' ? 7 : undefined,
    showControls: false
  }
  nodes.value.push(newNode)
}

// 从 FlowCanvas 拖拽连线完成后创建新连线
const handleCanvasStartSequence = ({ sourceId, targetId }) => {
  const newSequence = {
    id: `sequence_${Date.now()}`,
    sourceRef: sourceId,
    targetRef: targetId,
    name: '',
    conditionExpression: '',
    showControls: false
  }
  sequences.value.push(newSequence)
  ElMessage.success('连线创建成功')
}

// 选择节点
const selectNode = (node) => {
  selectedNode.value = node
  selectedSequence.value = null
}

// 编辑节点
const editNode = (node) => {
  selectedNode.value = node
  selectedSequence.value = null
}

// 删除节点
const deleteNode = (id) => {
  nodes.value = nodes.value.filter(el => el.id !== id)
  sequences.value = sequences.value.filter(conn =>
      conn.sourceRef !== id && conn.targetRef !== id
  )
  if (selectedNode.value?.id === id) {
    selectedNode.value = null
  }
  // 清除相关的隐藏定时器
  if (elementHideTimers.value[id]) {
    clearTimeout(elementHideTimers.value[id])
    delete elementHideTimers.value[id]
  }
}

// 选择连线
const selectSequence = (sequence) => {
  selectedSequence.value = sequence
  selectedNode.value = null
}

// 编辑连线
const editSequence = (sequence) => {
  selectedSequence.value = sequence
  selectedNode.value = null
}

// 删除连线
const deleteSequence = (id) => {
  sequences.value = sequences.value.filter(seq => seq.id !== id)
  selectedSequence.value = null
  ElMessage.success('连线已删除')
  // 清除相关的隐藏定时器
  if (sequenceHideTimers.value[id]) {
    clearTimeout(sequenceHideTimers.value[id])
    delete sequenceHideTimers.value[id]
  }
}

// 生成 BPMN JSON
const generateBpmnJson = () => ({
  nodes: nodes.value.map(el => ({
    id: el.id,
    type: el.type,
    name: el.name,
    x: el.x,
    y: el.y,
    assigneeType: el.assigneeType,
    assignee: el.assignee,
    assigneeLabel: el.assigneeLabel,
    permission: el.permission,
  })),
  sequences: sequences.value.map(seq => ({
    id: seq.id,
    sourceRef: seq.sourceRef,
    targetRef: seq.targetRef,
    name: seq.name,
    conditionExpression: seq.conditionExpression,
  })),
  notices: noticeConfig.value
})

// 保存流程（控制台输出）
const saveProcess = () => {
  const json = generateBpmnJson()
  const data = {
    templateId: templateId.value,
    bpmJson: JSON.stringify(json)
  }
  createTemplateVersionAPI(data).then(res => {
    if (res.code === 200) {
      ElMessage.success("流程模板保存成功");
    }
  }).catch(() => {})
}

// 确认清空
const confirmClear = () => {
  nodes.value = []
  sequences.value = []
  selectedNode.value = null
  selectedSequence.value = null
  showClearConfirm.value = false
  ElMessage.success('流程图已清空')
}

// 判断连线是否来自网关
const isConnectionFromGateway = (sequence) => {
  const sourceNode = nodes.value.find(el => el.id === sequence.sourceRef)
  return sourceNode && sourceNode.type === 'exclusiveGateway'
}

// 获取源节点名称
const getSourceNodeName = (sourceRef) => {
  const node = nodes.value.find(el => el.id === sourceRef)
  return node ? node.name : '未知节点'
}

// 获取目标节点名称
const getTargetNodeName = (targetRef) => {
  const node = nodes.value.find(el => el.id === targetRef)
  return node ? node.name : '未知节点'
}

const conditionField = ref('')
const conditionOperator = ref('')
const conditionValue = ref('')

// 监听选中的连线变化 解析现有的条件表达式（当选择连线时）
watch(selectedSequence, (newVal) => {
  if (newVal && newVal.conditionExpression) {
    parseConditionExpression(newVal.conditionExpression)
  } else {
    conditionField.value = ''
    conditionOperator.value = ''
    conditionValue.value = ''
  }
})

// 解析现有表达式的方法
const parseConditionExpression = (expression) => {
  const match = expression.match(/\$\{([^}]+)\}/)
  if (match) {
    const condition = match[1]
    const parts = condition.split(/(>=|<=|==|>|<|!=)/)
    if (parts.length >= 3) {
      conditionField.value = parts[0].trim()
      conditionOperator.value = parts[1].trim()
      conditionValue.value = parts[2].trim()
    }
  }
}

// 更新条件表达式
const updateConditionExpression = (field, operator, value) => {
  const f = field ?? conditionField.value
  const o = operator ?? conditionOperator.value
  const v = value ?? conditionValue.value
  if (selectedSequence.value) {
    selectedSequence.value.conditionExpression = formatConditionExpression(f, o, v)
    selectedSequence.value.name = formatConditionName(f, o, v)
  }
}

// 格式化条件表达式
const formatConditionExpression = (field, operator, value) => {
  const f = field ?? conditionField.value
  const o = operator ?? conditionOperator.value
  const v = value ?? conditionValue.value
  if (f && o && v) {
    return `\${${f} ${o} ${v}}`
  }
  return ''
}

// 格式化连线名称
const formatConditionName = (field, operator, value) => {
  const f = field ?? conditionField.value
  const o = operator ?? conditionOperator.value
  const v = value ?? conditionValue.value
  const fieldName = fieldOptions.value.find(el => el.value === f)
  const operatorName = operatorOptions.find(el => el.value === o)
  if (fieldName?.label && operatorName?.label && v) {
    return `${fieldName.label} ${operatorName.label} ${v}`
  }
  return ''
}

// 获取节点的四个边框连接点
const getNodeConnectionPoints = (node) => {
  const x = node.x
  const y = node.y
  let width = 110
  let height = 72

  if (node.type === 'exclusiveGateway') {
    width = 80
    height = 50
    return {
      top:    { x: x + width / 2, y: y },
      bottom: { x: x + width / 2, y: y + height },
      left:   { x: x, y: y + height / 2 },
      right:  { x: x + width, y: y + height / 2 }
    }
  }

  if (node.type.includes('Event')) {
    width = 48
    height = 48
  }

  if (node.type === 'userTask') {
    height = 60
  }

  return {
    left: { x: x, y: y + height / 2 },
    right: { x: x + width, y: y + height / 2 },
    top: { x: x + width / 2, y: y },
    bottom: { x: x + width / 2, y: y + height }
  }
}


</script>

<style scoped lang="scss">
.bpmn-designer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  color: #333;
  overflow: hidden;
}

.designer-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.designer-right {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.designer-header {
  padding: 10px 20px;
  background: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  z-index: 100;
}

/* 右侧属性面板 */
.attr-panel {
  width: 300px;
  flex-shrink: 0;
  border-left: 1px solid $border-color-light;
  background: $bg-card;
  padding: $spacing-md;
  overflow-y: auto;

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }
}

.palette {
  width: 140px;
  background: #fdfdfd;
  border-right: 1px solid #ebeef5;
  padding: 12px;
  overflow-y: auto;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.02);
}

.palette-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  margin-bottom: 15px;
  border-bottom: 2px solid #409eff;
  color: #303133;
  font-weight: bold;
  font-size: 15px;
}

.collapse-title {
  font-weight: 600;
  color: #606266;
  font-size: 13px;
}

:deep(.el-collapse) {
  border: none;
}

:deep(.el-collapse-item__header) {
  height: 44px;
  line-height: 44px;
  border: none;
  background: transparent;
}

:deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}

.palette-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  margin-bottom: $spacing-xs;
  background: #fff;
  border: 1px dashed $border-color;
  border-radius: $border-radius-sm;
  cursor: grab;
  user-select: none;
  transition: $transition-fast;
}

.palette-item:active {
  cursor: grabbing;
}

.palette-item:hover {
  border-color: $color-primary;
  color: $color-primary;
  background: $color-primary-soft;
}

.palette-item i {
  font-size: 16px;
  transition: none;
}

.palette-item:hover i {
  transform: none;
}

.icon-start { color: #52c41a; }
.icon-end { color: #f5222d; }
.icon-task { color: #1890ff; }
.icon-gateway { color: #fa8c16; }
.icon-ai { color: #722ed1; }

.canvas {
  flex: 1;
  position: relative;
  background: #fff;
  overflow: auto;
  min-height: 500px;
}

.bpmn-node {
  position: absolute;
  z-index: 30;
  padding: 6px 8px;
  border: 1.5px solid #b3d8ff;
  border-left: 3px solid #b3d8ff;
  border-radius: 8px;
  background: #ffffff;
  cursor: move;
  user-select: none;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  color: #333;
  height: 50px;
  width: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.bpmn-node:hover {
  border-color: #c0c4cc;
  border-left-color: #409eff;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.bpmn-startEvent:hover {
  border-left-color: #67c23a;
}
.bpmn-endEvent:hover {
  border-left-color: #f56c6c;
}
.bpmn-exclusive-gateway:hover {
  border-left-color: #e6a23c !important;
}

.bpmn-startEvent, .bpmn-endEvent {
  width: 110px;
  height: 50px;
  border-radius: 8px;
  padding: 6px 8px;
}

.bpmn-startEvent {
  border-color: #b7eb8f;
  border-left-color: #67c23a;
  background: #ffffff;
}
.bpmn-endEvent {
  border-color: #fab6b6;
  border-left-color: #f56c6c;
  background: #ffffff;
}
.bpmn-userTask {
  border-color: #a0cfff;
  border-left-color: #409eff;
  min-height: 50px;
  height: auto;
}

.selected-node {
  border-width: 2px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}
.bpmn-startEvent.selected-node {
  border-color: #67c23a;
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.15);
}
.bpmn-endEvent.selected-node {
  border-color: #f56c6c;
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.15);
}
.bpmn-userTask.selected-node {
  border-color: #409eff;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.15);
}
.bpmn-exclusive-gateway.selected-node {
  border-color: #e6a23c !important;
  box-shadow: 0 6px 20px rgba(230, 162, 60, 0.15) !important;
}

.bpmn-exclusive-gateway {
  border: 1.5px solid #f3d19e !important;
  border-left: 3px solid #e6a23c !important;
  background: #ffffff !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06) !important;
  width: 110px;
  height: 50px;
  padding: 6px 8px;
  transform: none;
}

.bpmn-exclusive-gateway:hover {
  border-color: #f3d19e !important;
  border-left-color: #e6a23c !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1) !important;
}

.bpmn-exclusive-gateway .node-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.bpmn-exclusive-gateway .node-content span {
  word-break: break-all;
  white-space: pre-wrap;
  font-size: 12px;
  line-height: 1.2;
  max-width: 80px;
}

.node-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 100%;
  height: 100%;
}

.node-main-icon {
  font-size: 16px;
  color: #606266;
  margin-bottom: 0;
}

.bpmn-startEvent .node-main-icon {
  color: #67c23a;
}
.bpmn-endEvent .node-main-icon {
  color: #f56c6c;
}
.bpmn-userTask .node-main-icon {
  color: #409eff;
}

.gateway-icon {
  font-size: 16px;
  color: #e6a23c;
  margin-bottom: 0;
}

.node-name {
  font-size: 10px;
  color: #303133;
  font-weight: 600;
  line-height: 1.2;
  word-break: break-all;
  max-width: 90px;
}

.gateway-name {
  font-size: 11px;
  max-width: 90px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
  word-break: break-all;
}

.node-info {
  margin-top: 1px;
  max-width: 100px;
  word-break: break-all;
  white-space: pre-wrap;
  line-height: 1.3;
}

/* 审批人类型样式 */
.assignee-type {
  background-color: #75adea;
  color: white;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;
  max-width: 100%;
  word-break: break-all;
  display: inline-block;
}

.node-controls {
  position: absolute;
  top: -40px;
  left: 0;
  z-index: 1000;
  display: flex;
  gap: 5px;
  background: white;
  padding: 5px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.sequence-point {
  position: absolute;
  width: 10px;
  height: 10px;
  background: #1890ff;
  border-radius: 50%;
  cursor: crosshair;
  z-index: 100;
  opacity: 0;
  transition: opacity 0.2s;
}

.sequence-point:hover { opacity: 1; }

/* 五个方向的连接点位置定位 */
.left-point   { top: 50%; left: -5px; transform: translateY(-50%); }
.right-point  { top: 50%; right: -5px; transform: translateY(-50%); }
.top-point    { top: -5px; left: 50%; transform: translateX(-50%); }
.bottom-point { bottom: -5px; left: 50%; transform: translateX(-50%); }

.sequences-svg {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 20;
}
.sequences-svg path {
  pointer-events: visibleStroke;
}
.sequences-svg path:hover {
  stroke: #1890ff;
  stroke-width: 3;
  cursor: pointer;
}

.selected-sequence {
  stroke: #fa541c !important;
  stroke-width: 3px !important;
}

.sequence-controls {
  position: absolute;
  z-index: 1001;
  display: flex;
  gap: 5px;
  background: white;
  padding: 5px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.sequence-hit-area {
  cursor: pointer;
}

.sequences-svg path:not(.sequence-hit-area):hover {
  stroke: #1890ff !important;
  stroke-width: 3;
}

.sequence-label {
  pointer-events: all;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.8);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.sequence-label:hover {
  background: rgba(24, 144, 255, 0.1);
}
</style>



