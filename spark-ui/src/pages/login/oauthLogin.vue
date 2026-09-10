<template>
  <div class="oauth-login">
    <div class="oauth-container">
      <div class="oauth-card">
        <p class="oauth-desc">{{ statusText }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {getSessionAPI} from "@/api/manage/auth/login.js";

const router = useRouter()
const route = useRoute()
const store = useStore()

const statusText = ref('正在处理OAuth授权，请稍候...')

/**
 * 处理OAuth登录
 */
function handleOAuthLogin() {
  const { code, state } = route.query
  if (!code) {
    ElMessage.error('缺少授权码参数')
    statusText.value = '登录失败：缺少授权码'
    setTimeout(() => {
      router.push('/login')
    }, 2000)
    return
  }
  statusText.value = '正在验证授权信息...'
  const loginForm = {
    loginName: code,
    loginType: Number(state),
    loginPlatform: 4
  }
  store.dispatch('user/login', { loginForm }).then(res => {
    if (res.code === 200) {
      getSessionAPI().then(res => {
        statusText.value = '登录成功，正在跳转...'
        ElMessage.success('登录成功')
        router.push({ path: '/' })
      })
    } else {
      statusText.value = '登录失败：' + (res.message || '授权验证失败')
    }
  })
}

onMounted(() => {
  handleOAuthLogin()
})
</script>

<style scoped lang="scss">
.oauth-login {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(160deg, #f7faff 0%, #eaf2fe 55%, #e3edfd 100%);
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
  padding: 60px 80px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(26, 111, 232, 0.12);
}

.oauth-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
</style>
