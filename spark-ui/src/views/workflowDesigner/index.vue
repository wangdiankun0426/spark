<template>
  <div class="workflow-designer">
    <!--顶部工具栏-->
    <div class="designer-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-logo">
          <el-icon :size="22"><Workflow /></el-icon>
        </span>
        <span class="toolbar-title">{{ workflowName || '新建WorkFlow' }}</span>
      </div>
      <div class="toolbar-right">
        <el-button @click="previewVisible = true">
          <el-icon><View /></el-icon>流程图预览
        </el-button>
        <el-button @click="showClearConfirm = true">
          <el-icon><Delete /></el-icon>清空流程
        </el-button>
        <el-button @click="handleOpenJson">
          <el-icon><Document /></el-icon>JSON 数据
        </el-button>
        <el-button v-hasPermi="['workflow:version:save']" type="primary" :loading="saving" @click="handleSaveVersion()">
          <el-icon><DocumentChecked /></el-icon>保存流程
        </el-button>
      </div>
    </div>

    <!--画布区-->
    <div class="designer-body">
      <canvas-index
          :nodes="nodes"
          :sequences="sequences"
          :selected-node-id="selectedNodeId"
          :selected-edge-id="selectedEdgeId"
          :canvas-width="canvasWidth"
          :canvas-height="canvasHeight"
          :field-options="fieldOptionsForCanvas"
          :reveal-ids="revealIds"
          @select-node="handleSelectNode"
          @select-edge="handleSelectEdge"
          @focus-edge="handleFocusEdge"
          @drop-node="handleDropNode"
          @connect="handleConnectNodes"
          @delete-edge="handleDeleteEdge"
          @move-node="handleMoveNode"
          @insert-after="insertAfter"
          @insert-on-edge="insertOnEdge"
          @place-node="handlePlaceNode"
          @replace-node="handleReplaceNode"
      />
      <!--右侧浮层属性面板-->
      <property-drawer
          v-if="selectedNode || selectedEdge"
          :node="selectedNode"
          :edge="selectedEdge"
          :nodes="nodes"
          :sequences="sequences"
          :form-fields="formFields"
          @update="handleUpdateNode"
          @close="handleClosePanel"
          @delete-node="handleDeleteNode"
          @delete-edge="handleDeleteSelectedEdge"
      />
    </div>

    <!--底部状态条-->
    <div class="designer-statusbar">
      <span>节点 {{ nodes.length }}</span>
      <i class="status-divider"></i>
      <span>连线 {{ sequences.length }}</span>
      <i class="status-divider"></i>
      <span :class="validateOk ? 'status-ok' : 'status-error'">{{ validateText }}</span>
      <span class="status-right">{{ workflowId ? `版本 ${revNum ? 'v' + revNum : '新'}` : '未保存到模板' }}</span>
    </div>

    <!--JSON 数据：可复制给他人，也可粘贴其他流程的 JSON 后应用到当前画布-->
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

    <!--流程图预览：只读，仅展示当前画布-->
    <el-dialog
        v-model="previewVisible"
        title="流程图预览"
        width="900px"
        top="8vh"
        append-to-body
        destroy-on-close
    >
      <workflow-preview
          v-if="previewVisible"
          :nodes="previewDag.nodes"
          :sequences="previewDag.sequences"
          :field-options="fieldOptionsForCanvas"
          :height="520"
      />
    </el-dialog>

    <!--清空确认对话框-->
    <el-dialog title="确认清空" v-model="showClearConfirm" width="30%" :show-close="false">
      <p>确定要清空整个流程图吗？此操作不可撤销。</p>
      <template #footer>
        <el-button @click="showClearConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmClear">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, onBeforeRouteLeave } from 'vue-router';
import { ElMessage } from 'element-plus';
import { queryWorkflowDetailAPI } from '@/api/workflow/template.js';
import { queryFormFieldListAPI } from '@/api/form/formField.js';
import { saveVersionAPI, queryVersionDetailAPI } from '@/api/workflow/version.js';
import CanvasIndex from '@/views/workflowDesigner/canvas/index.vue';
import { NODE_META, NODE_SIZE, DEFAULT_PYTHON_CODE } from '@/views/workflowDesigner/nodes/index.js';
import PropertyDrawer from '@/views/workflowDesigner/PropertyDrawer.vue';
import WorkflowPreview from '@/components/WorkflowPreview/index.vue';
import Workflow from "@/assets/icons/workflow.vue";

