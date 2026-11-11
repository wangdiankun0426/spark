/**
 * 工作流变量引用的来源计算
 * 配置面板与分支条件的节点数据下拉均展示全部节点输出，口径一致
 */
import { NODE_META, getNodeOutputs } from '../nodes/index.js'

export const WF_VARS_KEY = Symbol('wfVarGroups')

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
 * 构建分支条件「节点数据」字段下拉选项，展示全部节点输出，value 为 SpEL 扁平直拼键
 * @param nodes 全部节点
 * @returns {Array<{nodeId: string, name: string, options: Array<{label: string, value: string}>}>} 按节点分组的选项
 */
export function buildNodeFieldOptions(nodes) {
  return buildAllNodeGroups(nodes).map(g => ({
    nodeId: g.nodeId,
    name: g.name,
    options: g.items.map(item => ({
      label: item.label,
      value: item.value.replace(/^#\{node:/, '').replace(/\}#$/, '')
    }))
  }))
}