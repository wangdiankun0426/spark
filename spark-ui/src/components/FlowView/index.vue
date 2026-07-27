<template>
  <div class="bpmn-designer-container">
    <div class="canvas"
         @drop="drop"
         @dragover="allowDrop"
         @click="canvasClick"
         ref="canvasRef">
      <!-- 流程节点 -->
      <div
          v-for="node in nodes"
          :key="node.id"
          class="bpmn-node"
          :class="getNodeClass(node.type)"
          :style="{ top: node.y + 'px', left: node.x + 'px' }"
          @click.stop="selectNode(node)"
          @mousedown="startDragNode($event, node)"
          @mouseenter="showNodeControls(node)"
          @mouseleave="delayHideNodeControls(node)"
      >
        <!-- 排他网关使用 SVG 绘制扁平菱形 -->
        <div v-if="node.type === 'exclusiveGateway'" class="node-content">
          <svg width="80" height="50" viewBox="0 0 80 50" style="position: absolute; top:0; left:0; z-index: -1;">
            <defs>
              <linearGradient id="gatewayGradientView" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#ffffff;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#fff7e6;stop-opacity:1" />
              </linearGradient>
            </defs>
            <path d="M 40 1.5 L 78.5 25 L 40 48.5 L 1.5 25 Z" fill="url(#gatewayGradientView)" stroke="#e6a23c" stroke-width="2" />
          </svg>
          <el-icon class="gateway-icon"><Share /></el-icon>
          <span class="gateway-name">{{ node.name }}</span>
        </div>
        <div v-else class="node-content">
          <el-icon class="node-main-icon">
            <component :is="getNodeIcon(node.type)" />
          </el-icon>
          <span class="node-name">{{ node.name }}</span>
          <div v-if="node.type === 'userTask'" class="node-info">
            <span class="assignee-type">
              {{ getAssigneeTypeLabel(node.assigneeType) }}
            </span>
          </div>
        </div>

        <!-- 节点控制按钮 -->
        <div
            v-if="node.showControls"
            @mouseenter="keepNodeControls(node)"
            @mouseleave="delayHideNodeControls(node)"
        >
        </div>
      </div>
      <!-- 连线 SVG -->
      <svg class="sequences-svg" :width="canvasWidth" :height="canvasHeight">
        <defs>
          <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
            <polygon points="0 0, 10 3.5, 0 7" fill="#666" />
          </marker>
        </defs>

        <!-- 1. 热区路径（不可见，但可点击，宽度 12px） -->
        <path
            v-for="sequence in sequences"
            :key="'hit-' + sequence.id"
            :d="getPolylinePath(sequence)"
            fill="none"
            stroke="transparent"
            stroke-width="12"
            class="sequence-hit-area"
            @click.stop="selectSequence(sequence)"
            @mouseenter="showSequenceControls(sequence)"
            @mouseleave="delayHideSequenceControls(sequence)"
        />
        <!-- 2. 视觉路径（真实显示的线，2px 宽） -->
        <path
            v-for="sequence in sequences"
            :key="sequence.id"
            :d="getPolylinePath(sequence)"
            stroke="#666"
            stroke-width="2"
            fill="none"
            marker-end="url(#arrowhead)"
            :class="{ 'selected-sequence': selectedSequence && selectedSequence.id === sequence.id }"
        />
        <!-- 3. 分支条件文本 -->
        <text
            v-for="sequence in sequences"
            :key="'text-' + sequence.id"
            :x="getSequenceTextPosition(sequence).x"
            :y="getSequenceTextPosition(sequence).y"
            fill="#333"
            font-size="12"
            text-anchor="middle"
            dominant-baseline="middle"
            class="sequence-label"
            @click.stop="selectSequence(sequence)"
            @mouseenter="showSequenceControls(sequence)"
            @mouseleave="delayHideSequenceControls(sequence)"
        >
          {{ sequence.name }}
        </text>
        <!-- 临时连线（拖拽连线时显示） -->
        <path
            v-if="draggingSequence"
            :d="getDraggingPolylinePath()"
            stroke="#1890ff"
            stroke-width="2"
            fill="none"
            marker-end="url(#arrowhead)"
            stroke-dasharray="5,5"
        />
      </svg>
      <!-- 连线控制按钮 -->
      <div
          v-for="sequence in sequences"
          :key="'controls-' + sequence.id"
          v-show="sequence.showControls"
          :style="{
              top: getSequenceMidPoint(sequence).y - 20 + 'px',
              left: getSequenceMidPoint(sequence).x - 40 + 'px'
            }"
          @mouseenter="keepSequenceControls(sequence)"
          @mouseleave="delayHideSequenceControls(sequence)"
      >
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {useRoute} from "vue-router";
import {createTemplateVersionAPI, queryTemplateVersionDetailAPI} from "@/api/flow/templateVersion.js";
import {CircleClose, Delete, Folder, Share, VideoPlay} from "@element-plus/icons-vue";
import {queryFormFieldListAPI} from "@/api/form/formField.js";
import {pageUserListAPI} from "@/api/system/user.js";

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
const userList = ref([])