const route = useRoute();
const workflowId = ref(null);
const workflowName = ref('');
const revId = ref(null);
const revNum = ref('');
const nodes = ref([]);
const sequences = ref([]);
const selectedNodeId = ref(null);

const selectedNode = ref(null);
const selectedEdge = ref(null);
const formFields = ref([]);
const jsonVisible = ref(false);
/** 流程图预览弹窗：只读展示当前编辑态，无需先保存 */
const previewVisible = ref(false);

/** 画布连线条件标签用：表单字段 label/value 映射 */
const fieldOptionsForCanvas = computed(() =>
  formFields.value.map(f => ({ label: f.label, value: f.code }))
);

/** 仅高亮的连线（点线不弹面板，只做选中态，用于露出连线中点的插入按钮） */
const focusedEdgeId = ref(null);

/** 需要整体滚进可视区的一组节点（创建条件分支后露出 分支→占位→结束 全貌） */
const revealIds = ref([]);

/** 选中连线 id */
const selectedEdgeId = computed(() => selectedEdge.value?.id || focusedEdgeId.value || null);

/** 校验状态：必须存在开始与结束节点 */
const validateText = computed(() => {
  const hasStart = nodes.value.some(n => n.type === 'startEvent');
  const hasEnd = nodes.value.some(n => n.type === 'endEvent');
  if (!hasStart && !hasEnd) return '缺少开始与结束节点';
  if (!hasStart) return '缺少开始节点';
  if (!hasEnd) return '缺少结束节点';
  return '校验通过';
});

const validateOk = computed(() => validateText.value === '校验通过');

/** 画布尺寸：根据节点位置动态计算，避免出现大面积空白滚动区 */
const canvasWidth = ref(1600);
const canvasHeight = ref(900);

function calcCanvasSize() {
  // 右侧多留一截：属性面板是浮层（340），留出可滚动空间让最右节点也能被滚出来
  const PADDING_X = 420;
  const PADDING_Y = 220;
  let maxX = 0, maxY = 0;
  nodes.value.forEach(node => {
    maxX = Math.max(maxX, node.x + NODE_SIZE.width);
    maxY = Math.max(maxY, node.y + NODE_SIZE.height);
  });
  canvasWidth.value = Math.max(maxX + PADDING_X, 200);
  canvasHeight.value = Math.max(maxY + PADDING_Y, 200);
}

watch(() => nodes.value.length, () => calcCanvasSize());

onMounted(() => {
  const id = route.params.id;
  const rev = route.params.revId;
  if (id) {
    workflowId.value = id;
    loadWorkflowData(id, rev);
  } else {
    // 新建工作流：直接给出开始 → 结束的骨架
    ensureDefaultNodes();
  }
});

async function loadWorkflowData(id, rev) {
  const res = await queryWorkflowDetailAPI({ id });
  if (res.code === 200 && res.data) {
    workflowName.value = res.data.name;
    revNum.value = res.data.revNum || '';
    revId.value = res.data.revId;
    // 加载绑定输入表单的字段定义（start节点属性面板展示用）
    if (res.data.formId) {
      const fRes = await queryFormFieldListAPI({ formId: res.data.formId });
      if (fRes.code === 200 && fRes.data) {
        formFields.value = fRes.data;
      }
    }
  }
  const loadRevId = (rev && rev !== '0') ? rev : revId.value;
  if (loadRevId) {
    const vRes = await queryVersionDetailAPI({ id: loadRevId });
    if (vRes.code === 200 && vRes.data && vRes.data.dagJson) {
      try {
        const dag = JSON.parse(vRes.data.dagJson);
        nodes.value = dag.nodes || [];
        sequences.value = dag.sequences || [];
        if (rev && rev !== '0') { revId.value = Number(rev); }
        revNum.value = vRes.data.revNum || '';
        calcCanvasSize();
      } catch (e) {
        console.error('解析DAG JSON失败', e);
      }
    }
  }
  // 版本里还没有任何节点（首次编辑 / 空 DAG）时补上开始 → 结束骨架
  ensureDefaultNodes();
}

