<template>
  <div class="character-node" @click="$emit('click', $event)" :style="stylePosition">
    <!-- 连接 Handle -->
    <Handle type="target" position="left" id="left" class="handle handle-left" />
    <Handle type="target" position="right" id="right" class="handle handle-right" />
    <Handle type="source" position="left" id="left-source" class="handle handle-left" />
    <Handle type="source" position="right" id="right-source" class="handle handle-right" />

    <!-- 圆形人物名称 -->
    <div class="circle">
      <span class="name">{{ characterData.name || '人物' }}</span>
      <button class="delete-btn" @click.stop="handleDelete">×</button>
    </div>

    <!-- 下方信息 -->
    <div class="info-list" v-if="hasInfo">
      <div class="info-item" v-if="characterData.relatedEvents">
        📅 {{ characterData.relatedEvents }}
      </div>
      <div class="info-item" v-if="characterData.relatedClues">
        🧩 {{ characterData.relatedClues }}
      </div>
      <div class="info-item" v-if="characterData.status">
        💬 {{ characterData.status }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue'
import { Handle } from '@vue-flow/core'

const props = defineProps({
  id: String,
  data: Object,
  position: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const emit = defineEmits(['click', 'delete'])

const characterData = computed(() => props.data || {})

const hasInfo = computed(() =>
  characterData.value.relatedEvents || characterData.value.relatedClues || characterData.value.status
)

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
.character-node {
  position: absolute;
  width: 140px;
  text-align: center;
  visibility: visible !important;
  transform: none !important;
}

.circle {
  width: 100px;
  height: 100px;
  margin: 0 auto;
  border-radius: 50%;
  background: #fefefe;
  border: 3px solid #4a90e2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #2c3e50;
  position: relative;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.25s ease;
}
.circle:hover {
  border-color: #2b70c9;
  transform: scale(1.05);
}
.name {
  font-size: 16px;
  padding: 0 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #fff;
  border: 1px solid #ccc;
  color: #e74c3c;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 14px;
  font-weight: bold;
  line-height: 20px;
  text-align: center;
  cursor: pointer;
}
.delete-btn:hover {
  background: #e74c3c;
  color: #fff;
}

.info-list {
  margin-top: 8px;
  font-size: 12px;
  color: #555;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px 8px;
}

.info-item {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.handle {
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
