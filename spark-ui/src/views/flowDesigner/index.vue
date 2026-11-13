<template>
  <div class="flow-designer">
    <!--顶部工具栏-->
    <div class="designer-toolbar">
      <div class="toolbar-left">
         <span class="toolbar-logo">
          <el-icon :size="22"><FlowApplication /></el-icon>
        </span>
        <span class="toolbar-title">{{ basicInfo.name || '新建流程' }}</span>
      </div>
      <div class="toolbar-right">
        <el-button @click="previewVisible = true">
          <el-icon><View /></el-icon>流程图预览
        </el-button>
        <el-button @click="notificationConfigRef.open()">
          <el-icon><Bell /></el-icon>流程通知
        </el-button>
        <el-button @click="showClearConfirm = true">
          <el-icon><Delete /></el-icon>清空流程
        </el-button>
        <el-button @click="handleOpenJson">
          <el-icon><Tickets /></el-icon>JSON数据
        </el-button>
        <el-button v-hasPermi="['flow:templateVersion:create']" type="primary" @click="saveProcess">
          <el-icon><Folder /></el-icon>保存流程
        </el-button>
      </div>
    </div>

    <!--主体：左侧基础信息 + 画布（属性面板为画布上的浮层）-->
    <div class="designer-body">
      <basic-info-panel :info="basicInfo" :form-options="formOptions" />

      <div class="canvas-wrap" ref="canvasWrapRef">
        <canvas-index
            ref="canvasRef"
            :nodes="nodes"
            :sequences="sequences"
            source-key="sourceRef"
            target-key="targetRef"
            :selected-node-id="selectedNode?.id"
            :canvas-width="canvasWidth"
            :canvas-height="canvasHeight"
            :node-components="NODE_COMPONENTS"
            :field-options="fieldOptions"
            :lanes="lanes"
            :hidden-edge-ids="hiddenEdges"
            auto-layout
            @select-node="selectNode"
            @select-branch="selectBranch"
            @node-contextmenu="handleNodeContextMenu"
            @add-after="handleAddAfter"
            @add-branch="handleAddBranch"
            @zoom-change="handleZoomChange"
        />

        <!--左下角缩放条：与 workflow 设计器一致-->
        <div class="canvas-zoombar" @mousedown.stop>
          <span class="zoom-btn" title="缩小" @click="canvasRef?.zoomOut()">
            <el-icon :size="12"><Minus /></el-icon>
          </span>
          <span class="zoom-value" title="恢复 100%" @click="canvasRef?.resetZoom()">{{ zoomPercent }}%</span>
          <span class="zoom-btn" title="放大" @click="canvasRef?.zoomIn()">
            <el-icon :size="12"><Plus /></el-icon>
          </span>
        </div>

        <!--右侧属性面板：选中节点/分支才滑出，不挤压画布-->
        <property-drawer
            v-if="selectedNode || selectedBranch"
            :node="selectedNode"
            :branch="selectedBranch"
            :nodes="nodes"
            :field-options="fieldOptions"
            :node-tasks="nodeTaskConfig"
            @delete-node="deleteNode"
            @delete-branch="confirmDeleteBranch"
            @close="closePanel"
        />
      </div>
    </div>

    <!--底部状态栏-->
    <div class="designer-statusbar">
      <span>节点 {{ nodes.length }}</span>
      <i class="status-divider"></i>
      <span>连线 {{ sequences.length }}</span>
      <i class="status-divider"></i>
      <el-tooltip :disabled="!dagCheck.issues.length" placement="top">
        <template #content>
          <div v-for="(item, index) in dagCheck.issues" :key="index" class="dag-check-tip-line">· {{ item }}</div>
        </template>
        <span class="status-check" :class="`status-${dagCheck.level}`">{{ dagCheck.text }}</span>
      </el-tooltip>
      <span class="status-right">版本 {{ revNum ? 'v' + revNum : '新流程模板' }}</span>
    </div>

    <!--节点右键菜单-->
    <node-context-menu
        v-model:visible="menu.visible"
        :x="menu.x"
        :y="menu.y"
        :node="menu.node"
        :insertable="insertableNodes"
        @pick="insertAfterNode"
    />

    <!--流程图预览：只读，仅展示画布中的流程图-->
    <el-dialog
        v-model="previewVisible"
        title="流程图预览"
        width="860px"
        top="8vh"
        append-to-body
        destroy-on-close
    >
      <flow-preview
          v-if="previewVisible"
          class="preview-canvas"
          :bpm-json="previewJson"
          :field-options="fieldOptions"
      />
    </el-dialog>

    <!--清空确认对话框-->
    <el-dialog
        title="确认清空"
        v-model="showClearConfirm"
        width="30%"
    >
      <p>确定要清空整个流程图吗？清空后只保留固定的开始与结束节点。</p>
      <template #footer>
        <el-button @click="showClearConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmClear">确定</el-button>
      </template>
    </el-dialog>

    <!--流程通知-->
    <NoticeConfig ref="notificationConfigRef" :config="noticeConfig" />

    <!--流程JSON：可复制给他人，也可粘贴其他流程的JSON后应用到当前画布-->
    <el-drawer
        v-model="jsonVisible"
        title="流程 JSON"
        direction="ltr"
        :size="920"
        append-to-body
    >
      <div class="json-box">
        <el-input
            v-model="jsonText"
            type="textarea"
            :rows="26"
            placeholder="打开时填入当前流程JSON，可复制到其他流程模板，也可粘贴其他流程的JSON后点击应用"
        />
      </div>
      <template #footer>
        <el-button plain @click="handleCopyJson"><el-icon><CopyDocument /></el-icon>复制当前</el-button>
        <el-button type="primary" @click="handleApplyJson"><el-icon><Check /></el-icon>应用</el-button>
      </template>
    </el-drawer>

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, provide, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from "vue-router";
import { createTemplateVersionAPI, queryTemplateVersionDetailAPI } from "@/api/flow/templateVersion.js";
import { queryTemplateDetailAPI, updateTemplateAPI } from "@/api/flow/template.js";
import { queryFormFieldListAPI } from "@/api/form/formField.js";
import { queryFormListAPI } from "@/api/form/form.js";
import CanvasIndex from '@/views/flowDesigner/canvas/index.vue'
import NoticeConfig from './NoticeConfig.vue'
import PropertyDrawer from './PropertyDrawer.vue'
import BasicInfoPanel from './BasicInfoPanel.vue'
import NodeContextMenu from './NodeContextMenu.vue'
import FlowPreview from '@/components/FlowPreview/index.vue'
import { NODE_COMPONENTS, NODE_META } from '@/views/flowDesigner/nodes/index.js'
import { FLOW_VARS_KEY, buildVarGroups } from './var/flowVariables.js'
import { layoutFlow, applyPositions } from '@/views/flowDesigner/utils/flowLayout.js'
import { validateFlowDag } from '@/views/flowDesigner/utils/flowValidate.js'
import FlowApplication from "@/assets/icons/flowApplication.vue";

