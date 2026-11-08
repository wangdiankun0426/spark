/**
 * 流程自动布局：把「节点 + 连线」解析成纵向单链，条件分支展开成并排泳道
 *
 * 设计约束（见流程设计器改版设计稿）：
 * 1. 主干单进单出，顺序由上往下排布，节点位置完全由连线推导，不再自由拖拽
 * 2. 条件分支是唯一的一对多：分支节点下方按出边数量展开成泳道，末端汇合到同一个节点
 * 3. 布局结果同时用于编辑画布与只读预览，保证两处视觉一致
 */
import { NODE_META } from '../nodes'
import { formatConditionLines } from './flowCondition'

/** 各类型节点默认尺寸（节点自身未指定时回退到元数据） */
const DEFAULT_WIDTH = 128
const DEFAULT_HEIGHT = 60
/** 纵向行间距 */
const GAP_Y = 42
/** 泳道横向间距 */
const LANE_GAP = 28
/** 分支条件标签单行高度（与样式中的 line-height 保持一致） */
const CHIP_LINE_H = 16
/** 分支条件标签上下内边距 + 边框合计高度 */
const CHIP_PAD_Y = 10
/** 分支条件标签单行最大文案宽度：超出后换行，多条件逐行展示不截断 */
const CHIP_TEXT_WIDTH = 208
/** 分支条件标签与泳道首节点间距 */
const CHIP_GAP = 16
/** 画布左右留白 */
const PAD_X = 40
/** 画布上/下留白 */
const PAD_TOP = 32
const PAD_BOTTOM = 40
/** 内容最小宽度 */
const MIN_WIDTH = 520
/** 泳道最小宽度 */
const MIN_LANE_WIDTH = 150
/** 泳道最大宽度：容得下折行后的最长条件文案，保证标签不被裁切 */
const MAX_LANE_WIDTH = 240
/** 条件分支至少展示的分支数：不足的部分渲染为空分支占位（编辑态可点击添加节点） */
const MIN_BRANCHES = 2
/** 遍历保护上限，防止脏数据造成死循环 */
const WALK_LIMIT = 200

/**
 * 估算单个字符宽度（11px 字号下中文字符按一字宽、西文按 0.56 字宽）
 * @param ch 字符
 * @returns {number} 估算像素宽度
 */
function estimateCharWidth(ch) {
  // 中日韩文字与全角符号按整字宽，其余按半字宽
  return /[\u2e80-\u9fff\u3000-\u303f\uff00-\uffef]/.test(ch) ? 11 : 6.2
}

/**
 * 估算条件文案宽度
 * @param text 文案
 * @returns {number} 估算像素宽度
 */
function estimateTextWidth(text) {
  let width = 0
  for (const ch of String(text)) {
    width += estimateCharWidth(ch)
  }
  return width
}

/**
 * 文案按宽度折行：条件较长时折成多行展示，保证内容完整不被省略号截断
 * 优先在空格处断行，避免把「大于」「单据状态」这类词拆开
 * @param text 文案
 * @param maxWidth 单行最大宽度
 * @returns {string[]} 折行后的文案行
 */
function wrapText(text, maxWidth) {
  const lines = []
  let current = ''
  let width = 0
  for (const ch of String(text)) {
    const chWidth = estimateCharWidth(ch)
    if (current && width + chWidth > maxWidth) {
      const space = current.lastIndexOf(' ')
      if (space > 0) {
        lines.push(current.slice(0, space).trim())
        current = current.slice(space + 1)
        width = estimateTextWidth(current)
      } else {
        lines.push(current.trim())
        current = ''
        width = 0
      }
    }
    current += ch
    width += chWidth
  }
  if (current.trim()) lines.push(current.trim())
  return lines
}

/**
 * 条件标签的展示行：先按「且/或」拆成多条件，再各自折行，逐行完整展示
 * @param expression 条件表达式
 * @param fieldOptions 表单字段选项（label/value）
 * @returns {string[]} 展示行列表
 */
function buildChipLines(expression, fieldOptions) {
  return formatConditionLines(expression, fieldOptions)
      .flatMap(line => wrapText(line, CHIP_TEXT_WIDTH))
}

/**
 * 节点渲染尺寸
 * @param node 节点
 * @returns {{w: number, h: number}} 宽高
 */
function getNodeSize(node) {
  const meta = NODE_META[node.type] || {}
  return {
    w: node.width || meta.width || DEFAULT_WIDTH,
    h: node.height || meta.height || DEFAULT_HEIGHT
  }
}

/**
 * 把一条出边展开成泳道：从分支节点沿单出边一路向下走到汇合点或结束节点
 * @param edge 分支节点的出边
 * @param ctx 布局上下文（节点索引、出入边索引、已访问集合、字段选项）
 * @returns {{nodes: Array, label: string, edge: object}} 泳道节点、条件文案与所属连线
 */
