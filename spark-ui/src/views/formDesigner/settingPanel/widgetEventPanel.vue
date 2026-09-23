<template>
  <div class="widget-event-panel">
    <el-divider>事件配置</el-divider>
    <!--事件清单常驻面板：点哪一项就在抽屉里打开对应事件的规则表-->
    <div class="event-list">
      <div
          v-for="item in WIDGET_EVENT_LIST"
          :key="item.value"
          class="event-list-item"
          :class="{ 'has-rules': rulesOf(item.value).length > 0 }"
          @click="openEvent(item.value)"
      >
        <span class="event-name">{{ item.label }}</span>
        <span class="event-count">{{ rulesOf(item.value).length }}</span>
      </div>
    </div>
    <el-drawer
        v-model="drawerVisible"
        title="事件配置"
        direction="ltr"
        size="80%"
    >
      <div class="drawer-body">
        <el-tabs v-model="activeEvent">
          <el-tab-pane
              v-for="eventItem in WIDGET_EVENT_LIST"
              :key="eventItem.value"
              :name="eventItem.value"
              :label="tabLabel(eventItem)"
          >
            <div class="rule-table">
              <div class="rule-row rule-head">
                <span class="col-action">动作</span>
                <span class="col-target">目标字段</span>
                <span class="col-value">值</span>
                <span class="col-cond">条件</span>
                <span class="col-del"></span>
              </div>
              <div
                  v-for="(rule, index) in rulesOf(activeEvent)"
                  :key="index"
                  class="rule-row"
              >
                <el-select
                    v-model="rule.action"
                    class="col-action"
                    @change="handleActionChange(rule)"
                >
                  <el-option
                      v-for="item in FORM_ACTION_LIST"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
                <el-select
                    v-model="rule.target"
                    class="col-target"
                    filterable
                    placeholder="选择目标字段"
                >
                  <el-option
                      v-for="widget in targetOptions(rule.action)"
                      :key="widget.config.code"
                      :label="widget.config.label"
                      :value="widget.config.code"
                  />
                </el-select>
                <div class="col-value">
                  <template v-if="needValue(rule.action)">
                    <el-switch
                        v-if="isBooleanAction(rule.action)"
                        v-model="rule.value"
                    />
                    <el-input
                        v-else
                        v-model="rule.value"
                        placeholder="可用 #{字段编码} 引用其他字段值"
                    />
                  </template>
                </div>
                <div class="col-cond">
                  <span class="cond-label">当</span>
                  <el-select
                      :model-value="rule.when ? rule.when.field : ''"
                      class="cond-field"
                      placeholder="无条件"
                      @change="handleWhenFieldChange(rule, $event)"
                  >
                    <el-option
                        label="无条件"
                        value=""
                    />
                    <el-option
                        v-for="widget in props.widgetList"
                        :key="widget.config.code"
                        :label="widget.config.label"
                        :value="widget.config.code"
                    />
                  </el-select>
                  <template v-if="hasWhen(rule)">
                    <el-select
                        v-model="rule.when.op"
                        class="cond-op"
                    >
                      <el-option
                          v-for="item in FORM_CONDITION_OP_LIST"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                      />
                    </el-select>
                    <el-input
                        v-if="needCompare(rule.when.op)"
                        v-model="rule.when.value"
                        class="cond-value"
                        :placeholder="condValuePlaceholder(rule.when.op)"
                    />
                  </template>
                </div>
                <el-button
                    link
                    type="danger"
                    class="col-del"
                    @click="removeRule(activeEvent, index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div
                  v-if="!rulesOf(activeEvent).length"
                  class="rule-empty"
              >该事件暂无规则，点击下方添加</div>
            </div>
            <div class="rule-footer">
              <el-button
                  link
                  type="primary"
                  @click="addRule(activeEvent)"
              >
                <el-icon><Plus /></el-icon>添加规则
              </el-button>
              <span class="rule-count">共 {{ rulesOf(activeEvent).length }} 条</span>
            </div>
          </el-tab-pane>
        </el-tabs>
        <div class="event-tip">
          条件选「无条件」表示无条件执行；多条规则全部执行，不按命中即停；赋值支持 #{字段编码} 取其他字段当前值。
          修改直接作用于画布数据，点击「保存模板」后才落库。
        </div>
      </div>
    </el-drawer>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import {
  WIDGET_EVENT_LIST,
  FORM_ACTION_LIST,
  FORM_CONDITION_OP_LIST,
  LOCAL_VALUE_TYPES,
  CLEAR_BLOCKED_TYPES,
} from '@/components/FormView/formEvent.js'

const props = defineProps({
  widget: { type: Object, required: true },
  widgetList: { type: Array, default: () => [] },
})

// 抽屉开关
const drawerVisible = ref(false)

// 当前查看的事件 tab
const activeEvent = ref(WIDGET_EVENT_LIST[0].value)

// 兼容历史已保存的表单：老数据没有 events 字段，打开面板时惰性初始化
props.widget.config.events = props.widget.config.events || {}

/**
 * 从面板的事件清单打开对应事件的规则配置
 * @param eventName 事件名
 */
function openEvent(eventName) {
  activeEvent.value = eventName
  drawerVisible.value = true
}

/**
 * 取某个事件下的规则列表
 * @param eventName 事件名
 * @returns {Array}
 */
function rulesOf(eventName) {
  const rules = props.widget.config.events[eventName]
  return Array.isArray(rules) ? rules : []
}

/**
 * 新增一条规则，动作值与条件按默认动作初始化
 * @param eventName 事件名
 */
