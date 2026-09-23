<template>
  <div class="widget-lib">
    <div class="lib-group">
      <div class="lib-group-title">基础组件</div>
      <div class="component-grid">
        <el-button
            v-for="(basicWidget) in basicWidgetList"
            class="component-btn"
            draggable="true"
            @click="addWidget(basicWidget)"
            @dragstart="handleDragStart($event, basicWidget.type)"
        >
          <el-icon><component :is="widgetIconMap[basicWidget.type] || 'EditPen'" /></el-icon>
          <span>{{ basicWidget.name }}</span>
        </el-button>
      </div>
    </div>
    <div class="lib-group">
      <div class="lib-group-title">系统组件</div>
      <div class="component-grid">
        <el-button
            v-for="(basicWidget) in sysWidgetList"
            class="component-btn"
            draggable="true"
            @click="addWidget(basicWidget)"
            @dragstart="handleDragStart($event, basicWidget.type)"
        >
          <el-icon><component :is="widgetIconMap[basicWidget.type] || 'EditPen'" /></el-icon>
          <span>{{ basicWidget.name }}</span>
        </el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import {basicWidgetList, sysWidgetList} from "./widgetList.js"

const props = defineProps({
  designer: Object,
})

/** 组件类型 → 图标映射 */
const widgetIconMap = {
  input: 'EditPen',
  textarea: 'Document',
  radio: 'CircleCheck',
  select: 'ArrowDown',
  number: 'Plus',
  date: 'Calendar',
  'select-user': 'User',
  'select-dept': 'OfficeBuilding',
  'select-role': 'Avatar',
  'select-knowledge': 'Collection',
  'select-graph': 'Share',
  'upload-attachment': 'UploadFilled',
};

/**
 * 添加组件
 * @param widget
 */
function addWidget(widget) {
  // 这里对widget进行深拷贝，不然会出现推入新对象时却发现数组中新增的对象变成了之前修改过的对象的样子
  const widget_ = JSON.parse(JSON.stringify(widget));
  widget_.config.code = "field"+Date.now();
  props.designer.widgetList.push(widget_);
  props.designer.selectedId = props.designer.widgetList.length-1;
}

/**
 * 拖拽开始：记录组件类型
 * @param event
 * @param type
 */
function handleDragStart(event, type) {
  event.dataTransfer.setData('application/x-widget-type', type);
  event.dataTransfer.effectAllowed = 'copy';
}
</script>
<style scoped lang="scss">
.widget-lib {
  padding: 6px 2px;
}
.lib-group + .lib-group {
  margin-top: $spacing-md;
}
.lib-group-title {
  margin-bottom: $spacing-sm;
  font-size: 13px;
  font-weight: bold;
  color: $color-text-secondary;
}
.component-grid {
  display: grid;
  grid-template-columns: minmax(0, 120px);
  justify-content: center;
  row-gap: $spacing-sm;

  > .el-button.component-btn {
    margin-left: 0;
    margin-right: 0;
  }
}
.el-button.component-btn {
  width: 100%;
  height: 36px;
  padding: 0 6px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background-color: var(--el-fill-color-light);
  border: 1px dashed var(--el-border-color-darker);
  border-radius: 4px;
  transition: all 0.2s;

  &:hover,
  &:focus,
  &:active {
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-8);
    border-color: var(--el-color-primary);
  }
}
.el-button {
  font-weight: 400;
}
</style>
