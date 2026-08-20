<template>
  <el-form label-width="auto">
    <el-form-item label="字段名">
      <el-input v-model="props.config.code" readonly/>
    </el-form-item>
    <el-form-item label="标题">
      <el-input
          v-model="props.config.label"
          clearable
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
    <el-form-item label="占位内容">
      <el-input v-model="props.config.placeholder" />
    </el-form-item>
    <el-form-item label="是否必填">
      <el-switch v-model="props.config.required" />
    </el-form-item>
    <el-form-item label="是否只读">
      <el-switch v-model="props.config.readonly" />
    </el-form-item>
    <el-form-item label="是否禁用">
      <el-switch v-model="props.config.disabled" />
    </el-form-item>
    <el-form-item label="是否隐藏">
      <el-switch v-model="props.config.hidden" />
    </el-form-item>
  </el-form>
</template>
<script setup>
defineOptions({
  name: "customTextareaSetting"
});
const props = defineProps({
  config: Object,
})

</script>
