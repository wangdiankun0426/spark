<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="kd-header">
      <div class="kd-header-left">
        <el-button text @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <el-divider direction="vertical" />
        <div class="kd-header-title">
          <el-icon class="kd-header-icon"><component :is="headerIcon" /></el-icon>
          {{ headerTitle }}
        </div>
      </div>
      <div class="kd-header-right">
        <el-input
            v-model="documentQuery.name"
            placeholder="搜索文档名称"
            clearable
            :prefix-icon="Search"
            style="width: 220px"
            @input="handleGetDocumentList"
        />
        <el-button type="warning" @click="handleResetDocumentQuery">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
        <el-button type="info" @click="handleGetDocumentList">
          <el-icon><Search /></el-icon>查询
        </el-button>
        <el-button type="primary" @click="handleUploadDocument">
          <el-icon style="margin-right: 4px"><DocumentAdd /></el-icon>上传文档
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0">
          <el-icon style="margin-right: 4px"><Delete /></el-icon>删除
          <span v-if="selectedIds.length > 0">({{ selectedIds.length }})</span>
        </el-button>
        <el-button type="warning" @click="handleBatchEvent" :disabled="selectedIds.length === 0">
          <el-icon style="margin-right: 4px"><Refresh /></el-icon>重置事件
          <span v-if="selectedIds.length > 0">({{ selectedIds.length }})</span>
        </el-button>
      </div>
    </div>
    <!-- 文档列表 -->
    <div style="width: 100%;">
      <el-table
          ref="tableRef"
          height="calc(100vh - 206px)"
          :data="documentList"
          highlight-current-row
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column prop="id" label="编号" width="100" align="center"/>
        <el-table-column prop="name" label="名称" min-width="300px" align="left">
          <template #default="scope">
            <DocumentIcon :ext="scope.row.ext" />
            <el-link
                class="document-name-link"
                :underline="false"
                @click="handlePreviewDocument(scope.row)"
            >
              {{ scope.row.name }}
            </el-link>
            <el-tag size="small" type="info" style="margin-left: 4px">V{{ scope.row.versionNo }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="" :width="isGraph ? 300 : 240" align="center">
          <template #default="scope">
            <div class="row-actions">
              <el-button v-if="isGraph" type="primary" text @click="handleOpenGraphDetail(scope.row.id)">
                <el-icon><Connection /></el-icon>
                <span style="font-size: 12px; font-weight: 400">图谱</span>
              </el-button>
              <el-button type="success" text @click="handleDocumentEvent(scope.row.id)">
                <el-icon><HelpFilled /></el-icon>
                <span style="font-size: 12px; font-weight: 400">事件</span>
              </el-button>
              <el-button type="warning" text @click="handleDocumentVersion(scope.row.id)">
                <el-icon><Files /></el-icon>
                <span style="font-size: 12px; font-weight: 400">版本</span>
              </el-button>
              <el-button type="primary" text @click="handleOpenChunkPage(scope.row.id)">
                <el-icon><Grid /></el-icon>
                <span style="font-size: 12px; font-weight: 400">分块</span>
              </el-button>
              <el-button type="info" text @click="handleDocumentMetadata(scope.row.id)">
                <el-icon><Tickets /></el-icon>
                <span style="font-size: 12px; font-weight: 400">元数据</span>
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sizeStr" label="大小" align="center"/>
        <el-table-column prop="ownerName" label="所有者" align="center" />
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column fixed="right" label="操作" width="300" align="center">
          <template #default="scope">
            <el-button type="primary" text @click="handleUploadNewVersion(scope.row)">
              <el-icon><Upload /></el-icon>
              <span style="font-size: 12px; font-weight: 400">上传新版本</span>
            </el-button>
            <el-button v-if="isEditableDocument(scope.row)" type="warning" text @click="handleEditDocument(scope.row)">
              <el-icon><EditPen /></el-icon>
              <span style="font-size: 12px; font-weight: 400">编辑</span>
            </el-button>
            <el-button type="success" text @click="handleOpenUpdateDocumentForm(scope.row)">
              <el-icon><Edit /></el-icon>
              <span style="font-size: 12px; font-weight: 400">修改</span>
            </el-button>
            <el-button type="danger" text @click="handleDeleteDocument(scope.row.id)">
              <el-icon><Delete /></el-icon>
              <span style="font-size: 12px; font-weight: 400">删除</span>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <div>
      <el-pagination
          :current-page="documentQuery.pageNo"
          :page-size="documentQuery.pageSize"
          :page-sizes="pageSizes"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handlePageChangeSize"
          @current-change="handlePageChangeNo"
      />
    </div>

    <!-- 文档修改表单 -->
    <el-drawer
        v-model="documentFormVisible"
        :title="documentFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseDocumentForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="documentForm"
          label-width="auto"
          :rules="documentFormRules"
          ref="documentFormRef"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="documentForm.name" placeholder="请输入名称">
            <template v-if="documentForm.ext" #append>.{{ documentForm.ext }}</template>
          </el-input>
        </el-form-item>
        <el-form-item label="大小" prop="sizeStr">
          <el-input v-model="documentForm.sizeStr" placeholder="请输入大小" readonly />
        </el-form-item>
        <el-form-item label="所有者" prop="ownerName">
          <el-input v-model="documentForm.ownerName" placeholder="请输入所有者" readonly />
        </el-form-item>
        <el-form-item :label="isGraph ? '所属知识图谱' : '所属知识库'" prop="kbName">
          <el-input v-model="documentForm.kbName" :placeholder="isGraph ? '所属知识图谱' : '所属知识库'" readonly />
        </el-form-item>
        <el-form-item label="创建时间" prop="createdDt">
          <el-input v-model="documentForm.createdDt" placeholder="创建时间" readonly />
        </el-form-item>
        <el-form-item label="修改人" prop="updatedByName">
          <el-input v-model="documentForm.updatedByName" placeholder="修改人" readonly />
        </el-form-item>
        <el-form-item label="修改时间" prop="updatedDt">
          <el-input v-model="documentForm.updatedDt" placeholder="修改时间" readonly />
        </el-form-item>
      </el-form>
      <div class="drawer-tips">
        提示：修改文档名称不会影响文件本身，但会影响检索关键词匹配，请谨慎操作。
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmitDocumentForm">保存</el-button>
          <el-button @click="handleCloseDocumentForm">取消</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 文件上传 -->
    <document-upload v-model="uploadDocumentFormVisible" :prt-id="prtId" :doc-id="uploadDocId" :ext="uploadDocExt" @success="handleGetDocumentList" />

    <!-- 文件事件详情 -->
    <document-event v-model="documentEventVisible" :doc-id="documentEventDocId" />

    <!-- 文档版本管理 -->
    <document-version v-model="documentVersionVisible" :doc-id="documentVersionDocId" @success="handleGetDocumentList" />

    <!-- 元数据表单列表 -->
    <el-drawer
        v-model="metadataVisible"
        title="元数据表单列表"
        direction="ltr"
        size="24%"
        :close-on-click-modal="false"
    >
      <div class="metadata-list">
        <div
            v-for="item in metadataList"
            :key="item.id"
            class="metadata-item"
            @click="handleOpenMetadataForm(item.id)"
        >
          <el-icon class="metadata-item-icon"><Tickets /></el-icon>
          <span class="metadata-item-name">{{ item.name }}</span>
        </div>
      </div>
      <div>
        <el-pagination
            small
            :current-page="metadataQuery.pageNo"
            :page-size="metadataQuery.pageSize"
            :page-sizes="pageMetadataSizes"
            :background="true"
            layout="total, sizes, prev, pager, next"
            :total="metadataTotal"
            @size-change="handleMetadataPageChangeSize"
            @current-change="handleMetadataPageChangeNo"
        />
      </div>
    </el-drawer>

    <!-- 元数据表单 -->
    <el-drawer
        v-model="metadataFormVisible"
        title="元数据表单"
        direction="ltr"
        size="40%"
        :close-on-click-modal="false"
    >
      <form-view
          :form="formJson"
          v-if="metadataFormVisible"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmitMetadataForm">保存</el-button>
          <el-button @click="metadataFormVisible = false">取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageDocumentListAPI, updateDocumentAPI, queryDocumentDetailAPI, deleteDocumentAPI,
  batchDeleteDocumentAPI, batchResetDocumentEventAPI
} from '@/api/dms/document.js'
import { queryKnowledgeDetailAPI } from '@/api/kb/knowledge.js'
import { queryGraphDetailAPI } from '@/api/kg/graph.js'
import FormView from '@/components/FormView/index.vue'
import DocumentIcon from '@/components/DocumentIcon/index.vue'
import DocumentEvent from '@/components/DocumentEvent/index.vue'
import DocumentUpload from '@/components/DocumentUpload/index.vue'
import DocumentVersion from '@/components/DocumentVersion/index.vue'
import { pageFormListAPI, queryFormJsonAPI } from '@/api/form/form.js'
import { detailFormValueAPI, saveFormValueAPI } from '@/api/form/formValue.js'
import {
  ArrowLeft, Collection, Connection, Search, Refresh, DocumentAdd,
  HelpFilled, Grid, Edit, Delete, Tickets, Files, Upload, EditPen
} from '@element-plus/icons-vue'
import { getDocumentCategory, DOCUMENT_CATEGORY } from '@/utils/documentUtil.js'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// 场景识别：文档列表统一传 prtId，按上级 id 末两位判断类型（14 知识库 / 16 知识图谱）
const prtId = computed(() => route.query.prtId)
const isGraph = computed(() => String(prtId.value ?? '').endsWith('16'))