// 节点 ID 形如 n_<时间戳>；同毫秒批量创建时递增时间戳避让，保证不撞车且不带序号后缀
let idSeed = 0;

function genNodeId() {
  let stamp = Date.now();
  let id = 'n_' + stamp;
  while (nodes.value.some(n => n.id === id)) {
    stamp += 1;
    id = 'n_' + stamp;
  }
  return id;
}

function genEdgeId() { idSeed += 1; return 's_' + Date.now() + '_' + idSeed; }

function findNodeById(id) {
  return nodes.value.find(n => n.id === id) || null;
}

/**
 * 创建节点：默认位置、默认配置
 */
function createNode(type, x, y, fixedId) {
  const meta = NODE_META[type] || {};
  const newNode = {
    id: fixedId || genNodeId(),
    type,
    name: meta.name || type,
    x: Math.round(x),
    y: Math.round(y),
    config: getDefaultConfig(type)
  };
  nodes.value.push(newNode);
  return newNode;
}

function handleDropNode(type, x, y) {
  const node = createNode(type, x - NODE_SIZE.width / 2, y - NODE_SIZE.height / 2);
  if (type === 'exclusiveGateway') {
    attachGatewayBranches(node);
    clearSelection();
  } else {
    // 拖进来的节点默认接到结束节点，避免成为游离子图
    connectDefaultTarget(node, null);
    handleSelectNode(node);
  }
}

/**
 * 空白画布兜底：默认摆好「开始 → 结束」，客户进来就有可连、可插入的骨架
 * 间距 72，与横向编排的列距一致
 */
const DEFAULT_NODE_X = 520;
const DEFAULT_NODE_Y = 380;
const DEFAULT_COL_GAP = 40;

function ensureDefaultNodes() {
  if (nodes.value.length) return;
  // 骨架固定用 n_0 / n_1，便于识别端点；其余节点仍走时间戳 ID
  const start = createNode('startEvent', DEFAULT_NODE_X, DEFAULT_NODE_Y, 'n_0');
  const end = createNode('endEvent', DEFAULT_NODE_X + NODE_SIZE.width + DEFAULT_COL_GAP, DEFAULT_NODE_Y, 'n_1');
  sequences.value = [{ id: genEdgeId(), source: start.id, target: end.id, conditionExpression: '' }];
  calcCanvasSize();
}

function getDefaultConfig(type) {
  switch (type) {
    case 'startEvent':
      return {};
    case 'endEvent':
      return { outputs: [] };
    case 'llmTask':
      return { modelId: null, prompt: '' };
    case 'exclusiveGateway':
      return {};
    case 'placeholder':
      return {};
    case 'agentTask':
      return { agentId: null, task: '', memoryId: '' };
    case 'kbSearch':
      return { kbIds: '', query: '', topK: 5 };
    case 'kgSearch':
      return { graphId: '', query: '' };
    case 'docParse':
      return { fileCode: '' };
    case 'notify':
      return { titleCode: '', contentCode: '', userCode: '', refCode: '' };
    case 'kbArchive':
      return { knowledgeId: null, fileCode: '' };
    case 'codeExecute':
      return { code: DEFAULT_PYTHON_CODE, inputs: [], outputs: [{ name: '结果', key: 'result' }] };
    case 'humanReview':
      return { prompt: '', reviewerIds: '' };
    default: return {};
  }
}

// 选中与面板

function handleSelectNode(node) {
  const nodeId = node ? node.id : null;
  selectedNodeId.value = nodeId;
  selectedNode.value = nodeId ? nodes.value.find(n => n.id === nodeId) : null;
  selectedEdge.value = null;
  focusedEdgeId.value = null;
}

function handleSelectEdge(edge) {
  selectedEdge.value = edge;
  focusedEdgeId.value = null;
  if (edge) {
    selectedNodeId.value = null;
    selectedNode.value = null;
  }
}

