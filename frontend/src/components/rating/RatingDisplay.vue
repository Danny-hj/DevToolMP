<template>
  <div v-if="statistics" class="rating-display">
    <div class="rating-summary">
      <div class="average-rating">
        <span class="score">{{ (statistics.averageScore || 0).toFixed(1) }}</span>
        <el-rate
          v-model="averageRating"
          disabled
          show-score
          :score-template="'{value}'"
        />
        <span class="total">{{ statistics.totalRatings || 0 }} 条评价</span>
      </div>
    </div>

    <div class="rating-distribution">
      <div v-for="i in 5" :key="i" class="distribution-item">
        <span class="label">{{ 6 - i }} 星</span>
        <el-progress
          :percentage="getPercentage(6 - i)"
          :stroke-width="8"
          :show-text="false"
          :color="'#409eff'"
        />
        <span class="count">{{ getCount(6 - i) }}</span>
      </div>
    </div>
  </div>
  <div v-else class="rating-display">
    <el-skeleton :rows="3" animated />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  statistics: {
    type: Object,
    default: null
  }
})

const averageRating = computed(() => {
  if (!props.statistics) return 0
  return Math.round(props.statistics.averageScore || 0)
})

const getCount = (score) => {
  if (!props.statistics) return 0
  switch (score) {
    case 5: return props.statistics.fiveStarCount || 0
    case 4: return props.statistics.fourStarCount || 0
    case 3: return props.statistics.threeStarCount || 0
    case 2: return props.statistics.twoStarCount || 0
    case 1: return props.statistics.oneStarCount || 0
    default: return 0
  }
}

const getPercentage = (score) => {
  if (!props.statistics) return 0
  const total = props.statistics.totalRatings || 0
  if (total === 0) return 0
  return (getCount(score) / total) * 100
}
</script>

<style scoped>
.rating-display {
  background-color: #2a2a2a;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.rating-summary {
  text-align: center;
  margin-bottom: 24px;
}

.average-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.score {
  font-size: 48px;
  font-weight: bold;
  color: #409eff;
}

.total {
  color: #909399;
  font-size: 14px;
}

.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  width: 40px;
  text-align: right;
  color: #b0b0b0;
  font-size: 14px;
}

.distribution-item .el-progress {
  flex: 1;
}

.count {
  width: 60px;
  color: #909399;
  font-size: 14px;
  text-align: right;
}
</style>
