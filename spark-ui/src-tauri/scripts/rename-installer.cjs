/**
 * 将 Tauri NSIS 安装包重命名为 sparkcloud_<version>_x64-setup.exe，
 * 并清理 target/release/bundle 下历史版本的安装包，只保留本次产物
 * 应用安装后的显示名仍由 tauri.conf.json 的 productName 决定
 */
const fs = require('fs')
const path = require('path')
const {cleanInstaller} = require('./clean-artifacts.cjs')

const pkg = require('../../package.json')
const nsisDir = path.join(__dirname, '..', 'target', 'release', 'bundle', 'nsis')
const targetName = `sparkcloud_${pkg.version}_x64-setup.exe`
const targetPath = path.join(nsisDir, targetName)

if (!fs.existsSync(nsisDir)) {
  console.error('[rename-installer] NSIS bundle 目录不存在：', nsisDir)
  process.exit(1)
}

// Tauri 产出的原始文件名是 <productName>_<version>_x64-setup.exe，
// 目录里可能残留上一次已改名的 sparkcloud_*.exe 与更早版本的原始包，统一按修改时间取最新的
const built = fs
  .readdirSync(nsisDir)
  .filter(file => /x64-setup\.exe$/i.test(file) && !/^sparkcloud_/i.test(file))
  .map(file => ({file, mtime: fs.statSync(path.join(nsisDir, file)).mtimeMs}))
  .sort((a, b) => b.mtime - a.mtime)[0]

const existing = fs.existsSync(targetPath) ? fs.statSync(targetPath) : null

if (built && (!existing || built.mtime >= existing.mtimeMs)) {
  fs.rmSync(targetPath, {force: true})
  fs.renameSync(path.join(nsisDir, built.file), targetPath)
  console.log(`[rename-installer] ${built.file} -> ${targetName}`)
} else if (existing) {
  console.log(`[rename-installer] 已是目标文件名：${targetName}`)
} else {
  console.error('[rename-installer] 未找到本次构建的安装包文件')
  process.exit(1)
}

cleanInstaller(targetPath)
