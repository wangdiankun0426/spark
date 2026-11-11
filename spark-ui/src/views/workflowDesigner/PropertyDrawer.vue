<template>
  <div class="property-drawer" @mousedown.stop>
    <!--分支条件配置-->
    <template v-if="edge">
      <div class="drawer-header">
        <span class="drawer-head">
          <span class="drawer-ico drawer-ico--edge">
            <el-icon :size="14"><Share /></el-icon>
          </span>
          <span class="drawer-title">分支配置</span>
        </span>
        <span class="drawer-actions">
          <span class="header-btn" @click="$emit('close')"><el-icon><Close /></el-icon></span>
        </span>
      </div>
      <div class="drawer-body">
        <div class="sec-card">
          <div class="sec-title">分支信息</div>
          <el-form label-position="top">
            <div class="info-row">
              <el-form-item label="所属分支" class="info-col">
                <div class="readonly-field" :title="getNodeName(edge.source)">{{ getNodeName(edge.source) }}</div>
              </el-form-item>
              <el-form-item label="汇合到" class="info-col">
                <div class="readonly-field" :title="joinName">{{ joinName }}</div>
              </el-form-item>
            </div>
            <el-form-item label="分支名称">
              <el-input v-model="edge.name" placeholder="选填，用于在画布上标注该分支" maxlength="20" />
            </el-form-item>
          </el-form>
        </div>
        <div class="sec-card">
          <div class="sec-title">分支条件</div>
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
              <el-select
                  v-else
                  v-model="cond.field"
                  placeholder="选择上游节点输出"
                  filterable
                  @change="syncCondition"
              >
                <el-option-group
                    v-for="g in nodeFieldOptions"
                    :key="g.nodeId"
                    :label="g.name"
                >
                  <el-option
                      v-for="opt in g.options"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                  />
                </el-option-group>
              </el-select>
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
            <div v-if="!edge.conditionExpression" class="condition-tip">
              未设置条件时该分支按默认路径流转；多条件用「且 / 或」组合。
            </div>
          </div>
        </div>
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
    </template>

    <!--节点配置-->
    <template v-else-if="node">
      <div class="drawer-header">
        <span class="drawer-head">
          <span class="drawer-ico" :style="{ background: nodeMeta.bgColor, color: nodeMeta.color }">
            <el-icon :size="14"><component :is="nodeMeta.icon" /></el-icon>
          </span>
          <span class="drawer-title">{{ nodeMeta.name || '节点配置' }}</span>
        </span>
        <span class="drawer-actions">
          <span class="header-btn" @click="$emit('close')"><el-icon><Close /></el-icon></span>
        </span>
      </div>
      <div class="drawer-body">
        <div class="sec-card">
          <div class="sec-title">基础信息</div>
          <el-form label-position="top">
            <el-form-item label="节点名称">
              <el-input v-model="form.name" @input="handleNameInput" />
            </el-form-item>
            <el-form-item label="节点 ID">
              <div class="readonly-field readonly-field--code" :title="node.id">{{ node.id }}</div>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="needConfig(node.type)" class="sec-card">
          <div class="sec-title">节点配置</div>
          <el-form label-position="top">
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
                v-if="node.type === 'kbSearch'"
                :form="form"
                @update="emitUpdate"
            />
            <kg-search-config
                v-if="node.type === 'kgSearch'"
                :form="form"
                @update="emitUpdate"
            />
            <code-execute-config
                v-if="node.type === 'codeExecute'"
                :form="form"
                @update="emitUpdate"
            />
            <human-review-config
                v-if="node.type === 'humanReview'"
                :form="form"
                @update="emitUpdate"
            />
            <el-empty
                v-if="!hasConfigComponent(node.type)"
                description="该节点无需配置"
                :image-size="40"
            />
          </el-form>
        </div>

        <div v-if="needRetryTimeout(node.type)" class="sec-card">
          <div class="sec-title">执行策略</div>
          <el-form label-position="top">
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
          </el-form>
        </div>
      </div>
      <!-- 开始/结束节点不可删除，连入口都不显示 -->
      <div class="drawer-footer" v-if="!isEndpoint">
        <el-button
            type="danger"
            size="small"
            style="width:100%"
            @click="$emit('delete-node', node.id)">
          <el-icon><Delete /></el-icon>删除节点
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, provide } from 'vue'
import { NODE_META } from './nodes/index.js'
import { WF_VARS_KEY, buildVarGroups, buildNodeFieldOptions } from './var/flowVariables.js'
import DocParseConfig from './propertyPanel/DocParseConfig.vue'
import NotifyConfig from './propertyPanel/NotifyConfig.vue'
import KbArchiveConfig from './propertyPanel/KbArchiveConfig.vue'
import LlmTaskConfig from './propertyPanel/LlmTaskConfig.vue'
import AgentTaskConfig from './propertyPanel/AgentTaskConfig.vue'
import KbSearchConfig from './propertyPanel/KbSearchConfig.vue'
import KgSearchConfig from './propertyPanel/KgSearchConfig.vue'
import CodeExecuteConfig from './propertyPanel/CodeExecuteConfig.vue'
import HumanReviewConfig from './propertyPanel/HumanReviewConfig.vue'

