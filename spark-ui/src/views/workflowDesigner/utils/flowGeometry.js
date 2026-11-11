/**
 * 工作流画布几何计算：纯函数，编辑画布与只读预览共用
 * 输入节点统一带绝对坐标 { x, y }，连线统一带 { source, target }
 */
import { NODE_SIZE, getNodeOutputs } from '../nodes/index.js'
import { buildConditionChipLines, CONDITION_LINE_MAX_WIDTH } from './flowCondition.js'

/** 条件 chip 排版常量：行高、上下内边距、横向内边距、圆角 */
export const CHIP_LINE_H = 15
export const CHIP_PAD_Y = 5
export const CHIP_PAD_X = 8
export const CHIP_RADIUS = 7

/** 分支尚未配条件时，线上显示的入口文案 */
export const CHIP_BLANK_TEXT = '＋ 分支条件'

/** 保留一位小数，避免 SVG path 出现超长浮点串 */
export const round1 = (num) => Math.round(num * 10) / 10

export function findNodeById(nodes, id) {
  return (nodes || []).find(n => n.id === id) || null
}

/** 连线锚点：固定从源节点右端口出发，接入目标节点左端口，目标侧预留 7px 给箭头 */
export function getEdgeAnchors(nodes, edge) {
  const source = findNodeById(nodes, edge?.source)
  const target = findNodeById(nodes, edge?.target)
  if (!source || !target) return null
  return {
    source,
    target,
    sp: { x: source.x + NODE_SIZE.width + 1, y: source.y + NODE_SIZE.height / 2 },
    tp: { x: target.x - 7, y: target.y + NODE_SIZE.height / 2 }
  }
}

/** 贝塞尔连线路径：水平切线 C 命令，跨行时控制点距离 28~64 */
export function bezierPath(sp, tp) {
  if (sp.y === tp.y) {
    return `M ${round1(sp.x)} ${round1(sp.y)} L ${round1(tp.x)} ${round1(tp.y)}`
  }
  const dx = Math.abs(tp.x - sp.x)
  const c = Math.max(28, Math.min(64, dx / 2))
  return `M ${round1(sp.x)} ${round1(sp.y)} C ${round1(sp.x + c)} ${round1(sp.y)}, ${round1(tp.x - c)} ${round1(tp.y)}, ${round1(tp.x)} ${round1(tp.y)}`
}

export function getEdgePath(nodes, edge) {
  const anchors = getEdgeAnchors(nodes, edge)
  if (!anchors) return ''
  return bezierPath(anchors.sp, anchors.tp)
}

export function edgeMidpoint(nodes, edge) {
  const anchors = getEdgeAnchors(nodes, edge)
  if (!anchors) return { x: 0, y: 0 }
  return {
    x: (anchors.sp.x + anchors.tp.x) / 2,
    y: (anchors.sp.y + anchors.tp.y) / 2
  }
}

/** 分叉圆点：同一节点存在多条出线时在出端口画点 */
export function buildGatewayForks(nodes, sequences) {
  const outCount = {}
  ;(sequences || []).forEach(e => {
    outCount[e.source] = (outCount[e.source] || 0) + 1
  })
  return (nodes || [])
      .filter(node => (outCount[node.id] || 0) > 1)
      .map(node => ({
        id: node.id,
        x: node.x + NODE_SIZE.width,
        y: node.y + NODE_SIZE.height / 2
      }))
}

/** 是否已配置条件：分支出线与普通连线都能挂条件 */
export function hasCondition(edge) {
  return !!String(edge?.conditionExpression || '').trim()
}

/** 估算单行文案宽度，单位 px，与折行估算口径一致 */
export function measureText(text) {
  let w = 0
  for (const ch of String(text)) {
    w += /[\u2e80-\u9fff\u3000-\u303f\uff00-\uffef]/.test(ch) ? 11 : 6.2
  }
  return w
}

/** 排他网关条件分支的出线 */
export function isBranchEdge(nodes, edge) {
  const source = findNodeById(nodes, edge?.source)
  return !!source && source.type === 'exclusiveGateway'
}

/** 条件文案的字段名映射：表单字段叠加全部节点输出的扁平直拼键，连线标签据此显示参数名 */
function buildLabelOptions(nodes, fieldOptions) {
  const options = [...(fieldOptions || [])]
  ;(nodes || []).forEach(node => {
    getNodeOutputs(node).forEach(out => {
      const key = out.value.replace(/^#\{node:/, '').replace(/\}#$/, '')
      options.push({ label: out.label, value: key })
    })
  })
  return options
}

/**
 * 连线上的分支标签
 * - configured：已配条件，多条件按「且 / 或」拆行并折行，全部展示
 * - blank：分支出线尚未配条件，给出可点的空态入口，只读预览传 blankEntry: false 关掉
 * - mergeName：分支名称并入标签首行，避免名称与条件框各占一块互相压字
 */
export function buildConditionChips({ nodes, sequences, fieldOptions = [], blankEntry = true, mergeName = true }) {
  const labelOptions = buildLabelOptions(nodes, fieldOptions)
  const chips = []
  ;(sequences || []).forEach(edge => {
    const configured = hasCondition(edge)
    if (!configured && !(blankEntry && isBranchEdge(nodes, edge))) return

    const anchors = getEdgeAnchors(nodes, edge)
    const mid = edgeMidpoint(nodes, edge)
    const hasName = mergeName && !!String(edge.name || '').trim()
    let labelLines
    if (configured) {
      const lines = buildConditionChipLines(edge.conditionExpression, labelOptions)
      labelLines = lines.length ? lines : [String(edge.conditionExpression || '')]
    } else {
      labelLines = [CHIP_BLANK_TEXT]
    }
    // 分支名称作为首行，条件行顺延，靠 isName 标记区分样式
    const rows = hasName
        ? [{ text: edge.name, isName: true }, ...labelLines.map(text => ({ text }))]
        : labelLines.map(text => ({ text }))
    const width = Math.min(
        Math.ceil(Math.max(...rows.map(row => measureText(row.text))) + CHIP_PAD_X * 2),
        CONDITION_LINE_MAX_WIDTH + CHIP_PAD_X * 2
    )
    const height = rows.length * CHIP_LINE_H + CHIP_PAD_Y * 2
    const multi = rows.length > 1 || hasName
    // 标签尽量居中，但两侧各留 6px 不压到源/目标节点；连线太短时退回居中
    const half = width / 2
    const minX = anchors ? anchors.sp.x + half + 6 : mid.x
    const maxX = anchors ? anchors.tp.x - half - 6 : mid.x
    const chipX = maxX >= minX ? Math.min(Math.max(mid.x, minX), maxX) : (minX + maxX) / 2
    chips.push({
      edgeId: edge.id,
      edge,
      blank: !configured,
      // 多行时以连线中点为中心上下展开，单行仍贴在连线下方
      x: chipX,
      y: rows.length > 1 ? mid.y + 8 - (height - 20) / 2 : mid.y + 8,
      width,
      height,
      multi,
      radius: height > 24 ? CHIP_RADIUS : 7,
      textX: multi ? chipX - width / 2 + CHIP_PAD_X : chipX,
      lines: rows.map((row, i) => ({
        text: row.text,
        isName: !!row.isName,
        // 基线落在行高中下部，视觉上与行框居中对齐
        y: CHIP_PAD_Y + i * CHIP_LINE_H + 11
      }))
    })
  })
  return chips
}
