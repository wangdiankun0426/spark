<template>
  <el-drawer
      v-model="drawerVisible"
      :title="`密钥管理 - ${providerName}`"
      direction="ltr"
      size="72%"
      :before-close="handleCloseDrawer"
      :close-on-click-modal="false"
  >
    <div class="key-toolbar">
      <el-button v-hasPermi="['llm:providerKey:create']" type="primary" @click="handleOpenCreate">
        <el-icon><Plus /></el-icon>新增密钥
      </el-button>
      <el-button v-hasPermi="['llm:providerKey:query']" :loading="loading" @click="loadKeyList">刷新</el-button>
    </div>

    <el-table :data="keyList" v-loading="loading" height="calc(100vh - 240px)">
      <el-table-column prop="keyMask" label="密钥" min-width="220" align="left" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '已启用' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="冷却截止时间" width="180" align="center">
        <template #default="scope">
          {{ scope.row.cooldownUntil ? formatDate(scope.row.cooldownUntil) : '无' }}
        </template>
      </el-table-column>
      <el-table-column label="修改时间" width="180" align="center">
        <template #default="scope">
          {{ scope.row.updatedDt ? formatDate(scope.row.updatedDt) : '-' }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="140" align="center">
        <template #default="scope">
          <el-button v-hasPermi="['llm:providerKey:update']" type="primary" text @click="handleOpenUpdate(scope.row)">修改</el-button>
          <el-button v-hasPermi="['llm:providerKey:delete']" type="danger" text @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无密钥" :image-size="100" />
      </template>
    </el-table>

    <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        class="key-pagination"
        :total="total"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />

    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="420px"
        append-to-body
        :before-close="handleCloseForm"
        :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="密钥" prop="secretKey">
          <el-input
              v-model="form.secretKey"
              type="password"
              show-password
              autocomplete="new-password"
              :placeholder="form.id ? '留空则不更换密钥' : '请输入密钥'"
              maxlength="256"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
      </el-form>
      <el-alert
          class="key-form-alert"
          type="info"
          :closable="false"
          show-icon
          :title="form.id ? '留空则不更换密钥，保存后仅显示尾 4 位' : '密钥保存后仅显示尾 4 位，完整密钥不会回显'"
      />
      <template #footer>
        <div class="drawer-footer">
          <el-button v-if="!form.id" v-hasPermi="['llm:providerKey:create']" type="primary" @click="handleSubmit">保存</el-button>
          <el-button v-else v-hasPermi="['llm:providerKey:update']" type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>
  </el-drawer>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageProviderKeyListAPI,
  createProviderKeyAPI,
  updateProviderKeyAPI,
  deleteProviderKeyAPI
} from '@/api/llm/providerKey.js'
import { formatDate } from '@/utils/dateUtil.js'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  providerId: { type: [String, Number], default: undefined },
  providerName: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const drawerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})
const formTitle = computed(() => form.value.id ? '修改密钥' : '新增密钥')
const formRules = computed(() => ({
  secretKey: form.value.id ? [] : [{ required: true, trigger: 'blur', message: '请输入密钥' }]
}))

const loading = ref(false)
const keyList = ref([])
const total = ref(0)
const pageSizes = [10, 20, 50]
const query = ref({ pageNo: 1, pageSize: 10 })
const formVisible = ref(false)
const formRef = ref(null)
const form = ref(createEmptyForm())

watch(() => props.modelValue, visible => {
  if (visible) {
    query.value.pageNo = 1
    loadKeyList()
    return
  }
  handleCloseForm()
})

watch(() => props.providerId, () => {
  if (!props.modelValue) return
  query.value.pageNo = 1
  loadKeyList()
})

/**
 * 分页查询当前厂商的密钥列表
 */
function loadKeyList() {
  if (!props.providerId) return
  loading.value = true
  pageProviderKeyListAPI({
    providerId: props.providerId,
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize
  }).then(res => {
    if (res.code !== 200) return
    keyList.value = res.data.rows || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 切换每页条数后回到第一页
 * @param size 每页条数
 */
function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  loadKeyList()
}

/**
 * 切换页码查询密钥列表
 * @param pageNo 页码
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadKeyList()
}

/**
 * 构造不含历史密钥的空表单
 */
function createEmptyForm() {
  return {
    id: undefined,
    secretKey: '',
    status: 1
  }
}

/**
 * 打开新增密钥表单
 */
function handleOpenCreate() {
  form.value = createEmptyForm()
  formVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/**
 * 打开修改表单，仅回填状态，不回填掩码或完整密钥
 * @param row 密钥行
 */
function handleOpenUpdate(row) {
  form.value = {
    id: row.id,
    secretKey: '',
    status: row.status
  }
  formVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/**
 * 提交新增或修改，编辑留空时不提交密钥字段
 */
function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    const isUpdate = !!form.value.id
    const data = isUpdate
        ? {
          id: form.value.id,
          secretKey: form.value.secretKey || undefined,
          status: form.value.status
        }
        : {
          providerId: props.providerId,
          secretKey: form.value.secretKey,
          status: form.value.status
        }
    const api = isUpdate ? updateProviderKeyAPI : createProviderKeyAPI
    api(data).then(res => {
      if (res.code !== 200) return
      ElMessage.success(isUpdate ? '密钥修改成功' : '密钥创建成功')
      handleCloseForm()
      loadKeyList()
    })
  })
}

/**
 * 删除密钥并处理末页空数据
 * @param row 密钥行
 */
function handleDelete(row) {
  ElMessageBox.confirm('是否确定删除此条密钥？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteProviderKeyAPI({ id: row.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('密钥删除成功')
      if (keyList.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1
      }
      loadKeyList()
    })
  }).catch(() => {})
}

/**
 * 关闭表单并清空本地明文
 */
function handleCloseForm() {
  formVisible.value = false
  form.value = createEmptyForm()
  formRef.value?.clearValidate()
}

/**
 * 关闭密钥管理抽屉
 */
function handleCloseDrawer() {
  handleCloseForm()
  drawerVisible.value = false
}
</script>

<style scoped lang="scss">
.key-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
}

.key-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-md;
}

.key-form-alert {
  margin-top: $spacing-md;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
