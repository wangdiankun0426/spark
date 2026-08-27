<template>
  <!--连线配置-->
  <div class="property-drawer" v-if="edge">
    <div class="drawer-header">
      <span class="drawer-title">连线配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="drawer-body">
      <el-form :model="edge" label-width="auto">
        <el-form-item label="连线ID">
          <el-input :model-value="edge.id" readonly />
        </el-form-item>
        <el-form-item label="源节点">
          <el-input :value="getNodeName(edge.source)" readonly />
        </el-form-item>
        <el-form-item label="目标节点">
          <el-input :value="getNodeName(edge.target)" readonly />
        </el-form-item>
        <el-form-item v-if="isConditionBranch(edge)" label="分支条件">
          <div class="condition-builder">
            <div v-for="(cond, idx) in conditions" :key="idx" class="condition-row">
              <div class="condition-row-header">
                <el-select
                    size="small"
                    v-if="idx > 0"
                    v-model="cond.logic"
                    class="logic-select"
                    @change="syncCondition"
                >
                  <el-option label="且" value="&&" />
                  <el-option label="或" value="||" />
                </el-select>
                <span v-else class="logic-label">当</span>
                <el-button
                    v-if="conditions.length > 1"
                    link
                    type="danger"
                    @click="removeCondition(idx)"
                >删除</el-button>
              </div>
              <el-select
                  v-model="cond.source"
                  placeholder="选择来源"
                  @change="syncCondition">
                <el-option label="表单值" value="form" />
                <el-option label="节点数据" value="node" />
              </el-select>
              <el-select
                  v-if="cond.source !== 'node'"
                  v-model="cond.field"
                  placeholder="选择表单字段"
                  @change="syncCondition">
                <el-option
                    v-for="f in fieldOptions"
                    :key="f.value"
                    :label="f.label"
                    :value="f.value"
                />
              </el-select>
              <el-input
                  v-else
                  v-model="cond.field"
                  placeholder="输入上游节点输出数据，如 nodeId.result"
                  @input="syncCondition"
              />
              <el-select
                  v-model="cond.relation"
                  placeholder="选择关系"
                  @change="syncCondition"
              >
                <el-option label="等于" value="==" />
                <el-option label="不等于" value="!=" />
                <el-option label="包含" value="contains" />
                <el-option label="大于" value=">" />
                <el-option label="大于等于" value=">=" />
                <el-option label="小于" value="<" />
                <el-option label="小于等于" value="<=" />
              </el-select>
              <el-input v-model="cond.value" placeholder="条件值" @input="syncCondition" />
            </div>
            <el-button
                type="primary"
                @click="addCondition">
              <el-icon><Plus /></el-icon>添加条件
            </el-button>
            <div v-if="parseFailed" class="condition-warning">
              当前表达式为高级写法，无法可视化解析；修改条件后将覆盖原表达式
            </div>
            <div v-if="edge.conditionExpression" class="condition-preview">{{ edge.conditionExpression }}</div>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="drawer-footer">
      <el-button
          type="danger"
          size="small"
          style="width:100%"
          @click="$emit('delete-edge')">
        <el-icon><Delete /></el-icon>删除连线
      </el-button>
    </div>
  </div>

  <!--节点配置-->
  <div class="property-drawer" v-else-if="node">
    <div class="drawer-header">
      <span class="drawer-title">节点配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="drawer-body">
      <el-form
          :model="form"
          label-width="auto"
      >
        <el-form-item label="节点ID">
          <el-input
              :model-value="node.id"
              readonly
          />
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input
              v-model="form.name"
              @change="emitUpdate"
          />
        </el-form-item>
        <!--按节点类型拆分配置组件-->
        <el-empty
            v-if="['startEvent', 'endEvent', 'exclusiveGateway', 'parallelGateway'].includes(node.type)"
            description="该节点无需配置"
            :image-size="40"
        />
        <doc-parse-config
            v-if="node.type === 'docParse'"
            :form="form"
            @update="emitUpdate"
        />
        <notify-config
            v-if="node.type === 'notify'"
            :form="form"
            @update="emitUpdate"
        />
        <kb-archive-config
            v-if="node.type === 'kbArchive'"
            :form="form"
            @update="emitUpdate"
        />
        <llm-task-config
            v-if="node.type === 'llmTask'"
            :form="form"
            @update="emitUpdate"
        />
        <agent-task-config
            v-if="node.type === 'agentTask'"
            :form="form"
            @update="emitUpdate"
        />
        <kb-search-config
            v-if="node.type === 'knowledgeSearch'"
            :form="form"
            @update="emitUpdate"
        />
        <kg-search-config
            v-if="node.type === 'knowledgeGraphSearch'"
            :form="form"
            @update="emitUpdate"
        />
        <variable-op-config
            v-if="node.type === 'variableOp'"
            :form="form"
            @update="emitUpdate"
        />
        <code-execute-config
            v-if="node.type === 'codeExecute'"
            :form="form"
            @update="emitUpdate"
        />
        <http-request-config
            v-if="node.type === 'httpRequest'"
            :form="form"
            @update="emitUpdate"
        />
        <human-review-config
            v-if="node.type === 'humanReview'"
            :form="form"
            @update="emitUpdate"
        />
        <!--执行策略配置-->
        <template v-if="!['startEvent', 'endEvent', 'exclusiveGateway', 'parallelGateway'].includes(node.type)">
          <el-divider>执行策略</el-divider>
          <el-form-item label="超时时间">
            <el-input-number
                v-model="form.config.timeoutMs"
                :min="1000"
                :max="3600000"
                :step="1000"
                :step-strictly="true"
                placeholder="毫秒"
                @change="emitUpdate"
            />
            <div class="form-tips">节点执行超时时间，默认5分钟（300000ms）</div>
          </el-form-item>
          <el-form-item label="重试次数">
            <el-input-number
                v-model="form.config.maxRetries"
                :min="0"
                :max="10"
                :step="1"
                placeholder="次"
                @change="emitUpdate"
            />
            <div class="form-tips">执行失败时的重试次数，0表示不重试</div>
          </el-form-item>
          <el-form-item label="重试间隔">
            <el-input-number
                v-model="form.config.retryInitialInterval"
                :min="100"
                :max="60000"
                :step="100"
                :step-strictly="true"
                placeholder="毫秒"
                @change="emitUpdate"
            />
            <div class="form-tips">首次重试等待时间，默认1000ms</div>
          </el-form-item>
          <el-form-item label="间隔倍数">
            <el-input-number
                v-model="form.config.retryMultiplier"
                :min="1"
                :max="10"
                :step="0.1"
                :precision="1"
                placeholder="倍"
                @change="emitUpdate"
            />
            <div class="form-tips">每次重试间隔的递增倍数，默认2.0</div>
          </el-form-item>
          <el-form-item label="最大间隔">
            <el-input-number
                v-model="form.config.retryMaxInterval"
                :min="1000"
                :max="300000"
                :step="1000"
                :step-strictly="true"
                placeholder="毫秒"
                @change="emitUpdate"
            />
            <div class="form-tips">重试间隔上限，默认30000ms</div>
          </el-form-item>
        </template>
      </el-form>
    </div>
    <div class="drawer-footer">
      <el-button
          type="danger"
          size="small"
          style="width:100%"
          @click="$emit('delete-node', node.id)">
        <el-icon><Delete /></el-icon>删除节点
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import DocParseConfig from './propertyPanel/DocParseConfig.vue';
import NotifyConfig from './propertyPanel/NotifyConfig.vue';
import KbArchiveConfig from './propertyPanel/KbArchiveConfig.vue';
import LlmTaskConfig from './propertyPanel/LlmTaskConfig.vue';
import AgentTaskConfig from './propertyPanel/AgentTaskConfig.vue';
import KbSearchConfig from './propertyPanel/KbSearchConfig.vue';
import KgSearchConfig from './propertyPanel/KgSearchConfig.vue';
import VariableOpConfig from './propertyPanel/VariableOpConfig.vue';
import CodeExecuteConfig from './propertyPanel/CodeExecuteConfig.vue';
import HttpRequestConfig from './propertyPanel/HttpRequestConfig.vue';
import HumanReviewConfig from './propertyPanel/HumanReviewConfig.vue';

