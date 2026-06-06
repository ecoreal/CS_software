<template>
  <div class="visual-workspace-container" :class="{ 'show': show }">
    <!-- 动态背景效果 -->
    <div class="dynamic-background">
      <div class="fog-layer"></div>
      <div class="particles-container" ref="particlesContainer"></div>
    </div>
    
    <div class="visual-workspace-backdrop" @click="closeWorkspace"></div>
    <div class="visual-workspace narrative-interface">
      <!-- 关闭按钮 - 设计成老式印章 -->
      <button class="close-btn stamp-btn" @click="closeWorkspace">
        <i class="fas fa-times"></i>
      </button>
      
      <!-- 标题 - 设计成档案标题 -->
      <div class="workspace-header">
        <div class="file-stamp"></div>
        <h2 class="workspace-title">
          <i class="fas fa-folder-open"></i>
          剧本档案室
        </h2>
        <div class="file-stamp right"></div>
      </div>
      
      <!-- 主要内容区域 -->
      <div class="workspace-content">
        <!-- 主分类选择 -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="currentView === 'categories'" class="element-categories">
            <div class="categories-container">
              <!-- 左侧大卡片 - 人物档案 -->
              <div class="category-card character narrative-file" @click="showElementList('Character')">
                <div class="category-icon">
                  <i class="fas fa-user-circle"></i>
                </div>
                <div class="category-info">
                  <h3>人物档案</h3>
                  <p>{{ characterElements.length }} 个角色</p>
                </div>
                <div class="card-decoration"></div>
              </div>
              
              <!-- 右侧两个小卡片 -->
              <div class="category-right-column">
                <!-- 道具证物袋 -->
                <div class="category-card prop narrative-evidence" @click="showElementList('Prop')">
                  <div class="category-icon">
                    <i class="fas fa-cube"></i>
                  </div>
                  <div class="category-info">
                    <h3>证物袋</h3>
                    <p>{{ propElements.length }} 个道具</p>
                  </div>
                  <div class="evidence-tag">证物编号: {{ propElements.length }}</div>
                </div>
                
                <!-- 场景地图 -->
                <div class="category-card scene narrative-map" @click="showElementList('Scene')">
                  <div class="category-icon">
                    <i class="fas fa-map-marked-alt"></i>
                  </div>
                  <div class="category-info">
                    <h3>场景地图</h3>
                    <p>{{ sceneElements.length }} 个场景</p>
                  </div>
                  <div class="map-compass"></div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 元素列表 -->
          <div v-else-if="currentView === 'list'" class="element-list">
            <div class="list-header">
              <button class="back-btn narrative-btn" @click="showCategories">
                <i class="fas fa-arrow-left"></i>
                返回分类
              </button>
              <h3 class="list-title">
                <i :class="getCategoryIcon(currentCategory)"></i>
                {{ getCategoryName(currentCategory) }}
              </h3>
            </div>
            
            <div class="elements-grid">
              <div 
                v-for="element in currentElements" 
                :key="element.id" 
                class="element-card"
                :class="getElementCardClass(element.type)"
                @click="showElementDetail(element)"
              >
                <div class="element-card-inner">
                  <div class="element-card-front">
                    <div v-if="element.imageUrl" class="element-image">
                      <img :src="getImageUrl(element.imageUrl)" :alt="element.name" @error="handleImageError">
                    </div>
                    <div v-else class="element-placeholder">
                      <i :class="getCategoryIcon(currentCategory)"></i>
                    </div>
                    <div class="element-info">
                      <h4>{{ element.name }}</h4>
                      <p>{{ truncateDescription(element.description) }}</p>
                    </div>
                    <div class="card-hint">点击查看详情</div>
                  </div>
                  <div class="element-card-back">
                    <div class="element-back-content">
                      <h4>{{ element.name }}</h4>
                      <p>{{ truncateDescription(element.description, 100) }}</p>
                      <div class="view-more">查看完整内容 →</div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-if="currentElements.length === 0" class="empty-elements">
                <div class="empty-icon">
                  <i class="fas fa-box-open"></i>
                </div>
                <p>暂无{{ getCategoryName(currentCategory) }}元素</p>
              </div>
            </div>
          </div>
          
          <!-- 元素详情 -->
          <div v-else-if="currentView === 'detail'" class="element-detail">
            <div class="detail-header">
              <button class="back-btn narrative-btn" @click="showElementList(currentCategory)">
                <i class="fas fa-arrow-left"></i>
                返回列表
              </button>
              <h3 class="detail-title">{{ selectedElement.name }}</h3>
            </div>
            
            <div class="detail-content">
              <div class="detail-image-section">
                <div v-if="selectedElement.imageUrl" class="detail-image narrative-photo">
                  <img :src="getImageUrl(selectedElement.imageUrl)" :alt="selectedElement.name" @error="handleImageError">
                  <div class="photo-date">{{ formatDate(selectedElement.imageGeneratedAt) }}</div>
                </div>
                <div v-else class="detail-placeholder">
                  <i :class="getCategoryIcon(currentCategory)"></i>
                </div>
                
                <button 
                  class="generate-image-btn" 
                  @click="generateImage"
                  :disabled="isGeneratingImage"
                >
                  <i v-if="isGeneratingImage" class="fas fa-spinner fa-spin"></i>
                  <i v-else class="fas fa-magic"></i>
                  {{ isGeneratingImage ? '生成中...' : '生成图片' }}
                </button>
              </div>
              
              <div class="detail-info-section">
                <div class="detail-type">
                  <span class="label">类型:</span>
                  <span class="value">{{ getCategoryName(selectedElement.type) }}</span>
                </div>
                
                <div class="detail-description narrative-notes">
                  <span class="label">描述:</span>
                  <div class="value description-content">{{ selectedElement.description || '暂无描述' }}</div>
                </div>
                
                <div v-if="selectedElement.imageGeneratedAt" class="detail-generated-time">
                  <span class="label">图片生成时间:</span>
                  <span class="value">{{ formatDate(selectedElement.imageGeneratedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, defineEmits, onBeforeUnmount } from 'vue';
import { usescriptStore } from '@/stores/scriptStore';

const emit = defineEmits(['close']);
const scriptStore = usescriptStore();
const particlesContainer = ref(null);

// 控制组件显示动画
const show = ref(false);

// 当前视图: 'categories' | 'list' | 'detail'
const currentView = ref('categories');
const currentCategory = ref('');
const selectedElement = ref(null);
const isGeneratingImage = ref(false);

// 获取各类型的元素
const characterElements = computed(() => scriptStore.getVisualElementsByType('Character'));
const propElements = computed(() => scriptStore.getVisualElementsByType('Prop'));
const sceneElements = computed(() => scriptStore.getVisualElementsByType('Scene'));

// 当前显示的元素列表
const currentElements = computed(() => {
  return scriptStore.getVisualElementsByType(currentCategory.value);
});

// 组件挂载后显示动画
onMounted(() => {
  // 禁止背景滚动
  document.body.style.overflow = 'hidden';
  
  // 初始化粒子效果
  initParticles();
  
  nextTick(() => {
    show.value = true;
  });
});

// 组件卸载前清理
onBeforeUnmount(() => {
  // 恢复背景滚动
  document.body.style.overflow = '';
  
  // 清理粒子效果
  cleanupParticles();
});

// 初始化粒子效果
let particles = [];
let animationFrameId = null;

const initParticles = () => {
  if (!particlesContainer.value) return;
  
  const container = particlesContainer.value;
  const particlesCount = 30;
  
  // 创建粒子
  for (let i = 0; i < particlesCount; i++) {
    const particle = document.createElement('div');
    particle.classList.add('particle');
    
    // 随机位置和大小
    const size = Math.random() * 5 + 2;
    const posX = Math.random() * 100;
    const posY = Math.random() * 100;
    const opacity = Math.random() * 0.5 + 0.2;
    
    // 设置样式
    particle.style.width = `${size}px`;
    particle.style.height = `${size}px`;
    particle.style.left = `${posX}%`;
    particle.style.top = `${posY}%`;
    particle.style.opacity = opacity;
    
    // 添加到容器
    container.appendChild(particle);
    particles.push({
      element: particle,
      x: posX,
      y: posY,
      size: size,
      speedX: Math.random() * 0.2 - 0.1,
      speedY: Math.random() * 0.2 - 0.1
    });
  }
  
  // 添加鼠标移动事件
  document.addEventListener('mousemove', handleMouseMove);
  
  // 开始动画
  animateParticles();
};

// 清理粒子效果
const cleanupParticles = () => {
  document.removeEventListener('mousemove', handleMouseMove);
  
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
  }
  
  particles.forEach(p => {
    if (p.element && p.element.parentNode) {
      p.element.parentNode.removeChild(p.element);
    }
  });
  
  particles = [];
};

// 处理鼠标移动
const handleMouseMove = (e) => {
  const mouseX = e.clientX / window.innerWidth;
  const mouseY = e.clientY / window.innerHeight;
  
  particles.forEach(particle => {
    // 根据鼠标位置微调粒子位置
    const offsetX = (mouseX - 0.5) * 20;
    const offsetY = (mouseY - 0.5) * 20;
    
    particle.element.style.transform = `translate(${offsetX}px, ${offsetY}px)`;
  });
};

// 粒子动画
const animateParticles = () => {
  particles.forEach(particle => {
    // 更新位置
    particle.x += particle.speedX;
    particle.y += particle.speedY;
    
    // 边界检查
    if (particle.x < 0) particle.x = 100;
    if (particle.x > 100) particle.x = 0;
    if (particle.y < 0) particle.y = 100;
    if (particle.y > 100) particle.y = 0;
    
    // 应用新位置
    particle.element.style.left = `${particle.x}%`;
    particle.element.style.top = `${particle.y}%`;
  });
  
  animationFrameId = requestAnimationFrame(animateParticles);
};

// 关闭工作区
const closeWorkspace = () => {
  show.value = false;
  setTimeout(() => {
    emit('close');
  }, 300); // 等待动画完成
};

// 显示分类页面
const showCategories = () => {
  currentView.value = 'categories';
  currentCategory.value = '';
};

// 显示元素列表
const showElementList = (category) => {
  currentCategory.value = category;
  currentView.value = 'list';
};

// 显示元素详情
const showElementDetail = (element) => {
  selectedElement.value = element;
  currentView.value = 'detail';
};

// 获取完整的图片URL
const getImageUrl = (url) => {
  if (!url) return '';
  if( url.startsWith('data:image/png;base64,')) {
    return url;
  }
  else{
    return "data:image/png;base64,"+url;
  }
};

// 处理图片加载错误
const handleImageError = (event) => {
  // console.error('图片加载失败', event);
  // 设置为默认图片
  event.target.src = '/placeholder-image.png'; // 替换为你的默认图片路径
  // 添加错误样式
  event.target.classList.add('image-error');
};

// 生成图片
const generateImage = async () => {
  if (!selectedElement.value || isGeneratingImage.value) return;
  
  isGeneratingImage.value = true;
  
  try {
    const imageUrl = await scriptStore.generateVisualElementImage(selectedElement.value.id);
    if (imageUrl) {
      // 更新选中元素的图片URL
      selectedElement.value = {
        ...selectedElement.value,
        imageUrl: getImageUrl(imageUrl),
        imageGeneratedAt: new Date()
      };
    }
  } catch (error) {
    console.error('生成图片失败', error);
  } finally {
    isGeneratingImage.value = false;
  }
};

// 获取元素卡片类名
const getElementCardClass = (type) => {
  switch (type) {
    case 'Character': return 'character-card';
    case 'Prop': return 'prop-card';
    case 'Scene': return 'scene-card';
    default: return '';
  }
};

// 获取分类图标
const getCategoryIcon = (category) => {
  switch (category) {
    case 'Character': return 'fas fa-user-circle';
    case 'Prop': return 'fas fa-cube';
    case 'Scene': return 'fas fa-map-marked-alt';
    default: return 'fas fa-question-circle';
  }
};

// 获取分类名称
const getCategoryName = (category) => {
  switch (category) {
    case 'Character': return '人物';
    case 'Prop': return '道具';
    case 'Scene': return '场景';
    default: return '未知';
  }
};

// 截断描述文本
const truncateDescription = (description, length = 50) => {
  if (!description) return '暂无描述';
  return description.length > length ? description.substring(0, length) + '...' : description;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};
</script>

<style>
.visual-workspace-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transform: translateY(100%);
  transition: opacity 0.3s ease, transform 0.3s ease;
  pointer-events: none; /* 初始状态不接收点击事件 */
}

