<template>
  <el-form label-width="auto">
    <el-form-item label="字段名">
      <el-input v-model="props.config.code" readonly/>
    </el-form-item>
    <el-form-item label="标题">
      <el-input
          v-model="props.config.label"
          clearable
          type="textarea"
          :rows="2"
      />
    </el-form-item>
    <el-form-item label="表单栅格">
      <el-slider
          v-model="props.config.width"
          :step="4"
          show-stops
          :min="0"
          :max="24"
      />
    </el-form-item>
    <el-form-item label="默认值">
      <el-input v-model="props.config.defaultValue" />
    </el-form-item>
    <el-form-item label="占位内容">
      <el-input v-model="props.config.placeholder" />
    </el-form-item>
    <el-form-item label="是否禁用">
      <el-switch v-model="props.config.disabled" />
    </el-form-item>
    <el-form-item label="是否隐藏">
      <el-switch v-model="props.config.hidden" />
    </el-form-item>
    <el-divider>
      选项
    </el-divider>
    <div v-for="(option, index) in props.config.options">
        <el-input v-model="option.value" placeholder="选项值" style="width: 70px; margin-right: 10px"/>
        <span style="font-size: 20px">:</span>
        <el-input v-model="option.label" placeholder="选项标签" style="width: 150px; margin-left: 10px; margin-right: 10px"/>
        <el-button
            type="text"
            @click="handleDeleteOption(index)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    <el-button
          type="text"
          @click="handleAddOption">
        <el-icon><Plus /></el-icon>&nbsp;添加
      </el-button>
  </el-form>
</template>
<script setup>
defineOptions({
  name: "customRadioSetting"
});
const props = defineProps({
  config: Object,
})

/**
 * 删除选项
 * */
function handleDeleteOption(index) {
  props.config.options.splice(index, 1);
}

/**
 * 添加选项
 */
function handleAddOption() {
  const option = {
    value: "",
    label: "",
  };
  props.config.options.push(option);
}

</script>