/** 点连线本身：只高亮、不弹面板；分支配置从线上的「分支条件」标签进入 */
function handleFocusEdge(edge) {
  if (!edge) return;
  // 已经在这条连线的分支配置面板上时，保持面板不收起
  if (selectedEdge.value && selectedEdge.value.id === edge.id) return;
  selectedEdge.value = null;
  focusedEdgeId.value = edge.id;
  selectedNodeId.value = null;
  selectedNode.value = null;
}

function handleClosePanel() {
  selectedNodeId.value = null;
  selectedNode.value = null;
  selectedEdge.value = null;
  focusedEdgeId.value = null;
}

function handleUpdateNode(updatedNode) {
  const idx = nodes.value.findIndex(n => n.id === updatedNode.id);
  if (idx >= 0) {
    nodes.value[idx] = { ...updatedNode };
    selectedNode.value = nodes.value[idx];
  }
}

// 节点增删改

function handleDeleteNode(nodeId) {
  const node = findNodeById(nodeId);
  if (node && ['startEvent', 'endEvent'].includes(node.type)) return;
  // 级联清理：只剩空占位下游时一并删除，避免分支节点消失后留下孤立占位
  const removed = new Set([nodeId]);
  let changed = true;
  while (changed) {
    changed = false;
    nodes.value.forEach(n => {
      if (removed.has(n.id) || n.type !== 'placeholder') return;
      const incoming = sequences.value.filter(e => e.target === n.id);
      if (incoming.length && incoming.every(e => removed.has(e.source))) {
        removed.add(n.id);
        changed = true;
      }
    });
  }
  nodes.value = nodes.value.filter(n => !removed.has(n.id));
  sequences.value = sequences.value.filter(e => !removed.has(e.source) && !removed.has(e.target));
  if (selectedNodeId.value && removed.has(selectedNodeId.value)) {
    selectedNodeId.value = null;
    selectedNode.value = null;
  }
  selectedEdge.value = null;
}

/**
 * 占位节点原地替换为真实节点：保留 id / 位置 / 已有连线
 */
function handleReplaceNode(nodeId, type) {
  const node = findNodeById(nodeId);
  if (!node || node.type !== 'placeholder') return;
  const meta = NODE_META[type] || {};
  node.type = type;
  node.name = meta.name || type;
  node.config = getDefaultConfig(type);
  if (type === 'exclusiveGateway') {
    // 占位原来的出线（→ 汇合点）改由两条新分支承接，否则会多出一条直连
    const outIdx = sequences.value.findIndex(e => e.source === node.id);
    const joinId = outIdx >= 0 ? sequences.value[outIdx].target : null;
    if (outIdx >= 0) sequences.value.splice(outIdx, 1);
    attachGatewayBranches(node, { joinId });
  }
  calcCanvasSize();
  // 分支节点没有可配项，别用属性面板挡住刚连好的分支
  if (type === 'exclusiveGateway') clearSelection();
  else selectNewNode(node);
}

/** 网格吸附：对齐 8px */
function snapToGrid(value) {
  return Math.round(value / 8) * 8;
}

function handleMoveNode(nodeId, x, y) {
  const node = nodes.value.find(n => n.id === nodeId);
  if (node) {
    node.x = snapToGrid(x);
    node.y = snapToGrid(y);
  }
}

// 插入节点：三个 ＋ 入口共用

/** 清空选中：分支节点创建后不弹属性面板（配置在连线上） */
function clearSelection() {
  selectedNodeId.value = null;
  selectedNode.value = null;
  selectedEdge.value = null;
  focusedEdgeId.value = null;
}

function selectNewNode(node) {
  selectedNodeId.value = node.id;
  selectedNode.value = node;
  selectedEdge.value = null;
}

// 条件分支：创建后自动补两条分支，未配置的一侧先放占位节点

/** 分支节点与分支列之间的横向间距：留出宽度给连线上的条件标签 */
const BRANCH_GAP_X = 148;
/** 两条分支之间的纵向间距（卡片高 62 + 38 留白，容下条件标签） */
const BRANCH_DY = 100;

/** 占位节点：类型未定，点击后弹出选择器原地替换 */
function createPlaceholder(x, y) {
  return createNode('placeholder', x, y);
}

function addBranchEdge(gateway, node, condition) {
  sequences.value.push({
    id: genEdgeId(),
    source: gateway.id,
    target: node.id,
    conditionExpression: condition || ''
  });
}

