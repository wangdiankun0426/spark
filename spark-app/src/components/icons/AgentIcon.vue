<template>
  <image class="icon-img" src="/static/icons/agent.png" :style="iconStyle" mode="aspectFit"/>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  size: { type: Number, default: 44 }
})

const iconStyle = computed(() => ({
  width: props.size + "px",
  height: props.size + "px",
  flexShrink: 0,
  display: "block"
}))
</script>