// 头部标题图标与文本
const parentName = ref('')
const headerIcon = computed(() => isGraph.value ? Connection : Collection)
const headerTitle = computed(() => parentName.value || (isGraph.value ? '知识图谱文档' : '知识库文档'))

// 文档列表查询
const documentQuery = ref({
  pageNo: 1,
  pageSize: 30,
  name: undefined,
  prtId: undefined
})
const documentList = ref([])
const total = ref(0)
const pageSizes = [30, 50, 100]

// 文档修改表单
const documentFormVisible = ref(false)
const documentFormTitle = ref('')
const documentForm = ref({
  id: undefined,
  name: undefined,
  ext: undefined,
  sizeStr: undefined,
  ownerName: undefined,
  kbName: undefined,
  createdDt: undefined,
  updatedByName: undefined,
  updatedDt: undefined
})
const documentFormRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入名称' }],
  sizeStr: [{ required: true, trigger: 'blur', message: '请输入大小' }],
  ownerName: [{ required: true, trigger: 'blur', message: '请输入所有者' }]
}

// 上传相关
const uploadDocumentFormVisible = ref(false)
const uploadDocId = ref(undefined)
const uploadDocExt = ref(undefined)

// 事件详情
const documentEventVisible = ref(false)
const documentEventDocId = ref(undefined)

