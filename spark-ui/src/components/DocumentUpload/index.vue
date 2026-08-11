<template>
  <el-drawer
    v-model="drawerVisible"
    title="上传文档"
    direction="ltr"
    size="30%"
    :before-close="handleClose"
  >
    <el-form :model="form" label-width="auto">
      <el-form-item label="选择文件">
        <el-upload
          class="document-uploader"
          drag
          :show-file-list="true"
          :file-list="uploadFileList"
          :action="documentUploadUrl"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-progress="handleUploadProgress"
          :before-upload="beforeUploadDocument"
          :headers="uploadHeaders"
          :limit="1"
          :on-exceed="handleExceed"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">请上传小于50MB的文件</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item v-if="uploadProgress.visible" label="上传进度">
        <div class="upload-progress-container">
          <el-progress
            :percentage="uploadProgress.percentage"
            :status="uploadProgress.status"
            :stroke-width="20"
          />
          <div class="progress-info">
            <span>{{ uploadProgress.currentSize }} / {{ uploadProgress.totalSize }}</span>
            <span>{{ uploadProgress.speed }}</span>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <div class="drawer-tips">
      {{ tips }}
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { UploadFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  prtId: { type: [String, Number], default: undefined },
  tips: {
    type: String,
    default: '提示：单文件大小不超过 50MB。上传成功后将自动触发预览、内容提取、索引、向量化等后续事件，可在"事件"中查看处理进度。'
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const token = computed(() => store.getters['user/getToken'])

const drawerVisible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const uploadHeaders = ref({ Authorization: undefined })
const documentUploadUrl = ref(undefined)
const uploadFileList = ref([])
const uploadProgress = ref({
  visible: false,
  percentage: 0,
  status: '',
  currentSize: '0 KB',
  totalSize: '0 KB',
  speed: '0 KB/s'
})
let uploadStartTime = 0

watch(() => props.modelValue, (val) => {
  if (val) {
    const baseUrl = process.env.BASE_HTTP_API
    documentUploadUrl.value = baseUrl + '/kb/document/upload?prtId=' + props.prtId
    uploadHeaders.value.Authorization = token.value
    uploadFileList.value = []
    uploadProgress.value = {
      visible: false,
      percentage: 0,
      status: '',
      currentSize: '0 KB',
      totalSize: '0 KB',
      speed: '0 KB/s'
    }
  }
})

function handleUploadSuccess(res) {
  if (res.code === 200) {
    uploadProgress.value.status = 'success'
    uploadProgress.value.percentage = 100
    ElMessage.success('文件上传成功')
    emit('success')
    setTimeout(() => {
      uploadFileList.value = []
      uploadProgress.value.visible = false
      uploadProgress.value.percentage = 0
      uploadProgress.value.status = ''
    }, 1000)
  } else {
    uploadProgress.value.status = 'exception'
    ElMessage.error(res.message)
  }
}

function handleUploadError(err) {
  uploadProgress.value.status = 'exception'
  ElMessage.error('文件上传失败：' + (err.message || '未知错误'))
}

function handleUploadProgress(event) {
  const timeElapsed = (Date.now() - uploadStartTime) / 1000
  const percentage = Math.round(event.percent)
  const loaded = event.loaded
  const total = event.total
  const speed = timeElapsed > 0 ? loaded / timeElapsed : 0
  uploadProgress.value.visible = true
  uploadProgress.value.percentage = percentage
  uploadProgress.value.currentSize = formatFileSize(loaded)
  uploadProgress.value.totalSize = formatFileSize(total)
  uploadProgress.value.speed = formatFileSize(speed) + '/s'
}

function beforeUploadDocument(file) {
  if (file.size > 1024 * 1024 * 50) {
    ElMessage.error('请上传小于50MB的文件')
    return false
  }
  uploadStartTime = Date.now()
  uploadProgress.value = {
    visible: false,
    percentage: 0,
    status: '',
    currentSize: '0 KB',
    totalSize: formatFileSize(file.size),
    speed: '0 KB/s'
  }
  return true
}

function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先删除已选择的文件')
}

function handleClose() {
  uploadFileList.value = []
  uploadProgress.value = {
    visible: false,
    percentage: 0,
    status: '',
    currentSize: '0 KB',
    totalSize: '0 KB',
    speed: '0 KB/s'
  }
  drawerVisible.value = false
}
</script>

<style scoped lang="scss">
.document-uploader :deep(.el-upload) {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100%;
  padding: 40px 20px;
}
.document-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}
.document-uploader :deep(.el-upload.is-dragover) {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-8);
}
.el-icon--upload {
  font-size: 48px;
  color: var(--el-color-primary);
  margin-bottom: 16px;
}
.el-upload__text {
  color: var(--el-text-color-regular);
  font-size: 14px;
}
.el-upload__text em {
  color: var(--el-color-primary);
  font-style: normal;
  font-weight: 500;
}
.upload-progress-container {
  width: 100%;
  padding: 10px 0;
}
.progress-info {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.drawer-tips {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background-color: $color-primary-light;
  border-left: 3px solid $color-primary;
  border-radius: $border-radius-sm;
  color: $color-text-secondary;
  font-size: 12px;
  line-height: 1.6;
}
</style>