const props = defineProps({
  bpmJson: Object
});

onMounted(() => {
  nodes.value = props.bpmJson.nodes.map(el => ({
    id: el.id,
    type: el.type,
    name: el.name,
    x: el.x,
    y: el.y,
    assigneeType: el.assigneeType,
    assignee: el.assignee,
    assigneeArray: el.assigneeArray
  }))
  sequences.value = props.bpmJson.sequences.map(seq => ({
    id: seq.id,
    sourceRef: seq.sourceRef,
    targetRef: seq.targetRef,
    name: seq.name,
    conditionExpression: seq.conditionExpression
  }))
});

// 响应式状态
const nodes = ref([])
const sequences = ref([])
const selectedNode = ref(null)
const selectedSequence = ref(null)
const showProperties = ref(false)
const canvasRef = ref(null)
const canvasWidth = ref(2000)
const canvasHeight = ref(1000)
const showClearConfirm = ref(false)

// 连线状态
const draggingSequence = ref(null) // 拖拽连线状态
const sourceNode = ref(null) // 源节点

// 控制按钮隐藏延迟
const elementHideTimers = ref({})
const sequenceHideTimers = ref({})

// 节点配置（已移除 serviceTask 和 parallelGateway）
const nodeConfig = {
  startEvent: { icon: 'video-play', label: '开始', color: '#52c41a', bgColor: '#f6ffed' },
  endEvent: { icon: 'circle-close', label: '结束', color: '#f5222d', bgColor: '#fff1f0' },
  userTask: { icon: 'user', label: '用户任务', color: '#1890ff', bgColor: '#e6f7ff' },
  exclusiveGateway: { icon: 'share', label: '排他网关', color: '#fa8c16', bgColor: '#fffbe6' }
}

// 审批人类型选项
const assigneeTypeOptions = [
  { label: '指定用户', value: '1' },
  { label: '流程发起人', value: '2' },
  { label: '自动通过', value: '3' }
]

// 获取审批人类型标签
const getAssigneeTypeLabel = (type) => {
  const find = assigneeTypeOptions.find(item => item.value === type);
  if (!find) {
    return '未选择审批人';
  }
  return find.label;
}

// 获取节点图标
const getNodeIcon = (type) => {
  const iconName = nodeConfig[type]?.icon || 'question'
  // 首字母大写以匹配 Element Plus 图标组件名
  return iconName.charAt(0).toUpperCase() + iconName.slice(1)
}

// 获取节点类名
const getNodeClass = (type) => {
  return `bpmn-${type.replace('Gateway', '-gateway')}`
}

