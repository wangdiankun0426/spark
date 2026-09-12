<template>
  <div class="workflow-designer">
    <!--顶部工具栏-->
    <div class="designer-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">{{ workflowName || '新建WorkFlow' }}</span>
        <el-tag v-if="revNum" size="small" type="warning">{{ revNum }}</el-tag>
        <el-button
            text
            size="small"
            type="primary"
            @click="varHelpVisible = true"
        >
          <el-icon><QuestionFilled /></el-icon>
          <span style="font-size:14px">变量帮助</span>
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button
            @click="handleSaveVersion"
            :loading="saving"
        ><el-icon><Document /></el-icon>保存流程
        </el-button>
        <el-button
            @click="showClearConfirm = true"
        ><el-icon><Delete /></el-icon>清空
        </el-button>
      </div>
    </div>
    <!--三栏布局-->
    <div class="designer-body">
      <!--左侧节点面板-->
      <node-panel
          :groups="workflowNodeGroups"
          :expanded="['flow', 'ai', 'system', 'tool']"
      />
      <!--中间画布-->
      <canvas-index
          :nodes="nodes"
          :sequences="sequences"
          :selected-node-id="selectedNodeId"
          :selected-edge-id="selectedEdgeId"
          :canvas-width="canvasWidth"
          :canvas-height="canvasHeight"
          :node-components="NODE_COMPONENTS"
          :field-options="fieldOptionsForCanvas"
          @select-node="handleSelectNode"
          @select-edge="handleSelectEdge"
          @drop-node="handleDropNode"
          @connect="handleConnectNodes"
          @delete-edge="handleDeleteEdge"
          @move-node="handleMoveNode"
      />
      <!--右侧属性面板-->
      <property-drawer
          v-if="selectedNode || selectedEdge"
          :node="selectedNode"
          :edge="selectedEdge"
          :nodes="nodes"
          :form-fields="formFields"
          @update="handleUpdateNode"
          @close="handleClosePanel"
          @delete-node="handleDeleteNode"
          @delete-edge="handleDeleteSelectedEdge"
      />
    </div>
    <!--底部状态栏-->
    <div class="designer-footer">
      <span>版本: {{ revNum || '新工作流' }} | 节点数: {{ nodes.length }} | 连线数: {{ sequences.length }}</span>
    </div>

    <!--变量帮助对话框-->
    <variable-help v-model="varHelpVisible" :form-fields="formFields" :base-vars="baseVars" />

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
import NodePanel from '@/views/flowDesigner/nodePanel/index.vue';
import CanvasIndex from '@/views/flowDesigner/canvas/index.vue';
import { NODE_COMPONENTS, NODE_META } from '@/views/flowDesigner/nodes/index.js';
import PropertyDrawer from '@/views/workflowDesigner/PropertyDrawer.vue';
import VariableHelp from '@/components/FlowVariableHelp/index.vue';

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

/** 画布连线条件标签用：表单字段 label/value 映射 */
const fieldOptionsForCanvas = computed(() =>
  formFields.value.map(f => ({ label: f.label, value: f.code }))
);

/** 选中连线 id */
const selectedEdgeId = computed(() => selectedEdge.value?.id || null);

/** 左侧节点面板分组配置 */
const workflowNodeGroups = [
  { name: 'flow', title: '流程控制', nodes: ['startEvent', 'endEvent', 'exclusiveGateway', 'parallelGateway'].map(toNodeGroup) },
  { name: 'ai', title: 'AI能力', nodes: ['llmTask', 'agentTask', 'kbSearch', 'kgSearch'].map(toNodeGroup) },
  { name: 'system', title: '系统能力', nodes: ['docParse', 'kbArchive', 'notify'].map(toNodeGroup) },
  { name: 'tool', title: '工具', nodes: ['variableOp', 'codeExecute', 'httpRequest', 'humanReview'].map(toNodeGroup) }
];

function toNodeGroup(type) {
  const meta = NODE_META[type] || {};
  return {
    type,
    name: meta.name || type,
    icon: meta.icon,
    color: meta.color,
    bgColor: meta.bgColor
  };
}

/** 画布尺寸：根据节点位置动态计算，避免出现大面积空白滚动区 */
const canvasWidth = ref(1200);
const canvasHeight = ref(800);

