<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="market-header">
      <div class="market-header-left">
        <div class="market-header-title">
          模型市场
        </div>
        <div class="market-header-subtitle">浏览各厂商提供的语言模型、向量模型与排序模型，按需选用</div>
      </div>
      <div class="market-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索模型名称"
            clearable
            :prefix-icon="Search"
            style="width: 300px"
            @change="handleSearch"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
        />
        <el-button
            type="primary"
            @click="handleOpenCreateModel">
          <el-icon><Plus /></el-icon>新增模型
        </el-button>
      </div>
    </div>

    <!-- 主体：左厂商 + 右模型 -->
    <div class="market-body" v-loading="loading">
      <!-- 左侧厂商列表 -->
      <div class="provider-pane">
        <div class="provider-pane-title-row">
          <div class="provider-pane-title">厂商列表</div>
          <el-button type="primary" size="small" @click="handleOpenCreateProvider">
            <el-icon><Plus /></el-icon>新增厂商
          </el-button>
        </div>
        <div class="provider-list">
          <div
              class="provider-item"
              :class="{ 'provider-item-active': activeProviderId === null }"
              @click="handleSelectProvider(null)"
          >
            <div class="provider-item-icon provider-item-icon-all">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="provider-item-info">
              <div class="provider-item-name">全部厂商</div>
              <div class="provider-item-count">{{ providerList.length }} 家厂商</div>
            </div>
          </div>
          <div
              v-for="provider in providerList"
              :key="provider.id"
              class="provider-item"
              :class="{ 'provider-item-active': activeProviderId === provider.id }"
              @click="handleSelectProvider(provider.id)"
          >
            <el-image
                v-if="provider.icon"
                :src="provider.icon"
                fit="cover"
                class="provider-item-icon"
            >
              <template #error>
                <div class="provider-item-icon provider-item-icon-fallback">
                  {{ provider.name?.charAt(0) }}
                </div>
              </template>
            </el-image>
            <div v-else class="provider-item-icon provider-item-icon-fallback">
              {{ provider.name?.charAt(0) }}
            </div>
            <div class="provider-item-info">
              <div class="provider-item-name">{{ provider.name }}</div>
              <div class="provider-item-count" v-if="provider.description">{{ provider.description }}</div>
            </div>
            <div class="provider-item-actions">
              <el-dropdown
                  trigger="hover"
                  @command="command => handleProviderCommand(command, provider)"
              >
                <span class="p-more" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>修改
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧模型卡片网格 -->
      <div class="model-pane">
        <div class="model-pane-toolbar">
          <div class="model-pane-current">
            <el-icon><Collection /></el-icon>
            <span>{{ currentProviderName }}</span>
            <el-divider direction="vertical" />
            <span class="model-pane-total">共 {{ total }} 个模型</span>
          </div>
          <div class="model-toolbar-actions">
            <el-radio-group v-model="modelTypeFilter" size="small">
              <el-radio-button :label="0">全部</el-radio-button>
              <el-radio-button :label="1">
                <el-icon class="model-type-icon">
                  <ModelLanguage/>
                </el-icon>
                语言模型
              </el-radio-button>
              <el-radio-button :label="2">
                <el-icon class="model-type-icon">
                  <ModelVector/>
                </el-icon>
                向量模型
              </el-radio-button>
              <el-radio-button :label="3">
                <el-icon class="model-type-icon">
                  <ModelRank/>
                </el-icon>
                排序模型
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div
            class="model-grid"
            v-if="modelList.length"
        >
          <info-card
              v-for="model in modelList"
              :key="model.id"
              :icon="getIcon(model)"
              :title="model.name"
              :description="model.description || '暂无描述'"
              :actions="cardActions(model)"
          />
        </div>
        <el-empty
            class="empty-grid"
            v-else
            :description="emptyText"
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
      </div>
    </div>

    <!-- 新增 / 修改 厂商抽屉 -->
    <el-drawer
        v-model="providerFormVisible"
        :title="providerFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseProviderForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="providerForm"
          label-width="auto"
          :rules="providerFormRules"
          ref="providerFormRef"
      >
        <el-form-item label="厂商名称" prop="name">
          <el-input
              v-model="providerForm.name"
              placeholder="请输入厂商名称，如：DeepSeek"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="厂商图标" prop="icon">
          <el-input
              v-model="providerForm.icon"
              placeholder="请输入厂商图标 URL 或图标标识"
              maxlength="128"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="API地址" prop="apiUrl">
          <el-input
              v-model="providerForm.apiUrl"
              placeholder="请输入 API 地址，如：https://api.openai.com"
              maxlength="128"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="密钥" prop="secretKey">
          <el-input
              v-model="providerForm.secretKey"
              placeholder="请输入厂商密钥（API Key），如：sk-xxxxxxxx"
              type="password"
              show-password
              maxlength="128"
          />
        </el-form-item>
        <el-form-item label="厂商描述" prop="description">
          <el-input
              v-model="providerForm.description"
              type="textarea"
              :rows="5"
              placeholder="请输入厂商描述，简要说明厂商背景与提供的服务，最多 200 字"
              maxlength="256"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="providerForm.remark"
              type="textarea"
              :rows="5"
              placeholder="请输入备注信息，记录其他需要说明的事项，最多 200 字"
              maxlength="256"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number
              v-model="providerForm.orderNum"
              :min="1"
              :max="999"
          />
        </el-form-item>
      </el-form>
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>厂商名称建议与官方名称保持一致，会展示在模型选择列表中；</div>
            <div>厂商图标填图标 URL 或图标标识，用于列表中的品牌展示；</div>
            <div>API 地址为该厂商的接口基址，需带协议头，如 https://api.openai.com；</div>
            <div>密钥为调用该厂商模型所用的 API Key，请确保其具备模型调用权限，保存后以密文回显；</div>
            <div>排序用于控制厂商在列表中的展示顺序。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitProviderForm">保存</el-button>
          <el-button @click="handleCloseProviderForm">取消</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 新增 / 修改 模型抽屉 -->
    <el-drawer
        v-model="modelFormVisible"
        :title="modelFormTitle"
        direction="ltr"
        size="30%"
        :before-close="handleCloseModelForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="modelForm"
          label-width="auto"
          :rules="modelFormRules"
          ref="modelFormRef"
      >
        <el-form-item label="供应商" prop="providerId">
          <el-select
              v-model="modelForm.providerId"
              placeholder="请选择模型供应商，如：DeepSeek"
              style="width: 100%"
          >
            <el-option
                v-for="item in providerList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型类型" prop="type">
          <el-select
              v-model="modelForm.type"
              placeholder="请选择模型类型"
              style="width: 100%"
          >
            <el-option
                v-for="item in modelTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" prop="name">
          <el-input
              v-model="modelForm.name"
              placeholder="请输入模型名称，如：gpt-4、claude-3-opus"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="思考模式" prop="enableThinking" v-if="modelForm.type === 1">
          <el-switch
              v-model="modelForm.enableThinking"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="温度参数" prop="temperature" v-if="modelForm.type === 1">
          <el-input-number
              v-model="modelForm.temperature"
              :min="0"
              :max="2"
              :step="0.01"
              :precision="2"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="modelForm.status"
              :active-value="1"
              :inactive-value="-1"
              active-text="已启用"
              inactive-text="已停用"
              inline-prompt
          />
        </el-form-item>
        <el-form-item label="模型描述" prop="description">
          <el-input
              v-model="modelForm.description"
              placeholder="请输入模型描述，简要说明模型能力与适用场景，最多 200 字"
              type="textarea"
              :rows="5"
              maxlength="256"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
              v-model="modelForm.remark"
              placeholder="请输入备注信息，记录其他需要说明的事项，最多 200 字"
              type="textarea"
              :rows="5"
              maxlength="256"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <el-alert
          type="info"
          :closable="false"
          show-icon
      >
        <template #title>
          <div class="form-tip">
            <div>供应商决定调用该模型时使用的 API 地址与密钥，需先在「模型厂商」中完成配置；</div>
            <div>模型名称必须与厂商接口中的模型标识完全一致（如 gpt-4、deepseek-chat），否则调用会失败；</div>
            <div>模型类型决定用途：语言模型用于对话与信息抽取，向量模型用于知识库向量化，排序模型用于召回结果重排；</div>
            <div>思考模式与温度参数仅对语言模型生效，温度值越大回答越发散、越小越稳定；</div>
            <div>停用的模型不会出现在知识库、智能体等功能的模型可选列表中。</div>
          </div>
        </template>
      </el-alert>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleSubmitModelForm">保存</el-button>
          <el-button @click="handleCloseModelForm">取消</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageModelListAPI,
  createModelAPI,
  updateModelAPI,
  deleteModelAPI,
  queryModelDetailAPI
} from '@/api/llm/model.js'
import {
  pageProviderListAPI,
  createProviderAPI,
  updateProviderAPI,
  deleteProviderAPI,
  queryProviderDetailAPI
} from '@/api/llm/provider.js'
import InfoCard from '@/components/InfoCard/index.vue'
import {Search, Grid, Collection, Plus, MoreFilled, Delete} from '@element-plus/icons-vue'
import ModelLanguage from "@/assets/icons/modelLanguage.vue";
import ModelVector from "@/assets/icons/modelVector.vue";
import ModelRank from "@/assets/icons/ModelRank.vue";

