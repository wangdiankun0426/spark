<template>
  <!-- 主题图标：亮色显示月亮（切到暗色），暗色显示太阳（切回亮色），纯 CSS 绘制以兼容小程序 -->
  <view class="theme-glyph" :class="[dark ? 'theme-glyph--sun' : 'theme-glyph--moon', 'theme-glyph--' + size]"/>
</template>
<script setup>
defineProps({
  // 当前是否暗色皮肤，决定画太阳还是月亮
  dark: {
    type: Boolean,
    default: false
  },
  // 尺寸档位，颜色由父级 color 控制
  size: {
    type: String,
    default: 'normal'
  }
});
</script>
<style scoped lang="scss">
.theme-glyph {
  display: block;
  box-sizing: border-box;
  flex: none;
}

.theme-glyph--small {
  font-size: 16px;
}

.theme-glyph--normal {
  font-size: 22px;
}

.theme-glyph--large {
  font-size: 30px;
}

/* 月亮：空心圆靠内阴影切出月牙，几何尺寸全部用 em 跟随 font-size */
.theme-glyph--moon {
  width: 1em;
  height: 1em;
  border-radius: 50%;
  box-shadow: inset -0.34em -0.16em 0 0 currentColor;
  transform: rotate(-24deg);
}

/* 太阳：实心小圆 + 八向投影做光芒 */
.theme-glyph--sun {
  width: 0.56em;
  height: 0.56em;
  margin: 0.22em;
  border-radius: 50%;
  background-color: currentColor;
  box-shadow:
      0 -0.46em 0 -0.17em currentColor,
      0 0.46em 0 -0.17em currentColor,
      -0.46em 0 0 -0.17em currentColor,
      0.46em 0 0 -0.17em currentColor,
      0.33em 0.33em 0 -0.17em currentColor,
      -0.33em 0.33em 0 -0.17em currentColor,
      0.33em -0.33em 0 -0.17em currentColor,
      -0.33em -0.33em 0 -0.17em currentColor;
}
</style>
