<template>
  <div class="flow-designer">
    <!--顶部工具栏-->
    <div class="designer-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">{{ templateName || '新建流程' }}</span>
        <el-tag v-if="revNum" size="small" type="warning">{{ revNum }}</el-tag>
        <el-button text size="small" type="primary" @click="varHelpVisible = true">
          <el-icon><QuestionFilled /></el-icon>
          <span style="font-size:12px">变量帮助</span>
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button
            type="primary"
            @click="saveProcess"
        >
          <el-icon><Folder /></el-icon>保存流程
        </el-button>
        <el-button
            type="warning"
            @click="notificationConfigRef.open()"
        >
          <el-icon><Bell /></el-icon>流程通知
        </el-button>
        <el-button
            type="danger"
            @click="showClearConfirm = true"
        >
          <el-icon><Delete /></el-icon>清空
        </el-button>
      </div>
    </div>
    <!--三栏布局-->
    <div class="designer-body">
      <!--左侧节点面板-->
      <node-panel
          :groups="flowNodeGroups"
          :expanded="['baseNode']"
          data-key="node-type"
      />

      <!--中间画布-->
      <canvas-index
          :nodes="nodes"
          :sequences="sequences"
          source-key="sourceRef"
          target-key="targetRef"
          :selected-node-id="selectedNode?.id"
          :selected-edge-id="selectedSequence?.id"
          :canvas-width="canvasWidth"
          :canvas-height="canvasHeight"
          :node-components="NODE_COMPONENTS"
          :field-options="fieldOptions"
          drop-data-key="node-type"
          @select-node="selectNode"
          @select-edge="selectSequence"
          @connect="handleConnect"
          @delete-edge="deleteSequence"
          @drop-node="(type, x, y) => handleCanvasDrop({ nodeType: type, x, y })"
          @move-node="handleMoveNode"
      />

      <!--右侧属性面板（选中节点/连线时展示，未选中时隐藏释放画布空间）-->
      <property-drawer
          v-if="selectedNode || selectedSequence"
          :node="selectedNode"
          :sequence="selectedSequence"
          :nodes="nodes"
          :field-options="fieldOptions"
          :node-tasks="nodeTaskConfig"
          @delete-node="deleteNode"
          @delete-sequence="deleteSequence"
          @close="closePanel"
      />
    </div>
    <!--底部状态栏-->
    <div class="designer-footer">
      <span>版本: {{ revNum || '新工作流' }} | 节点数: {{ nodes.length }} | 连线数: {{ sequences.length }}</span>
    </div>

    <!--清空确认对话框-->
    <el-dialog title="确认清空" v-model="showClearConfirm" width="30%" :show-close="false">
      <p>确定要清空整个流程图吗？此操作不可撤销。</p>
      <template #footer>
        <el-button @click="showClearConfirm = false">取消</el-button>
        <el-button type="primary" @click="confirmClear">确定</el-button>
      </template>
    </el-dialog>

    <!--流程通知-->
    <NoticeConfig ref="notificationConfigRef" :config="noticeConfig" />

    <!--变量帮助-->
    <variable-help v-model="varHelpVisible" :form-fields="helpFormFields" :base-vars="helpBaseVars" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from "vue-router";
import { createTemplateVersionAPI, queryTemplateVersionDetailAPI } from "@/api/flow/templateVersion.js";
import { queryTemplateDetailAPI } from "@/api/flow/template.js";
import { queryFormFieldListAPI } from "@/api/form/formField.js";
import NodePanel from '@/views/flowDesigner/nodePanel/index.vue'
import CanvasIndex from '@/views/flowDesigner/canvas/index.vue'
import NoticeConfig from './NoticeConfig.vue'
import PropertyDrawer from './PropertyDrawer.vue'
import VariableHelp from '@/components/FlowVariableHelp/index.vue'
import { NODE_COMPONENTS, NODE_META } from '@/views/flowDesigner/nodes/index.js'

const templateId = ref(0);
const revId = ref(0);
const templateName = ref('');
const revNum = ref('');
const fieldOptions = ref([]);

