<template>
  <view class="chat-detail-container safe-area-page" :class="themeClass">
    <PageHeader title="聊天详情" @back="goBack"/>

    <scroll-view class="detail-body" scroll-y>
      <!-- 操作卡：沿用设置页的行样式 -->
      <view class="section-card">
        <view class="section-head">
          <SectionTitle>会话设置</SectionTitle>
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
import {useTheme} from '@/utils/useTheme'
import {getHeaderIconColor, getChevronColor} from '@/utils/themeUtil'
import PageHeader from '@/components/PageHeader/index.vue'
import SectionTitle from '@/components/SectionTitle/index.vue'

const {isDark, themeClass} = useTheme()

// 会话参数由聊天页带入，跳转聊天记录页时原样透传
const spaceId = ref('')
const targetType = ref('agent')
const targetName = ref('')
const recordTitle = ref('聊天记录')

const chevronColor = computed(() => getChevronColor(isDark.value))
const rowIconColor = computed(() => getHeaderIconColor(isDark.value))

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
  background-color: $app-bg-page;
}

.detail-body {
  flex: 1;
  min-height: 0;
  padding: 12px 14px 20px;
  box-sizing: border-box;
}

.section-card {
  padding: 13px 14px 4px;
  border-radius: 16px;
  background-color: $app-bg-card;
  box-shadow: $app-shadow-card;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 7px;
  padding-bottom: 6px;
}

.link-row {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 0;
  border-top: 1px solid $app-divide;

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
  background-color: $app-primary-soft;
}

.row-title {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  color: $app-text-primary;
}

.row-value {
  font-size: 12px;
  color: $app-text-tertiary;
}
</style>
