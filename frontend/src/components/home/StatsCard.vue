<template>
  <el-card class="stats-card" :body-style="{ padding: '0' }">
    <div class="stats-content">
      <div class="stat-icon">
        <slot name="icon">
          <el-icon :size="32">
            <component :is="icon" />
          </el-icon>
        </slot>
      </div>
      <div class="stat-info">
        <div class="stat-value">
          <AnimatedNumber :value="value" :duration="duration" />
          <span v-if="unit" class="stat-unit">{{ unit }}</span>
        </div>
        <div class="stat-label">{{ label }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import AnimatedNumber from './AnimatedNumber.vue'

defineProps({
  icon: {
    type: [String, Object],
    default: null
  },
  value: {
    type: Number,
    required: true
  },
  label: {
    type: String,
    required: true
  },
  unit: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 1500
  }
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.stats-card {
  height: 100%;
  border: 1px solid $border-color-base;
  background: $background-color-base;
  transition: $transition-base;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 0 20px rgba(0, 255, 157, 0.2);
    transform: translateY(-4px);
  }

  :deep(.el-card__body) {
    padding: $spacing-xl;
  }
}

.stats-content {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.stat-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $border-radius-large;
  background: linear-gradient(135deg, $background-color-light, $background-color-lighter);
  color: $primary-color;
  font-size: 32px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
  font-size: $font-size-hero;
  font-weight: 700;
  color: $text-color-primary;
  line-height: 1;
}

.stat-unit {
  font-size: $font-size-large;
  color: $text-color-secondary;
  font-weight: 600;
}

.stat-label {
  font-size: $font-size-base;
  color: $text-color-secondary;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .stat-value {
    font-size: 32px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }
}
</style>