const templateId = ref(0);
const revId = ref(0);
const revNum = ref('');
const fieldOptions = ref([]);

/** 模板基础信息：与流程图一起随「保存流程」提交 */
const basicInfo = reactive({
  name: '',
  formId: null,
  type: 1,
  status: 1,
  remark: ''
});

/** 流程表单下拉项 */
const formOptions = ref([]);

/** 变量下拉分组：基础变量恒在，表单值随绑定表单变化，下发给任务参数与流程通知 */
const varGroups = computed(() => buildVarGroups(fieldOptions.value));
provide(FLOW_VARS_KEY, varGroups);

const nodes = ref([])
const sequences = ref([])
const selectedNode = ref(null)
/** 选中的分支：{ edge, gatewayId, joinId }，由泳道条件标签点击产生 */
const selectedBranch = ref(null)
const canvasWidth = ref(2000)
const canvasHeight = ref(1000)
/** 分支泳道条件标签 + 空分支占位卡 */
const lanes = ref([])
/** 空分支占位连线：不绘制，避免出现默认连到结束节点的线 */
const hiddenEdges = ref([])
const showClearConfirm = ref(false)
const previewVisible = ref(false)
const jsonVisible = ref(false)
const jsonText = ref('')

/** 底部状态栏 DAG 校验：结构错误优先，其次是空分支这类编辑过程中的待配置项 */
const dagCheck = computed(() => validateFlowDag(nodes.value, sequences.value, { lanes: lanes.value }))