/**
 * 在指定列找一个不与现有节点重叠的纵向空位
 * @param x 列坐标
 * @param startY 期望的纵向位置
 * @param excludeIds 不参与重叠判断的节点（分支节点本身、刚建的占位）
 * @returns {number} 可用的 y 坐标
 */
function findFreeY(x, startY, excludeIds) {
  const occupied = nodes.value.filter(n => !excludeIds.includes(n.id));
  const step = NODE_SIZE.height + 22;
  let y = snapToGrid(startY);
  for (let i = 0; i < 20; i += 1) {
    const hit = occupied.some(n =>
      x < n.x + NODE_SIZE.width && x + NODE_SIZE.width > n.x
      && y < n.y + NODE_SIZE.height && y + NODE_SIZE.height > n.y
    );
    if (!hit) return y;
    y += step;
  }
  return y;
}

/**
 * 分支的汇合点：默认是结束节点；插到已有连线上时，原下游节点就是汇合点
 * @param gateway 分支节点
 * @param joinId 指定的汇合点 id
 */
function findJoinNode(gateway, joinId) {
  const picked = joinId ? findNodeById(joinId) : null;
  if (picked && picked.id !== gateway.id) return picked;
  return nodes.value.find(n => n.type === 'endEvent' && n.id !== gateway.id) || null;
}

/**
 * 为条件分支节点自动生成两条分支
 * 分支列上下各放一个占位节点，占位节点再汇合到汇合点（默认结束节点），
 * 与流程设计器一致：分支创建后流程就是通到结束节点的，不会断在半路
 * @param gateway 分支节点
 * @param options.joinId 汇合点 id，插到已有连线上时传原下游节点
 * @param options.carryCondition 原连线上的条件表达式，转移到第一条分支
 */
function attachGatewayBranches(gateway, { joinId = null, carryCondition = '' } = {}) {
  const join = findJoinNode(gateway, joinId);
  const branchX = snapToGrid(gateway.x + NODE_SIZE.width + BRANCH_GAP_X);
  const joinX = snapToGrid(branchX + NODE_SIZE.width + INSERT_GAP);

  // 汇合点必须排在分支列右侧，否则汇合连线会往回画：先整体右推，占位节点随后再建
  if (join && join.x < joinX) {
    // 基准线先取快照：join 自身也会被右推，若边遍历边比较会让后面的节点漏推
    const baseX = join.x;
    const delta = joinX - baseX;
    nodes.value.forEach(n => {
      if (n.id !== gateway.id && n.x >= baseX) n.x += delta;
    });
  }

  const upperY = findFreeY(branchX, gateway.y - BRANCH_DY / 2, [gateway.id]);
  const upper = createPlaceholder(branchX, upperY);
  // 上方占位被挤下来时，下方占位要跟着往下让一档，否则两张卡会叠在一起
  const lowerStartY = Math.max(gateway.y + BRANCH_DY / 2, upperY + NODE_SIZE.height + 22);
  const lowerY = findFreeY(branchX, lowerStartY, [gateway.id]);
  const lower = createPlaceholder(branchX, lowerY);

  addBranchEdge(gateway, upper, carryCondition);
  addBranchEdge(gateway, lower, '');

  // 两条分支都汇合到同一个节点，占位替换成真实节点后连线自然变成 新节点 → 汇合点
  if (join) {
    addBranchEdge(upper, join, '');
    addBranchEdge(lower, join, '');
  }
  calcCanvasSize();
  // 分支节点的配置都在连线上，创建后不弹属性面板，改为把整条分支滚进可视区
  revealIds.value = [gateway.id, upper.id, lower.id, join ? join.id : null].filter(Boolean);
  return { upper, lower, join };
}

/** 插入节点时给下游让位的偏移量：卡片宽度 + 列间距 */
const INSERT_GAP = 40;

/** 把 x 不小于基准线的节点整体右移，避免插入的卡片压住下游（按 id 跳过新节点：响应式数组里拿到的是 Proxy） */
function shiftDownstream(baseX, skipNode) {
  const shift = NODE_SIZE.width + INSERT_GAP;
  const skipId = skipNode && skipNode.id;
  nodes.value.forEach(n => {
    if (n.id !== skipId && n.x >= baseX) n.x += shift;
  });
}

