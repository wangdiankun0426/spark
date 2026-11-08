<template>
  <div class="err-page" :class="{ 'is-warn': isForbidden }">
    <div class="err-stage">
      <div class="err-art" aria-hidden="true">
        <!-- 无权限：路径撞上防护环 -->
        <svg
          v-if="isForbidden"
          class="err-art-svg"
          viewBox="0 0 560 560"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id="err403Lock" x1="238" y1="248" x2="324" y2="340" gradientUnits="userSpaceOnUse">
              <stop class="err-stop-a" offset="0" />
              <stop class="err-stop-b" offset="1" />
            </linearGradient>
            <linearGradient id="err403Path" x1="60" y1="474" x2="130" y2="270" gradientUnits="userSpaceOnUse">
              <stop class="err-stop-a" offset="0" stop-opacity=".34" />
              <stop class="err-stop-b" offset="1" />
            </linearGradient>
            <radialGradient id="err403Glow" cx="50%" cy="50%" r="50%">
              <stop class="err-glow-a" offset="0" />
              <stop class="err-glow-b" offset="1" />
            </radialGradient>
            <pattern id="err403Dot" width="24" height="24" patternUnits="userSpaceOnUse">
              <circle class="err-dot" cx="1.4" cy="1.4" r="1.4" />
            </pattern>
            <radialGradient id="err403MaskGrad" cx="50%" cy="50%" r="50%">
              <stop offset="0" stop-color="#ffffff" stop-opacity=".85" />
              <stop offset=".66" stop-color="#ffffff" stop-opacity=".28" />
              <stop offset="1" stop-color="#ffffff" stop-opacity="0" />
            </radialGradient>
            <mask id="err403Mask">
              <rect width="560" height="560" fill="url(#err403MaskGrad)" />
            </mask>
          </defs>

          <circle cx="280" cy="280" r="250" fill="url(#err403Glow)" />
          <g mask="url(#err403Mask)">
            <rect width="560" height="560" fill="url(#err403Dot)" />
          </g>

          <circle cx="280" cy="280" r="158" class="err-ring-shield" />
          <circle cx="280" cy="280" r="158" class="err-ring-shield-line" />
          <g class="err-spin">
            <circle cx="280" cy="280" r="196" class="err-ring-dash" />
          </g>

          <path class="err-lock-shackle" stroke="url(#err403Lock)" d="M250 250v-14a30 30 0 0 1 60 0v14" />
          <rect x="232" y="248" width="96" height="86" rx="19" fill="url(#err403Lock)" />
          <path
            class="err-lock-sheen"
            d="M232 267c0-10.5 8.5-19 19-19h58c10.5 0 19 8.5 19 19v13c-32 9-64 9-96 0v-13Z"
          />
          <circle cx="280" cy="283" r="9" class="err-keyhole" />
          <path class="err-keyhole" d="M275 289h10l3.4 17h-16.8L275 289Z" />

          <path
            class="err-path-blocked"
            stroke="url(#err403Path)"
            d="M40 474C130 468 118 336 122 284"
          />
          <circle cx="122" cy="282" r="5.5" class="err-pulse-dot" />
          <g transform="translate(122 282)">
            <circle r="8" class="err-pulse-ring" />
            <circle r="8" class="err-pulse-ring err-delay-1" />
            <circle r="8" class="err-pulse-ring err-delay-2" />
          </g>

          <circle cx="196" cy="150" r="1.8" class="err-frag" />
          <circle cx="170" cy="366" r="2.2" class="err-frag" />
        </svg>

        <!-- 页面不存在：断开的流程线 -->
        <svg
          v-else
          class="err-art-svg"
          viewBox="0 0 560 560"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            <linearGradient id="err404Path" x1="76" y1="404" x2="300" y2="200" gradientUnits="userSpaceOnUse">
              <stop class="err-stop-a" offset="0" />
              <stop class="err-stop-b" offset="1" />
            </linearGradient>
            <radialGradient id="err404Glow" cx="50%" cy="50%" r="50%">
              <stop class="err-glow-a" offset="0" />
              <stop class="err-glow-b" offset="1" />
            </radialGradient>
            <pattern id="err404Dot" width="24" height="24" patternUnits="userSpaceOnUse">
              <circle class="err-dot" cx="1.4" cy="1.4" r="1.4" />
            </pattern>
            <radialGradient id="err404MaskGrad" cx="50%" cy="50%" r="50%">
              <stop offset="0" stop-color="#ffffff" stop-opacity=".85" />
              <stop offset=".66" stop-color="#ffffff" stop-opacity=".28" />
              <stop offset="1" stop-color="#ffffff" stop-opacity="0" />
            </radialGradient>
            <mask id="err404Mask">
              <rect width="560" height="560" fill="url(#err404MaskGrad)" />
            </mask>
          </defs>

          <circle cx="280" cy="280" r="248" fill="url(#err404Glow)" />
          <g mask="url(#err404Mask)">
            <rect width="560" height="560" fill="url(#err404Dot)" />
          </g>

          <g class="err-spin">
            <circle cx="280" cy="280" r="200" class="err-ring-dash" />
          </g>
          <circle cx="280" cy="280" r="150" class="err-ring-solid" />

          <path class="err-path-solid" stroke="url(#err404Path)" d="M74 448C152 444 180 374 246 332" />
          <path class="err-path-dash" d="M312 296C376 258 392 168 472 136" />

          <path class="err-path-gap err-flow" d="M257 325L301 301" />
          <circle cx="246" cy="332" r="7.5" class="err-dot-open" />
          <circle cx="312" cy="296" r="7.5" class="err-dot-open" />

          <g transform="translate(312 220) rotate(-13)">
            <g class="err-float">
              <rect x="-35" y="-44" width="70" height="88" rx="10" class="err-card" />
              <path class="err-card-line" d="M-19 -20H1" />
              <path class="err-card-line" d="M-19 -6H17" />
              <path class="err-card-line" d="M-19 8H-3" />
              <circle cx="18" cy="24" r="5.5" class="err-card-dot" />
            </g>
          </g>

          <g transform="translate(396 158)">
            <g class="err-badge-float">
              <circle r="16" fill="url(#err404Path)" />
              <text class="err-badge-text" y="5" text-anchor="middle">?</text>
            </g>
          </g>

          <g class="err-mark">
            <path d="M74 438v20M64 448h20" />
          </g>
          <g class="err-mark">
            <path d="M472 126v20M462 136h20" />
          </g>

          <circle cx="340" cy="272" r="2.4" class="err-frag" />
          <circle cx="210" cy="282" r="2" class="err-frag" />
          <circle cx="372" cy="250" r="1.8" class="err-frag" />
        </svg>
      </div>

      <div class="err-copy">
        <span class="err-eyebrow">Error <em>{{ code }}</em></span>
        <h1 class="err-title">
          {{ titleMain }}<em>{{ titleEm }}</em>{{ titleAfter }}
        </h1>
        <p class="err-desc">{{ desc }}</p>
        <div class="err-meta">
          <b>错误代码 {{ code }}</b>
          <span class="err-sep"></span>
          <span v-if="requestId">请求 ID <code>{{ requestId }}</code></span>
          <span v-else-if="metaTag">需要权限 <em class="err-tag">{{ metaTag }}</em></span>
          <template v-if="metaHint">
            <span class="err-sep"></span>
            <span>{{ metaHint }}</span>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 错误码，同时决定插画与强调色：403 走琥珀，其余走品牌蓝
  code: { type: String, default: '404' },
  titleMain: { type: String, default: '' },
  titleEm: { type: String, default: '' },
  titleAfter: { type: String, default: '' },
  desc: { type: String, default: '' },
  // 二选一：404 用请求 ID，403 用所需权限名
  requestId: { type: String, default: '' },
  metaTag: { type: String, default: '' },
  metaHint: { type: String, default: '' }
});

