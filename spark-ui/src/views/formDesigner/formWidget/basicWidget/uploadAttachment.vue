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
        :disabled="props.widget.config.disabled || props.widget.config.readonly"
    >
      <el-button
          plain
          :disabled="props.widget.config.disabled || props.widget.config.readonly"
      >
        <el-icon><UploadFilled /></el-icon>
        点击上传
      </el-button>
    </el-upload>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadSystemAttachmentAPI, downloadSystemAttachmentAPI } from '@/api/system/attachment.js'

defineOptions({
  name: "customUploadAttachment"
})

const props = defineProps({
  widget: Object,
})

const router = useRouter()

// 上传文件大小上限（50MB）
const maxFileSize = 50 * 1024 * 1024

// widget.config 值约定：
//   value：附件id（单选为单个ID，多选为逗号串），用于业务关联与下载
//   showValue：附件信息 JSON 数组字符串，如 [{"id":1,"name":"a.pdf","sizeStr":"1MB","ownerName":"张三","createdDt":"2026-08-20 15:00:00"}]
// 附件列表（含名称/大小/上传人/上传时间），由上传接口返回数据直接维护
const fileList = ref([])

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
 * 上传前校验：单选限制一个附件、文件大小限制
 * @param file 待上传文件
 * @returns {boolean}
 */
function beforeUpload(file) {
  if (!props.widget.config.multiple && fileList.value.length > 0) {
    ElMessage.warning('只能上传一个附件，请先删除已上传的附件')
    return false
  }
  if (file.size > maxFileSize) {
    ElMessage.error('请上传小于50MB的文件')
    return false
  }
  return true
}

/**
 * 自定义上传：调用系统附件上传接口，返回数据直接存入附件列表
 * @param option 上传选项（含 file）
 */
function handleUploadRequest(option) {
  uploadSystemAttachmentAPI(option.file).then(res => {
    if (res.code !== 200 || !res.data) {
      return
    }
    const attachment = res.data
    fileList.value.push({
      id: attachment.id,
      name: attachment.name,
      sizeStr: attachment.sizeStr,
      ownerName: attachment.ownerName,
      createdDt: formatTime(new Date())
    })
    syncConfigValue()
    ElMessage.success('附件上传成功')
  })
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
.upload-file-table {
  width: 100%;
  margin-top: 6px;

  .el-link {
    font-size: 13px;
  }
}
</style>