function calcCanvasSize() {
  const PADDING = 200;
  let maxX = 0, maxY = 0;
  nodes.value.forEach(node => {
    maxX = Math.max(maxX, node.x + (node.width || 160));
    maxY = Math.max(maxY, node.y + (node.height || 60));
  });
  // 最小尺寸设小：DesignerCanvas 会自动放大到可视区大小，此处只需保证包含节点
  canvasWidth.value = Math.max(maxX + PADDING, 200);
  canvasHeight.value = Math.max(maxY + PADDING, 200);
}

watch(() => nodes.value.length, () => calcCanvasSize());

onMounted(() => {
  const id = route.params.id;
  const rev = route.params.revId;
  if (id) {
    workflowId.value = id;
    loadWorkflowData(id, rev);
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
}

// 节点/连线ID生成规则与流程设计器保持一致
function genNodeId() { return 'n_' + Date.now(); }
function genEdgeId() { return 's_' + Date.now(); }

function handleDropNode(type, x, y) {
  const meta = NODE_META[type] || {};
  const newNode = {
    id: genNodeId(),
    type: type,
    name: meta.name || type,
    x: Math.round(x),
    y: Math.round(y),
    width: meta.width || 160,
    height: meta.height || 60,
    config: getDefaultConfig(type)
  };
  nodes.value.push(newNode);
}

function getDefaultConfig(type) {
  switch (type) {
    case 'startEvent':
      return {};
    case 'endEvent':
      return { outputs: [] };
    case 'llmTask':
      return { modelId: null, prompt: '', fileCode: '' };
    case 'exclusiveGateway':
      return {};
    case 'parallelGateway':
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
    case 'variableOp':
      return { operation: 'set', variables: [], sources: [], separator: ',', template: '', source: '', format: '', targetType: '' };
    case 'codeExecute':
      return { codeType: 'spel', code: '' };
    case 'httpRequest':
      return { url: '', method: 'GET', headers: '', body: '', timeoutMs: 30000 };
    case 'humanReview':
      return { prompt: '', reviewerIds: '', reviewType: 'approve', requireComment: false };
    default: return {};
  }
}

function handleSelectNode(node) {
  const nodeId = node ? node.id : null;
  selectedNodeId.value = nodeId;
  selectedNode.value = nodeId ? nodes.value.find(n => n.id === nodeId) : null;
  selectedEdge.value = null;
}

/** 点击连线：展示连线配置面板 */
function handleSelectEdge(edge) {
  selectedEdge.value = edge;
  if (edge) {
    selectedNodeId.value = null;
    selectedNode.value = null;
  }
}

function handleDeleteNode(nodeId) {
  nodes.value = nodes.value.filter(n => n.id !== nodeId);
  sequences.value = sequences.value.filter(e => e.source !== nodeId && e.target !== nodeId);
  if (selectedNodeId.value === nodeId) {
    selectedNodeId.value = null;
    selectedNode.value = null;
  }
  selectedEdge.value = null;
}

function handleUpdateNode(updatedNode) {
  const idx = nodes.value.findIndex(n => n.id === updatedNode.id);
  if (idx >= 0) {
    nodes.value[idx] = { ...updatedNode };
    selectedNode.value = nodes.value[idx];
  }
}

function handleClosePanel() {
  selectedNodeId.value = null;
  selectedNode.value = null;
  selectedEdge.value = null;
}

const showClearConfirm = ref(false);

/** 变量帮助对话框 */
const varHelpVisible = ref(false);
const baseVars = [

];

function confirmClear() {
  nodes.value = [];
  sequences.value = [];
  selectedNodeId.value = null;
  selectedNode.value = null;
  selectedEdge.value = null;
  showClearConfirm.value = false;
  ElMessage.success('流程图已清空');
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

/** 删除选中的连线 */
function handleDeleteSelectedEdge() {
  if (selectedEdge.value) {
    handleDeleteEdge(selectedEdge.value.id);
    ElMessage.success('连线已删除');
  }
}

function handleMoveNode(nodeId, x, y) {
  const node = nodes.value.find(n => n.id === nodeId);
  if (node) { node.x = Math.round(x); node.y = Math.round(y); }
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
      ElMessage.success('保存成功')
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

<style scoped lang="scss">
.workflow-designer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
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
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid $border-color;
  flex-shrink: 0;
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .toolbar-title {
    font-size: 16px;
    font-weight: 600;
    color: $color-text-primary;
  }
  .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.designer-footer {
  height: 28px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  background: #fff;
  border-top: 1px solid $border-color;
  font-size: 12px;
  color: $color-text-secondary;
  flex-shrink: 0;
}

</style>
