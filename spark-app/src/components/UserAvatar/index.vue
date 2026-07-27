<template>
  <view
    class="user-avatar"
    :class="['avatar-' + type, 'avatar-shape-' + shape]"
    :style="avatarStyle"
  >
    <!-- 用户头像：图片加载，失败时文字回退 -->
    <template v-if="type === 'user'">
      <up-image
        :src="userAvatarUrl"
        :width="size + 'px'"
        :height="size + 'px'"
        shape="circle"
        :show-loading="false"
        :error-icon="false"
      >
        <template #error>
          <text class="avatar-text">{{ avatarText }}</text>
        </template>
      </up-image>
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
import { computed } from 'vue'

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

// 用户头像背景
.avatar-user {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
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