/**
 * 以 package.json 为唯一版本源，同步到 src-tauri/Cargo.toml
 * tauri.conf.json 不配置 version，Tauri 打包时自动继承 Cargo.toml
 * 在 tauri build 前自动执行，保证 Web 与桌面端版本一致
 */
const fs = require('fs')
const path = require('path')

const root = path.join(__dirname, '..')
const version = require('../package.json').version

const cargoTomlPath = path.join(root, 'src-tauri', 'Cargo.toml')
let cargoToml = fs.readFileSync(cargoTomlPath, 'utf8')
const nextCargoToml = cargoToml.replace(/(^version\s*=\s*")[^"]+(")/m, `$1${version}$2`)
if (nextCargoToml !== cargoToml) {
  fs.writeFileSync(cargoTomlPath, nextCargoToml)
  console.log(`[sync-version] Cargo.toml -> ${version}`)
}