const notificationConfigRef = ref(null)
const noticeConfig = ref([
  { type: 2, label: '待办通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，请及时审批！', recipient: '#{base:appAssignee}#' },
  { type: 3, label: '完结通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，已审批完结！', recipient: '#{base:appUser}#' },
  { type: 4, label: '驳回通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，已被驳回！', recipient: '#{base:appUser}#' },
  { type: 5, label: '催办通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，正在催促您审批，请及时处理！', recipient: '#{base:appAssignee}#' }
])

const nodeTaskConfig = ref([])

/* ---------------- 布局 ---------------- */

/** 画布可视区宽度：作为布局最小宽度，保证内容始终水平居中 */
const canvasWrapRef = ref(null)
/** 画布组件实例：左下角缩放条调用其 zoomIn / zoomOut / resetZoom */
const canvasRef = ref(null)
/** 当前缩放百分比，由画布 zoom-change 回传 */
const zoomPercent = ref(100)

/** 缩放比例变化：同步左下角百分比文案 */
function handleZoomChange(value) {
  zoomPercent.value = Math.round(value * 100)
}
const wrapWidth = ref(0)
let wrapObserver = null

/**
 * 重新计算布局：节点坐标、分支泳道、画布尺寸全部由流程顺序推导
 */
const applyLayout = () => {
  if (!nodes.value.length) {
    lanes.value = []
    hiddenEdges.value = []
    canvasWidth.value = Math.max(wrapWidth.value, 520)
    canvasHeight.value = 600
    return
  }
  const result = layoutFlow(nodes.value, sequences.value, {
    fieldOptions: fieldOptions.value,
    minWidth: wrapWidth.value
  })
  applyPositions(nodes.value, result.positions)
  lanes.value = result.lanes
  hiddenEdges.value = result.hiddenEdges
  canvasWidth.value = result.width
  canvasHeight.value = result.height
}

/**
 * 分支条件变化（属性面板里改条件）时重排泳道：
 * 泳道条件标签的文案与宽度都由表达式推导，不重排会一直显示旧文案/不显示
 */
watch(
    () => sequences.value.map(seq => seq.conditionExpression).join('|'),
    () => applyLayout()
)

/* ---------------- 节点与连线的基础操作 ---------------- */

/** 生成唯一 id */
const nextId = (prefix) => `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`

/** 新建连线对象 */
const newSequence = (sourceRef, targetRef) => ({
  id: nextId('s'),
  sourceRef,
  targetRef,
  name: '',
  conditionExpression: ''
})

/** 按类型创建节点（字段与旧数据结构保持一致，后端无需改造） */
const createNode = (type) => {
  const meta = NODE_META[type] || {}
  const node = {
    id: nextId('n'),
    type,
    name: meta.name || type,
    x: 0,
    y: 0
  }
  if (type === 'userTask') {
    node.assigneeType = ''
    node.assignee = ''
    node.assigneeLabel = ''
    node.approveType = '1'
    node.urgeEnabled = false
    node.urgeInterval = 8
    node.permission = 15
  }
  return node
}

/** 出边 / 入边 */
const outEdgesOf = (id) => sequences.value.filter(seq => seq.sourceRef === id)
const inEdgesOf = (id) => sequences.value.filter(seq => seq.targetRef === id)

/**
 * 空白模板兜底：默认创建开始 + 结束 + 一条连线
 */
const ensureDefaultNodes = () => {
  if (nodes.value.length) return
  const start = createNode('startEvent')
  const end = createNode('endEvent')
  nodes.value = [start, end]
  sequences.value = [newSequence(start.id, end.id)]
}

