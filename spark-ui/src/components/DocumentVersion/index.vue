<template>
  <el-drawer
    v-model="drawerVisible"
    title="文档版本"
    direction="ltr"
    size="44%"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <div class="drawer-tips">
      提示：回滚版本后，系统将自动重新解析、分块、向量化文档内容。
    </div>
    <!-- 版本列表 -->
    <el-table :data="versionList" height="calc(100vh - 240px)">
      <el-table-column label="版本" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.currentFlag ? 'success' : 'info'">
            V{{ scope.row.versionNo }}{{ scope.row.currentFlag ? ' 当前' : '' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="180" align="left" show-overflow-tooltip />
      <el-table-column prop="sizeStr" label="大小" width="90" align="center" />
      <el-table-column prop="createdByName" label="上传人" width="100" align="center" />
      <el-table-column prop="createdDt" label="时间" width="160" align="center" />
      <el-table-column fixed="right" label="操作" width="170" align="center">
        <template #default="scope">
          <el-button type="primary" text @click="handleDownload(scope.row)">下载</el-button>
          <template v-if="!scope.row.currentFlag">
            <el-button type="warning" text @click="handleRollback(scope.row)">回滚</el-button>
            <el-button type="danger" text @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <el-pagination
      :current-page="versionQuery.pageNo"
      :page-size="versionQuery.pageSize"
      :background="true"
      layout="total, prev, pager, next"
      :total="total"
      @current-change="handlePageChangeNo"
    />
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { downloadDocumentAPI } from '@/api/dms/document.js'
import {
  pageDocumentVersionListAPI,
  rollbackDocumentVersionAPI, deleteDocumentVersionAPI, downloadDocumentVersionAPI
} from '@/api/dms/documentVersion.js'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  docId: { type: [String, Number], default: undefined }
})

const emit = defineEmits(['update:modelValue', 'success'])

const drawerVisible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

// 版本列表查询
const versionQuery = ref({ pageNo: 1, pageSize: 15 })
const versionList = ref([])
const total = ref(0)

watch(() => props.modelValue, (val) => {
  if (val) {
    versionQuery.value.pageNo = 1
    handleGetVersionList()
  }
})

/**
 * 查询版本列表
 */
function handleGetVersionList() {
  pageDocumentVersionListAPI({ ...versionQuery.value, docId: props.docId }).then(res => {
    versionList.value = res.data.rows
    total.value = res.data.total
  })
}

/**
 * 分页变更
 */
function handlePageChangeNo(pageNo) {
  versionQuery.value.pageNo = pageNo
  handleGetVersionList()
}

/**
 * 下载版本文件
 * @param row 版本行
 */
function handleDownload(row) {
  const request = row.currentFlag
      ? downloadDocumentAPI({ id: props.docId, ext: row.ext })
      : downloadDocumentVersionAPI({ id: row.id, ext: row.ext })
  request.then(blob => {
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }).catch(() => {
    ElMessage.error('版本文件下载失败')
  })
}

/**
 * 回滚到历史版本
 * @param row 版本行
 */
function handleRollback(row) {
  ElMessageBox.confirm(`确定回滚到版本 V${row.versionNo} 吗？当前版本将保存为历史版本。`, '确认回滚', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    rollbackDocumentVersionAPI({ id: row.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('回滚成功')
      handleGetVersionList()
      emit('success')
    })
  }).catch(() => {})
}

/**
 * 删除历史版本
 * @param row 版本行
 */
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除版本 V${row.versionNo} 吗？删除后不可恢复。`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteDocumentVersionAPI({ id: row.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('删除版本成功')
      handleGetVersionList()
    })
  }).catch(() => {})
}

/**
 * 关闭抽屉
 */
function handleClose() {
  drawerVisible.value = false
}
</script>

<style scoped lang="scss">
.drawer-tips {
  margin-bottom: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background-color: $color-primary-light;
  border-left: 3px solid $color-primary;
  border-radius: $border-radius-sm;
  color: $color-text-secondary;
  font-size: 12px;
  line-height: 1.6;
}
</style>
