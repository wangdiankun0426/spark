<template>
  <div class="agreement-container safe-area-page">
    <view class="agreement-header" :style="navHeaderStyle()">
      <view class="header-left" @click="onClickBack">
        <up-icon name="arrow-left" size="20" color="#2f7bff"></up-icon>
        <text class="header-title">{{ doc.title }}</text>
      </view>
    </view>

    <scroll-view scroll-y class="agreement-body">
      <view class="doc-meta">
        <text class="doc-name">{{ doc.title }}</text>
        <text class="doc-desc">更新日期：{{ doc.updateDate }}　版本：{{ doc.version }}</text>
      </view>
      <view class="doc-section" v-for="(section, index) in doc.sections" :key="index">
        <text class="section-heading">{{ section.heading }}</text>
        <text
            class="section-paragraph"
            v-for="(paragraph, pIndex) in section.paragraphs"
            :key="pIndex"
        >{{ paragraph }}</text>
      </view>
      <view class="doc-footer">
        <text class="footer-text">本文件更新日期：{{ doc.updateDate }}，自发布之日起生效。</text>
      </view>
    </scroll-view>
  </div>
</template>
<script setup>
import {computed, ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {navHeaderStyle} from '@/utils/navBarUtil';
import {agreementMap} from '@/utils/agreementDoc';

const docType = ref('service');

const doc = computed(() => agreementMap[docType.value] || agreementMap.service);

onLoad((options) => {
  if (options && (options.type === 'service' || options.type === 'privacy')) {
    docType.value = options.type;
  }
});

/**
 * 返回上一页
 */
function onClickBack() {
  uni.navigateBack();
}
</script>
<style scoped lang="scss">
.agreement-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #ffffff;
}

.agreement-header {
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
    color: #2f7bff;
    /* #endif */
    /* #ifdef H5 */
    background-image: linear-gradient(92deg, #2563eb 0%, #3d8bff 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    /* #endif */
  }
}

.agreement-body {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 32rpx 32rpx 0;
}

.doc-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0 8rpx;

  .doc-name {
    font-size: 36rpx;
    font-weight: 700;
    color: $spark-color-text;
    letter-spacing: 2rpx;
  }

  .doc-desc {
    margin-top: 16rpx;
    font-size: 22rpx;
    color: $spark-color-text-secondary;
  }
}

.doc-section {
  margin-top: 36rpx;

  .section-heading {
    display: block;
    margin-bottom: 16rpx;
    font-size: 30rpx;
    font-weight: 700;
    color: $spark-color-text;
    line-height: 1.6;
  }

  .section-paragraph {
    display: block;
    margin-bottom: 14rpx;
    font-size: 26rpx;
    color: $spark-color-text;
    line-height: 1.8;
    text-align: justify;
  }
}

.doc-footer {
  padding: 24rpx 0;
  padding-bottom: calc(48rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));

  .footer-text {
    display: block;
    font-size: 24rpx;
    color: $spark-color-text-secondary;
    text-align: center;
  }
}
</style>
