/**
 * 工作流变量引用的来源计算
 * 配置面板下拉展示全部节点输出，分支条件下拉按上游可解析集合计算，两者口径不同
 */
import { NODE_META, PASSTHROUGH_TYPES, getNodeOutputs } from '../nodes/index.js'

export const WF_VARS_KEY = Symbol('wfVarGroups')

/**
 * 收集某节点可引用的上游节点输出，按节点分组
 * 仅直接前驱的输出可解析，直传类型节点输出等于输入需继续上溯，执行器节点输出不含上游输入故到此为止
 * @param startId 起点节点 id
 * @param nodes 全部节点
 * @param sequences 全部连线
 * @returns {Array<{nodeId: string, name: string, color: string, items: Array}>} 节点分组列表
 */
function collectUpstream(startId, nodes, sequences) {
  const nodeById = new Map(nodes.map(n => [n.id, n]))
  const preds = id => sequences.filter(e => e.target === id).map(e => e.source)
  const seen = new Set()
  const queue = [...preds(startId)]
  const groups = []
  const byNode = new Map()
  while (queue.length) {
    const id = queue.shift()
    // 防环并排除自身：节点自身输出不在自身输入里
    if (!id || seen.has(id) || id === startId) continue
    seen.add(id)
    const node = nodeById.get(id)
    if (!node) continue
    if (PASSTHROUGH_TYPES.includes(node.type)) {
      queue.push(...preds(id))
      continue
    }
    const outputs = getNodeOutputs(node)
    if (!outputs.length) continue
    let group = byNode.get(id)
    if (!group) {
      group = { nodeId: id, name: node.name, color: NODE_META[node.type]?.color || '', items: [] }
      byNode.set(id, group)
      groups.push(group)
    }
    outputs.forEach(out => {
      // 按完整 value 去重：不同节点共享 text 等键名，不能按键名去重
      if (!group.items.some(item => item.value === out.value)) {
        group.items.push({ label: out.label, value: out.value })
      }
    })
  }
  return groups
}

/**
 * 构建全部节点的输出分组，供变量下拉的节点数据页签展示
 * @param nodes 全部节点
 * @returns {Array<{nodeId: string, name: string, color: string, items: Array}>} 节点分组列表
 */
function buildAllNodeGroups(nodes) {
  const groups = []
  nodes.forEach(node => {
    if (!node) return
    const outputs = getNodeOutputs(node)
    if (!outputs.length) return
    groups.push({
      nodeId: node.id,
      name: node.name,
      color: NODE_META[node.type]?.color || '',
      items: outputs.map(out => ({ label: out.label, value: out.value }))
    })
  })
  return groups
}

/**
 * 构建配置面板变量下拉的分组选项
 * @param node 当前选中节点，为 null 时返回空数组
 * @param nodes 全部节点
 * @param formFields 绑定表单字段 [{label, code}]
 * @returns {Array<{type: string, title: string, items: Array, nodeId?: string, color?: string}>} 分组列表
 */
export function buildVarGroups(node, nodes, formFields) {
  if (!node) return []
  const groups = []
  const fields = formFields || []
  if (fields.length) {
    groups.push({
      type: 'form',
      title: '表单值',
      items: fields.map(f => ({ label: f.label, value: `#{form:${f.code}}#` }))
    })
    groups.push({
      type: 'formTxt',
      title: '表单显示值',
      items: fields.map(f => ({ label: f.label, value: `#{formTxt:${f.code}}#` }))
    })
  }
  buildAllNodeGroups(nodes).forEach(g => {
    groups.push({ type: 'node', title: g.name, nodeId: g.nodeId, color: g.color, items: g.items })
  })
  return groups
}

/**
 * 构建分支条件「节点数据」字段下拉选项，value 为 SpEL 扁平直拼键
 * 锚点是网关自身，条件在网关的输入空间上求值
 * @param startId 网关节点 id
 * @param nodes 全部节点
 * @param sequences 全部连线
 * @returns {Array<{nodeId: string, name: string, options: Array<{label: string, value: string}>}>} 按节点分组的选项
 */
export function buildNodeFieldOptions(startId, nodes, sequences) {
  if (!startId) return []
  return collectUpstream(startId, nodes, sequences).map(g => ({
    nodeId: g.nodeId,
    name: g.name,
    options: g.items.map(item => ({
      label: item.label,
      value: item.value.replace(/^#\{node:/, '').replace(/\}#$/, '')
    }))
  }))
}