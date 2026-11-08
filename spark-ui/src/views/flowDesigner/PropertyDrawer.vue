<template>
  <!--分支配置-->
  <div class="attr-panel" v-if="branch">
    <div class="panel-header">
      <span class="panel-title">分支配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="panel-body">
      <el-form :model="branch.edge" label-width="auto">
        <el-form-item label="所属分支">
          <div class="readonly-field" :title="gatewayName">{{ gatewayName }}</div>
        </el-form-item>
        <el-form-item label="汇合到">
          <div class="readonly-field" :title="joinName">{{ joinName }}</div>
        </el-form-item>
        <el-form-item label="分支名称">
          <el-input v-model="branch.edge.name" placeholder="选填，用于在画布上标注该分支" />
        </el-form-item>
        <el-form-item label="分支条件">
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
              </el-select>
              <el-select
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
            <div v-if="branch.edge.conditionExpression" class="condition-preview">{{ branch.edge.conditionExpression }}</div>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="panel-footer">
      <el-button
          type="danger"
          style="width:100%"
          @click="$emit('delete-branch')"
      >
        <el-icon><Delete /></el-icon>删除分支
      </el-button>
    </div>
  </div>

  <!--节点配置-->
  <div class="attr-panel" v-else-if="node">
    <div class="panel-header">
      <span class="panel-title">节点配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="panel-body">
      <el-form :model="node" label-width="auto">
        <el-form-item label="节点ID">
          <div class="readonly-field readonly-field--code" :title="node.id">{{ node.id }}</div>
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="node.name" />
        </el-form-item>
        <assignee-selector
            v-if="node.type === 'userTask'"
            :node="node"
            :field-options="fieldOptions"
        />
        <permission-config
            v-if="node.type === 'userTask'"
            :node="node"
        />
        <urge-config
            v-if="node.type === 'userTask'"
            :node="node"
        />
        <task-config
            :node="node"
            :tasks="nodeTasks"
        />
      </el-form>
    </div>
    <div class="panel-footer">
      <el-button
          type="danger"
          style="width:100%"
          @click="$emit('delete-node', node.id)"
      >
        <el-icon><Delete /> </el-icon>删除节点
      </el-button>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, watch } from 'vue'
import AssigneeSelector from './propertyPanel/AssigneeSelector.vue'
import PermissionConfig from './propertyPanel/PermissionConfig.vue'
import UrgeConfig from './propertyPanel/UrgeConfig.vue'
import TaskConfig from './propertyPanel/TaskConfig.vue'

const props = defineProps({
  node: { type: Object, default: null },
  /** 选中的分支：{ edge, gatewayId, joinId }，edge 为分支所在的真实连线对象 */
  branch: { type: Object, default: null },
  nodes: { type: Array, default: () => [] },
  fieldOptions: { type: Array, default: () => [] },
  nodeTasks: { type: Array, default: () => [] }
})

const emit = defineEmits(['delete-node', 'delete-branch', 'close'])

/**
 * 新建一行条件
 * @returns {{field: string, relation: string, value: string, logic: string}}
 */
function newCondition() {
  return { source: 'form', field: '', relation: '', value: '', logic: '&&' }
}

/**
 * 条件构建器状态：多条件列表
 * @type {Ref<UnwrapRef<{field: string, relation: string, value: string, logic: string}[]>>}
 */
const conditions = ref([newCondition()])

/**
 * 原表达式无法可视化解析时为 true，此时保留原表达式不被空条件覆盖
 * @type {Ref<UnwrapRef<boolean>>}
 */
const parseFailed = ref(false)

/**
 * 选中分支变化时，解析已有条件表达式回填
 */
watch(() => props.branch?.edge, (edge) => {
  const rows = parseCondition(edge ? edge.conditionExpression : '')
  if (rows) {
    conditions.value = rows
    parseFailed.value = false
  } else {
    conditions.value = [newCondition()]
    parseFailed.value = true
  }
}, { immediate: true })

/** 所属分支节点名称 */
const gatewayName = computed(() => getNodeName(props.branch?.gatewayId) || '条件分支')

