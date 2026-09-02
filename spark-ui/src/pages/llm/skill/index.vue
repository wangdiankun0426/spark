<template>
  <div class="app-container">
    <!-- 顶部头部 -->
    <div class="skill-header">
      <div class="skill-header-left">
        <div class="skill-header-title">
          <el-icon class="skill-header-icon">
            <component :is="Skill" />
          </el-icon>
          技能库
        </div>
        <div class="skill-header-subtitle">浏览平台沉淀的技能，智能体命中技能时将按其指令执行</div>
      </div>
      <div class="skill-header-right">
        <el-input
            v-model="keyword"
            placeholder="搜索技能名称"
            clearable
            :prefix-icon="Search"
            style="width: 240px"
        />
      </div>
    </div>

    <!-- 技能卡片网格 -->
    <div class="skill-grid" v-if="skills.length">
      <info-card
          v-for="skill in skills"
          :key="skill.id"
          :theme="getTheme(skill)"
          :icon="Skill"
          :title="skill.name"
          :id-text="'编号 #' + skill.id"
          :description="skill.description || '暂无描述'"
          :height="220"
          @click="handleOpenSkill(skill)"
      >
        <template #action>
          <span class="action-item">
            查看
            <el-icon><ArrowRight /></el-icon>
          </span>
        </template>
      </info-card>
    </div>
    <!-- 空状态 -->
    <el-empty v-else :description="keyword ? '未找到匹配的技能' : '暂无技能'" :image-size="120" />

    <!-- 技能详情抽屉 -->
    <el-drawer
        v-model="drawerVisible"
        :title="currentSkill?.name || ''"
        direction="ltr"
        size="55%"
    >
      <div v-loading="loading">
        <template v-if="currentSkill">
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="编号">{{ currentSkill.id }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentSkill.status === 1 ? 'success' : 'info'" size="small">
                {{ currentSkill.status === 1 ? '已启用' : '已停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ currentSkill.description }}
            </el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">技能内容</el-divider>
          <v-md-preview v-if="currentSkill.content" :text="currentSkill.content"/>
          <el-empty v-else description="该技能暂无内容" :image-size="80"/>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { pageSkillListAPI, querySkillDetailAPI } from '@/api/llm/skill.js'
import { Search, ArrowRight } from '@element-plus/icons-vue'
import Skill from "@/assets/icons/skill.vue";
import InfoCard from '@/components/InfoCard/index.vue'

const skills = ref([])
const keyword = ref('')
const drawerVisible = ref(false)
const loading = ref(false)
const currentSkill = ref(null)

// 主题色循环，与 agent 卡片保持一致
const themes = ['blue', 'green', 'purple', 'orange', 'cyan', 'pink', 'indigo']

let searchTimer = null

onMounted(() => {
  loadSkills()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

/**
 * 名称搜索防抖，300ms 后走接口查询
 */
watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    loadSkills()
  }, 300)
})

/**
 * 加载启用的技能列表，携带名称关键字
 */
function loadSkills() {
  const query = { page: false, status: 1 }
  if (keyword.value) {
    query.name = keyword.value
  }
  pageSkillListAPI(query).then(res => {
    if (res.data && res.data.rows) {
      skills.value = res.data.rows
    }
  })
}

/**
 * 根据 skill.id 计算主题名
 * @param skill
 */
function getTheme(skill) {
  return themes[skill.id % themes.length]
}

/**
 * 打开技能详情抽屉，拉取技能指令全文
 * @param skill
 */
function handleOpenSkill(skill) {
  drawerVisible.value = true
  loading.value = true
  currentSkill.value = skill
  const query = { id: skill.id }
  querySkillDetailAPI(query).then(res => {
    if (res.data) {
      currentSkill.value = res.data
    }
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped lang="scss">
.skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: $spacing-md $spacing-xl;
  background-color: $bg-card;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-card;
}

.skill-header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.skill-header-title {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  font-size: 20px;
  font-weight: 700;
  color: $color-text-primary;
}

.skill-header-icon {
  font-size: 24px;
  color: $color-primary;
}

.skill-header-subtitle {
  font-size: 13px;
  color: $color-text-secondary;
}

.skill-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.skill-grid {
  height: calc(100vh - #{$nav-height} - 180px);
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
  align-content: start;
}
</style>
