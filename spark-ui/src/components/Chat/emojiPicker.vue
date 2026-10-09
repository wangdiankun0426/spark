<template>
  <div class="emoji-picker">
    <!-- 两个面板常驻 DOM，v-show 切换避免反复销毁重建 -->
    <el-scrollbar class="emoji-picker-body" height="280px" always>
      <div v-show="activeTab === 'emoji'">
        <div
            v-for="(chunk, idx) in emojiChunks"
            :key="idx"
            class="emoji-grid emoji-chunk"
        >
          <span
              v-for="emoji in chunk"
              :key="emoji"
              class="emoji-cell"
              @click="selectEmoji(emoji)"
          >{{ emoji }}</span>
        </div>
      </div>
      <div v-show="activeTab === 'sticker'" class="sticker-grid">
        <span
            v-for="sticker in stickerList"
            :key="sticker"
            class="sticker-cell"
            @click="selectSticker(sticker)"
        >{{ sticker }}</span>
      </div>
    </el-scrollbar>
    <div class="emoji-picker-tabs">
      <span
          class="emoji-tab"
          :class="{ 'emoji-tab-active': activeTab === 'emoji' }"
          @click="activeTab = 'emoji'"
      >Emoji</span>
      <span
          class="emoji-tab"
          :class="{ 'emoji-tab-active': activeTab === 'sticker' }"
          @click="activeTab = 'sticker'"
      >表情包</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { emojiList, stickerList } from '@/components/Chat/emojiData.js'

const emit = defineEmits([
  // 选中 emoji，插入输入框光标处
  'select',
  // 选中贴纸，直接发送一条贴纸消息
  'sticker'
])

const activeTab = ref('emoji')

// 视觉上平铺无分组，渲染上每3行(24个)一块，配合 content-visibility 跳过可视区外块的绘制
const emojiChunks = computed(() => {
  const chunks = []
  const size = 24
  for (let i = 0; i < emojiList.length; i += size) {
    chunks.push(emojiList.slice(i, i + size))
  }
  return chunks
})

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
}

.emoji-picker-tabs {
  display: flex;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-color-light;
  flex-shrink: 0;
}

.emoji-tab {
  padding: 2px $spacing-sm;
  border-radius: $border-radius-sm;
  font-size: 13px;
  color: $color-text-secondary;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $color-primary;
  }
}

.emoji-tab-active {
  color: $color-primary;
  background-color: $color-primary-light;
  font-weight: 600;
}

.emoji-picker-body {
  margin-bottom: $spacing-sm;
  padding-right: $spacing-sm;
}

.emoji-chunk {
  content-visibility: auto;
  contain-intrinsic-size: auto 116px;
  margin-bottom: $spacing-xs;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: $spacing-xs;
}

.emoji-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  font-size: 20px;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;
  user-select: none;

  &:hover {
    background-color: $color-primary-soft;
  }
}

.sticker-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $spacing-sm;
}

.sticker-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  font-size: 40px;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: $transition-fast;
  user-select: none;

  &:hover {
    background-color: $color-primary-soft;
  }
}
</style>