/** 汇合节点名称（分支走完回到的节点） */
const joinName = computed(() => getNodeName(props.branch?.joinId) || '流程结束')

/** 获取节点名称用于分支信息展示 */
function getNodeName(refId) {
  const n = props.nodes.find(el => el.id === refId)
  return n ? n.name : ''
}

/** 添加一行条件 */
function addCondition() {
  conditions.value.push(newCondition())
}

/** 删除一行条件（至少保留一行） */
function removeCondition(idx) {
  if (conditions.value.length > 1) {
    conditions.value.splice(idx, 1)
    syncCondition()
  }
}

/** 根据构建器状态生成条件表达式，并写回分支连线 */
function syncCondition() {
  if (!props.branch?.edge) {
    return;
  }
  const expr = buildCondition(conditions.value);
  // 原表达式为高级写法且用户尚未填好任何条件时，不覆盖原表达式
  if (!expr && parseFailed.value) {
    return;
  }
  props.branch.edge.conditionExpression = expr;
}

/**
 * 生成 Flowable SpEL 条件表达式（多条件以 且/或 连接）
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
  // 包含：field.contains('value')
  let m = frag.match(/^([\w]+)\.contains\(['"](.*)['"]\)$/);
  if (m) {
    c.field = m[1];
    c.relation = 'contains';
    c.value = m[2];
    return c;
  }
  // 带引号：field == 'value'
  m = frag.match(/^([\w]+)\s*(==|!=|>=|<=|>|<)\s*['"](.*)['"]$/);
  if (m) {
    c.field = m[1];
    c.relation = m[2];
    c.value = m[3];
    return c;
  }
  // 无引号：field > 80
  m = frag.match(/^([\w]+)\s*(==|!=|>=|<=|>|<)\s*(.*)$/);
  if (m) {
    c.field = m[1];
    c.relation = m[2];
    c.value = m[3];
    return c;
  }
  return null;
}
</script>

<style scoped lang="scss">
.attr-panel {
  // 画布上的浮层：覆盖在画布右侧，不挤压画布、不触发节点重新布局
  position: absolute;
  top: 12px;
  right: 12px;
  bottom: 12px;
  width: 310px;
  z-index: 80;
  background: $app-bg-card;
  border: 1px solid $border-color;
  border-radius: 10px;
  box-shadow: 0 12px 32px rgba(15, 34, 66, 0.14);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: attr-panel-in 0.18s ease;

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px $spacing-md;
    border-bottom: 1px solid $border-color-light;
    flex-shrink: 0;
  }

  .panel-body {
    flex: 1;
    overflow-y: auto;
    padding: 12px $spacing-md;
  }

  .panel-footer {
    flex-shrink: 0;
    padding: 12px $spacing-md;
    border-top: 1px solid $border-color-light;
  }

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: $color-text-primary;
  }
}

@keyframes attr-panel-in {
  from {
    transform: translateX(14px);
    opacity: 0.4;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

// 只读信息行：与输入框等高但无焦点/无边框交互，明确表达「只能看、不能改」
.readonly-field {
  box-sizing: border-box;
  width: 100%;
  height: 32px;
  padding: 0 11px;
  display: flex;
  align-items: center;
  border: 1px solid $border-color-light;
  border-radius: 4px;
  background: $app-bg-sub;
  color: $app-text-secondary;
  font-size: 13px;
  line-height: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: default;
  user-select: text;
}

// 节点ID 等技术标识用等宽字体，位数对齐更易比较
.readonly-field--code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  letter-spacing: 0.2px;
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
  background: $app-bg-sub;
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

.condition-warning {
  padding: 6px 8px;
  border-radius: 4px;
  background: $el-color-warning-light-9;
  font-size: 12px;
  color: $el-color-warning;
  word-break: break-all;
}

.condition-preview {
  padding: 6px 8px;
  border-radius: 4px;
  background: $app-bg-sub;
  font-size: 12px;
  color: $app-primary;
  word-break: break-all;
}
</style>
