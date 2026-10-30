/**
 * 将 Tauri NSIS 安装包重命名为 sparkcloud_<version>_x64-setup.exe
 * 应用安装后的显示名仍由 tauri.conf.json 的 productName（星火云）决定
 */
const fs = require('fs')
const path = require('path')

const pkg = require('../package.json')
const nsisDir = path.join(__dirname, '..', 'src-tauri', 'target', 'release', 'bundle', 'nsis')
const targetName = `sparkcloud_${pkg.version}_x64-setup.exe`

if (!fs.existsSync(nsisDir)) {
  console.error('[rename-installer] NSIS bundle 目录不存在：', nsisDir)
  process.exit(1)
}

const installer = fs.readdirSync(nsisDir).find(file => /x64-setup\.exe$/i.test(file))
if (!installer) {
  console.error('[rename-installer] 未找到安装包文件')
  process.exit(1)
}

const from = path.join(nsisDir, installer)
const to = path.join(nsisDir, targetName)
if (fs.existsSync(to)) {
  fs.unlinkSync(to)
}
fs.renameSync(from, to)
console.log(`[rename-installer] ${installer} -> ${targetName}`)
