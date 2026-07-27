<template>
  <div class="app-container">
    <!-- 操作按钮 -->
    <div>
      <el-button type="primary" @click="handleOpenCreate">
        <el-icon><Plus/></el-icon>新建MCP
      </el-button>
    </div>
    <!-- mcp列表 -->
    <div>
      <el-table
          :data="mcpList"
          height="calc(100vh - 165px)"
          highlight-current-row
      >
        <el-table-column prop="id" label="编号" width="80" align="center"/>
        <el-table-column prop="name" label="名称" min-width="160" align="center" show-overflow-tooltip/>
        <el-table-column prop="providerName" label="厂商" width="160" align="center" show-overflow-tooltip/>
        <el-table-column prop="transportName" label="传输类型" width="110" align="center"/>
        <el-table-column label="连接信息" min-width="300" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.transport === 1">{{ row.command || '-' }}</span>
            <span v-else-if="row.transport === 2">{{ row.url || '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="timeout" label="超时(秒)" width="100" align="center"/>
        <el-table-column prop="statusName" label="状态" width="90" align="center"/>
        <el-table-column prop="description" label="描述" min-width="200" align="center" show-overflow-tooltip/>
        <el-table-column prop="createdByName" label="创建人" align="center"/>
        <el-table-column prop="createdDt" label="创建时间" width="160" align="center"/>
        <el-table-column prop="updatedByName" label="修改人" align="center"/>
        <el-table-column prop="updatedDt" label="修改时间" width="160" align="center"/>
        <el-table-column label="操作" width="300" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
                type="success"
                text
                @click="handleOpenUpdate(row)">
              <el-icon><Edit/></el-icon>修改
            </el-button>
            <el-button
                type="warning"
                text
                :loading="!!testingMap[row.id]"
                @click="handleTestConnection(row)"
            >
              <el-icon v-if="!testingMap[row.id]"><Connection/></el-icon>测试
            </el-button>
            <el-button type="danger" text @click="handleDelete(row)">
              <el-icon><Delete/></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <div>
      <el-pagination
          :current-page="mcpQuery.pageNo"
          :page-size="mcpQuery.pageSize"
          :page-sizes="pageSizes"
          :total="total"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑表单 -->
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
                  <el-icon><Delete/></el-icon>
                </el-button>
              </div>
              <el-button type="primary" text @click="handleAddArg">
                <el-icon><Plus/></el-icon>添加参数
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="环境变量">
            <div class="mcp-dynamic-list">
              <div
                  v-for="(item, idx) in mcpForm.env"
                  :key="'env-' + idx"
                  class="mcp-dynamic-item mcp-env-item"
              >
                <el-input
                    v-model="item.key"
                    placeholder="变量名"
                    maxlength="50"
                />
                <el-input
                    v-model="item.value"
                    placeholder="变量值"
                    maxlength="200"
                />
                <el-button type="danger" text @click="handleRemoveEnv(idx)">
                  <el-icon><Delete/></el-icon>
                </el-button>
              </div>
              <el-button type="primary" text @click="handleAddEnv">
                <el-icon><Plus/></el-icon>添加变量
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
      <!--字段填写提示-->
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>名称为必填项，建议使用能体现服务用途的标识，便于后续识别；</div>
            <div>传输类型选择 SSE 时需选择所属厂商并填写 URL，厂商请先在厂商管理中维护；</div>
            <div>传输类型选择 STDIO 时需填写命令与参数、环境变量，无需选择厂商；</div>
            <div>参数与环境变量支持动态增删，提交前会自动序列化为 JSON 字符串保存；</div>
            <div>超时时间取值范围 1-600 秒，建议根据目标服务响应速度合理设置；</div>
            <div>状态默认启用，禁用后该 MCP 服务器不会被业务调用；</div>
            <div>保存后可点击列表「测试」按钮验证连通性，连接失败会有明确提示。</div>
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
import {ref, reactive, getCurrentInstance, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  pageMcpListAPI,
  detailMcpAPI,
  createMcpAPI,
  updateMcpAPI,
  deleteMcpAPI,
  testConnectionMcpAPI
} from '@/api/llm/mcp.js'
import {pageProviderListAPI} from '@/api/llm/provider.js'
import {
  Plus, Edit, Delete, Connection
} from '@element-plus/icons-vue'

const {proxy} = getCurrentInstance()

// 列表查询条件（仅分页参数）
const mcpQuery = ref({
  pageNo: 1,
  pageSize: 15
})
const mcpList = ref([])
const total = ref(0)
const pageSizes = [15, 30, 50, 100]

// 表单相关
const formVisible = ref(false)
const formTitle = ref('')
const mcpForm = ref(createEmptyForm())
const formRules = {
  name: [{required: true, message: '请输入名称', trigger: 'blur'}],
  providerId: [{required: true, validator: validateProviderId, trigger: 'change'}],
  transport: [{required: true, message: '请选择传输类型', trigger: 'change'}],
  command: [{required: true, message: '请输入命令', trigger: 'blur'}],
  url: [{required: true, message: '请输入 URL', trigger: 'blur'}],
  timeout: [{required: true, message: '请输入超时时间', trigger: 'blur'}]
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

// 测试中状态映射，按行 id 维度控制 loading
const testingMap = reactive({})

// 厂商选项
const providerOptions = ref([])

/**
 * 加载厂商选项列表（一次性拉取全量）
 */
function loadProviderOptions() {
  pageProviderListAPI({pageNo: 1, pageSize: 1000}).then(res => {
    if (res.code === 200 && res.data) {
      providerOptions.value = res.data.rows || []
    }
  })
}

onMounted(() => {
  handleGetList()
  loadProviderOptions()
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
 * 加载列表数据
 */
function handleGetList() {
  pageMcpListAPI(mcpQuery.value).then(res => {
    if (res.code === 200) {
      mcpList.value = res.data.rows || []
      total.value = res.data.total || 0
    }
  })
}

/**
 * 切换分页大小
 * @param size
 */
function handleSizeChange(size) {
  mcpQuery.value.pageSize = size
  mcpQuery.value.pageNo = 1
  handleGetList()
}

/**
 * 切换页码
 * @param page
 */
function handlePageChange(page) {
  mcpQuery.value.pageNo = page
  handleGetList()
}

/**
 * 打开新建表单
 */
function handleOpenCreate() {
  mcpForm.value = createEmptyForm()
  formTitle.value = '新建 MCP 服务'
  formVisible.value = true
}

/**
 * 打开修改表单，先拉取详情
 * @param row
 */
function handleOpenUpdate(row) {
  detailMcpAPI({id: row.id}).then(res => {
    if (res.code !== 200) {
      return
    }
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
 * 删除二次确认
 * @param row
 */
function handleDelete(row) {
  ElMessageBox.confirm('是否确定删除此 MCP 服务?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteMcpAPI({id: row.id}).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        handleGetList()
      }
    })
  }).catch(() => {
  })
}

/**
 * 测试连通性
 * @param row
 */
function handleTestConnection(row) {
  testingMap[row.id] = true
  testConnectionMcpAPI({id: row.id}).then(res => {
    if (res.code === 200) {
      ElMessage.success('连接成功')
    } else if (res.code === 2203) {
      ElMessage.error('连接失败')
    }
  }).finally(() => {
    testingMap[row.id] = false
  })
}

/**
 * 提交表单
 */
function handleSubmit() {
  proxy.$refs.formRef.validate(valid => {
    if (!valid) {
      return
    }
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
        .filter(item => item.key)
        .reduce((obj, item) => {
          obj[item.key] = item.value
          return obj
        }, {})
    data.env = JSON.stringify(envObj)
  }
  return data
}

/**
 * 关闭表单并重置
 */
function handleCloseForm() {
  mcpForm.value = createEmptyForm()
  if (proxy.$refs.formRef) {
    proxy.$refs.formRef.clearValidate()
  }
  formVisible.value = false
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
  if (proxy.$refs.formRef) {
    proxy.$refs.formRef.validateField('providerId')
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
  mcpForm.value.env.push({key: '', value: ''})
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
      return Object.keys(obj).map(key => ({key, value: obj[key]}))
    }
    return []
  } catch (e) {
    return []
  }
}

/**
 * 详情展示用：参数数组转空格分隔字符串
 * @param argsStr
 */
function formatArgs(argsStr) {
  const arr = parseArgs(argsStr)
  return arr.length ? arr.join(' ') : '-'
}

/**
 * 详情展示用：环境变量对象转 key=value 文本
 * @param envStr
 */
function formatEnv(envStr) {
  let obj = {}
  try {
    obj = JSON.parse(envStr || '{}')
  } catch (e) {
    obj = {}
  }
  const keys = Object.keys(obj)
  if (!keys.length) {
    return '-'
  }
  return keys.map(k => `${k}=${obj[k]}`).join('；')
}
</script>

<style scoped lang="scss">
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

.drawer-footer {
  padding: 0 $spacing-md;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
