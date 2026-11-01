import {isDesktop} from '@desktop/env.js'

function browserSave(blob, filename) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 保存二进制文件（Web / 桌面端统一入口）
 *
 * 浏览器端走 a[download]；桌面端交由 @desktop/download.js 直接写入「文档/星火云」
 * 或用户设置的目录，不再弹保存框。桌面端实现按需加载，Web 版不会下载这部分代码。
 * @param blob 文件二进制
 * @param filename 默认文件名
 */
export async function saveBlob(blob, filename) {
  if (!isDesktop) {
    browserSave(blob, filename)
    return
  }
  const {saveBlobToDisk} = await import('@desktop/download.js')
  await saveBlobToDisk(blob, filename)
}