.visual-workspace-container.show {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto; /* 显示后接收点击事件 */
}

/* 动态背景 */
.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
}

.fog-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.02), rgba(0,0,0,0.05));
  animation: fogAnimation 30s infinite alternate;
}

.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  transition: transform 0.3s ease-out;
}

@keyframes fogAnimation {
  0% { transform: translateX(-5%) translateY(-5%); }
  100% { transform: translateX(5%) translateY(5%); }
}

.visual-workspace-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(3px);
}

/* 叙事化界面 */
.narrative-interface {
  position: relative;
  width: 90%;
  max-width: 1200px;
  height: 90%;
  background-color: #f8f5e6;
  background-image: 
    linear-gradient(rgba(255,255,255,.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,.3) 1px, transparent 1px);
  background-size: 20px 20px;
  box-shadow: 0 0 20px rgba(0,0,0,.3), inset 0 0 50px rgba(0,0,0,.1);
  border: 1px solid #d0c8b0;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 1001;
}

.file-stamp {
  width: 60px;
  height: 60px;
  background-color: rgba(180, 30, 30, 0.7);
  border-radius: 50%;
  position: relative;
  transform: rotate(-15deg);
}

.file-stamp.right {
  transform: rotate(15deg);
}

.file-stamp::before {
  content: "机密";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: bold;
}

