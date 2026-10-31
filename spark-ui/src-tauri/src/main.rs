// 桌面端主进程：窗口、系统托盘、新窗口与外部链接接管
// Release 构建时隐藏 Windows 控制台窗口
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::sync::atomic::{AtomicBool, AtomicU32, Ordering};

use tauri::{
    menu::{Menu, MenuItem, PredefinedMenuItem},
    tray::{MouseButton, MouseButtonState, TrayIconBuilder, TrayIconEvent},
    Emitter, Manager, PhysicalPosition, WebviewUrl, WebviewWindowBuilder,
};
use tauri_plugin_opener::OpenerExt;

/// 新窗口自增序号，用于生成唯一窗口 label
static WINDOW_SEQ: AtomicU32 = AtomicU32::new(0);

/// 桌面宠物退场动画播放中，页面收到 pet-exit 后自行隐藏，此标记用于处理动画期间的再召唤
static PET_EXITING: AtomicBool = AtomicBool::new(false);

/// 桌面宠物窗口逻辑尺寸，高度按宠物本体加头顶倒计时和气泡空间紧凑收口，
/// 贴屏幕上沿或下沿时不会出现大块透明空白
const PET_W: f64 = 240.0;
const PET_H: f64 = 180.0;

/// 宠物头顶距窗口顶部的逻辑偏移，由舞台高度、底部留白和图像 bbox 偏移组成，
/// 计算方式 180 - 2 - 120 + 184/1024*120 约等于 79.6
const PET_HEAD_TOP: f64 = 79.6;
/// 宠物本体左右留白，允许窗口横向越界让宠物贴住屏幕左右边缘
const PET_SIDE_PAD: f64 = 79.0;
/// 松手时窗口顶部距屏幕上沿小于该逻辑距离即吸附贴顶
const PET_SNAP_BAND: f64 = 40.0;

/// 桌面宠物静态资源，编译期内嵌进二进制，客户端专属，不进前端 dist
static PET_HTML: &[u8] = include_bytes!("../desktopPet/index.html");
static PET_JS: &[u8] = include_bytes!("../desktopPet/pet.js");
/// 姿势图按动作分包在 desktopPet/poses/<动作名>/ 下，方便单独迭代
/// 资源 key 与磁盘相对路径一致，pet.js 按同一路径引用
/// 宏让 key 与 include_bytes! 路径同源，新增帧只需写一次文件名
macro_rules! pose_asset {
    ($file:literal) => {
        (
            concat!("poses/", $file),
            include_bytes!(concat!("../desktopPet/poses/", $file)),
        )
    };
}

static PET_POSES: &[(&str, &[u8])] = &[
    // 待机与表情
    pose_asset!("idle/idle.png"),
    pose_asset!("happy/happy.png"),
    pose_asset!("sad/sad.png"),
    // 睡觉
    pose_asset!("sleep/sleep.png"),
    // 跑两步
    pose_asset!("run/run-1.png"),
    pose_asset!("run/run-2.png"),
    pose_asset!("run/run-3.png"),
    pose_asset!("run/run-4.png"),
    pose_asset!("run/run-5.png"),
    pose_asset!("run/run-6.png"),
    pose_asset!("run/run-7.png"),
    // 敲木鱼
    pose_asset!("wooden-fish/wooden-fish-1.png"),
    pose_asset!("wooden-fish/wooden-fish-3.png"),
    pose_asset!("wooden-fish/wooden-fish-4.png"),
    pose_asset!("wooden-fish/wooden-fish-5.png"),
    pose_asset!("wooden-fish/wooden-fish-6.png"),
    pose_asset!("wooden-fish/wooden-fish-7.png"),
];

/// 宠物窗口加载地址，自定义协议在 Windows 上由 WebView2 映射为 http://pet.localhost/
fn pet_webview_url() -> tauri::Url {
    let raw = if cfg!(windows) {
        "http://pet.localhost/index.html"
    } else {
        "pet://localhost/index.html"
    };
    raw.parse().expect("pet url must be valid")
}

/// 按路径返回内嵌的宠物资源，作为自定义协议的处理器
fn pet_asset_response(path: &str) -> tauri::http::Response<Vec<u8>> {
    let (body, mime, status): (&[u8], &str, u16) = match path {
        "index.html" => (PET_HTML, "text/html; charset=utf-8", 200),
        "pet.js" => (PET_JS, "text/javascript; charset=utf-8", 200),
        _ => match PET_POSES.iter().find(|(name, _)| *name == path) {
            Some((_, data)) => (*data, "image/png", 200),
            None => (&b"not found"[..], "text/plain; charset=utf-8", 404),
        },
    };
    tauri::http::Response::builder()
        .status(status)
        .header("Content-Type", mime)
        .header("Cache-Control", "no-cache")
        .body(body.to_vec())
        .expect("static pet asset response")
}

