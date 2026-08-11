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
                    @click="addWidget(basicWidget)"
                >
                  {{basicWidget.name}}
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
                    @click="addWidget(basicWidget)"
                >
                  {{basicWidget.name}}
                </el-button>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
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
</script>
<style scoped>
:deep(.el-card__header) {
  padding: 10px !important;
}
:deep(.el-collapse-item__header) {
  margin-left: 8px;
  font-style: italic;
  font-weight: bold;
}
.component-btn {
  width: 100px;
  margin-bottom: 10px;
}
</style>
