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
    <div class="upload-file-list">
      <div class="upload-file-item" v-for="(file, index) in fileList" :key="file.id">
        <el-link type="primary" :underline="false" @click="handlePreview(file)">
          <el-icon><Document /></el-icon>
          <span>{{ file.name }}</span>
        </el-link>
        <div class="upload-file-item__actions">
          <el-button
              v-if="props.widget.config.downloadable"
              link
              type="info"
              title="下载"
              @click="handleDownload(file)"
          >
            <el-icon><Download /></el-icon>下载
          </el-button>
          <el-button
              v-if="!props.widget.config.disabled && !props.widget.config.readonly"
              link
              type="danger"
              @click="handleRemove(index)"
          >
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </div>
      </div>
    </div>
  </el-form-item>
</template>

<script setup>
import { computed } from 'vue'
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

// widget.config.value 约定：
//   多选：逗号串 "id1,id2"，与 showValue（逗号串名称）一一对应
//   单选：单个 ID（Number）
const fileList = computed(() => {
  const ids = String(props.widget.config.value == null ? '' : props.widget.config.value)
      .split(',').filter(Boolean)
  const names = String(props.widget.config.showValue == null ? '' : props.widget.config.showValue)
      .split(',').filter(Boolean)
  return ids.map((id, index) => ({ id: Number(id), name: names[index] || '' }))
})

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
 * 自定义上传：调用系统附件上传接口
 * @param option 上传选项（含 file）
 */
function handleUploadRequest(option) {
  uploadSystemAttachmentAPI(option.file).then(res => {
    if (res.code !== 200 || !res.data) {
      return
    }
    const ids = fileList.value.map(file => file.id)
    const names = fileList.value.map(file => file.name)
    ids.push(res.data.id)
    names.push(res.data.name)
    if (props.widget.config.multiple) {
      props.widget.config.value = ids.join(',')
      props.widget.config.showValue = names.join(',')
    } else {
      props.widget.config.value = ids[0]
      props.widget.config.showValue = names[0]
    }
    ElMessage.success('附件上传成功')
  })
}

/**
 * 移除已上传附件（仅移除表单值关联，不删除系统附件记录）
 * @param index 附件下标
 */
function handleRemove(index) {
  const ids = fileList.value.map(file => file.id)
  const names = fileList.value.map(file => file.name)
  ids.splice(index, 1)
  names.splice(index, 1)
  if (props.widget.config.multiple) {
    props.widget.config.value = ids.length > 0 ? ids.join(',') : null
    props.widget.config.showValue = names.length > 0 ? names.join(',') : null
  } else {
    props.widget.config.value = ids.length > 0 ? ids[0] : null
    props.widget.config.showValue = names.length > 0 ? names[0] : null
  }
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
.upload-file-list {
  width: 100%;
  margin-top: 6px;
}
.upload-file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 28px;
  padding: 0 8px;
  border-radius: $border-radius-sm;

  &:hover {
    background-color: $color-primary-light;
  }

  .el-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    max-width: calc(100% - 64px);

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }
}
</style>
