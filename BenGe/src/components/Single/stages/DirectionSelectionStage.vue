<template>
  <div class="stage-container">
    <!-- 左侧方向选择区域 -->
    <div class="left-panel" :style="{ width: leftPanelWidth + '%' }">
      <h3 class="panel-title">选择剧本方向</h3>
      
    <!-- 方向选择卡片 -->
      <div class="direction-cards">
        <div 
          v-for="(direction, index) in directions" 
          :key="direction.id"
          :class="['direction-card-container', { 'selected': isDirectionSelected(direction.id), 'middle-card': index === 1 }]"
          @click="showFullContent(direction)"
        >
          <div class="direction-card">
            <div class="card-front">
              <div class="card-number">{{ index + 1 }}</div>
              <div class="core-idea-title">核心谜题</div>
              <div class="direction-content">{{ truncateContent(direction.content) }}</div>
              <div class="card-hint">
                <i class="fas fa-search-plus"></i> 点击查看完整内容
              </div>
            </div>
            <div class="card-back">
              <div class="card-number">{{ index + 1 }}</div>
              <div class="core-idea-title">核心创意</div>
              <div class="core-idea-content">{{ truncateContent(direction.coreIdea, 150) }}</div>
              <div class="card-hint">
                <i class="fas fa-search-plus"></i> 点击查看完整内容
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 下一步按钮 -->
      <div class="next-step-container">
        <button 
          class="next-step-btn" 
          :disabled="!hasSelectedDirection"
          @click="goToNextStage"
        >
          进入框架设计阶段
          <i class="fas fa-arrow-right"></i>
        </button>
      </div>
    </div>
    
    <!-- 拖拽调整区域宽度的分隔线 -->
    <div 
      class="resize-handle" 
      :style="{ left: leftPanelWidth + '%' }"
      @mousedown="startResize"
    ></div>
    
    <!-- 右侧对话区域 -->
    <div class="right-panel" :style="{ width: (100 - leftPanelWidth) + '%' }">
      <!-- 对话历史 -->
      <div class="chat-history" ref="chatHistoryRef">
        <div 
          v-for="(message, index) in chatHistory" 
          :key="index"
          :class="['message-card', message.sender === 'user' ? 'user-message' : 'ai-message']"
        >
          <div class="message-header">
            <div class="message-sender">{{ message.sender === 'user' ? '您' : 'AI助手' }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
          <div class="message-content" v-if="!message.isTyping">{{ message.content }}</div>
          <div v-else class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
      
      <!-- 快捷提示区域 -->
      <div class="quick-prompts-panel">
        <div class="prompt-header">
          <h4>创作提示</h4>
          <button class="toggle-btn" @click="togglePrompts">
            <i :class="['fas', showPrompts ? 'fa-chevron-up' : 'fa-chevron-down']"></i>
            {{ showPrompts ? '收起' : '展开' }}
          </button>
        </div>
        
        <transition name="slide">
          <div v-if="showPrompts" class="prompt-content">
            <!-- 提示词分类标签 -->
            <div class="prompt-categories">
              <div 
                v-for="(category, idx) in promptCategories" 
                :key="idx"
                :class="['category-tab', activeCategory === category.id ? 'active' : '']"
                @click="activeCategory = category.id"
              >
                <i :class="getCategoryIcon(category.id)"></i>
                {{ category.name }}
              </div>
            </div>
            
            <!-- 快捷提示词网格 -->
            <div class="quick-prompts-grid">
              <div 
                v-for="(prompt, idx) in filteredPrompts" 
                :key="idx"
                class="prompt-chip"
                @click="insertAtCursor(prompt.text)"
                :title="prompt.description"
              >
                {{ prompt.label }}
              </div>
            </div>
          </div>
        </transition>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <textarea 
          ref="messageInputRef"
          v-model="messageInput"
          :style="{ height: inputHeight }"
          placeholder="描述您想要的剧本方向，例如：'我想要一个发生在清朝的悬疑剧本...'"
          @input="adjustInputHeight"
          @keydown.enter.prevent="sendMessage"
        ></textarea>
        <button 
          class="send-btn" 
          :disabled="!messageInput.trim() || isProcessing"
          @click="sendMessage"
        >
          <i v-if="isProcessing" class="fas fa-spinner fa-spin"></i>
          <i v-else class="fas fa-paper-plane"></i>
        </button>
      </div>
    </div>

    <!-- 全屏内容展示遮罩 -->
    <div v-if="showFullContentModal" class="full-content-modal" @click.self="closeFullContent">
      <div class="modal-content">
        <div class="modal-close-btn" @click="closeFullContent">
          <i class="fas fa-times"></i>
        </div>
        <h3 class="modal-title">剧本方向 {{ currentViewingIndex + 1 }}</h3>
        
        <div class="modal-section">
          <h4>核心谜题</h4>
          <div class="modal-text">{{ currentViewingDirection.content }}</div>
        </div>
        
        <div class="modal-section">
          <h4>核心创意</h4>
          <div class="modal-text">{{ currentViewingDirection.coreIdea }}</div>
        </div>
        
        <div class="modal-actions">
          <button 
            class="select-btn" 
            @click="selectAndClose(currentViewingDirection.id)"
            :class="{ 'selected': isDirectionSelected(currentViewingDirection.id)}"
          >
            {{ isDirectionSelected(currentViewingDirection.id) ? '已选择' : '选择此方向' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, defineProps, onMounted, nextTick, watch ,onUnmounted} from 'vue';
import { getScriptDirections } from '@/api/script';
import { marked } from 'marked';
import { usescriptStore } from '@/stores/scriptStore';
import '@fortawesome/fontawesome-free/css/all.min.css';

const scriptStore = usescriptStore();

const props = defineProps({
  scriptId: {
    type: Number,
    required: true
  },
  userInfo: {
    type: String,
    required: true
  }
});

// 本地状态
const directions = computed(() => scriptStore.scriptDirections);
const chatHistory = ref([]);
const messageInput = ref('');
const isProcessing = ref(false);
const inputHeight = ref('50px');
const chatHistoryRef = ref(null);
const messageInputRef = ref(null);

// 面板宽度调整
const leftPanelWidth = ref(60); // 默认左侧面板宽度60%
const isResizing = ref(false);
const startX = ref(0);
const startWidth = ref(0);

// 提示词显示控制
const showPrompts = ref(true);

// 提示词分类
const activeCategory = ref('all');

// 提示词分类定义
const promptCategories = [
  { id: 'all', name: '全部' },
  { id: 'setting', name: '时代背景' },
  { id: 'genre', name: '类型风格' },
  { id: 'theme', name: '主题元素' }
];

// 获取分类图标
const getCategoryIcon = (categoryId) => {
  switch(categoryId) {
    case 'setting': return 'fas fa-clock';
    case 'genre': return 'fas fa-film';
    case 'theme': return 'fas fa-palette';
    default: return 'fas fa-th-list';
  }
};

// 提示词库
const promptLibrary = [
  { label: '古代背景', text: '我想要一个发生在古代的剧本，', category: 'setting', description: '设置故事发生在古代时期' },
  { label: '现代都市', text: '我想要一个现代都市背景的剧本，', category: 'setting', description: '设置故事发生在现代城市' },
  { label: '未来世界', text: '我想要一个未来世界背景的剧本，', category: 'setting', description: '设置故事发生在未来世界' },
  { label: '奇幻世界', text: '我想要一个奇幻世界背景的剧本，', category: 'setting', description: '设置故事发生在奇幻世界' },
  { label: '悬疑推理', text: '我想要一个悬疑推理类型的剧本，', category: 'genre', description: '设置剧本为悬疑推理类型' },
  { label: '爱情喜剧', text: '我想要一个爱情喜剧类型的剧本，', category: 'genre', description: '设置剧本为爱情喜剧类型' },
  { label: '科幻冒险', text: '我想要一个科幻冒险类型的剧本，', category: 'genre', description: '设置剧本为科幻冒险类型' },
  { label: '历史正剧', text: '我想要一个历史正剧类型的剧本，', category: 'genre', description: '设置剧本为历史正剧类型' },
  { label: '友情', text: '我想要一个关于友情的剧本，', category: 'theme', description: '以友情为主题' },
  { label: '成长', text: '我想要一个关于成长的剧本，', category: 'theme', description: '以成长为主题' },
  { label: '复仇', text: '我想要一个关于复仇的剧本，', category: 'theme', description: '以复仇为主题' },
  { label: '救赎', text: '我想要一个关于救赎的剧本，', category: 'theme', description: '以救赎为主题' }
];

// 根据分类过滤提示词
const filteredPrompts = computed(() => {
  return promptLibrary.filter(prompt => {
    return activeCategory.value === 'all' || prompt.category === activeCategory.value;
  });
});

// 是否已选择方向
const hasSelectedDirection = computed(() => {
  return directions.value.some(dir => dir.selected);
});

// 切换提示词显示
const togglePrompts = () => {
  showPrompts.value = !showPrompts.value;
};

// 开始调整宽度
const startResize = (e) => {
  isResizing.value = true;
  startX.value = e.clientX;
  startWidth.value = leftPanelWidth.value;
  
  document.addEventListener('mousemove', handleResize);
  document.addEventListener('mouseup', stopResize);
};

// 处理宽度调整
const handleResize = (e) => {
  if (!isResizing.value) return;
  
  const deltaX = e.clientX - startX.value;
  const containerWidth = document.querySelector('.stage-container').offsetWidth;
  const deltaPercent = (deltaX / containerWidth) * 100;
  
  // 限制最小和最大宽度
  leftPanelWidth.value = Math.min(Math.max(30, startWidth.value + deltaPercent), 70);
};

// 停止调整宽度
const stopResize = () => {
  isResizing.value = false;
  document.removeEventListener('mousemove', handleResize);
  document.removeEventListener('mouseup', stopResize);
};

// 在光标位置插入文本
const insertAtCursor = (text) => {
  const textarea = messageInputRef.value;
  if (!textarea) return;
  
  const startPos = textarea.selectionStart;
  const endPos = textarea.selectionEnd;
  const beforeText = messageInput.value.substring(0, startPos);
  const afterText = messageInput.value.substring(endPos);
  
  messageInput.value = beforeText + text + afterText;
  
  nextTick(() => {
    // 设置光标位置到插入文本之后
    textarea.focus();
    const newCursorPos = startPos + text.length;
    textarea.setSelectionRange(newCursorPos, newCursorPos);
    adjustInputHeight();
  });
};

// 调整输入框高度
const adjustInputHeight = () => {
  const textarea = messageInputRef.value;
  if (textarea) {
    textarea.style.height = 'auto';
    const newHeight = Math.min(Math.max(textarea.scrollHeight, 50), 150);
    inputHeight.value = `${newHeight}px`;
  }
};

// 流式输出相关变量 - 放在其他 ref 变量附近
const isStreaming = ref(false);
const streamingMessage = ref(null);
const streamingText = ref('');
const streamingInterval = ref(null); //ai的固定文本流(显示的文本)
const fullResponseText = ref('');//aid的固定文本流（不显示）
const pendingSlogans = ref([]);


// 发送消息
const sendMessage = async () => {
  if (!messageInput.value.trim() || isProcessing.value) return;
  
  // 添加用户消息到聊天历史
  chatHistory.value.push({
    sender: 'user',
    content: messageInput.value,
    timestamp: new Date()
  });
  
  isProcessing.value = true;
  const userInput = messageInput.value;
  messageInput.value = ''; // 清空输入框
  
  try {
    // 添加AI正在输入的提示
    chatHistory.value.push({
      sender: 'ai',
      content: '',
      isTyping: true,
      timestamp: new Date()
    });

    chatHistory.value.pop();    // 移除正在输入的提示
    scriptStore.clearDirections();// 清空现有方向

    const response = await fetch('/api/ai/slogan/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'Connection': 'keep-alive'
      },
      body: JSON.stringify({
        prompt: userInput,
        scriptId: props.scriptId,
      }),
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let slogans = [];

    let fullContent = ''; // 用于累积流式返回的完整内容

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true }); // 流式解码
      const lines = chunk.split('\n').filter(line => line.trim() !== ''); // 按行分割并过滤空行

      for (let line of lines) {
        if (!line.startsWith('data:')) continue; // 只处理 `data:` 开头的行
        else line = line.slice(5).trim();
        try {
          if(line.startsWith('data:')) {
            line = line.slice(5).trim(); // 去掉 "data:" 并去除空白
          }
          if (line === '[DONE]') continue; // 忽略结束标记
          const data = JSON.parse(line);

          // 检查是否有 `choices[0].delta.content`（类似 OpenAI 流式响应）
          if (data.choices && data.choices[0].delta && data.choices[0].delta.content) {


            console.log(data.choices[0].delta.content);
            fullContent += data.choices[0].delta.content; // 累积内容

            // 如果后端是逐字返回，可以按需拆分句子（如按换行符）
            const newLines = fullContent.split('# 标语').filter(l => l.trim());

            // 更新 slogans 列表
            slogans = newLines.map((text, index) => ({
              content: text.split("# 核心创意")[0]?.trim(),
              coreIdea: text.split("# 核心创意")[1]?.trim(),
              id: index + 1, // 生成唯一 ID
            }));

            pendingSlogans.value = [...slogans];
          }
        } catch (error) {
          // console.error('解析失败:', error, '原始数据:', line);
        }
      }
    }
    // console.log("fullcontent:::",fullContent);
    // console.log('最终 slogans:', slogans);    

      // 设置完整的AI回复文本
      fullResponseText.value = '我已为您生成了几个剧本方向，请在左侧选择一个您喜欢的方向继续创作。您可以将鼠标悬停在卡片上查看核心创意。';
      
      // 添加AI消息占位符
      const aiMessage = {
        sender: 'ai',
        content: '',
        timestamp: new Date()
      };
      chatHistory.value.push(aiMessage);
      streamingMessage.value = aiMessage;
      
      // 开始流式输出
      startStreaming();
    }catch (error) {
    // console.error('Error getting script directions:', error);
    chatHistory.value.push({
      sender: 'ai',
      content: '抱歉，获取剧本方向时出现了错误。',
      timestamp: new Date()
    });
  } finally {
    isProcessing.value = false;
  }
};


