<template>
  <el-form-item
      :label="props.widget.config.label"
      :required="props.widget.config.required"
      v-if="!props.widget.config.hidden"
  >
    <el-upload
        class="upload-file-widget"
        :show-file-list="false"
        :http-request="handleUploadRequest"
        :before-upload="beforeUpload"
        :multiple="props.widget.config.multiple"
        :disabled="props.widget.config.disabled || props.widget.config.readonly || uploading"
    >
      <el-button
          plain
          :disabled="props.widget.config.disabled || props.widget.config.readonly || uploading"
      >
        <el-icon><UploadFilled /></el-icon>
        点击上传
      </el-button>
    </el-upload>
    <div v-if="uploading || canResume" class="upload-progress">
      <el-progress :percentage="uploadPercent" :stroke-width="6" :status="uploadFailed ? 'exception' : ''" />
      <div class="upload-progress-status">{{ uploadStatusText }}</div>
      <el-button v-if="canResume" type="warning" size="small" @click="handleResume" :disabled="uploading">
        继续上传
      </el-button>
    </div>
    <el-table
        class="upload-file-table"
        :data="fileList"
        size="small"
    >
      <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link
              type="primary"
              :underline="false"
              @click="handlePreview(row)"
          >{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="sizeStr" label="大小" width="90" align="center" />
      <el-table-column prop="ownerName" label="上传人" width="110" align="center"  />
      <el-table-column prop="createdDt" label="上传时间" width="140" align="center" />
      <el-table-column label="操作" width="130" fixed="right" align="center" >
        <template #default="{ row, $index }">
          <el-button
              v-if="props.widget.config.downloadable"
              link
              type="primary"
              @click="handleDownload(row)"
          >下载</el-button>
          <el-button
              v-if="!props.widget.config.disabled && !props.widget.config.readonly"
              link
              type="danger"
              @click="handleRemove($index)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form-item>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { downloadSystemAttachmentAPI } from '@/api/system/attachment.js'
import { chunkUploadFile } from '@/utils/chunkUploadUtil.js'

defineOptions({
  name: "customUploadAttachment"
})

const props = defineProps({
  widget: Object,
})

const router = useRouter()

// widget.config 值约定：
//   value：附件id（单选为单个ID，多选为逗号串），用于业务关联与下载
//   showValue：附件信息 JSON 数组字符串，如 [{"id":1,"name":"a.pdf","sizeStr":"1MB","ownerName":"张三","createdDt":"2026-08-20 15:00:00"}]
// 附件列表（含名称/大小/上传人/上传时间），由上传接口返回数据直接维护
const fileList = ref([])
// 上传中状态：控制上传按钮禁用与进度展示
const uploading = ref(false)
const uploadPercent = ref(0)
const uploadStatusText = ref('')
const uploadFailed = ref(false)
// 断点续传状态
const lastUploadId = ref(undefined)
const lastFile = ref(undefined)

// 是否可续传：有保存的 uploadId 且上传失败
const canResume = computed(() => {
  return lastUploadId.value && lastFile.value && uploadFailed.value && !uploading.value
})

/**
 * 初始化附件列表：从 showValue（JSON数组）回显，兼容旧的逗号串名称格式
 */
const initFileList = () => {
  fileList.value = []
  const showValue = props.widget.config.showValue
  if (!showValue) {
    return
  }
  try {
    const list = JSON.parse(showValue)
    if (Array.isArray(list)) {
      fileList.value = list.filter(item => item && item.id != null)
      return
    }
  } catch (e) {
    // 非JSON格式，按旧格式（逗号串名称）处理
  }
  const ids = String(props.widget.config.value == null ? '' : props.widget.config.value)
      .split(',').filter(Boolean)
  const names = String(showValue).split(',').filter(Boolean)
  fileList.value = ids.map((id, index) => ({ id: Number(id), name: names[index] || '' }))
}
initFileList()

/**
 * 同步表单值：value 为附件id，showValue 为附件信息 JSON 数组字符串
 */
const syncConfigValue = () => {
  const ids = fileList.value.map(file => file.id)
  if (ids.length === 0) {
    props.widget.config.value = null
    props.widget.config.showValue = null
    return
  }
  props.widget.config.value = props.widget.config.multiple ? ids.join(',') : ids[0]
  props.widget.config.showValue = JSON.stringify(fileList.value)
}

/**
 * 格式化时间为 yyyy-MM-dd HH:mm:ss
 * @param date 时间对象
 * @returns {string} 格式化时间
 */
const formatTime = (date) => {
  const pad = n => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/**
 * 上传前校验：单选限制一个附件
 * @param file 待上传文件
 * @returns {boolean}
 */
function beforeUpload(file) {
  if (!props.widget.config.multiple && fileList.value.length > 0) {
    ElMessage.warning('只能上传一个附件，请先删除已上传的附件')
    return false
  }
  // 清除之前的续传状态
  lastUploadId.value = undefined
  lastFile.value = undefined
  uploadFailed.value = false
  return true
}

/**
 * 执行上传（新上传或续传）
 * @param file 待上传文件
 * @param uploadId 续传会话id（可选）
 */
async function doUpload(file, uploadId) {
  uploading.value = true
  uploadPercent.value = 0
  uploadStatusText.value = uploadId ? '继续上传中...' : '准备上传...'
  uploadFailed.value = false
  lastFile.value = file
  try {
    const attachment = await chunkUploadFile(file, {
      uploadId,
      onUploadId: (id) => {
        lastUploadId.value = id
      },
      onStatus: (statusText) => {
        uploadStatusText.value = statusText
      },
      onProgress: (progress) => {
        uploadPercent.value = progress.percent
      }
    })
    // 上传成功，清除续传状态
    lastUploadId.value = undefined
    lastFile.value = undefined
    uploadFailed.value = false
    fileList.value.push({
      id: attachment.id,
      name: attachment.name,
      sizeStr: attachment.sizeStr,
      ownerName: attachment.ownerName,
      createdDt: formatTime(new Date())
    })
    syncConfigValue()
    ElMessage.success('附件上传成功')
  } catch (err) {
    uploadFailed.value = true
    uploadStatusText.value = '上传失败，可点击"继续上传"重试'
    // 失败提示由 request.js 或工具内抛出处理，此处避免重复提示
  } finally {
    uploading.value = false
  }
}

/**
 * 自定义上传：分片上传文件，成功后存入附件列表
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
    doUpload(lastFile.value, lastUploadId.value)
  }
}

/**
 * 移除已上传附件（仅移除表单值关联，不删除系统附件记录）
 * @param index 附件下标
 */
function handleRemove(index) {
  fileList.value.splice(index, 1)
  syncConfigValue()
}

/**
 * 预览附件（新开标签页，复用系统文档预览页）
 * @param file 附件信息（含 id）
 */
function handlePreview(file) {
  const { href } = router.resolve({ path: '/document/preview', query: { id: file.id } })
  window.open(href, '_blank')
}

/**
 * 下载已上传附件
 * @param file 附件信息（含 id、name）
 */
function handleDownload(file) {
  downloadSystemAttachmentAPI({ id: file.id }).then(blob => {
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }).catch(() => {
    ElMessage.error('附件下载失败')
  })
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 10px;
}
.upload-progress {
  width: 100%;
  margin: 6px 0;
}
.upload-progress-status {
  margin-top: 4px;
  margin-bottom: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.upload-file-table {
  width: 100%;
  margin-top: 6px;

  .el-link {
    font-size: 13px;
  }
}
</style>
