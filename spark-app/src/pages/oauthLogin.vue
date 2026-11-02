<template>
  <view class="oauth-login safe-area-page" :class="themeClass">
    <view class="oauth-container">
      <view class="oauth-card">
        <text class="oauth-desc">{{ statusText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import {onLoad} from "@dcloudio/uni-app";
import {useTheme} from "@/utils/useTheme";

const store = useStore()
const {themeClass} = useTheme()
const statusText = ref('正在处理OAuth授权，请稍候...')
const code = ref('')
const state = ref('')

/**
 * 解析OAuth授权的state参数
 * state 格式为「登录类型.租户id」，如 4.103，其中 4 表示企微oauth2登录
 * @param {string} stateValue state参数值
 * @returns {{loginType: number, tenantId: number|undefined}} 解析结果
 */
function parseOAuthState(stateValue) {
  const [loginType, tenantId] = String(stateValue || '').split('.')
  return {
    loginType: Number(loginType),
    tenantId: tenantId ? Number(tenantId) : undefined
  }
}

/**
 * 处理OAuth登录
 */
function handleOAuthLogin() {
  if (!code.value) {
    statusText.value = '登录失败：缺少授权码'
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login' })
    }, 2000)
    return
  }
  statusText.value = '正在验证授权信息...'
  const oauthState = parseOAuthState(state.value)
  const loginForm = {
    loginName: code.value,
    loginType: oauthState.loginType,
    loginPlatform: 5,
    tenantId: oauthState.tenantId
  }
  store.dispatch('user/login', { loginForm }).then(res => {
    if (res.code === 200) {
      statusText.value = '登录成功，正在跳转...'
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/home/index' })
      }, 500)
    } else {
      statusText.value = '登录失败：' + (res.message || '授权验证失败')
    }
  })
}

/**
 * 页面加载时获取参数
 */
onLoad((options) => {
  code.value = options.code || ''
  state.value = options.state || ''
  handleOAuthLogin()
})
</script>

<style scoped lang="scss">
.oauth-login {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: var(--app-gradient-login);
}

.oauth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.oauth-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 80rpx;
  background-color: var(--app-bg-card);
  border-radius: 24rpx;
  border: 1rpx solid var(--app-border-light);
  box-shadow: var(--app-shadow-card);
}

.oauth-desc {
  font-size: 28rpx;
  color: var(--app-text-muted);
}
</style>
