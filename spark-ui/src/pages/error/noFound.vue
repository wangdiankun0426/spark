<template>
  <div class="error-page-container">
    <div class="error-content">
      <el-result icon="warning" title="404" sub-title="抱歉，您访问的页面不存在或已被移除。">
        <template #extra>
          <div class="action-buttons">
            <el-button type="primary" class="primary-btn" @click="$router.push('/')">返回首页</el-button>
            <el-button class="secondary-btn" @click="$router.back()">返回上一页</el-button>
          </div>
        </template>
      </el-result>

      <div class="background-decor">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
      </div>
    </div>
  </div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
.error-page-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  overflow: hidden;
  position: relative;
}

.error-content {
  z-index: 10;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 40px 60px;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.3);
  animation: slideUp 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

:deep(.el-result) {
  .el-result__title p {
    font-size: 80px;
    font-weight: 800;
    background: linear-gradient(135deg, #1d4ed8 0%, #60a5fa 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 20px;
  }
  .el-result__sub-title p {
    color: #64748b;
    font-size: 16px;
    font-weight: 500;
  }
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 20px;

  .primary-btn {
    padding: 12px 28px;
    height: auto;
    font-weight: 600;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(37, 99, 235, 0.3);
    }
  }

  .secondary-btn {
    padding: 12px 28px;
    height: auto;
    font-weight: 600;
    border-radius: 12px;
    color: #475569;
    border: 1.5px solid #e2e8f0;
    transition: all 0.3s ease;

    &:hover {
      background: #f8fafc;
      border-color: #cbd5e1;
      color: #1e293b;
    }
  }
}

.background-decor {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;

  .circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
  }

  .circle-1 {
    width: 400px;
    height: 400px;
    background: rgba(96, 165, 250, 0.15);
    top: -100px;
    right: -100px;
  }

  .circle-2 {
    width: 300px;
    height: 300px;
    background: rgba(37, 99, 235, 0.1);
    bottom: -50px;
    left: -50px;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
