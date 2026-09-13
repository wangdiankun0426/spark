<template>
  <el-drawer
    v-model="drawerVisible"
    title="上传文档"
    direction="ltr"
    size="30%"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="auto">
      <el-form-item label="选择文件">
        <el-upload
          class="document-uploader"
          drag
          :show-file-list="false"
          :http-request="handleUploadRequest"
          :before-upload="beforeUploadDocument"
          :disabled="uploading"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">超过 1MB 自动分片上传</div>
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
          <div class="progress-status">{{ uploadProgress.statusText }}</div>
        </div>
      </el-form-item>
    </el-form>
    <div class="drawer-tips">
      {{ tips }}
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="canResume" type="warning" @click="handleResume" :disabled="uploading">
          继续上传
        </el-button>
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { chunkUploadFile } from '@/utils/chunkUploadUtil.js'
import { fileDocumentAPI } from '@/api/dms/document.js'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  prtId: { type: [String, Number], default: undefined },
  tips: {
    type: String,
    default: '提示：超过 1MB 自动分片上传。上传成功后将自动触发预览、内容提取、索引、向量化等后续事件，可在"事件"中查看处理进度。'
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const drawerVisible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const uploading = ref(false)
const uploadProgress = ref(defaultProgress())
// 断点续传状态
const lastUploadId = ref(undefined)
const lastFile = ref(undefined)

// 是否可续传：有保存的 uploadId 且上传失败
const canResume = computed(() => {
  return lastUploadId.value && lastFile.value && uploadProgress.value.status === 'exception'
})

watch(() => props.modelValue, (val) => {
  if (val) {
    uploadProgress.value = defaultProgress()
    lastUploadId.value = undefined
    lastFile.value = undefined
  }
})

/**
 * 构造默认进度状态
 * @returns {Object} 进度状态
 */
function defaultProgress() {
  return {
    visible: false,
    percentage: 0,
    status: '',
    statusText: '',
    currentSize: '0 B',
    totalSize: '0 B',
    speed: ''
  }
}

/**
 * 上传前校验
 * @param file 待上传文件
 * @returns {boolean}
 */
function beforeUploadDocument(file) {
  // 清除之前的续传状态
  lastUploadId.value = undefined
  lastFile.value = undefined
  uploadProgress.value = {
    visible: true,
    percentage: 0,
    status: '',
    statusText: '准备上传...',
    currentSize: '0 B',
    totalSize: formatFileSize(file.size),
    speed: ''
  }
  return true
}

/**
 * 执行上传（新上传或续传）
 * @param file 待上传文件
 * @param uploadId 续传会话id（可选）
 */
async function doUpload(file, uploadId) {
  uploading.value = true
  lastFile.value = file
  try {
    const attachment = await chunkUploadFile(file, {
      uploadId,
      onUploadId: (id) => {
        lastUploadId.value = id
      },
      onStatus: (statusText) => {
        uploadProgress.value.statusText = statusText
      },
      onProgress: (progress) => {
        uploadProgress.value.percentage = progress.percent
        uploadProgress.value.currentSize = formatFileSize(progress.uploadedBytes)
        uploadProgress.value.totalSize = formatFileSize(progress.totalBytes)
        uploadProgress.value.speed = progress.speed
      }
    })
    uploadProgress.value.statusText = '归档文档中'
    const res = await fileDocumentAPI({ attId: attachment.id, prtId: props.prtId })
    if (res.code !== 200) {
      uploadProgress.value.status = 'exception'
      return
    }
    // 上传成功，清除续传状态
    lastUploadId.value = undefined
    lastFile.value = undefined
    uploadProgress.value.status = 'success'
    uploadProgress.value.percentage = 100
    ElMessage.success('文件上传成功')
    emit('success')
    setTimeout(() => {
      uploadProgress.value = defaultProgress()
    }, 1000)
  } catch (err) {
    uploadProgress.value.status = 'exception'
    uploadProgress.value.statusText = '上传失败，可点击"继续上传"重试'
    ElMessage.error('文件上传失败：' + (err.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

/**
 * 自定义上传：分片上传（不超过 1MB 直接整包）并归档为文档
 * @param option 上传选项（含 file）
 */
function handleUploadRequest(option) {
  doUpload(option.file)
}

/**
 * 继续上传：使用保存的 uploadId 续传
 */
function handleResume() {
  if (lastUploadId.value && lastFile.value) {
    uploadProgress.value.status = ''
    uploadProgress.value.statusText = '继续上传中...'
    doUpload(lastFile.value, lastUploadId.value)
  }
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns {string} 格式化大小
 */
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 关闭抽屉
 */
function handleClose() {
  uploadProgress.value = defaultProgress()
  lastUploadId.value = undefined
  lastFile.value = undefined
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
  font-weight: 400;
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
.progress-status {
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
