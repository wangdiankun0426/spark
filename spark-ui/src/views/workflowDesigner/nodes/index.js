/**
 * AI 工作流设计器（DAG）节点类型表
 *
 * NODE_META 供节点卡渲染（name/icon/color/bgColor）、节点选择器分组（group/desc）
 * 与画布定位（width/height）使用；userTask 为历史数据兼容类型，不进选择器分组。
 */

export const NODE_SIZE = { width: 196, height: 62 }

export const NODE_META = {
  // 流程控制
  startEvent: { name: '开始', icon: 'VideoPlay', color: '#52c41a', bgColor: '#f6ffed', group: 'flow', desc: '流程起点，接收表单输入' },
  endEvent: { name: '结束', icon: 'CircleClose', color: '#ff4d4f', bgColor: '#fff2f0', group: 'flow', desc: '流程终点，输出执行结果' },
  exclusiveGateway: { name: '条件分支', icon: 'Share', color: '#fa8c16', bgColor: '#fffbe6', group: 'flow', desc: '按条件分流，自动带两条分支' },
  // AI 能力
  llmTask: { name: 'LLM模型', icon: 'Cpu', color: '#004fc5', bgColor: '#e8f0fd', group: 'ai', desc: '调用大模型生成内容' },
  agentTask: { name: 'Agent任务', icon: 'MagicStick', color: '#722ed1', bgColor: '#f9f0ff', group: 'ai', desc: '带工具与记忆的智能体' },
  kbSearch: { name: '知识库检索', icon: 'Search', color: '#eb2f96', bgColor: '#fff0f6', group: 'ai', desc: '向量召回 Top K 片段' },
  kgSearch: { name: '知识图谱', icon: 'Share', color: '#2f54eb', bgColor: '#f0f5ff', group: 'ai', desc: '按实体关系查询图谱' },
  // 系统能力
  docParse: { name: '文档解析', icon: 'Document', color: '#13c2c2', bgColor: '#e6fffb', group: 'system', desc: 'PDF / Word 抽取正文' },
  kbArchive: { name: '知识库归档', icon: 'Collection', color: '#237804', bgColor: '#f6ffed', group: 'system', desc: '文档归档到知识库' },
  notify: { name: '发送通知', icon: 'Bell', color: '#722ed1', bgColor: '#f9f0ff', group: 'system', desc: '站内消息通知指定用户' },
  // 工具
  variableOp: { name: '变量操作', icon: 'Setting', color: '#faad14', bgColor: '#fffbe6', group: 'tool', desc: '变量读写、拼接与格式化' },
  codeExecute: { name: '代码执行', icon: 'Monitor', color: '#1890ff', bgColor: '#e6f7ff', group: 'tool', desc: 'SpEL / Groovy 脚本处理' },
  httpRequest: { name: 'HTTP请求', icon: 'Link', color: '#52c41a', bgColor: '#f6ffed', group: 'tool', desc: '调用外部 HTTP 接口' },
  humanReview: { name: '人工审核', icon: 'UserFilled', color: '#fa8c16', bgColor: '#fffbe6', group: 'tool', desc: '挂起等待人工审核' },
  // 历史数据兼容，不在选择器中提供
  userTask: { name: '用户任务', icon: 'User', color: '#1890ff', bgColor: '#e6f7ff', group: null, desc: '挂起等待用户处理' },
  parallelGateway: { name: '并行网关', icon: 'CopyDocument', color: '#13c2c2', bgColor: '#e6fffb', group: null, desc: '历史并行分支节点，已不再提供新建' },
  // 条件分支自动生成的分支占位：点击后选择要执行的节点类型
  placeholder: { name: '待添加节点', icon: 'Plus', color: '#8fa0bb', bgColor: '#eef2f8', group: null, desc: '点击选择要执行的节点' }
}

export const NODE_GROUPS = [
  { name: 'flow', title: '流程控制' },
  { name: 'ai', title: 'AI能力' },
  { name: 'system', title: '系统能力' },
  { name: 'tool', title: '工具' }
]

/** 节点选择器可选类型：分组存在且非开始/结束端点 */
export const PICKER_TYPES = Object.keys(NODE_META)
  .filter(type => NODE_META[type].group && type !== 'startEvent' && type !== 'endEvent')

function getNodeConfig(node) {
  return node.config || {}
}

function truncate(text, max = 18) {
  const s = String(text)
  return s.length > max ? s.slice(0, max) + '…' : s
}

/**
 * 各类型节点卡第二行摘要文案
 */
const SUMMARY_BUILDERS = {
  startEvent: () => '表单输入入口',
  endEvent: (node) => {
    const outputs = getNodeConfig(node).outputs || []
    return outputs.length ? `输出 ${outputs.length} 项结果` : '流程结果输出'
  },
  exclusiveGateway: () => '条件分支',
  parallelGateway: () => '并行分支',
  placeholder: () => '点击选择节点',
  userTask: () => '用户任务节点',
  llmTask: (node) => {
    const config = getNodeConfig(node)
    if (!config.modelId) return '待配置模型'
    return config.prompt ? '模型与提示词已配置' : '已选模型 · 待配置提示词'
  },
  agentTask: (node) => (getNodeConfig(node).agentId ? '智能体已绑定' : '待绑定智能体'),
  kbSearch: (node) => {
    const config = getNodeConfig(node)
    return config.kbIds ? `Top ${config.topK || 5} 片段召回` : '待选择知识库'
  },
  kgSearch: (node) => (getNodeConfig(node).graphId ? '图谱已绑定' : '待选择知识图谱'),
  docParse: (node) => (getNodeConfig(node).fileCode ? '已绑定文件字段' : '待绑定文件'),
  kbArchive: (node) => (getNodeConfig(node).knowledgeId ? '归档目标已配置' : '待配置归档目标'),
  notify: (node) => (getNodeConfig(node).userCode ? '通知对象已配置' : '待配置通知对象'),
  variableOp: (node) => {
    const variables = getNodeConfig(node).variables || []
    return variables.length ? `${variables.length} 个变量操作` : '待配置变量'
  },
  codeExecute: (node) => {
    const config = getNodeConfig(node)
    if (!config.code) return '待编写脚本'
    return config.codeType === 'groovy' ? 'Groovy 脚本已编写' : 'SpEL 脚本已编写'
  },
  httpRequest: (node) => {
    const config = getNodeConfig(node)
    return config.url ? `${config.method || 'GET'} ${truncate(config.url, 14)}` : '待配置请求地址'
  },
  humanReview: (node) => (getNodeConfig(node).reviewerIds ? '审核人已配置' : '待配置审核人')
}

/**
 * 生成节点卡第二行摘要文案
 * @param node 节点数据
 * @returns {string}
 */
export function getNodeSummary(node) {
  const builder = SUMMARY_BUILDERS[node.type]
  if (builder) return builder(node)
  return NODE_META[node.type]?.desc || ''
}
