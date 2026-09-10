<template>
  <div class="document-preview-page">
    <!--顶部信息栏-->
    <div class="document-preview-page__header">
      <span class="document-preview-page__title">{{ documentName }}</span>
    </div>
    <!--预览主体-->
    <div class="document-preview-page__body">
      <!--详情加载中-->
      <div v-if="detailLoading" class="document-preview-page__placeholder">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <!--详情加载失败-->
      <div v-else-if="detailError" class="document-preview-page__placeholder">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ detailError }}</span>
      </div>
      <template v-else>
        <!--文件加载中-->
        <div v-if="fileLoading" class="document-preview-page__placeholder">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <!--文件加载失败-->
        <div v-else-if="fileError" class="document-preview-page__placeholder">
          <el-icon><WarningFilled /></el-icon>
          <span>{{ fileError }}</span>
        </div>
        <!--预览主体-->
        <div ref="filePreviewRef" v-else class="file-preview">
          <!--word 文档-->
          <VueOfficeDocx
            v-if="previewType === 'word'"
            :src="fileData"
            class="file-preview__office"
          />
          <!--excel 表格-->
          <VueOfficeExcel
            v-else-if="previewType === 'excel'"
            :src="fileData"
            class="file-preview__office"
          />
          <!--ppt 演示文稿-->
          <VueOfficePptx
            v-else-if="previewType === 'ppt'"
            :src="fileData"
            :options="pptxOptions"
            class="file-preview__office"
          />
          <!--pdf.js 渲染 pdf-->
          <div v-else-if="previewType === 'pdf'" class="file-preview__pdf">
            <canvas
              v-for="page in pdfPageCount"
              :key="page"
              ref="canvasList"
              class="file-preview__pdf-canvas"
            />
          </div>
          <!--图片-->
          <img
            v-else-if="previewType === 'image'"
            :src="objectUrl"
            class="file-preview__image"
          />
          <!--音频-->
          <audio
            v-else-if="previewType === 'audio'"
            :src="objectUrl"
            controls
            class="file-preview__audio"
          />
          <!--视频-->
          <video
            v-else-if="previewType === 'video'"
            :src="objectUrl"
            controls
            class="file-preview__video"
          />
          <!--markdown 文件-->
          <v-md-preview
            v-else-if="previewType === 'markdown'"
            :text="markdownContent"
            class="file-preview__markdown"
          />
          <!--不支持的类型-->
          <div v-else class="document-preview-page__placeholder">
            <el-icon><QuestionFilled /></el-icon>
            <span>该文件类型暂不支持预览</span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import VueOfficeDocx from '@vue-office/docx'
import VueOfficeExcel from '@vue-office/excel'
import VueOfficePptx from '@vue-office/pptx'
import '@vue-office/docx/lib/index.css'
import '@vue-office/excel/lib/index.css'
import * as pdfjsLib from 'pdfjs-dist'
import PdfjsWorker from 'pdfjs-dist/build/pdf.worker.min.js?worker'
import { queryDocumentDetailAPI, downloadDocumentAPI } from '@/api/dms/document.js'
import { queryAttachmentDetailAPI, downloadAttachmentAPI } from '@/api/dms/attachment.js'
import { getDocumentCategory, DOCUMENT_CATEGORY } from '@/utils/documentUtil.js'

pdfjsLib.GlobalWorkerOptions.workerPort = new PdfjsWorker()

const TYPE_WORD = 'word'
const TYPE_EXCEL = 'excel'
const TYPE_PPT = 'ppt'
const TYPE_PDF = 'pdf'
const TYPE_IMAGE = 'image'
const TYPE_AUDIO = 'audio'
const TYPE_VIDEO = 'video'
const TYPE_MARKDOWN = 'markdown'
const TYPE_UNSUPPORTED = 'unsupported'

// 类别 -> 预览类型
const CATEGORY_PREVIEW_MAP = {
  [DOCUMENT_CATEGORY.WORD]: TYPE_WORD,
  [DOCUMENT_CATEGORY.EXCEL]: TYPE_EXCEL,
  [DOCUMENT_CATEGORY.PDF]: TYPE_PDF,
  [DOCUMENT_CATEGORY.PPT]: TYPE_PPT,
  [DOCUMENT_CATEGORY.TXT]: TYPE_MARKDOWN,
  [DOCUMENT_CATEGORY.MARKDOWN]: TYPE_MARKDOWN,
  [DOCUMENT_CATEGORY.IMAGE]: TYPE_IMAGE,
  [DOCUMENT_CATEGORY.VIDEO]: TYPE_VIDEO,
  [DOCUMENT_CATEGORY.AUDIO]: TYPE_AUDIO
}

const route = useRoute()
const documentId = computed(() => route.query.id)
// 按对象ID尾数区分类型：文档为9、附件为7（genObjectId 规则：n*100+typeValue）
const isAttachment = computed(() => Number(documentId.value) % 100 === 7)
const documentName = ref('')
const documentExt = ref('')
const detailLoading = ref(false)
const detailError = ref('')

const fileLoading = ref(false)
const fileError = ref('')
const fileData = ref(null)
const objectUrl = ref('')
const markdownContent = ref('')
const pdfPageCount = ref(0)
const canvasList = ref([])
const filePreviewRef = ref(null)
const pptxOptions = ref({})

const previewType = computed(() => CATEGORY_PREVIEW_MAP[getDocumentCategory(documentExt.value)] || TYPE_UNSUPPORTED)

loadDocumentDetail()

