import { ref, nextTick } from 'vue'
import { getNoReadMsgListAPI, pageMsgListAPI } from '@/api/chat/msg.js'

// 每页加载的历史消息条数
const PAGE_SIZE = 20
// 距顶部小于该距离时触发加载更早的历史
const LOAD_TOP_OFFSET = 40
// 滚动到底部用的极大值，超出内容高度的部分由小程序收敛到最大值
const BOTTOM_SCROLL_TOP = 999999

/**
 * 聊天消息加载（移动端）
 * 首屏取未读消息（服务端同时标记已读），条数不足一页时补一页种子；
 * 滚动到顶部时按页加载更早的历史，加载后用 scroll-into-view 定位到加载前的最早一条，避免视口跳动
 * 服务端按 id 倒序返回，统一转成正序（旧 -> 新）使用，历史页插到列表头部
 * @param spaceId 会话空间 id，ref 或 computed
 * @param filter 历史查询的额外检索条件，ref 对象（如 { createdStartTime, createdEndTime }）
 * @returns 列表状态、滚动绑定值与操作方法
 */
export function useChatHistory(spaceId, filter) {
  // 消息列表，按时间正序
  const list = ref([])
  // 是否正在加载历史
  const loading = ref(false)
  // 是否还有更早的历史
  const hasMore = ref(true)
  // scroll-view 的 scroll-top 绑定值
  const scrollTop = ref(0)
  // scroll-view 的 scroll-into-view 绑定值，历史加载后定位用
  const scrollIntoView = ref('')
  // 滚到底部的交替标记，保证 scroll-top 每次都在变化
  let scrollToggle = false
  // 已加载到的历史页码
  let pageNo = 0

  /**
   * 取未读消息作为首屏，条数不足一页时补一页种子
   * @returns {Promise<void>}
   */
  function loadUnread() {
    if (!spaceId.value) {
      return Promise.resolve()
    }
    return getNoReadMsgListAPI({ spaceId: spaceId.value }).then(res => {
      if (res.code !== 200 || res.data === null) {
        return
      }
      const rows = toAsc(res.data)
      // 标记未读起点，模板据此展示「以下是最新消息」分隔线
      if (rows.length) {
        rows[0].unreadStart = true
      }
      list.value = rows
      // 条数不足一页时大概率撑不满一屏，补一页种子保证有内容可滚动，只补一页
      if (rows.length < PAGE_SIZE) {
        return loadMore()
      }
    })
  }

  /**
   * 加载更早的一页历史，插到列表头部
   * @returns {Promise<void>}
   */
  function loadMore() {
    if (loading.value || !hasMore.value || !spaceId.value) {
      return Promise.resolve()
    }
    loading.value = true
    const nextPage = pageNo + 1
    const query = {
      ...(filter ? filter.value : null),
      spaceId: spaceId.value,
      pageNo: nextPage,
      pageSize: PAGE_SIZE
    }
    return pageMsgListAPI(query).then(res => {
      if (res.code !== 200) {
        return
      }
      const rows = toAsc((res.data && res.data.rows) || [])
      pageNo = nextPage
      hasMore.value = rows.length >= PAGE_SIZE
      list.value = mergeOlder(rows, list.value)
    }).finally(() => {
      loading.value = false
    })
  }

  /**
   * 滚动到顶部附近时加载更早的历史
   * @param e scroll-view 滚动事件
   */
  function onScroll(e) {
    const detail = (e && e.detail) || {}
    if ((detail.scrollTop || 0) > LOAD_TOP_OFFSET || loading.value || !hasMore.value) {
      return
    }
    // 记录加载前的最早一条消息 id，加载后把它定位回顶部，视口视觉位置保持不变
    const anchorId = list.value.length ? list.value[0].id : null
    loadMore().then(() => {
      if (anchorId === null || anchorId === undefined) {
        return
      }
      scrollIntoView.value = 'msg-' + anchorId
    })
  }

  /**
   * 滚动到底部
   * 小程序 scroll-top 相同值不会再次滚动，用两个极大值交替触发
   */
  function scrollToBottom() {
    scrollToggle = !scrollToggle
    nextTick(() => {
      scrollTop.value = BOTTOM_SCROLL_TOP + (scrollToggle ? 1 : 0)
    })
  }

  /**
   * 重置为未加载状态
   */
  function reset() {
    list.value = []
    loading.value = false
    hasMore.value = true
    scrollIntoView.value = ''
    scrollToggle = false
    pageNo = 0
  }

  return { list, loading, hasMore, scrollTop, scrollIntoView, loadUnread, loadMore, onScroll, scrollToBottom, reset }
}

/**
 * 服务端按 id 倒序返回，转为正序
 * @param rows 消息列表
 * @returns 正序列表
 */
function toAsc(rows) {
  return (rows || []).slice().sort((a, b) => a.id - b.id)
}

/**
 * 把更早的历史页并到列表头部，按 id 去重（历史页可能与本轮已加载的消息重叠）
 * @param rows 历史页消息
 * @param current 已加载列表
 * @returns 合并后的列表
 */
function mergeOlder(rows, current) {
  const loadedIds = new Set()
  current.forEach(item => {
    if (item.id !== undefined) {
      loadedIds.add(item.id)
    }
  })
  const older = rows.filter(item => !loadedIds.has(item.id))
  return older.concat(current)
}