// 获取两个节点之间的最佳连接锚点
const getAnchorPoints = (source, target) => {
  const sWidth = source.type === 'exclusiveGateway' ? 80 : (source.type.includes('Event') ? 48 : 110);
  const sHeight = source.type === 'exclusiveGateway' ? 50 : (source.type.includes('Event') ? 48 : 72);
  const tWidth = target.type === 'exclusiveGateway' ? 80 : (target.type.includes('Event') ? 48 : 110);
  const tHeight = target.type === 'exclusiveGateway' ? 50 : (target.type.includes('Event') ? 48 : 72);

  const sPoints = getNodeConnectionPoints(source);
  const tPoints = getNodeConnectionPoints(target);
  
  // 计算间距判断
  const verticalClearance = target.y - (source.y + sHeight);
  const revVerticalClearance = source.y - (target.y + tHeight);
  const horizontalClearance = target.x - (source.x + sWidth);
  const revHorizontalClearance = source.x - (target.x + tWidth);
  
  let sourcePoint, targetPoint;
  
  // 优先级 1: 垂直分离（最常见的流程方向）
  if (verticalClearance > 20) {
    sourcePoint = { ...sPoints.bottom, dir: 'bottom' };
    targetPoint = { ...tPoints.top, dir: 'top' };
  } else if (revVerticalClearance > 20) {
    sourcePoint = { ...sPoints.top, dir: 'top' };
    targetPoint = { ...tPoints.bottom, dir: 'bottom' };
  } 
  // 优先级 2: 水平分离
  else if (horizontalClearance > 20) {
    sourcePoint = { ...sPoints.right, dir: 'right' };
    targetPoint = { ...tPoints.left, dir: 'left' };
  } else if (revHorizontalClearance > 20) {
    sourcePoint = { ...sPoints.left, dir: 'left' };
    targetPoint = { ...tPoints.right, dir: 'right' };
  } 
  // 兜底方案：基于中心点距离，但仍使用 clearance 逻辑的备选
  else {
    const dx = (target.x + tWidth/2) - (source.x + sWidth/2);
    const dy = (target.y + tHeight/2) - (source.y + sHeight/2);
    if (Math.abs(dx) > Math.abs(dy)) {
      if (dx > 0) {
        sourcePoint = { ...sPoints.right, dir: 'right' };
        targetPoint = { ...tPoints.left, dir: 'left' };
      } else {
        sourcePoint = { ...sPoints.left, dir: 'left' };
        targetPoint = { ...tPoints.right, dir: 'right' };
      }
    } else {
      if (dy > 0) {
        sourcePoint = { ...sPoints.bottom, dir: 'bottom' };
        targetPoint = { ...tPoints.top, dir: 'top' };
      } else {
        sourcePoint = { ...sPoints.top, dir: 'top' };
        targetPoint = { ...tPoints.bottom, dir: 'bottom' };
      }
    }
  }

  // 这里的 copy 以便修改
  const sp = { ...sourcePoint };
  const tp = { ...targetPoint };

  // 自动对齐：如果节点在 20px 范围内接近对齐，则强制拉直
  if (sp.dir === 'top' || sp.dir === 'bottom') {
    if (Math.abs(sp.x - tp.x) < 20) {
      tp.x = sp.x;
    }
  } else {
    if (Math.abs(sp.y - tp.y) < 20) {
      tp.y = sp.y;
    }
  }
  
  return { sourcePoint: sp, targetPoint: tp };
}

// 获取折线路径（替代原来的直线路径）
const getPolylinePath = (sequence) => {
  const source = nodes.value.find(el => el.id === sequence.sourceRef)
  const target = nodes.value.find(el => el.id === sequence.targetRef)

  if (!source || !target) return ''
  
  const { sourcePoint, targetPoint } = getAnchorPoints(source, target);

  const sp = sourcePoint;
  const tp = targetPoint;

  // 如果已经完美对齐，直接返回直线
  if (sp.x === tp.x || sp.y === tp.y) {
    return `M ${sp.x} ${sp.y} L ${tp.x} ${tp.y}`;
  }

  // 保证起始和终点都有一个小段的“向外”位移，避免穿透
  const offset = 20;
  if (sp.dir === 'left' || sp.dir === 'right') {
    // 水平出发：M -> (Sp.x + offset, Sp.y) -> (Sp.x + offset, Tp.y) -> Tp
    // 自动选取一个合适的拐点，通常是两者中心 X 坐标
    let midX = (sp.x + tp.x) / 2;
    // 强制 midX 在 source/target 的外部
    if (sp.dir === 'right') midX = Math.max(midX, sp.x + offset);
    else midX = Math.min(midX, sp.x - offset);
    
    if (tp.dir === 'right') midX = Math.max(midX, tp.x + offset);
    else if (tp.dir === 'left') midX = Math.min(midX, tp.x - offset);

    return `M ${sp.x} ${sp.y} L ${midX} ${sp.y} L ${midX} ${tp.y} L ${tp.x} ${tp.y}`;
  } else {
    // 垂直出发
    let midY = (sp.y + tp.y) / 2;
    if (sp.dir === 'bottom') midY = Math.max(midY, sp.y + offset);
    else midY = Math.min(midY, sp.y - offset);
    
    if (tp.dir === 'bottom') midY = Math.max(midY, tp.y + offset);
    else if (tp.dir === 'top') midY = Math.min(midY, tp.y - offset);

    return `M ${sp.x} ${sp.y} L ${sp.x} ${midY} L ${tp.x} ${midY} L ${tp.x} ${tp.y}`;
  }
}

