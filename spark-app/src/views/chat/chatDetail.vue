<template>
  <view class="chat-detail-container safe-area-page" :class="themeClass">
    <!-- 顶部标题栏：与其他二级页统一「返回 + 居中标题」 -->
    <view class="page-header" :style="navHeaderStyle()">
      <view class="page-header-back" @click="goBack">
        <up-icon name="arrow-left" size="19" :color="backIconColor"></up-icon>
      </view>
      <text class="page-header-title">聊天详情</text>
      <view class="page-header-extra"></view>
    </view>

    <scroll-view class="detail-body" scroll-y>
      <!-- 会话对象：头像 + 名称 + 类型，说明这份记录属于谁 -->
      <view class="profile-card">
        <user-avatar :type="avatarType" shape="rounded" show-icon :size="46"/>
        <view class="profile-main">
          <text class="profile-name">{{ targetName || '未命名会话' }}</text>
          <text class="profile-sub">{{ typeLabel }}</text>
        </view>
      </view>

      <!-- 操作卡：沿用设置页的行样式 -->
      <view class="section-card">
        <view class="section-head">
          <view class="section-bar"></view>
          <text class="section-title">会话设置</text>
        </view>
        <view class="link-row" @click="openChatRecord">
          <view class="row-icon">
            <up-icon name="chat-fill" size="15" :color="rowIconColor"/>
          </view>
          <text class="row-title">查看聊天内容</text>
          <text class="row-value">历史记录</text>
          <up-icon name="arrow-right" size="13" :color="chevronColor"></up-icon>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import {ref, computed} from 'vue'
import {onLoad} from '@dcloudio/uni-app'
import {navHeaderStyle} from '@/utils/navBarUtil'
import {useTheme} from '@/utils/useTheme'
import {getHeaderIconColor, getChevronColor} from '@/utils/themeUtil'
import UserAvatar from '@/components/UserAvatar/index.vue'

const {isDark, themeClass} = useTheme()

// 会话参数由聊天页带入，跳转聊天记录页时原样透传
const spaceId = ref('')
const targetType = ref('agent')
const targetName = ref('')
const recordTitle = ref('聊天记录')

const backIconColor = computed(() => getHeaderIconColor(isDark.value))
const chevronColor = computed(() => getChevronColor(isDark.value))
const rowIconColor = computed(() => getHeaderIconColor(isDark.value))

// 头像类型与文案：同事 / 智能体 / 模型
const avatarType = computed(() => (targetType.value === 'user' ? 'user' : targetType.value))
const typeLabel = computed(() => {
  if (targetType.value === 'user') {
    return '同事会话'
  }
  return targetType.value === 'model' ? '模型会话' : '智能体会话'
})

onLoad((options) => {
  if (!options) {
    return
  }
  spaceId.value = options.spaceId || ''
  targetType.value = options.targetType || 'agent'
  targetName.value = options.targetName ? decodeURIComponent(options.targetName) : ''
  recordTitle.value = options.title ? decodeURIComponent(options.title) : '聊天记录'
})

function goBack() {
  uni.navigateBack()
}

/**
 * 查看聊天内容
 */
function openChatRecord() {
  if (!spaceId.value) {
    return
  }
  uni.navigateTo({
    url: '/views/chat/chatMsgRecord?spaceId=' + spaceId.value +
        '&targetType=' + targetType.value +
        '&targetName=' + encodeURIComponent(targetName.value) +
        '&title=' + encodeURIComponent(recordTitle.value)
  })
}
</script>

<style scoped lang="scss">
.chat-detail-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--app-bg-page);
}

.detail-body {
  flex: 1;
  min-height: 0;
  padding: 12px 14px 20px;
  box-sizing: border-box;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  margin-bottom: 10px;
  border-radius: 16px;
  background-color: var(--app-bg-card);
  box-shadow: var(--app-shadow-card);
}

.profile-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-sub {
  font-size: 11.5px;
  color: var(--app-text-tertiary);
}

.section-card {
  padding: 13px 14px 4px;
  border-radius: 16px;
  background-color: var(--app-bg-card);
  box-shadow: var(--app-shadow-card);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-bottom: 6px;
}

.section-bar {
  width: 3px;
  height: 12px;
  border-radius: 2px;
  background-image: var(--app-gradient-fill);
}

.section-title {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.link-row {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 0;
  border-top: 1px solid var(--app-divide);

  &:active {
    opacity: 0.7;
  }
}

.row-icon {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--app-primary-soft);
}

.row-title {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  color: var(--app-text-primary);
}

.row-value {
  font-size: 12px;
  color: var(--app-text-tertiary);
}
</style>