const loading = ref(false)
const keyword = ref('')
const modelTypeFilter = ref(0)
const providerList = ref([])
const modelList = ref([])
const total = ref(0)
const pageSizes = [12, 24, 48]
// 分页查询条件
const query = ref({
  pageNo: 1,
  pageSize: 12,
})
const activeProviderId = ref(null)

let searchTimer = null

onMounted(() => {
  loadProviders()
  loadModels()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 关键字搜索防抖，300ms 后回到第一页并重新检索
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    query.value.pageNo = 1
    loadModels()
  }, 300)
})

/**
 * 模型类型切换
 */
watch(modelTypeFilter, () => {
  query.value.pageNo = 1
  loadModels()
})

/**
 * 当前选中厂商名称
 */
const currentProviderName = computed(() => {
  if (activeProviderId.value === null) {
    return '全部厂商'
  }
  const matched = providerList.value.find(p => p.id === activeProviderId.value)
  return matched ? matched.name : '全部厂商'
})

/**
 * 空状态文案
 */
const emptyText = computed(() => {
  if (keyword.value) {
    return '未找到匹配的模型'
  }
  if (modelTypeFilter.value !== 0) {
    return modelTypeFilter.value === 1 ? '暂无语言模型' : modelTypeFilter.value === 2 ? '暂无向量模型' : '暂无排序模型'
  }
  return activeProviderId.value === null ? '暂无模型' : '该厂商暂无模型'
})

