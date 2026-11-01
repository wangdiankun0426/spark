/**
 * 将 Tauri NSIS 安装包重命名为 sparkcloud_<version>_x64-setup.exe
 * 应用安装后的显示名仍由 tauri.conf.json 的 productName 决定
 */
const fs = require('fs')
const path = require('path')

const pkg = require('../../package.json')
const nsisDir = path.join(__dirname, '..', 'target', 'release', 'bundle', 'nsis')
const targetName = `sparkcloud_${pkg.version}_x64-setup.exe`

if (!fs.existsSync(nsisDir)) {
  console.error('[rename-installer] NSIS bundle 目录不存在：', nsisDir)
  process.exit(1)
}

// Tauri 每次产出的原始文件名是 <productName>_<version>_x64-setup.exe，
// 目录里可能残留上一次已改名的 sparkcloud_*.exe，必须排除，否则会把旧包当成这次的产物
const candidates = fs
  .readdirSync(nsisDir)
  .filter(file => /x64-setup\.exe$/i.test(file) && !/^sparkcloud_/i.test(file))
  .map(file => ({file, mtime: fs.statSync(path.join(nsisDir, file)).mtimeMs}))
  .sort((a, b) => b.mtime - a.mtime)

if (candidates.length === 0) {
  // 产物已是目标文件名时重复执行不应报错
  if (fs.existsSync(path.join(nsisDir, targetName))) {
    console.log(`[rename-installer] 已是目标文件名：${targetName}`)
    process.exit(0)
  }
  console.error('[rename-installer] 未找到本次构建的安装包文件')
  process.exit(1)
}

const installer = candidates[0].file
const from = path.join(nsisDir, installer)
const to = path.join(nsisDir, targetName)
if (from === to) {
  console.log(`[rename-installer] 已是目标文件名：${targetName}`)
  process.exit(0)
}
if (fs.existsSync(to)) {
  fs.unlinkSync(to)
}
fs.renameSync(from, to)
console.log(`[rename-installer] ${installer} -> ${targetName}`)
