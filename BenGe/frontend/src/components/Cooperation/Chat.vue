<template>
  <div class="chat-container">
    <div style="
        width: 100px;
        margin-left: auto;
        margin-right: auto;
        font-size: 12px;
        border-color: black;
        border-width: 16px;
        border-radius: 5px;
      ">
      现在开始聊天吧~
    </div>
    <!-- 聊天内容区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in socketState.messages" :key="index" :class="[
        'message',
        message.isSystem
          ? 'message-system'
          : message.isMe
            ? 'message-me'
            : 'message-other',
      ]">
        <div v-if="!message.isSystem" class="message-avatar">
          <div class="avatar-circle">
            <img :src="message.avatar" alt="avatar" style="width: 40px; height: 40px; border-radius: 20px" />
          </div>
        </div>
        <div class="message-content">
          <div v-if="!message.isSystem" class="message-sender">
            {{ message.sender }}
          </div>
          <div class="message-bubble" v-html="renderMarkdown(message.content)"></div>
          <div class="message-time">{{ message.time }}</div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <input type="text" v-model="newMessage" @keyup.enter="sendChatMessage" placeholder="输入消息..."
        class="message-input" />
      <div class="send-button" @click="sendChatMessage">
        <i class="fa-solid fa-paper-plane"></i>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, computed } from 'vue';
import loginImage from "@/assets/login.png";
import { socketState } from '@/stores/socket';
import { nextTick } from 'vue';
import { collectContextData } from '@/utils/contextCollector';
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';

// 初始化Markdown渲染器
const md = new MarkdownIt({
  html: false, // 禁用HTML标签以提高安全性
  breaks: true, // 转换换行符
  linkify: true, // 自动识别链接
  typographer: false // 禁用typographer避免符号转换问题
});
// 接收 props
const props = defineProps({
  roomId: {
    type: [Number, String],
    default: 1,
  },
  roomName: {
    type: String,
    default: "群聊房间",
  },
  members: {
    type: Array,
    default: () => [],
  },
  userId: {
    type: String,
    default: "anonymous",
  },
  userName: {
    type: String,
    default: "匿名用户",
  },
  avatar: {
    type: String,
    default: loginImage,
  },
  initialMessages: {
    type: Array,
    default: () => [],
  },
});

// 响应式变量
const isMemberListVisible = ref(false);
const newMessage = ref("");
const messages = ref(props.initialMessages);
const currentUserId = ref(null);
const currentUsername = ref(null);

// 切换成员列表显示
const toggleMemberList = () => {
  isMemberListVisible.value = !isMemberListVisible.value;
};

