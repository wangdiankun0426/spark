<template>
  <view class="flow-container safe-area-page" :class="themeClass">
    <!-- 顶部标题栏 -->
    <view class="flow-header" :style="navHeaderStyle()">
      <view class="header-left">
        <text class="header-title">流程</text>
        <view class="header-sub">
          <text class="sub-text">{{ headerStat.pre }}</text>
          <text v-if="headerStat.num" class="sub-num">{{ headerStat.num }}</text>
          <text class="sub-text">{{ headerStat.suf }}</text>
          <view
              class="header-filter"
              :class="{ 'is-on': appliedChips.length > 0 }"
              @click="openFilterSheet"
          >
            <text class="header-filter__text">筛选</text>
            <up-icon name="arrow-down" size="10" :color="filterIconColor"/>
            <text v-if="appliedChips.length > 0" class="header-filter__badge">{{ appliedChips.length }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类页签 -->
    <view class="flow-tabs">
      <view class="tabs-track">
        <view
            v-for="(tab, index) in tabList"
            :key="index"
            class="seg-tab"
            :class="{ 'is-active': index === tabIndex }"
            @click="handleTabChange(index)"
        >
          <text class="seg-tab__text">{{ tab.name }}</text>
        </view>
      </view>
    </view>

    <!-- 已选筛选条件：单个可移除，右侧一键清除 -->
    <view v-if="appliedChips.length > 0" class="applied-row">
      <view
          v-for="chip in appliedChips"
          :key="chip.key"
          class="applied-chip"
          @click="removeFilter(chip.key)"
      >
        <text class="applied-chip__text">{{ chip.label }}</text>
        <up-icon name="close" size="9" :color="brandColor"/>
      </view>
      <text class="applied-clear" @click="clearFilter">清除</text>
    </view>

    <!-- 流程实例列表 -->
    <scroll-view
        class="flow-list"
        scroll-y
        @scrolltolower="handleLoadMore"
    >
      <!-- 加载中 -->
      <view v-if="loading && instanceList.length === 0" class="loading-box">
        <up-loading-icon text="加载中..." color="#2f7bff"/>
      </view>

      <!-- 空状态 -->
      <view v-else-if="instanceList.length === 0" class="empty-card">
        <view class="empty-glow"></view>
        <view class="empty-ring"></view>
        <view class="empty-icon">
          <up-icon :name="emptyIcon" size="21" :color="brandColor"/>
        </view>
        <text class="empty-title">{{ emptyTitle }}</text>
        <text class="empty-desc">{{ emptyDesc }}</text>
        <view class="empty-btn" @click="refreshList">
          <up-icon name="reload" size="11" :color="brandColor"/>
          <text class="empty-btn__text">刷新看看</text>
        </view>
      </view>

      <!-- 实例卡片 -->
      <template v-else>
        <view
            class="instance-card"
            v-for="item in instanceList"
            :key="item.id"
            @click="handleInstanceClick(item)"
        >
          <!-- 左侧状态磁贴：底色与图标同步状态，扫一眼就能区分在办和已结 -->
          <view class="card-tile" :class="'tone-' + toneOf(item)">
            <up-icon :name="iconOf(item)" size="20" :color="toneColorOf(item)"/>
          </view>

          <view class="card-main">
            <view class="card-head">
              <text class="instance-name">{{ item.instanceName || item.name }}</text>
              <view v-if="isTodoTab" class="card-go">
                <text class="card-go__text">去审批</text>
                <up-icon name="arrow-right" size="9" :color="brandColor"/>
              </view>
              <text v-else class="instance-status" :class="'status-' + item.status">{{ item.statusName }}</text>
            </view>

            <view class="card-meta">
              <text v-if="item.typeName" class="meta-type">{{ item.typeName }}</text>
              <text v-if="item.typeName && item.levelName" class="meta-dot"></text>
              <text v-if="item.levelName" class="meta-level" :class="levelCls(item)">{{ item.levelName }}</text>
            </view>

            <view class="card-foot">
              <view class="foot-ava">
                <text class="foot-ava__text">{{ avatarText(item) }}</text>
              </view>
              <text class="foot-who">{{ item.createdByName || '—' }}</text>
              <text class="foot-when">{{ formatWhen(item.createdDt) }}</text>
            </view>
          </view>
        </view>

        <!-- 列表结束提示 -->
        <view class="end-row">
          <view class="end-line"></view>
          <text class="end-text">{{ loadStatus === 'nomore' ? '没有更多了' : '上拉加载更多' }}</text>
          <view class="end-line"></view>
        </view>
      </template>
    </scroll-view>

    <!-- 筛选面板：脱离文档流的 0 尺寸宿主，小程序端 popup 插槽内容会提前占位挤压列表 -->
    <view class="sheet-host">
      <up-popup
        :show="sheetVisible"
        mode="bottom"
        round="18"
        :bgColor="sheetBgColor"
        @close="closeSheet"
      >
      <view class="fs">
        <view class="fs-head">
          <text class="fs-title">筛选</text>
          <text class="fs-reset" @click="resetDraft">重置</text>
        </view>

        <view class="fs-group">
          <text class="fs-label">流程名称</text>
          <view class="fs-input">
            <up-icon name="search" size="14" :color="filterTextColor"/>
            <input
                class="fs-input__inner"
                v-model="draft.name"
                placeholder="输入流程名称关键字"
                placeholder-class="fs-input__ph"
                confirm-type="search"
            />
            <up-icon
                v-if="draft.name"
                name="close-circle-fill"
                size="15"
                :color="filterTextColor"
                @click="draft.name = ''"
            />
          </view>
        </view>

        <!-- 紧急程度：每个等级带色点与具体处理口径，选中态用品牌描边 -->
        <view class="fs-group">
          <text class="fs-label">紧急程度</text>
          <view class="level-grid">
            <view
                class="level-cell"
                :class="{ 'is-on': draft.level === null }"
                @click="draft.level = null"
            >
              <text class="level-cell__name">不限</text>
              <text class="level-cell__desc">全部等级</text>
            </view>
            <view
                v-for="item in levelOptions"
                :key="'lv-' + item.value"
                class="level-cell"
                :class="['lv-' + item.value, { 'is-on': draft.level === item.value }]"
                @click="draft.level = item.value"
            >
              <view class="level-cell__top">
                <view class="level-dot"></view>
                <text class="level-cell__name">{{ item.label }}</text>
              </view>
              <text class="level-cell__desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <!-- 待办列表本身就都是待处理单据，状态维度无意义，只在申请/已办/抄送里给 -->
        <view class="fs-group" v-if="!isTodoTab">
          <text class="fs-label">流程状态</text>
          <view class="fs-chips">
            <view class="fs-chip" :class="{ 'is-on': draft.statusIndex === -1 }" @click="draft.statusIndex = -1">不限</view>
            <view
                v-for="(item, index) in statusOptions"
                :key="'st-' + index"
                class="fs-chip"
                :class="{ 'is-on': draft.statusIndex === index }"
                @click="draft.statusIndex = index"
            >{{ item.label }}</view>
          </view>
        </view>

        <view class="fs-group">
          <text class="fs-label">申请时间</text>
          <view class="fs-chips">
            <view
                v-for="item in timeRanges"
                :key="item.key"
                class="fs-chip"
                :class="{ 'is-on': draft.timeKey === item.key }"
                @click="draft.timeKey = item.key"
            >{{ item.label }}</view>
          </view>
        </view>

        <view class="fs-foot">
          <view class="fs-btn is-ghost" @click="closeSheet">取消</view>
          <view class="fs-btn is-main" @click="applyFilter">确定</view>
        </view>
      </view>
      </up-popup>
    </view>

    <AppTabbar :value="active" @change="handleOnTabChange"/>
  </view>
</template>
<script setup>
import {ref, computed} from "vue";
import {onLoad} from "@dcloudio/uni-app";
import dayjs from "dayjs";
import {navHeaderStyle} from "@/utils/navBarUtil";
import {useTheme} from "@/utils/useTheme";
import {getSecondaryColor, getCardBgColor, getThemeToken} from "@/utils/themeUtil";
import AppTabbar from "@/components/AppTabbar/index.vue";
import {
  pageMyApplicationListAPI,
  pageMyTodoListAPI,
  pageMyDoneListAPI,
  pageCopyMyListAPI
} from "@/api/flow/instance";

const active = ref("flow");
const {isDark, themeClass} = useTheme();

// 筛选面板与已选条件的文字/图标色，跟随主题
const filterTextColor = computed(() => getSecondaryColor(isDark.value));
const sheetBgColor = computed(() => getCardBgColor(isDark.value));
// 品牌蓝：图标组件不吃 CSS 变量，这里取实色
const brandColor = computed(() => getThemeToken('brandIcon', isDark.value));
const filterIconColor = computed(() => (appliedChips.value.length > 0 ? brandColor.value : filterTextColor.value));

// 时间范围预设，换算成服务端要的起止时间，筛选本身在 SQL 里做
const timeRanges = [
  {key: 'all', label: '不限', days: 0},
  {key: 'today', label: '今天', days: 1},
  {key: 'week', label: '近 7 天', days: 7},
  {key: 'month', label: '近 30 天', days: 30}
];

// 紧急程度选项，与后端 FlowInstanceLevelEnum 对齐
const levelOptions = [
  {label: '一般', value: 1, desc: '常规事项'},
  {label: '重要', value: 2, desc: '优先处理'},
  {label: '紧急', value: 3, desc: '立即处理'}
];
// 流程状态分组，value 对应后端 FlowInstanceStatusEnum
const statusOptions = [
  {label: '进行中', values: [1, 2, 6]},
  {label: '审批通过', values: [3, 5]},
  {label: '审批驳回', values: [4]},
  {label: '已撤回', values: [9]}
];
// 已生效的筛选条件
const filters = ref({name: '', level: null, statusIndex: -1, timeKey: 'all'});
// 面板里正在编辑的副本，点「确定」才生效
const draft = ref({name: '', level: null, statusIndex: -1, timeKey: 'all'});
const sheetVisible = ref(false);


// 流程中心页签
const visibleTabList = [
  {name: '我的申请', api: pageMyApplicationListAPI, type: 1},
  {name: '我的待办', api: pageMyTodoListAPI, type: 3},
  {name: '我的已办', api: pageMyDoneListAPI, type: 4},
  {name: '抄送给我', api: pageCopyMyListAPI, type: 5}
];
const tabList = computed(() => visibleTabList.map(cfg => ({name: cfg.name})));
const tabIndex = ref(0);
// 当前激活页签配置
const activeTab = computed(() => visibleTabList[tabIndex.value]);
const isCopyTab = computed(() => (activeTab.value ? activeTab.value.type === 5 : false));
// 待办页签不提供状态筛选：待办本身都是待处理单据
const isTodoTab = computed(() => (activeTab.value ? activeTab.value.type === 3 : false));
const instanceList = ref([]);
const loading = ref(false);
const loadStatus = ref('loadmore');
const query = ref({pageNo: 1, pageSize: 15});
const total = ref(0);

// 页头副标题：条数与页签语境对齐，数字单独用品牌色，未拿到数据时不显示 0
const headerStat = computed(() => {
  if (loading.value && instanceList.value.length === 0) {
    return {pre: '正在加载…', num: '', suf: ''};
  }
  if (total.value === 0) {
    const emptyMap = ['还没有申请记录', '还没有待办', '还没有已办记录', '还没有抄送'];
    return {pre: emptyMap[tabIndex.value] || '', num: '', suf: ''};
  }
  const num = String(total.value);
  if (tabIndex.value === 1) {
    return {pre: '', num, suf: ' 条待办待处理'};
  }
  const sufMap = [' 条申请', '', ' 条已办记录', ' 条抄送给我'];
  return {pre: '共 ', num, suf: sufMap[tabIndex.value] || ''};
});

// 空态文案按页签区分
const emptyIcon = computed(() => {
  const icons = ['file-text', 'edit-pen', 'checkmark-circle', 'share'];
  return icons[tabIndex.value] || 'file-text';
});
const emptyTitle = computed(() => {
  const titles = ['还没有申请记录', '还没有待办', '还没有已办记录', '还没有抄送'];
  return titles[tabIndex.value] || '暂无数据';
});
const emptyDesc = computed(() => {
  const descs = [
    '你在系统里发起的流程，\n都会按时间汇总到这里',
    '有需要你审批的流程时，\n会第一时间出现在这里',
    '你审批过的流程都会留痕，\n方便随时回看处理结果',
    '别人抄送给你的流程，\n会出现在这里供你查阅'
  ];
  return descs[tabIndex.value] || '';
});

// 已选条件胶囊：面板收起后仍能看清当前筛了什么
const appliedChips = computed(() => {
  const chips = [];
  const current = filters.value;
  if (current.name) {
    chips.push({key: 'name', label: current.name});
  }
  if (current.level !== null) {
    const level = levelOptions.find(item => item.value === current.level);
    chips.push({key: 'level', label: level ? level.label : '紧急程度'});
  }
  const status = statusOptions[current.statusIndex];
  if (status && !isTodoTab.value) {
    chips.push({key: 'status', label: status.label});
  }
  const range = timeRanges.find(item => item.key === current.timeKey);
  if (range && range.days > 0) {
    chips.push({key: 'time', label: range.label});
  }
  return chips;
});

/**
 * 页面加载：支持从首页带 tab 参数直达指定页签，下标 0 申请、1 待办、2 已办、3 抄送；
 * 同时补上首屏列表加载，原先只有切换页签才请求，首次进入列表是空的
 * @param options 页面参数
 */
onLoad(options => {
  const index = Number(options && options.tab);
  if (!Number.isNaN(index) && index >= 0 && index < visibleTabList.length) {
    tabIndex.value = index;
  }
  handleGetInstanceList();
});

/**
 * 查询当前页签列表
 * 说明：列表内容不足一屏时 scroll-view 无法上拉，触底事件不会触发，
 * 接口若「总数 > 本页条数」就会永远看不到后面的数据。这里在拿到不满页的返回后自动续拉，
 * 最多续拉 3 次，避免后端总数与列表不一致时死循环。
 */
async function handleGetInstanceList() {
  const tab = activeTab.value;
  if (!tab) {
    instanceList.value = [];
    loadStatus.value = 'nomore';
    return;
  }
  loading.value = true;
  loadStatus.value = 'loading';
  let retry = 0;
  try {
    while (true) {
      const res = await tab.api(buildQuery());
      if (res.code !== 200 || !res.data) {
        instanceList.value = query.value.pageNo === 1 ? [] : instanceList.value;
        loadStatus.value = 'nomore';
        return;
      }
      const rows = res.data.rows || [];
      instanceList.value = query.value.pageNo === 1 ? rows : instanceList.value.concat(rows);
      total.value = res.data.total || 0;
      const shortPage = rows.length < query.value.pageSize;
      if (shortPage && instanceList.value.length > 0 && instanceList.value.length < total.value && retry < 3) {
        retry = retry + 1;
        query.value.pageNo = query.value.pageNo + 1;
        continue;
      }
      loadStatus.value = instanceList.value.length >= total.value ? 'nomore' : 'loadmore';
      return;
    }
  } finally {
    loading.value = false;
  }
}

/**
 * 卡片左侧磁贴的色调，取值与样式里的 tone-* 对应
 * @param item 行数据
 * @returns {string} 色调名
 */
function toneOf(item) {
  const status = item ? item.status : null;
  if (status === 2) return 'info';
  if (status === 3 || status === 5) return 'success';
  if (status === 4) return 'error';
  if (status === 9) return 'warning';
  return 'gray';
}

/**
 * 磁贴图标实色，与色调保持一致
 * @param item 行数据
 * @returns {string} 色值
 */
function toneColorOf(item) {
  const map = {info: 'info', success: 'success', error: 'error', warning: 'warning', gray: 'textTertiary'};
  return getThemeToken(map[toneOf(item)] || 'textTertiary', isDark.value);
}

/**
 * 磁贴图标：待办用待处理语义，抄送用转发语义，其余是单据
 * @param item 行数据
 * @returns {string} 图标名
 */
function iconOf(item) {
  if (isTodoTab.value) return 'edit-pen';
  if (isCopyTab.value) return 'share';
  return toneOf(item) === 'success' ? 'checkmark-circle' : 'file-text';
}

/**
 * 紧急程度配色：紧急红、重要橙，其余走次级灰
 * @param item 行数据
 * @returns {string} 样式类
 */
function levelCls(item) {
  const name = (item && item.levelName) || '';
  if (name.indexOf('急') !== -1) return 'is-urgent';
  if (name.indexOf('重要') !== -1) return 'is-important';
  return 'is-normal';
}

/**
 * 姓名首字，用作申请人的小头像
 * @param item 行数据
 * @returns {string} 单个字符
 */
function avatarText(item) {
  const name = (item && item.createdByName) || '';
  return name ? name.slice(0, 1) : '—';
}

/**
 * 列表里的时间：今天/昨天只给时分，今年给月日，跨年补年份
 * @param dt 后端时间字符串
 * @returns {string} 展示文案
 */
function formatWhen(dt) {
  if (!dt) return '';
  const time = dayjs(dt);
  if (!time.isValid()) return String(dt);
  const now = dayjs();
  const dayDiff = now.startOf('day').diff(time.startOf('day'), 'day');
  if (dayDiff === 0) return '今天 ' + time.format('HH:mm');
  if (dayDiff === 1) return '昨天 ' + time.format('HH:mm');
  if (time.year() === now.year()) return time.format('MM-DD');
  return time.format('YYYY-MM-DD');
}

/**
 * 空态里的刷新：重拉当前页签
 */
function refreshList() {
  reloadList();
}

/**
 * 切换页签
 * @param index 页签下标
 */
function handleTabChange(index) {
  if (index === tabIndex.value) {
    return;
  }
  tabIndex.value = index;
  query.value.pageNo = 1;
  instanceList.value = [];
  total.value = 0;
  handleGetInstanceList();
}

/**
 * 拼接请求参数：分页 + 已生效的筛选条件
 * @returns 请求参数
 */
function buildQuery() {
  const params = {...query.value};
  const current = filters.value;
  if (current.name) {
    params.name = current.name.trim();
  }
  if (current.level !== null) {
    params.level = current.level;
  }
  const status = statusOptions[current.statusIndex];
  if (status && !isTodoTab.value) {
    // statusList 以逗号串传给后端，由 Spring 绑定成 List<Integer>
    params.statusList = status.values.join(',');
  }
  const range = timeRanges.find(item => item.key === current.timeKey);
  if (range && range.days > 0) {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    start.setDate(start.getDate() - (range.days - 1));
    params.createdStartTime = formatDateTime(start);
    params.createdEndTime = formatDateTime(now);
  }
  return params;
}

/**
 * 格式化为 yyyy-MM-dd HH:mm:ss，与后端 DateTimeFormat 对齐
 * @param date 时间对象
 * @returns 时间字符串
 */
function formatDateTime(date) {
  const pad = n => (n < 10 ? '0' + n : '' + n);
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
      + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
}

/**
 * 打开筛选面板：把已生效条件复制进草稿
 */
function openFilterSheet() {
  draft.value = {...filters.value};
  sheetVisible.value = true;
}

/**
 * 关闭面板，未提交的改动直接丢弃
 */
function closeSheet() {
  sheetVisible.value = false;
}

/**
 * 重置草稿条件
 */
function resetDraft() {
  draft.value = {name: '', level: null, statusIndex: -1, timeKey: 'all'};
}

/**
 * 应用筛选：重置分页后重新请求
 */
function applyFilter() {
  filters.value = {...draft.value, name: (draft.value.name || '').trim()};
  sheetVisible.value = false;
  reloadList();
}

/**
 * 移除单个条件
 * @param key 条件标识
 */
function removeFilter(key) {
  if (key === 'name') {
    filters.value.name = '';
  } else if (key === 'level') {
    filters.value.level = null;
  } else if (key === 'status') {
    filters.value.statusIndex = -1;
  } else if (key === 'time') {
    filters.value.timeKey = 'all';
  }
  reloadList();
}

/**
 * 清除全部条件
 */
function clearFilter() {
  filters.value = {name: '', level: null, statusIndex: -1, timeKey: 'all'};
  reloadList();
}

/**
 * 条件变化后从第一页重新拉取
 */
function reloadList() {
  query.value.pageNo = 1;
  instanceList.value = [];
  total.value = 0;
  handleGetInstanceList();
}

/**
 * 触底加载更多
 */
function handleLoadMore() {
  if (loading.value || loadStatus.value === 'nomore') {
    return;
  }
  query.value.pageNo = query.value.pageNo + 1;
  handleGetInstanceList();
}

/**
 * 点击流程实例进入详情
 * @param item 行数据
 */
function handleInstanceClick(item) {
  uni.navigateTo({
    url: '/pages/flow/instanceDetail?id=' + (item.instanceId || item.id) + '&type=' + (activeTab.value ? activeTab.value.type : 1)
  });
}

/**
 * 底部导航栏
 * @param index
 */
function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/' + index + '/index'
  });
}
</script>
<style scoped lang="scss">
.flow-container {
  height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--app-page-base);
}

