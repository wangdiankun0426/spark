import {ElLoading, ElMessage, ElMessageBox} from 'element-plus'
import {localStorageKey} from '@/utils/keyUtils.js'
import {isDesktop} from './env.js'

/** 热更新清单地址，由 .env 的 VITE_HOT_UPDATE_URL 指定，留空则不启用 */
const MANIFEST_URL = import.meta.env.VITE_HOT_UPDATE_URL
/** Rust 侧下载进度事件名，与 src-tauri/src/hot_update.rs 的 PROGRESS_EVENT 保持一致 */
const PROGRESS_EVENT = 'hot-update-progress'

// 开发模式由 Vite 承载页面，没有独立的资源包可替换
const enabled = isDesktop && !import.meta.env.DEV && !!MANIFEST_URL

/**
 * 调用桌面端命令
 * @param command 命令名
 * @param payload 命令参数
 * @returns {Promise<any>}
 */
function invoke(command, payload) {
  return window.__TAURI__.core.invoke(command, payload)
}

/**
 * 检查版本并引导更新
 * @param silent 启动时静默检查，无更新或已忽略的版本不打扰用户
 * @returns {Promise<void>}
 */
export async function checkHotUpdate(silent = false) {
  if (!enabled) {
    if (!silent) {
      ElMessage.info('当前环境不支持热更新')
    }
    return
  }
  let result
  try {
    result = await invoke('hot_update_check', {manifestUrl: MANIFEST_URL})
  } catch (e) {
    if (!silent) {
      ElMessage.warning(e || '检查更新失败')
    }
    return
  }
  if (!result.hasUpdate) {
    if (!silent) {
      ElMessage.success(`已是最新版本 v${__APP_VERSION__}`)
    }
    return
  }
  // 用户主动触发时始终提示，静默检查则跳过用户选过「稍后」的版本
  if (silent && localStorage.getItem(localStorageKey.HOT_UPDATE_IGNORE) === result.version) {
    return
  }
  const size = result.size ? `（约 ${(result.size / 1024 / 1024).toFixed(1)} MB）` : ''
  const detail = result.note ? `，更新内容：${result.note}` : ''
  await ElMessageBox.confirm(
      `发现新版本 v${result.version}${size}${detail}，是否立即更新？`,
      '更新提示',
      {
        confirmButtonText: '立即更新',
        cancelButtonText: '稍后',
        type: 'info',
        distinguishCancelAndClose: true,
      }
  ).then(() => downloadUpdate(result)).catch(action => {
    if (action === 'cancel') {
      localStorage.setItem(localStorageKey.HOT_UPDATE_IGNORE, result.version)
    }
  })
}

/**
 * 下载并安装更新包，完成后询问是否重启
 * @param update 检查结果
 * @returns {Promise<void>}
 */
async function downloadUpdate(update) {
  const loading = ElLoading.service({
    lock: true,
    text: `正在下载更新包 v${update.version}`,
    background: 'rgba(0, 0, 0, 0.35)',
  })
  const unlisten = await window.__TAURI__.event.listen(PROGRESS_EVENT, event => {
    loading.setText(`正在下载更新包 ${event.payload.percent}%`)
  })
  let installed = false
  try {
    await invoke('hot_update_install', {
      zipUrl: update.zipUrl,
      version: update.version,
      size: update.size,
      expectedSha256: update.sha256,
    })
    installed = true
  } catch (e) {
    ElMessage.error(e || '更新失败')
  } finally {
    unlisten()
    loading.close()
  }
  if (!installed) {
    return
  }
  localStorage.removeItem(localStorageKey.HOT_UPDATE_IGNORE)
  ElMessageBox.confirm(
      '新版本已就绪，重启窗口后生效，是否立即重启？',
      '更新完成',
      {
        confirmButtonText: '立即重启',
        cancelButtonText: '稍后',
        type: 'success',
      }
  ).then(() => invoke('hot_update_reload')).catch(() => {})
}
