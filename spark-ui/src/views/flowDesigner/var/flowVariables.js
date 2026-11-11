/**
 * 流程设计器变量引用的来源计算
 * 流程变量只有表单值与基础数据两类来源，Flowable 节点不产出可引用输出，故无节点数据页签
 */
export const FLOW_VARS_KEY = Symbol('flowVarGroups')

/** 基础数据：流程运行时由后端注入的上下文 */
const BASE_VARS = [
  { label: '申请人', value: '#{base:appUserName}#' },
  { label: '流程名称', value: '#{base:flowName}#' },
  { label: '发起人', value: '#{base:appUser}#' },
  { label: '当前审批人', value: '#{base:appAssignee}#' }
]

/**
 * 构建变量下拉的分组选项
 * @param formFields 绑定表单字段 [{label, value}]，value 为字段编码
 * @returns {Array<{type: string, title: string, items: Array}>} 分组列表
 */
export function buildVarGroups(formFields) {
  const groups = []
  const fields = formFields || []
  if (fields.length) {
    groups.push({
      type: 'form',
      title: '表单值',
      items: fields.map(f => ({ label: f.label, value: `#{form:${f.value}}#` }))
    })
    groups.push({
      type: 'formTxt',
      title: '显示值',
      items: fields.map(f => ({ label: f.label, value: `#{formTxt:${f.value}}#` }))
    })
  }
  groups.push({ type: 'base', title: '基础数据', items: BASE_VARS.map(v => ({ label: v.label, value: v.value })) })
  return groups
}
