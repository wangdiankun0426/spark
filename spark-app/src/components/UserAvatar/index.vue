<template>
  <view
    class="user-avatar"
    :class="['avatar-' + type, 'avatar-shape-' + shape]"
    :style="avatarStyle"
  >
    <!-- 用户头像：默认图标占位，图片加载完成后展示 -->
    <template v-if="type === 'user'">
      <view class="avatar-img-wrap">
        <up-image
          :src="userAvatarUrl"
          :width="size + 'px'"
          :height="size + 'px'"
          shape="circle"
          :show-loading="false"
          :error-icon="false"
          @load="handleAvatarLoad"
        />
        <!-- 加载完成前覆盖显示默认头像图标 -->
        <view v-if="!avatarLoaded" class="avatar-placeholder">
          <up-icon name="account-fill" :size="iconSize" color="#ffffff"></up-icon>
        </view>
      </view>
    </template>

    <!-- 智能体/模型图标头像 -->
    <template v-else-if="type === 'agent'">
      <template v-if="shape === 'rounded'">
        <!-- 智能体列表：圆角矩形 + 文字 -->
        <text class="avatar-text">{{ avatarText }}</text>
      </template>
      <template v-else>
        <!-- 聊天中：紫色圆形 + 图标 -->
        <up-icon name="grid-fill" size="18" color="#fff"></up-icon>
      </template>
    </template>

    <template v-else-if="type === 'model'">
      <up-icon name="cpu" size="18" color="#fff"></up-icon>
    </template>

    <!-- 自己发送的消息：绿色圆形 + "我" -->
    <template v-else-if="type === 'self'">
      <text class="avatar-text">{{ avatarText }}</text>
    </template>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'user',
    validator: (val) => ['user', 'agent', 'model', 'self'].includes(val)
  },
  userId: {
    type: [Number, String],
    default: null
  },
  name: {
    type: String,
    default: ''
  },
  size: {
    type: Number,
    default: 48
  },
  shape: {
    type: String,
    default: 'circle',
    validator: (val) => ['circle', 'rounded'].includes(val)
  }
})

const baseUrl = process.env.BASE_HTTP_API

const userAvatarUrl = computed(() => {
  if (!props.userId) return ''
  return baseUrl + '/system/user/avatar?id=' + props.userId
})

// 头像图片是否加载完成，未完成前显示默认图标
const avatarLoaded = ref(false)

// 列表复用组件时切换用户，需重新加载
watch(() => props.userId, () => {
  avatarLoaded.value = false
})

const iconSize = computed(() => Math.round(props.size * 0.62))

/**
 * 头像图片加载完成
 */
function handleAvatarLoad() {
  avatarLoaded.value = true
}

const avatarText = computed(() => {
  const name = props.name || (props.type === 'self' ? '我' : '')
  if (!name) return '用'
  return name.substring(0, 2)
})

const avatarStyle = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px'
}))
</script>

<style scoped lang="scss">
.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;

  // 圆角矩形形状
  &.avatar-shape-rounded {
    border-radius: 10px;
  }
}

// 文字回退
.avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

// 头像图片容器：占位图标覆盖在图片上层
.avatar-img-wrap {
  position: relative;
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 用户头像背景（中性灰渐变，作为默认图标底色）
.avatar-user {
  background: linear-gradient(135deg, #8d99ae 0%, #adb5c0 100%);
}

// 智能体图标背景（紫色）
.avatar-agent {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
  &.avatar-shape-rounded {
    background: linear-gradient(135deg, #0052cc 0%, #1890ff 100%);
  }
}

// 模型图标背景（青色）
.avatar-model {
  background: linear-gradient(135deg, #13c2c2 0%, #36cfc9 100%);
}

// 自己消息背景（绿色）
.avatar-self {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}
</style>