/**
 * 新节点的默认汇合点
 * 分支节点下新增的分支跟随兄弟分支的汇合点，避免多出一条横跨回结束节点的线；
 * 其余情况一律用结束节点
 * @param node 新建的节点
 * @param source 触发创建的上游节点
 */
function resolveDefaultTarget(node, source) {
  if (source && source.type === 'exclusiveGateway') {
    const sibling = sequences.value
      .filter(e => e.source === source.id && e.target !== node.id)
      .map(e => sequences.value.find(x => x.source === e.target))
      .find(Boolean);
    const joinNode = sibling ? findNodeById(sibling.target) : null;
    if (joinNode && joinNode.id !== node.id) return joinNode;
  }
  return nodes.value.find(n => n.type === 'endEvent' && n.id !== node.id) || null;
}

/** 汇合点必须排在新节点右侧，否则连线会往回勾：先把下游整体右推一列 */
function ensureTargetRightOf(node, target) {
  const needX = snapToGrid(node.x + NODE_SIZE.width + INSERT_GAP);
  if (target.x >= needX) return;
  const baseX = target.x;
  const delta = needX - baseX;
  nodes.value.forEach(n => {
    if (n.id !== node.id && n.x >= baseX) n.x += delta;
  });
}

/**
 * 补齐新节点的默认出口：接到结束节点（或分支的汇合点），让流程图不会断在半路
 * 已经有出线（插到连线中间、占位替换）的不动
 * @param node 新建的节点
 * @param source 触发创建的上游节点
 */
function connectDefaultTarget(node, source) {
  if (!node) return;
  // 端点是流程边界，占位节点的出口由分支逻辑负责
  if (['startEvent', 'endEvent', 'placeholder'].includes(node.type)) return;
  if (sequences.value.some(e => e.source === node.id)) return;
  const target = resolveDefaultTarget(node, source);
  if (!target || target.id === node.id) return;
  ensureTargetRightOf(node, target);
  sequences.value.push({ id: genEdgeId(), source: node.id, target: target.id, conditionExpression: '' });
  calcCanvasSize();
}

/**
 * 入口一：插入到节点之后；仅有一条出线时自动插在中间并改接连线
 */
function insertAfter(sourceId, type) {
  const source = findNodeById(sourceId);
  if (!source) return;
  const outgoing = sequences.value.filter(e => e.source === sourceId);
  const nextEdgeIndex = sequences.value.findIndex(e => e.source === sourceId);
  const nextEdge = nextEdgeIndex >= 0 ? sequences.value[nextEdgeIndex] : null;
  const target = nextEdge ? findNodeById(nextEdge.target) : null;
  // 源节点是条件分支时，新分支要落在分支列上；否则会和分支列里的占位/分支节点横向压在一起
  const onBranchColumn = source.type === 'exclusiveGateway';
  const x = onBranchColumn
    ? snapToGrid(source.x + NODE_SIZE.width + BRANCH_GAP_X)
    : source.x + NODE_SIZE.width + INSERT_GAP;
  // 源节点本身已分流时，新节点往下错开一档，避免叠在已有分支上
  const y = onBranchColumn
    ? findFreeY(x, source.y + BRANCH_DY, [source.id])
    : source.y + (outgoing.length > 1 ? BRANCH_DY : 0);
  const node = createNode(type, x, y);
  if (nextEdge && target && outgoing.length === 1) {
    sequences.value.splice(nextEdgeIndex, 1);
    sequences.value.push({ id: genEdgeId(), source: sourceId, target: node.id, conditionExpression: '' });
    if (type === 'exclusiveGateway') {
      // 原下游当作汇合点，两条分支都补占位后汇入它
      attachGatewayBranches(node, { joinId: target.id, carryCondition: nextEdge.conditionExpression || '' });
    } else {
      shiftDownstream(target.x, node);
      sequences.value.push({ id: genEdgeId(), source: node.id, target: target.id, conditionExpression: nextEdge.conditionExpression || '' });
    }
  } else {
    sequences.value.push({ id: genEdgeId(), source: sourceId, target: node.id, conditionExpression: '' });
    if (type === 'exclusiveGateway') attachGatewayBranches(node);
  }
  // 没接上下游的节点，出口默认接到结束节点（分支节点已自带两条分支，会被跳过）
  connectDefaultTarget(node, source);
  calcCanvasSize();
  // 分支节点没有可配项，别用属性面板挡住刚连好的分支
  if (type === 'exclusiveGateway') clearSelection();
  else selectNewNode(node);
}

