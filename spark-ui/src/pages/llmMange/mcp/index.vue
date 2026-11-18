<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="mcp-header">
      <div class="mcp-header-left">
        <div class="mcp-header-title">
          MCP服务
        </div>
        <div class="mcp-header-subtitle">管理供智能体接入调用的 MCP 服务器，统一维护传输与连通参数</div>
      </div>
      <div class="mcp-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索服务名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
        />
        <el-button v-hasPermi="['llm:mcp:create']" type="primary" @click="handleOpenCreate">
          <el-icon><Plus /></el-icon>新增MCP服务
        </el-button>
      </div>
    </div>

    <!-- MCP 卡片网格 -->
    <div
        class="mcp-grid"
        v-if="mcpList.length"
    >
      <info-card
          v-for="item in mcpList"
          :key="item.id"
          :icon="Mcp"
          :title="item.name"
          :description="item.description || '暂无描述'"
          :disabled="item.status !== 1"
          :actions="cardActions(item)"
      />
    </div>
    <!-- 空状态 -->
    <el-empty
        class="empty-grid"
        v-else
        :description="keyword ? '未找到匹配的服务' : '暂无 MCP 服务'"
        :image-size="120"
    />
    <!-- 分页 -->
    <el-pagination
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="pageSizes"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
    />

    <!-- 新增 / 修改 MCP 服务表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="40%"
        :before-close="handleCloseForm"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="mcpForm"
          :rules="formRules"
          label-width="100px"
      >
        <el-form-item label="名称" prop="name">
          <el-input
              v-model="mcpForm.name"
              placeholder="请输入名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="独立密钥" prop="secretKey" :required="!mcpForm.id">
          <el-input
              v-model="mcpForm.secretKey"
              type="password"
              :placeholder="mcpForm.id ? '留空表示不更换独立密钥' : '请输入独立密钥'"
              maxlength="256"
              autocomplete="new-password"
              show-password
          />
        </el-form-item>
        <el-form-item label="URL" prop="url">
          <el-input
              v-model="mcpForm.url"
              placeholder="如：http://127.0.0.1:8080/mcp"
              maxlength="200"
          />
        </el-form-item>
        <el-form-item label="超时(秒)" prop="timeout">
          <el-input-number
              v-model="mcpForm.timeout"
              :min="1"
              :max="600"
              :step="1"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="mcpForm.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
              v-model="mcpForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入描述"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <div class="form-tip">
            <div>名称为必填项，建议使用能体现服务用途的标识，便于后续识别；</div>
            <div>新增需填写独立密钥，编辑留空表示不更换，独立密钥不会回显；</div>
            <div>超时时间取值范围 1-600 秒，建议根据目标服务响应速度合理设置；</div>
            <div>状态默认启用，禁用后该 MCP 服务器不会被业务调用。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCloseForm">取消</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 可用工具弹窗 -->
    <el-dialog
        v-model="toolsDialogVisible"
        :title="'可用工具 - ' + currentToolsName"
        width="1000px"
        append-to-body
    >
      <el-table
          :data="toolList"
          v-loading="toolsLoading"
          size="small"
          max-height="420"
      >
        <el-table-column
            label="工具方法名"
            width="200"
        >
          <template #default="scope">
            <span class="tool-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column
            label="描述"
            min-width="240"
            show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.description || '暂无描述' }}
          </template>
        </el-table-column>
        <template #empty>暂无可用工具</template>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageMcpListAPI,
  detailMcpAPI,
  createMcpAPI,
  updateMcpAPI,
  deleteMcpAPI,
  testMcpAPI,
  listMcpToolsAPI
} from '@/api/llm/mcp.js'
import { Search, Plus } from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'
import Mcp from "@/assets/icons/mcp.vue";

const mcpList = ref([])
const total = ref(0)
const pageSizes = [15, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 15,
})

// 表单
const formVisible = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const mcpForm = ref(createEmptyForm())
const formRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  secretKey: [{ validator: validateSecretKey, trigger: 'blur' }],
  url: [{ required: true, message: '请输入 URL', trigger: 'blur' }],
  timeout: [{ required: true, message: '请输入超时时间', trigger: 'blur' }]
}

// 测试中状态映射，按 id 维度控制 loading
const testingMap = reactive({})

// 可用工具弹窗
const toolsDialogVisible = ref(false)
const toolsLoading = ref(false)
const toolList = ref([])
const currentToolsName = ref('')
// 工具拉取中状态映射，按 id 维度控制 loading
const toolsLoadingMap = reactive({})

let searchTimer = null

onMounted(() => {
  handleGetList()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后回到第一页并重新查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1
    handleGetList()
  }, 300)
})

/**
 * 构造空表单对象
 */
function createEmptyForm() {
  return {
    id: undefined,
    name: '',
    secretKey: '',
    transport: 2,
    url: '',
    timeout: 30,
    description: '',
    status: 1
  }
}

/**
 * 校验独立密钥，新增必填，编辑允许留空
 */
