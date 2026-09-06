<template>
  <div class="error-page-container">
    <div class="error-content">
      <el-result
          icon="warning"
          title="404"
          sub-title="抱歉，您访问的页面不存在或已被移除。"
      />
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
    font-weight: 400;
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