const props = defineProps({
  node: { type: Object, default: null },
  edge: { type: Object, default: null },
  nodes: { type: Array, default: () => [] },
  sequences: { type: Array, default: () => [] },
  formFields: { type: Array, default: () => [] },
})
const emit = defineEmits(['update', 'close', 'delete-node', 'delete-edge'])

const form = ref({})

const nodeMeta = computed(() => (props.node ? NODE_META[props.node.type] || {} : {}))

const isEndpoint = computed(() =>
  !!props.node && ['startEvent', 'endEvent'].includes(props.node.type)
)

/** 无配置组件的网关与端点类型不渲染配置分节 */
const NO_CONFIG_TYPES = ['startEvent', 'endEvent', 'exclusiveGateway', 'parallelGateway', 'placeholder', 'userTask']

const CONFIG_COMPONENT_TYPES = [
  'docParse', 'notify', 'kbArchive', 'llmTask', 'agentTask', 'kbSearch', 'kgSearch',
  'codeExecute', 'humanReview'
]

function needConfig(type) {
  return !NO_CONFIG_TYPES.includes(type)
}

function hasConfigComponent(type) {
  return CONFIG_COMPONENT_TYPES.includes(type)
}

const retryTimeoutDefaults = {
  timeoutMs: 300000,
  maxRetries: 3,
  retryInitialInterval: 1000,
  retryMultiplier: 2.0,
  retryMaxInterval: 30000
}

const needRetryTimeout = (type) => {
  return !NO_CONFIG_TYPES.includes(type) && type !== 'userTask'
}