/**
 * 加载模型厂商列表
 */
function loadProviders() {
  pageProviderListAPI({ page: false }).then(res => {
    providerList.value = res.data.rows || []
  })
}

/**
 * 分页加载模型列表
 */
function loadModels() {
  loading.value = true
  const params = {
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
    providerId: activeProviderId.value || undefined,
    type: modelTypeFilter.value || undefined,
    name: keyword.value || undefined
  }
  pageModelListAPI(params).then(res => {
    modelList.value = res.data.rows || []
    total.value = res.data.total || 0
  }).finally(() => {
    loading.value = false
  })
}

/**
 * 立即触发模型检索，回到第一页
 */
function handleSearch() {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  query.value.pageNo = 1
  loadModels()
}

/**
 * 切换每页条数，回到第一页重新查询
 * @param size
 */
function handleSizeChange(size) {
  query.value.pageSize = size
  query.value.pageNo = 1
  loadModels()
}

/**
 * 切换页码重新查询
 * @param pageNo
 */
function handleCurrentChange(pageNo) {
  query.value.pageNo = pageNo
  loadModels()
}

/**
 * 切换选中厂商并重新查询模型列表，回到第一页
 * @param providerId
 */
function handleSelectProvider(providerId) {
  activeProviderId.value = providerId
  query.value.pageNo = 1
  loadModels()
}

// 新增 / 修改 厂商表单
const providerFormVisible = ref(false)
const providerFormTitle = ref('')
const providerFormRef = ref(null)
const providerForm = ref(createEmptyProviderForm())
const providerFormRules = {
  name: [{ required: true, trigger: 'blur', message: '请输入厂商名称' }],
  icon: [{ required: true, trigger: 'blur', message: '请输入厂商图标' }],
  apiUrl: [{ required: true, trigger: 'blur', message: '请输入API地址' }],
  secretKey: [{ required: true, trigger: 'blur', message: '请输入厂商密钥' }],
  orderNum: [{ required: true, trigger: 'blur', message: '请输入排序' }],
}

/**
 * 构造空厂商表单
 */
function createEmptyProviderForm() {
  return {
    id: undefined,
    name: undefined,
    icon: undefined,
    apiUrl: undefined,
    secretKey: undefined,
    description: undefined,
    remark: undefined,
    orderNum: undefined,
  }
}

