<template>
  <el-dialog
      :model-value="visible"
      :show-close="false"
      :close-on-click-modal="false"
      :before-close="handleClose"
      class="flow-detail-dialog"
      width="1180px"
      top="6vh"
      append-to-body
  >
    <!-- 头部横幅：与流程详情弹窗共用全局样式 -->
    <template #header>
      <div class="fd-head">
        <div class="fd-ico">
          <el-icon><Workflow /></el-icon>
        </div>
        <div class="fd-main">
          <div class="fd-title">
            <span class="fd-title-text">{{ detail?.templateName || '运行详情' }}</span>
            <span class="fd-chip">{{ detail?.statusName || '-' }}</span>
          </div>
          <div class="fd-sub">
            <span class="fd-avatar">{{ avatarText }}</span>
            <span>{{ detail?.createdByName || '-' }}</span>
            <span class="fd-sep">·</span>
            <span>触发于 {{ detail?.createdDt || '-' }}</span>
            <template v-if="detail?.revNum">
              <span class="fd-sep">·</span>
              <span>版本 v{{ detail.revNum }}</span>
            </template>
            <template v-if="waitingReviewNode">
              <span class="fd-sep">·</span>
              <span>当前环节：<b>{{ waitingReviewNode.nodeName }}</b></span>
            </template>
          </div>
        </div>
        <div class="fd-close" @click="handleClose">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </template>

    <el-tabs v-model="tabActive" class="fd-tabs">
      <!-- 实例详情：表单内容，实例级信息已在头部横幅展示 -->
      <el-tab-pane name="form">
        <template #label>
          <span class="fd-tab-label"><el-icon><Document /></el-icon>实例详情</span>
        </template>
        <div class="fd-pane" v-if="detail">
          <div class="fd-cap"><b>表单内容</b></div>
          <div v-if="formJson" class="fd-form is-readonly">
            <form-view v-if="visible && tabActive === 'form'" :disabled="true" :form="formJson" />
          </div>
          <el-empty v-else description="暂无表单内容" :image-size="60" />
        </div>
      </el-tab-pane>

      <!-- 执行日志 -->
      <el-tab-pane name="log">
        <template #label>
          <span class="fd-tab-label">
            <el-icon><Share /></el-icon>执行日志
            <span class="fd-tab-n" v-if="nodes.length">{{ nodes.length }}</span>
          </span>
        </template>
        <div class="fd-pane">
          <template v-if="detail?.errorMsg">
            <div class="fd-cap"><b>错误信息</b></div>
            <el-alert :title="detail.errorMsg" type="error" :closable="false" />
          </template>
          <div class="fd-cap" :class="{ 'fd-cap--gap': detail?.errorMsg }"><b>执行记录</b><span>共 {{ nodes.length }} 个节点</span></div>
          <el-timeline v-if="nodes.length > 0">
            <el-timeline-item
                v-for="n in nodes"
                :key="n.id"
                :type="getNodeDotType(n.status)"
                :timestamp="n.statusName"
                placement="top"
            >
              <div class="node-item">
                <div class="node-item-header">
                  <span class="node-name">{{ n.nodeName }}</span>
                  <span class="node-type-tag">{{ n.nodeType }}</span>
                </div>
                <div v-if="getNodeText(n.inputJson)" class="discuss-item">
                  <div class="discuss-header"><span class="discuss-name">输入数据</span></div>
                  <div class="discuss-content">{{ getNodeText(n.inputJson) }}</div>
                </div>
                <div v-if="getNodeText(n.outputJson)" class="discuss-item">
                  <div class="discuss-header"><span class="discuss-name">输出数据</span></div>
                  <div class="discuss-content">{{ getNodeText(n.outputJson) }}</div>
                </div>
                <div v-if="n.errorMsg" class="discuss-item">
                  <div class="discuss-header"><span class="discuss-name">错误信息</span></div>
                  <div class="discuss-content">{{ n.errorMsg }}</div>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="无节点记录" :image-size="60" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 底部操作条：挂起且当前人是审核人时显示通过驳回 -->
    <template #footer>
      <div class="fd-foot">
        <template v-if="canReview">
          <el-button v-debounce="handleReject" type="danger" plain><el-icon><CloseBold /></el-icon>驳回</el-button>
          <el-button v-debounce="handleApprove" type="primary"><el-icon><Select /></el-icon>通过</el-button>
        </template>
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import FormView from '@/components/FormView'
import Workflow from '@/assets/icons/workflow.vue'
import { queryInstanceDetailAPI, queryInstanceNodesAPI, reviewWorkflowInstanceAPI } from '@/api/workflow/instance.js'
import { detailFormValueAPI } from '@/api/form/formValue'

