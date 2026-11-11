<template>
  <div class="notify-config">
    <el-divider>通知配置</el-divider>
    <el-form-item label="标题">
      <var-field
          clearable
          v-model="form.config.titleCode"
          placeholder="支持变量，点击 {x} 插入"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="内容">
      <var-field
          clearable
          v-model="form.config.contentCode"
          textarea
          :rows="10"
          placeholder="支持变量，点击 {x} 插入"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="接收人">
      <var-field
          clearable
          v-model="form.config.userCode"
          placeholder="支持变量，点击 {x} 插入"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="所属对象">
      <var-field
          clearable
          v-model="form.config.refCode"
          placeholder="支持变量，点击 {x} 插入"
          @change="emit('update')"
      />
    </el-form-item>
  </div>
</template>

<script setup>
import VarField from '../var/VarField.vue'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])
</script>