/**
 * 打开新建厂商表单
 */
function handleOpenCreateProvider() {
  providerForm.value = createEmptyProviderForm()
  providerForm.value.orderNum = 999
  providerFormTitle.value = '新建厂商'
  providerFormVisible.value = true
}

/**
 * 打开修改厂商表单，先拉取详情回填
 * @param provider
 */
function handleOpenUpdateProvider(provider) {
  queryProviderDetailAPI({ id: provider.id }).then(res => {
    if (res.code !== 200 || !res.data) return
    const d = res.data
    providerForm.value = {
      id: d.id,
      name: d.name,
      icon: d.icon,
      apiUrl: d.apiUrl,
      secretKey: d.secretKey,
      description: d.description,
      remark: d.remark,
      orderNum: d.orderNum,
    }
    providerFormTitle.value = '修改厂商'
    providerFormVisible.value = true
  })
}

/**
 * 关闭厂商表单
 */
function handleCloseProviderForm() {
  providerForm.value = createEmptyProviderForm()
  if (providerFormRef.value) {
    providerFormRef.value.clearValidate()
  }
  providerFormTitle.value = ''
  providerFormVisible.value = false
}

/**
 * 提交厂商表单（新增 / 修改）
 */
function handleSubmitProviderForm() {
  providerFormRef.value.validate(valid => {
    if (!valid) return
    const data = { ...providerForm.value }
    const api = data.id ? updateProviderAPI : createProviderAPI
    api(data).then(res => {
      if (res.code !== 200) return
      ElMessage.success(data.id ? '厂商修改成功' : '厂商创建成功')
      handleCloseProviderForm()
      loadProviders()
    })
  })
}

/**
 * 删除厂商
 * @param provider
 */
function handleDeleteProvider(provider) {
  ElMessageBox.confirm('是否确定删除此条模型厂商?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteProviderAPI({ id: provider.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('删除模型厂商成功')
      if (activeProviderId.value === provider.id) {
        activeProviderId.value = null
        query.value.pageNo = 1
        loadModels()
      }
      loadProviders()
    })
  }).catch(() => {})
}

// 新增 / 修改 模型表单
const modelFormVisible = ref(false)
const modelFormTitle = ref('')
const modelFormRef = ref(null)
const modelForm = ref(createEmptyModelForm())
const modelTypeOptions = [
  { label: '语言模型', value: 1 },
  { label: '向量模型', value: 2 },
  { label: '排序模型', value: 3 }
]
const modelFormRules = {
  providerId: [{ required: true, trigger: 'change', message: '请选择供应商' }],
  type: [{ required: true, trigger: 'change', message: '请选择模型类型' }],
  name: [{ required: true, trigger: 'blur', message: '请输入模型名称' }],
  temperature: [
    { required: true, message: '请输入温度参数', trigger: 'blur' },
    { type: 'number', min: 0, max: 2, message: '温度参数范围为 0.0-2.0', trigger: 'blur' }
  ],
}

/**
 * 构造空模型表单
 */
function createEmptyModelForm() {
  return {
    id: undefined,
    providerId: undefined,
    type: undefined,
    name: undefined,
    enableThinking: -1,
    temperature: 0.10,
    status: 1,
    description: undefined,
    remark: undefined,
  }
}

/**
 * 打开新建模型表单
 */
function handleOpenCreateModel() {
  modelForm.value = createEmptyModelForm()
  modelFormTitle.value = '新建模型'
  modelFormVisible.value = true
}

/**
 * 打开修改模型表单，先拉取详情回填
 * @param model
 */
function handleOpenUpdateModel(model) {
  queryModelDetailAPI({ id: model.id }).then(res => {
    if (res.code !== 200 || !res.data) return
    const d = res.data
    modelForm.value = {
      id: d.id,
      providerId: d.providerId,
      type: d.type,
      name: d.name,
      enableThinking: d.enableThinking,
      temperature: d.temperature,
      status: d.status,
      description: d.description,
      remark: d.remark,
    }
    modelFormTitle.value = '修改模型'
    modelFormVisible.value = true
  })
}

/**
 * 关闭模型表单
 */
function handleCloseModelForm() {
  modelForm.value = createEmptyModelForm()
  if (modelFormRef.value) {
    modelFormRef.value.clearValidate()
  }
  modelFormTitle.value = ''
  modelFormVisible.value = false
}

/**
 * 提交模型表单（新增 / 修改）
 */
