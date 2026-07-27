<template>
  <el-avatar shape="circle" :size="size" :src="avatarUrl" />
</template>

<script setup>
import { ref, computed } from 'vue'

defineOptions({ name: 'userAvatar' })

const props = defineProps({
  // 用户 ID
  userId: { type: [Number, String], default: undefined },
  // 头像尺寸（px）
  size: { type: [Number, String], default: 40 }
})

// 头像时间戳，刷新时更新以避免浏览器缓存
const timestamp = ref(Date.now())

const baseUrl = process.env.BASE_HTTP_API

const avatarUrl = computed(() => {
  if (props.userId === undefined || props.userId === null || props.userId === '') {
    return undefined
  }
  return baseUrl + '/system/user/avatar?id=' + props.userId + '&time=' + timestamp.value
})

/**
 * 刷新头像（清除浏览器缓存重新加载）
 */
function refresh() {
  timestamp.value = Date.now()
}

defineExpose({ refresh })
</script>
