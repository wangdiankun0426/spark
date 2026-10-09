<template>
  <view class="emoji-picker">
    <!-- 两个面板常驻 DOM 切换，不用 v-show：小程序端 v-show 编译为 hidden 属性，会被面板自身的 display:flex 覆盖而失效 -->
    <scroll-view scroll-y="true" class="emoji-picker-body">
      <view class="emoji-grid" :class="{ 'emoji-panel-hidden': activeTab !== 'emoji' }">
        <text
            v-for="emoji in emojiList"
            :key="emoji"
            class="emoji-cell"
            @click="selectEmoji(emoji)"
        >{{ emoji }}</text>
      </view>
      <view class="sticker-grid" :class="{ 'emoji-panel-hidden': activeTab !== 'sticker' }">
        <text
            v-for="sticker in stickerList"
            :key="sticker"
            class="sticker-cell"
            @click="selectSticker(sticker)"
        >{{ sticker }}</text>
      </view>
    </scroll-view>
    <view class="emoji-picker-tabs">
      <text
          class="emoji-tab"
          :class="{ 'emoji-tab-active': activeTab === 'emoji' }"
          @click="activeTab = 'emoji'"
      >Emoji</text>
      <text
          class="emoji-tab"
          :class="{ 'emoji-tab-active': activeTab === 'sticker' }"
          @click="activeTab = 'sticker'"
      >表情包</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue"
import { emojiList, stickerList } from "@/views/chat/emojiData.js"

const emit = defineEmits([
  // 选中 emoji，插入输入框
  'select',
  // 选中贴纸，直接发送一条贴纸消息
  'sticker'
])

const activeTab = ref('emoji')

/**
 * 选中 emoji，上抛给父组件插入输入框
 * @param emoji 表情字符
 */
function selectEmoji(emoji) {
  emit('select', emoji)
}

/**
 * 选中贴纸，上抛给父组件直接发送
 * @param sticker 贴纸表情
 */
function selectSticker(sticker) {
  emit('sticker', sticker)
}
</script>

<style scoped lang="scss">
.emoji-picker {
  display: flex;
  flex-direction: column;
  margin-top: 16rpx;
  border-top: 1px solid #e9e9e9;
}

.emoji-picker-body {
  height: 460rpx;
}

.emoji-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 12rpx 8rpx;
}

.emoji-cell {
  width: 12.5%;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}

.sticker-grid {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx 12rpx;
}

.sticker-cell {
  width: 20%;
  height: 132rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 76rpx;
}

.emoji-picker-tabs {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 20rpx;
  border-top: 1px solid #e9e9e9;
}

.emoji-tab {
  padding: 6rpx 20rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #666;
}

.emoji-tab-active {
  color: #0052cc;
  background-color: #e8f0fd;
  font-weight: 600;
}

.emoji-grid.emoji-panel-hidden,
.sticker-grid.emoji-panel-hidden {
  display: none;
}
</style>
