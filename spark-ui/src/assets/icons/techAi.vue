<template>
  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <path class="spark-main" d="M12 3.2l1.8 4.4 4.4 1.8-4.4 1.8L12 15.6l-1.8-4.4-4.4-1.8 4.4-1.8z"/>
    <path class="spark-sub" d="M18.5 14.5l.8 2 2 .8-2 .8-.8 2-.8-2-2-.8 2-.8z"/>
  </svg>
</template>

<style scoped>
.spark-main {
  transform-box: fill-box;
  transform-origin: center;
  animation: sparkBreath 2.2s ease-in-out infinite;
}

.spark-sub {
  transform-box: fill-box;
  transform-origin: center;
  animation: sparkTwinkle 1.8s ease-in-out infinite;
}

@keyframes sparkBreath {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.12);
    opacity: 0.75;
  }
}

@keyframes sparkTwinkle {
  0%, 100% {
    transform: scale(0.8);
    opacity: 0.35;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
}
</style>
