<template>
  <el-select
      v-model="innerValue"
      :multiple="multiple"
      :disabled="disabled"
      :placeholder="placeholder"
      :clearable="clearable"
      :size="size"
      :collapse-tags="collapseTags"
      :fit-input-width="fitInputWidth"
      :loading="loading && userList.length === 0"
      :popper-class="popperClass"
      @change="handleChange"
      @visible-change="handleVisibleChange"
  >
    <el-option
        v-for="user in optionList"
        :key="user.id"
        :label="user.name"
        :value="user.id"
    />
    <template #header>
      <el-input
          v-model="query.name"
          placeholder="请输入用户名称搜索"
          clearable
          v-debounce-input:500="handleSearch"
          @clear="handleSearch"
      />
    </template>
  </el-select>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { pageUserListAPI } from '@/api/manage/sys/user'

defineOptions({ name: 'selectUser' })

const props = defineProps({
  // 多选绑 ID 数组；单选绑单个 ID
  modelValue: { type: [Array, Number, String], default: null },
  // 是否多选
  multiple: { type: Boolean, default: false },
  // 整体禁用
  disabled: { type: Boolean, default: false },
  // 占位文案
  placeholder: { type: String, default: '请选择用户' },
  // 是否可清空
  clearable: { type: Boolean, default: true },
  // 透传 el-select size
  size: { type: String, default: '' },
  // 多选时是否折叠 tag
  collapseTags: { type: Boolean, default: false },
  // 下拉宽度跟随输入框
  fitInputWidth: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'update:showValue', 'change'])

const instance = getCurrentInstance()
// 每个实例独立的下拉类名，用于定位滚动容器
const popperClass = `select-user-popper-${instance.uid}`
// 下拉滚动容器
let scrollWrap = null

// 已加载的用户数据
const userList = ref([])
// 已选用户名称缓存，用于回显未加载到的已选用户
const selectedUserMap = ref({})
// 分页与搜索条件
const query = ref({ pageNo: 1, pageSize: 20, name: undefined })
const total = ref(0)
const loading = ref(false)

// el-select 内部值，由 props.modelValue 同步
const innerValue = ref(
    props.multiple
      ? (Array.isArray(props.modelValue) ? [...props.modelValue] : [])
      : props.modelValue
)

// 是否还有未加载的用户
const hasMore = computed(() => userList.value.length < total.value)

// 下拉选项 = 已加载用户 + 未加载到的已选用户
const optionList = computed(() => {
  const list = [...userList.value]
  const existIds = new Set(list.map(user => user.id))
  getSelectedIds(props.modelValue).forEach(id => {
    if (!existIds.has(id) && selectedUserMap.value[id]) {
      list.push({ id, name: selectedUserMap.value[id] })
    }
  })
  return list
})

// 同步外部传入的 modelValue
watch(() => props.modelValue, (val) => {
  if (props.multiple) {
    innerValue.value = Array.isArray(val) ? [...val] : []
  } else {
    innerValue.value = val
  }
  syncSelectedUserNames(val)
}, { deep: true })

onMounted(() => {
  loadUserList()
  syncSelectedUserNames(props.modelValue)
})

onBeforeUnmount(() => {
  unbindScroll()
})

/**
 * 归一化选中值为 ID 数组
 * @param val 选中的用户 ID 或 ID 数组
 * @returns {number[]} ID 数组
 */
function getSelectedIds(val) {
  const ids = Array.isArray(val) ? val : [val]
  return ids.filter(id => id != null && id !== '')
}

/**
 * 分页查询用户列表
 * @param append 是否追加到已有数据之后，滚动加载时为 true
 */
function loadUserList(append = false) {
  if (append && loading.value) {
    return
  }
  loading.value = true
  pageUserListAPI(query.value).then(res => {
    if (res.code !== 200) {
      return
    }
    const rows = res.data.rows || []
    userList.value = append ? [...userList.value, ...rows] : rows
    total.value = res.data.total
    cacheUserNames(userList.value)
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 缓存用户名称，供已选回显与 showValue 使用
 * @param users 用户列表
 */
function cacheUserNames(users) {
  users.forEach(user => {
    selectedUserMap.value[user.id] = user.name
  })
}

/**
 * 补齐已选用户的名称，避免未加载到的已选用户回显为 ID
 * @param val 当前选中的用户 ID 或 ID 数组
 */
function syncSelectedUserNames(val) {
  const missingIds = getSelectedIds(val).filter(id => selectedUserMap.value[id] == null)
  if (missingIds.length === 0) {
    return
  }
  pageUserListAPI({ ids: missingIds.join(','), page: false }).then(res => {
    if (res.code !== 200) {
      return
    }
    cacheUserNames(res.data.rows || [])
  })
}

/**
 * 名称搜索，重置到第一页
 */
function handleSearch() {
  query.value.name = query.value.name || undefined
  query.value.pageNo = 1
  loadUserList()
}

/**
 * 下拉展开时绑定滚动监听，用于滚动到底部加载下一页
 * @param visible 下拉是否展开
 */
function handleVisibleChange(visible) {
  if (!visible) {
    return
  }
  nextTick(() => {
    // 下拉内容由 teleport 渲染，需通过类名定位内部滚动容器
    const wrap = document.querySelector(`.${popperClass} .el-scrollbar__wrap`)
    if (!wrap) {
      return
    }
    unbindScroll()
    scrollWrap = wrap
    wrap.addEventListener('scroll', handleScroll)
  })
}

/**
 * 滚动到底部时加载下一页
 * @param event 滚动事件
 */
function handleScroll(event) {
  if (!hasMore.value || loading.value) {
    return
  }
  const { scrollTop, scrollHeight, clientHeight } = event.target
  if (scrollHeight - scrollTop - clientHeight > 20) {
    return
  }
  query.value.pageNo += 1
  loadUserList(true)
}

/**
 * 解绑下拉滚动监听
 */
function unbindScroll() {
  if (scrollWrap) {
    scrollWrap.removeEventListener('scroll', handleScroll)
    scrollWrap = null
  }
}

/**
 * 根据 ID 数组反查名称数组
 * @param ids 用户 ID 数组
 * @returns {string[]} 名称数组
 */
function getNamesByIds(ids) {
  if (!Array.isArray(ids)) return []
  return ids.map(id => selectedUserMap.value[id]).filter(Boolean)
}

// 选项变化时同步外部 v-model 与 showValue
function handleChange(val) {
  emit('update:modelValue', val)
  if (props.multiple) {
    emit('update:showValue', getNamesByIds(val))
  } else {
    emit('update:showValue', selectedUserMap.value[val] || '')
  }
  emit('change', val)
}

// 对外暴露重新加载用户列表的方法
defineExpose({
  reload() {
    query.value.pageNo = 1
    loadUserList()
  }
})
</script>

<style scoped lang="scss">
</style>
