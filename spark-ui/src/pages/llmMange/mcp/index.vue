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
        <el-button type="primary" @click="handleOpenCreate">
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
          :icon="Connection"
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

    <!-- MCP 详情抽屉 -->
    <el-drawer
        v-model="detailVisible"
        :title="current?.name || 'MCP 服务详情'"
        direction="ltr"
        size="40%"
    >
      <template v-if="current">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="编号">{{ current.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ current.statusName }}
          </el-descriptions-item>
          <el-descriptions-item label="厂商">{{ current.providerName }}</el-descriptions-item>
          <el-descriptions-item label="传输类型">{{ current.transportName || current.transport }}</el-descriptions-item>
          <el-descriptions-item label="连接信息">
            <span v-if="current.transport === 1">{{ current.command || '-' }}</span>
            <span v-else-if="current.transport === 2">{{ current.url || '-' }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="超时(秒)">{{ current.timeout }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ current.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- 新增 / 修改 MCP 服务表单抽屉 -->
    <el-drawer
        v-model="formVisible"
        :title="formTitle"
        direction="ltr"
        size="40%"
        :before-close="handleCloseForm"
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
        <el-form-item label="厂商" prop="providerId" v-if="mcpForm.transport === 2">
          <el-select
              v-model="mcpForm.providerId"
              placeholder="请选择厂商"
              style="width: 100%"
              filterable
          >
            <el-option
                v-for="item in providerOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="传输类型" prop="transport">
          <el-radio-group v-model="mcpForm.transport" @change="handleTransportChange">
            <el-radio :label="1">STDIO</el-radio>
            <el-radio :label="2">SSE</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="mcpForm.transport === 1">
          <el-form-item label="命令" prop="command">
            <el-input
                v-model="mcpForm.command"
                placeholder="如：npx"
                maxlength="100"
            />
          </el-form-item>
          <el-form-item label="参数">
            <div class="mcp-dynamic-list">
              <div
                  v-for="(arg, idx) in mcpForm.args"
                  :key="'arg-' + idx"
                  class="mcp-dynamic-item"
              >
                <el-input
                    v-model="mcpForm.args[idx]"
                    placeholder="请输入参数"
                    maxlength="100"
                />
                <el-button type="danger" text @click="handleRemoveArg(idx)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button type="primary" text @click="handleAddArg">
                <el-icon><Plus /></el-icon>添加参数
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="环境变量">
            <div class="mcp-dynamic-list">
              <div
                  v-for="(envItem, idx) in mcpForm.env"
                  :key="'env-' + idx"
                  class="mcp-dynamic-item mcp-env-item"
              >
                <el-input
                    v-model="envItem.key"
                    placeholder="变量名"
                    maxlength="50"
                />
                <el-input
                    v-model="envItem.value"
                    placeholder="变量值"
                    maxlength="200"
                />
                <el-button type="danger" text @click="handleRemoveEnv(idx)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button type="primary" text @click="handleAddEnv">
                <el-icon><Plus /></el-icon>添加变量
              </el-button>
            </div>
          </el-form-item>
        </template>
        <template v-else-if="mcpForm.transport === 2">
          <el-form-item label="URL" prop="url">
            <el-input
                v-model="mcpForm.url"
                placeholder="如：http://127.0.0.1:8080/sse"
                maxlength="200"
            />
          </el-form-item>
        </template>
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
            <div>传输类型选择 SSE 时需选择所属厂商并填写 URL，厂商请先在厂商管理中维护；</div>
            <div>传输类型选择 STDIO 时需填写命令与参数、环境变量，无需选择厂商；</div>
            <div>参数与环境变量支持动态增删，提交前会自动序列化为 JSON 字符串保存；</div>
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
  testConnectionMcpAPI
} from '@/api/llm/mcp.js'
import { pageProviderListAPI } from '@/api/llm/provider.js'
import { Connection, Search, Plus, Delete } from '@element-plus/icons-vue'
import InfoCard from '@/components/InfoCard/index.vue'

const mcpList = ref([])
const total = ref(0)
const pageSizes = [10, 30, 50]
const keyword = ref('')
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 10,
})

// 详情抽屉
const detailVisible = ref(false)
const current = ref(null)

// 表单
const formVisible = ref(false)
const formTitle = ref('')
const formRef = ref(null)
const mcpForm = ref(createEmptyForm())
const formRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  providerId: [{ required: true, validator: validateProviderId, trigger: 'change' }],
  transport: [{ required: true, message: '请选择传输类型', trigger: 'change' }],
  command: [{ required: true, message: '请输入命令', trigger: 'blur' }],
  url: [{ required: true, message: '请输入 URL', trigger: 'blur' }],
  timeout: [{ required: true, message: '请输入超时时间', trigger: 'blur' }]
}

// 厂商选项
const providerOptions = ref([])

// 测试中状态映射，按 id 维度控制 loading
const testingMap = reactive({})

let searchTimer = null

onMounted(() => {
  handleGetList()
  loadProviderOptions()
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
    providerId: undefined,
    transport: 2,
    command: '',
    args: [],
    env: [],
    url: '',
    timeout: 30,
    description: '',
    status: 1
  }
}

