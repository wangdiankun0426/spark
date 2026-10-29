<template>
  <svg viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
    <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
    <circle class="dot dot-1" cx="9" cy="11.5" r="1.1" fill="currentColor" stroke="none"/>
    <circle class="dot dot-2" cx="12" cy="11.5" r="1.1" fill="currentColor" stroke="none"/>
    <circle class="dot dot-3" cx="15" cy="11.5" r="1.1" fill="currentColor" stroke="none"/>
  </svg>
</template>

<style scoped>
.dot {
  transform-box: fill-box;
  transform-origin: center;
  animation: dotJump 1.2s ease-in-out infinite;
}

.dot-2 { animation-delay: 0.2s; }
.dot-3 { animation-delay: 0.4s; }

@keyframes dotJump {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.55;
  }
  30% {
    transform: translateY(-2.2px);
    opacity: 1;
  }
}
</style>
