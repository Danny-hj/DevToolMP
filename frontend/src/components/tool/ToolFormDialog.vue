<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑工具' : '添加工具'"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item
        label="工具名称"
        prop="name"
      >
        <el-input
          v-model="formData.name"
          placeholder="请输入工具名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item
        label="工具描述"
        prop="description"
      >
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入工具描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item
        label="分类"
        prop="categoryId"
      >
        <el-select
          v-model="formData.categoryId"
          placeholder="请选择分类"
          style="width: 100%"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item
        label="Codehub所有者"
        prop="codehubOwner"
      >
        <el-input
          v-model="formData.codehubOwner"
          placeholder="例如: vuejs（可选）"
          @blur="validateGithubRepo"
        >
          <template #append>
            <el-button
              :loading="fetching"
              :disabled="!formData.codehubOwner || !formData.codehubRepo"
              @click="fetchGithubRepo"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item
        label="Codehub仓库"
        prop="codehubRepo"
      >
        <el-input
          v-model="formData.codehubRepo"
          placeholder="例如: vue（可选）"
          @blur="validateGithubRepo"
        >
          <template #append>
            <el-button
              :loading="fetching"
              :disabled="!formData.codehubOwner || !formData.codehubRepo"
              @click="fetchGithubRepo"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </template>
        </el-input>
        <div
          v-if="codehubRepoValid"
          class="repo-status"
        >
          <el-icon color="#67C23A">
            <SuccessFilled />
          </el-icon>
          <span>仓库有效</span>
        </div>
        <div
          v-else-if="formData.codehubOwner && formData.codehubRepo"
          class="repo-status invalid"
        >
          <el-icon color="#F56C6C">
            <CircleCloseFilled />
          </el-icon>
          <span>仓库无效或不存在</span>
        </div>
        <div
          v-if="!formData.codehubOwner || !formData.codehubRepo"
          class="repo-hint"
        >
          <el-icon><InfoFilled /></el-icon>
          <span>如果不填写 Codehub 信息，工具将无法同步 Codehub 数据和显示仓库链接</span>
        </div>
      </el-form-item>

      <el-form-item
        label="版本号"
        prop="version"
      >
        <el-input
          v-model="formData.version"
          placeholder="例如: 1.0.0"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item label="标签">
        <el-select
          v-model="formData.tags"
          multiple
          filterable
          allow-create
          placeholder="请输入标签，按回车添加"
          style="width: 100%"
        >
          <el-option
            v-for="tag in commonTags"
            :key="tag"
            :label="tag"
            :value="tag"
          />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="isEdit"
        label="状态"
        prop="status"
      >
        <el-radio-group v-model="formData.status">
          <el-radio label="active">
            已上架
          </el-radio>
          <el-radio label="inactive">
            已下架
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          native-type="submit"
          :loading="submitting"
        >
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
        <el-button @click="handleClose">
          取消
        </el-button>
        <el-button
          v-if="!isEdit && formData.codehubOwner && formData.codehubRepo"
          type="info"
          @click="fillFromGithub"
        >
          从Codehub获取信息
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, SuccessFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'

const props = defineProps({
  modelValue: Boolean,
  tool: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const toolsStore = useToolsStore()
const formRef = ref()
const dialogVisible = ref(false)
const submitting = ref(false)
const fetching = ref(false)
const codehubRepoValid = ref(false)

// 表单数据
const formData = ref({
  name: '',
  description: '',
  categoryId: null,
  codehubOwner: '',
  codehubRepo: '',
  version: '',
  tags: [],
  status: 'active'
})

// 是否为编辑模式
const isEdit = computed(() => !!props.tool)

// 分类列表（与数据库保持一致）
const categories = ref([
  { id: 1, name: 'MCP' },
  { id: 2, name: 'Skill' },
  { id: 3, name: '开发工具' },
  { id: 4, name: '设计工具' },
  { id: 5, name: '生产力工具' },
  { id: 6, name: '测试工具' }
])

// 移除fetchCategories相关的代码

// 常用标签
const commonTags = [
  'Vue', 'React', 'Angular', 'Node.js', 'TypeScript',
  '开发框架', 'UI组件', '构建工具', '测试', 'CLI'
]

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入工具名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入工具描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' }
  ]
}

