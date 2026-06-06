<template>
  <div class="character-card" @click="$emit('click', $event)" :style="stylePosition">
    <!-- 连接点，支持角色关系连线 -->
    <Handle type="target" position="left" id="left" :class="['vue-flow__handle', 'handle-left']" />
    <Handle type="target" position="right" id="right" :class="['vue-flow__handle', 'handle-right']" />
    <Handle type="source" position="left" id="left-source" :class="['vue-flow__handle', 'handle-left']" />
    <Handle type="source" position="right" id="right-source" :class="['vue-flow__handle', 'handle-right']" />
    
    <!-- 角色卡片内容 -->
    <div class="card-content">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-container">
          <img 
            :src="characterData.avatar || defaultAvatar" 
            :alt="characterData.name"
            class="character-avatar"
            @error="handleImageError"
          />
        </div>
        <button class="delete-button" @click.stop="handleDelete">
          ×
        </button>
      </div>
      
      <!-- 基本信息 -->
      <div class="info-section">
        <h3 class="character-name">{{ characterData.name || '未命名角色' }}</h3>
        <p class="character-occupation">{{ characterData.occupation || '未设定职业' }}</p>
        <div class="character-age" v-if="characterData.age">
          年龄: {{ characterData.age }}
        </div>
        
        <!-- 性格标签 -->
        <div class="personality-tags" v-if="characterData.personality && characterData.personality.length">
          <span 
            v-for="trait in characterData.personality.slice(0, 3)" 
            :key="trait"
            class="personality-tag"
          >
            {{ trait }}
          </span>
        </div>
        
        <!-- 关系数量指示 -->
        <div class="relationship-count" v-if="relationshipCount > 0">
          关系: {{ relationshipCount }}
        </div>
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
  position: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

const emit = defineEmits(['click', 'delete'])

// 默认头像 - 修复路径问题
const defaultAvatar = require('@/assets/avatar/1.jpg')

// 角色数据
const characterData = computed(() => props.data || {})

// 关系数量
const relationshipCount = computed(() => {
  return characterData.value.relationships?.length || 0
})

// 安全访问位置
const stylePosition = computed(() => {
  const x = props.position?.x ?? 0
  const y = props.position?.y ?? 0
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = defaultAvatar
}

// 处理删除
const handleDelete = (event) => {
  event.stopPropagation()
  emit('delete', props.id)
}
</script>

<style scoped>
.character-card {
  position: absolute;
  width: 200px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  border: 2px solid #e1e5e9;
  z-index: 1;
  visibility: visible !important;
  transform: none !important;
}

.character-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  border-color: #4a90e2;
}

.card-content {
  padding: 0;
}

.avatar-section {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 15px;
  text-align: center;
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.character-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.delete-button {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  color: #ff4757;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.delete-button:hover {
  background: #ff4757;
  color: white;
  transform: scale(1.1);
}

.info-section {
  padding: 12px 15px 15px;
}

.character-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.2;
}

.character-occupation {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #7f8c8d;
  font-style: italic;
}

.character-age {
  font-size: 12px;
  color: #95a5a6;
  margin-bottom: 8px;
}

.personality-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.personality-tag {
  background: #e8f4f8;
  color: #2980b9;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  border: 1px solid #bde4f4;
}

.relationship-count {
  font-size: 11px;
  color: #e67e22;
  font-weight: 500;
}

/* 连接点样式 */
.handle-left,
.handle-right {
  width: 8px;
  height: 8px;
  background: #4a90e2;
  border: 2px solid #ffffff;
  border-radius: 50%;
  position: absolute;
  z-index: 1000;
  pointer-events: auto;
  transition: all 0.2s ease;
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

.handle-left:hover,
.handle-right:hover {
  background: #2c5282;
  transform: translateY(-50%) scale(1.2);
}
</style> 