<template>
  <span>{{ displayValue }}</span>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    required: true
  },
  duration: {
    type: Number,
    default: 1500
  },
  decimals: {
    type: Number,
    default: 0
  }
})

const displayValue = ref('0')

const animate = (start, end, duration) => {
  const range = end - start
  const increment = range / (duration / 16) // 60 FPS
  let current = start
  const startTime = performance.now()

  const step = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 使用 easeOutQuart 缓动函数
    const easeOut = 1 - Math.pow(1 - progress, 4)

    current = start + (range * easeOut)

    if (props.decimals > 0) {
      displayValue.value = current.toFixed(props.decimals)
    } else {
      displayValue.value = Math.floor(current).toLocaleString()
    }

    if (progress < 1) {
      requestAnimationFrame(step)
    } else {
      if (props.decimals > 0) {
        displayValue.value = end.toFixed(props.decimals)
      } else {
        displayValue.value = end.toLocaleString()
      }
    }
  }

  requestAnimationFrame(step)
}

onMounted(() => {
  animate(0, props.value, props.duration)
})

watch(() => props.value, (newValue, oldValue) => {
  animate(oldValue || 0, newValue, props.duration)
})
</script>
