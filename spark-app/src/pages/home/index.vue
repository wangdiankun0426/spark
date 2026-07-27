<template>
  <view class="home-container safe-area-page">
    <!-- 顶部欢迎卡片（固定） -->
    <view class="welcome-card">
      <view class="welcome-top">
        <user-avatar type="user" :userId="userInfo.id" :name="userInfo.name" :size="100"/>
        <view class="welcome-info">
          <text class="welcome-greeting">Hello {{ userInfo.name }}（{{ userInfo.loginName }}），{{ greeting }}</text>
          <text class="welcome-slogan">欢迎使用星火云AI平台，聚微光成智，燃无限可能</text>
        </view>
      </view>
    </view>

    <!-- 滚动内容区 -->
    <view class="home-scroll-body">
      <!-- 数据卡片区 -->
      <DashboardCard title="数据概览" icon="grid-fill">
        <view class="data-card-grid">
          <view class="data-card" v-for="item in dataList" :key="item.name">
            <view class="data-info">
              <text class="data-name">{{ item.name }}</text>
              <text class="data-value">{{ item.value }}</text>
            </view>
            <view class="data-icon-wrap">
              <up-icon :name="item.icon" size="28" :color="item.color"></up-icon>
            </view>
          </view>
        </view>
      </DashboardCard>

      <!-- 知识库解析与检索评测 -->
      <DashboardCard title="知识库解析与检索评测" icon="file-text-fill">
        <view class="eval-section">
          <view class="eval-metric" v-for="m in kbEvalMetrics" :key="m.name">
            <view class="metric-row">
              <text class="metric-name">{{ m.name }}</text>
              <view class="metric-right">
                <text class="metric-value">{{ m.value }}</text>
                <up-icon v-if="m.trend === 'up'" name="arrow-upward" size="14" color="#ff4d4f"></up-icon>
                <up-icon v-else-if="m.trend === 'down'" name="arrow-downward" size="14" color="#52c41a"></up-icon>
                <text v-else class="trend-flat">-</text>
              </view>
            </view>
            <up-line-progress :percentage="m.progress" :activeColor="m.color" height="6" />
          </view>
        </view>
      </DashboardCard>

      <!-- 知识图谱构建与提炼 -->
      <DashboardCard title="知识图谱构建与提炼" icon="share-fill">
        <view class="eval-section">
          <view class="eval-metric kg-metric" v-for="m in kgEvalMetrics" :key="m.name">
            <text class="metric-name">{{ m.name }}</text>
            <view class="metric-right">
              <text class="metric-value">{{ m.value }}</text>
              <up-icon v-if="m.trend === 'up'" name="arrow-upward" size="14" color="#ff4d4f"></up-icon>
              <up-icon v-else-if="m.trend === 'down'" name="arrow-downward" size="14" color="#52c41a"></up-icon>
              <text v-else class="trend-flat">-</text>
            </view>
          </view>
        </view>
      </DashboardCard>

      <!-- 知识图谱构建与提炼 -->
      <DashboardCard title="知识图谱构建与提炼" icon="share-fill">
        <view class="eval-section">
          <view class="eval-metric kg-metric" v-for="m in kgEvalMetrics" :key="m.name">
            <text class="metric-name">{{ m.name }}</text>
            <view class="metric-right">
              <text class="metric-value">{{ m.value }}</text>
              <up-icon v-if="m.trend === 'up'" name="arrow-upward" size="14" color="#ff4d4f"></up-icon>
              <up-icon v-else-if="m.trend === 'down'" name="arrow-downward" size="14" color="#52c41a"></up-icon>
              <text v-else class="trend-flat">-</text>
            </view>
          </view>
        </view>
      </DashboardCard>

      <!-- 知识图谱构建与提炼 -->
      <DashboardCard title="知识图谱构建与提炼" icon="share-fill">
        <view class="eval-section">
          <view class="eval-metric kg-metric" v-for="m in kgEvalMetrics" :key="m.name">
            <text class="metric-name">{{ m.name }}</text>
            <view class="metric-right">
              <text class="metric-value">{{ m.value }}</text>
              <up-icon v-if="m.trend === 'up'" name="arrow-upward" size="14" color="#ff4d4f"></up-icon>
              <up-icon v-else-if="m.trend === 'down'" name="arrow-downward" size="14" color="#52c41a"></up-icon>
              <text v-else class="trend-flat">-</text>
            </view>
          </view>
        </view>
      </DashboardCard>

      <!-- 知识图谱构建与提炼 -->
      <DashboardCard title="知识图谱构建与提炼" icon="share-fill">
        <view class="eval-section">
          <view class="eval-metric kg-metric" v-for="m in kgEvalMetrics" :key="m.name">
            <text class="metric-name">{{ m.name }}</text>
            <view class="metric-right">
              <text class="metric-value">{{ m.value }}</text>
              <up-icon v-if="m.trend === 'up'" name="arrow-upward" size="14" color="#ff4d4f"></up-icon>
              <up-icon v-else-if="m.trend === 'down'" name="arrow-downward" size="14" color="#52c41a"></up-icon>
              <text v-else class="trend-flat">-</text>
            </view>
          </view>
        </view>
      </DashboardCard>

    </view>

    <!-- 底部导航栏 -->
    <up-tabbar :value="active" @change="handleOnTabChange" activeColor="#1890ff">
      <up-tabbar-item name="home" icon="home-fill" text="首页"/>
      <up-tabbar-item name="contacts" icon="man-add-fill" text="通讯录"/>
      <up-tabbar-item name="agent" icon="grid-fill" text="智能体"/>
      <up-tabbar-item name="message" icon="chat-fill" text="通知"/>
      <up-tabbar-item name="my" icon="account" text="我的"/>
    </up-tabbar>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from "vue"