watch(() => props.node, (newNode) => {
  // 名称是实时回写的：父组件换了新节点对象、但除名称外与表单一致时不再回灌表单，避免打断输入
  const f = form.value;
  if (newNode && f.id === newNode.id && f.type === newNode.type && f.x === newNode.x && f.y === newNode.y
      && f.name === newNode.name
      && JSON.stringify(f.config || {}) === JSON.stringify(newNode.config || {})) {
    return;
  }
  if (newNode) {
    const data = JSON.parse(JSON.stringify(newNode));
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

/** 节点名称：边输边同步到画布 */
function handleNameInput(val) {
  form.value.name = val;
  emitUpdate();
}

/** 表单字段选项，用于条件构建器的字段下拉选择 */
const fieldOptions = computed(() =>
  props.formFields.map(f => ({ label: f.label, value: f.code }))
);

/** 配置面板变量下拉的分组选项，经 provide 下发给各 VarField */
const varGroups = computed(() => buildVarGroups(props.node, props.nodes, props.formFields));
provide(WF_VARS_KEY, varGroups);

/** 分支条件「节点数据」字段下拉，value 为 SpEL 扁平直拼键，锚点是网关自身 */
const nodeFieldOptions = computed(() =>
  props.edge ? buildNodeFieldOptions(props.edge.source, props.nodes, props.sequences) : []
);

function getNodeName(nodeId) {
  const n = props.nodes.find(n => n.id === nodeId);
  return n ? n.name : '未知节点';
}

/**
 * 从某节点出发的广度优先可达节点序列，含起点按层序排列
 * @param startId 起点节点 id
 * @returns {string[]} 可达节点 id 列表
 */
function reachOrder(startId) {
  const order = [];
  const seen = new Set();
  const queue = [startId];
  while (queue.length) {
    const id = queue.shift();
    if (!id || seen.has(id)) continue;
    seen.add(id);
    order.push(id);
    props.sequences.forEach(e => {
      if (e.source === id) queue.push(e.target);
    });
  }
  return order;
}

/**
 * 汇合到：本分支与兄弟分支最先走到一起的节点
 * 单出口连线没有兄弟分支，直接显示它的目标节点；分支各自收尾时显示本分支的结束节点
 */
const joinName = computed(() => {
  const edge = props.edge;
  if (!edge) return '';
  const siblings = props.sequences.filter(e => e.source === edge.source && e.id !== edge.id);
  if (!siblings.length) return getNodeName(edge.target);
  const others = new Set();
  siblings.forEach(s => reachOrder(s.target).forEach(id => others.add(id)));
  const mine = reachOrder(edge.target);
  const joinId = mine.find(id => others.has(id));
  if (joinId) return getNodeName(joinId);
  const endNode = mine
    .map(id => props.nodes.find(n => n.id === id))
    .find(n => n && n.type === 'endEvent');
  return endNode ? endNode.name : '流程结束';
});

/**
 * 新建一行条件
 * @returns {{source: string, field: string, relation: string, value: string, logic: string}}
 */
function newCondition() {
  return { source: 'form', field: '', relation: '', value: '', logic: '&&' };
}

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

function addCondition() {
  conditions.value.push(newCondition());
}

/** 删除一行条件，至少保留一行 */
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
 * 生成 el 表达式，多条件以 且/或 连接
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
 * 生成单个条件片段，不带 ${} 包裹
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
  // 纯数字值不加引号按数值比较，否则加单引号按字符串比较
  const isNumeric = /^-?\d+(\.\d+)?$/.test(c.value);
  const val = isNumeric ? c.value : `'${c.value}'`;
  return `${code} ${c.relation} ${val}`;
}

/**
 * 解析条件表达式回填构建器
 * @param expr 条件表达式
 * @returns {Array|null} 条件列表，每行含 logic，无法解析时返回 null
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
 * 在顶层按 && / || 拆分表达式，引号内的连接符不拆分
 * @param s 表达式，不含 ${} 包裹
 * @returns {{parts: string[], logics: string[]}} 片段列表与各片段前的连接符
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
 * @param frag 片段，如 amount > 80
 * @returns {{field: string, relation: string, value: string}|null} 无法解析时返回 null
 */
function parseFragment(frag) {
  const c = { source: 'form', field: '', relation: '', value: '', logic: '&&' };
  // 包含：field.contains('value')，字段支持节点ID前缀
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
@use './styles/vars.scss' as *;
.property-drawer {
  position: absolute;
  top: 12px;
  right: 12px;
  bottom: 12px;
  width: $wf-panel-width;
  display: flex;
  flex-direction: column;
  background: $wf-panel-bg;
  border: 1px solid $wf-panel-border;
  border-radius: 14px;
  box-shadow: $wf-panel-shadow;
  overflow: hidden;
  z-index: 70;
}

.drawer-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid $wf-panel-border;
}

.drawer-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-ico {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

/* 连线面板：分支图标固定用告警色，与条件分支节点同色系 */
.drawer-ico--edge {
  background: $el-color-warning-light-9;
  color: $el-color-warning;
}

.drawer-title {
  font-weight: 600;
  font-size: 14px;
  color: $app-text-primary;
}

.drawer-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-btn {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  cursor: pointer;
  color: $app-text-secondary;

  &:hover {
    background: $wf-sec-bg;
    color: $wf-line-active;
  }
}

.drawer-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.sec-card {
  background: $wf-sec-bg;
  border: 1px solid $wf-panel-border;
  border-radius: 10px;
  padding: 10px 11px;
}

.sec-card :deep(.el-divider__text) {
  background-color: $wf-sec-bg;
}

.sec-title {
  font-size: 12px;
  font-weight: 600;
  color: $app-text-primary;
  margin-bottom: 9px;
}

/* 分支信息首行两列：所属分支 / 汇合到，省一行竖向空间 */
.info-row {
  display: flex;
  gap: 10px;

  .info-col {
    flex: 1;
    min-width: 0;
  }
}

.readonly-field {
  box-sizing: border-box;
  width: 100%;
  height: 32px;
  padding: 0 11px;
  display: flex;
  align-items: center;
  border: 1px solid $wf-panel-border;
  border-radius: 7px;
  background: $wf-panel-bg;
  color: $app-text-secondary;
  font-size: 13px;
  line-height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: default;
  user-select: text;
}

.readonly-field--code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  letter-spacing: 0.2px;
}

.drawer-footer {
  flex-shrink: 0;
  padding: 10px 12px;
  border-top: 1px solid $wf-panel-border;
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
  border: 1px solid $wf-panel-border;
  border-radius: 7px;
  background: $wf-panel-bg;
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
    color: $app-text-secondary;
  }
}

.condition-warning {
  font-size: 12px;
  color: $el-color-warning;
  line-height: 1.5;
}

.condition-preview {
  font-size: 11px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: $app-text-secondary;
  background: $wf-panel-bg;
  border: 1px solid $wf-panel-border;
  border-radius: 6px;
  padding: 6px 8px;
  word-break: break-all;
}

.condition-tip {
  font-size: 11.5px;
  line-height: 1.6;
  color: $app-text-placeholder;
}

.form-tips {
  font-size: 12px;
  color: $app-text-secondary;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
