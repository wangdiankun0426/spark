<template>
  <el-drawer
      v-model="visible"
      title="文件事件"
      direction="ltr"
      size="30%"
      :close-on-click-modal="false"
  >
    <div class="drawer-tips">
      提示：图标为"成功"表示事件已完成，"警告"表示事件未完成或处理中。分块、索引与向量化事件需等待内容提取事件完成后才会触发，如长时间未完成请联系管理员。
    </div>
    <div class="event-list">
      <el-result
          v-if="documentEvent.contentStatus !== 4"
          :icon="documentEvent.contentStatus === 2 ? 'success' : 'warning'"
          title="内容提取事件"
          sub-title="该事件为文件内容提取事件，提取文件内的文本内容！"
      >
        <template #extra>
          <div class="event-extra">
            <el-tag type="primary" size="large">{{ documentEvent.contentStatusName }}</el-tag>
            <el-button type="warning" size="small" :loading="resetting.content" @click="handleReset('content')">
              重置
            </el-button>
          </div>
        </template>
      </el-result>
      <el-result
          v-if="documentEvent.chunkStatus !== 4"
          :icon="documentEvent.chunkStatus === 2 ? 'success' : 'warning'"
          title="分块事件"
          sub-title="该事件为对提取后的内容按块大小进行切块，块之间保留一定的重叠量，需等待文件内容提取事件执行完毕之后方可触发！"
      >
        <template #extra>
          <div class="event-extra">
            <el-tag type="primary" size="large">{{ documentEvent.chunkStatusName }}</el-tag>
            <el-button type="warning" size="small" :loading="resetting.chunk" @click="handleReset('chunk')">
              重置
            </el-button>
          </div>
        </template>
      </el-result>
      <el-result
          v-if="documentEvent.indexStatus !== 4"
          :icon="documentEvent.indexStatus === 2 ? 'success' : 'warning'"
          title="索引事件"
          sub-title="该事件为文件对象创建检索数据，需等待文件内容提取事件执行完毕之后方可触发！"
      >
        <template #extra>
          <div class="event-extra">
            <el-tag type="primary" size="large">{{ documentEvent.indexStatusName }}</el-tag>
            <el-button type="warning" size="small" :loading="resetting.index" @click="handleReset('index')">
              重置
            </el-button>
          </div>
        </template>
      </el-result>
      <el-result
          v-if="documentEvent.vectorStatus !== 4"
          :icon="documentEvent.vectorStatus === 2 ? 'success' : 'warning'"
          title="向量化事件"
          sub-title="该事件为文件进行切块，并为每一块切块生成向量化，需等待文件内容提取事件执行完毕之后方可触发！"
      >
        <template #extra>
          <div class="event-extra">
            <el-tag type="primary" size="large">{{ documentEvent.vectorStatusName }}</el-tag>
            <el-button type="warning" size="small" :loading="resetting.vector" @click="handleReset('vector')">
              重置
            </el-button>
          </div>
        </template>
      </el-result>
      <el-result
          v-if="documentEvent.graphStatus !== 4"
          :icon="documentEvent.graphStatus === 2 ? 'success' : 'warning'"
          title="知识图谱事件"
          sub-title="该事件为文件提取知识图谱，需等待文件内容提取事件执行完毕之后方可触发！"
      >
        <template #extra>
          <div class="event-extra">
            <el-tag type="primary" size="large">{{ documentEvent.graphStatusName }}</el-tag>
            <el-button type="warning" size="small" :loading="resetting.graph" @click="handleReset('graph')">
              重置
            </el-button>
          </div>
        </template>
      </el-result>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryDocumentEventDetailAPI, updateDocumentEventAPI } from '@/api/dms/documentEvent.js'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  docId: { type: [Number, String], default: undefined }
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const documentEvent = ref({})
const resetting = reactive({ view: false, content: false, chunk: false, index: false, vector: false, graph: false })

/**
 * 抽屉打开且 docId 存在时加载事件详情
 */
watch(
  () => [props.modelValue, props.docId],
  ([isOpen, docId]) => {
    if (isOpen && docId) {
      loadDocumentEvent(docId)
    }
    if (!isOpen) {
      documentEvent.value = {}
    }
  }
)

/**
 * 加载文档事件详情
 */
function loadDocumentEvent(docId) {
  queryDocumentEventDetailAPI({ docId }).then(res => {
    if (res.code !== 200) return
    documentEvent.value = res.data || {}
  })
}

/**
 * 重置事件
 * @param {'view'|'content'|'chunk'|'index'|'vector'|'graph'} type 事件类型
 */
function handleReset(type) {
  const titleMap = {
    view: '预览事件',
    content: '内容提取事件',
    chunk: '分块事件',
    index: '索引事件',
    vector: '向量化事件',
    graph: '知识图谱事件'
  }
  const fieldMap = {
    content: 'contentStatus',
    chunk: 'chunkStatus',
    index: 'indexStatus',
    vector: 'vectorStatus',
    graph: 'graphStatus'
  }
  ElMessageBox.confirm(
    `确认重置${titleMap[type]}吗？重置后将以最新内容重新触发该事件。`,
    '重置确认',
    {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      resetting[type] = true
      updateDocumentEventAPI({ id: documentEvent.value.id, [fieldMap[type]]: 1 })
        .then(res => {
          if (res.code !== 200) return
          ElMessage.success('已重置，事件处理中')
          loadDocumentEvent(props.docId)
        })
        .finally(() => {
          resetting[type] = false
        })
    })
    .catch(() => {})
}
</script>

<style scoped lang="scss">
.drawer-tips {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background-color: $color-primary-light;
  border-radius: $border-radius-sm;
  color: $color-text-secondary;
  font-size: 12px;
  line-height: 1.6;
}
.event-list {
  :deep(.el-result) {
    padding: $spacing-sm 0;
    border-bottom: 1px dashed $border-color-light;
  }
  :deep(.el-result:last-child) {
    border-bottom: 0;
  }
  .event-extra {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: $spacing-sm;
  }
}
</style>
