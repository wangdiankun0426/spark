<template>
  <el-avatar :shape="shape" :size="size" :src="avatarUrl">
    <slot>
      <el-icon><UserFilled /></el-icon>
    </slot>
  </el-avatar>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'

defineOptions({ name: 'userAvatar' })

const props = defineProps({
  // 用户 ID
  userId: { type: [Number, String], default: undefined },
  // 头像尺寸（px）
  size: { type: [Number, String], default: 40 },
  // 形状：circle / square
  shape: { type: String, default: 'circle' }
})

// 头像时间戳，刷新时更新以避免浏览器缓存
const timestamp = ref(Date.now())

const baseUrl = process.env.BASE_HTTP_API

const avatarUrl = computed(() => {
  if (props.userId === undefined || props.userId === null || props.userId === '') {
    return undefined
  }
  return baseUrl + '/sys/user/avatar?id=' + props.userId + '&time=' + timestamp.value
})

/**
 * 刷新头像（清除浏览器缓存重新加载）
 */
function refresh() {
  timestamp.value = Date.now()
}

defineExpose({ refresh })
</script>
