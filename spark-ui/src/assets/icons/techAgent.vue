<template>
  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <g class="robot-body">
      <line x1="12" y1="3.2" x2="12" y2="5.6"/>
      <circle class="antenna" cx="12" cy="2.6" r="0.9" fill="currentColor" stroke="none"/>
      <rect x="4" y="5.6" width="16" height="13" rx="3.5"/>
      <circle class="eye" cx="9.4" cy="12" r="1.2" fill="currentColor" stroke="none"/>
      <circle class="eye eye-2" cx="14.6" cy="12" r="1.2" fill="currentColor" stroke="none"/>
      <line x1="9.2" y1="15.8" x2="14.8" y2="15.8"/>
    </g>
  </svg>
</template>

<style scoped>
.robot-body {
  transform-box: fill-box;
  transform-origin: center;
  animation: robotFloat 2.6s ease-in-out infinite;
}

.eye {
  transform-box: fill-box;
  transform-origin: center;
  animation: eyeBlink 2.6s ease-in-out infinite;
}

.eye-2 {
  animation-delay: 0.15s;
}

.antenna {
  animation: antennaGlow 1.6s ease-in-out infinite;
}

@keyframes robotFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-1.4px);
  }
}

@keyframes eyeBlink {
  0%, 88%, 100% {
    opacity: 1;
  }
  92% {
    opacity: 0.2;
  }
}

@keyframes antennaGlow {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}
</style>