// 添加以下函数来控制流式输出
const startStreaming = () => {
  isStreaming.value = true;
  streamingText.value = '';
  let textIndex = 0;
  let sloganIndex = 0;
  let sloganDelay = 0;
  
  // 每30毫秒执行一次
  streamingInterval.value = setInterval(() => {
    // 处理文本流式输出
    if (textIndex < fullResponseText.value.length) {
      streamingText.value += fullResponseText.value[textIndex];
      if (streamingMessage.value) {
        streamingMessage.value.content = streamingText.value;
      }
      textIndex++;
    } 
    
    // 处理slogans流式输出
    // 每隔一定时间添加一个slogan
    if (sloganIndex < pendingSlogans.value.length) {
      sloganDelay++;
      // 每40个循环(约1.2秒)添加一个slogan
      if (sloganDelay >= 40) {
        // 添加方向时保留原始ID
        scriptStore.addDirection(pendingSlogans.value[sloganIndex]);
        sloganIndex++;
        sloganDelay = 0;
      }
    }
    
    // 当文本和slogans都显示完成，停止流式输出
    if (textIndex >= fullResponseText.value.length && sloganIndex >= pendingSlogans.value.length) {
      stopStreaming();
    }
  }, 30);
};

const stopStreaming = () => {
  clearInterval(streamingInterval.value);
  isStreaming.value = false;
  streamingMessage.value = null;
  pendingSlogans.value = [];
};