.flow-header {
  display: flex;
  align-items: flex-start;
  padding: 14px 16px 10px;
  background-color: $spark-color-header-bg;
  border-bottom: none;

  .header-left {
    flex: 1;
    min-width: 0;
  }

  .header-title {
    display: block;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 0.5px;
    color: var(--app-text-primary);
  }

  .header-sub {
    display: flex;
    align-items: center;
    margin-top: 5px;
    font-size: 11.5px;
    color: var(--app-text-tertiary);
  }

  .sub-num {
    margin: 0 3px;
    font-size: 11.5px;
    font-weight: 600;
    color: var(--app-brand);
  }
}

.header-filter {
  position: relative;
  display: flex;
  align-items: center;
  gap: 3px;
  height: 30px;
  margin-left: auto;
  padding: 0 11px;
  border-radius: 15px;
  background-color: var(--app-bg-card);
  border: 1px solid var(--app-border-light);
  box-shadow: var(--app-shadow-card);
}

.header-filter__text {
  font-size: 12.5px;
  color: var(--app-text-secondary);
}

.header-filter.is-on {
  border-color: var(--app-primary-soft);
}

.header-filter.is-on .header-filter__text {
  color: var(--app-brand);
  font-weight: 600;
}

.header-filter__badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 15px;
  height: 15px;
  padding: 0 3px;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 9.5px;
  font-weight: 700;
  line-height: 15px;
  text-align: center;
  color: var(--app-fill-tx);
  background-image: var(--app-gradient-fill);
  box-shadow: 0 0 0 2px $spark-color-header-bg;
}

