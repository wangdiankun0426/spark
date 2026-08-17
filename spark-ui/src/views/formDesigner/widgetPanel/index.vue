<template>
  <el-tabs v-model="activeName">
    <el-tab-pane name="componentLib">
        <template #label>
          <span>组件库</span>
        </template>
        <el-collapse v-model="activeNames">
          <el-collapse-item name="1" title="基础组件">
            <el-row>
              <el-col
                  :span="12"
                  v-for="(basicWidget) in basicWidgetList"
              >
                <el-button
                    class="component-btn"
                    draggable="true"
                    @click="addWidget(basicWidget)"
                    @dragstart="handleDragStart($event, basicWidget.type)"
                >
                  <el-icon><component :is="widgetIconMap[basicWidget.type] || 'EditPen'" /></el-icon>
                  <span>{{ basicWidget.name }}</span>
                </el-button>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item name="2" title="系统组件">
            <el-row>
              <el-col
                  :span="12"
                  v-for="(basicWidget) in sysWidgetList"
              >
                <el-button
                    class="component-btn"
                    draggable="true"
                    @click="addWidget(basicWidget)"
                    @dragstart="handleDragStart($event, basicWidget.type)"
                >
                  <el-icon><component :is="widgetIconMap[basicWidget.type] || 'EditPen'" /></el-icon>
                  <span>{{ basicWidget.name }}</span>
                </el-button>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>
    <el-tab-pane name="examples">
      <template #label>
        <span>示例</span>
      </template>
    </el-tab-pane>
  </el-tabs>
</template>
<script setup>
import {ref} from 'vue';
import {basicWidgetList, sysWidgetList} from "./widgetList.js"

const props = defineProps({
  designer: Object,
})
const activeName = ref('componentLib');
const activeNames = ['1', '2'];

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
:deep(.el-card__header) {
  padding: 10px !important;
}
:deep(.el-collapse-item__header) {
  margin-left: 8px;
  font-style: italic;
  font-weight: bold;
}
.el-button.component-btn {
  width: 100px;
  height: 38px;
  margin-bottom: 10px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background-color: #fafafa;
  border: 1px dashed #c0c4cc;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover,
  &:focus,
  &:active {
    color: #409eff;
    background-color: #ecf5ff;
    border-color: #409eff;
  }
}
.el-button {
  font-weight: 400;
}
</style>