import { useStore } from "vuex"
import DashboardCard from "@/components/home/DashboardCard.vue"
import UserAvatar from "@/components/UserAvatar/index.vue"
import { userDetailAPI } from "@/api/system/user"

const store = useStore()
const active = ref("home")

const userInfo = computed(() => store.getters["user/getUserInfo"] || {
  id: undefined,
  name: "用户",
  loginName: "unknown"
})

onMounted(() => {
  loadUserInfo()
})

function loadUserInfo() {
  userDetailAPI().then(res => {
    if (res.code === 200) {
      store.dispatch("user/setUserInfo", { userInfo: res.data })
    }
  }).catch(err => {
    console.error("获取用户信息失败", err)
  })
}
// 根据时间段显示问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 写死的固定数据 - 数据卡片
const dataList = ref([
  { name: '知识库文档', value: 128, icon: 'file-text-fill', color: '#1890ff' },
  { name: '知识图谱实体', value: 56320, icon: 'share-fill', color: '#722ed1' },
  { name: '三元组数量', value: 189450, icon: 'tags-fill', color: '#13c2c2' },
  { name: '本月抽取', value: 12560, icon: 'calendar-fill', color: '#52c41a' }
])

// 写死的固定数据 - 知识库评测指标
const kbEvalMetrics = ref([
  { name: '文档解析成功率', value: '98.5%', trend: 'up', progress: 98.5, color: '#52c41a' },
  { name: '检索召回率', value: '92.3%', trend: 'up', progress: 92.3, color: '#1890ff' },
  { name: '检索精确率', value: '88.7%', trend: '-', progress: 88.7, color: '#faad14' }
])

// 写死的固定数据 - 知识图谱指标
const kgEvalMetrics = ref([
  { name: '三元组总量', value: '189,450', trend: 'up' },
  { name: '本月新增', value: '12,560', trend: 'up' },
  { name: '实体覆盖率', value: '87.3%', trend: 'up' }
])

function handleOnTabChange(index) {
  uni.reLaunch({
    url: '/pages/' + index + '/index'
  })
}
</script>

<style scoped lang="scss">
.home-container {
  height: 100vh;
  overflow: hidden;
  background-color: $uni-bg-color-dashboard;
}

/* 滚动内容区 */
.home-scroll-body {
  /* #ifdef H5 */
  height: calc(100vh - 230px);
  /* #endif */
  /* #ifdef MP-WEIXIN */
  height: calc(100vh - 310px);
  /* #endif */
  overflow-y: auto;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #0052cc 0%, #1890ff 100%);
  padding: 40px 16px 10px;
  color: #fff;
}

.welcome-top {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.welcome-info {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
}

.welcome-greeting {
  font-size: 16px;
  font-weight: 600;
  display: block;
  margin-bottom: 4px;
  line-height: 1.4;
}

.welcome-slogan {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.5;
}


/* 数据卡片区 */
.data-card-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -6px;

  .data-card {
    width: calc(50% - 12px);
    margin: 2px;
  }
}

.data-card {
  background-color: #fafbfc;
  border-radius: 8px;
  margin: 20px;
  display: flex;
}

.data-info {
  flex: 1;
  min-width: 0;
}

.data-name {
  font-size: 13px;
  color: #666;
  display: block;
  margin-bottom: 6px;
}

.data-value {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}

.data-icon-wrap {
  flex-shrink: 0;
  margin-left: 8px;
}

/* 评测指标区 */
.eval-section {
  display: flex;
  flex-direction: column;
}

.eval-metric {
  margin-top: 16px;

  .metric-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
}

.eval-metric:first-child {
  margin-top: 0;
}

.kg-metric {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.metric-name {
  font-size: 13px;
  color: #666;
}

.metric-right {
  display: flex;
  align-items: center;
}

.metric-value {
  margin-right: 4px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.trend-flat {
  font-size: 14px;
  color: #999;
}

</style>

