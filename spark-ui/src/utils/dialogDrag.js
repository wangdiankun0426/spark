/**
 * 全局弹窗拖拽
 * @author wangdiankun
 * @since 2026-10-30
 */

let dragState = null;

/**
 * 解析元素内联 transform 中已有的 translate 偏移
 * @param el 弹窗元素
 * @returns {{x: number, y: number}}
 */
function getTranslate(el) {
  const match = /translate\(\s*(-?[\d.]+)px\s*,\s*(-?[\d.]+)px\s*\)/.exec(el.style.transform);
  return match ? { x: parseFloat(match[1]), y: parseFloat(match[2]) } : { x: 0, y: 0 };
}

const HEADER_SELECTOR = '.el-dialog__header, .el-message-box__header';
// 自定义关闭钮（流程详情 .fd-close / 个人中心 .up-close）不作为拖动把手
const CLOSE_BTN_SELECTOR = '.el-dialog__headerbtn, .el-message-box__headerbtn, .fd-close, .up-close';
const BOX_SELECTOR = '.el-dialog, .el-message-box';

function onMouseDown(e) {
  const header = e.target.closest && e.target.closest(HEADER_SELECTOR);
  if (!header || e.target.closest(CLOSE_BTN_SELECTOR)) {
    return;
  }
  const dialog = header.closest(BOX_SELECTOR);
  if (!dialog || dialog.classList.contains('is-fullscreen')) {
    return;
  }
  const origin = getTranslate(dialog);
  dragState = { dialog, startX: e.clientX, startY: e.clientY, originX: origin.x, originY: origin.y };
  document.body.style.userSelect = 'none';
}

function onMouseMove(e) {
  if (!dragState) {
    return;
  }
  const x = dragState.originX + e.clientX - dragState.startX;
  const y = dragState.originY + e.clientY - dragState.startY;
  dragState.dialog.style.transform = `translate(${x}px, ${y}px)`;
}

function onMouseUp() {
  if (!dragState) {
    return;
  }
  dragState = null;
  document.body.style.userSelect = '';
}

function onDoubleClick(e) {
  const header = e.target.closest && e.target.closest(HEADER_SELECTOR);
  if (!header || e.target.closest(CLOSE_BTN_SELECTOR)) {
    return;
  }
  const dialog = header.closest(BOX_SELECTOR);
  if (dialog) {
    dialog.style.transform = '';
  }
}

/**
 * 初始化全局弹窗拖拽：el-dialog / ElMessageBox 按住标题栏拖动，双击标题栏复位
 */
export function initDialogDrag() {
  document.addEventListener('mousedown', onMouseDown);
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
  document.addEventListener('dblclick', onDoubleClick);
}
