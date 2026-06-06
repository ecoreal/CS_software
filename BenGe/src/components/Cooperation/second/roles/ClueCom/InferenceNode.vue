<template>
  <div class="inference-node" @click="$emit('click', $event)" :style="stylePosition">
    <!-- Handle -->
    <Handle type="target" position="left" id="left" class="handle-left" />
    <Handle type="target" position="right" id="right" class="handle-right" />
    <Handle type="source" position="left" id="left-source" class="handle-left" />
    <Handle type="source" position="right" id="right-source" class="handle-right" />

    <!-- 内容部分 -->
    <div class="inference-content">
      <div class="inference-header">
        <span class="inference-title">{{ nodeData.title || '推理结论' }}</span>
        <button class="delete-button" @click.stop="handleDelete">×</button>
      </div>

      <p class="inference-summary">{{ nodeData.summary || '暂无结论内容' }}</p>

      <div class="evidence-list" v-if="nodeData.evidence && nodeData.evidence.length">
        <p class="evidence-title">依据：</p>
        <ul>
          <li v-for="(item, index) in nodeData.evidence" :key="index">{{ item }}</li>
        </ul>
      </div>

      <div class="conclusion-tags" v-if="nodeData.tags && nodeData.tags.length">
        <span v-for="tag in nodeData.tags.slice(0, 4)" :key="tag" class="conclusion-tag">{{ tag }}</span>
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

const nodeData = computed(() => props.data || {})
const stylePosition = computed(() => ({
  left: `${props.position?.x ?? 0}px`,
  top: `${props.position?.y ?? 0}px`
}))

const handleDelete = (e) => {
  e.stopPropagation()
  emit('delete', props.id)
}
</script>

<style scoped>
.inference-node {
  position: absolute;
  width: 280px;
  background: #f9f9f9;
  border-radius: 12px;
  border: 2px solid #4a90e2;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 1;
  visibility: visible !important;
  transform: none !important;
}
.inference-node:hover {
  transform: translateY(-2px);
  border-color: #2c5282;
}

.inference-content {
  padding: 16px;
}
.inference-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.inference-title {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}
.inference-summary {
  font-size: 14px;
  color: #333;
  margin: 10px 0;
}
.evidence-list {
  margin-top: 8px;
}
.evidence-title {
  font-weight: bold;
  font-size: 13px;
  margin-bottom: 4px;
  color: #444;
}
.evidence-list ul {
  padding-left: 20px;
  margin: 0;
  font-size: 13px;
  color: #555;
}
.conclusion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}
.conclusion-tag {
  background: #e0f0fc;
  color: #2c7edb;
  font-size: 11px;
  padding: 3px 8px;
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

.handle-left,
.handle-right {
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
.handle-left {
  left: -6px;
}
.handle-right {
  right: -6px;
}
</style>