const props = defineProps({
  /** 弹窗显隐（受控，v-model） */
  modelValue: { type: Boolean, default: false },
  /** 实例 id，打开弹窗时加载详情 */
  instanceId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'reviewed'])

const store = useStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const tabActive = ref('form')
const detail = ref(null)
const nodes = ref([])
const formJson = ref(null)

/** 头像文字取触发人首字 */
const avatarText = computed(() => (detail.value?.createdByName || '工').slice(0, 1))

/** 最新一条挂起中的人工审核节点行 */
const waitingReviewNode = computed(() => {
  const rows = nodes.value.filter(n => n.status === 1 && n.nodeType === 'humanReview')
  return rows.length > 0 ? rows[rows.length - 1] : null
})

/** 当前人是审核人且实例挂起才允许操作，审核人为空视为不限制 */
const canReview = computed(() => {
  if (!detail.value || detail.value.status !== 6 || !waitingReviewNode.value) {
    return false
  }
  const userInfo = store.getters['user/getUserInfo']
  const userId = userInfo ? String(userInfo.id) : ''
  let reviewerIds = ''
  try {
    const info = JSON.parse(waitingReviewNode.value.outputJson || '{}')
    reviewerIds = info.reviewerIds || ''
  } catch (e) {
    reviewerIds = ''
  }
  if (!reviewerIds) {
    return true
  }
  return reviewerIds.split(',').includes(userId)
})

/** 节点状态对应时间线圆点颜色 */
function getNodeDotType(status) {
  const typeMap = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger', 5: 'info' }
  return typeMap[status] || 'info'
}

/** 解析节点参数并返回可展示文本 */
function getNodeText(n) {
  if (!n) return '';
  try {
    const out = JSON.parse(n);
    if (typeof out.text === 'string' && out.text) {
      return out.text;
    }
    const str = JSON.stringify(out, null, 2);
    return str === '{}' ? '' : str;
  } catch (e) {
    return n;
  }
}

/** 加载运行实例落库的表单数据并回填表单展示 */
async function loadFormData() {
  const res = await detailFormValueAPI({ objId: props.instanceId });
  if (res.code !== 200 || !res.data || !res.data.formJson) {
    return;
  }
  try {
    const formJsonResult = JSON.parse(res.data.formJson);
    const values = res.data.values || [];
    const codes = values.map(v => v.code);
    (formJsonResult.widgetList || []).forEach(widget => {
      const index = codes.indexOf(widget.config && widget.config.code);
      if (index === -1) return;
      widget.config.value = values[index].value;
      widget.config.showValue = values[index].showValue;
    });
    formJson.value = formJsonResult;
  } catch (e) { /* ignore */ }
}

/** 加载实例详情与节点执行日志 */
async function loadDetail() {
  detail.value = null;
  nodes.value = [];
  formJson.value = null;
  const res = await queryInstanceDetailAPI({ id: props.instanceId });
  if (res.code === 200 && res.data) {
    detail.value = res.data;
    const nRes = await queryInstanceNodesAPI({ instanceId: props.instanceId });
    if (nRes.code === 200 && nRes.data) {
      nodes.value = nRes.data;
    }
    loadFormData();
  }
}

watch(visible, (val) => {
  if (val && props.instanceId) {
    tabActive.value = 'form'
    loadDetail()
  }
});

function handleClose() {
  visible.value = false;
}

function handleApprove() {
  ElMessageBox.confirm('确认通过该审核？实例将继续执行', '通过', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    submitReview(true, '')
  }).catch(() => {})
}

function handleReject() {
  ElMessageBox.prompt('请输入审核意见，选填', '驳回', {
    inputType: 'textarea',
    inputProps: { rows: 4, maxlength: 500 },
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    inputValidator: validateComment
  }).then(({ value }) => {
    const comment = value ? value.trim() : ''
    submitReview(false, comment)
  }).catch(() => {})
}

function validateComment(value) {
  if (value && value.length > 500) {
    return '审核意见不能超过500字'
  }
  return true
}

/** 提交审核结论，成功后通知父级刷新列表并重载详情 */
async function submitReview(approved, comment) {
  const data = {
    instanceId: props.instanceId,
    nodeRunId: waitingReviewNode.value?.id,
    approved: approved,
    comment: comment
  }
  const res = await reviewWorkflowInstanceAPI(data)
  if (res.code !== 200) return
  ElMessage.success(approved ? '已通过，流程继续执行' : '已驳回')
  emit('reviewed')
  loadDetail()
}
</script>

<style scoped lang="scss">
// 同页第二个分节与上文拉开间距
.fd-cap--gap {
  margin-top: 18px;
}

.node-type-tag {
  font-size: 11px;
  color: $app-text-secondary;
  background: $app-bg-sub;
  padding: 1px 7px;
  border-radius: 8px;
}
</style>