// 修改拖拽连线路径函数
const getDraggingPolylinePath = () => {
  if (!draggingSequence.value) return ''
  const { startX, startY, endX, endY, dir } = draggingSequence.value

  if (dir === 'left' || dir === 'right') {
    const midX = (startX + endX) / 2
    return `M ${startX} ${startY} L ${midX} ${startY} L ${midX} ${endY} L ${endX} ${endY}`
  } else {
    const midY = (startY + endY) / 2
    return `M ${startX} ${startY} L ${startX} ${midY} L ${endX} ${midY} L ${endX} ${endY}`
  }
}

// 拖拽开始
const dragStart = (event, type) => {
  event.dataTransfer.setData("node-type", type)
  event.dataTransfer.setData("node-name", nodeConfig[type]?.label || type)
}

// 允许拖放
const allowDrop = (event) => {
  event.preventDefault()
}

// 放置节点
const drop = (event) => {
  event.preventDefault()
  const nodeType = event.dataTransfer.getData("node-type")
  if (!nodeType) {
    return;
  }
  const rect = canvasRef.value.getBoundingClientRect()
  const x = event.clientX - rect.left - 50
  const y = event.clientY - rect.top - 25
  const newNode = {
    id: `node_${Date.now()}`,
    type: nodeType,
    x: x,
    y: y,
    name: nodeConfig[nodeType]?.label || nodeType,
    assigneeType: nodeType === 'userTask' ? '' : undefined,
    assignee: nodeType === 'userTask' ? '' : undefined,
    assigneeArray: nodeType === 'userTask' ? [] : undefined,
    // 默认不显示控制按钮
    showControls: false
  }
  nodes.value.push(newNode)
}

