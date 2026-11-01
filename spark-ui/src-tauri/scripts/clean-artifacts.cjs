/**
 * 清理历史构建产物：安装包与热更包都只保留最新一次，避免本地体积不断堆积
 * 由 rename-installer.cjs 与 pack-hot-update.cjs 在产出成功后调用
 */
const fs = require('fs')
const path = require('path')

const root = path.join(__dirname, '..', '..')
/** tauri build 的产物根目录，nsis / msi 等各打包目标的安装包都在这里 */
const bundleDir = path.join(__dirname, '..', 'target', 'release', 'bundle')
/** 热更产物目录，与发布时上传到服务器的内容一致 */
const hotUpdateDir = path.join(root, 'release', 'desktop')

/**
 * 递归列出目录下所有文件
 */
function listFiles(dir) {
  if (!fs.existsSync(dir)) return []
  return fs.readdirSync(dir, {withFileTypes: true}).flatMap(entry => {
    const full = path.join(dir, entry.name)
    return entry.isDirectory() ? listFiles(full) : [full]
  })
}

/**
 * 删除目录下除 keep 之外的所有文件，并清掉因之变空的子目录
 * @returns {{removed: number, freed: number}} 删除的文件数与释放的字节数
 */
function prune(dir, keep) {
  const keepSet = new Set(keep.map(file => path.resolve(file)))
  const result = {removed: 0, freed: 0}
  for (const file of listFiles(dir)) {
    if (keepSet.has(path.resolve(file))) continue
    result.freed += fs.statSync(file).size
    fs.rmSync(file, {force: true})
    result.removed += 1
    console.log(`[clean] 删除历史产物：${path.relative(root, file)}`)
  }
  removeEmptyDirs(dir)
  return result
}

/**
 * 自底向上删除空目录，打包目标目录（nsis 等）本身保留
 */
function removeEmptyDirs(dir) {
  if (!fs.existsSync(dir)) return
  for (const entry of fs.readdirSync(dir, {withFileTypes: true})) {
    if (!entry.isDirectory()) continue
    const full = path.join(dir, entry.name)
    removeEmptyDirs(full)
    if (fs.readdirSync(full).length === 0) fs.rmSync(full, {recursive: true, force: true})
  }
}

/**
 * 汇报清理结果，未产生历史产物时不打印噪音
 */
function report(label, result) {
  if (result.removed === 0) return
  const freed =
    result.freed >= 1024 * 1024
      ? `${(result.freed / 1024 / 1024).toFixed(1)} MB`
      : `${Math.max(1, Math.round(result.freed / 1024))} KB`
  console.log(`[clean] ${label}：删除 ${result.removed} 个历史文件，释放 ${freed}`)
}

/**
 * 清理安装包产物，只保留本次生成的安装包
 * @param {string} keepFile 需要保留的安装包绝对路径
 */
function cleanInstaller(keepFile) {
  if (!fs.existsSync(keepFile)) {
    // 保留文件缺失时宁可不动，避免把整个产物目录清空
    console.warn(`[clean] 本次安装包不存在，跳过清理：${keepFile}`)
    return
  }
  report('安装包', prune(bundleDir, [keepFile]))
}

/**
 * 清理热更产物，只保留本次生成的 zip 与清单
 * @param {string[]} keepFiles 需要保留的文件绝对路径
 */
function cleanUpdate(keepFiles) {
  report('热更包', prune(hotUpdateDir, keepFiles))
}

module.exports = {cleanInstaller, cleanUpdate}
