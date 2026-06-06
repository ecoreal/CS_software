<template>
  <div
    class="atmosphere-node"
    :class="{ selected: isSelected }"
    :style="{ 
      background: moodColorMap[data.mood] || moodColorMap.default,
      ...stylePosition
    }"
    @click="handleClick"
    @dblclick="handleDoubleClick"
  >
    <!-- 删除按钮 -->
    <button 
      v-if="isSelected"
      class="delete-btn"
      @click.stop="handleDelete"
      title="删除节点"
    >
      ×
    </button>

    <!-- 节点头部：情绪和灯光图标 -->
    <div class="node-header">
      <span class="mood-icon" :title="data.mood || '情绪氛围'">
        {{ moodIconMap[data.mood] || '🎬' }}
      </span>
      <span class="time-label" v-if="data.timeLabel">{{ data.timeLabel }}</span>
      <span class="lighting-icon" :title="data.lighting || '灯光设定'">
        {{ lightingIconMap[data.lighting] || '💡' }}
      </span>
    </div>

    <!-- 节点主体内容 -->
    <div class="node-body">
      <div class="title">{{ data.title || '氛围节点' }}</div>
      
      <div class="info-row" v-if="data.mood">
        <span class="label">氛围:</span>
        <span class="value">{{ data.mood }}</span>
      </div>
      
      <div class="info-row" v-if="data.lighting">
        <span class="label">灯光:</span>
        <span class="value">{{ data.lighting }}</span>
      </div>
      
      <div class="info-row" v-if="data.music">
        <span class="label">音效:</span>
        <span class="value">{{ data.music }}</span>
        <span class="music-icon">🎵</span>
      </div>
      
      <div class="info-row" v-if="data.weather">
        <span class="label">天气:</span>
        <span class="value">{{ data.weather }}</span>
        <span class="weather-icon">{{ weatherIconMap[data.weather] || '🌤️' }}</span>
      </div>
    </div>

    <!-- 节点底部：连接点 -->
    <div class="node-handles">
      <!-- 左侧连接点 -->
      <Handle
        type="target"
        position="left"
        id="left"
        class="handle handle-left"
      />
      
      <!-- 右侧连接点 -->
      <Handle
        type="source"
        position="right"
        id="right"
        class="handle handle-right"
      />
      
      <!-- 顶部连接点 -->
      <Handle
        type="target"
        position="top"
        id="top"
        class="handle handle-top"
      />
      
      <!-- 底部连接点 -->
      <Handle
        type="source"
        position="bottom"
        id="bottom"
        class="handle handle-bottom"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits } from 'vue'
import { Handle } from '@vue-flow/core'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  data: {
    type: Object,
    default: () => ({})
  },
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  },
  selected: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['delete', 'click', 'open'])

// 计算是否选中
const isSelected = computed(() => props.selected)

// 安全访问位置，避免 undefined 报错
const stylePosition = computed(() => {
  const x = props.position?.x ?? 0
  const y = props.position?.y ?? 0
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

// 情绪色彩映射
const moodColorMap = {
  '紧张':  'linear-gradient(135deg, #e17055 0%, #d63031 100%)',
  '温馨':  'linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%)',
  '神秘':  'linear-gradient(135deg, #6c5ce7 0%, #2d3436 100%)',
  '恐怖':  'linear-gradient(135deg, #3b3b3b 0%, #1a1a1a 100%)',
  '浪漫':  'linear-gradient(135deg, #fd79a8 0%, #fdcb6e 100%)',
  '悲伤':  'linear-gradient(135deg, #74b9ff 0%, #0984e3 100%)',
  '欢乐':  'linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%)',
  '平静':  'linear-gradient(135deg, #00b894 0%, #55a3ff 100%)',
  'default': 'linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%)' // 改为更有活力的紫色渐变
}

// 情绪图标映射
const moodIconMap = {
  '紧张': '😰',
  '温馨': '😊',
  '神秘': '🕵️',
  '恐怖': '😱',
  '浪漫': '💕',
  '悲伤': '😢',
  '欢乐': '😄',
  '平静': '😌'
}

// 灯光图标映射
const lightingIconMap = {
  '昏暗': '🌑',
  '暖黄': '🕯️',
  '刺眼': '🔆',
  '忽明忽暗': '💡',
  '彩色': '🌈',
  '冷光': '❄️',
  '自然光': '☀️',
  '烛光': '🕯️'
}

// 天气图标映射
const weatherIconMap = {
  '晴朗': '☀️',
  '多云': '⛅',
  '阴天': '☁️',
  '小雨': '🌦️',
  '暴雨': '⛈️',
  '雪天': '❄️',
  '雾气': '🌫️',
  '大风': '💨'
}

// 处理点击事件
const handleClick = () => {
  // 构造完整的节点数据对象
  const nodeData = {
    id: props.id,
    type: 'atmosphere',
    data: props.data,
    position: props.position
  }
  
  emit('click', nodeData)
}

// 双击直接触发 open 事件（可选），同样不阻止冒泡
const handleDoubleClick = () => {
  // 构造完整的节点数据对象
  const nodeData = {
    id: props.id,
    type: 'atmosphere',
    data: props.data,
    position: props.position
  }
  emit('open', nodeData)
}

// 处理删除
const handleDelete = (event) => {
  event.stopPropagation()
  emit('delete', props.id)
}
</script>

<style scoped>
.atmosphere-node {
  position: absolute;
  width: 200px;
  min-height: 120px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(5px);
  color: white;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  z-index: 1;
  visibility: visible !important;
}

.atmosphere-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
}

.atmosphere-node.selected {
  border-color: #40a9ff;
  box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.3);
}

/* 删除按钮 */
.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: #ff4d4f;
  color: white;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  background: #ff7875;
  transform: scale(1.1);
}

/* 节点头部 */
.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px 10px 0 0;
}

.mood-icon, .lighting-icon {
  font-size: 18px;
  cursor: help;
}

.time-label {
  font-size: 11px;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}

/* 节点主体 */
.node-body {
  padding: 12px;
}

.title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  text-align: center;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  font-size: 11px;
  gap: 4px;
}

.label {
  font-weight: 500;
  opacity: 0.9;
  min-width: 32px;
}

.value {
  flex: 1;
  opacity: 0.95;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.music-icon, .weather-icon {
  font-size: 12px;
  opacity: 0.8;
}

/* 连接点样式 */
.handle {
  width: 8px;
  height: 8px;
  background: white;
  border: 2px solid #40a9ff;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.handle:hover {
  width: 12px;
  height: 12px;
  background: #40a9ff;
}

.handle-left {
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.handle-right {
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
}

.handle-top {
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
}

.handle-bottom {
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .atmosphere-node {
    width: 160px;
    min-height: 100px;
  }
  
  .title {
    font-size: 12px;
  }
  
  .info-row {
    font-size: 10px;
  }
}
</style> 