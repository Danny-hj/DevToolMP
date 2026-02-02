<template>
  <div class="rating-list">
    <div
      v-if="loading && ratings.length === 0"
      class="loading-container"
    >
      <el-skeleton
        :rows="3"
        animated
      />
    </div>

    <div
      v-else-if="ratings.length === 0"
      class="empty-state"
    >
      <el-empty description="暂无评价，快来发表第一条评价吧！" />
    </div>

    <div
      v-else
      class="rating-items"
    >
      <div
        v-for="rating in ratings"
        :key="rating.id"
        class="rating-item"
      >
        <!-- 用户信息 -->
        <div class="rating-header">
          <div class="user-info">
            <el-avatar
              :size="40"
              class="user-avatar"
            >
              {{ rating.username ? rating.username.charAt(0).toUpperCase() : 'A' }}
            </el-avatar>
            <div class="user-details">
              <div class="username">
                {{ rating.username || '匿名用户' }}
              </div>
              <div class="rating-date">
                {{ formatDate(rating.createdAt) }}
              </div>
            </div>
          </div>
          <div class="rating-score">
            <el-rate
              v-model="rating.score"
              disabled
              show-score
              :score-template="rating.score.toFixed(1)"
            />
          </div>
        </div>

        <!-- 评价内容 -->
        <div class="rating-content">
          <p class="comment-text">
            {{ rating.comment }}
          </p>
        </div>

        <!-- 评价操作 -->
        <div class="rating-actions">
          <div class="action-item">
            <el-icon class="action-icon">
              <ChatLineRound />
            </el-icon>
            <span>{{ rating.likes || 0 }} 有用</span>
          </div>
          <el-button
            text
            size="small"
            @click="handleReply(rating)"
          >
            <el-icon><Promotion /></el-icon>
            回复
          </el-button>
        </div>

        <!-- 回复列表 -->
        <div
          v-if="rating.replies && rating.replies.length > 0"
          class="replies-section"
        >
          <div
            v-for="reply in rating.replies"
            :key="reply.id"
            class="reply-item"
          >
            <div class="reply-header">
              <span class="reply-username">{{ reply.username || '匿名用户' }}</span>
              <span
                v-if="reply.replyToUsername"
                class="reply-to"
              >
                回复 {{ reply.replyToUsername }}
              </span>
              <span class="reply-date">{{ formatDate(reply.createdAt) }}</span>
            </div>
            <div class="reply-content">
              {{ reply.content }}
            </div>
          </div>
        </div>

        <!-- 回复输入框 -->
        <div
          v-if="replyingTo === rating.id"
          class="reply-input-section"
        >
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="2"
            placeholder="输入回复内容..."
            maxlength="200"
            show-word-limit
          />
          <div class="reply-actions">
            <el-button
              size="small"
              @click="cancelReply"
            >
              取消
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="submitReply(rating.id)"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div
        v-if="total > pageSize"
        class="pagination-container"
      >
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound, Promotion } from '@element-plus/icons-vue'
import { useRatingStore } from '@/stores/rating'

const props = defineProps({
  toolId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['rating-updated'])

const ratingStore = useRatingStore()

const ratings = computed(() => ratingStore.ratings)
const loading = computed(() => ratingStore.loading)
const total = computed(() => ratingStore.total)

const currentPage = ref(1)
const pageSize = ref(10)
const replyingTo = ref(null)
const replyContent = ref('')

const loadRatings = async () => {
  await ratingStore.fetchRatings(props.toolId, currentPage.value - 1, pageSize.value)
}

onMounted(() => {
  loadRatings()
})

const handlePageChange = (page) => {
  currentPage.value = page
  loadRatings()
  // 滚动到评价列表顶部
  document.querySelector('.rating-list')?.scrollIntoView({ behavior: 'smooth' })
}

const handleReply = (rating) => {
  replyingTo.value = rating.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const submitReply = async (ratingId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    await ratingStore.createReply(ratingId, {
      content: replyContent.value,
      username: null
    })
    ElMessage.success('回复成功')
    cancelReply()
    // 重新加载评价列表
    await loadRatings()
    emit('rating-updated')
  } catch (error) {
    ElMessage.error('回复失败')
    console.error('回复失败:', error)
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }

  // 小于24小时
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }

  // 小于30天
  if (diff < 2592000000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }

  // 格式化为完整日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

defineExpose({
  refresh: loadRatings
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.rating-list {
  margin-top: $spacing-xxl;
}

.loading-container {
  padding: $spacing-xl 0;
}

.empty-state {
  padding: $spacing-xxl 0;
}

.rating-items {
  display: flex;
  flex-direction: column;
  gap: $spacing-xl;
}

.rating-item {
  background: $background-color-light;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-large;
  padding: $spacing-xl;
  transition: $transition-base;

  &:hover {
    border-color: rgba($primary-color, 0.3);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-lg;
  gap: $spacing-md;
}

.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex: 1;
  min-width: 0;
}

.user-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, $primary-color, #00ffcc);
  color: #000;
  font-weight: 600;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rating-date {
  font-size: $font-size-small;
  color: $text-color-secondary;
}

.rating-score {
  flex-shrink: 0;

  :deep(.el-rate) {
    .el-rate__icon {
      font-size: 18px;
    }

    .el-rate__text {
      font-size: $font-size-base;
      color: $primary-color;
      font-weight: 600;
    }
  }
}

.rating-content {
  margin-bottom: $spacing-md;
}

.comment-text {
  margin: 0;
  font-size: $font-size-base;
  color: $text-color-regular;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.rating-actions {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color-light;
}

.action-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  color: $text-color-secondary;
  font-size: $font-size-small;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    color: $primary-color;
  }
}

.action-icon {
  font-size: 16px;
}

.replies-section {
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $border-color-light;
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.reply-item {
  background: $background-color-darker;
  border-radius: $border-radius-base;
  padding: $spacing-md;
}

.reply-header {
  margin-bottom: $spacing-sm;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.reply-username {
  font-size: $font-size-small;
  font-weight: 600;
  color: $text-color-primary;
}

.reply-to {
  font-size: $font-size-small;
  color: $text-color-secondary;

  &::before,
  &::after {
    content: ' ';
  }
}

.reply-date {
  font-size: $font-size-small;
  color: $text-color-placeholder;
}

.reply-content {
  font-size: $font-size-small;
  color: $text-color-regular;
  line-height: 1.5;
  word-break: break-word;
}

.reply-input-section {
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $border-color-light;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  margin-top: $spacing-sm;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: $spacing-xxl;

  :deep(.el-pagination) {
    .el-pager li {
      background: $background-color-light;
      color: $text-color-regular;
      border: 1px solid $border-color-base;

      &.is-active {
        background: $primary-color;
        border-color: $primary-color;
        color: #000;
      }

      &:hover {
        border-color: $primary-color;
      }
    }

    button {
      background: $background-color-light;
      color: $text-color-regular;
      border: 1px solid $border-color-base;

      &:hover {
        border-color: $primary-color;
      }

      &:disabled {
        opacity: 0.5;
      }
    }
  }
}

@media (max-width: 768px) {
  .rating-item {
    padding: $spacing-lg;
  }

  .rating-header {
    flex-direction: column;
    gap: $spacing-sm;
  }

  .user-info {
    width: 100%;
  }

  .rating-score {
    width: 100%;
    display: flex;
    justify-content: flex-start;

    :deep(.el-rate) {
      display: flex;
      align-items: center;
    }
  }

  .rating-actions {
    flex-wrap: wrap;
  }
}
</style>