/**
 * 入口二：插入到连线中间，原连线拆成两段
 */
function insertOnEdge(edgeId, type) {
  const edgeIndex = sequences.value.findIndex(e => e.id === edgeId);
  if (edgeIndex < 0) return;
  const edge = sequences.value[edgeIndex];
  const source = findNodeById(edge.source);
  const target = findNodeById(edge.target);
  if (!source || !target) return;
  const x = source.x + NODE_SIZE.width + INSERT_GAP;
  const y = source.y + (target.y - source.y) / 2;
  const node = createNode(type, x, y);
  sequences.value.splice(edgeIndex, 1);
  sequences.value.push({ id: genEdgeId(), source: edge.source, target: node.id, conditionExpression: edge.conditionExpression || '' });
  if (type === 'exclusiveGateway') {
    // 原目标节点当作汇合点，两条分支都补占位后汇入它
    attachGatewayBranches(node, { joinId: edge.target });
  } else {
    shiftDownstream(target.x, node);
    sequences.value.push({ id: genEdgeId(), source: node.id, target: edge.target, conditionExpression: '' });
  }
  // 兜底：仍未接出线的节点（如替换式插入异常）默认接到结束节点
  connectDefaultTarget(node, source);
  calcCanvasSize();
  // 分支节点没有可配项，别用属性面板挡住刚连好的分支
  if (type === 'exclusiveGateway') clearSelection();
  else selectNewNode(node);
}

/**
 * 入口三：双击空白放置，出口默认接到结束节点
 */
function handlePlaceNode(type, x, y) {
  const node = createNode(type, x, y);
  if (type === 'exclusiveGateway') {
    attachGatewayBranches(node);
    clearSelection();
  } else {
    connectDefaultTarget(node, null);
    selectNewNode(node);
  }
}

/* ------------------------------ JSON 数据 ------------------------------ */

const jsonText = ref('');

/** 序列化：只保留持久化需要的结构，剔除渲染派生字段 */
function buildDag() {
  return {
    nodes: nodes.value.map(n => {
      const node = {
        id: n.id,
        type: n.type,
        name: n.name,
        x: Math.round(n.x),
        y: Math.round(n.y),
        config: n.config || {}
      };
      if (n.disabled) node.disabled = true;
      return node;
    }),
    sequences: sequences.value.map(e => {
      const edge = { id: e.id, source: e.source, target: e.target };
      if (e.name) edge.name = e.name;
      if (e.conditionExpression) edge.conditionExpression = e.conditionExpression;
      return edge;
    })
  };
}

/** 预览数据：与保存提交的结构一致，取当前编辑态 */
const previewDag = computed(() => buildDag());

/** 打开抽屉：填入当前编辑态数据（与保存提交的内容一致） */
function handleOpenJson() {
  jsonText.value = JSON.stringify(buildDag(), null, 2);
  jsonVisible.value = true;
}

/** 复制当前 JSON */
function handleCopyJson() {
  const text = JSON.stringify(buildDag(), null, 2);
  const done = () => ElMessage.success('已复制流程JSON');
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(done).catch(() => fallbackCopy(text, done));
  } else {
    fallbackCopy(text, done);
  }
}

/** 剪贴板不可用时的兜底复制 */
function fallbackCopy(text, done) {
  const ta = document.createElement('textarea');
  ta.value = text;
  ta.style.position = 'fixed';
  ta.style.opacity = '0';
  document.body.appendChild(ta);
  ta.select();
  try {
    document.execCommand('copy');
    done();
  } finally {
    document.body.removeChild(ta);
  }
}