/// 打开应用内部路由新窗口
/// route 为 hash 路由，与主窗口共用登录态
/// 必须为 async 命令，同步命令在主线程执行，窗口创建需要主线程事件循环配合，
/// 两者互相等待会在 Windows 上死锁
#[tauri::command]
async fn open_app_window(app: tauri::AppHandle, route: String) {
    let label = format!("window-{}", WINDOW_SEQ.fetch_add(1, Ordering::Relaxed));
    let url = WebviewUrl::App(format!("index.html{}", route).into());
    let result = WebviewWindowBuilder::new(&app, label, url)
        .title("星火云")
        .inner_size(1400.0, 900.0)
        .min_inner_size(1200.0, 800.0)
        .center()
        .build();
    if let Err(e) = result {
        eprintln!("failed to open app window: {}", e);
    }
}

/// 用系统默认浏览器打开外部链接
#[tauri::command]
fn open_external_url(app: tauri::AppHandle, url: String) {
    if let Err(e) = app.opener().open_url(url, None::<&str>) {
        eprintln!("failed to open external url: {}", e);
    }
}

/// 显示并聚焦主窗口
fn show_main_window(app: &tauri::AppHandle) {
    if let Some(window) = app.get_webview_window("main") {
        let _ = window.show();
        let _ = window.set_focus();
    }
}

/// 计算桌面宠物在主显示器工作区右下角的物理坐标，位于任务栏上方
fn pet_bottom_right_pos(app: &tauri::AppHandle) -> Option<(i32, i32)> {
    let monitor = app.primary_monitor().ok().flatten()?;
    let work_area = monitor.work_area();
    let scale = monitor.scale_factor();
    let w = (PET_W * scale) as i32;
    let h = (PET_H * scale) as i32;
    let margin_x = (8.0 * scale) as i32;
    let x = work_area.position.x + work_area.size.width as i32 - w - margin_x;
    // 垂直不留额外边距：窗口底边贴工作区下沿，宠物脚正好落在任务栏上沿
    let y = work_area.position.y + work_area.size.height as i32 - h;
    Some((x, y))
}

/// 获取或首次创建桌面宠物窗口，仅在创建时定位右下角，之后尊重用户拖拽位置
fn ensure_pet_window(app: &tauri::AppHandle) -> Result<tauri::WebviewWindow, String> {
    if let Some(pet) = app.get_webview_window("pet") {
        return Ok(pet);
    }
    let mut builder = WebviewWindowBuilder::new(app, "pet", WebviewUrl::External(pet_webview_url()))
        .title("小星")
        .inner_size(PET_W, PET_H)
        .decorations(false)
        .transparent(true)
        .always_on_top(true)
        .skip_taskbar(true)
        .resizable(false)
        .shadow(false)
        .focused(false)
        .visible(false);
    if let Some((x, y)) = pet_bottom_right_pos(app) {
        builder = builder.position(x as f64, y as f64);
    }
    let pet = builder.build().map_err(|e| e.to_string())?;
    // builder.position 按逻辑像素处理，这里用物理坐标再校准，
    // 保证高缩放屏上也能精确落在工作区右下角
    if let Some((x, y)) = pet_bottom_right_pos(app) {
        let _ = pet.set_position(PhysicalPosition::new(x, y));
    }
    Ok(pet)
}

/// 切换桌面宠物显示/隐藏；首次调用时创建窗口
/// 返回切换后的可见状态
fn toggle_pet_impl(app: &tauri::AppHandle) -> Result<bool, String> {
    let pet = ensure_pet_window(app)?;
    let visible = pet.is_visible().map_err(|e| e.to_string())?;
    if visible && !PET_EXITING.load(Ordering::Relaxed) {
        // 收起时通知宠物页面播放退场动画，页面播完自行 hide
        // 1.5s 兜底，页面异常未隐藏则强制隐藏
        PET_EXITING.store(true, Ordering::Relaxed);
        let _ = app.emit_to("pet", "pet-exit", ());
        let handle = app.clone();
        std::thread::spawn(move || {
            std::thread::sleep(std::time::Duration::from_millis(1500));
            if PET_EXITING.swap(false, Ordering::Relaxed) {
                if let Some(w) = handle.get_webview_window("pet") {
                    if w.is_visible().unwrap_or(false) {
                        let _ = w.hide();
                    }
                }
            }
        });
        Ok(false)
    } else if visible {
        // 退场动画期间又被召回：终止退场，播放出场动画
        PET_EXITING.store(false, Ordering::Relaxed);
        let _ = app.emit_to("pet", "pet-enter", ());
        Ok(true)
    } else {
        PET_EXITING.store(false, Ordering::Relaxed);
        pet.show().map_err(|e| e.to_string())?;
        let _ = app.emit_to("pet", "pet-enter", ());
        Ok(true)
    }
}

