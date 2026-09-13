<template>
  <el-drawer
      v-model="visible"
      title="运行详情"
      direction="ltr"
      size="50%"
      append-to-body
      :close-on-click-modal="false"
  >
    <template v-if="detail">
      <div
          style="margin-bottom: 30px"
          v-if="formJson && formJson.widgetList && formJson.widgetList.length > 0">
        <div class="section-title">表单数据</div>
        <form-view
            :form="formJson"
            disabled
        />
      </div>
      <el-empty
          v-else
          description="无表单数据"
          :image-size="40"
      />

      <div class="section-title">实例数据</div>
      <el-descriptions
          :column="1"
          border
          size="small"
      >
        <el-descriptions-item label="工作流">{{ detail.templateName }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.revNum }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusName }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.durationMs != null ? detail.durationMs + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="触发人">{{ detail.createdByName }}</el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ detail.createdDt }}</el-descriptions-item>
      </el-descriptions>

      <div
          v-if="detail.errorMsg"
          class="section-title"
          style="color:#f56c6c"
      >错误信息</div>
      <el-alert
          v-if="detail.errorMsg"
          :title="detail.errorMsg"
          type="error"
          :closable="false"
      />

      <el-divider />

      <div class="section-title">执行日志</div>
      <el-timeline v-if="nodes.length > 0">
        <el-timeline-item
            v-for="n in nodes"
            :key="n.id"
            :timestamp="n.statusName"
            :type="n.status === 3 ? 'success' : n.status === 4 ? 'danger' : 'info'"
        >
          <div class="node-status">{{n.nodeName}} ( {{n.nodeType}} )</div>
          <div v-if="getNodeText(n.inputJson)">
            输入数据:
            <div class="node-output">{{ getNodeText(n.inputJson) }}</div>
          </div>
          <div v-if="getNodeText(n.outputJson)">
            输出数据:
            <div class="node-output">{{ getNodeText(n.outputJson) }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="无节点记录" :image-size="40" />
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import FormView from '@/components/FormView';
import { queryInstanceDetailAPI, queryInstanceNodesAPI } from '@/api/workflow/instance.js';
import { detailFormValueAPI } from '@/api/form/formValue';

const props = defineProps({
  /** 抽屉显隐（受控，v-model） */
  modelValue: { type: Boolean, default: false },
  /** 实例 id，打开抽屉时加载详情 */
  instanceId: { type: [Number, String], default: null }
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const detail = ref(null);
const nodes = ref([]);
const formJson = ref(null);

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

/** 抽屉打开时按 instanceId 加载实例详情与节点执行日志 */
watch(visible, async (val) => {
  if (!val || !props.instanceId) return;
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
});
</script>

<style scoped lang="scss">
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #172b4d;
  margin: $spacing-sm 0 6px;
}
.node-status {
  font-size: 12px;
  color: #172b4d;
}
.node-output {
  margin-top: 4px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 160px;
  overflow-y: auto;
}
</style>
