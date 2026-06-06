<template>
  <div class="clue-node" @click="$emit('click', $event)" :style="stylePosition">
    <!-- Handle -->
    <Handle type="target" position="left" id="left" class="handle-left" />
    <Handle type="target" position="right" id="right" class="handle-right" />
    <Handle type="source" position="left" id="left-source" class="handle-left" />
    <Handle type="source" position="right" id="right-source" class="handle-right" />

    <!-- 内容部分 -->
    <div class="clue-content">
      <div class="clue-header">
        <span class="clue-title">{{ clueData.title || '未命名线索' }}</span>
        <button class="delete-button" @click.stop="handleDelete">×</button>
      </div>
      <p class="clue-detail">{{ clueData.detail || '暂无线索描述' }}</p>
      <div class="clue-tags" v-if="clueData.tags && clueData.tags.trim()">
        <span v-for="tag in getTagsArray(clueData.tags).slice(0, 3)" :key="tag" class="clue-tag">{{ tag }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue'
import { Handle } from '@vue-flow/core'

const props = defineProps({
  id: String,
  type: String,
  data: Object,
  position: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const emit = defineEmits(['click', 'delete'])

const clueData = computed(() => props.data || {})
const stylePosition = computed(() => ({
  left: `${props.position?.x ?? 0}px`,
  top: `${props.position?.y ?? 0}px`
}))

// 将字符串标签转换为数组
const getTagsArray = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  return tags.split(',').map(tag => tag.trim()).filter(Boolean)
}

const handleDelete = (e) => {
  e.stopPropagation()
  emit('delete', props.id)
}
</script>

<style scoped>
.clue-node {
  position: absolute;
  width: 200px;
  background: #fefefe;
  border-radius: 10px;
  border: 2px solid #ddd;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  z-index: 1;

  visibility: visible !important;
  transform: none !important;
}
.clue-node:hover {
  transform: translateY(-2px);
  border-color: #4a90e2;
}
.clue-content {
  padding: 12px 14px;
}
.clue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.clue-title {
  font-size: 15px;
  font-weight: bold;
  color: #2c3e50;
}
.clue-detail {
  font-size: 13px;
  color: #555;
  margin-top: 6px;
}
.clue-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}
.clue-tag {
  background: #e8f4f8;
  color: #2980b9;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  border: 1px solid #bde4f4;
}
.delete-button {
  background: transparent;
  color: #e74c3c;
  font-size: 16px;
  border: none;
  cursor: pointer;
}
.handle-left, .handle-right {
  width: 8px;
  height: 8px;
  background: #4a90e2;
  border: 2px solid #fff;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);

  z-index: 1000;
}
.handle-left { left: -6px; }
.handle-right { right: -6px; }
</style>
