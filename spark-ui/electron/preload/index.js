const { contextBridge, ipcRenderer } = require('electron')

/**
 * 预加载脚本 — 在渲染进程创建前执行
 * 通过 contextBridge 安全地暴露主进程功能到渲染进程
 *
 * 渲染进程通过 window.electronAPI.xxx 访问
 */
contextBridge.exposeInMainWorld('electronAPI', {
  // ====== 文件对话框 ======
  /** 打开文件选择对话框 */
  openFileDialog: (options) => ipcRenderer.invoke('dialog:openFile', options),

  // ====== 文件系统 ======
  /** 读取本地文件内容 */
  readFile: (filePath) => ipcRenderer.invoke('fs:readFile', filePath),

  // ====== 应用信息 ======
  /** 获取应用信息 */
  getAppInfo: () => ipcRenderer.invoke('app:getInfo'),

  // ====== 窗口控制 ======
  /** 最小化窗口 */
  minimizeWindow: () => ipcRenderer.send('window:minimize'),
  /** 最大化/还原窗口 */
  maximizeWindow: () => ipcRenderer.send('window:maximize'),
  /** 关闭窗口 */
  closeWindow: () => ipcRenderer.send('window:close'),

  // ====== 监听事件 ======
  /** 监听主进程消息 */
  onMessage: (channel, callback) => {
    const validChannels = ['update:available', 'update:downloaded', 'app:notification']
    if (validChannels.includes(channel)) {
      ipcRenderer.on(channel, (_event, ...args) => callback(...args))
    }
  },

  // ====== 清理监听 ======
  /** 移除监听器 */
  removeListener: (channel, callback) => {
    ipcRenderer.removeListener(channel, callback)
  },
})