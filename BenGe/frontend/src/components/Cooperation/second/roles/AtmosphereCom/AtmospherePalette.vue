<template>
  <teleport to="body">
  <div v-if="visible" class="palette-overlay" @click="handleOverlayClick">
    <div class="palette-panel" @click.stop>
      <div class="palette-header">
        <h3>氛围调色板</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      
      <div class="palette-content">
        <div class="palette-section">
          <h4>情感氛围</h4>
          <div class="atmosphere-grid">
            <div 
              v-for="atmo in emotionalAtmospheres" 
              :key="atmo.id"
              class="atmosphere-card"
              :style="{ background: atmo.color }"
              @click="selectAtmosphere(atmo)"
            >
              <div class="card-content">
                <span class="atmosphere-icon">{{ atmo.icon }}</span>
                <span class="atmosphere-name">{{ atmo.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="palette-section">
          <h4>环境氛围</h4>
          <div class="atmosphere-grid">
            <div 
              v-for="atmo in environmentalAtmospheres" 
              :key="atmo.id"
              class="atmosphere-card"
              :style="{ background: atmo.color }"
              @click="selectAtmosphere(atmo)"
            >
              <div class="card-content">
                <span class="atmosphere-icon">{{ atmo.icon }}</span>
                <span class="atmosphere-name">{{ atmo.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="palette-section">
          <h4>时间氛围</h4>
          <div class="atmosphere-grid">
            <div 
              v-for="atmo in timeAtmospheres" 
              :key="atmo.id"
              class="atmosphere-card"
              :style="{ background: atmo.color }"
              @click="selectAtmosphere(atmo)"
            >
              <div class="card-content">
                <span class="atmosphere-icon">{{ atmo.icon }}</span>
                <span class="atmosphere-name">{{ atmo.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </teleport>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['close', 'select'])

// 情感氛围预设
const emotionalAtmospheres = ref([
  {
    id: 'warm',
    name: '温馨',
    icon: '🏠',
    color: 'linear-gradient(135deg, #ffeaa7, #fab1a0)',
    data: {
      title: '温馨氛围',
      mood: '温馨', // 使用氛围节点颜色映射中的关键词
      lighting: '柔和的暖光',
      music: '轻柔的背景音乐',
      weather: '晴朗',
      notes: '营造家的感觉，让人感到放松和安心'
    }
  },
  {
    id: 'tense',
    name: '紧张',
    icon: '⚡',
    color: 'linear-gradient(135deg, #e17055, #d63031)',
    data: {
      title: '紧张氛围',
      mood: '紧张', // 使用氛围节点颜色映射中的关键词
      lighting: '强烈的对比光',
      music: '快节奏、不和谐音效',
      weather: '阴沉',
      notes: '制造压迫感，让观众感到紧张和期待'
    }
  },
  {
    id: 'mysterious',
    name: '神秘',
    icon: '🌙',
    color: 'linear-gradient(135deg, #6c5ce7, #2d3436)',
    data: {
      title: '神秘氛围',
      mood: '神秘', // 使用氛围节点颜色映射中的关键词
      lighting: '昏暗、阴影重重',
      music: '低沉的背景音',
      weather: '雾气弥漫',
      notes: '营造未知感，激发观众的好奇心'
    }
  },
  {
    id: 'romantic',
    name: '浪漫',
    icon: '💕',
    color: 'linear-gradient(135deg, #fd79a8, #fdcb6e)',
    data: {
      title: '浪漫氛围',
      mood: '浪漫', // 使用氛围节点颜色映射中的关键词
      lighting: '柔和的粉色光',
      music: '优美的旋律',
      weather: '微风轻拂',
      notes: '营造爱情的美好感觉'
    }
  }
])

// 环境氛围预设
const environmentalAtmospheres = ref([
  {
    id: 'urban',
    name: '都市',
    icon: '🏙️',
    color: 'linear-gradient(135deg, #74b9ff, #0984e3)',
    data: {
      title: '都市氛围',
      mood: '紧张', // 都市快节奏对应紧张情绪
      lighting: '霓虹灯、街灯',
      music: '城市噪音、汽车声',
      weather: '多变',
      notes: '现代都市的快节奏生活感'
    }
  },
  {
    id: 'nature',
    name: '自然',
    icon: '🌿',
    color: 'linear-gradient(135deg, #00b894, #55a3ff)',
    data: {
      title: '自然氛围',
      mood: '平静', // 自然环境对应平静情绪
      lighting: '自然光',
      music: '鸟鸣、流水声',
      weather: '清爽',
      notes: '大自然的宁静与和谐'
    }
  },
  {
    id: 'ancient',
    name: '古典',
    icon: '🏛️',
    color: 'linear-gradient(135deg, #a29bfe, #6c5ce7)',
    data: {
      title: '古典氛围',
      mood: '神秘', // 古典历史感对应神秘情绪
      lighting: '烛光、古典灯具',
      music: '古典音乐',
      weather: '稳定',
      notes: '历史的厚重感和文化底蕴'
    }
  }
])

// 时间氛围预设
const timeAtmospheres = ref([
  {
    id: 'dawn',
    name: '黎明',
    icon: '🌅',
    color: 'linear-gradient(135deg, #ffeaa7, #fab1a0)',
    data: {
      title: '黎明氛围',
      mood: '欢乐', // 黎明新生对应欢乐情绪
      lighting: '晨光',
      music: '鸟鸣声',
      weather: '清晨薄雾',
      notes: '新的开始，充满希望'
    }
  },
  {
    id: 'midnight',
    name: '深夜',
    icon: '🌃',
    color: 'linear-gradient(135deg, #2d3436, #636e72)',
    data: {
      title: '深夜氛围',
      mood: '悲伤', // 深夜孤独对应悲伤情绪
      lighting: '月光、星光',
      music: '夜晚的寂静',
      weather: '夜风',
      notes: '深夜的宁静与思考'
    }
  }
])

// 选择氛围
const selectAtmosphere = (atmosphere) => {
  // console.log('选择氛围:', atmosphere.name)
  emit('select', atmosphere)
}

// 点击遮罩关闭
const handleOverlayClick = () => {
  emit('close')
}
</script>

<style scoped>
.palette-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.palette-panel {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
  margin: 20px;
}

.palette-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.palette-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #95a5a6;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f8f9fa;
  color: #2c3e50;
}

.palette-content {
  padding: 24px;
}

.palette-section {
  margin-bottom: 32px;
}

.palette-section:last-child {
  margin-bottom: 0;
}

.palette-section h4 {
  margin: 0 0 16px 0;
  color: #34495e;
  font-size: 16px;
  font-weight: 600;
}

.atmosphere-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.atmosphere-card {
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.atmosphere-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.atmosphere-icon {
  font-size: 32px;
  margin-bottom: 8px;
  display: block;
}

.atmosphere-name {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
</style>
