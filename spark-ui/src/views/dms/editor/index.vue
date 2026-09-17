<template>
  <div class="document-editor-page">
    <!--编辑主体-->
    <div class="document-editor-page__body">
      <!--加载中-->
      <div v-if="loading" class="document-editor-page__placeholder">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <!--加载失败-->
      <div v-else-if="loadError" class="document-editor-page__placeholder">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ loadError }}</span>
      </div>
      <!--markdown 编辑器-->
      <v-md-editor
        v-else-if="editMode === 'markdown'"
        v-model="content"
        height="100%"
        class="document-editor-page__editor"
        @save="handleSave"
      />
      <!--office 文档：OnlyOffice 编辑器-->
      <div v-else-if="editMode === 'onlyoffice'" class="document-editor-page__onlyoffice">
        <div id="onlyoffice-editor" class="document-editor-page__onlyoffice-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import { queryDocumentDetailAPI, downloadDocumentAPI } from '@/api/dms/document.js'
import { queryOnlyOfficeConfigAPI } from '@/api/dms/onlyoffice.js'
import { chunkUploadFile } from '@/utils/chunkUploadUtil.js'
import { getDocumentCategory, DOCUMENT_CATEGORY } from '@/utils/documentUtil.js'

const route = useRoute()
const documentId = computed(() => route.query.id)

const documentName = ref('')
const documentExt = ref('')
const loading = ref(false)
const loadError = ref('')
const content = ref('')
const saving = ref(false)
// 编辑模式：markdown / onlyoffice
const editMode = ref('')

// OnlyOffice 编辑器实例与 api.js 加载状态
let onlyOfficeEditor = null
let onlyOfficeApiPromise = null

loadDocument()

/**
 * 加载文档详情，按类型分流编辑模式
 */
function loadDocument() {
  if (!documentId.value) {
    loadError.value = '文档 ID 不存在'
    return
  }
  loading.value = true
  queryDocumentDetailAPI({ id: documentId.value }).then(res => {
    if (res.code !== 200) {
      loadError.value = '文档详情获取失败'
      return
    }
    documentName.value = res.data.name
    documentExt.value = res.data.ext
    const category = getDocumentCategory(documentExt.value)
    if (category === DOCUMENT_CATEGORY.MARKDOWN || category === DOCUMENT_CATEGORY.TXT) {
      editMode.value = 'markdown'
      loadContent()
      return
    }
    if (category === DOCUMENT_CATEGORY.WORD || category === DOCUMENT_CATEGORY.EXCEL || category === DOCUMENT_CATEGORY.PPT) {
      editMode.value = 'onlyoffice'
      renderOnlyOffice()
      return
    }
    loadError.value = '该文件类型暂不支持在线编辑'
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 加载 OnlyOffice api.js
 * @param serverUrl 文档服务器地址
 * @returns {Promise<void>}
 */
function loadOnlyOfficeApi(serverUrl) {
  if (window.DocsAPI) {
    return Promise.resolve()
  }
  if (onlyOfficeApiPromise) {
    return onlyOfficeApiPromise
  }
  onlyOfficeApiPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `${serverUrl}/web-apps/apps/api/documents/api.js`
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
  return onlyOfficeApiPromise
}

/**
 * 用 OnlyOffice 渲染 office 编辑器
 */
async function renderOnlyOffice() {
  try {
    const res = await queryOnlyOfficeConfigAPI({ id: documentId.value })
    if (res.code !== 200 || !res.data) {
      loadError.value = res.message || '编辑配置获取失败'
      return
    }
    const config = res.data
    await loadOnlyOfficeApi(config.serverUrl)
    delete config.serverUrl
    // 先关闭 loading，让模板渲染出容器
    loading.value = false
    await nextTick()
    onlyOfficeEditor = new window.DocsAPI.DocEditor('onlyoffice-editor', config)
  } catch (e) {
    loadError.value = '编辑器加载失败'
  }
}

/**
 * 加载文档内容
 */
async function loadContent() {
  loading.value = true
  try {
    const blob = await downloadDocumentAPI({ id: documentId.value, ext: documentExt.value })
    content.value = await blob.text()
  } catch (e) {
    loadError.value = '文档内容加载失败'
  } finally {
    loading.value = false
  }
}

/**
 * 保存文档内容，构造文件走上传新版本链路
 */
async function handleSave() {
  if (!content.value || !content.value.trim()) {
    ElMessage.warning('文档内容不能为空')
    return
  }
  saving.value = true
  try {
    const file = new File([content.value], documentName.value, { type: 'text/markdown' })
    await chunkUploadFile(file, { docId: documentId.value })
    ElMessage.success('保存成功，已生成新版本')
  } catch (err) {
    ElMessage.error('保存失败：' + (err.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

onBeforeUnmount(() => {
  if (onlyOfficeEditor) {
    onlyOfficeEditor.destroyEditor()
    onlyOfficeEditor = null
  }
})
</script>

<style scoped lang="scss">
.document-editor-page {
  display: flex;
  flex-direction: column;
  height: 100vh;

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

  &__editor {
    width: 100%;
    height: 100%;
  }

  &__onlyoffice {
    width: 100%;
    height: 100%;
  }

  &__onlyoffice-container {
    width: 100%;
    height: 100%;
  }
}
</style>