// 监听modelValue变化
watch(() => props.modelValue, (val) => {
  console.log('[ToolFormDialog] modelValue changed:', val)
  dialogVisible.value = val
  if (val) {
    console.log('[ToolFormDialog] Dialog opening, tool:', props.tool)
    if (props.tool) {
      // 编辑模式：填充数据
      Object.assign(formData.value, {
        name: props.tool.name || '',
        description: props.tool.description || '',
        categoryId: props.tool.categoryId || null,
        codehubOwner: props.tool.codehubOwner || '',
        codehubRepo: props.tool.codehubRepo || '',
        version: props.tool.version || '',
        tags: props.tool.tags || [],
        status: props.tool.status || 'active'
      })
      codehubRepoValid.value = true
    } else {
      // 新增模式：重置表单（延迟执行，确保DOM已渲染）
      nextTick(() => {
        resetForm()
      })
    }
  }
}, { immediate: true })

// 监听dialogVisible变化
watch(dialogVisible, (val) => {
  console.log('[ToolFormDialog] dialogVisible changed:', val)
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  formData.value = {
    name: '',
    description: '',
    categoryId: null,
    codehubOwner: '',
    codehubRepo: '',
    version: '',
    tags: [],
    status: 'active'
  }
  codehubRepoValid.value = false
  // 使用 nextTick 确保表单已渲染
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 验证Codehub仓库
const validateGithubRepo = async () => {
  if (!formData.value.codehubOwner || !formData.value.codehubRepo) {
    codehubRepoValid.value = false
    return
  }

  try {
    const isValid = await toolsStore.validateCodehubRepo(
      formData.value.codehubOwner,
      formData.value.codehubRepo
    )
    codehubRepoValid.value = isValid
  } catch {
    codehubRepoValid.value = false
  }
}

// 从Codehub获取仓库信息
const fetchGithubRepo = async () => {
  if (!formData.value.codehubOwner || !formData.value.codehubRepo) {
    ElMessage.warning('请先填写Codehub所有者和仓库名')
    return
  }

  fetching.value = true
  try {
    const repoInfo = await toolsStore.fetchCodehubRepoInfo(
      formData.value.codehubOwner,
      formData.value.codehubRepo
    )

    if (repoInfo) {
      // 自动填充信息
      if (!formData.value.name) {
        formData.value.name = repoInfo.name || ''
      }
      if (!formData.value.description) {
        formData.value.description = repoInfo.description || ''
      }
      codehubRepoValid.value = true
      ElMessage.success('Codehub仓库信息获取成功')
    } else {
      codehubRepoValid.value = false
      ElMessage.error('无法获取Codehub仓库信息，请检查仓库地址是否正确')
    }
  } catch {
    codehubRepoValid.value = false
    ElMessage.error('获取Codehub仓库信息失败')
  } finally {
    fetching.value = false
  }
}

// 从Codehub填充完整信息
const fillFromGithub = async () => {
  await fetchGithubRepo()
  if (!codehubRepoValid.value) {
    return
  }

  try {
    const repoInfo = await toolsStore.fetchCodehubRepoInfo(
      formData.value.codehubOwner,
      formData.value.codehubRepo
    )

    if (repoInfo) {
      formData.value.name = repoInfo.name || ''
      formData.value.description = repoInfo.description || ''
      formData.value.version = 'latest'
      ElMessage.success('已从Codehub填充信息')
    }
  } catch {
    ElMessage.error('填充信息失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()

  submitting.value = true
  try {
    const submitData = {
      name: formData.value.name,
      description: formData.value.description,
      categoryId: formData.value.categoryId,
      codehubOwner: formData.value.codehubOwner,
      codehubRepo: formData.value.codehubRepo,
      version: formData.value.version,
      tags: formData.value.tags,
      status: formData.value.status
    }

    if (isEdit.value) {
      // 编辑模式
      await toolsStore.updateTool(props.tool.id, submitData)
      ElMessage.success('工具更新成功')
    } else {
      // 新增模式
      await toolsStore.createTool(submitData)
      ElMessage.success('工具创建成功')
    }

    emit('success')
    handleClose()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.repo-status {
  margin-top: $spacing-sm;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: $font-size-small;

  &.invalid {
    color: $danger-color;
  }

  &:not(.invalid) {
    color: $success-color;
  }
}

.repo-hint {
  margin-top: $spacing-sm;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: $font-size-small;
  color: $info-color;
  padding: $spacing-sm;
  background: rgba($info-color, 0.1);
  border-radius: $border-radius-base;
}
</style>
