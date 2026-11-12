// 桌面端主进程：窗口、系统托盘、新窗口与外部链接接管
// Release 构建时隐藏 Windows 控制台窗口
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod hot_update;

use std::sync::atomic::{AtomicU32, Ordering};

use tauri::{
    menu::{Menu, MenuItem},
    tray::{MouseButton, MouseButtonState, TrayIconBuilder, TrayIconEvent},
    Manager, WebviewUrl, WebviewWindowBuilder,
};
use tauri_plugin_opener::OpenerExt;

/// 新窗口自增序号，用于生成唯一窗口 label
static WINDOW_SEQ: AtomicU32 = AtomicU32::new(0);

/// 应用窗口加载地址，走 app 协议以便热更目录覆盖内置资源
/// 开发模式由 Vite 开发服务器承载，不使用该协议
fn app_webview_url(route: &str) -> tauri::Url {
    let raw = if cfg!(windows) {
        format!("http://app.localhost/index.html{}", route)
    } else {
        format!("app://localhost/index.html{}", route)
    };
    raw.parse().expect("app url must be valid")
}

/// 打开应用内部路由新窗口
/// route 为 hash 路由，与主窗口共用登录态
/// 必须为 async 命令，同步命令在主线程执行，窗口创建需要主线程事件循环配合，
/// 两者互相等待会在 Windows 上死锁
#[tauri::command]
async fn open_app_window(app: tauri::AppHandle, route: String) {
    let label = format!("window-{}", WINDOW_SEQ.fetch_add(1, Ordering::Relaxed));
    let url = if tauri::is_dev() {
        WebviewUrl::App(format!("index.html{}", route).into())
    } else {
        WebviewUrl::External(app_webview_url(&route))
    };
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

/// 显示并聚焦主窗口，最小化或隐藏状态下都直接恢复到前台
fn show_main_window(app: &tauri::AppHandle) {
    if let Some(window) = app.get_webview_window("main") {
        let _ = window.unminimize();
        let _ = window.show();
        let _ = window.set_focus();
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
            hot_update::hot_update_check,
            hot_update::hot_update_install,
            hot_update::hot_update_reload
        ])
        // 主窗口收到关闭请求时隐藏到托盘而不是退出进程，退出统一走托盘菜单
        // 子窗口不拦截，保持系统标题栏的正常关闭行为
        .on_window_event(|window, event| {
            if window.label() == "main" {
                if let tauri::WindowEvent::CloseRequested { api, .. } = event {
                    let _ = window.hide();
                    api.prevent_close();
                }
            }
        })
        // 自定义协议 app://：正式包的前端资源入口，热更目录优先、安装包内置资源兜底
        .register_uri_scheme_protocol("app", |ctx, request| {
            hot_update::serve(ctx.app_handle(), request.uri().path())
        })
        .setup(|app| {
            // 主窗口按环境选择加载地址：开发模式走 Vite devUrl 并支持 HMR，正式包走 app 协议承接热更资源
            // 静态配置无法按环境切换 URL，故与 open_app_window 一样在代码中分支
            let main_url = if tauri::is_dev() {
                WebviewUrl::App("index.html".into())
            } else {
                WebviewUrl::External(app_webview_url(""))
            };
            WebviewWindowBuilder::new(app, "main", main_url)
                .title("星火云")
                .inner_size(1400.0, 900.0)
                .min_inner_size(1200.0, 800.0)
                .center()
                .decorations(false)
                .build()?;
            // 安装包升级后清理已失效的旧热更资源
            hot_update::cleanup_outdated(app.handle());
            // 系统托盘右键菜单提供退出，左键点击托盘图标显示主窗口
            let quit = MenuItem::with_id(app, "quit", "退出星火云", true, None::<&str>)?;
            let menu = Menu::with_items(app, &[&quit])?;

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
