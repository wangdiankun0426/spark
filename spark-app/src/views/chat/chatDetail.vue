<template>
  <view class="chat-detail-container safe-area-page" :class="themeClass">
    <!-- 顶部标题栏 -->
    <view class="detail-header" :style="navHeaderStyle()">
      <view class="header-left" @click="goBack">
        <up-icon name="arrow-left" size="20" color="#2f7bff"></up-icon>
        <text class="header-title">聊天详情</text>
      </view>
    </view>

    <view class="detail-content">
      <up-cell-group>
        <up-cell
            title="查看聊天内容"
            isLink
            @click="openChatRecord"
        />
      </up-cell-group>
    </view>
  </view>
</template>

<script setup>
import {ref} from 'vue'
import {onLoad} from '@dcloudio/uni-app'
import {navHeaderStyle} from '@/utils/navBarUtil'
import {useTheme} from '@/utils/useTheme'

const {themeClass} = useTheme()

// 会话参数由聊天页带入，跳转聊天记录页时原样透传
const spaceId = ref('')
const targetType = ref('agent')
const targetName = ref('')
const recordTitle = ref('聊天记录')

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
  background-color: $uni-bg-color-dashboard;
}

.detail-header {
  flex-shrink: 0;
  padding: 16px 16px;
  background-color: $spark-color-header-bg;
  border-bottom: 1rpx solid $spark-color-header-border;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-title {
    font-size: 17px;
    font-style: italic;
    font-weight: 700;
    letter-spacing: 1px;
    /* #ifdef MP-WEIXIN */
    color: var(--app-brand);
    /* #endif */
    /* #ifdef H5 */
    background-image: var(--app-title-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    /* #endif */
  }
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding-top: 8px;
}
</style>
