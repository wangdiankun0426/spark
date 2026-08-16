<template>
  <el-drawer v-model="visible" title="运行详情" direction="rtl" size="40%" append-to-body>
    <template v-if="detail">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="工作流">{{ detail.templateName }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detail.revNum }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusName }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.durationMs != null ? detail.durationMs + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.createdByName" label="触发人">{{ detail.createdByName }}</el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ detail.createdDt }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <div class="section-title">输入参数</div>
      <el-input :model-value="formatJson(detail.inputJson)" type="textarea" :rows="4" readonly />
      <div class="section-title">输出结果</div>
      <el-input :model-value="formatJson(detail.outputJson)" type="textarea" :rows="6" readonly />
      <div v-if="detail.errorMsg" class="section-title" style="color:#f56c6c">错误信息</div>
      <el-alert v-if="detail.errorMsg" :title="detail.errorMsg" type="error" :closable="false" />

      <el-divider />
      <div class="section-title">节点执行日志</div>
      <el-timeline v-if="nodes.length > 0">
        <el-timeline-item
            v-for="n in nodes"
            :key="n.id"
            :timestamp="n.nodeName + ' (' + n.nodeType + ')'"
            :type="n.status === 3 ? 'success' : n.status === 4 ? 'danger' : 'info'"
            size="small"
        >
          <div class="node-status">{{ n.status === 3 ? '完成(' + n.durationMs + 'ms)' : n.status === 4 ? '失败: ' + (n.errorMsg || '') : '等待/运行中' }}</div>
          <div v-if="getNodeOutputText(n)" class="node-output">{{ getNodeOutputText(n) }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="无节点记录" :image-size="40" />
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { queryInstanceDetailAPI, queryInstanceNodesAPI } from '@/api/workflow/instance.js';

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

function formatJson(str) {
  if (!str) return '';
  try {
    return JSON.stringify(JSON.parse(str), null, 2);
  } catch (e) {
    return str;
  }
}

/** 解析节点输出并返回可展示文本（LLM节点优先展示text输出） */
function getNodeOutputText(n) {
  if (!n.outputJson) return '';
  try {
    const out = JSON.parse(n.outputJson);
    if (typeof out.text === 'string' && out.text) {
      return out.text;
    }
    const str = JSON.stringify(out, null, 2);
    return str === '{}' ? '' : str;
  } catch (e) {
    return n.outputJson;
  }
}

/** 抽屉打开时按 instanceId 加载实例详情与节点执行日志 */
watch(visible, async (val) => {
  if (!val || !props.instanceId) return;
  detail.value = null;
  nodes.value = [];
  const res = await queryInstanceDetailAPI({ id: props.instanceId });
  if (res.code === 200 && res.data) {
    detail.value = res.data;
    const nRes = await queryInstanceNodesAPI({ instanceId: props.instanceId });
    if (nRes.code === 200 && nRes.data) {
      nodes.value = nRes.data;
    }
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
