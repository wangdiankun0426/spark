export const RANGE_OPTIONS = [
  { label: '近1小时', value: '1h', hours: 1 },
  { label: '近6小时', value: '6h', hours: 6 },
  { label: '近24小时', value: '24h', hours: 24 },
  { label: '近7天', value: '7d', hours: 168 }
]

function pad(num) {
  return String(num).padStart(2, '0')
}

function formatDate(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/**
 * 按区间值计算查询起止时间
 * @param range 区间值
 * @returns {{startTime: string, endTime: string}}
 */
export function buildTimeRange(range) {
  const option = RANGE_OPTIONS.find(item => item.value === range) || RANGE_OPTIONS[0]
  const end = new Date()
  const start = new Date(end.getTime() - option.hours * 3600 * 1000)
  return { startTime: formatDate(start), endTime: formatDate(end) }
}

/**
 * 字节数转可读文本
 * @param bytes 字节
 * @returns {string}
 */
export function formatBytes(bytes) {
  if (!bytes || bytes <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / Math.pow(1024, index)
  return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`
}

/**
 * 毫秒转可读耗时
 * @param ms 毫秒
 * @returns {string}
 */
export function formatMs(ms) {
  if (ms === null || ms === undefined) return '--'
  if (ms < 1000) return `${ms} ms`
  return `${(ms / 1000).toFixed(2)} s`
}

/**
 * 安全解析JSON字符串
 * @param text JSON文本
 * @returns {object|array|null}
 */
export function parseJson(text) {
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch (e) {
    return null
  }
}