/**
 * 厂商校验：仅 SSE 传输类型下必填
 * @param rule
 * @param value
 * @param callback
 */
function validateProviderId(rule, value, callback) {
  if (mcpForm.value.transport === 2 && !value) {
    callback(new Error('请选择厂商'))
  } else {
    callback()
  }
}

/**
 * 加载厂商选项列表（一次性拉取全量）
 */
function loadProviderOptions() {
  pageProviderListAPI({ pageNo: 1, pageSize: 1000 }).then(res => {
    if (res.code === 200 && res.data) {
      providerOptions.value = res.data.rows || []
    }
  })
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
    { key: 'detail', label: '详情', icon: 'View', onClick: () => handleOpenDetail(item) },
    {
      key: 'test',
      label: '测试',
      icon: 'Tools',
      loading: !!testingMap[item.id],
      onClick: () => handleTestConnection(item)
    },
    { key: 'edit', label: '修改', icon: 'Edit',onClick: () => handleOpenUpdate(item) },
    { key: 'delete', label: '删除',icon: 'Delete', onClick: () => handleDelete(item) }
  ]
}

/**
 * 打开详情抽屉
 * @param item
 */
function handleOpenDetail(item) {
  current.value = item
  detailVisible.value = true
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
      providerId: data.providerId,
      transport: data.transport,
      command: data.command || '',
      args: parseArgs(data.args),
      env: parseEnv(data.env),
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
function handleTestConnection(item) {
  if (testingMap[item.id]) return
  testingMap[item.id] = true
  testConnectionMcpAPI({ id: item.id }).then(res => {
    if (res.code === 200) {
      ElMessage.success('连接成功')
    } else if (res.code === 2203) {
      ElMessage.error('连接失败')
    }
  }).finally(() => {
    testingMap[item.id] = false
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
 * 组装提交数据，args/env 序列化为 JSON 字符串
 */
function buildSubmitData() {
  const form = mcpForm.value
  const data = {
    name: form.name,
    transport: form.transport,
    timeout: form.timeout,
    description: form.description,
    status: form.status
  }
  if (form.id) {
    data.id = form.id
  }
  if (form.transport === 2) {
    data.providerId = form.providerId
    data.url = form.url
  } else if (form.transport === 1) {
    data.command = form.command
    // 过滤空串后序列化为 JSON 数组字符串
    data.args = JSON.stringify(
        form.args.filter(a => a !== null && a !== undefined && a !== '')
    )
    // 将键值对数组还原为对象后序列化为 JSON 对象字符串
    const envObj = form.env
        .filter(envItem => envItem.key)
        .reduce((obj, envItem) => {
          obj[envItem.key] = envItem.value
          return obj
        }, {})
    data.env = JSON.stringify(envObj)
  }
  return data
}

/**
 * 切换传输类型时清空对端字段，避免脏数据
 */
function handleTransportChange() {
  if (mcpForm.value.transport === 1) {
    mcpForm.value.url = ''
    mcpForm.value.providerId = undefined
  } else {
    mcpForm.value.command = ''
    mcpForm.value.args = []
    mcpForm.value.env = []
  }
  // 切换传输类型后重新校验厂商字段
  if (formRef.value) {
    formRef.value.validateField('providerId')
  }
}

/**
 * 新增参数项
 */
function handleAddArg() {
  mcpForm.value.args.push('')
}

/**
 * 移除参数项
 * @param idx
 */
function handleRemoveArg(idx) {
  mcpForm.value.args.splice(idx, 1)
}

/**
 * 新增环境变量项
 */
function handleAddEnv() {
  mcpForm.value.env.push({ key: '', value: '' })
}

/**
 * 移除环境变量项
 * @param idx
 */
function handleRemoveEnv(idx) {
  mcpForm.value.env.splice(idx, 1)
}

/**
 * 解析 args JSON 字符串为数组
 * @param argsStr
 */
function parseArgs(argsStr) {
  try {
    const arr = JSON.parse(argsStr || '[]')
    return Array.isArray(arr) ? arr.map(v => String(v)) : []
  } catch (e) {
    return []
  }
}

/**
 * 解析 env JSON 字符串为键值对数组
 * @param envStr
 */
function parseEnv(envStr) {
  try {
    const obj = JSON.parse(envStr || '{}')
    if (obj && typeof obj === 'object' && !Array.isArray(obj)) {
      return Object.keys(obj).map(key => ({ key, value: obj[key] }))
    }
    return []
  } catch (e) {
    return []
  }
}
</script>

<style scoped lang="scss">
.mcp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
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
  height: calc(100vh - #{$nav-height} - 190px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}

.empty-grid {
  height: calc(100vh - #{$nav-height} - 190px);
}

.drawer-footer {
  padding: 0 $spacing-md;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.mcp-dynamic-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.mcp-dynamic-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
}

.mcp-env-item {
  :deep(.el-input) {
    flex: 1;
  }
}

.form-tip {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  font-size: 12px;
  line-height: 1.6;
}
</style>
