import {ElMessage} from 'element-plus'
import {localStorageKey} from '@/utils/keyUtils.js'

/** 未设置时默认落在「文档/星火云」 */
const DEFAULT_DIR_NAME = '星火云'

/**
 * 获取桌面端下载目录（用户设置优先，否则为「文档/星火云」）
 * @returns {Promise<string>}
 */
export async function getDownloadDir() {
  const saved = localStorage.getItem(localStorageKey.DOWNLOAD_DIR)
  if (saved) return saved
  const docDir = await window.__TAURI__.path.documentDir()
  const defaultDir = await window.__TAURI__.path.join(docDir, DEFAULT_DIR_NAME)
  localStorage.setItem(localStorageKey.DOWNLOAD_DIR, defaultDir)
  return defaultDir
}

/**
 * 弹出系统目录选择框，返回所选目录或 null
 * @returns {Promise<string|null>}
 */
export async function pickDownloadDir() {
  const selected = await window.__TAURI__.dialog.open({ directory: true })
  return selected || null
}

/**
 * 保存用户选择的下载目录
 * @param dir 目录绝对路径
 */
export function saveDownloadDir(dir) {
  localStorage.setItem(localStorageKey.DOWNLOAD_DIR, dir)
}

/**
 * 生成不冲突的文件名（a.pdf -> a(1).pdf）
 */
async function resolveUniquePath(fs, pathApi, dir, filename) {
  const dot = filename.lastIndexOf('.')
  const base = dot > 0 ? filename.substring(0, dot) : filename
  const ext = dot > 0 ? filename.substring(dot) : ''
  let target = filename
  let index = 1
  while (await fs.exists(await pathApi.join(dir, target))) {
    target = `${base}(${index})${ext}`
    index++
  }
  return pathApi.join(dir, target)
}

/**
 * 桌面端保存二进制文件：直接写入「文档/星火云」或用户设置目录，不弹保存框
 * 浏览器端由 @/utils/download.js 走 a[download]，不会进入这里
 * @param blob 文件二进制
 * @param filename 默认文件名
 */
export async function saveBlobToDisk(blob, filename) {
  const fs = window.__TAURI__.fs
  const pathApi = window.__TAURI__.path
  const dir = await getDownloadDir()
  await fs.mkdir(dir, { recursive: true })
  const filePath = await resolveUniquePath(fs, pathApi, dir, filename)
  const data = new Uint8Array(await blob.arrayBuffer())
  await fs.writeFile(filePath, data)
  ElMessage.success('已保存到：' + filePath)
}
