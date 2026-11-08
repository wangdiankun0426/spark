/**
 * AI 工作流设计器（DAG）节点类型表
 *
 * 元数据同时供左侧节点面板（name/icon/color/bgColor）、画布定位（width/height）
 * 与未配置组件时的兜底渲染使用。
 */
import StartNode from './StartNode.vue'
import EndNode from './EndNode.vue'
import GatewayNode from './GatewayNode.vue'
import UserTaskNode from './UserTaskNode.vue'
import LlmTaskNode from './LlmTaskNode.vue'

export const NODE_COMPONENTS = {
  // 开始
  startEvent: StartNode,
  // 结束
  endEvent: EndNode,
  // 条件分支
  exclusiveGateway: GatewayNode,
  // 用户任务
  userTask: UserTaskNode,
  // LLM 模型
  llmTask: LlmTaskNode
}

/**
 * 节点类型元数据
 * notify（发送通知）、并行网关与各类 AI／工具节点没有专属组件，画布走兜底渲染
 */
export const NODE_META = {
  // 流程控制
  startEvent: { name: '开始', icon: 'VideoPlay', color: '#52c41a', bgColor: '#f6ffed', width: 128, height: 60 },
  endEvent: { name: '结束', icon: 'CircleClose', color: '#ff4d4f', bgColor: '#fff2f0', width: 128, height: 60 },
  exclusiveGateway: { name: '条件分支', icon: 'Share', color: '#fa8c16', bgColor: '#fffbe6', width: 128, height: 60 },
  userTask: { name: '用户任务', icon: 'User', color: '#1890ff', bgColor: '#e6f7ff', width: 128, height: 86 },
  parallelGateway: { name: '并行网关', icon: 'CopyDocument', color: '#13c2c2', bgColor: '#e6fffb', width: 128, height: 60 },
  // AI 能力
  llmTask: { name: 'LLM模型', icon: 'Cpu', color: '#004fc5', bgColor: '#e8f0fd', width: 128, height: 60 },
  agentTask: { name: 'Agent任务', icon: 'MagicStick', color: '#722ed1', bgColor: '#f9f0ff', width: 128, height: 60 },
  kbSearch: { name: '知识库检索', icon: 'Search', color: '#eb2f96', bgColor: '#fff0f6', width: 128, height: 60 },
  kgSearch: { name: '知识图谱', icon: 'Share', color: '#2f54eb', bgColor: '#f0f5ff', width: 128, height: 60 },
  // 系统能力
  docParse: { name: '文档解析', icon: 'Document', color: '#13c2c2', bgColor: '#e6fffb', width: 128, height: 60 },
  kbArchive: { name: '知识库归档', icon: 'Collection', color: '#237804', bgColor: '#f6ffed', width: 128, height: 60 },
  notify: { name: '发送通知', icon: 'Bell', color: '#722ed1', bgColor: '#f9f0ff', width: 128, height: 60 },
  // 工具
  variableOp: { name: '变量操作', icon: 'Setting', color: '#faad14', bgColor: '#fffbe6', width: 128, height: 60 },
  codeExecute: { name: '代码执行', icon: 'Monitor', color: '#1890ff', bgColor: '#e6f7ff', width: 128, height: 60 },
  httpRequest: { name: 'HTTP请求', icon: 'Link', color: '#52c41a', bgColor: '#f6ffed', width: 128, height: 60 },
  humanReview: { name: '人工审核', icon: 'UserFilled', color: '#fa8c16', bgColor: '#fffbe6', width: 128, height: 60 }
}
