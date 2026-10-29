<template>
  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <path class="flow-line" d="M7 10.6c3.8 0 5.2-4 9-4"/>
    <path class="flow-line flow-line-2" d="M7 13.4c3.8 0 5.2 4 9 4"/>
    <circle class="node" cx="5" cy="12" r="2.2"/>
    <circle class="node node-2" cx="18" cy="6.6" r="2.2"/>
    <circle class="node node-3" cx="18" cy="17.4" r="2.2"/>
  </svg>
</template>

<style scoped>
.flow-line {
  stroke-dasharray: 2.6 3;
  animation: dashFlow 1.1s linear infinite;
}

.flow-line-2 {
  animation-delay: 0.55s;
}

.node {
  transform-box: fill-box;
  transform-origin: center;
  animation: nodePulse 2s ease-in-out infinite;
}

.node-2 { animation-delay: 0.3s; }
.node-3 { animation-delay: 0.6s; }

@keyframes dashFlow {
  to {
    stroke-dashoffset: -11.2;
  }
}

@keyframes nodePulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.45;
  }
}
</style>