// Markdown渲染函数
const renderMarkdown = (content) => {
  if (!content) return '';

  try {
    // 清理内容，移除可能的多余符号和空白
    let cleanContent = content.trim();

    // 渲染Markdown
    const rendered = md.render(cleanContent);

    // 使用DOMPurify清理HTML，防止XSS攻击
    const sanitized = DOMPurify.sanitize(rendered);

    return sanitized;
  } catch (error) {
    // console.error('Markdown渲染失败:', error);
    // 渲染失败时返回原始文本，但进行HTML转义
    return content.replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
};

// 获取当前时间
const getCurrentTime = () => {
  const now = new Date();
  return `${now.getHours()}:${now.getMinutes().toString().padStart(2, "0")}`;
};

// 发送消息
const sendChatMessage = () => {
  if (newMessage.value.trim() === "") return;

  let messageContent = newMessage.value;

  // 对于@ai消息，收集上下文数据并以JSON格式发送
  if (newMessage.value.startsWith('@ai')) {
    try {
      const contextData = collectContextData();
      // 将用户消息和上下文数据组合为JSON格式
      const aiMessageData = {
        userMessage: newMessage.value.replace(/^@ai\s*/, '').trim() || '你好',
        contextData: contextData
      };
      messageContent = `@ai:${JSON.stringify(aiMessageData)}`;
    } catch (error) {
      // console.error('收集上下文数据失败:', error);
      // 如果上下文收集失败，发送简单的@ai消息
      messageContent = newMessage.value;
    }
  }

  const messageData = {
    type: "chat",
    content: messageContent,
    time: getCurrentTime(),
    avatar: props.avatar,
  };

  if (socketState.socket && socketState.socket.readyState === WebSocket.OPEN) {
    socketState.socket.send(JSON.stringify(messageData));
    // 发送消息后自动滚动到底部
    scrollToBottom();
  } else {
    // console.error("WebSocket连接未就绪");
  }

  newMessage.value = "";
};

// 检查用户是否在查看历史消息
const isUserScrollingUp = () => {
  if (!messagesContainer.value) return false;
  const container = messagesContainer.value;

  // 更宽松的阈值，考虑到新消息可能导致内容高度变化
  const threshold = 300; // 增加阈值到300px
  const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;

  return distanceFromBottom > threshold;
};

// 滚动到最底部（带平滑效果和智能判断）
const scrollToBottom = (force = false) => {
  // console.log('🎯 [SCROLL DEBUG] scrollToBottom被调用, force:', force);

  nextTick(() => {
    if (!messagesContainer.value) {
      // console.log('❌ [SCROLL DEBUG] messagesContainer未找到');
      return;
    }

    const container = messagesContainer.value;


    // 如果用户正在查看历史消息且不是强制滚动，则不自动滚动
    if (!force && isUserScrollingUp()) {
      // console.log('🚫 [SCROLL DEBUG] 用户正在查看历史消息，跳过自动滚动');
      return;
    }

    // console.log('✅ [SCROLL DEBUG] 开始执行滚动');

    // 平滑滚动到底部
    container.scrollTo({
      top: container.scrollHeight,
      behavior: 'smooth'
    });

    // 验证滚动结果
    setTimeout(() => {

    }, 200);
  });
};

// Refs - 移到前面确保正确初始化
const messagesContainer = ref(null);

// 监听 props 更新
watch(() => props.initialMessages, (newVal) => {
  messages.value = newVal;
});

// 用于跟踪消息数量和滚动状态的变量
let lastMessageCount = 0;
let wasNearBottom = true; // 记录新消息到达前是否接近底部

// 检查是否接近底部（用于判断是否应该自动滚动）
const isNearBottom = () => {
  if (!messagesContainer.value) return true;
  const container = messagesContainer.value;
  const threshold = 150; // 150px阈值
  const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
  return distanceFromBottom <= threshold;
};

// 监听消息数组变化
watch(() => socketState.messages, (newMessages) => {
  // console.log('🔍 [SCROLL DEBUG] 消息数组变化检测');
  // console.log('当前消息数量:', newMessages?.length);
  // console.log('上次记录数量:', lastMessageCount);

  const currentCount = newMessages?.length || 0;
  const hasNewMessage = currentCount > lastMessageCount;

  if (hasNewMessage && currentCount > 0) {
    const latestMessage = newMessages[currentCount - 1];


    // 判断是否应该自动滚动
    const shouldAutoScroll = wasNearBottom || latestMessage.isMe;


    if (shouldAutoScroll) {
      // console.log('🚀 [SCROLL DEBUG] 准备执行自动滚动');

      nextTick(() => {
        // console.log('📋 [SCROLL DEBUG] nextTick执行');
        setTimeout(() => {
          // console.log('⏰ [SCROLL DEBUG] setTimeout执行，强制滚动到底部');
          scrollToBottom(true); // 强制滚动，忽略isUserScrollingUp检查
        }, latestMessage.isAI ? 200 : 50); // AI消息需要更多时间渲染
      });
    } else {
      // console.log('🚫 [SCROLL DEBUG] 用户不在底部，跳过自动滚动');
    }
  }

  // 更新状态
  lastMessageCount = currentCount;
  // 延迟更新wasNearBottom状态，避免在DOM更新过程中误判
  setTimeout(() => {
    wasNearBottom = isNearBottom();
    // console.log('📍 [SCROLL DEBUG] 更新底部状态:', wasNearBottom);
  }, 100);
}, {
  deep: true,
  flush: 'post'
});

// 组件挂载时滚动到底部
onMounted(() => {
  // 初始化状态
  lastMessageCount = socketState.messages?.length || 0;
  wasNearBottom = true;



  // 延迟执行确保组件完全挂载
  nextTick(() => {
    setTimeout(() => {
      scrollToBottom(true);
    }, 200);
  });
});
</script>


<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  /* max-height: 648px; */
  width: 100%;
  background-color: #ffffff2d;
  backdrop-filter: blur(3px);
  border-radius: 16px;
  overflow: hidden;
  /* box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); */
}