// 版本管理
const documentVersionVisible = ref(false)
const documentVersionDocId = ref(undefined)

// 元数据相关
const metadataQuery = ref({ pageNo: 1, pageSize: 30, type: 3 })
const metadataList = ref([])
const pageMetadataSizes = [15, 30, 50]
const metadataTotal = ref(0)
const metadataVisible = ref(false)
const metadataFormVisible = ref(false)
const currentDocId = ref(undefined)
const currentFormId = ref(undefined)
const formJson = ref(undefined)

// 批量操作相关
const selectedIds = ref([])

// 路由参数变化时重新加载
watch(prtId, () => {
  documentQuery.value.prtId = prtId.value
  loadParentDetail()
  handleGetDocumentList()
}, { immediate: true })

/**
 * 加载所属知识库 / 知识图谱详情，用于头部标题展示
 */
function loadParentDetail() {
  if (!prtId.value) {
    parentName.value = ''
    return
  }
  if (isGraph.value) {
    queryGraphDetailAPI({ id: prtId.value }).then(res => {
      if (res.code === 200 && res.data) {
        parentName.value = res.data.name || ''
      }
    })
  } else {
    queryKnowledgeDetailAPI({ id: prtId.value }).then(res => {
      if (res.code === 200 && res.data) {
        parentName.value = res.data.name || ''
      }
    })
  }
}

/**
 * 返回上一层
 */
function handleBack() {
  router.back();
}

/**
 * 查询文档列表（按当前知识库 / 知识图谱过滤）
 */
function handleGetDocumentList() {
  documentQuery.value.prtId = prtId.value
  pageDocumentListAPI(documentQuery.value).then(res => {
    documentList.value = res.data.rows
    total.value = res.data.total
  })
}

/**
 * 重置查询条件
 */
function handleResetDocumentQuery() {
  documentQuery.value.pageNo = 1
  documentQuery.value.pageSize = 30
  documentQuery.value.name = undefined
  handleGetDocumentList()
}

/**
 * 分页变更
 */
function handlePageChangeSize(pageSize) {
  documentQuery.value.pageSize = pageSize
  handleGetDocumentList()
}

function handlePageChangeNo(pageNo) {
  documentQuery.value.pageNo = pageNo
  handleGetDocumentList()
}

