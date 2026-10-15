<template>
  <!-- MCP token 维护弹窗 -->
  <el-dialog
    v-model="visible"
    title="MCP Token"
    width="1100"
    :before-close="closeDialog"
  >
    <div class="mcp-token-toolbar">
      <el-button type="primary" @click="openCreateForm">
        <el-icon><Plus /></el-icon>新增 MCP Token
      </el-button>
    </div>
    <el-table :data="mcpKeyList" v-loading="loading" height="400">
      <el-table-column prop="name" label="名称" min-width="120" align="left"/>
      <el-table-column prop="keyValue" label="密钥" min-width="220" align="left">
        <template #default="scope">
          <span class="key-value">{{ scope.row.keyValue }}</span>
          <el-button type="primary" text @click="copyText(scope.row.keyValue)">复制</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="statusName" label="状态" width="80" align="center"/>
      <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
      <el-table-column label="最近使用" width="160" align="center">
        <template #default="scope">
          {{ scope.row.lastUsedDt || '暂无' }}
        </template>
      </el-table-column>
      <el-table-column prop="useCount" label="使用次数" width="90" align="center"/>
      <el-table-column fixed="right" label="操作" width="160" align="center">
        <template #default="scope">
          <el-button type="success" text @click="copyText(buildConfig(scope.row))">复制</el-button>
          <el-button type="danger" text @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 token 表单弹窗 -->
    <el-dialog
      v-model="formVisible"
      title="新增 MCP Token"
      width="480"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="mcpKeyForm"
          :rules="formRules"
          label-width="90px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
              v-model="mcpKeyForm.name"
              placeholder="请输入名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
              v-model="mcpKeyForm.expireDt"
              type="datetime"
              placeholder="不选择表示永不过期"
              style="width: 100%"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="formVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createMcpKeyAPI, deleteMcpKeyAPI, myMcpKeyListAPI } from '@/api/llm/mcpKey.js'

const props = defineProps({
  // 弹窗显隐
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

// 弹窗显隐双向绑定
const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const mcpKeyList = ref([])
const loading = ref(false)

// 新增表单
const formVisible = ref(false)
const formRef = ref(null)
const mcpKeyForm = ref({ name: '', expireDt: null })
const formRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

// 弹窗打开时加载密钥列表
watch(() => props.modelValue, val => {
  if (val) {
    getList()
  }
})

/**
 * 查询当前用户的密钥列表
 */
function getList() {
  loading.value = true
  myMcpKeyListAPI().then(res => {
    if (res.code === 200) {
      mcpKeyList.value = res.data || []
    }
    loading.value = false
  })
}

/**
 * 打开新增表单
 */
function openCreateForm() {
  mcpKeyForm.value = { name: '', expireDt: null }
  formVisible.value = true
}

/**
 * 提交新增
 */
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    createMcpKeyAPI(mcpKeyForm.value).then(res => {
      if (res.code !== 200) return
      ElMessage.success('创建成功')
      formVisible.value = false
      getList()
    })
  })
}

/**
 * 删除二次确认
 * @param item 密钥
 */
function handleDelete(item) {
  ElMessageBox.confirm('是否确定删除此 token?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteMcpKeyAPI({ id: item.id }).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    })
  }).catch(() => {})
}

/**
 * 构造粘贴到本地 AI 客户端的接入配置
 * @param item 密钥
 */
function buildConfig(item) {
  const config = {
    mcpServers: {
      spark: {
        type: 'http',
        url: process.env.BASE_HTTP_API + '/mcp',
        headers: {
          Authorization: 'Bearer ' + item.keyValue
        }
      }
    }
  }
  return JSON.stringify(config, null, 2)
}

/**
 * 复制文本到剪贴板
 * @param text 文本
 */
function copyText(text) {
  if (!text) {
    return
  }
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('已复制')
    }).catch(() => {
      ElMessage.warning('复制失败 请手动复制')
    })
    return
  }
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败 请手动复制')
  }
  document.body.removeChild(textarea)
}

/**
 * 关闭弹窗
 */
function closeDialog() {
  visible.value = false
}
</script>

<style scoped lang="scss">
.mcp-token-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: $spacing-md;
}

.key-value {
  margin-right: $spacing-sm;
  font-family: monospace;
}
</style>
