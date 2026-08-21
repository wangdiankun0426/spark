<template>
  <div class="notify-config">
    <el-divider content-position="left">通知配置</el-divider>
    <el-form-item label="标题">
      <el-input
          clearable
          v-model="form.config.titleCode"
          placeholder="支持变量，详见变量帮助"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="内容">
      <el-input
          v-model="form.config.contentCode"
          type="textarea"
          :rows="10"
          placeholder="支持变量，详见变量帮助"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="接收人">
      <el-input
          clearable
          v-model="form.config.userCode"
          placeholder="支持变量，详见变量帮助"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="所属对象">
      <el-input
          clearable
          v-model="form.config.refCode"
          placeholder="支持变量，详见变量帮助"
          @change="emit('update')"
      />
    </el-form-item>
  </div>
</template>

<script setup>
const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])
</script>