// 组件卸载时清理定时器
onUnmounted(() => {
  if (streamingInterval.value) {
    clearInterval(streamingInterval.value);
  }
});



// 添加新的状态变量
const showFullContentModal = ref(false);
const currentViewingDirection = ref(null);
const currentViewingIndex = ref(0);

// 截断内容函数
const truncateContent = (content, maxLength = 100) => {
  if (!content) return '';
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength) + '...';
};

// 显示完整内容
const showFullContent = (direction) => {
  currentViewingDirection.value = direction;
  currentViewingIndex.value = directions.value.findIndex(dir => dir.id === direction.id);
  showFullContentModal.value = true;
  
  // 防止滚动
  document.body.style.overflow = 'hidden';
};

// 关闭完整内容
const closeFullContent = () => {
  showFullContentModal.value = false;
  
  // 恢复滚动
  document.body.style.overflow = '';
};

// 选择并关闭
const selectAndClose = (directionId) => {
  selectDirection(directionId);
  closeFullContent();
};

// 检查方向是否被选中
const isDirectionSelected = (directionId) => {
  return scriptStore.selectedDirection && scriptStore.selectedDirection.id === directionId;
};
// 选择方向（只在本地更新选择状态，不调用API）
const selectDirection = async (directionId) => {
  try {
    // 使用scriptStore的selectDirectionLocally方法（只更新本地状态）
    const success = scriptStore.selectDirectionLocally(directionId);
    
    if (success) {
      // 找到正确的方向索引
      const selectedIndex = directions.value.findIndex(dir => dir.id === directionId) + 1;
      
      // 添加AI确认消息
      chatHistory.value.push({
        sender: 'ai',
        content: `您已选择方向${selectedIndex}，点击"进入框架设计阶段"按钮确认并继续创作。`,
        timestamp: new Date()
      });
      
      // console.log(`已选择方向ID: ${directionId}, 索引: ${selectedIndex-1}`);
    }
  } catch (error) {
    // console.error("选择方向失败", error);
    
    // 添加错误提示
    chatHistory.value.push({
      sender: 'ai',
      content: '抱歉，选择方向时出现错误，请重试。',
      timestamp: new Date()
    });
  } finally {
    // 聊天历史滚动到底部
    nextTick(() => {
      if (chatHistoryRef.value) {
        chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
      }
    });
  }
};