.applied-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 0 14px 2px;
  background-color: $spark-color-header-bg;
}

.applied-chip {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 24px;
  padding: 0 8px;
  border-radius: 8px;
  background-color: var(--app-primary-soft);
}

.applied-chip__text {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11.5px;
  color: var(--app-brand);
}

.applied-clear {
  margin-left: auto;
  font-size: 11.5px;
  color: var(--app-text-tertiary);
}

.flow-tabs {
  padding: 0 14px 12px;
  background-color: $spark-color-header-bg;
}

.tabs-track {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 38px;
  padding: 3px;
  border-radius: 12px;
  background-color: var(--app-tab-track);
}

.seg-tab {
  flex: 1;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  transition: background 0.25s ease, box-shadow 0.25s ease;
}

.seg-tab__text {
  font-size: 12.5px;
  color: var(--app-text-secondary);
  white-space: nowrap;
  transition: color 0.25s ease;
}

.seg-tab.is-active {
  background-image: var(--app-gradient-fill);
  box-shadow: var(--app-shadow-brand);
}

.seg-tab.is-active .seg-tab__text {
  color: var(--app-fill-tx);
  font-weight: 600;
}

.flow-list {
  width: 100%;
  padding: 12px 14px 0;
  flex: 1;
  height: 0;
  min-height: 0;
  box-sizing: border-box;
  overflow-y: auto;
}