/** 变量帮助 */
const varHelpVisible = ref(false);
const helpFormFields = computed(() => fieldOptions.value.map(f => ({ label: f.label, code: f.value })));
const helpBaseVars = [
  { label: '申请人', value: '#{base:appUserName}#' },
  { label: '流程名称', value: '#{base:flowName}#' },
  { label: '发起人', value: '#{base:appUser}#' },
  { label: '当前审批人', value: '#{base:appAssignee}#' }
];

onMounted(() => {
  const params = useRoute().params;
  templateId.value = Number(params.id);
  revId.value = Number(params.revId);
  // 加载模板名称与最新版本号
  queryTemplateDetailAPI({ id: templateId.value }).then(res => {
    if (res.code === 200 && res.data) {
      templateName.value = res.data.name || '';
      if (res.data.revNum) revNum.value = res.data.revNum;
    }
  }).catch(() => {})
  queryTemplateVersionDetailAPI({ id: revId.value }).then(res => {
    if (res.code === 200) {
      if (res.data.bpmJson !== undefined) {
        const bpmJson = JSON.parse(res.data.bpmJson);
        nodes.value = bpmJson.nodes || [];
        sequences.value = bpmJson.sequences || [];
        noticeConfig.value = bpmJson.notices ? bpmJson.notices : noticeConfig.value;
        nodeTaskConfig.value = bpmJson.nodeTasks || [];
        calcCanvasSize();
      }
      getFormFieldList(res.data.formId);
    }
  }).catch(() => {})
})

const getFormFieldList = (formId) => {
  queryFormFieldListAPI({ formId }).then(res => {
    if (res.code === 200 && res.data !== undefined) {
      fieldOptions.value = res.data.map(item => ({ label: item.label, value: item.code, type: item.type }));
    }
  }).catch(() => {})
}

const nodes = ref([])
const sequences = ref([])
const selectedNode = ref(null)
const selectedSequence = ref(null)
const canvasWidth = ref(2000)
const canvasHeight = ref(1000)
const showClearConfirm = ref(false)

const notificationConfigRef = ref(null)
const noticeConfig = ref([
  { type: 2, label: '待办通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，请及时审批！', recipient: '#{base:appAssignee}#' },
  { type: 3, label: '完结通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，已审批完结！', recipient: '#{base:appUser}#' },
  { type: 4, label: '驳回通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，已被驳回！', recipient: '#{base:appUser}#' },
  { type: 5, label: '催办通知', enabled: true, content: '#{base:appUserName}# 申请的 #{base:flowName}#，正在催促您审批，请及时处理！', recipient: '#{base:appAssignee}#' }
])

const nodeTaskConfig = ref([])

const calcCanvasSize = () => {
  const PADDING = 200;
  const nodeWidths = { exclusiveGateway: 80, startEvent: 120, endEvent: 120 };
  const nodeHeights = { exclusiveGateway: 50, startEvent: 48, endEvent: 48 };
  const defaultNodeWidth = 110, defaultNodeHeight = 72;
  let maxX = 0, maxY = 0;
  nodes.value.forEach(node => {
    maxX = Math.max(maxX, node.x + (nodeWidths[node.type] || defaultNodeWidth));
    maxY = Math.max(maxY, node.y + (nodeHeights[node.type] || defaultNodeHeight));
  });
  canvasWidth.value = Math.max(maxX + PADDING, 800);
  canvasHeight.value = Math.max(maxY + PADDING, 600);
};

watch(() => nodes.value.length, () => calcCanvasSize());

/** 左侧节点面板分组配置 */
const flowNodeGroups = [
  {
    name: 'baseNode',
    title: '基础节点',
    nodes: ['startEvent', 'endEvent', 'exclusiveGateway', 'userTask'].map(type => ({
      type,
      name: NODE_META[type]?.name || type,
      icon: NODE_META[type]?.icon,
      color: NODE_META[type]?.color,
      bgColor: NODE_META[type]?.bgColor
    }))
  }
];