/** 点选节点 */
const selectNode = (node) => {
  selectedNode.value = node
  selectedBranch.value = null
}

/**
 * 点选分支（泳道条件标签）：面板只认分支，连线本身不再是点击对象
 * @param lane 布局输出的泳道（含 edgeId / gatewayId / joinId）
 */
const selectBranch = (lane) => {
  const edge = lane?.edgeId ? sequences.value.find(seq => seq.id === lane.edgeId) : null
  if (!edge) return
  selectedBranch.value = { edge, gatewayId: lane.gatewayId, joinId: lane.joinId }
  selectedNode.value = null
}

const closePanel = () => {
  selectedNode.value = null
  selectedBranch.value = null
}

/* ---------------- 右键菜单：在节点后添加 ---------------- */

/** 可插入的节点类型 */
const insertableNodes = ['userTask', 'exclusiveGateway'].map(type => ({
  type,
  name: NODE_META[type]?.name || type,
  icon: NODE_META[type]?.icon,
  color: NODE_META[type]?.color,
  bgColor: NODE_META[type]?.bgColor
}))

const menu = reactive({ visible: false, x: 0, y: 0, node: null, branch: null })

/** 打开节点右键菜单 */
const openMenu = (node, x, y) => {
  menu.node = node
  menu.branch = null
  menu.x = x
  menu.y = y
  menu.visible = true
  if (node) selectNode(node)
}

/** 画布节点右键 */
const handleNodeContextMenu = ({ node, event }) => {
  openMenu(node, event.clientX, event.clientY)
}

/** 节点下沿 ＋ 快捷入口：等价于右键 */
const handleAddAfter = (node, event) => {
  openMenu(node, event.clientX, event.clientY)
}

/** 空分支占位卡点击：为当前分支添加节点 */
const handleAddBranch = (lane, event) => {
  const gateway = nodes.value.find(n => n.id === lane.gatewayId)
  if (!gateway) return
  openMenu(gateway, event.clientX, event.clientY)
  menu.branch = lane
}

/** 分支的汇合节点：优先用布局推导的汇合点，兜底为结束节点 */
const joinIdOf = (lane) => {
  if (lane.joinId && nodes.value.some(n => n.id === lane.joinId)) return lane.joinId
  const end = nodes.value.find(n => n.type === 'endEvent')
  return end && end.id !== lane.gatewayId ? end.id : null
}

/**
 * 往空分支里放第一个节点：改写该分支的占位连线，再补一条 新节点→汇合点
 * @param lane 空分支（含 gatewayId / edgeId / joinId）
 * @param node 新建节点
 */
const fillBranch = (lane, node) => {
  const joinId = joinIdOf(lane)
  const edge = lane.edgeId ? sequences.value.find(seq => seq.id === lane.edgeId) : null
  if (edge) {
    edge.targetRef = node.id
  } else {
    sequences.value.push(newSequence(lane.gatewayId, node.id))
  }
  if (joinId) sequences.value.push(newSequence(node.id, joinId))
}

/**
 * 插入条件分支：原链路的后续节点成为汇合点，直接生成两条空分支
 * （两条占位连线都不绘制，节点补进来后再改写，避免默认连一条线到结束节点）
 * @param target 插入位置的前一个节点
 * @param gateway 新建的分支节点
 */
const insertGatewayAfter = (target, gateway) => {
  const end = nodes.value.find(n => n.type === 'endEvent')
  const outs = outEdgesOf(target.id)
  const joinId = outs.length === 1 ? outs[0].targetRef : (end ? end.id : null)
  if (outs.length === 1) {
    // 原「目标 → 后继」改为「分支节点 → 后继」，成为第一条空分支的占位连线
    outs[0].sourceRef = gateway.id
  }
  sequences.value.push(newSequence(target.id, gateway.id))
  if (joinId && joinId !== gateway.id) {
    // 第二条空分支同样指向汇合点
    sequences.value.push(newSequence(gateway.id, joinId))
  }
}

/**
 * 在目标节点后插入新节点，连线自动生成
 * 结束节点插到它之前，分支节点则新增一条泳道
 * @param type 节点类型
 */