function validateSecretKey(rule, value, callback) {
  if (!mcpForm.value.id && (!value || !value.trim())) {
    callback(new Error('请输入独立密钥'))
    return
  }
  callback()
}

/**
 * 分页查询 MCP 服务列表，携带名称关键字
 */
function handleGetList() {
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
  }
  if (keyword.value) {
    params.name = keyword.value
  }
  pageMcpListAPI(params).then(res => {
    if (res.code === 200) {
      mcpList.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  })
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  handleGetList()
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  handleGetList()
}

/**
 * 卡片底部工具栏按钮
 * @param item
 */
function cardActions(item) {
  return [
    {
      key: 'tools',
      label: '工具',
      icon: 'Menu',
      loading: !!toolsLoadingMap[item.id],
      perms: ['llm:mcp:listTools'],
      onClick: () => handleViewTools(item)
    },
    {
      key: 'test',
      label: '测试',
      icon: 'Tools',
      loading: !!testingMap[item.id],
      perms: ['llm:mcp:test'],
      onClick: () => handleTest(item)
    },
    { key: 'edit', label: '修改', icon: 'Edit', perms: ['llm:mcp:update'], onClick: () => handleOpenUpdate(item) },
    { key: 'delete', label: '删除', icon: 'Delete', perms: ['llm:mcp:delete'], onClick: () => handleDelete(item) }
  ]
}

/**
 * 打开新建表单
 */
function handleOpenCreate() {
  mcpForm.value = createEmptyForm()
  formTitle.value = '新增 MCP 服务'
  formVisible.value = true
}

/**
 * 打开修改表单，先拉取详情
 * @param item
 */
function handleOpenUpdate(item) {
  detailMcpAPI({ id: item.id }).then(res => {
    if (res.code !== 200) return
    const data = res.data
    mcpForm.value = {
      id: data.id,
      name: data.name,
      secretKey: '',
      transport: data.transport,
      url: data.url || '',
      timeout: data.timeout,
      description: data.description || '',
      status: data.status
    }
    formTitle.value = '修改 MCP 服务'
    formVisible.value = true
  })
}

/**
 * 测试连通性
 * @param item
 */
function handleTest(item) {
  if (testingMap[item.id]) return
  testingMap[item.id] = true
  testMcpAPI({ id: item.id }).then(res => {
    if (res.code === 200) {
      ElMessage.success('连接成功')
    } else if (res.code === 2202) {
      ElMessage.error('连接失败')
    }
  }).finally(() => {
    testingMap[item.id] = false
  })
}

/**
 * 实时拉取 MCP 服务可用工具并弹窗展示
 * @param item
 */
function handleViewTools(item) {
  if (toolsLoadingMap[item.id]) return
  toolsLoadingMap[item.id] = true
  currentToolsName.value = item.name
  toolList.value = []
  toolsDialogVisible.value = true
  toolsLoading.value = true
  listMcpToolsAPI({ id: item.id }).then(res => {
    if (res.code === 200) {
      toolList.value = res.data || []
    } else {
      toolsDialogVisible.value = false
    }
  }).finally(() => {
    toolsLoading.value = false
    toolsLoadingMap[item.id] = false
  })
}

/**
 * 删除二次确认
 * @param item
 */
function handleDelete(item) {
  ElMessageBox.confirm('是否确定删除此 MCP 服务?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteMcpAPI({ id: item.id }).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        // 删除当前页最后一条时回退上一页，避免停留在空页
        if (mcpList.value.length === 1 && query.value.pageNo > 1) {
          query.value.pageNo -= 1
        }
        handleGetList()
      }
    })
  }).catch(() => {})
}

/**
 * 关闭表单并重置
 */
function handleCloseForm() {
  mcpForm.value = createEmptyForm()
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  formVisible.value = false
}

/**
 * 提交表单
 */
function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    const data = buildSubmitData()
    const api = data.id ? updateMcpAPI : createMcpAPI
    api(data).then(res => {
      if (res.code === 200) {
        ElMessage.success(data.id ? '修改成功' : '创建成功')
        handleCloseForm()
        handleGetList()
      } else if (res.code === 2202) {
        ElMessage.error('名称已存在')
      }
    })
  })
}

/**
 * 组装提交数据
 */
function buildSubmitData() {
  const form = mcpForm.value
  const data = {
    name: form.name,
    transport: form.transport,
    secretKey: form.secretKey,
    url: form.url,
    timeout: form.timeout,
    description: form.description,
    status: form.status
  }
  if (form.id) {
    data.id = form.id
    if (!form.secretKey || !form.secretKey.trim()) {
      data.secretKey = undefined
    }
  }
  return data
}
</script>

<style scoped lang="scss">
.mcp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.mcp-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.mcp-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.mcp-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.mcp-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.mcp-grid {
  height: calc(100vh - #{$nav-height} - 175px);
  overflow-y: auto;
  padding-top: 6px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}

.empty-grid {
  height: calc(100vh - #{$nav-height} - 175px);
}

.drawer-footer {
  padding: 0 $spacing-md;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.form-tip {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}

.tool-name {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  color: $color-primary;
  word-break: break-all;
}
</style>