function handleSubmitModelForm() {
  modelFormRef.value.validate(valid => {
    if (!valid) return
    const data = {
      id: modelForm.value.id,
      providerId: modelForm.value.providerId,
      type: modelForm.value.type,
      name: modelForm.value.name,
      enableThinking: modelForm.value.type === 1 ? modelForm.value.enableThinking : -1,
      temperature: modelForm.value.temperature,
      status: modelForm.value.status,
      description: modelForm.value.description,
      remark: modelForm.value.remark,
    }
    const api = data.id ? updateModelAPI : createModelAPI
    api(data).then(res => {
      if (res.code !== 200) return
      ElMessage.success(data.id ? '模型修改成功' : '模型创建成功')
      handleCloseModelForm()
      loadModels()
    })
  })
}

/**
 * 删除模型
 * @param model
 */
function handleDeleteModel(model) {
  ElMessageBox.confirm('是否确定删除此条模型?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteModelAPI({ id: model.id }).then(res => {
      if (res.code !== 200) return
      ElMessage.success('删除模型成功')
      if (modelList.value.length === 1 && query.value.pageNo > 1) {
        query.value.pageNo -= 1
      }
      loadModels()
    })
  }).catch(() => {})
}

/**
 * 卡片底部工具栏按钮
 * @param model
 */
function cardActions(model) {
  return [
    { key: 'edit', label: '修改', icon: 'Edit', onClick: () => handleOpenUpdateModel(model) },
    { key: 'delete', label: '删除', icon: 'Delete',onClick: () => handleDeleteModel(model) }
  ]
}

/**
 * 根据模型类型返回头像图标
 * @param model
 */
function getIcon(model) {
  if (model.type === 1) return ModelLanguage
  if (model.type === 2) return ModelVector
  return ModelRank
}

/**
 * 厂商「···」菜单命令
 * @param command 命令标识（edit/delete）
 * @param provider 厂商行数据
 */
function handleProviderCommand(command, provider) {
  if (command === 'edit') {
    handleOpenUpdateProvider(provider)
    return
  }
  handleDeleteProvider(provider)
}
</script>

<style scoped lang="scss">
.market-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
}

.market-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.market-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.market-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.market-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.market-body {
  display: flex;
  gap: $spacing-lg;
  align-items: flex-start;
}

.provider-pane {
  width: 240px;
  flex-shrink: 0;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
  padding: $spacing-md;
  position: sticky;
  top: $spacing-md;
}

.provider-pane-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  padding: $spacing-xs $spacing-sm $spacing-sm;
}

.provider-pane-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-secondary;
}

.provider-item-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  opacity: 0;
  transition: $transition-fast;
}

.provider-item:hover .provider-item-actions {
  opacity: 1;
}

.p-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: $border-radius-sm;
  color: $color-text-secondary;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    background-color: $color-primary-light;
    color: $color-primary;
  }
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

.provider-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  height: calc(100vh - 270px);
  overflow-y: auto;
}

.provider-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: $transition-fast;
  border: 1px solid transparent;

  &:hover {
    background-color: $color-primary-soft;
  }

  &.provider-item-active {
    background-color: $color-primary-light;
    border-color: $color-primary;
  }
}

.provider-item-icon {
  width: 32px;
  height: 32px;
  border-radius: $border-radius-md;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: $color-text-white;
  background-color: $color-primary;
}

.provider-item-icon-all {
  background-color: $color-text-secondary;
}

.provider-item-icon-fallback {
  background-color: $color-primary-mid;
}

.provider-item-info {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.provider-item-name {
  font-size: 13px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.provider-item-count {
  font-size: 11px;
  color: $color-text-placeholder;
}

.model-pane {
  flex: 1;
  min-width: 0;
}

.model-pane-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-card;
  margin-bottom: $spacing-lg;
}

.model-pane-current {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 14px;
  font-weight: 600;
  color: $color-text-primary;

  .el-icon {
    color: $color-primary;
  }
}

.model-pane-total {
  font-size: 13px;
  font-weight: 400;
  color: $color-text-secondary;
}

.model-grid {
  height: calc(100vh - #{$nav-height} - 270px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
.empty-grid {
  height: calc(100vh - #{$nav-height} - 270px);
}

.model-toolbar-actions {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.model-type-icon {
  margin-right: 4px;
  font-size: 16px;
  vertical-align: -3px;
}
</style>