const handleCanvasDrop = ({ nodeType, x, y }) => {
  if (!nodeType) return;
  const meta = NODE_META[nodeType] || {}
  const nodeName = meta.name || nodeType
  const width = meta.width || 110
  const height = meta.height || 72
  nodes.value.push({
    id: `n_${Date.now()}`,
    type: nodeType,
    x: x - width / 2,
    y: y - height / 2,
    name: nodeName,
    assigneeType: nodeType === 'userTask' ? '' : undefined,
    assignee: nodeType === 'userTask' ? '' : undefined,
    assigneeLabel: nodeType === 'userTask' ? '' : undefined,
    approveType: nodeType === 'userTask' ? '1' : undefined,
    urgeEnabled: nodeType === 'userTask' ? false : undefined,
    urgeInterval: nodeType === 'userTask' ? 8 : undefined,
    permission: nodeType === 'userTask' ? 15 : undefined
  })
}

const handleCanvasStartSequence = ({ sourceId, targetId }) => {
  sequences.value.push({
    id: `s_${Date.now()}`,
    sourceRef: sourceId,
    targetRef: targetId,
    name: '',
    conditionExpression: '' })
  ElMessage.success('连线创建成功')
}

/** DesignerCanvas 连线创建事件适配（(sourceId, targetId) → { sourceId, targetId }） */
const handleConnect = (sourceId, targetId) => {
  handleCanvasStartSequence({ sourceId, targetId })
}

const selectNode = (node) => { selectedNode.value = node; selectedSequence.value = null }
const selectSequence = (sequence) => { selectedSequence.value = sequence; selectedNode.value = null }

/** 关闭右侧属性面板 */
const closePanel = () => { selectedNode.value = null; selectedSequence.value = null }

/** 画布节点拖动：更新节点坐标 */
const handleMoveNode = (nodeId, x, y) => {
  const node = nodes.value.find(n => n.id === nodeId)
  if (node) {
    node.x = Math.round(x)
    node.y = Math.round(y)
  }
}

const deleteNode = (id) => {
  nodes.value = nodes.value.filter(el => el.id !== id)
  sequences.value = sequences.value.filter(conn => conn.sourceRef !== id && conn.targetRef !== id)
  nodeTaskConfig.value = nodeTaskConfig.value.filter(task => task.nodeId !== id)
  if (selectedNode.value?.id === id) selectedNode.value = null
  ElMessage.success('节点已删除')
}

const deleteSequence = (id) => {
  sequences.value = sequences.value.filter(seq => seq.id !== id)
  selectedSequence.value = null
  ElMessage.success('连线已删除')
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

const saveProcess = () => {
  const json = generateBpmnJson()
  createTemplateVersionAPI({ templateId: templateId.value, bpmJson: JSON.stringify(json) }).then(res => {
    if (res.code === 200) {
      ElMessage.success("流程模板保存成功");
      // 保存后自动获取最新版本号
      queryTemplateDetailAPI({ id: templateId.value }).then(detailRes => {
        if (detailRes.code === 200 && detailRes.data) {
          templateName.value = detailRes.data.name || templateName.value;
          revId.value = detailRes.data.revId || revId.value;
          revNum.value = detailRes.data.revNum || '';
        }
      }).catch(() => {})
    }
  }).catch(() => {})
}

const confirmClear = () => {
  nodes.value = []; sequences.value = []; selectedNode.value = null; selectedSequence.value = null; showClearConfirm.value = false
  nodeTaskConfig.value = []
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
  background: #fff;
  border-bottom: 1px solid $border-color;
  flex-shrink: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .toolbar-title {
      font-size: 16px;
      font-weight: 600;
      color: $color-text-primary;
    }
  }

  .toolbar-right {
    display: flex;
    gap: $spacing-sm;
  }
}

.designer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// 底部状态栏
.designer-footer {
  height: 28px;
  padding: 0 $spacing-md;
  display: flex;
  align-items: center;
  background: #fff;
  border-top: 1px solid $border-color;
  font-size: 12px;
  color: $color-text-secondary;
  flex-shrink: 0;
}
</style>
