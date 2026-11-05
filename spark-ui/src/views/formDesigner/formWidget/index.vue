<template>
  <div
      class="form-widget-box"
      :class="{ 'form-widget-box--dragover': isDragOver }"
      @dragenter.prevent="handleDragEnter"
      @dragover.prevent
      @dragleave="handleDragLeave"
      @drop.prevent="handleDropWidget"
  >
    <center v-if="props.designer.widgetList.length === 0" class="form-empty"> 请从左侧组件库中选择一个组件 </center>
    <el-form
        v-else
        :label-position="props.designer.formConfig.position"
        :size="props.designer.formConfig.size"
    >
      <el-row :gutter="24">
        <el-col :span="widget.config.width"
              v-for="(widget, index) in props.designer.widgetList"
              :key="index"
              :class="[handleSelectedStyle(index), { 'widget-dragging': dragIndex === index, 'widget-drag-over': dragOverIndex === index && dragIndex !== null && dragIndex !== index }]"
              draggable="true"
              @click="handleSelectWidget(index)"
              @dragstart="handleWidgetDragStart(index, $event)"
              @dragover.prevent="handleWidgetDragOver(index, $event)"
              @drop.prevent="handleWidgetDrop(index, $event)"
              @dragend="handleWidgetDragEnd">
          <component
              :is="getFieldName(widget)"
              :widget="widget"
              :key="widget.config.code"
          />
          <slot v-if="props.designer.selectedId === index">
            <div class="widget-tool">
              <button
                  class="widget-tool-btn is-ghost"
                  @click.stop="handleDeleteWidget(index)"
              >
                ✕
              </button>
              <button
                  class="widget-tool-btn is-copy"
                  @click.stop="handleCopyWidget(index)"
              >
                复制
              </button>
            </div>
          </slot>
      </el-col>
      </el-row>
    </el-form>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { basicWidgetList, sysWidgetList } from '../widgetPanel/widgetList.js';

const props = defineProps({
  designer: Object
})

// 拖拽高亮状态
const isDragOver = ref(false);
let dragCounter = 0;

/**
 * 获取自定义组件名称
 * @param widget
 * @returns {string}
 */
function getFieldName(widget) {
  return "custom-"+widget.type;
}

/**
 * 拖拽进入画布：计数加一并高亮
 */
function handleDragEnter() {
  dragCounter++;
  isDragOver.value = true;
}

/**
 * 拖拽离开画布：计数减一，归零后取消高亮
 */
function handleDragLeave() {
  dragCounter--;
  if (dragCounter <= 0) {
    dragCounter = 0;
    isDragOver.value = false;
  }
}

/**
 * 画布拖放：按组件类型新增组件
 * @param event
 */
function handleDropWidget(event) {
  dragCounter = 0;
  isDragOver.value = false;
  const type = event.dataTransfer.getData('application/x-widget-type');
  if (!type) {
    return;
  }
  const template = [...basicWidgetList, ...sysWidgetList].find(w => w.type === type);
  if (!template) {
    return;
  }
  const widget = JSON.parse(JSON.stringify(template));
  widget.config.code = "field" + Date.now();
  props.designer.widgetList.push(widget);
  props.designer.selectedId = props.designer.widgetList.length - 1;
}

/**
 * 选择组件
 * @param index
 */
function handleSelectWidget(index) {
  props.designer.selectedId = index;
}

/**
 * 处理选中的边框样式
 * @param widget
 */
function handleSelectedStyle(index) {
  if (props.designer.selectedId === index) {
    return 'selected_component'
  } else {
    return 'no_select_component'
  }
}

/**
 * 移除组件
 * @param index
 * 使用 .stop 修饰符来阻止事件冒泡 不然会出现触发前一个组件的handleSelectWidget 导致异常
 */
function handleDeleteWidget(index) {
  props.designer.widgetList.splice(index, 1);
  props.designer.selectedId = null;
}

/**
 * 复制组件
 */
function handleCopyWidget(index) {
  const widget = props.designer.widgetList[index];
  // 这里对widget进行深拷贝，不然会出现推入新对象时却发现数组中新增的对象变成了之前修改过的对象的样子
  const widget_ = JSON.parse(JSON.stringify(widget));
  widget_.config.code = "field"+Date.now();
  props.designer.widgetList.push(widget_);
  // 复制后仍保持选中原组件，不跳到末尾的副本上；push 追加到末尾，原组件下标不变
  props.designer.selectedId = index;
}

// 画布内组件拖动排序状态
const dragIndex = ref(null);
const dragOverIndex = ref(null);