const props = defineProps({
  node: { type: Object, default: null },
  edge: { type: Object, default: null },
  nodes: { type: Array, default: () => [] },
  formFields: { type: Array, default: () => [] }
});
const emit = defineEmits(['update', 'close', 'delete-node', 'delete-edge']);

const form = ref({});

/** 执行策略默认值 */
const retryTimeoutDefaults = {
  timeoutMs: 300000,
  maxRetries: 3,
  retryInitialInterval: 1000,
  retryMultiplier: 2.0,
  retryMaxInterval: 30000
};

/** 需要执行策略的节点类型 */
const needRetryTimeout = (type) => {
  return !['startEvent', 'endEvent', 'exclusiveGateway', 'parallelGateway'].includes(type);
};

watch(() => props.node, (newNode) => {
  if (newNode) {
    const data = JSON.parse(JSON.stringify(newNode));
    // 确保 config 对象存在
    if (!data.config) {
      data.config = {};
    }
    // 为需要的节点类型自动填充执行策略默认值
    if (needRetryTimeout(data.type)) {
      Object.keys(retryTimeoutDefaults).forEach(key => {
        if (data.config[key] === undefined || data.config[key] === null) {
          data.config[key] = retryTimeoutDefaults[key];
        }
      });
    }
    form.value = data;
  } else {
    form.value = {};
  }
}, { immediate: true, deep: true });

