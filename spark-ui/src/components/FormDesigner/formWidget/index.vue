<template>
  <div v-if="props.designer.widgetList.length === 0">
    <center style="font-size: 13px; color: #8c939d"> 请从左侧组件库中选择一个组件 </center>
  </div>
  <div v-else class="form-widget-box">
    <el-form
        :label-position="props.designer.formConfig.position"
        :size="props.designer.formConfig.size"
    >
      <el-row :gutter="24">
        <el-col :span="widget.config.width"
              v-for="(widget, index) in props.designer.widgetList"
              :key="index"
              :class="handleSelectedStyle(index)"
              @click="handleSelectWidget(index)">
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
const props = defineProps({
  designer: Object
})

/**
 * 获取自定义组件名称
 * @param widget
 * @returns {string}
 */
function getFieldName(widget) {
  return "custom-"+widget.type;
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
</script>
<style scoped>
.form-widget-box {
  padding: 0 20px;
  height: 88vh;
  overflow-y : auto;
  overflow-x: hidden;
}
.selected_component {
  border: 1px dashed #0052cc;
  cursor: pointer;
  position: relative;
}
.no_select_component {
  cursor: pointer;
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