const insertAfterNode = (type) => {
  const target = menu.node
  const branch = menu.branch
  menu.visible = false
  menu.branch = null
  if (!type || !target) return

  const node = createNode(type)
  nodes.value.push(node)

  if (branch) {
    // 从空分支占位卡进入：节点直接落到该分支
    fillBranch(branch, node)
  } else if (target.type === 'endEvent') {
    // 插在结束节点之前：所有汇入结束的连线改指新节点，再补一条 新节点→结束
    inEdgesOf(target.id).forEach(seq => { seq.targetRef = node.id })
    sequences.value.push(newSequence(node.id, target.id))
  } else if (target.type === 'exclusiveGateway') {
    // 分支节点右键：新增一条泳道，末端汇入结束节点
    const end = nodes.value.find(n => n.type === 'endEvent')
    sequences.value.push(newSequence(target.id, node.id))
    if (end) sequences.value.push(newSequence(node.id, end.id))
  } else if (type === 'exclusiveGateway') {
    insertGatewayAfter(target, node)
  } else {
    const outs = outEdgesOf(target.id)
    if (outs.length === 1) {
      // 原 目标→后继 改为 新节点→后继，再补 目标→新节点
      outs[0].sourceRef = node.id
    }
    sequences.value.push(newSequence(target.id, node.id))
  }

  applyLayout()
  selectNode(node)
}

/**
 * 删除节点（静默）：前后自动桥接，桥接不了的连到结束节点
 * @param id 节点 id
 */
const removeNodeSilently = (id) => {
  const sources = [...new Set(inEdgesOf(id).map(seq => seq.sourceRef))]
  const targets = [...new Set(outEdgesOf(id).map(seq => seq.targetRef))]
  const end = nodes.value.find(n => n.type === 'endEvent')

  nodes.value = nodes.value.filter(n => n.id !== id)
  sequences.value = sequences.value.filter(seq => seq.sourceRef !== id && seq.targetRef !== id)

  // 出边为空（悬空节点）时统一接到结束节点，避免流程断链
  const bridges = targets.length ? targets : (end && end.id !== id ? [end.id] : [])
  sources.forEach(source => {
    bridges.forEach(target => {
      if (source === target) return
      const exists = sequences.value.some(seq => seq.sourceRef === source && seq.targetRef === target)
      if (!exists) sequences.value.push(newSequence(source, target))
    })
  })

  nodeTaskConfig.value = nodeTaskConfig.value.filter(task => task.nodeId !== id)
}

/**
 * 删除节点：前后自动桥接，桥接不了的连到结束节点
 * @param id 节点 id
 */
const deleteNode = (id) => {
  const node = nodes.value.find(n => n.id === id)
  if (!node) return
  if (node.type === 'startEvent' || node.type === 'endEvent') {
    ElMessage.warning('开始与结束节点不可删除')
    return
  }
  removeNodeSilently(id)
  if (selectedNode.value?.id === id) selectedNode.value = null
  applyLayout()
  ElMessage.success('节点已删除')
}

/**
 * 收集分支链路上的节点：从分支首个节点沿单出边走到汇合点为止
 * @param startId 分支连线的目标节点
 * @param joinId 汇合节点 id
 * @returns {Array} 该分支独占的节点（不含汇合点）
 */
const collectBranchChain = (startId, joinId) => {
  const chain = []
  let cur = nodes.value.find(n => n.id === startId)
  let guard = 0
  while (cur && guard++ < 100) {
    if (cur.id === joinId) break
    // 开始/结束节点与已收集节点不再往下走，避免脏数据死循环
    if (cur.type === 'startEvent' || cur.type === 'endEvent') break
    if (chain.some(n => n.id === cur.id)) break
    chain.push(cur)
    const outs = outEdgesOf(cur.id)
    if (outs.length !== 1) break
    cur = nodes.value.find(n => n.id === outs[0].targetRef)
  }
  return chain
}

