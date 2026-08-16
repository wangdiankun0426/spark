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
              <el-button
                  class="widget-tool-btn"
                  @click.stop="handleDeleteWidget(index)"
                  type="warning"
              >
                移除
              </el-button>
              <el-button
                  class="widget-tool-btn"
                  type="primary"
                  @click.stop="handleCopyWidget(index)"
              >
                复制
              </el-button>
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
  props.designer.selectedId = props.designer.widgetList.length-1;
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
<style scoped>
.form-widget-box {
  padding: 0 20px;
  height: 88vh;
  overflow-y : auto;
  overflow-x: hidden;
}
.form-widget-box--dragover {
  border: 2px dashed #0052cc;
  background: rgba(0, 82, 204, 0.04);
}
.form-empty {
  padding-top: 40px;
  font-size: 13px;
  color: #8c939d;
}
.selected_component {
  border: 1px dashed #0052cc;
  cursor: pointer;
  position: relative;
}
.no_select_component {
  cursor: pointer;
}
.widget-dragging {
  opacity: 0.5;
}
.widget-drag-over {
  border-top: 2px solid #0052cc;
  background: rgba(0, 82, 204, 0.04);
}
.widget-tool {
  position: absolute;
  bottom: 0;
  right: 0;
  z-index: 1;
  width: 110px;
  height: 20px
}
.widget-tool-btn {
  height: 100%;
  width: 45px;
  float: right;
  color: white;
}
</style>