/**
 * 上传文档
 */
function handleUploadDocument() {
  uploadDocId.value = undefined
  uploadDocExt.value = undefined
  uploadDocumentFormVisible.value = true
}

/**
 * 上传新版本
 */
function handleUploadNewVersion(row) {
  uploadDocId.value = row.id
  uploadDocExt.value = row.ext
  uploadDocumentFormVisible.value = true
}

/**
 * 提交文档修改表单
 */
function handleSubmitDocumentForm() {
  proxy.$refs.documentFormRef.validate(valid => {
    if (!valid) return
    if (!documentForm.value.id) {
      ElMessage.warning('文档id不存在')
      return
    }
    // 拼接后缀，后缀不允许修改
    const fullName = documentForm.value.ext
        ? `${documentForm.value.name}.${documentForm.value.ext}`
        : documentForm.value.name
    const data = { id: documentForm.value.id, name: fullName }
    updateDocumentAPI(data).then(res => {
      if (res.code !== 200) return
      ElMessage.success('文档修改成功')
      handleCloseDocumentForm()
      handleGetDocumentList()
    })
  })
}

/**
 * 关闭文档表单
 */
function handleCloseDocumentForm() {
  documentForm.value.id = undefined
  documentForm.value.name = undefined
  documentForm.value.ext = undefined
  documentForm.value.sizeStr = undefined
  documentForm.value.ownerName = undefined
  documentForm.value.kbName = undefined
  documentForm.value.createdDt = undefined
  documentForm.value.updatedByName = undefined
  documentForm.value.updatedDt = undefined
  documentFormTitle.value = ''
  documentFormVisible.value = false
}

/**
 * 删除文档
 */
function handleDeleteDocument(id) {
  ElMessageBox.confirm('是否确定删除此条文档?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteDocumentAPI({ id }).then(() => {
      handleGetDocumentList()
      ElMessage.success('删除文档成功')
    })
  }).catch(() => {})
}

/**
 * 打开修改文档表单
 */
function handleOpenUpdateDocumentForm(row) {
  queryDocumentDetailAPI({ id: row.id }).then(res => {
    const ext = res.data.ext
    // 拆分主名与后缀，后缀在输入框 append 区只读展示，禁止修改
    const fullName = res.data.name || ''
    const mainName = ext && fullName.endsWith('.' + ext)
        ? fullName.slice(0, -(ext.length + 1))
        : fullName
    documentForm.value.id = res.data.id
    documentForm.value.name = mainName
    documentForm.value.ext = ext
    documentForm.value.sizeStr = res.data.sizeStr
    documentForm.value.ownerName = res.data.ownerName
    documentForm.value.kbName = res.data.kbName
    documentForm.value.createdDt = res.data.createdDt
    documentForm.value.updatedByName = res.data.updatedByName
    documentForm.value.updatedDt = res.data.updatedDt
    documentFormTitle.value = '修改文档'
    documentFormVisible.value = true
  })
}

/**
 * 展示文档事件
 */
function handleDocumentEvent(docId) {
  documentEventDocId.value = docId
  documentEventVisible.value = true
}

/**
 * 展示文档版本
 */
function handleDocumentVersion(docId) {
  documentVersionDocId.value = docId
  documentVersionVisible.value = true
}

/**
 * 展示文档元数据列表
 */
function handleDocumentMetadata(docId) {
  detailFormValueAPI({ objId: docId }).then(res => {
    if (res.code !== 200) return
    currentDocId.value = docId
    if (res.data === undefined) {
      handleGetMetadataList(docId)
    } else {
      const values = res.data.values
      const formJsonResult = JSON.parse(res.data.formJson)
      const widgetList = formJsonResult.widgetList
      const codes = values.map(value => value.code)
      widgetList.forEach(widget => {
        const index = codes.indexOf(widget.config.code)
        if (index === -1) return
        widget.config.value = values[index].value
        widget.config.showValue = values[index].showValue
      })
      formJson.value = formJsonResult
      currentFormId.value = res.data.formId
      metadataFormVisible.value = true
    }
  })
}

/**
 * 查询元数据列表
 */
function handleGetMetadataList() {
  pageFormListAPI(metadataQuery.value).then(res => {
    metadataList.value = res.data.rows
    metadataTotal.value = res.data.total
    metadataVisible.value = true
  })
}

/**
 * 打开元数据表单
 */