/**
 * 删除整条分支：移除分支上的节点与连线，分支节点与汇合点之间留一条空分支
 * （空分支在画布上渲染为「添加节点」占位卡）
 */
const deleteBranch = () => {
  const branch = selectedBranch.value
  if (!branch?.edge) return
  selectedBranch.value = null
  const chain = collectBranchChain(branch.edge.targetRef, branch.joinId)
  if (!chain.length) {
    // 空分支：去掉占位连线，布局会自动补一张占位卡
    sequences.value = sequences.value.filter(seq => seq.id !== branch.edge.id)
  } else {
    chain.forEach(node => removeNodeSilently(node.id))
  }
  applyLayout()
  ElMessage.success('分支已删除')
}

/** 删除分支前二次确认：会连带删除该分支上的节点 */
const confirmDeleteBranch = () => {
  ElMessageBox.confirm('该分支上的节点与连线都会被移除，确定继续？', '删除分支', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(() => deleteBranch()).catch(() => {})
}

/* ---------------- 数据加载与保存 ---------------- */

onMounted(() => {
  const params = useRoute().params;
  templateId.value = Number(params.id);
  // 路由带版本号时取指定版本，不带版本号时取模板当前版本
  if (params.revId) revId.value = Number(params.revId);

  if (canvasWrapRef.value) {
    wrapObserver = new ResizeObserver(entries => {
      const width = entries[0].contentRect.width
      if (Math.abs(width - wrapWidth.value) < 2) return
      wrapWidth.value = width
      applyLayout()
    })
    wrapObserver.observe(canvasWrapRef.value)
    wrapWidth.value = canvasWrapRef.value.clientWidth
  }

  loadFormOptions();

  queryTemplateDetailAPI({ id: templateId.value }).then(res => {
    if (res.code === 200 && res.data) {
      const detail = res.data;
      basicInfo.name = detail.name || '';
      basicInfo.formId = detail.formId ?? null;
      basicInfo.type = detail.type ?? 1;
      basicInfo.status = detail.status ?? 1;
      basicInfo.remark = detail.remark || '';
      if (detail.revNum) revNum.value = detail.revNum;
      // 未指定版本号时回落到模板当前版本
      if (!revId.value) revId.value = Number(detail.revId);
    }
    loadTemplateVersion();
  }).catch(() => {
    loadTemplateVersion();
  })
})

onBeforeUnmount(() => {
  wrapObserver?.disconnect()
})

/** 加载流程表单下拉项（与模板列表口径一致：type=2） */
const loadFormOptions = () => {
  queryFormListAPI({ type: 2, page: false }).then(res => {
    if (res.code !== 200 || !res.data) return
    const list = Array.isArray(res.data) ? res.data : (res.data.records || res.data.list || [])
    formOptions.value = list.map(item => ({ label: item.name, value: item.id }))
  }).catch(() => {})
}

/** 加载流程模板版本内容 */
const loadTemplateVersion = () => {
  if (!revId.value) {
    ensureDefaultNodes()
    applyLayout()
    return
  }
  queryTemplateVersionDetailAPI({ id: revId.value }).then(res => {
    if (res.code !== 200 || !res.data) {
      ensureDefaultNodes()
      applyLayout()
      return
    }
    // 版本号取版本自身，避免打开历史版本时误显模板当前版本号
    if (res.data.revNum) revNum.value = res.data.revNum;
    if (res.data.bpmJson !== undefined) {
      const bpmJson = JSON.parse(res.data.bpmJson);
      nodes.value = bpmJson.nodes || [];
      sequences.value = bpmJson.sequences || [];
      noticeConfig.value = bpmJson.notices ? bpmJson.notices : noticeConfig.value;
      nodeTaskConfig.value = bpmJson.nodeTasks || [];
      // 基础信息缺失时用版本上的表单兜底
      if (!basicInfo.formId && res.data.formId) basicInfo.formId = res.data.formId;
      ensureDefaultNodes();
      applyLayout();
    }
    getFormFieldList(res.data.formId);
  }).catch(() => {
    ensureDefaultNodes()
    applyLayout()
  })
}

const getFormFieldList = (formId) => {
  if (!formId) return;
  queryFormFieldListAPI({ formId }).then(res => {
    if (res.code === 200 && res.data !== undefined) {
      fieldOptions.value = res.data.map(item => ({ label: item.label, value: item.code, type: item.type }));
      applyLayout();
    }
  }).catch(() => {})
}

/** 生成节点任务配置：过滤未选模板或节点已删除的任务，并按节点内顺序生成执行顺序 */
const generateNodeTasks = () => {
  const validTasks = nodeTaskConfig.value.filter(task => task.taskTemplateId && nodes.value.some(el => el.id === task.nodeId))
  const sortCount = {}
  return validTasks.map(task => {
    sortCount[task.nodeId] = (sortCount[task.nodeId] || 0) + 1
    return { ...task, sort: sortCount[task.nodeId] }
  })
}

const generateBpmnJson = () => ({
  nodes: nodes.value.map(el => ({ id: el.id, type: el.type, name: el.name, x: el.x, y: el.y, assigneeType: el.assigneeType, assignee: el.assignee, assigneeLabel: el.assigneeLabel, approveType: el.approveType, urgeEnabled: el.urgeEnabled === true, urgeInterval: el.urgeInterval, permission: el.permission })),
  sequences: sequences.value.map(seq => ({ id: seq.id, sourceRef: seq.sourceRef, targetRef: seq.targetRef, name: seq.name, conditionExpression: seq.conditionExpression })),
  notices: noticeConfig.value,
  // 节点任务配置
  nodeTasks: generateNodeTasks()
})

/* ---------------- JSON 数据 ---------------- */

/** 打开 JSON 抽屉：填入当前编辑态数据（与保存提交的内容一致） */
const handleOpenJson = () => {
  jsonText.value = JSON.stringify(generateBpmnJson(), null, 2)
  jsonVisible.value = true
}

/** 复制当前流程 JSON */
const handleCopyJson = () => {
  const text = JSON.stringify(generateBpmnJson(), null, 2)
  const done = () => ElMessage.success('已复制流程JSON')
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallbackCopy(text, done))
  } else {
    fallbackCopy(text, done)
  }
}