function addRule(eventName) {
  if (!Array.isArray(props.widget.config.events[eventName])) {
    props.widget.config.events[eventName] = []
  }
  props.widget.config.events[eventName].push({
    when: null,
    action: 'set-hidden',
    target: '',
    value: false,
  })
}

/**
 * 删除一条规则
 * @param eventName 事件名
 * @param index 规则下标
 */
function removeRule(eventName, index) {
  const rules = props.widget.config.events[eventName]
  if (Array.isArray(rules)) {
    rules.splice(index, 1)
  }
}

/**
 * tab 标题：有规则时带上条数，方便扫一眼判断哪个事件配了东西
 * @param eventItem 事件项
 * @returns {string}
 */
function tabLabel(eventItem) {
  const count = rulesOf(eventItem.value).length
  return count ? eventItem.label + ' ' + count : eventItem.label
}

/**
 * 条件字段变更：选「无条件」时清空条件对象，否则按默认运算符初始化
 * @param rule 规则
 * @param code 字段编码，空串表示无条件
 */
function handleWhenFieldChange(rule, code) {
  if (!code) {
    rule.when = null
    return
  }
  if (!rule.when) {
    rule.when = { field: code, op: 'eq', value: '' }
  } else {
    rule.when.field = code
  }
}

/**
 * 动作切换：重置动作值，并在目标字段不再被该动作支持时清空
 * @param rule 规则
 */
function handleActionChange(rule) {
  if (isBooleanAction(rule.action)) {
    rule.value = false
  } else {
    rule.value = ''
  }
  if (rule.target && !targetOptions(rule.action).some(w => w.config.code === rule.target)) {
    rule.target = ''
  }
}

/**
 * 判断规则是否开启了条件。以 when 是否为空判断，
 * 否则运算符与比较值两项的 v-model 会绑定到空对象上
 * @param rule 规则
 * @returns {boolean}
 */
function hasWhen(rule) {
  return !!rule.when
}

/**
 * 判断动作值是否为布尔开关
 * @param action 动作类型
 * @returns {boolean}
 */
function isBooleanAction(action) {
  return ['set-hidden', 'set-disabled', 'set-required'].indexOf(action) !== -1
}

/**
 * 动作是否需要配置值
 * @param action 动作类型
 * @returns {boolean}
 */
function needValue(action) {
  const item = FORM_ACTION_LIST.find(el => el.value === action)
  return !!(item && item.needValue)
}

/**
 * 运算符是否需要比较值：为空、不为空无需比较值
 * @param op 运算符
 * @returns {boolean}
 */
function needCompare(op) {
  return op !== 'isEmpty' && op !== 'notEmpty'
}

/**
 * 比较值一栏的提示文案：包含/不包含支持填多个值，其余运算符只填单个值
 * @param op 运算符
 * @returns {string}
 */
function condValuePlaceholder(op) {
  return (op === 'in' || op === 'notIn') ? '多个值用逗号分隔' : '比较值'
}

/**
 * 按动作类型计算可选目标字段：赋值只给本地可解析类型，清空排除上传附件，且都不含当前字段
 * @param action 动作类型
 * @returns {Array}
 */
function targetOptions(action) {
  return props.widgetList.filter(widget => {
    if (widget === props.widget) {
      return false
    }
    if (action === 'set-value') {
      return LOCAL_VALUE_TYPES.indexOf(widget.type) !== -1
    }
    if (action === 'clear') {
      return CLEAR_BLOCKED_TYPES.indexOf(widget.type) === -1
    }
    return true
  })
}
</script>
<style scoped lang="scss">
.widget-event-panel {
  .event-list {
    border: 1px solid $border-color-light;
    border-radius: $border-radius-sm;
  }
  .event-list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-sm 12px;
    font-size: 13px;
    color: $color-text-secondary;
    cursor: pointer;
    transition: $transition-fast;
    &:not(:last-child) {
      border-bottom: 1px solid $border-color-light;
    }
    &:hover {
      background: var(--el-fill-color-light);
      color: $color-text-primary;
    }
    .event-count {
      color: $color-text-placeholder;
    }
    &.has-rules .event-count {
      color: $color-primary;
      font-weight: 600;
    }
  }
}
.drawer-body {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0 4px;
}
.drawer-body > .el-tabs {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
:deep(.el-tabs__header) {
  flex-shrink: 0;
}
:deep(.el-tabs__content) {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
:deep(.el-tab-pane) {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.rule-table {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  border: 1px solid $border-color-light;
  border-radius: $border-radius-sm;
}
.rule-row {
  display: grid;
  grid-template-columns: 130px 160px 210px minmax(0, 1fr) 36px;
  gap: 12px;
  align-items: center;
  padding: 12px;
}
.rule-row + .rule-row {
  border-top: 1px solid $border-color-light;
}
.rule-head {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--el-fill-color-light);
  font-size: 12px;
  color: $color-text-secondary;
}
.col-del {
  justify-self: center;
}
.col-cond {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.cond-label {
  flex-shrink: 0;
  color: $color-text-secondary;
}
.cond-field {
  flex: 1 1 150px;
  min-width: 80px;
}
.cond-op {
  flex: 0 1 110px;
  min-width: 72px;
}
.cond-value {
  flex: 1 1 150px;
  min-width: 96px;
}
.rule-empty {
  padding: 24px 0;
  font-size: 12px;
  color: $color-text-placeholder;
  text-align: center;
}
.rule-footer {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 12px;
  font-size: 12px;
  color: $color-text-secondary;
}
.event-tip {
  flex-shrink: 0;
  margin-top: 16px;
  font-size: 12px;
  line-height: 1.6;
  color: $color-text-placeholder;
}
</style>