/* 头部样式 */
.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: black;
  position: relative;
  z-index: 10;
}

/* .member-toggle {
  background: none;
  border: none;
  color: black;
  font-size: 20px;
  cursor: pointer;
  margin-right: 12px;
  padding: 4px 8px;
}

.room-name {
  margin: 0;
  font-size: 18px;
  font-weight: normal;
} */

/* 成员列表样式 */

/* 聊天消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  background-color: transparent;
}

.message {
  display: flex;
  margin-bottom: 8px;
}

.message-me {
  flex-direction: row-reverse;
  color: white;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 4px;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
}

.message-content {
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 18px;
  line-height: 1.4;
  word-break: break-word;
}

/* Markdown渲染样式 */
.message-bubble h1,
.message-bubble h2,
.message-bubble h3,
.message-bubble h4,
.message-bubble h5,
.message-bubble h6 {
  margin: 8px 0 4px 0;
  font-weight: bold;
}

.message-bubble h1 { font-size: 1.2em; }
.message-bubble h2 { font-size: 1.1em; }
.message-bubble h3 { font-size: 1.05em; }
.message-bubble h4,
.message-bubble h5,
.message-bubble h6 { font-size: 1em; }

.message-bubble p {
  margin: 4px 0;
}

.message-bubble ul,
.message-bubble ol {
  margin: 4px 0;
  padding-left: 20px;
}

.message-bubble li {
  margin: 2px 0;
}

.message-bubble strong {
  font-weight: bold;
}

.message-bubble em {
  font-style: italic;
}

.message-bubble code {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-bubble pre {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 8px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 4px 0;
}

.message-bubble pre code {
  background-color: transparent;
  padding: 0;
}

.message-bubble blockquote {
  border-left: 3px solid rgba(0, 0, 0, 0.2);
  margin: 4px 0;
  padding-left: 10px;
  font-style: italic;
}

.message-bubble a {
  color: inherit;
  text-decoration: underline;
}

/* 针对AI消息的特殊样式 */
.message-other .message-bubble code {
  background-color: rgba(0, 0, 0, 0.08);
}

.message-other .message-bubble pre {
  background-color: rgba(0, 0, 0, 0.08);
}

.message-other .message-bubble blockquote {
  border-left-color: rgba(0, 0, 0, 0.15);
}

.message-me .message-bubble code {
  background-color: rgba(255, 255, 255, 0.2);
}

.message-me .message-bubble pre {
  background-color: rgba(255, 255, 255, 0.2);
}

.message-me .message-bubble blockquote {
  border-left-color: rgba(255, 255, 255, 0.3);
}

.message-me .message-bubble {
  background-color: #5d98f5;
  border-top-right-radius: 4px;
}

.message-other .message-bubble {
  background-color: #e9edf4;
  border-top-left-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: hsl(0, 11%, 84%);
  margin-top: 4px;
}

.message-me .message-time {
  text-align: right;
}

/* 系统消息样式 */
.message-system {
  justify-content: center;
  margin: 8px 0;
}

.message-system .message-content {
  max-width: 80%;
  text-align: center;
}

.message-system .message-bubble {
  background-color: #f0f0f0;
  color: #666;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 12px;
}

/* 输入区域样式 */
.chat-input-area {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-top: 0.5px solid #eee;
}

.message-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #ccc;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

/* 外层按钮容器 - 圆形蓝底 */
.send-button {
  width: 36px;
  height: 36px;
  margin-left: 10px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 图标本身 */
.send-button i {
  font-size: 16px;
  color: #2563eb;
  pointer-events: none; /* 避免事件冲突 */
  transition: all 0.3s ease;
}

.send-button:hover {
  background-color: #2563eb;
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.4);
}
.send-button:hover i {
  color: white;
}

</style>