/** 剪贴板不可用时的兜底复制 */
const fallbackCopy = (text, done) => {
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  try {
    document.execCommand('copy')
    done()
  } finally {
    document.body.removeChild(ta)
  }
}

/** 应用 JSON：整体覆盖当前节点/连线/通知/节点任务，再重排画布 */
const handleApplyJson = () => {
  let json
  try {
    json = JSON.parse(jsonText.value)
  } catch (e) {
    ElMessage.warning('JSON格式有误，请检查后重试')
    return
  }
  if (!Array.isArray(json?.nodes) || !Array.isArray(json?.sequences)) {
    ElMessage.warning('JSON需包含 nodes 与 sequences 数组')
    return
  }
  nodes.value = json.nodes.map(node => ({ ...node }))
  sequences.value = json.sequences.map(seq => ({ ...seq }))
  if (Array.isArray(json.notices)) noticeConfig.value = json.notices
  nodeTaskConfig.value = Array.isArray(json.nodeTasks) ? json.nodeTasks : []
  selectedNode.value = null
  selectedBranch.value = null
  jsonVisible.value = false
  ensureDefaultNodes()
  applyLayout()
  ElMessage.success('已应用流程JSON')
}

/** 预览数据：取当前编辑态，无需先保存 */
const previewJson = computed(() => ({
  nodes: nodes.value.map(node => ({ id: node.id, type: node.type, name: node.name, x: node.x, y: node.y, assigneeLabel: node.assigneeLabel })),
  sequences: sequences.value.map(seq => ({
    id: seq.id,
    sourceRef: seq.sourceRef,
    targetRef: seq.targetRef,
    name: seq.name,
    conditionExpression: seq.conditionExpression
  }))
}))

/**
 * 保存：基础信息与流程图版本一起提交，避免两处入口分别保存
 */
