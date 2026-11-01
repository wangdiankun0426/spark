// 前端资源热更新：比对远端清单、下载并解压 dist 包，同时以 app 协议优先提供热更目录资源
use std::fs;
use std::io::Write;
use std::path::{Path, PathBuf};
use std::time::Duration;

use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use tauri::http::{Response, StatusCode};
use tauri::{AppHandle, Emitter, Manager};

/// 热更根目录，放在本地数据目录而非漫游目录，避免整套前端资源随漫游配置同步
const HOT_DIR: &str = "hotupdate";
/// 记录当前生效热更版本的文件
const CURRENT_FILE: &str = "current.txt";
/// 资源根目录，内部按版本号分子目录
const WEB_DIR: &str = "web";
/// 下载临时文件名
const DOWNLOAD_FILE: &str = "download.tmp";
/// 解压临时目录名
const STAGING_DIR: &str = "staging";
/// 下载进度事件，前端据此刷新百分比
const PROGRESS_EVENT: &str = "hot-update-progress";
/// 进度上报间隔，避免大包下载时事件高频刷屏
const PROGRESS_STEP: u64 = 128 * 1024;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct UpdateManifest {
    version: String,
    #[serde(default)]
    note: String,
    zip_url: String,
    #[serde(default)]
    size: u64,
    #[serde(default)]
    sha256: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct HotUpdateCheck {
    has_update: bool,
    version: String,
    note: String,
    size: u64,
    zip_url: String,
    sha256: String,
}

#[derive(Clone, Serialize)]
#[serde(rename_all = "camelCase")]
struct HotUpdateProgress {
    downloaded: u64,
    total: u64,
    percent: u32,
}

/// 热更根目录，目录不存在时按需创建
fn hot_root(app: &AppHandle) -> Result<PathBuf, String> {
    app.path()
        .app_local_data_dir()
        .map(|dir| dir.join(HOT_DIR))
        .map_err(|e| format!("获取本地数据目录失败: {}", e))
}

/// 读取已安装的热更版本，标记缺失或资源目录不完整时视为未安装
fn installed_patch(app: &AppHandle) -> Option<String> {
    let root = hot_root(app).ok()?;
    let version = fs::read_to_string(root.join(CURRENT_FILE)).ok()?;
    let version = version.trim().to_string();
    if version.is_empty() || !root.join(WEB_DIR).join(&version).join("index.html").is_file() {
        return None;
    }
    Some(version)
}

/// 当前生效的热更版本，版本落后于安装包时视为未安装热更
fn patch_version(app: &AppHandle) -> Option<String> {
    let version = installed_patch(app)?;
    // 重装新版安装包后内置资源可能新于旧热更包，继续沿用会把界面降级
    if version_greater(&app.package_info().version.to_string(), &version) {
        return None;
    }
    Some(version)
}

/// 启动时清理：安装包版本已高于热更版本时删除旧热更资源与版本标记，回收磁盘空间
pub fn cleanup_outdated(app: &AppHandle) {
    let Some(version) = installed_patch(app) else {
        return;
    };
    if !version_greater(&app.package_info().version.to_string(), &version) {
        return;
    }
    if let Ok(root) = hot_root(app) {
        let _ = fs::remove_dir_all(root.join(WEB_DIR).join(&version));
        let _ = fs::remove_file(root.join(CURRENT_FILE));
    }
}

/// 当前实际运行的资源版本，优先取热更版本
fn current_version(app: &AppHandle) -> String {
    patch_version(app).unwrap_or_else(|| app.package_info().version.to_string())
}

/// 按点分段转为数字比较，缺失段按 0 处理，保证 0.0.10 大于 0.0.9
fn version_greater(left: &str, right: &str) -> bool {
    let left = version_segments(left);
    let right = version_segments(right);
    for index in 0..left.len().max(right.len()) {
        let a = *left.get(index).unwrap_or(&0);
        let b = *right.get(index).unwrap_or(&0);
        if a != b {
            return a > b;
        }
    }
    false
}

fn version_segments(version: &str) -> Vec<u64> {
    version
        .trim()
        .trim_start_matches('v')
        .split('.')
        .map(|segment| segment.parse::<u64>().unwrap_or(0))
        .collect()
}

/// 字节流转小写十六进制字符串，用于输出下载包的 sha256 摘要
fn hex_lower(bytes: &[u8]) -> String {
    bytes.iter().map(|b| format!("{:02x}", b)).collect()
}

/// 版本号会参与目录拼接，只允许字母、数字、点和短横线
fn check_version(version: &str) -> Result<(), String> {
    let valid = !version.is_empty()
        && version
            .chars()
            .all(|c| c.is_ascii_alphanumeric() || c == '.' || c == '-');
    if valid {
        Ok(())
    } else {
        Err("更新版本号不合法".to_string())
    }
}

fn http_client() -> Result<reqwest::Client, String> {
    reqwest::Client::builder()
        .timeout(Duration::from_secs(300))
        .build()
        .map_err(|e| format!("创建下载客户端失败: {}", e))
}

/// 清单里的包地址支持相对路径，按清单自身所在目录拼接
fn resolve_url(manifest_url: &str, zip_url: &str) -> String {
    if zip_url.starts_with("http://") || zip_url.starts_with("https://") {
        return zip_url.to_string();
    }
    match manifest_url.rfind('/') {
        Some(index) => format!("{}/{}", &manifest_url[..index], zip_url.trim_start_matches('/')),
        None => zip_url.to_string(),
    }
}

async fn fetch_manifest(url: &str) -> Result<UpdateManifest, String> {
    let response = http_client()?
        .get(url)
        .send()
        .await
        .map_err(|e| format!("请求更新清单失败: {}", e))?;
    if !response.status().is_success() {
        return Err(format!("请求更新清单失败，状态码 {}", response.status()));
    }
    let text = response
        .text()
        .await
        .map_err(|e| format!("读取更新清单失败: {}", e))?;
    serde_json::from_str(&text).map_err(|e| format!("更新清单格式错误: {}", e))
}

/// 比对远端清单与本地版本，只做检查不下载
#[tauri::command]
pub async fn hot_update_check(app: AppHandle, manifest_url: String) -> Result<HotUpdateCheck, String> {
    let manifest = fetch_manifest(&manifest_url).await?;
    let has_update = version_greater(&manifest.version, &current_version(&app));
    Ok(HotUpdateCheck {
        has_update,
        version: manifest.version,
        note: manifest.note,
        size: manifest.size,
        zip_url: resolve_url(&manifest_url, &manifest.zip_url),
        sha256: manifest.sha256,
    })
}

/// 下载并安装热更包，过程中通过事件上报下载进度
#[tauri::command]
pub async fn hot_update_install(
    app: AppHandle,
    zip_url: String,
    version: String,
    size: u64,
    expected_sha256: String,
) -> Result<String, String> {
    check_version(&version)?;
    let expected_sha256 = expected_sha256.trim();
    if expected_sha256.is_empty() {
        return Err("更新清单缺少校验值，拒绝安装".to_string());
    }
    let root = hot_root(&app)?;
    let web_root = root.join(WEB_DIR);
    fs::create_dir_all(&web_root).map_err(|e| format!("创建资源目录失败: {}", e))?;

    let zip_path = root.join(DOWNLOAD_FILE);
    let mut hasher = Sha256::new();
    download(&app, &zip_url, &zip_path, size, &mut hasher).await?;
    let actual_sha256 = hex_lower(&hasher.finalize());
    if actual_sha256 != expected_sha256.to_ascii_lowercase() {
        let _ = fs::remove_file(&zip_path);
        return Err("更新包校验失败，文件可能已损坏或被篡改".to_string());
    }

    let staging = root.join(STAGING_DIR);
    let _ = fs::remove_dir_all(&staging);
    let extract_zip = zip_path.clone();
    let extract_target = staging.clone();
    let extract_result =
        tauri::async_runtime::spawn_blocking(move || extract(&extract_zip, &extract_target))
            .await
            .map_err(|e| format!("解压任务执行失败: {}", e))?;
    extract_result?;

    if !staging.join("index.html").is_file() {
        let _ = fs::remove_dir_all(&staging);
        return Err("更新包内容不完整，缺少 index.html".to_string());
    }

    // 解压完整后再切到版本目录，避免半包被协议读到导致白屏
    let target = web_root.join(&version);
    let _ = fs::remove_dir_all(&target);
    fs::rename(&staging, &target).map_err(|e| format!("替换资源目录失败: {}", e))?;
    fs::write(root.join(CURRENT_FILE), version.as_bytes())
        .map_err(|e| format!("写入版本标记失败: {}", e))?;
    let _ = fs::remove_file(&zip_path);
    clear_other_versions(&web_root, &version);
    Ok(version)
}

/// 重新加载所有应用窗口，让新版本资源立即生效
#[tauri::command]
pub fn hot_update_reload(app: AppHandle) {
    // 只刷新主窗口与应用窗口，宠物窗口是独立页面，不跟随热更资源
    for (label, window) in app.webview_windows() {
        if label == "main" || label.starts_with("window-") {
            let _ = window.eval("window.location.reload()");
        }
    }
}

async fn download(
    app: &AppHandle,
    url: &str,
    target: &Path,
    fallback_size: u64,
    hasher: &mut Sha256,
) -> Result<(), String> {
    let mut response = http_client()?
        .get(url)
        .send()
        .await
        .map_err(|e| format!("下载更新包失败: {}", e))?;
    if !response.status().is_success() {
        return Err(format!("下载更新包失败，状态码 {}", response.status()));
    }
    // 部分静态服务器不返回 Content-Length，用清单里的 size 兜底保证进度百分比可用
    let header_total = response.content_length().unwrap_or(0);
    let total = if header_total > 0 { header_total } else { fallback_size };
    let mut file = fs::File::create(target).map_err(|e| format!("创建临时文件失败: {}", e))?;
    let mut downloaded: u64 = 0;
    let mut reported: u64 = 0;

    while let Some(chunk) = response
        .chunk()
        .await
        .map_err(|e| format!("读取更新包失败: {}", e))?
    {
        file.write_all(&chunk)
            .map_err(|e| format!("写入更新包失败: {}", e))?;
        hasher.update(&chunk);
        downloaded += chunk.len() as u64;
        let reached_end = total > 0 && downloaded >= total;
        if downloaded - reported >= PROGRESS_STEP || reached_end {
            reported = downloaded;
            let _ = app.emit(
                PROGRESS_EVENT,
                HotUpdateProgress {
                    downloaded,
                    total,
                    percent: percent(downloaded, total),
                },
            );
        }
    }
    let _ = app.emit(
        PROGRESS_EVENT,
        HotUpdateProgress {
            downloaded,
            total,
            percent: 100,
        },
    );
    Ok(())
}

fn percent(downloaded: u64, total: u64) -> u32 {
    if total == 0 {
        return 0;
    }
    (downloaded.min(total) * 100 / total) as u32
}

fn extract(zip_path: &Path, target: &Path) -> Result<(), String> {
    let file = fs::File::open(zip_path).map_err(|e| format!("打开更新包失败: {}", e))?;
    let mut archive = zip::ZipArchive::new(file).map_err(|e| format!("解析更新包失败: {}", e))?;
    fs::create_dir_all(target).map_err(|e| format!("创建解压目录失败: {}", e))?;

    for index in 0..archive.len() {
        let mut entry = archive
            .by_index(index)
            .map_err(|e| format!("读取更新包条目失败: {}", e))?;
        let name = entry
            .name()
            .map_err(|e| format!("读取条目名称失败: {}", e))?
            .into_owned();
        // Windows 打包工具写入的分隔符是反斜杠，统一按两种分隔符拆分后再判断安全性
        let mut segments: Vec<&str> = Vec::new();
        let mut unsafe_path = false;
        for segment in name.split(['/', '\\']) {
            match segment {
                "" | "." => {}
                ".." => {
                    unsafe_path = true;
                    break;
                }
                value => segments.push(value),
            }
        }
        if unsafe_path || segments.is_empty() {
            continue;
        }

        let mut destination = target.to_path_buf();
        for segment in &segments {
            destination.push(segment);
        }
        if entry.is_dir() {
            fs::create_dir_all(&destination).map_err(|e| format!("创建目录失败: {}", e))?;
            continue;
        }
        if let Some(parent) = destination.parent() {
            fs::create_dir_all(parent).map_err(|e| format!("创建目录失败: {}", e))?;
        }
        let mut output = fs::File::create(&destination).map_err(|e| format!("写入文件失败: {}", e))?;
        std::io::copy(&mut entry, &mut output).map_err(|e| format!("解压文件失败: {}", e))?;
    }
    Ok(())
}

/// 安装成功后只保留当前版本，避免热更目录无限增长
fn clear_other_versions(web_root: &Path, keep: &str) {
    let Ok(entries) = fs::read_dir(web_root) else {
        return;
    };
    for entry in entries.flatten() {
        if entry.file_name().to_string_lossy() == keep {
            continue;
        }
        let _ = fs::remove_dir_all(entry.path());
    }
}

/// 当前生效热更版本的资源目录
fn patch_root(app: &AppHandle) -> Option<PathBuf> {
    let version = patch_version(app)?;
    hot_root(app)
        .ok()
        .map(|root| root.join(WEB_DIR).join(version))
}

/// app 协议入口：热更目录优先，缺失的文件回退安装包内置资源
pub fn serve(app: &AppHandle, path: &str) -> Response<Vec<u8>> {
    let relative = match normalize_path(path) {
        Some(value) => value,
        None => return text_response(StatusCode::NOT_FOUND, "not found"),
    };
    if let Some(root) = patch_root(app) {
        if let Ok(body) = fs::read(root.join(&relative)) {
            return build_response(StatusCode::OK, mime_of(&relative), cache_control(&relative), body);
        }
    }
    if let Some(asset) = app.asset_resolver().get(relative.clone()) {
        return build_response(StatusCode::OK, &asset.mime_type, cache_control(&relative), asset.bytes);
    }
    // 带扩展名的资源缺失即 404；无扩展名按入口页处理，兼容直接访问前端路由
    if Path::new(&relative).extension().is_none() {
        if let Some(asset) = app.asset_resolver().get("index.html".to_string()) {
            return build_response(StatusCode::OK, &asset.mime_type, "no-cache", asset.bytes);
        }
    }
    text_response(StatusCode::NOT_FOUND, "not found")
}

/// 归一化请求路径，出现上跳段直接判为非法，避免越出资源目录
/// Windows 上反斜杠也是路径分隔符，%5C 解码后同样要拦截，与解压侧保持一致
fn normalize_path(path: &str) -> Option<String> {
    let decoded = percent_decode(path);
    let mut segments: Vec<&str> = Vec::new();
    for segment in decoded.split(['/', '\\']) {
        match segment {
            "" | "." => {}
            ".." => return None,
            value => segments.push(value),
        }
    }
    if segments.is_empty() {
        return None;
    }
    Some(segments.join("/"))
}

/// 资源名可能含中文等非 ASCII 字符，WebView 会按百分号编码请求
fn percent_decode(path: &str) -> String {
    let bytes = path.as_bytes();
    let mut output: Vec<u8> = Vec::with_capacity(bytes.len());
    let mut index = 0;
    while index < bytes.len() {
        if bytes[index] == b'%' && index + 2 < bytes.len() {
            let high = (bytes[index + 1] as char).to_digit(16);
            let low = (bytes[index + 2] as char).to_digit(16);
            if let (Some(high), Some(low)) = (high, low) {
                output.push((high * 16 + low) as u8);
                index += 3;
                continue;
            }
        }
        output.push(bytes[index]);
        index += 1;
    }
    String::from_utf8_lossy(&output).into_owned()
}

/// Vite 产物带内容哈希，可长期缓存；入口与其余文件每次回源校验
fn cache_control(path: &str) -> &'static str {
    if path.starts_with("assets/") {
        "public, max-age=31536000, immutable"
    } else {
        "no-cache"
    }
}

