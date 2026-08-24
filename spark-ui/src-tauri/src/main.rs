// 桌面端主进程：窗口、系统托盘、新窗口与外部链接接管
// Release 构建时隐藏 Windows 控制台窗口
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::sync::atomic::{AtomicU32, Ordering};

use tauri::{
    menu::{Menu, MenuItem},
    tray::{MouseButton, MouseButtonState, TrayIconBuilder, TrayIconEvent},
    Manager, WebviewUrl, WebviewWindowBuilder,
};
use tauri_plugin_opener::OpenerExt;

/// 新窗口自增序号，用于生成唯一窗口 label
static WINDOW_SEQ: AtomicU32 = AtomicU32::new(0);

/// 打开应用内部路由新窗口
/// route 为 hash 路由（如 #/document/preview?id=1），与主窗口共用登录态（localStorage 同源共享）
/// 注意：必须为 async 命令。同步命令在主线程执行，而窗口创建需要主线程事件循环配合，
/// 两者互相等待会在 Windows 上死锁（新窗口空白且整个应用卡死，只能强制结束进程）
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

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![open_app_window, open_external_url])
        .setup(|app| {
            // 系统托盘：显示窗口 / 退出
            let show = MenuItem::with_id(app, "show", "显示窗口", true, None::<&str>)?;
            let quit = MenuItem::with_id(app, "quit", "退出", true, None::<&str>)?;
            let menu = Menu::with_items(app, &[&show, &quit])?;

            TrayIconBuilder::with_id("main-tray")
                .icon(app.default_window_icon().unwrap().clone())
                .tooltip("星火云")
                .menu(&menu)
                // 左键点击不弹菜单，由点击事件显示主窗口
                .show_menu_on_left_click(false)
                .on_menu_event(|app, event| match event.id.as_ref() {
                    "show" => show_main_window(app),
                    "quit" => app.exit(0),
                    _ => {}
                })
                .on_tray_icon_event(|tray, event| {
                    // 单击托盘图标显示主窗口
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