/** 应用 JSON：整体覆盖当前节点/连线 */
function handleApplyJson() {
  let dag;
  try {
    dag = JSON.parse(jsonText.value);
  } catch (e) {
    ElMessage.warning('JSON格式有误，请检查后重试');
    return;
  }
  if (!Array.isArray(dag?.nodes) || !Array.isArray(dag?.sequences)) {
    ElMessage.warning('JSON需包含 nodes 与 sequences 数组');
    return;
  }
  nodes.value = dag.nodes.map(n => ({ ...n }));
  sequences.value = dag.sequences.map(e => ({ ...e }));
  selectedNodeId.value = null;
  selectedNode.value = null;
  selectedEdge.value = null;
  focusedEdgeId.value = null;
  jsonVisible.value = false;
  calcCanvasSize();
  ensureDefaultNodes();
  ElMessage.success('已应用流程JSON');
}

function handleConnectNodes(sourceId, targetId) {
  if (sourceId === targetId) return;
  const exists = sequences.value.find(e => e.source === sourceId && e.target === targetId);
  if (exists) return;
  sequences.value.push({ id: genEdgeId(), source: sourceId, target: targetId, conditionExpression: '' });
}

function handleDeleteEdge(edgeId) {
  sequences.value = sequences.value.filter(e => e.id !== edgeId);
  if (selectedEdge.value?.id === edgeId) { selectedEdge.value = null; }
}

function handleDeleteSelectedEdge() {
  if (selectedEdge.value) {
    handleDeleteEdge(selectedEdge.value.id);
    ElMessage.success('连线已删除');
  }
}

const showClearConfirm = ref(false);

function confirmClear() {
  nodes.value = [];
  sequences.value = [];
  selectedNodeId.value = null;
  selectedNode.value = null;
  selectedEdge.value = null;
  showClearConfirm.value = false;
  // 清空后仍保留开始 → 结束骨架，画布不落到全空状态
  ensureDefaultNodes();
  ElMessage.success('流程图已清空');
}

const saving = ref(false);

async function handleSaveVersion(silent) {
  if (!workflowId.value) {
    if (!silent) ElMessage.warning('请先从工作流列表进入设计器');
    return false;
  }
  saving.value = true;
  try {
    const dagJson = JSON.stringify({ nodes: nodes.value, sequences: sequences.value });
    const res = await saveVersionAPI({ templateId: workflowId.value, dagJson });
    if (res.code === 200) {
      const detailRes = await queryWorkflowDetailAPI({ id: workflowId.value });
      if (detailRes.code === 200 && detailRes.data) {
        revNum.value = detailRes.data.revNum;
        revId.value = detailRes.data.revId;
      }
      if (!silent) ElMessage.success('保存成功');
      return true;
    }
    if (!silent) ElMessage.error('保存失败');
    return false;
  } catch (e) {
    if (!silent) ElMessage.error('保存异常: ' + (e.message || ''));
    return false;
  } finally {
    saving.value = false;
  }
}

// 离开页面时自动保存（浏览器关闭/刷新/路由跳转）
onBeforeUnmount(async () => {
  if (workflowId.value && nodes.value.length > 0) {
    // 同步发送保存请求（sendBeacon 或 同步XHR 保证数据不丢失）
    const dagJson = JSON.stringify({ nodes: nodes.value, sequences: sequences.value });
    try {
      await saveVersionAPI({ templateId: workflowId.value, dagJson });
    } catch (e) { /* ignore */ }
  }
});

onBeforeRouteLeave(async (to, from, next) => {
  if (workflowId.value && nodes.value.length > 0) {
    await handleSaveVersion(true);
  }
  next();
});

</script>

<style lang="scss">
/* 令牌定义层不进 scoped：流程图预览弹窗等 teleport 内容也要能取到 --wf-* */
@use './styles/tokens.scss';
</style>

<style scoped lang="scss">
@use './styles/vars.scss' as *;

.workflow-designer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: $app-bg-page;
  z-index: 100;
}

.designer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 16px;
  background: $app-bg-card;
  border-bottom: 1px solid $app-border-light;
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
  position: relative;
  display: flex;
  overflow: hidden;
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

  .status-ok {
    color: $el-color-success;
  }

  .status-error {
    color: $el-color-danger;
  }

  .status-right {
    margin-left: auto;
    color: $app-text-placeholder;
  }
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