function emitUpdate() {
  emit('update', form.value);
}

/** 表单字段选项（label/value），用于条件构建器的字段下拉选择 */
const fieldOptions = computed(() =>
  props.formFields.map(f => ({ label: f.label, value: f.code }))
);

/** 获取节点名称用于连线信息展示 */
function getNodeName(nodeId) {
  const n = props.nodes.find(n => n.id === nodeId);
  return n ? n.name : '未知节点';
}

/**
 * 新建一行条件
 * @returns {{source: string, field: string, relation: string, value: string, logic: string}}
 */
function newCondition() {
  return { source: 'form', field: '', relation: '', value: '', logic: '&&' };
}

/** 条件构建器状态：多条件列表 */
const conditions = ref([newCondition()]);

/** 原表达式无法可视化解析时为 true，此时保留原表达式不被空条件覆盖 */
const parseFailed = ref(false);

/** 选中连线变化时，解析已有条件表达式回填 */
watch(() => props.edge, (edge) => {
  const rows = parseCondition(edge ? edge.conditionExpression : '');
  if (rows) {
    conditions.value = rows;
    parseFailed.value = false;
  } else {
    conditions.value = [newCondition()];
    parseFailed.value = true;
  }
}, { immediate: true });

/** 判断连线源节点是否为排他网关（条件分支） */
function isConditionBranch(edge) {
  const source = props.nodes.find(el => el.id === edge.source);
  return !!source && source.type === 'exclusiveGateway';
}

/** 添加一行条件 */
function addCondition() {
  conditions.value.push(newCondition());
}

/** 删除一行条件（至少保留一行） */
function removeCondition(idx) {
  if (conditions.value.length > 1) {
    conditions.value.splice(idx, 1);
    syncCondition();
  }
}

/** 根据构建器状态生成条件表达式，并写回连线 */
function syncCondition() {
  if (!props.edge) {
    return;
  }
  const expr = buildCondition(conditions.value);
  // 原表达式为高级写法且用户尚未填好任何条件时，不覆盖原表达式
  if (!expr && parseFailed.value) {
    return;
  }
  props.edge.conditionExpression = expr;
}

/**
 * 生成 el 表达式（多条件以 且/或 连接）
 * @param rows 条件列表 [{ field, relation, value, logic }]
 * @returns {string} 如 ${amount > 80 && status == '1'}，无完整条件时返回空串
 */
function buildCondition(rows) {
  const fragments = [];
  rows.forEach((row) => {
    const fragment = buildFragment(row);
    if (fragment) {
      fragments.push({ logic: row.logic || '&&', expr: fragment });
    }
  });
  if (!fragments.length) {
    return '';
  }
  return `\${${fragments.slice(1).reduce((acc, f) => `${acc} ${f.logic} ${f.expr}`, fragments[0].expr)}}`;
}

/**
 * 生成单个条件片段（不带 ${} 包裹）
 * @param c 条件 { field, relation, value }
 * @returns {string} 如 amount > 80 / status == '1'，不完整时返回空串
 */
