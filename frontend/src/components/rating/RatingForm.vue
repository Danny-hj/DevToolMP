<template>
  <div class="rating-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="昵称" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入你的昵称（可选）"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="评分" prop="score">
        <el-rate v-model="form.score" show-text />
      </el-form-item>

      <el-form-item label="评论" prop="comment">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="4"
          placeholder="请分享你的使用体验..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          提交评价
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  toolId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['success', 'cancel'])

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  score: 0,
  comment: ''
})

const rules = {
  score: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 10, message: '评论内容至少10个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    loading.value = true

    emit('success', {
      username: form.username || '匿名用户',
      score: form.score,
      comment: form.comment
    })

    ElMessage.success('评价提交成功')
    formRef.value.resetFields()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('评价提交失败')
    }
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
  formRef.value.resetFields()
}
</script>

<style scoped>
.rating-form {
  background-color: #2a2a2a;
  border-radius: 8px;
  padding: 24px;
}
</style>