const isForbidden = computed(() => props.code === '403');
</script>

<style lang="scss" scoped>
.err-page {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 32px 24px;
  background: $error-page-bg;
  overflow: hidden;
}

.err-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 720px;
}

.err-art {
  width: 440px;
  max-width: 64vw;
  aspect-ratio: 1 / 1;
  margin-bottom: 24px;
}

.err-art-svg {
  display: block;
  width: 100%;
  height: 100%;
  overflow: visible;
}

.err-copy {
  max-width: 560px;
  text-align: center;
}

.err-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  border-radius: 999px;
  background: $error-brand-soft;
  color: $app-primary;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;

  em {
    font-style: normal;
    letter-spacing: 0.08em;
    opacity: 0.7;
  }
}

.err-title {
  margin: 20px 0 14px;
  color: $error-ink;
  font-size: 42px;
  font-weight: 700;
  line-height: 1.24;
  letter-spacing: -0.6px;

  em {
    padding: 0 0.04em;
    font-style: normal;
    white-space: nowrap;
    background-image: linear-gradient(to top, $error-mark 0 0.26em, $error-mark-end 0.26em);
    background-repeat: no-repeat;
  }
}

.err-desc {
  max-width: 500px;
  margin: 0 auto;
  color: $error-ink-2;
  font-size: 15px;
  line-height: 1.9;
}

.err-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 26px;
  color: $error-ink-3;
  font-size: 12.5px;

  b {
    color: $error-ink-2;
    font-weight: 600;
  }

  code {
    padding: 2px 6px;
    border-radius: 5px;
    background: $error-line-2;
    color: $error-ink-2;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-size: 11.5px;
  }
}

.err-sep {
  width: 1px;
  height: 11px;
  background: $error-line;
}

.err-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 5px;
  background: $error-warn-soft;
  color: $error-warn-ink;
  font-style: normal;
  font-weight: 600;
}