function buildLane(edge, ctx) {
  const { byId, outMap, inMap, visited, fieldOptions } = ctx
  const lines = buildChipLines(edge.conditionExpression, fieldOptions)
  const lane = {
    nodes: [],
    // 多条件逐行展示，长条件自动折行（label 仅用于 title 提示与宽度估算）
    lines,
    label: lines.join(' '),
    edge
  }
  let cur = byId.get(edge.targetRef)
  let guard = 0
  while (cur && !visited.has(cur.id) && guard++ < WALK_LIMIT) {
    // 结束节点与多入边的汇合点归属主干，不并入泳道
    if (cur.type === 'endEvent') break
    if (inMap.get(cur.id).length > 1) break
    visited.add(cur.id)
    lane.nodes.push(cur)
    const nexts = outMap.get(cur.id).filter(s => !visited.has(s.targetRef))
    if (nexts.length !== 1) break
    cur = byId.get(nexts[0].targetRef)
  }
  return lane
}

/**
 * 求多条泳道的汇合点：所有泳道末端出边（空泳道取其自身连线）指向同一个节点时返回该节点
 * @param lanes 泳道列表
 * @param outMap 出边索引
 * @param byId 节点索引
 * @param visited 已访问集合
 * @returns {object|null} 汇合节点，无法汇合时返回 null
 */
function resolveJoin(lanes, outMap, byId, visited) {
  const candidates = new Map()
  lanes.forEach(lane => {
    const tail = lane.nodes[lane.nodes.length - 1]
    // 空泳道没有节点，连线本身就指向汇合点
    const outs = tail ? outMap.get(tail.id) : (lane.edge ? [lane.edge] : [])
    outs.forEach(edge => {
      if (visited.has(edge.targetRef)) return
      candidates.set(edge.targetRef, (candidates.get(edge.targetRef) || 0) + 1)
    })
  })
  if (candidates.size === 1) {
    const [joinId] = [...candidates.keys()]
    return byId.get(joinId) || null
  }
  return null
}

/**
 * 解析渲染块序列：主干节点块 + 分支泳道块
 * @param nodes 节点列表
 * @param ctx 布局上下文
 * @returns {Array} 渲染块列表
 */
function resolveBlocks(nodes, ctx) {
  const { byId, outMap, inMap, visited, fieldOptions } = ctx
  const blocks = []
  if (!nodes.length) return blocks

  // 起点优先取无入边的开始节点，其次任意开始节点，再次无入边节点
  const start = nodes.find(n => n.type === 'startEvent' && !inMap.get(n.id).length)
      || nodes.find(n => n.type === 'startEvent')
      || nodes.find(n => !inMap.get(n.id).length)
      || nodes[0]

  let cur = start
  let guard = 0
  while (cur && !visited.has(cur.id) && guard++ < WALK_LIMIT) {
    visited.add(cur.id)
    blocks.push({ kind: 'node', node: cur })

    // 条件分支：出边各自展开成泳道，泳道结束后回到主干继续往下
    if (cur.type === 'exclusiveGateway') {
      const laneEdges = outMap.get(cur.id).filter(s => !visited.has(s.targetRef))
      const lanes = laneEdges.map(s => buildLane(s, ctx))
      const join = resolveJoin(lanes, outMap, byId, visited)
      // 分支数不足时补空分支（没有任何连线）：编辑态渲染为「添加节点」占位卡
      while (lanes.length < MIN_BRANCHES) {
        lanes.push({ nodes: [], lines: [], label: '', edge: null })
      }
      blocks.push({ kind: 'lanes', gatewayId: cur.id, joinId: join?.id || null, lanes })
      if (!join) break
      cur = join
      continue
    }

    const nexts = outMap.get(cur.id).filter(s => !visited.has(s.targetRef))
    if (nexts.length !== 1) break
    cur = byId.get(nexts[0].targetRef)
  }

  // 未参与主干的游离节点按顺序追加到末尾，避免叠在原点
  nodes.forEach(node => {
    if (!visited.has(node.id)) {
      visited.add(node.id)
      blocks.push({ kind: 'node', node })
    }
  })
  return blocks
}

/**
 * 计算流程布局
 * @param nodes 节点列表
 * @param sequences 连线列表（sourceRef / targetRef / conditionExpression）
 * @param options {fieldOptions, minWidth}
 * @returns {{positions: Object, lanes: Array, hiddenEdges: Array, width: number, height: number}}
 *          positions: 节点 id -> {x, y, w, h}；lanes: 分支条件标签与空分支占位卡；
 *          hiddenEdges: 空分支的连线 id（不绘制，避免出现默认连到结束节点的线）
 */