/* 标题样式 */
.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 2px double #a89e88;
}

.workspace-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #5a4a42;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.2);
  margin: 0;
  font-size: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.workspace-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 关闭按钮 - 印章样式 */
.stamp-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #a67c52;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
  transition: all 0.3s ease;
  z-index: 10;
  cursor: pointer;
}

.stamp-btn:hover {
  background-color: #8a6642;
  transform: rotate(90deg);
}

/* 叙事化按钮 */
.narrative-btn {
  background-color: #e8dfc9;
  border: 1px solid #d0c8b0;
  color: #5a4a42;
  padding: 8px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.narrative-btn:hover {
  background-color: #d0c8b0;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

/* 分类卡片样式 */
.categories-container {
  display: flex;
  width: 80%;
  max-width: 1000px;
  height: 400px;
  gap: 20px;
  margin: 0 auto;
}

/* 人物档案卡片 */
.narrative-file {
  background-color: #f0e6d2;
  border: 1px solid #d0c8b0;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.narrative-file::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('data:image/png;base64,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');
  opacity: 0.05;
  pointer-events: none;
}

.category-card {
  flex: 1;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.category-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.category-card.character {
  flex: 2;
  height: 100%;
}

.category-right-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

.category-icon {
  font-size: 2.5rem;
  margin-bottom: 15px;
  color: #5a4a42;
}

.category-info h3 {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #5a4a42;
  margin-bottom: 10px;
}

.card-decoration {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 50px;
  height: 50px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='50' height='50' viewBox='0 0 50 50'%3E%3Cpath fill='%23a67c52' fill-opacity='0.2' d='M25,0 L50,25 L25,50 L0,25 Z'/%3E%3C/svg%3E");
  opacity: 0.5;
}

/* 道具卡片 - 证物袋风格 */
.narrative-evidence {
  background-color: #e8dfc9;
  border: 2px solid #a67c52;
  position: relative;
}

.evidence-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #a67c52;
  color: white;
  padding: 3px 8px;
  font-size: 12px;
  border-radius: 3px;
  transform: rotate(3deg);
}

/* 场景卡片 - 地图风格 */
.narrative-map {
  background-color: #f0e6d2;
  border: 1px solid #d0c8b0;
  position: relative;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Crect fill='none' width='100' height='100'/%3E%3Cpath fill='%23d0c8b0' fill-opacity='0.2' d='M0,0 L100,100 M100,0 L0,100'/%3E%3C/svg%3E");
}

.map-compass {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 40px;
  height: 40px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='40' height='40' viewBox='0 0 40 40'%3E%3Ccircle cx='20' cy='20' r='18' fill='none' stroke='%23a67c52' stroke-width='1'/%3E%3Cpath d='M20,2 L20,38 M2,20 L38,20' stroke='%23a67c52' stroke-width='1'/%3E%3Cpath d='M20,20 L20,5' stroke='%23a67c52' stroke-width='2' stroke-linecap='round'/%3E%3Cpath d='M20,20 L30,25' stroke='%23a67c52' stroke-width='1' stroke-linecap='round'/%3E%3Ctext x='19' y='8' font-family='Arial' font-size='6' fill='%23a67c52' text-anchor='middle'>N</text>%3C/svg%3E");
  opacity: 0.7;
}

/* 元素列表样式 */
.element-list {
  padding: 20px;
}

.list-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  gap: 20px;
}

.list-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #5a4a42;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.elements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

/* 元素卡片样式 */
.element-card {
  background-color: #f0e6d2;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  cursor: pointer;
  height: 280px;
  perspective: 1000px;
  position: relative;
}

.element-card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.6s;
  transform-style: preserve-3d;
}

