/**
 * 流程设计器（Flowable）节点类型表
 *
 * 每种类型一个独立组件，自带包裹/颜色/图标/标签等全部样式；
 * 元数据同时供画布定位（width/height）、未配置组件时的兜底渲染与右键菜单
 * （name/icon/color/bgColor）使用。
 *
 */
import StartNode from './StartNode.vue'
import EndNode from './EndNode.vue'
import GatewayNode from './GatewayNode.vue'
import UserTaskNode from './UserTaskNode.vue'

export const NODE_COMPONENTS = {
  // 开始
  startEvent: StartNode,
  // 结束
  endEvent: EndNode,
  // 条件分支
  exclusiveGateway: GatewayNode,
  // 用户任务
  userTask: UserTaskNode
}

/**
 * 节点类型元数据：名称 + 默认尺寸 + 图标/配色
 * notify（发送通知）没有专属组件，画布走兜底渲染，因此只需要元数据
 */
export const NODE_META = {
  startEvent: { name: '开始', icon: 'VideoPlay', color: '#52c41a', bgColor: '#f6ffed', width: 128, height: 60 },
  endEvent: { name: '结束', icon: 'CircleClose', color: '#ff4d4f', bgColor: '#fff2f0', width: 128, height: 60 },
  exclusiveGateway: { name: '条件分支', icon: 'Share', color: '#fa8c16', bgColor: '#fffbe6', width: 128, height: 60 },
  userTask: { name: '用户任务', icon: 'User', color: '#1890ff', bgColor: '#e6f7ff', width: 128, height: 86 },
  notify: { name: '发送通知', icon: 'Bell', color: '#722ed1', bgColor: '#f9f0ff', width: 128, height: 60 }
}
