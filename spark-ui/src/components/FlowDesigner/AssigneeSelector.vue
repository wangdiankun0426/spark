<template>
  <div class="assignee-selector">
    <el-form-item label="审批人类型">
      <el-select v-model="node.assigneeType" placeholder="请选择处理人类型" @change="handleTypeChange">
        <el-option
            v-for="item in assigneeTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
    </el-form-item>

    <!-- 指定用户 -->
    <el-form-item label="指定用户" v-if="node.assigneeType === '3'">
      <select-user
          v-model="assigneeArray"
          multiple
          clearable
          @update:showValue="handleShowValue"
      />
    </el-form-item>

    <!-- 指定部门 -->
    <el-form-item label="指定部门" v-if="node.assigneeType === '4'">
      <select-dept
          v-model="assigneeArray"
          multiple
          clearable
          @update:showValue="handleShowValue"
      />
    </el-form-item>

    <!-- 指定角色 -->
    <el-form-item label="指定角色" v-if="node.assigneeType === '5'">
      <select-role
          v-model="assigneeArray"
          multiple
          clearable
          @update:showValue="handleShowValue"
      />
    </el-form-item>

    <!-- 表单数据（选择表单中的用户/部门/角色字段） -->
    <el-form-item label="表单字段" v-if="node.assigneeType === '6'">
      <el-select
          v-model="node.assignee"
          placeholder="请选择表单字段"
          clearable
          filterable
          @change="handleFieldChange"
      >
        <el-option
            v-for="item in filteredFieldOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
    </el-form-item>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import SelectUser from '@/components/SelectUser'
import SelectDept from '@/components/SelectDept'
import SelectRole from '@/components/SelectRole'

defineOptions({ name: 'AssigneeSelector' })

const props = defineProps({
  node: { type: Object, required: true },
  fieldOptions: { type: Array, default: () => [] }
})

const assigneeTypeOptions = [
  { label: '流程发起人', value: '1' },
  { label: '系统自动通过', value: '2' },
  { label: '指定用户', value: '3' },
  { label: '指定部门', value: '4' },
  { label: '指定角色', value: '5' },
  { label: '表单数据', value: '6' }
]

/**
 * 将逗号分隔的 assignee 转为数组，供 SelectUser/SelectDept/SelectRole 使用
 */
const assigneeArray = computed({
  get() {
    if (!props.node.assignee) return []
    return String(props.node.assignee).split(',').map(id => Number(id)).filter(Boolean)
  },
  set(val) {
    props.node.assignee = (val || []).join(',')
  }
})

/**
 * 过滤出可选字段（仅 select-user、select-dept、select-role 类型）
 */
const filteredFieldOptions = computed(() => {
  return props.fieldOptions.filter(item => {
    const type = item.type || ''
    return ['select-user','select-dept','select-role'].includes(type)
  })
})

/** 切换审批人类型时，清空 assignee 和 assigneeLabel */
function handleTypeChange(val) {
  props.node.assignee = ''
  props.node.assigneeLabel = ''
  if (val === '1' || val === '2') {
    const find = assigneeTypeOptions.find(item => item.value === val)
    props.node.assigneeLabel = find ? find.label : ''
  }
}

/** 选择用户/部门/角色后，更新 label 为名称列表 */
function handleShowValue(names) {
  if (Array.isArray(names)) {
    props.node.assigneeLabel = names.join('、')
  } else {
    props.node.assigneeLabel = names || ''
  }
}

/** 选择表单字段后，更新 label 为字段标题 */
function handleFieldChange(val) {
  if (val) {
    const find = props.fieldOptions.find(item => item.value === val)
    props.node.assigneeLabel = find ? find.label : val
  } else {
    props.node.assigneeLabel = ''
  }
}

// 初始化 label：如果已有 assignee 值但无 label，尝试回填
watch(() => props.node.assignee, (val) => {
  if (val && !props.node.assigneeLabel) {
    const type = props.node.assigneeType
    if (type === '6') {
      const find = props.fieldOptions.find(item => item.value === val)
      if (find) props.node.assigneeLabel = find.label
    }
  }
}, { immediate: false })
</script>

<style scoped lang="scss">
.assignee-selector {
  width: 100%;
}
</style>