/// 把宠物窗口钳制回当前显示器工作区，全部按物理像素计算：
/// - 底部贴住工作区下沿，宠物脚踩在任务栏上沿
/// - 顶部进入吸附带时上移窗口，让宠物头顶齐屏幕上沿
/// - 左右允许 PET_SIDE_PAD 越界，让宠物本体贴住屏幕两侧
/// 返回钳制后的物理坐标
#[tauri::command]
fn pet_fix_position(app: tauri::AppHandle) -> Result<(i32, i32), String> {
    let pet = app.get_webview_window("pet").ok_or("pet window not found")?;
    let pos = pet.outer_position().map_err(|e| e.to_string())?;
    let monitor = pet
        .current_monitor()
        .map_err(|e| e.to_string())?
        .ok_or("no monitor")?;
    let wa = monitor.work_area();
    let scale = monitor.scale_factor();

    let win_w = (PET_W * scale).round() as i32;
    let win_h = (PET_H * scale).round() as i32;
    let head_top = (PET_HEAD_TOP * scale).round() as i32;
    let pad = (PET_SIDE_PAD * scale).round() as i32;
    let band = (PET_SNAP_BAND * scale).round() as i32;

    let wa_x = wa.position.x;
    let wa_y = wa.position.y;
    let wa_w = wa.size.width as i32;
    let wa_h = wa.size.height as i32;

    // 左右允许宠物本体贴边，窗口透明留白可以出屏
    let x = pos
        .x
        .clamp(wa_x - pad, wa_x + wa_w - win_w + pad);

    // 上下：底部不进任务栏；靠近顶部时吸附，让头顶齐屏幕上沿
    let y = if pos.y <= wa_y + band {
        wa_y - head_top
    } else {
        pos.y.min(wa_y + wa_h - win_h)
    };

    if x != pos.x || y != pos.y {
        pet.set_position(PhysicalPosition::new(x, y))
            .map_err(|e| e.to_string())?;
    }
    Ok((x, y))
}

/// 切换后同步托盘菜单文案：未显示 -> "召唤小星"，已显示 -> "送走小星"
fn update_pet_tray_menu(app: &tauri::AppHandle, visible: bool) {
    let Some(tray) = app.tray_by_id("main-tray") else {
        return;
    };
    let label = if visible { "送走小星" } else { "召唤小星" };
    let build = (|| -> tauri::Result<()> {
        let pet = MenuItem::with_id(app, "pet", label, true, None::<&str>)?;
        let sep = PredefinedMenuItem::separator(app)?;
        let quit = MenuItem::with_id(app, "quit", "退出星火云", true, None::<&str>)?;
        let menu = Menu::with_items(app, &[&pet, &sep, &quit])?;
        tray.set_menu(Some(menu))
    })();
    if let Err(e) = build {
        eprintln!("failed to update pet tray menu: {}", e);
    }
}

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_fs::init())
        .invoke_handler(tauri::generate_handler![
            open_app_window,
            open_external_url,
            pet_fix_position
        ])
        // 自定义协议 pet://：向宠物窗口提供内嵌的宠物页面与姿势图资源
        .register_uri_scheme_protocol("pet", |_ctx, request| {
            let path = request.uri().path().trim_start_matches('/');
            let path = if path.is_empty() { "index.html" } else { path };
            pet_asset_response(path)
        })
        .setup(|app| {
            // 系统托盘右键菜单提供桌面宠物开关和退出，左键点击托盘图标显示主窗口
            // 初始宠物未显示，菜单文案为召唤小星，切换后由 update_pet_tray_menu 同步
            let pet = MenuItem::with_id(app, "pet", "召唤小星", true, None::<&str>)?;
            let sep = PredefinedMenuItem::separator(app)?;
            let quit = MenuItem::with_id(app, "quit", "退出星火云", true, None::<&str>)?;
            let menu = Menu::with_items(app, &[&pet, &sep, &quit])?;

            let tray_icon = tauri::image::Image::from_bytes(include_bytes!("../icons/tray-icon.png"))
                .unwrap_or_else(|_| app.default_window_icon().unwrap().clone());

            TrayIconBuilder::with_id("main-tray")
                .icon(tray_icon)
                .tooltip("星火云")
                .menu(&menu)
                // 左键点击不弹菜单，由点击事件显示主窗口
                .show_menu_on_left_click(false)
                .on_menu_event(|app, event| match event.id.as_ref() {
                    "quit" => app.exit(0),
                    // 托盘菜单事件在主线程回调，窗口创建需子线程执行，
                    // 否则会与事件循环互相等待导致 Windows 死锁，同 open_app_window 注释
                    "pet" => {
                        let handle = app.clone();
                        std::thread::spawn(move || {
                            match toggle_pet_impl(&handle) {
                                Ok(visible) => update_pet_tray_menu(&handle, visible),
                                Err(e) => eprintln!("failed to toggle pet window: {}", e),
                            }
                        });
                    }
                    _ => {}
                })
                .on_tray_icon_event(|tray, event| {
                    if let TrayIconEvent::Click {
                        button: MouseButton::Left,
                        button_state: MouseButtonState::Up,
                        ..
                    } = event
                    {
                        show_main_window(tray.app_handle());
                    }
                })
                .build(app)?;

            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
