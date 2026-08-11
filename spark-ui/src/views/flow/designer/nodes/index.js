/**
 * 节点类型组件库：每种节点类型独立组件，自带包裹/颜色/图标/标签等全部样式
 * 流程设计器（Flowable）与工作流设计器（AI DAG）共用同一套组件，
 * 通过 type 名映射到对应组件（start/startEvent 等为同一视觉组件）。
 */
import StartNode from './StartNode.vue'
import EndNode from './EndNode.vue'
import GatewayNode from './GatewayNode.vue'
import UserTaskNode from './UserTaskNode.vue'
import LlmTaskNode from './LlmTaskNode.vue'

export const NODE_COMPONENTS = {
  // 开始（流程设计器与工作流设计器共用）
  startEvent: StartNode,
  // 结束
  endEvent: EndNode,
  // 条件分支
  exclusiveGateway: GatewayNode,
  // 用户任务（含审批人标签）
  userTask: UserTaskNode,
  // LLM 模型（工作流设计器）
  llmTask: LlmTaskNode
}

/**
 * 节点类型元数据（统一维护）：名称 + 默认尺寸 + 图标/配色
 * 画布定位/连线计算用 size；节点面板与兜底渲染用 icon/color/bgColor；
 * 画布中节点包裹样式由节点类型组件负责
 */
export const NODE_META = {
  startEvent: { name: '开始', icon: 'VideoPlay', color: '#52c41a', bgColor: '#f6ffed', width: 120, height: 48 },
  endEvent: { name: '结束', icon: 'CircleClose', color: '#ff4d4f', bgColor: '#fff2f0', width: 120, height: 48 },
  exclusiveGateway: { name: '条件分支', icon: 'Share', color: '#fa8c16', bgColor: '#fffbe6', width: 108, height: 72 },
  userTask: { name: '用户任务', icon: 'User', color: '#1890ff', bgColor: '#e6f7ff', width: 128, height: 72 },
  serviceTask: { name: 'AI工作流', icon: 'Cpu', color: '#722ed1', bgColor: '#f9f0ff', width: 128, height: 72 },
  llmTask: { name: 'LLM模型', icon: 'Cpu', color: '#004fc5', bgColor: '#e8f0fd', width: 160, height: 60 }
}