function handleOpenMetadataForm(formId) {
  queryFormJsonAPI({ id: formId }).then(res => {
    if (!res.data) return
    formJson.value = JSON.parse(res.data)
    currentFormId.value = formId
    metadataFormVisible.value = true
  })
}

/**
 * 提交元数据表单
 */
function handleSubmitMetadataForm() {
  const list = JSON.parse(JSON.stringify(formJson.value)).widgetList
  const values = []
  list.forEach(widget => {
    const config = widget.config
    values.push({
      code: config.code,
      type: widget.type,
      value: config.value,
      showValue: config.showValue
    })
  })
  saveFormValueAPI({
    objId: currentDocId.value,
    formId: currentFormId.value,
    values
  }).then(res => {
    if (res.code !== 200) return
    ElMessage.success('元数据提交成功')
    formJson.value = undefined
    currentFormId.value = undefined
    metadataFormVisible.value = false
    metadataVisible.value = false
  })
}

/**
 * 元数据分页
 */
function handleMetadataPageChangeSize(pageSize) {
  metadataQuery.value.pageSize = pageSize
}

function handleMetadataPageChangeNo(pageNo) {
  metadataQuery.value.pageNo = pageNo
}

/**
 * 打开文档分块页（新开标签页）
 */
function handleOpenChunkPage(docId) {
  const { href } = router.resolve({ path: '/document/chunk', query: { id: docId } })
  window.open(href, '_blank')
}

/**
 * 打开文档预览（新开标签页）
 */
function handlePreviewDocument(row) {
  const { href } = router.resolve({ path: '/document/preview', query: { id: row.id } })
  window.open(href, '_blank')
}

/**
 * 是否支持在线编辑的文档
 */
function isEditableDocument(row) {
  const category = getDocumentCategory(row.ext)
  return category === DOCUMENT_CATEGORY.MARKDOWN || category === DOCUMENT_CATEGORY.TXT
      || category === DOCUMENT_CATEGORY.WORD || category === DOCUMENT_CATEGORY.EXCEL || category === DOCUMENT_CATEGORY.PPT
}

/**
 * 打开文档在线编辑（新开标签页）
 */
function handleEditDocument(row) {
  const { href } = router.resolve({ path: '/document/editor', query: { id: row.id } })
  window.open(href, '_blank')
}

/**
 * 打开文档对应的知识图谱详情页（新开标签页，仅图谱文档场景）
 * @param docId 文档 ID
 */
function handleOpenGraphDetail(docId) {
  const { href } = router.resolve({ path: '/graph/detail', query: { docId } })
  window.open(href, '_blank')
}

/**
 * 表格选择变化
 */
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

/**
 * 批量删除
 */
function handleBatchDelete() {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个文档吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    batchDeleteDocumentAPI(selectedIds.value).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        selectedIds.value = []
        handleGetDocumentList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
  }).catch(() => {})
}

/**
 * 批量重置事件
 */
function handleBatchEvent() {
  ElMessageBox.confirm(`确定要重置选中的 ${selectedIds.value.length} 个文档的事件吗？`, '确认重置事件', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    batchResetDocumentEventAPI(selectedIds.value).then(res => {
      if (res.code === 200) {
        ElMessage.success('重置事件成功')
        selectedIds.value = []
      } else {
        ElMessage.error(res.message || '重置事件失败')
      }
    })
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.kd-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.kd-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.kd-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 18px;
  font-weight: 700;
  color: $color-text-primary;
}

.kd-header-icon {
  font-size: 22px;
  color: $color-primary;
}

.kd-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.document-name-link {
  color: $color-text-primary;
  transition: color $transition-fast;
  &:hover {
    color: $color-primary;
  }
}

// 行内快捷操作：hover 该行时才显示
.row-actions {
  display: flex;
  justify-content: center;
  opacity: 0;
  transition: opacity $transition-fast;
}

:deep(.el-table__row:hover) .row-actions {
  opacity: 1;
}

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

// 元数据表单列表
.metadata-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-sm;
  height: calc(100vh - 140px);
  overflow-y: auto;

  .metadata-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    font-size: 14px;
    color: $color-text-primary;
    border-radius: $border-radius-sm;
    cursor: pointer;
    transition: $transition-fast;

    .metadata-item-icon {
      flex-shrink: 0;
      font-size: 16px;
    }

    .metadata-item-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &:hover {
      color: $color-primary;
      background-color: $color-primary-light;

      .metadata-item-icon {
        color: $color-primary;
      }
    }
  }
}
</style>
