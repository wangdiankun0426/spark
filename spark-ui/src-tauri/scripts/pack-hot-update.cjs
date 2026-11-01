/**
 * 桌面端热更新打包：把 dist 压成 zip 并生成版本清单 latest.json
 * 用法：npm run build:web && npm run pack:update -- "本次更新说明"
 * 发布：把 release/desktop 下的 zip 与 latest.json 传到服务器 /opt/spark/bundle/desktop/
 */
const fs = require('fs')
const path = require('path')
const crypto = require('crypto')
const {execFileSync} = require('child_process')

const root = path.join(__dirname, '..', '..')
const distDir = path.join(root, 'dist')
const outDir = path.join(root, 'release', 'desktop')
const version = require(path.join(root, 'package.json')).version
const note = process.argv[2] || ''

if (!fs.existsSync(path.join(distDir, 'index.html'))) {
  console.error('[pack-update] dist 不存在，请先执行 npm run build:web')
  process.exit(1)
}

fs.mkdirSync(outDir, {recursive: true})
const zipName = `dist-${version}.zip`
const zipPath = path.join(outDir, zipName)

// 复用 Windows 自带 Compress-Archive，无需为打包再引入压缩库
execFileSync('powershell', [
  '-NoProfile',
  '-Command',
  `Compress-Archive -Path '${path.join(distDir, '*')}' -DestinationPath '${zipPath}' -Force`,
], {stdio: 'inherit'})

// 客户端下载后按该值校验完整性，不符则拒绝安装
const sha256 = crypto.createHash('sha256').update(fs.readFileSync(zipPath)).digest('hex')

const manifest = {
  version,
  note,
  zipUrl: zipName,
  size: fs.statSync(zipPath).size,
  sha256,
  publishedAt: new Date().toISOString(),
}
fs.writeFileSync(path.join(outDir, 'latest.json'), JSON.stringify(manifest, null, 2), 'utf8')

console.log(`[pack-update] 版本 ${version} 打包完成，产物目录：${outDir}`)
console.log(`[pack-update] 上传文件：${zipName}、latest.json`)
