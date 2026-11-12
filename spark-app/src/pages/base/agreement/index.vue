<template>
  <view class="agreement-container safe-area-page" :class="themeClass">
    <PageHeader :title="doc.title" @back="onClickBack"/>

    <scroll-view scroll-y class="agreement-body">
      <view class="doc-meta">
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
  </view>
</template>
<script setup>
import {computed, ref} from 'vue';
import {onLoad} from '@dcloudio/uni-app';
import {useTheme} from '@/utils/useTheme';
import PageHeader from '@/components/PageHeader/index.vue';
import {agreementMap} from '@/utils/agreementDoc';

const {themeClass} = useTheme();

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
  background-color: $app-page-base;
}

.agreement-body {
  flex: 1;
  /* 小程序里 scroll-view 不认 flex:1，必须补 height:0 才会被撑满 */
  height: 0;
  min-height: 0;
  box-sizing: border-box;
  padding: 32rpx 32rpx 0;
}

.doc-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10rpx 0 8rpx;

  .doc-name {
    font-size: 36rpx;
    font-weight: 700;
    color: $app-text-primary;
    letter-spacing: 2rpx;
  }

  .doc-desc {
    margin-top: 16rpx;
    font-size: 22rpx;
    color: $app-text-secondary;
  }
}

.doc-section {
  margin-top: 36rpx;

  .section-heading {
    display: block;
    margin-bottom: 16rpx;
    font-size: 30rpx;
    font-weight: 700;
    color: $app-text-primary;
    line-height: 1.6;
  }

  .section-paragraph {
    display: block;
    margin-bottom: 14rpx;
    font-size: 26rpx;
    color: $app-text-primary;
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
    color: $app-text-secondary;
    text-align: center;
  }
}
</style>
