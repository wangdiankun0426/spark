<template>
  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <line class="flow-line" x1="8.2" y1="7" x2="15.8" y2="7"/>
    <line class="flow-line" x1="7.4" y1="8.8" x2="10.8" y2="15.4"/>
    <line class="flow-line" x1="16.6" y1="8.8" x2="13.2" y2="15.4"/>
    <circle class="node" cx="6" cy="7" r="2.2"/>
    <circle class="node" cx="18" cy="7" r="2.2"/>
    <circle class="node" cx="12" cy="17.5" r="2.2"/>
  </svg>
</template>

<style scoped>
.flow-line {
  stroke-dasharray: 2.6 3;
  animation: dashFlow 1.1s linear infinite;
}

.node {
  transform-box: fill-box;
  transform-origin: center;
  animation: nodePulse 2s ease-in-out infinite;
}

.node:nth-of-type(4) { animation-delay: 0.3s; }
.node:nth-of-type(5) { animation-delay: 0.6s; }

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