.element-card:hover .element-card-inner {
  transform: rotateY(180deg);
}

.element-card-front, .element-card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
}

.element-card-back {
  transform: rotateY(180deg);
  background-color: #e8dfc9;
  padding: 20px;
}

.element-image {
  height: 150px;
  overflow: hidden;
}

.element-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.element-card:hover .element-image img {
  transform: scale(1.05);
}

.element-placeholder {
  height: 150px;
  background-color: #e0d8c0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: #a67c52;
  opacity: 0.5;
}

.element-info {
  padding: 15px;
  flex: 1;
}

.element-info h4 {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #5a4a42;
  margin-top: 0;
  margin-bottom: 10px;
}

.element-info p {
  color: #8a7a6d;
  font-size: 0.9rem;
  margin: 0;
}

.card-hint {
  position: absolute;
  bottom: 10px;
  right: 10px;
  font-size: 0.8rem;
  color: #a67c52;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.element-card:hover .card-hint {
  opacity: 1;
}

.element-back-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.element-back-content h4 {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #5a4a42;
  margin-top: 0;
  margin-bottom: 15px;
}

.element-back-content p {
  color: #5a4a42;
  flex: 1;
  line-height: 1.6;
}

.view-more {
  align-self: flex-end;
  color: #a67c52;
  font-weight: bold;
  margin-top: 10px;
}