// 拖动节点
const startDragNode = (event, node) => {
  if (sourceNode.value) {
    // 如果正在拖拽连线，则不能拖动节点
    return
  }
  const startX = event.clientX
  const startY = event.clientY
  const nodeX = node.x
  const nodeY = node.y
  const onMouseMove = (moveEvent) => {
    const dx = moveEvent.clientX - startX
    const dy = moveEvent.clientY - startY
    node.x = nodeX + dx
    node.y = nodeY + dy
  }
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 开始连线 - 从节点开始拖拽
const startSequence = (event, node, position) => {
  event.stopPropagation()
  sourceNode.value = node
  const rect = canvasRef.value.getBoundingClientRect()

  // 根据位置获取正确的连接点
  const points = getNodeConnectionPoints(node)
  const startPoint = points[position] || points.right

  draggingSequence.value = {
    startX: startPoint.x,
    startY: startPoint.y,
    endX: event.clientX - rect.left,
    endY: event.clientY - rect.top,
    dir: position
  }

  // 添加鼠标移动和释放事件监听器
  const onMouseMove = (moveEvent) => {
    const rect = canvasRef.value.getBoundingClientRect()
    draggingSequence.value.endX = moveEvent.clientX - rect.left
    draggingSequence.value.endY = moveEvent.clientY - rect.top
  }

  const onMouseUp = (upEvent) => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    // 检查是否释放到了另一个节点上
    const targetNode = findNodeAtPosition(upEvent.clientX, upEvent.clientY)
    if (targetNode && targetNode.id !== sourceNode.value.id) {
      // 创建连线
      const newSequence = {
        id: `sequence_${Date.now()}`,
        sourceRef: sourceNode.value.id,
        targetRef: targetNode.id,
        name: '',
        conditionExpression: '', // 默认空条件
        showControls: false // 默认不显示控制按钮
      }
      sequences.value.push(newSequence)
      ElMessage.success('连线创建成功')
    } else if (targetNode && targetNode.id === sourceNode.value.id) {
      ElMessage.warning('不能连接到自己')
    }
    // 清除连线状态
    draggingSequence.value = null
    sourceNode.value = null
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 查找指定位置的节点
const findNodeAtPosition = (clientX, clientY) => {
  const rect = canvasRef.value.getBoundingClientRect()
  const x = clientX - rect.left
  const y = clientY - rect.top
  // 从后往前遍历，确保找到最上层的节点
  for (let i = nodes.value.length - 1; i >= 0; i--) {
    const node = nodes.value[i]
    if (x >= node.x && x <= node.x + 100 && y >= node.y && y <= node.y + 50) {
      return node
    }
  }
  return null
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
  showProperties.value = true
}

// 删除节点
const deleteNode = (id) => {
  nodes.value = nodes.value.filter(el => el.id !== id)
  sequences.value = sequences.value.filter(conn =>
      conn.sourceRef !== id && conn.targetRef !== id
  )
  if (selectedNode.value?.id === id) {
    selectedNode.value = null
    showProperties.value = false
  }
  // 清除相关的隐藏定时器
  if (elementHideTimers.value[id]) {
    clearTimeout(elementHideTimers.value[id])
    delete elementHideTimers.value[id]
  }
}

// 计算连线中点用于删除按钮定位
const getSequenceMidPoint = (sequence) => {
  const sourceEl = nodes.value.find(el => el.id === sequence.sourceRef)
  const targetEl = nodes.value.find(el => el.id === sequence.targetRef)
  if (sourceEl && targetEl) {
    const { sourcePoint, targetPoint } = getAnchorPoints(sourceEl, targetEl);
    return { x: (sourcePoint.x + targetPoint.x) / 2, y: (sourcePoint.y + targetPoint.y) / 2 };
  }
  return { x: 0, y: 0 }
}

// 选择连线
const selectSequence = (sequence) => {
  selectedSequence.value = sequence
  selectedNode.value = null
  showProperties.value = true
}

// 编辑连线
const editSequence = (sequence) => {
  selectedSequence.value = sequence
  selectedNode.value = null
  showProperties.value = true
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

// 获取连线路径
const getSequencePath = (sequence) => {
  const source = nodes.value.find(el => el.id === sequence.sourceRef)
  const target = nodes.value.find(el => el.id === sequence.targetRef)
  if (source && target) {
    return `M ${source.x + 100} ${source.y + 25} L ${target.x} ${target.y + 25}`
  }
  return ''
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
    assigneeArray: el.assigneeArray,
  })),
  sequences: sequences.value.map(seq => ({
    id: seq.id,
    sourceRef: seq.sourceRef,
    targetRef: seq.targetRef,
    name: seq.name,
    conditionExpression: seq.conditionExpression,
  }))
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
  showProperties.value = false
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

// 显示节点控制按钮
const showNodeControls = (node) => {
  node.showControls = true
  // 清除可能存在的隐藏定时器
  if (elementHideTimers.value[node.id]) {
    clearTimeout(elementHideTimers.value[node.id])
    delete elementHideTimers.value[node.id]
  }
}

// 延迟隐藏节点控制按钮
const delayHideNodeControls = (node) => {
  // 如果已存在定时器，先清除
  if (elementHideTimers.value[node.id]) {
    clearTimeout(elementHideTimers.value[node.id])
  }
  // 设置新的定时器
  elementHideTimers.value[node.id] = setTimeout(() => {
    node.showControls = false
    delete elementHideTimers.value[node.id]
  }, 200)
}

// 保持节点控制按钮显示
const keepNodeControls = (node) => {
  // 清除定时器，保持显示
  if (elementHideTimers.value[node.id]) {
    clearTimeout(elementHideTimers.value[node.id])
    delete elementHideTimers.value[node.id]
  }
}

// 显示连线控制按钮
const showSequenceControls = (sequence) => {
  sequence.showControls = true
  // 清除可能存在的隐藏定时器
  if (sequenceHideTimers.value[sequence.id]) {
    clearTimeout(sequenceHideTimers.value[sequence.id])
    delete sequenceHideTimers.value[sequence.id]
  }
}

// 延迟隐藏连线控制按钮
const delayHideSequenceControls = (sequence) => {
  // 如果已存在定时器，先清除
  if (sequenceHideTimers.value[sequence.id]) {
    clearTimeout(sequenceHideTimers.value[sequence.id])
  }
  // 设置新的定时器
  sequenceHideTimers.value[sequence.id] = setTimeout(() => {
    sequence.showControls = false
    delete sequenceHideTimers.value[sequence.id]
  }, 200)
}

// 保持连线控制按钮显示
const keepSequenceControls = (sequence) => {
  // 清除定时器，保持显示
  if (sequenceHideTimers.value[sequence.id]) {
    clearTimeout(sequenceHideTimers.value[sequence.id])
    delete sequenceHideTimers.value[sequence.id]
  }
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
  if (newVal) {
    showProperties.value = true
  }
})