// 进入下一阶段（确认选择并调用API）
const goToNextStage = async () => {
  if (hasSelectedDirection.value) {
    isProcessing.value = true;
    
    try {
      // 调用confirmDirection方法提交选择到后端
      await scriptStore.confirmDirection();
      
    } finally {
      isProcessing.value = false;
      
      // 聊天历史滚动到底部
      nextTick(() => {
        if (chatHistoryRef.value) {
          chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
        }
      });
    }
  }
};

// 格式化时间
const formatTime = (date) => {
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

// 当聊天历史更新时，自动滚动到底部
watch(() => chatHistory.value.length, () => {
  nextTick(() => {
    if (chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
    }
  });
});

// 组件挂载后
onMounted(() => {
  // 初始化聊天历史
  if (chatHistory.value.length === 0) {
    chatHistory.value.push({
      sender: 'ai',
      content: '你好！我是你的剧本创作助手。请告诉我你想要创作的剧本类型、时代背景或主题，我将为你提供几个创作方向。',
      timestamp: new Date()
    });
  }
  
  // 聊天历史滚动到底部
  nextTick(() => {
    if (chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
    }
  });
  
  // 输入框聚焦
  if (messageInputRef.value) {
    messageInputRef.value.focus();
  }
});
</script>

<style scoped>
.stage-container {
  display: flex;
  height: 100%;
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', Arial, sans-serif;
}

/* 左侧方向选择区域 */
.left-panel {
  padding: 20px;
  overflow-y: auto;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  border-right: 1px solid #eaeaea;
  align-items: center;
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 30px 0;
  font-family: 'Montserrat', 'Arial', sans-serif;
  align-self: flex-start;
  width: 100%;
}

.direction-cards {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  width: 100%;
  flex-wrap: wrap;
}

.direction-card-container {
  perspective: 1000px;
  width: 180px;
  height: 280px; /* 竖直卡片，高度大于宽度 */
  cursor: pointer;
  transition: transform 0.3s;
}

.direction-card-container:hover {
  transform: translateY(-5px);
}

/* 第二个卡片向下偏移 */
.direction-card-container.middle-card {
  margin-top: 30px;
}

.direction-card-container.selected .direction-card {
  border-color: #6366F1;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.25);
}

.direction-card {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.8s;
  transform-style: preserve-3d;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
}

.direction-card-container:hover .direction-card {
  transform: rotateY(180deg);
}

.card-front, .card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 10px;
  overflow: hidden;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.card-front {
  background: linear-gradient(to bottom, #ffffff, #f8f8ff);
  z-index: 2;
}

.card-back {
  background: linear-gradient(135deg, #f0f0ff, #e6e6ff);
  transform: rotateY(180deg);
  z-index: 1;
}

.card-number {
  position: absolute;
  top: 0;
  right: 0;
  width: 36px;
  height: 36px;
  background-color: #6366F1;
  color: white;
  border-radius: 0 10px 0 12px; /* 右上角四分之一圆形 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  box-shadow: -2px 2px 5px rgba(0, 0, 0, 0.1);
}

/* 全屏内容展示遮罩 */
.full-content-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.4s ease;
}

.modal-close-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 40px;
  height: 40px;
  background-color: #6366F1;
  color: white;
  border-radius: 0 12px 0 12px; /* 右上角四分之一圆形 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.2s;
}

.modal-close-btn:hover {
  background-color: #4F46E5;
}

.modal-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  font-family: 'Montserrat', 'Arial', sans-serif;
  padding-right: 40px; /* 为关闭按钮留出空间 */
}

.modal-section {
  margin-bottom: 24px;
}

.modal-section h4 {
  font-size: 18px;
  font-weight: 600;
  color: #4F46E5;
  margin: 0 0 12px 0;
  font-family: 'Montserrat', 'Arial', sans-serif;
}

.modal-text {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  white-space: pre-line;
  font-family: 'Georgia', 'Times New Roman', serif;
}

.modal-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.select-btn {
  padding: 12px 24px;
  background-color: #6366F1;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-family: 'Montserrat', 'Arial', sans-serif;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.select-btn:hover {
  background-color: #4F46E5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.select-btn.selected {
  background-color: #10B981;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.direction-content {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  white-space: pre-line;
  font-family: 'Georgia', 'Times New Roman', serif;
  flex: 1;
  margin-top: 20px;
  /* 移除滚动条 */
  overflow: hidden;
}

.card-hint {
  font-size: 12px;
  color: #6366F1;
  margin-top: 10px;
  text-align: center;
  opacity: 0.7;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.core-idea-title {
  font-size: 16px;
  font-weight: 600;
  color: #4F46E5;
  margin: 20px 0 15px 0;
  text-align: center;
  font-family: 'Montserrat', 'Arial', sans-serif;
}

.core-idea-content {
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  /* 移除滚动条 */
  overflow: hidden;
  font-family: 'Georgia', 'Times New Roman', serif;
}

.next-step-container {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid #eaeaea;
  width: 85%;
  max-width: 500px;
}

.next-step-btn {
  width: 100%;
  padding: 14px;
  background-color: #6366F1;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-family: 'Montserrat', 'Arial', sans-serif;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.next-step-btn:hover:not(:disabled) {
  background-color: #4F46E5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.next-step-btn:disabled {
  background-color: #d1d1d1;
  cursor: not-allowed;
}

/* 拖拽调整区域宽度的分隔线 */
.resize-handle {
  position: absolute;
  top: 0;
  width: 10px;
  height: 100%;
  background-color: transparent;
  cursor: col-resize;
  z-index: 10;
  transform: translateX(-50%);
}

.resize-handle:hover,
.resize-handle:active {
  background-color: rgba(99, 102, 241, 0.1);
}

/* 右侧对话区域 */
.right-panel {
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  height: 100%;
}

.chat-history {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: #f5f5f5;
}

.message-card {
  max-width: 85%;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.3s forwards;
}

.user-message {
  align-self: flex-end;
  background-color: #e1f5fe;
  border-bottom-right-radius: 2px;
}

.ai-message {
  align-self: flex-start;
  background-color: white;
  border-bottom-left-radius: 2px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.message-sender {
  font-size: 13px;
  font-weight: 600;
  color: #555;
}

.message-time {
  font-size: 12px;
  color: #888;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  white-space: pre-line;
  font-family: 'Georgia', 'Times New Roman', serif;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.typing-indicator span {
  height: 8px;
  width: 8px;
  border-radius: 50%;
  background-color: #6366F1;
  display: inline-block;
  margin: 0 2px;
  animation: bounce 1.5s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-8px); }
}

/* 快捷提示区域 */
.quick-prompts-panel {
  background-color: white;
  border-top: 1px solid #eaeaea;
  overflow: hidden;
}

.prompt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
}

.prompt-header h4 {
  margin: 0;
  font-size: 14px;
  color: #333;
  font-family: 'Montserrat', 'Arial', sans-serif;
}

.toggle-btn {
  background: none;
  border: none;
  color: #6366F1;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.prompt-content {
  padding: 0 16px 12px;
}

.prompt-categories {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.category-tab {
  padding: 4px 10px;
  font-size: 12px;
  background-color: #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.category-tab:hover {
  background-color: #e0e0ff;
}

.category-tab.active {
  background-color: #6366F1;
  color: white;
}

.quick-prompts-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.prompt-chip {
  padding: 5px 10px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.prompt-chip:hover {
  background-color: #e0e0ff;
  border-color: #d0d0ff;
  transform: translateY(-2px);
}

/* 提示词展开/收起动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  max-height: 300px;
  opacity: 1;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
  margin-bottom: 0;
}

/* 输入区域 */
.input-area {
  display: flex;
  padding: 12px 16px;
  background-color: white;
  border-top: 1px solid #eaeaea;
  align-items: flex-end;
  gap: 8px;
}

.input-area textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  outline: none;
  transition: all 0.3s;
  font-family: 'Georgia', 'Times New Roman', serif;
}

.input-area textarea:focus {
  border-color: #6366F1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #6366F1;
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background-color: #4F46E5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.send-btn:disabled {
  background-color: #d1d1d1;
  cursor: not-allowed;
}
</style>