<template>
  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <rect x="5" y="3" width="14" height="18" rx="2"/>
    <line class="flow-line" x1="8.5" y1="8.5" x2="15.5" y2="8.5"/>
    <line class="flow-line" x1="8.5" y1="12.5" x2="15.5" y2="12.5"/>
    <path class="draw-check" d="M9.5 16.5l1.8 1.8 3.5-3.8"/>
  </svg>
</template>

<style scoped>
.flow-line {
  stroke-dasharray: 3 3;
  animation: dashFlow 1.4s linear infinite;
}

.draw-check {
  stroke-dasharray: 9;
  stroke-dashoffset: 9;
  animation: checkDraw 2.4s ease-in-out infinite;
}

@keyframes dashFlow {
  to {
    stroke-dashoffset: -12;
  }
}

@keyframes checkDraw {
  0%, 55% {
    stroke-dashoffset: 9;
    opacity: 0.4;
  }
  75% {
    stroke-dashoffset: 0;
    opacity: 1;
  }
  95%, 100% {
    stroke-dashoffset: 0;
    opacity: 0.2;
  }
}
</style>