.loading-box {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.instance-card {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 12px 13px;
  margin-bottom: 10px;
  border-radius: 16px;
  background-color: var(--app-bg-card);
  border: 1px solid var(--app-border);
  box-shadow: var(--app-shadow-card);

  &:active {
    background-color: var(--app-bg-hover);
  }
}

.card-tile {
  flex: none;
  width: 40px;
  height: 40px;
  margin-top: 1px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.tone-info {
    background-color: var(--app-tile-info);
  }

  &.tone-success {
    background-color: var(--app-tile-success);
  }

  &.tone-error {
    background-color: var(--app-tile-error);
  }

  &.tone-warning {
    background-color: var(--app-tile-warning);
  }

  &.tone-gray {
    background-color: var(--app-bg-sub);
  }
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.instance-name {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.2px;
  line-height: 20px;
  color: var(--app-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.instance-status {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  height: 20px;
  padding: 0 7px;
  border-radius: 6px;
  font-size: 10.5px;
  font-weight: 600;
  line-height: 1;

  &::before {
    content: '';
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background-color: currentColor;
  }
}

.card-go {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  height: 25px;
  padding: 0 9px;
  border-radius: 8px;
  background-color: var(--app-primary-soft);
}

.card-go__text {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--app-brand);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 4px;
  font-size: 11.5px;
  line-height: 16px;
  color: var(--app-text-tertiary);
  overflow: hidden;
  white-space: nowrap;
}

.meta-type {
  flex: none;
  max-width: 55%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-dot {
  flex: none;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background-color: var(--app-text-placeholder);
}

.meta-level {
  flex: none;

  &.is-normal {
    color: var(--app-text-tertiary);
  }

  &.is-important {
    color: var(--app-text-warning);
  }

  &.is-urgent {
    color: var(--app-text-error);
    font-weight: 600;
  }
}

.card-foot {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 7px;
}

.foot-ava {
  flex: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--app-primary-soft);
}

.foot-ava__text {
  font-size: 9.5px;
  font-weight: 600;
  color: var(--app-brand);
  line-height: 1;
}

.foot-who {
  flex: 1;
  min-width: 0;
  font-size: 11.5px;
  color: var(--app-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.foot-when {
  flex: none;
  font-size: 11px;
  color: var(--app-text-placeholder);
}

.status-1,
.status-6,
.status-7,
.status-8 {
  color: var(--app-text-tertiary);
  background-color: var(--app-bg-sub);
}

.status-2 {
  color: var(--app-text-info);
  background-color: var(--app-bg-info);
}

.status-3,
.status-5 {
  color: var(--app-text-success);
  background-color: var(--app-bg-success);
}

.status-4 {
  color: var(--app-text-error);
  background-color: var(--app-bg-error);
}

.status-9 {
  color: var(--app-text-warning);
  background-color: var(--app-bg-warning);
}

.end-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 40px;
}

.end-line {
  flex: 1;
  height: 1px;
  background-color: var(--app-border-light);
}

.end-text {
  flex: none;
  font-size: 11px;
  color: var(--app-text-tertiary);
}

.empty-card {
  position: relative;
  overflow: hidden;
  margin: 2px 0 0;
  padding: 22px 18px 20px;
  border-radius: 18px;
  background-image: var(--app-gradient-hero);
  border: 1px solid var(--app-border-light);
  box-shadow: var(--app-shadow-hero);
}

.empty-glow {
  position: absolute;
  right: -46px;
  top: -52px;
  width: 168px;
  height: 168px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--app-primary-soft) 0%, rgba(47, 123, 255, 0) 68%);
}

.empty-ring {
  position: absolute;
  right: -14px;
  top: -20px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 1px solid var(--app-primary-soft);
}

.empty-icon {
  position: relative;
  width: 46px;
  height: 46px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--app-bg-card);
  border: 1px solid var(--app-border-light);
  box-shadow: var(--app-shadow-card);
}

.empty-title {
  position: relative;
  display: block;
  margin-top: 13px;
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.empty-desc {
  position: relative;
  display: block;
  margin-top: 6px;
  font-size: 11.5px;
  line-height: 1.65;
  color: var(--app-text-tertiary);
  white-space: pre-line;
}

.empty-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 14px;
  height: 30px;
  padding: 0 13px;
  border-radius: 9px;
  background-color: var(--app-primary-soft);
  border: 1px solid var(--app-primary-soft);
}

.empty-btn__text {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--app-brand);
}