function buildFragment(c) {
  if (!c.field || !c.relation || c.value === undefined || c.value === '') {
    return '';
  }
  const code = c.field;
  if (c.relation === 'contains') {
    return `${code}.contains('${c.value}')`;
  }
  // 纯数字值不加引号（数值比较），否则加单引号（字符串比较）
  const isNumeric = /^-?\d+(\.\d+)?$/.test(c.value);
  const val = isNumeric ? c.value : `'${c.value}'`;
  return `${code} ${c.relation} ${val}`;
}

/**
 * 解析条件表达式回填构建器
 * @param expr 条件表达式
 * @returns {Array|null} 条件列表（每行含 logic），无法解析时返回 null
 */
function parseCondition(expr) {
  if (!expr) {
    return [newCondition()];
  }
  let s = String(expr).trim();
  if (s.startsWith('${') && s.endsWith('}')) {
    s = s.slice(2, -1);
  }
  const { parts, logics } = splitFragments(s);
  const rows = [];
  for (let i = 0; i < parts.length; i++) {
    const row = parseFragment(parts[i]);
    if (!row) {
      return null;
    }
    row.logic = i === 0 ? '&&' : (logics[i - 1] || '&&');
    rows.push(row);
  }
  return rows.length ? rows : null;
}

/**
 * 在顶层按 && / || 拆分表达式（引号内的连接符不拆分）
 * @param s 表达式（不含 ${} 包裹）
 * @returns {{parts: string[], logics: string[]}} 片段列表与各片段前的连接符（logics[i] 位于 parts[i] 与 parts[i+1] 之间）
 */
function splitFragments(s) {
  const parts = [];
  const logics = [];
  let cur = '';
  let inQuote = false;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch === "'") {
      inQuote = !inQuote;
      cur += ch;
    } else if (!inQuote && (s.startsWith('&&', i) || s.startsWith('||', i))) {
      logics.push(s.substr(i, 2));
      parts.push(cur.trim());
      cur = '';
      i++;
    } else {
      cur += ch;
    }
  }
  parts.push(cur.trim());
  return { parts, logics };
}

/**
 * 解析单个条件片段
 * @param frag 片段（如 amount > 80）
 * @returns {{field: string, relation: string, value: string}|null} 无法解析时返回 null
 */
function parseFragment(frag) {
  const c = { source: 'form', field: '', relation: '', value: '', logic: '&&' };
  // 包含：field.contains('value')（字段支持节点ID前缀，如 nodeId.answer）
  let m = frag.match(/^([\w.]+)\.contains\(['"](.*)['"]\)$/);
  if (m) {
    c.field = m[1];
    c.relation = 'contains';
    c.value = m[2];
    return fillSource(c);
  }
  // 带引号：field == 'value'
  m = frag.match(/^([\w.]+)\s*(==|!=|>=|<=|>|<)\s*['"](.*)['"]$/);
  if (m) {
    c.field = m[1];
    c.relation = m[2];
    c.value = m[3];
    return fillSource(c);
  }
  // 无引号：field > 80
  m = frag.match(/^([\w.]+)\s*(==|!=|>=|<=|>|<)\s*(.*)$/);
  if (m) {
    c.field = m[1];
    c.relation = m[2];
    c.value = m[3];
    return fillSource(c);
  }
  return null;
}

/**
 * 回填条件来源：能匹配表单字段编码视为表单值，否则视为节点数据
 * @param c 已解析的条件
 * @returns {object} 条件对象
 */
function fillSource(c) {
  c.source = fieldOptions.value.some(f => f.value === c.field) ? 'form' : 'node';
  return c;
}
</script>

<style scoped lang="scss">
.property-drawer {
  width: 400px;
  background: #fff;
  border-left: 1px solid $border-color;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid $border-color-light;
}
.drawer-title {
  font-weight: 600;
  font-size: 14px;
  color: $color-text-primary;
}
.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}
.drawer-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid $border-color-light;
}
.edge-condition-config {
  width: 100%;
}
.condition-builder {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.condition-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  border: 1px solid $border-color-light;
  border-radius: 4px;
  background: #fafafa;
}
.condition-row-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logic-select {
    width: 64px;
  }

  .logic-label {
    font-size: 12px;
    color: $color-text-secondary;
  }
}

.form-tips {
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