function loadDocumentDetail() {
  if (!documentId.value) {
    detailError.value = '文档 ID 不存在'
    return
  }
  detailLoading.value = true
  const detailAPI = isAttachment.value ? queryAttachmentDetailAPI : queryDocumentDetailAPI
  detailAPI({ id: documentId.value }).then(res => {
    if (res.code !== 200) {
      detailError.value = isAttachment.value ? '附件详情获取失败' : '文档详情获取失败'
      return
    }
    documentName.value = res.data.name
    documentExt.value = res.data.ext
    loadAndPreview()
  }).finally(() => {
    detailLoading.value = false
  })
}

// 下载时传后端的 ext
function resolveDownloadExt() {
  const category = getDocumentCategory(documentExt.value)
  if (category === DOCUMENT_CATEGORY.PDF) {
    return 'pdf'
  }
  return (documentExt.value || '').toLowerCase().replace(/^\./, '')
}

function revokeObjectUrl() {
  if (objectUrl.value) {
    URL.revokeObjectURL(objectUrl.value)
    objectUrl.value = ''
  }
}

// 用 pdf.js 渲染 PDF：按容器宽度算 scale 适配宽度，用 DPR 提高分辨率避免发虚
async function renderPdf(blob) {
  const arrayBuffer = await blob.arrayBuffer()
  const pdfDoc = await pdfjsLib.getDocument({ data: arrayBuffer }).promise
  // 先关闭 fileLoading，让模板走出 loading 占位符渲染出 canvas 容器
  // 否则 canvas 不存在，canvasList 收集不到，渲染会被全部跳过
  fileLoading.value = false
  pdfPageCount.value = pdfDoc.numPages
  await nextTick()
  const container = document.querySelector('.file-preview__pdf')
  const containerWidth = (container?.clientWidth || 800) - 32
  const dpr = window.devicePixelRatio || 1
  for (let i = 1; i <= pdfDoc.numPages; i++) {
    const page = await pdfDoc.getPage(i)
    const baseViewport = page.getViewport({ scale: 1 })
    const viewport = page.getViewport({ scale: containerWidth / baseViewport.width })
    const canvas = canvasList.value[i - 1]
    if (!canvas) continue
    canvas.width = Math.floor(viewport.width * dpr)
    canvas.height = Math.floor(viewport.height * dpr)
    canvas.style.width = `${Math.floor(viewport.width)}px`
    canvas.style.height = `${Math.floor(viewport.height)}px`
    const context = canvas.getContext('2d')
    const transform = dpr !== 1 ? [dpr, 0, 0, dpr, 0, 0] : undefined
    await page.render({ canvasContext: context, viewport, transform }).promise
  }
}

async function loadAndPreview() {
  fileError.value = ''
  fileData.value = null
  revokeObjectUrl()
  markdownContent.value = ''
  pdfPageCount.value = 0
  canvasList.value = []

  if (previewType.value === TYPE_UNSUPPORTED) {
    fileLoading.value = false
    return
  }

  fileLoading.value = true
  try {
    const blob = isAttachment.value
        ? await downloadAttachmentAPI({ id: documentId.value })
        : await downloadDocumentAPI({
          id: documentId.value,
          ext: resolveDownloadExt()
        })
    if (previewType.value === TYPE_WORD || previewType.value === TYPE_EXCEL || previewType.value === TYPE_PPT) {
      fileData.value = await blob.arrayBuffer()
    } else if (previewType.value === TYPE_PDF) {
      await renderPdf(blob)
    } else if (previewType.value === TYPE_MARKDOWN) {
      markdownContent.value = await blob.text()
    } else {
      objectUrl.value = URL.createObjectURL(blob)
    }
  } catch (e) {
    fileError.value = '文件加载失败'
    console.error('file preview error:', e)
  } finally {
    fileLoading.value = false
  }
}

// 确保 PPTX 组件初始化时能读到正确的容器尺寸
onMounted(() => {
  nextTick(() => {
    const el = filePreviewRef.value
    if (el) {
      pptxOptions.value = {
        width: el.clientWidth,
        height: el.clientHeight
      }
    }
  })
})

onBeforeUnmount(() => {
  revokeObjectUrl()
})
</script>

<style scoped lang="scss">
.document-preview-page {
  display: flex;
  flex-direction: column;
  height: 100vh;

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-sm $spacing-md;
    background-color: $bg-card;
    border-bottom: 1px solid $border-color-light;
  }

  &__title {
    font-size: 16px;
    font-weight: 400;
    color: $color-text-primary;
  }

  &__body {
    flex: 1;
    overflow: hidden;
  }

  &__placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    gap: $spacing-sm;
    color: $color-text-secondary;
    font-size: 14px;

    .el-icon {
      font-size: 32px;
    }
  }
}

.file-preview {
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: $bg-page;

  &__office {
    width: 100%;
    height: 100%;

    :deep(.vue-office-pptx) {
      height: 100%;
      overflow: hidden;
    }
  }

  &__pdf {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-md;
    gap: $spacing-md;
  }

  &__pdf-canvas {
    display: block;
    box-shadow: $shadow-card;
    border-radius: $border-radius-sm;
    background-color: $bg-card;
  }

  &__image {
    display: block;
    margin: 0 auto;
  }

  &__audio {
    display: block;
    width: 100%;
    max-width: 600px;
    margin: $spacing-lg auto;
  }

  &__video {
    display: block;
    width: 100%;
    max-width: 960px;
    margin: $spacing-lg auto;
  }

  &__markdown {
    width: 100%;
    min-height: 100%;
    padding: $spacing-lg;
    background-color: $bg-card;
    box-sizing: border-box;
  }
}
</style>