.fs {
  border-radius: 18px 18px 0 0;
  padding: 18px 18px calc(20px + env(safe-area-inset-bottom));
  background-color: var(--app-bg-card);
  max-height: 78vh;
  overflow-y: auto;
}

.fs-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.fs-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--app-text-primary);
}

.fs-reset {
  font-size: 12.5px;
  color: var(--app-text-tertiary);
}

.fs-group + .fs-group {
  margin-top: 16px;
}

.fs-label {
  display: block;
  margin-bottom: 9px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--app-text-secondary);
}

.fs-input {
  display: flex;
  align-items: center;
  gap: 7px;
  height: 38px;
  padding: 0 11px;
  border-radius: 10px;
  background-color: var(--app-bg-sub);
  border: 1px solid var(--app-border-light);
}

.fs-input__inner {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: var(--app-text-primary);
}

.fs-input__ph {
  font-size: 13px;
  color: var(--app-text-tertiary);
}

.fs-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.fs-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 14px;
  border-radius: 9px;
  font-size: 12.5px;
  color: var(--app-text-secondary);
  background-color: var(--app-bg-sub);
  border: 1px solid transparent;
}

.fs-chip.is-on {
  color: var(--app-fill-tx);
  font-weight: 600;
  background-image: var(--app-gradient-fill);
  border-color: transparent;
}