// 解析现有表达式的方法
const parseConditionExpression = (expression) => {
  // 解析 ${field > value} 格式的表达式
  const match = expression.match(/\$\{([^}]+)\}/)
  if (match) {
    const condition = match[1]
    // 按操作符长度降序排列，避免短操作符先匹配的问题（例如>=被误识别为=）
    const parts = condition.split(/(>=|<=|==|>|<|!=)/)
    if (parts.length >= 3) {
      conditionField.value = parts[0].trim()
      conditionOperator.value = parts[1].trim()
      conditionValue.value = parts[2].trim()
    }
  }
}

// 更新条件表达式
const updateConditionExpression = () => {
  if (selectedSequence.value) {
    selectedSequence.value.conditionExpression = formatConditionExpression()
    selectedSequence.value.name = formatConditionName();
  }
}

// 格式化条件表达式
const formatConditionExpression = () => {
  if (conditionField.value && conditionOperator.value && conditionValue.value) {
    return `\${${conditionField.value} ${conditionOperator.value} ${conditionValue.value}}`
  }
  return ''
}

// 格式化连线名称
const formatConditionName = () => {
  const fieldName = fieldOptions.value.find(el => el.value === conditionField.value)
  const operatorName = operatorOptions.find(el => el.value === conditionOperator.value)
  if (fieldName.label && operatorName.label && conditionValue.value) {
    return `${fieldName.label} ${operatorName.label} ${conditionValue.value}`
  }
  return ''
}

/**
 * 用户选择
 * @param value
 */
const handleChangeUserAssignee = (value) => {
  selectedNode.value.assignee = Array.isArray(value) ? value.join(',') : value
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

  return {
    left: { x: x, y: y + height / 2 },
    right: { x: x + width, y: y + height / 2 },
    top: { x: x + width / 2, y: y },
    bottom: { x: x + width / 2, y: y + height }
  }
}

// 添加获取连线文本位置的函数
const getSequenceTextPosition = (sequence) => {
  const mid = getSequenceMidPoint(sequence);
  return {
    x: mid.x,
    y: mid.y - 10 // 稍微向上偏移，避免与连线重叠
  }
}

</script>

<style scoped>
.bpmn-designer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  color: #333;
}

.designer-header {
  width: 100%;
  padding: 10px 20px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 100;
}

