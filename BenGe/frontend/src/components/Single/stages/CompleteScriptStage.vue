<template>
  <div class="complete-script-container">
    <!-- 顶部导航栏 -->
    <div class="stage-header">
      <div class="stage-nav">
        <button class="back-btn" @click="backToFramework">
          <i class="fas fa-arrow-left"></i>
          返回框架设计
        </button>
        <h2 class="stage-title">完整剧本</h2>
      </div>
      <div class="stage-actions">
        <button class="visual-elements-btn" @click="toggleVisualWorkspace">
          <i class="fas fa-images"></i>
          可视化元素
        </button>
        <button class="export-btn" @click="exportScript">
          <i class="fas fa-file-export"></i>
          导出剧本
        </button>
      </div>
    </div>
    
    <!-- 剧本内容区域 -->
    <div class="script-content-area">
      <div v-if="scriptContent" class="script-content" v-html="formattedContent"></div>
      <div v-else class="empty-content">
        <div class="empty-icon"><i class="fas fa-file-alt fa-3x"></i></div>
        <p>剧本内容为空，请返回框架设计阶段完善您的剧本</p>
      </div>
    </div>

    <!-- 可视化元素工作区 -->
    <transition name="slide-up">
      <visual-workspace 
        v-if="showVisualWorkspace" 
        @close="toggleVisualWorkspace"
      />
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { ref } from 'vue';
import { usescriptStore } from '@/stores/scriptStore';
import { marked } from 'marked';
import '@fortawesome/fontawesome-free/css/all.min.css';
import VisualWorkspace from '@/components/Single/VisualWorkspace.vue';

const scriptStore = usescriptStore();

// 控制可视化元素工作区的显示
const showVisualWorkspace = ref(false);

// 切换可视化工作区显示
const toggleVisualWorkspace = () => {
  showVisualWorkspace.value = !showVisualWorkspace.value;
  
  // 当显示可视化工作区时，禁止背景滚动
  if (showVisualWorkspace.value) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
};

// 获取剧本内容
const scriptContent = computed(() => {
  return scriptStore.scriptContent || '';
});

// 格式化剧本内容（支持Markdown）
const formattedContent = computed(() => {
  return scriptContent.value ? marked(scriptContent.value) : '';
});

// 返回框架设计阶段
const backToFramework = () => {
  scriptStore.scriptStage = 2;
};

// 导出剧本
const exportScript = () => {
  if (!scriptContent.value) return;
  
  // 创建一个Blob对象
  const blob = new Blob([scriptContent.value], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  
  // 创建一个下载链接
  const a = document.createElement('a');
  a.href = url;
  a.download = `${scriptStore.scriptTitle || '剧本'}.md`;
  document.body.appendChild(a);
  a.click();
  
  // 清理
  setTimeout(() => {
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, 0);
};
</script>

<style scoped>
.complete-script-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f9f9f9;
  font-family: 'Segoe UI', Arial, sans-serif;
}

.stage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 9;
}

.stage-nav {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background-color: #e0e0e0;
  transform: translateY(-2px);
}

.stage-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.stage-actions {
  display: flex;
  gap: 10px;
}

/* 可视化元素按钮 */
.visual-elements-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #8B5CF6;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.visual-elements-btn:hover {
  background-color: #7C3AED;
  transform: translateY(-2px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #6366F1;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.export-btn:hover {
  background-color: #4F46E5;
  transform: translateY(-2px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.script-content-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background-color: white;
  margin: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.script-content {
  line-height: 1.8;
  color: #333;
  font-size: 16px;
  font-family: 'Georgia', 'Times New Roman', serif;
}

.script-content h1,
.script-content h2,
.script-content h3 {
  color: #4F46E5;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.script-content p {
  margin: 1em 0;
}

.script-content ul,
.script-content ol {
  padding-left: 1.5em;
  margin: 1em 0;
}

.script-content blockquote {
  border-left: 4px solid #6366F1;
  padding-left: 1em;
  margin-left: 0;
  color: #555;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  text-align: center;
  color: #888;
}

.empty-icon {
  color: #ccc;
  margin-bottom: 20px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateY(100%);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>