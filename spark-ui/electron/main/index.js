const { app, BrowserWindow, Tray, Menu, ipcMain, shell, dialog, nativeImage } = require('electron')
const { join } = require('path')

// 判断是否为开发模式
const isDev = !app.isPackaged

/** @type {BrowserWindow | null} */
let mainWindow = null

/** @type {Tray | null} */
let tray = null

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 800,
    title: '星火云AI应用平台',
    icon: join(__dirname, '../../public/favicon.ico'),
    webPreferences: {
      preload: join(__dirname, '../preload/index.mjs'),
      // 启用 sandbox 以提高安全性，同时通过 preload 暴露必要 API
      sandbox: false,
      // 允许渲染进程使用 Node.js 特性（通过 preload 控制）
      nodeIntegration: false,
      contextIsolation: true,
    },
    // 开发模式显示菜单栏，生产模式隐藏
    autoHideMenuBar: !isDev,
    show: false, // 等 ready-to-show 再显示，避免白屏闪烁
  })

  // 窗口准备好后再显示，避免白屏闪烁
  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
  })

  // 加载页面
  if (isDev && process.env.ELECTRON_RENDERER_URL) {
    // 开发模式：加载 Vite 开发服务器
    mainWindow.loadURL(process.env.ELECTRON_RENDERER_URL)
    // 自动打开开发者工具
    mainWindow.webContents.openDevTools()
  } else {
    // 生产模式：加载构建后的文件
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }

  // 监听窗口关闭事件
  mainWindow.on('closed', () => {
    mainWindow = null
  })

  // 创建新窗口的统一配置
  const newWindowOptions = {
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 800,
    webPreferences: {
      preload: join(__dirname, '../preload/index.mjs'),
      sandbox: false,
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false,
    },
    autoHideMenuBar: true,
  }

  // 拦截 window.open 请求
  // 内部路由 → 在 Electron 新窗口中打开；外部链接 → 系统浏览器
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    const appUrl = mainWindow.webContents.getURL()
    if (url.startsWith('/') || (appUrl && url.startsWith(new URL(appUrl).origin))) {
      return { action: 'allow', overrideBrowserWindowOptions: newWindowOptions }
    }
    shell.openExternal(url)
    return { action: 'deny' }
  })
}

function createTray() {
  // 使用 nativeImage 创建托盘图标（16x16 像素）
  const icon = nativeImage.createFromPath(join(__dirname, '../../public/favicon.ico'))
  tray = new Tray(icon.resize({ width: 16, height: 16 }))

  const contextMenu = Menu.buildFromTemplate([
    {
      label: '显示窗口',
      click: () => {
        if (mainWindow) {
          mainWindow.show()
          mainWindow.focus()
        }
      },
    },
    { type: 'separator' },
    {
      label: '退出',
      click: () => app.quit(),
    },
  ])

  tray.setToolTip('星火云AI应用平台')
  tray.setContextMenu(contextMenu)

  // 双击托盘图标显示窗口
  tray.on('double-click', () => {
    if (mainWindow) {
      mainWindow.show()
      mainWindow.focus()
    }
  })
}

// ====== IPC 通信处理 ======

// 打开文件选择对话框
ipcMain.handle('dialog:openFile', async (event, options) => {
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile'],
    ...options,
  })
  return result
})

// 读取本地文件
ipcMain.handle('fs:readFile', async (event, filePath) => {
  const fs = require('fs')
  try {
    const data = fs.readFileSync(filePath)
    return { success: true, data }
  } catch (error) {
    return { success: false, error: error.message }
  }
})

// 获取应用信息
ipcMain.handle('app:getInfo', () => {
  return {
    version: app.getVersion(),
    name: app.getName(),
    electron: process.versions.electron,
    chrome: process.versions.chrome,
    node: process.versions.node,
    platform: process.platform,
    arch: process.arch,
  }
})

// ====== 应用生命周期 ======

app.whenReady().then(() => {
  // 开发模式保留菜单栏（开发者工具），生产模式隐藏
  if (isDev) {
    const devTemplate = [
      {
        label: '开发',
        submenu: [
          { label: '开发者工具', accelerator: 'F12', role: 'toggleDevTools' },
          { type: 'separator' },
          { label: '重新加载', accelerator: 'CmdOrCtrl+R', role: 'reload' },
          { label: '强制重新加载', accelerator: 'CmdOrCtrl+Shift+R', role: 'forceReload' },
        ],
      },
    ]
    Menu.setApplicationMenu(Menu.buildFromTemplate(devTemplate))
  } else {
    Menu.setApplicationMenu(null)
  }
  createWindow()
  createTray()

  // macOS 下点击 dock 图标重新创建窗口
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

// 所有窗口关闭时退出应用（macOS 除外）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// 退出前清理托盘
app.on('before-quit', () => {
  if (tray) {
    tray.destroy()
    tray = null
  }
})