/* 元素详情样式 */
.element-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  gap: 20px;
}

.detail-title {
  font-family: 'STKaiti', 'KaiTi', serif;
  color: #5a4a42;
  margin: 0;
  font-size: 1.5rem;
}

.detail-content {
  display: flex;
  gap: 30px;
}

.detail-image-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center;
}

.detail-image {
  width: 100%;
  max-width: 400px;
}

.detail-image img {
  width: 100%;
  height: auto;
  border-radius: 4px;
}

.detail-placeholder {
  width: 100%;
  max-width: 400px;
  height: 300px;
  background-color: #e0d8c0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 5rem;
  color: #a67c52;
  opacity: 0.5;
  border-radius: 4px;
}

.generate-image-btn {
  background-color: #5a4a42;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-image-btn:hover:not(:disabled) {
  background-color: #7a6a62;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.generate-image-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.detail-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-type, .detail-generated-time {
  display: flex;
  gap: 10px;
}

.label {
  font-weight: bold;
  color: #8a7a6d;
  min-width: 80px;
}

.value {
  color: #5a4a42;
}

/* 照片样式 */
.narrative-photo {
  position: relative;
  padding: 10px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  transform: rotate(-2deg);
  transition: transform 0.3s ease;
}

.narrative-photo:hover {
  transform: rotate(0) scale(1.02);
}

.narrative-photo img {
  width: 100%;
  height: auto;
  border: 1px solid #e0d8c0;
}

.photo-date {
  position: absolute;
  bottom: 5px;
  right: 10px;
  font-size: 0.8em;
  color: #8a7a6d;
  font-style: italic;
}

/* 手写笔记样式 */
.narrative-notes {
  background-color: #fffbeb;
  padding: 15px;
  border: 1px solid #e0d8c0;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  font-family: 'STKaiti', 'KaiTi', serif;
  line-height: 1.6;
  position: relative;
  border-radius: 4px;
}

.narrative-notes::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(#efe8d8 1px, transparent 1px);
  background-size: 100% 24px;
  pointer-events: none;
  opacity: 0.5;
  z-index: 0;
}

.description-content {
  position: relative;
  z-index: 1;
  padding: 5px;
}

/* 空元素提示 */
.empty-elements {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px;
  color: #8a7a6d;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  opacity: 0.5;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

/* 图片错误样式 */
.image-error {
  object-fit: contain !important;
  background-color: #f8f8f8;
  padding: 20px;
}

/* 卡片类型特定样式 */
.character-card {
  border-left: 4px solid #7c5a42;
}

.prop-card {
  border-left: 4px solid #426b7c;
}

.scene-card {
  border-left: 4px solid #5a7c42;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .categories-container {
    flex-direction: column;
    height: auto;
  }
  
  .category-right-column {
    flex-direction: row;
  }
  
  .detail-content {
    flex-direction: column;
  }
}
</style>