/**
 * 组件拖拽开始：记录源下标
 * @param index
 * @param event
 */
function handleWidgetDragStart(index, event) {
  dragIndex.value = index;
  event.dataTransfer.setData('application/x-widget-index', String(index));
  event.dataTransfer.effectAllowed = 'move';
}

/**
 * 组件拖拽悬停：记录目标下标
 * @param index
 * @param event
 */
function handleWidgetDragOver(index, event) {
  if (event.dataTransfer.getData('application/x-widget-index') !== '') {
    dragOverIndex.value = index;
  }
}

/**
 * 组件拖拽释放：调整组件顺序
 * @param index
 * @param event
 */
function handleWidgetDrop(index, event) {
  // 非画布内组件拖拽（如面板新增）不在此处理，交由容器 handleDropWidget
  if (event.dataTransfer.getData('application/x-widget-index') === '') {
    return;
  }
  if (dragIndex.value === null || dragIndex.value === index) {
    resetWidgetDrag();
    return;
  }
  const list = props.designer.widgetList;
  const [moved] = list.splice(dragIndex.value, 1);
  list.splice(index, 0, moved);
  props.designer.selectedId = index;
  resetWidgetDrag();
}

/**
 * 组件拖拽结束：清空状态
 */
function handleWidgetDragEnd() {
  resetWidgetDrag();
}

function resetWidgetDrag() {
  dragIndex.value = null;
  dragOverIndex.value = null;
}
</script>
<style scoped lang="scss">
.form-widget-box {
  padding: 16px 24px;
  height: 100%;
  overflow-y : auto;
  overflow-x: hidden;
  // 星星点点网格颜色取主题令牌，亮暗主题自动切换
  background-color: $app-canvas-bg;
  background-image:
    radial-gradient(circle, $app-canvas-dot 1.2px, transparent 1.8px),
    radial-gradient(circle, $app-canvas-dot-soft 1px, transparent 1.6px);
  background-size: 22px 22px, 22px 22px;
  background-position: 0 0, 11px 11px;
}
.form-widget-box--dragover {
  border: 2px dashed $el-color-primary;
  background-color: $app-primary-soft;
}
.form-empty {
  padding-top: 40px;
  font-size: 13px;
  color: $app-text-placeholder;
}
// 字段间距：紧凑（去卡片化后靠轻量悬停/选中态表达交互）
:deep(.el-col) {
  padding-top: 4px;
  padding-bottom: 4px;
  margin-top: 6px;
}
:deep(.el-col .el-form-item) {
  margin-bottom: 0;
}
// 非选中：无边框透明底，悬停淡蓝虚线感
.no_select_component {
  border: 1.5px solid transparent;
  cursor: pointer;
  border-radius: 8px;
  transition: $transition-fast;

  &:hover {
    border-color: rgba(47, 123, 255, 0.3);
    background-color: $app-primary-soft;
  }
}
// 选中：细蓝边 + 左侧渐变色条 + 轻投影
.selected_component {
  border: 1.5px solid $el-color-primary;
  box-shadow: 0 2px 10px rgba(47, 123, 255, 0.10);
  cursor: pointer;
  position: relative;
  border-radius: 8px;
  background-color: $app-primary-soft;

  &::before {
    content: "";
    position: absolute;
    left: -1.5px;
    top: 6px;
    bottom: 6px;
    width: 3px;
    border-radius: 3px;
    background: linear-gradient(180deg, $el-color-primary, $el-color-primary-light-5);
  }
}
.widget-dragging {
  opacity: 0.5;
}
.widget-drag-over {
  border-top: 2px solid $el-color-primary;
  background: $app-primary-soft;
}
// 悬浮工具：选中字段顶部浮出的胶囊按钮组
.widget-tool {
  position: absolute;
  top: -10px;
  right: 8px;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 3px;
}
.widget-tool-btn {
  height: 20px;
  padding: 0 8px;
  border: none;
  border-radius: 10px;
  font-size: 11px;
  line-height: 20px;
  cursor: pointer;
  transition: $transition-fast;

  &.is-copy {
    background: $el-color-primary;
    color: $color-text-white;
    box-shadow: 0 2px 8px rgba(47, 123, 255, 0.35);

    &:hover { filter: brightness(1.08); }
  }

  &.is-ghost {
    background: $app-bg-card;
    color: $el-color-danger;
    box-shadow: 0 2px 6px rgba(23, 43, 77, 0.12);

    &:hover { border-color: $el-color-danger; }
  }
}
</style>