fn mime_of(path: &str) -> &'static str {
    let extension = Path::new(path)
        .extension()
        .and_then(|value| value.to_str())
        .unwrap_or_default()
        .to_ascii_lowercase();
    match extension.as_str() {
        "html" => "text/html; charset=utf-8",
        "js" | "mjs" => "text/javascript; charset=utf-8",
        "css" => "text/css; charset=utf-8",
        "json" | "map" => "application/json; charset=utf-8",
        "txt" => "text/plain; charset=utf-8",
        "svg" => "image/svg+xml",
        "png" => "image/png",
        "jpg" | "jpeg" => "image/jpeg",
        "gif" => "image/gif",
        "webp" => "image/webp",
        "ico" => "image/x-icon",
        "woff" => "font/woff",
        "woff2" => "font/woff2",
        "ttf" => "font/ttf",
        "otf" => "font/otf",
        "wasm" => "application/wasm",
        "pdf" => "application/pdf",
        "mp4" => "video/mp4",
        _ => "application/octet-stream",
    }
}

fn build_response(
    status: StatusCode,
    mime: &str,
    cache: &str,
    body: Vec<u8>,
) -> Response<Vec<u8>> {
    Response::builder()
        .status(status)
        .header("Content-Type", mime)
        .header("Cache-Control", cache)
        .body(body)
        .expect("static web asset response")
}

fn text_response(status: StatusCode, body: &str) -> Response<Vec<u8>> {
    build_response(status, "text/plain; charset=utf-8", "no-cache", body.as_bytes().to_vec())
}