.level-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.level-cell {
  width: calc(25% - 6px);
  box-sizing: border-box;
  min-height: 52px;
  padding: 7px 2px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border-radius: 10px;
  background-color: var(--app-bg-sub);
  border: 1px solid transparent;
}

.level-cell.is-on {
  border-color: var(--app-brand);
  background-color: var(--app-primary-soft);
}

.level-cell__top {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  max-width: 100%;
}

.level-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  background-color: var(--app-text-tertiary);
}

.lv-2 .level-dot {
  background-color: var(--app-text-warning);
}

.lv-3 .level-dot {
  background-color: var(--app-text-error);
}

.level-cell__name {
  font-size: 12.5px;
  font-weight: 600;
  line-height: 17px;
  color: var(--app-text-secondary);
  white-space: nowrap;
}

.level-cell.is-on .level-cell__name {
  color: var(--app-brand);
}

.level-cell__desc {
  max-width: 100%;
  font-size: 9.5px;
  line-height: 12px;
  color: var(--app-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.level-cell.is-on .level-cell__desc {
  color: var(--app-brand);
  opacity: 0.82;
}

.fs-foot {
  display: flex;
  gap: 10px;
  margin-top: 22px;
}

.fs-btn {
  flex: 1;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 600;
}

.fs-btn.is-ghost {
  color: var(--app-text-secondary);
  background-color: var(--app-bg-sub);
  border: 1px solid var(--app-border-light);
}

.fs-btn.is-main {
  color: var(--app-fill-tx);
  background-image: var(--app-gradient-fill);
  box-shadow: var(--app-shadow-brand);
}
</style>