.designer-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.palette {
  width: 120px;
  background: white;
  padding: 15px;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

.palette h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.palette-item {
  padding: 8px 10px;
  margin: 6px 10px;
  background: #fafafa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: move;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  color: #333;
  font-size: 13px; /* 减小字体 */
}

.palette-item:hover {
  background: #e6f3ff;
  border-color: #1890ff;
  transform: translateX(2px);
}

.canvas {
  flex: 1;
  position: relative;
  background: #fff;
  overflow: auto;
  min-height: 500px;
}

.canvas {
  flex: 1;
  position: relative;
  background: #fff;
  overflow: auto;
  min-height: 500px;
}

.bpmn-node {
  position: absolute;
  z-index: 30; /* 保证在连线之上可点击 */
  padding: 8px 10px;
  border: 1.5px solid #b3d8ff;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
  cursor: move;
  user-select: none;
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 82, 204, 0.08);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  color: #333;
  height: 72px;
  width: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.bpmn-node:hover {
  border-color: #409eff;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.2);
  transform: translateY(-1px);
}

.bpmn-startEvent, .bpmn-endEvent {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  padding: 5px;
}

.bpmn-startEvent { 
  border-color: #95d475; 
  background: linear-gradient(135deg, #ffffff 0%, #f0f9eb 100%); 
}
.bpmn-endEvent { 
  border-color: #fab6b6; 
  background: linear-gradient(135deg, #ffffff 0%, #fef0f0 100%); 
}
.bpmn-userTask { 
  border-color: #a0cfff; 
}
.bpmn-exclusive-gateway {
  border: none !important;
  background: none !important;
  box-shadow: none !important;
  width: 80px;
  height: 50px;
  padding: 0;
  transform: none;
}

/* 移除之前的 ::before，改为直接用 SVG 绘图 */
.bpmn-exclusive-gateway::before {
  display: none;
}

.bpmn-exclusive-gateway:hover svg path {
  stroke-width: 3;
  filter: drop-shadow(0 0 4px rgba(230, 162, 60, 0.4));
}

.bpmn-exclusive-gateway .node-content {
  transform: none;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: static;
}
.bpmn-exclusive-gateway .node-content span {
  font-size: 11px;
  max-width: 45px;
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
  gap: 4px;
  width: 100%;
  height: 100%;
}

.node-main-icon {
  font-size: 18px;
  color: #606266;
  margin-bottom: 2px;
}

.gateway-icon {
  font-size: 16px;
  color: #e6a23c;
  margin-bottom: 0px;
}

.node-name {
  font-size: 11px;
  color: #303133;
  font-weight: 600;
  line-height: 1.2;
  word-break: break-all;
  max-width: 90px;
}

.gateway-name {
  font-size: 10px;
  max-width: 50px;
  font-weight: 600;
  color: #303133;
}

.node-info {
  margin-top: 2px;
}

/* 审批人类型样式 */
.assignee-type {
  background-color: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
  padding: 1px 4px;
  border-radius: 4px;
  font-size: 9px;
  font-weight: normal;
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

/* 四个方向的连接点 */
.start-point {
  top: 50%;
  left: -5px;
  transform: translateY(-50%);
}

.end-point {
  top: 50%;
  right: -5px;
  transform: translateY(-50%);
}

/* 左侧连接点 */
.left-point {
  top: 50%;
  left: -5px;
  transform: translateY(-50%);
}

/* 右侧连接点 */
.right-point {
  top: 50%;
  right: -5px;
  transform: translateY(-50%);
}

/* 上方连接点 */
.top-point {
  top: -5px;
  left: 50%;
  transform: translateX(-50%);
}

/* 下方连接点 */
.bottom-point {
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
}

.sequences-svg {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none; /* 允许穿透点击到 path */
  z-index: 20; /* 👈 提高层级 */
}
.sequences-svg path {
  pointer-events: visibleStroke; /* 每个 path 可点击 */
}
.sequences-svg path:hover {
  stroke: #1890ff;
  stroke-width: 3;
  cursor: pointer;
}
.selected-sequence {
  stroke: #1890ff !important;
  stroke-width: 3px !important;
}

/* 热区路径：不可见，但可点击 */
.sequence-hit-area {
  cursor: pointer;
}

/* 视觉路径：hover 时变色，提升可发现性 */
.sequences-svg path:not(.sequence-hit-area):hover {
  stroke: #1890ff !important;
  stroke-width: 3;
}

/* 选中状态 */
.selected-sequence {
  stroke: #fa541c !important;
  stroke-width: 3 !important;
}

.sequence-label:hover {
  background: rgba(24, 144, 255, 0.1);
}
</style>