/* 无权限配色：仅强调色切换，其余沿用品牌蓝 */
.err-page.is-warn {
  .err-eyebrow {
    background: $error-warn-soft;
    color: $error-warn-ink;
  }

  .err-title em {
    background-image: linear-gradient(to top, $error-warn-mark 0 0.26em, $error-warn-mark-end 0.26em);
  }
}

/* ============ 插画 ============ */
.err-stop-a {
  stop-color: $error-art-1;
}

.err-stop-b {
  stop-color: $error-art-2;
}

.err-glow-a {
  stop-color: $error-art-glow;
}

.err-glow-b {
  stop-color: $error-art-glow-end;
}

.err-dot {
  fill: $error-art-dot;
}

.err-ring-dash {
  fill: none;
  stroke: $error-art-line;
  stroke-width: 1.6;
  stroke-dasharray: 2 9;
  stroke-linecap: round;
}

.err-ring-solid {
  fill: none;
  stroke: $error-art-line-2;
  stroke-width: 1.4;
}

.err-ring-shield {
  fill: none;
  stroke: $error-art-shield;
  stroke-width: 22;
}

.err-ring-shield-line {
  fill: none;
  stroke: $error-art-line;
  stroke-width: 1.4;
}

.err-path-solid {
  fill: none;
  stroke-width: 4.5;
  stroke-linecap: round;
}

.err-path-dash {
  fill: none;
  stroke: $error-art-line-strong;
  stroke-width: 3;
  stroke-dasharray: 7 9;
  stroke-linecap: round;
}

.err-path-gap {
  fill: none;
  stroke: $error-art-line-strong;
  stroke-width: 2;
  stroke-dasharray: 3 7;
  stroke-linecap: round;
}

.err-path-blocked {
  fill: none;
  stroke-width: 4.5;
  stroke-linecap: round;
}

.err-dot-open {
  fill: $error-art-card;
  stroke: $error-art-1;
  stroke-width: 2.5;
}

.err-card {
  fill: $error-art-card;
  stroke: $error-art-line-strong;
  stroke-width: 1.7;
  stroke-dasharray: 6 6;
  filter: drop-shadow($error-art-shadow);
}

.err-card-line {
  stroke: $error-art-line-strong;
  stroke-width: 2;
  stroke-linecap: round;
  opacity: 0.75;
}

.err-card-dot,
.err-frag {
  fill: $error-art-dot;
}

.err-badge-text {
  fill: $color-text-white;
  font-size: 15px;
  font-weight: 700;
}

.err-mark {
  stroke: $error-art-dot;
  stroke-width: 1.2;
  stroke-linecap: round;
}

.err-lock-shackle {
  fill: none;
  stroke-width: 13;
  stroke-linecap: round;
}

.err-lock-sheen {
  fill: $color-text-white;
  opacity: 0.13;
}

.err-keyhole {
  fill: $app-bg-page;
}

.err-pulse-dot {
  fill: $error-warn;
}

.err-pulse-ring {
  fill: none;
  stroke: $error-warn;
  stroke-width: 1.8;
  opacity: 0;
}

/* ============ 动效 ============ */
.err-spin {
  transform-origin: 280px 280px;
  animation: err-spin 90s linear infinite;
}

.err-float {
  animation: err-float 6.5s ease-in-out infinite;
}

.err-badge-float {
  animation: err-float-small 7.5s ease-in-out infinite;
}

.err-flow {
  animation: err-flow 9s linear infinite;
}

.err-pulse-ring {
  animation: err-pulse 2.8s ease-out infinite;
}

.err-delay-1 {
  animation-delay: 0.9s;
}

.err-delay-2 {
  animation-delay: 1.8s;
}

.err-copy > * {
  animation: err-rise 0.62s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.err-copy > *:nth-child(2) {
  animation-delay: 0.06s;
}

.err-copy > *:nth-child(3) {
  animation-delay: 0.12s;
}

.err-copy > *:nth-child(4) {
  animation-delay: 0.18s;
}

@keyframes err-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes err-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8px);
  }
}

@keyframes err-float-small {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-7px);
  }
}

@keyframes err-flow {
  to {
    stroke-dashoffset: -100;
  }
}

@keyframes err-pulse {
  0% {
    r: 8;
    opacity: 0.55;
  }

  100% {
    r: 46;
    opacity: 0;
  }
}

@keyframes err-rise {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: none;
  }
}

@media (max-height: 720px) {
  .err-art {
    width: 340px;
    margin-bottom: 12px;
  }

  .err-title {
    margin: 14px 0 10px;
    font-size: 32px;
  }

  .err-meta {
    margin-top: 18px;
  }
}

@media (max-width: 768px) {
  .err-art {
    width: 300px;
    margin-bottom: 16px;
  }

  .err-title {
    font-size: 30px;
  }

  .err-desc {
    font-size: 13.5px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .err-spin,
  .err-float,
  .err-badge-float,
  .err-flow,
  .err-pulse-ring,
  .err-copy > * {
    animation: none;
  }
}
</style>
