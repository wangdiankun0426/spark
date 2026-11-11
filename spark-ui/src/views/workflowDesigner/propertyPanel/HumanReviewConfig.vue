<template>
  <div class="human-review-config">
    <el-divider>人工审核配置</el-divider>
    <el-form-item label="审核提示">
      <var-field
          clearable
          v-model="form.config.prompt"
          textarea
          :rows="4"
          placeholder="输入审核提示信息"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="审核人">
      <el-select
          v-model="form.config.reviewerIds"
          multiple
          placeholder="选择审核人"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="u in userList" :key="u.id" :label="u.name" :value="u.id" />
      </el-select>
    </el-form-item>
    <el-divider>输出数据</el-divider>
    <div
        v-for="b in outputVar"
        :key="b.value"
        class="var-row"
    >
      <span class="var-label">{{ b.label }}</span>
      <code class="var-code" :title="b.value" v-text="b.value"></code>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pageUserListAPI } from '@/api/manage/sys/user.js'
import VarField from '../var/VarField.vue'
import { getNodeOutputs } from '../nodes/index.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const userList = ref([])
onMounted(async () => {
  // 字典接口失败时保持空候选，不阻断面板挂载
  try {
    const res = await pageUserListAPI({ page: false })
    if (res && res.code === 200 && res.data?.rows) {
      userList.value = res.data.rows
    }
  } catch (e) {
    userList.value = []
  }
})

const outputVar = computed(() => getNodeOutputs(props.form))
</script>

<style scoped lang="scss">
.var-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  .var-label {
    flex-shrink: 0;
    font-size: 13px;
    color: $color-text-primary;
  }
  .var-code {
    flex: 1;
    min-width: 0;
    font-size: 12px;
    color: $app-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
