<template>
  <!--连线配置-->
  <div class="attr-panel" v-if="sequence">
    <div class="panel-header">
      <span class="panel-title">连线配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="panel-body">
      <el-form :model="sequence" label-width="auto">
        <el-form-item label="连线ID">
          <el-input :model-value="sequence.id" readonly />
        </el-form-item>
        <el-form-item label="源节点">
          <el-input :value="getNodeName(sequence.sourceRef)" readonly />
        </el-form-item>
        <el-form-item label="目标节点">
          <el-input :value="getNodeName(sequence.targetRef)" readonly />
        </el-form-item>
        <el-form-item v-if="isConditionBranch(sequence)" label="分支条件">
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
            <div v-if="sequence.conditionExpression" class="condition-preview">{{ sequence.conditionExpression }}</div>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <div class="panel-footer">
      <el-button
          type="danger"
          style="width:100%"
          @click="$emit('delete-sequence', sequence.id)"
      >
        <el-icon><Delete /></el-icon>删除连线
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
          <el-input :model-value="node.id" readonly />
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
            :field-options="fieldOptions"
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
import { ref, watch } from 'vue'
import AssigneeSelector from './propertyPanel/AssigneeSelector.vue'
import PermissionConfig from './propertyPanel/PermissionConfig.vue'
import UrgeConfig from './propertyPanel/UrgeConfig.vue'
import TaskConfig from './propertyPanel/TaskConfig.vue'

const props = defineProps({
  node: { type: Object, default: null },
  sequence: { type: Object, default: null },
  nodes: { type: Array, default: () => [] },
  fieldOptions: { type: Array, default: () => [] },
  nodeTasks: { type: Array, default: () => [] }
})

const emit = defineEmits(['delete-node', 'delete-sequence', 'close'])

/**
 * 新建一行条件
 * @returns {{field: string, relation: string, value: string, logic: string}}
 */
function newCondition() {
  return { field: '', relation: '', value: '', logic: '&&' }
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
 * 选中连线变化时，解析已有条件表达式回填
 */
watch(() => props.sequence, (seq) => {
  const rows = parseCondition(seq ? seq.conditionExpression : '')
  if (rows) {
    conditions.value = rows
    parseFailed.value = false
  } else {
    conditions.value = [newCondition()]
    parseFailed.value = true
  }
}, { immediate: true })

/** 获取节点名称用于连线信息展示 */
function getNodeName(refId) {
  const n = props.nodes.find(el => el.id === refId)
  return n ? n.name : '未知节点'
}

/** 判断连线源节点是否为条件分支（排他网关） */
function isConditionBranch(seq) {
  const source = props.nodes.find(el => el.id === seq.sourceRef)
  return !!source && source.type === 'exclusiveGateway'
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

/** 根据构建器状态生成条件表达式，并写回连线 */
function syncCondition() {
  if (!props.sequence) {
    return;
  }
  const expr = buildCondition(conditions.value);
  // 原表达式为高级写法且用户尚未填好任何条件时，不覆盖原表达式
  if (!expr && parseFailed.value) {
    return;
  }
  props.sequence.conditionExpression = expr;
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
  const c = { field: '', relation: '', value: '', logic: '&&' };
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
  width: 400px;
  background: #fff;
  border-left: 1px solid $border-color;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;

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

.condition-warning {
  padding: 6px 8px;
  border-radius: 4px;
  background: #fdf6ec;
  font-size: 12px;
  color: #e6a23c;
  word-break: break-all;
}

.condition-preview {
  padding: 6px 8px;
  border-radius: 4px;
  background: #f5f7fa;
  font-size: 12px;
  color: #0052cc;
  word-break: break-all;
}
</style>
