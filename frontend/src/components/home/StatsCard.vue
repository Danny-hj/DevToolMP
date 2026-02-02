<template>
  <el-card
    class="stats-card"
    :body-style="{ padding: '0' }"
  >
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
          <AnimatedNumber
            :value="value"
            :duration="duration"
          />
          <span
            v-if="unit"
            class="stat-unit"
          >{{ unit }}</span>
        </div>
        <div class="stat-label">
          {{ label }}
        </div>
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
  overflow: hidden;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 0 20px rgba(0, 255, 157, 0.2);
    transform: translateY(-4px);
  }

  :deep(.el-card__body) {
    padding: $spacing-lg $spacing-xl;
  }
}

.stats-content {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  min-width: 0;
  padding: $spacing-xs 0;
}

.stat-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $border-radius-large;
  background: linear-gradient(135deg, $background-color-light, $background-color-lighter);
  color: $primary-color;
  font-size: 26px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  padding-right: $spacing-xs;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 0;
  margin-bottom: $spacing-xs;
  font-size: 28px;
  font-weight: 700;
  color: $text-color-primary;
  line-height: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: keep-all;

  .animated-number {
    max-width: 100%;
  }
}

.stat-unit {
  font-size: 16px;
  color: $text-color-secondary;
  font-weight: 600;
  margin-left: $spacing-xs;
  flex-shrink: 0;
}

.stat-label {
  font-size: $font-size-small;
  color: $text-color-secondary;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .stats-content {
    gap: $spacing-sm;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-unit {
    font-size: 14px;
  }

  .stat-label {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  :deep(.el-card__body) {
    padding: $spacing-md;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-unit {
    font-size: 12px;
  }
}
</style>
