<template>
  <image class="icon-img" :src="src" :style="iconStyle" mode="aspectFit"/>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  size: { type: Number, default: 44 }
})

const src = 'data:image/svg+xml;base64,PHN2ZyB0PSIxNzg0MzcwOTE4MjAxIiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjE0MDk2IiB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCI+PHBhdGggZD0iTTQ4Ny43MzEyIDc1NC41MzQ0YTIxOC4yNjU2IDIxOC4yNjU2IDAgMSAxLTIxOC4yNjU2LTIxOC4yNjU2aDIxOC4yNjU2djIxOC4yNjU2eiIgZmlsbD0iIzQyODVGNCIgcC1pZD0iMTQwOTciPjwvcGF0aD48cGF0aCBkPSJNNTM2LjI2ODggNzU0LjUzNDRhMjE4LjI2NTYgMjE4LjI2NTYgMCAxIDAgMjE4LjI2NTYtMjE4LjI2NTZoLTIxOC4yNjU2djIxOC4yNjU2eiIgZmlsbD0iIzMzQTg1MiIgcC1pZD0iMTQwOTgiPjwvcGF0aD48cGF0aCBkPSJNNDg3LjczMTIgMjY5LjQ2NTZhMjE4LjI2NTYgMjE4LjI2NTYgMCAxIDAtMjE4LjI2NTYgMjE4LjI2NTZoMjE4LjI2NTZWMjY5LjQ2NTZ6IiBmaWxsPSIjRUE0MzM2IiBwLWlkPSIxNDA5OSI+PC9wYXRoPjxwYXRoIGQ9Ik03NTQuNTM0NCA1MS4yYTIxOC4yNjU2IDIxOC4yNjU2IDAgMSAxIDAgNDM2LjUzMTJoLTIxOC4yNjU2VjI2OS40NjU2QTIxOC4yNjU2IDIxOC4yNjU2IDAgMCAxIDc1NC41MzQ0IDUxLjJ6IiBmaWxsPSIjRkFCRDA1IiBwLWlkPSIxNDEwMCI+PC9wYXRoPjwvc3ZnPg=='
const iconStyle = computed(() => ({
  width: props.size + "px",
  height: props.size + "px",
  flexShrink: 0,
  display: "block"
}))
</script>
