<template>
  <div class="widget-lib">
    <!-- 组件检索（模糊匹配：按字符顺序命中即可，如"选图"匹配"选择知识图谱"） -->
    <el-input
        v-model="keyword"
        class="lib-search"
        placeholder="搜索组件"
        clearable
        :prefix-icon="Search"
    />
    <div class="lib-group" v-if="filteredBasic.length">
      <div class="lib-group-title">基础组件</div>
      <div class="component-grid">
        <el-button
            v-for="(basicWidget) in filteredBasic"
            class="component-btn"
            :title="basicWidget.name"
            draggable="true"
            @click="addWidget(basicWidget)"
            @dragstart="handleDragStart($event, basicWidget.type)"
        >
          <span class="widget-ico">
            <el-icon><component :is="widgetIconMap[basicWidget.type] || 'EditPen'" /></el-icon>
          </span>
          <span class="widget-name">{{ basicWidget.name }}</span>
        </el-button>
      </div>
    </div>
    <div class="lib-group" v-if="filteredSys.length">
      <div class="lib-group-title">系统组件</div>
      <div class="component-grid">
        <el-button
            v-for="(basicWidget) in filteredSys"
            class="component-btn"
            :title="basicWidget.name"
            draggable="true"
            @click="addWidget(basicWidget)"
            @dragstart="handleDragStart($event, basicWidget.type)"
        >
          <span class="widget-ico">
            <el-icon><component :is="widgetIconMap[basicWidget.type] || 'EditPen'" /></el-icon>
          </span>
          <span class="widget-name">{{ basicWidget.name }}</span>
        </el-button>
      </div>
    </div>
    <div class="lib-empty" v-if="!filteredBasic.length && !filteredSys.length">未找到匹配的组件</div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue';
import { Search } from '@element-plus/icons-vue';
import {basicWidgetList, sysWidgetList} from "./widgetList.js"

const props = defineProps({
  designer: Object,
})

/** 检索关键字 */
const keyword = ref('');

/**
 * 模糊匹配：关键字按字符顺序出现在名称中即命中
 * @param name 组件名
 * @param kw 关键字
 */
function fuzzyMatch(name, kw) {
  if (!kw) return true;
  const n = name.toLowerCase();
  const k = kw.toLowerCase();
  let i = 0;
  for (const ch of k) {
    i = n.indexOf(ch, i);
    if (i === -1) return false;
    i++;
  }
  return true;
}

/** 过滤后的组件列表 */
const filteredBasic = computed(() => basicWidgetList.filter(w => fuzzyMatch(w.name, keyword.value)));
const filteredSys = computed(() => sysWidgetList.filter(w => fuzzyMatch(w.name, keyword.value)));

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
  padding: 10px 10px;
}
.lib-group + .lib-group {
  margin-top: 8px;
}
// 分组标题：左侧渐变色条锚点
.lib-group-title {
  margin: 8px 0 6px;
  padding-left: 8px;
  position: relative;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.05em;
  color: $app-text-placeholder;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 10px;
    border-radius: 2px;
    background: linear-gradient(180deg, $el-color-primary, $el-color-primary-light-5);
  }
}
.component-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
}
// 组件项：无边框轻量样式，悬停浮出淡蓝卡片（图标单色线性，悬停变主蓝）
.el-button.component-btn {
  width: 100%;
  height: 30px;
  margin: 0;
  padding: 0 6px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 5px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  font-weight: 400;
  transition: $transition-fast;

  &:hover,
  &:focus,
  &:active {
    color: $color-text-primary;
    background-color: $app-primary-soft;
    border-color: rgba(47, 123, 255, 0.45);
    box-shadow: 0 3px 10px rgba(47, 123, 255, 0.12);
    transform: translateY(-1px);
  }
}
.widget-ico {
  flex: none;
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: $color-text-secondary;
  font-size: 15px;
  transition: $transition-fast;

  .el-icon {
    color: inherit;
  }
}
.el-button.component-btn:hover .widget-ico {
  color: $el-color-primary;
}
.widget-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
  font-size: 11.5px;
  line-height: 1.2;
}
// 检索框
.lib-search {
  margin-bottom: 8px;
}
// 检索无结果
.lib-empty {
  padding: 16px 0;
  text-align: center;
  font-size: 12px;
  color: $app-text-placeholder;
}
</style>
