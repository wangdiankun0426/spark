import { ref, nextTick } from 'vue'
import { getNoReadMsgListAPI, pageMsgListAPI } from '@/api/chat/msg.js'

// 每页加载的历史消息条数
const PAGE_SIZE = 20
// 距顶部小于该距离时触发加载更早的历史
const LOAD_TOP_OFFSET = 40

/**
 * 聊天消息加载
 * 首屏取未读消息（服务端同时标记已读），向上滚动按页加载更早的历史
 * 服务端按 id 倒序返回，统一转成正序（旧 -> 新）使用，历史页插到列表头部
 * @param spaceId 会话空间 id，ref 或 computed
 * @param scrollbarRef el-scrollbar 实例 ref
 * @param filter 历史查询的额外检索条件，ref 对象（如 { createdStartTime, createdEndTime }）
 * @returns 列表状态与操作方法
 */
export function useChatHistory(spaceId, scrollbarRef, filter) {
  // 消息列表，按时间正序
  const list = ref([])
  // 是否正在加载历史
  const loading = ref(false)
  // 是否还有更早的历史
  const hasMore = ref(true)
  // 已加载到的历史页码
  let pageNo = 0

  /**
   * 取未读消息作为首屏
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
   * 滚动到顶部附近时加载更早的历史，加载后还原视口位置，避免内容顶跑
   * 内容高度变化会让浏览器把 scrollTop 归零并派发滚动事件，这类事件不加载，否则未上拉就会把历史拉完
   * @param payload el-scrollbar 滚动事件参数 { scrollLeft, scrollTop }
   */
  function onScroll(payload) {
    if (!payload || payload.scrollTop > LOAD_TOP_OFFSET || loading.value || !hasMore.value || !isScrollable()) {
      return
    }
    const wrap = wrapEl()
    // 记录距底部的距离，加载后按新的内容高度还原
    const bottomOffset = wrap.scrollHeight - wrap.scrollTop
    loadMore().then(() => nextTick(() => {
      wrap.scrollTop = wrap.scrollHeight - bottomOffset
    }))
  }

  /**
   * 消息区内容是否已能滚动
   * @returns 是否可滚动
   */
  function isScrollable() {
    const wrap = wrapEl()
    return !!wrap && wrap.scrollHeight > wrap.clientHeight
  }

  /**
   * 滚动到底部，直接跳转不做平滑动画，避免动画途中经过顶部触发加载历史
   */
  function scrollToBottom() {
    nextTick(() => {
      const scrollbar = scrollbarRef.value
      if (!scrollbar || !scrollbar.wrapRef) {
        return
      }
      scrollbar.update()
      scrollbar.scrollTo({ top: scrollbar.wrapRef.scrollHeight })
    })
  }

  /**
   * 重置为未加载状态
   */
  function reset() {
    list.value = []
    loading.value = false
    hasMore.value = true
    pageNo = 0
  }

  /**
   * 获取滚动容器
   * @returns 滚动容器元素
   */
  function wrapEl() {
    const scrollbar = scrollbarRef.value
    return scrollbar ? scrollbar.wrapRef : null
  }

  return { list, loading, hasMore, loadUnread, loadMore, onScroll, isScrollable, scrollToBottom, reset }
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
