<template>
  <view
    class="user-avatar"
    :class="['avatar-' + type, 'avatar-shape-' + shape]"
    :style="avatarStyle"
  >
    <!-- 用户头像：默认图标占位，图片加载完成后展示 -->
    <template v-if="type === 'user'">
      <view class="avatar-img-wrap">
        <!-- 形状跟随外层容器：方形列表里不再套一个圆形头像 -->
        <up-image
          :src="userAvatarUrl"
          :width="size + 'px'"
          :height="size + 'px'"
          :shape="imageShape"
          :radius="shape === 'rounded' ? 10 : 0"
          :show-loading="false"
          :error-icon="false"
          :custom-style="{opacity: avatarLoaded ? 1 : 0}"
          @load="handleAvatarLoad"
        />
        <!-- 加载完成前覆盖显示文字头像，与设计稿的姓名首字一致 -->
        <view v-if="!avatarLoaded" class="avatar-placeholder">
          <text v-if="initial" class="avatar-initial" :style="{fontSize: initialSize + 'px'}">{{ initial }}</text>
          <up-icon v-else name="account-fill" :size="iconSize" color="#ffffff"></up-icon>
        </view>
      </view>
    </template>

    <!-- 智能体/模型图标头像 -->
    <template v-else-if="type === 'agent'">
      <template v-if="shape === 'rounded' && !showIcon">
        <!-- 智能体列表：圆角矩形 + 文字 -->
        <text class="avatar-text">{{ avatarText }}</text>
      </template>
      <template v-else>
        <!-- 聊天中：品牌色方块 + 图标 -->
        <up-icon name="grid-fill" :size="iconSize" color="#fff"></up-icon>
      </template>
    </template>

    <template v-else-if="type === 'model'">
      <template v-if="shape === 'rounded' && !showIcon">
        <!-- 模型列表：圆角矩形 + 文字 -->
        <text class="avatar-text">{{ avatarText }}</text>
      </template>
      <template v-else>
        <!-- 聊天中：青色方块 + 图标 -->
        <up-icon name="grid-fill" :size="iconSize" color="#fff"></up-icon>
      </template>
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
  },
  // 智能体/模型在聊天里用图标而不是文字，列表里仍用文字
  showIcon: {
    type: Boolean,
    default: false
  }
})

const baseUrl = process.env.BASE_HTTP_API

const userAvatarUrl = computed(() => {
  if (!props.userId) return ''
  return baseUrl + '/sys/user/avatar?id=' + props.userId
})

// 头像图片是否加载完成，未完成前显示默认图标
const avatarLoaded = ref(false)

// 列表复用组件时切换用户，需重新加载
watch(() => props.userId, () => {
  avatarLoaded.value = false
})

const iconSize = computed(() => Math.round(props.size * 0.62))

// uview 的 shape 只识别 circle，圆角矩形传 square 并由 radius 控制圆角
const imageShape = computed(() => props.shape === 'circle' ? 'circle' : 'square')

// 文字头像：统一取姓名首字，与设计稿的「李 / 王 / 陈」单字一致
const initial = computed(() => {
  const name = (props.name || '').trim()
  return name ? name.slice(0, 1) : ''
})
const initialSize = computed(() => Math.max(12, Math.round(props.size * 0.42)))

/**
 * 头像图片加载完成
 */
function handleAvatarLoad() {
  avatarLoaded.value = true
}

const avatarText = computed(() => {
  const name = props.name || (props.type === 'self' ? '我' : '')
  if (!name) return props.type === 'model' ? '模' : '用'
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

  &.avatar-shape-rounded {
    border-radius: 10px;
  }
}

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

.avatar-initial {
  font-weight: 600;
  color: var(--app-fill-tx);
  line-height: 1;
}

.avatar-user {
  background-image: linear-gradient(135deg, #8d99ae 0%, #adb5c0 100%);
}

.avatar-user.avatar-shape-rounded {
  background-image: var(--app-avatar-bg);
}

.avatar-agent {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
  &.avatar-shape-rounded {
    background: linear-gradient(135deg, #0052cc 0%, #1890ff 100%);
  }
}

.avatar-model {
  background: linear-gradient(135deg, #13c2c2 0%, #36cfc9 100%);
}

.avatar-self {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}
</style>