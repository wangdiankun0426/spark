import { ref } from 'vue'
import { pageMyChatSpaceListAPI } from '@/api/chat/space.js'

// 每页加载的会话数量
const PAGE_SIZE = 20
// 距底部小于该距离时加载下一页
const LOAD_BOTTOM_OFFSET = 40

/**
 * AI会话列表分页加载，支持按标题检索
 * 检索走后端，滚动到底部加载下一页
 * @param spaceType 会话类型：agent=智能体，model=模型
 * @param scrollbarRef el-scrollbar 实例 ref
 * @returns 列表状态与操作方法
 */
export function useAiSessionList(spaceType, scrollbarRef) {
  // 会话列表
  const sessions = ref([])
  // 是否正在加载
  const loading = ref(false)
  // 是否还有下一页
  const hasMore = ref(true)
  // 标题检索关键字
  const keyword = ref('')
  // 已加载到的页码
  let pageNo = 0
  // 请求序号，用于丢弃过期响应（检索期间连续输入时）
  let requestSeq = 0

  /**
   * 加载第一页并替换列表，检索条件变化时调用
   * @returns {Promise<void>}
   */
  function loadFirst() {
    return request(1, PAGE_SIZE, true)
  }

  /**
   * 加载下一页并追加
   * @returns {Promise<void>}
   */
  function loadMore() {
    if (loading.value || !hasMore.value) {
      return Promise.resolve()
    }
    return request(pageNo + 1, PAGE_SIZE, false)
  }

  /**
   * 重新加载已加载的全部会话（新增消息后刷新标题与时间）
   * @returns {Promise<void>}
   */
  function refresh() {
    return request(1, Math.max(PAGE_SIZE, sessions.value.length), true)
  }

  /**
   * 滚动到底部附近时加载下一页
   */
  function handleScroll() {
    const scrollbar = scrollbarRef.value
    const wrap = scrollbar ? scrollbar.wrapRef : null
    if (!wrap || loading.value || !hasMore.value) {
      return
    }
    if (wrap.scrollTop + wrap.clientHeight < wrap.scrollHeight - LOAD_BOTTOM_OFFSET) {
      return
    }
    loadMore()
  }

  /**
   * 检索条件变化后从头加载
   */
  function handleSearch() {
    loadFirst()
  }

  /**
   * 请求会话列表
   * @param page 页码
   * @param size 每页数量
   * @param replace 是否替换列表
   * @returns {Promise<void>}
   */
  function request(page, size, replace) {
    loading.value = true
    const seq = ++requestSeq
    const query = {
      spaceType: spaceType,
      pageNo: page,
      pageSize: size,
      title: keyword.value.trim() || null
    }
    return pageMyChatSpaceListAPI(query).then(res => {
      if (seq !== requestSeq || res.code !== 200) {
        return
      }
      const rows = (res.data && res.data.rows) || []
      pageNo = page
      hasMore.value = page * size < (res.data ? res.data.total : 0)
      sessions.value = replace ? rows : sessions.value.concat(rows)
    }).finally(() => {
      // 过期请求不解除加载态，避免打断有新请求时的加载提示
      if (seq === requestSeq) {
        loading.value = false
      }
    })
  }

  return { sessions, loading, hasMore, keyword, loadFirst, loadMore, refresh, handleScroll, handleSearch }
}