const saveProcess = () => {
  if (!basicInfo.name) {
    ElMessage.warning('请先填写流程名称')
    return
  }
  applyLayout()
  const tasks = [Promise.resolve({ code: 200 })];
  if (templateId.value) {
    tasks.push(updateTemplateAPI({ id: templateId.value, ...basicInfo }))
  }
  Promise.all(tasks).then(() => {
    const json = generateBpmnJson()
    return createTemplateVersionAPI({ templateId: templateId.value, bpmJson: JSON.stringify(json) })
  }).then(res => {
    if (res.code === 200) {
      ElMessage.success("流程模板保存成功");
      // 保存后自动获取最新版本号
      queryTemplateDetailAPI({ id: templateId.value }).then(detailRes => {
        if (detailRes.code === 200 && detailRes.data) {
          revId.value = detailRes.data.revId || revId.value;
          revNum.value = detailRes.data.revNum || '';
        }
      }).catch(() => {})
    }
  }).catch(() => {})
}

/** 清空：回到「开始 → 结束」的空白模板 */
const confirmClear = () => {
  nodes.value = [];
  sequences.value = [];
  selectedNode.value = null;
  selectedBranch.value = null;
  showClearConfirm.value = false
  nodeTaskConfig.value = []
  ensureDefaultNodes()
  applyLayout()
  ElMessage.success('流程图已清空')
}
</script>

<style scoped lang="scss">
.flow-designer {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  flex-direction: column;
  background: $bg-page;
  z-index: 100;
}

.designer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 $spacing-md;
  background: $app-bg-card;
  border-bottom: 1px solid $border-color;
  flex-shrink: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
  }

  .toolbar-logo {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: $el-color-primary-light-8;
    color: $el-color-primary;
  }

  .toolbar-title {
    font-size: 15px;
    font-weight: 600;
    color: $app-text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 画布容器：属性面板以浮层形式覆盖在右侧，不参与布局 */
.canvas-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  display: flex;
  overflow: hidden;
}

/* 左下角缩放条：与 workflow 设计器的观感保持一致 */
.canvas-zoombar {
  position: absolute;
  left: 14px;
  bottom: 14px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 3px;
  background: $app-bg-card;
  border: 1px solid $app-border;
  border-radius: 9px;
  box-shadow: 0 4px 16px rgba(31, 45, 74, 0.12);
  z-index: 50;
}

.zoom-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: $app-text-secondary;

  &:hover {
    background: $app-bg-sub;
    color: $el-color-primary;
  }
}

.zoom-value {
  min-width: 44px;
  text-align: center;
  font-size: 12px;
  color: $app-text-secondary;
  cursor: pointer;
  user-select: none;

  &:hover {
    color: $el-color-primary;
  }
}

.designer-statusbar {
  height: 30px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: $app-bg-card;
  border-top: 1px solid $app-border-light;
  font-size: 12px;
  color: $app-text-secondary;
  flex-shrink: 0;

  .status-divider {
    width: 1px;
    height: 10px;
    background: $app-border-light;
  }

  /* DAG 校验结果：文案可能带节点名，过长省略，全量问题在 tooltip 里 */
  .status-check {
    max-width: 460px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    cursor: default;

    &.status-ok {
      color: $el-color-success;
    }

    &.status-warn {
      color: $el-color-warning;
    }

    &.status-error {
      color: $el-color-danger;
    }
  }

  .status-right {
    margin-left: auto;
    color: $app-text-placeholder;
  }
}

/* 校验问题清单（挂到 body，不能用 scoped 选择器） */
.dag-check-tip-line {
  line-height: 18px;
  font-size: 12px;
}

/* 预览画布：给只读流程图一个确定高度，弹窗内不撑破 */
.preview-canvas {
  height: 520px;
}

/* JSON 抽屉：文本框撑满抽屉高度，等宽字体便于阅读 */
.json-box {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-textarea) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-textarea__inner) {
    flex: 1;
    min-height: 0;
    height: 100%;
    resize: none;
    font-family: Consolas, Monaco, 'Courier New', monospace;
    font-size: 12px;
    line-height: 1.5;
  }
}
</style>
