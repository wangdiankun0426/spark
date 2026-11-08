/**
 * 流程 DAG 校验
 *
 * 只做「图结构」层面的事情：端点是否齐全、有没有环、节点是否前后连通、分支是否完整。
 * 分为两级：
 *   - error  结构错误，后端部署或运行必然失败（缺开始/结束、有环、节点断链……）
 *   - warn   结构成立但流程还跑不通，属于编辑中的正常中间态（空分支还没放节点）
 * 与画布共用 sourceRef / targetRef 字段，可被流程预览等只读场景复用。
 */

const DEFAULT_SOURCE_KEY = 'sourceRef'
const DEFAULT_TARGET_KEY = 'targetRef'

/** 节点显示名：名称优先，缺名称回落到 id */
function labelOf(node) {
  return (node && (node.name || node.id)) || '未命名节点'
}

/** 名称列表转文案：最多列 3 个，其余合并为「等 N 个」 */
function joinNames(names, max = 3) {
  if (names.length <= max) return names.join('、')
  return `${names.slice(0, max).join('、')} 等 ${names.length} 个`
}

/**
 * 校验流程 DAG
 * @param nodes 节点列表
 * @param sequences 连线列表
 * @param options { sourceKey, targetKey, lanes }
 *        sourceKey / targetKey 为连线起止字段名（默认 sourceRef / targetRef）；
 *        lanes 是画布布局输出的分支泳道（含 gatewayId / pending），用于提示「分支还没放节点」
 * @returns {{ok: boolean, level: 'ok'|'warn'|'error', text: string,
 *            issues: string[], errors: string[], warnings: string[]}}
 *          text 为状态栏展示文案：通过时是「校验通过」，否则取第一条问题
 */
export function validateFlowDag(nodes = [], sequences = [], options = {}) {
  const sourceKey = options.sourceKey || DEFAULT_SOURCE_KEY
  const targetKey = options.targetKey || DEFAULT_TARGET_KEY
  const lanes = options.lanes || []
  const nodeList = (nodes || []).filter(n => n && n.id)
  const errors = []
  const warnings = []

  if (!nodeList.length) {
    return { ok: false, level: 'warn', text: '还没有节点', issues: ['还没有任何节点'], errors, warnings: ['还没有任何节点'] }
  }

  const byId = new Map(nodeList.map(n => [n.id, n]))

  /* ---------- 建图：顺手记录指向已删除节点的脏连线 ---------- */
  const outMap = new Map()
  const inMap = new Map()
  nodeList.forEach(n => {
    outMap.set(n.id, [])
    inMap.set(n.id, [])
  })

  let dangling = 0
  ;(sequences || []).forEach(seq => {
    const source = seq && seq[sourceKey]
    const target = seq && seq[targetKey]
    if (!byId.has(source) || !byId.has(target)) {
      if (source || target) dangling++
      return
    }
    outMap.get(source).push(seq)
    inMap.get(target).push(seq)
  })

  if (dangling) errors.push(`有 ${dangling} 条连线指向不存在的节点`)

  /* ---------- 端点：开始 / 结束 ---------- */
  const startNodes = nodeList.filter(n => n.type === 'startEvent')
  const endNodes = nodeList.filter(n => n.type === 'endEvent')

  if (!startNodes.length) errors.push('缺少开始节点')
  if (!endNodes.length) errors.push('缺少结束节点')
  if (startNodes.length > 1) errors.push(`存在 ${startNodes.length} 个开始节点，只允许 1 个`)

  startNodes.forEach(n => {
    if (inMap.get(n.id).length) errors.push(`开始节点「${labelOf(n)}」不应有入线`)
  })
  endNodes.forEach(n => {
    if (outMap.get(n.id).length) errors.push(`结束节点「${labelOf(n)}」不应有出线`)
  })

  /* ---------- 环路：Kahn 拓扑排序，排不完即存在环 ---------- */
  const indegree = new Map(nodeList.map(n => [n.id, inMap.get(n.id).length]))
  const queue = nodeList.filter(n => indegree.get(n.id) === 0).map(n => n.id)
  let sorted = 0
  while (queue.length) {
    const id = queue.shift()
    sorted++
    outMap.get(id).forEach(seq => {
      const next = seq[targetKey]
      const rest = indegree.get(next) - 1
      indegree.set(next, rest)
      if (rest === 0) queue.push(next)
    })
  }
  if (sorted < nodeList.length) errors.push('流程存在环路，无法按顺序执行')

  /** 按出边方向做广度优先，收集从这些节点出发能到达的全部节点 */
  const walkForward = startIds => {
    const seen = new Set(startIds)
    const stack = [...startIds]
    while (stack.length) {
      const id = stack.pop()
      outMap.get(id).forEach(seq => {
        const next = seq[targetKey]
        if (!seen.has(next)) {
          seen.add(next)
          stack.push(next)
        }
      })
    }
    return seen
  }

  /** 按入边方向回溯，找出所有能走到结束节点的节点 */
  const walkBackward = endIds => {
    const seen = new Set(endIds)
    const stack = [...endIds]
    while (stack.length) {
      const id = stack.pop()
      inMap.get(id).forEach(seq => {
        const prev = seq[sourceKey]
        if (!seen.has(prev)) {
          seen.add(prev)
          stack.push(prev)
        }
      })
    }
    return seen
  }

  /* ---------- 连通性：从开始能到达、且能走到结束 ---------- */
  if (startNodes.length) {
    const reached = walkForward(startNodes.map(n => n.id))
    const orphans = nodeList.filter(n => !reached.has(n.id))
    if (orphans.length) {
      errors.push(`无法从开始节点到达：${joinNames(orphans.map(labelOf))}`)
    }
  }

  if (endNodes.length) {
    const canReachEnd = walkBackward(endNodes.map(n => n.id))
    const dead = nodeList.filter(n => !canReachEnd.has(n.id))
    if (dead.length) {
      errors.push(`走不到结束节点：${joinNames(dead.map(labelOf))}`)
    }
  }

  /* ---------- 分支：空分支属于编辑中的待配置项 ---------- */
  // 分支泳道由布局推导（空分支只有占位卡、没有连线），按 gatewayId 归组后统计
  const laneMap = new Map()
  ;(lanes || []).forEach(lane => {
    if (!lane || !lane.gatewayId) return
    const list = laneMap.get(lane.gatewayId) || []
    list.push(lane)
    laneMap.set(lane.gatewayId, list)
  })

  nodeList
    .filter(n => n.type === 'exclusiveGateway')
    .forEach(gateway => {
      const outs = outMap.get(gateway.id)
      const gwLanes = laneMap.get(gateway.id) || []
      const total = gwLanes.length || outs.length
      if (!total) {
        errors.push(`条件分支「${labelOf(gateway)}」没有可执行的分支`)
        return
      }
      const empty = gwLanes.filter(lane => lane.pending).length
      if (!empty) return
      warnings.push(
        empty === total
          ? `条件分支「${labelOf(gateway)}」的 ${total} 条分支都还没放节点`
          : `条件分支「${labelOf(gateway)}」有 ${empty} 条分支还没放节点`
      )
    })

  const issues = [...errors, ...warnings]
  if (!issues.length) return { ok: true, level: 'ok', text: '校验通过', issues, errors, warnings }
  return {
    ok: false,
    level: errors.length ? 'error' : 'warn',
    text: issues[0],
    issues,
    errors,
    warnings
  }
}