export function layoutFlow(nodes, sequences, options = {}) {
  const { fieldOptions = [], minWidth = 0 } = options
  const byId = new Map(nodes.map(n => [n.id, n]))
  const outMap = new Map()
  const inMap = new Map()
  nodes.forEach(n => {
    outMap.set(n.id, [])
    inMap.set(n.id, [])
  })
  sequences.forEach(seq => {
    if (!byId.has(seq.sourceRef) || !byId.has(seq.targetRef)) return
    outMap.get(seq.sourceRef).push(seq)
    inMap.get(seq.targetRef).push(seq)
  })

  const ctx = { byId, outMap, inMap, visited: new Set(), fieldOptions }
  const blocks = resolveBlocks(nodes, ctx)

  // 第一遍：量尺寸（泳道宽度取泳道内最宽节点）
  let contentWidth = 0
  blocks.forEach(block => {
    if (block.kind === 'node') {
      contentWidth = Math.max(contentWidth, getNodeSize(block.node).w)
      return
    }
    block.lanes.forEach(lane => {
      const widths = lane.nodes.map(n => getNodeSize(n).w)
      // 泳道宽度同时容纳节点与条件文案：条件已按 CHIP_TEXT_WIDTH 折行，取最长一行
      const labelWidth = lane.lines.length
          ? Math.min(Math.max(...lane.lines.map(estimateTextWidth)) + 28, MAX_LANE_WIDTH)
          : 0
      lane.w = Math.max(MIN_LANE_WIDTH, ...widths, labelWidth)
      // 条件标签高度随行数增长（单行 24px，每多一行 +16px）
      lane.chipH = Math.max(1, lane.lines.length) * CHIP_LINE_H + CHIP_PAD_Y
      // 空分支按一个节点的高度预留占位卡位置
      lane.h = lane.nodes.length
          ? lane.nodes.reduce((acc, node, i) => acc + getNodeSize(node).h + (i ? GAP_Y : 0), 0)
          : DEFAULT_HEIGHT
    })
    block.w = block.lanes.reduce((acc, lane, i) => acc + lane.w + (i ? LANE_GAP : 0), 0)
    // 同一分支块内条件标签高度对齐，保证各泳道首节点仍在同一水平线上
    block.chipH = Math.max(...block.lanes.map(lane => lane.chipH))
    block.h = block.chipH + CHIP_GAP + Math.max(0, ...block.lanes.map(lane => lane.h))
    contentWidth = Math.max(contentWidth, block.w)
  })

  const width = Math.max(MIN_WIDTH, minWidth, contentWidth + PAD_X * 2)

  // 第二遍：自上而下定位，块内水平居中
  const positions = {}
  // 空分支的连线不画（否则会出现一条默认连到结束节点的线）
  const hiddenEdges = []
  /** 泳道条件标签 + 空分支占位卡 */
  const lanes = []
  let cursorY = PAD_TOP
  blocks.forEach(block => {
    if (block.kind === 'node') {
      const { w, h } = getNodeSize(block.node)
      positions[block.node.id] = {
        x: Math.round((width - w) / 2),
        y: Math.round(cursorY),
        w,
        h
      }
      cursorY += h + GAP_Y
      return
    }
    let laneX = (width - block.w) / 2
    block.lanes.forEach((lane, index) => {
      const pending = !lane.nodes.length
      // 空分支只渲染占位卡，不渲染条件标签
      if (!pending) {
        lanes.push({
          key: `lane-${cursorY}-${index}`,
          x: Math.round(laneX),
          y: Math.round(cursorY),
          w: Math.round(lane.w),
          h: block.chipH,
          lines: lane.lines,
          label: lane.label,
          // 条件标签的点击编辑入口：定位到该分支所属连线与汇合点
          gatewayId: block.gatewayId,
          joinId: block.joinId,
          edgeId: lane.edge?.id || null
        })
      }
      if (pending && lane.edge) hiddenEdges.push(lane.edge.id)
      if (pending) {
        lanes.push({
          key: `lane-slot-${cursorY}-${index}`,
          x: Math.round(laneX),
          y: Math.round(cursorY + block.chipH + CHIP_GAP),
          w: Math.round(lane.w),
          h: lane.h,
          pending: true,
          label: '',
          gatewayId: block.gatewayId,
          joinId: block.joinId,
          edgeId: lane.edge?.id || null
        })
      }
      let nodeY = cursorY + block.chipH + CHIP_GAP
      lane.nodes.forEach(node => {
        const { w, h } = getNodeSize(node)
        positions[node.id] = {
          x: Math.round(laneX + (lane.w - w) / 2),
          y: Math.round(nodeY),
          w,
          h
        }
        nodeY += h + GAP_Y
      })
      laneX += lane.w + LANE_GAP
    })
    cursorY += block.h + GAP_Y
  })

  return {
    positions,
    lanes,
    hiddenEdges,
    width,
    height: Math.max(0, Math.round(cursorY - GAP_Y + PAD_BOTTOM))
  }
}

/**
 * 给节点写入布局坐标（就地修改，供画布直接渲染）
 * @param nodes 节点列表
 * @param positions layoutFlow 返回的坐标表
 */
export function applyPositions(nodes, positions) {
  nodes.forEach(node => {
    const pos = positions[node.id]
    if (!pos) return
    node.x = pos.x
    node.y = pos.y
    node.width = pos.w
    node.height = pos.h
  })
}
