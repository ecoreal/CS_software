<template>
  <div class="chat-container">
    <div
      style="
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        margin-left: 20px;
      "
    >
      <h4>聊天区</h4>
    </div>
    <div
      style="
        width: 100px;
        margin-left: auto;
        margin-right: auto;
        font-size: 12px;
        border-color: black;
        border-width: 16px;
        border-radius: 5px;
      "
    >
      现在开始聊天吧~
    </div>
    <!-- 聊天内容区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div
        v-for="(message, index) in socketState.messages"
        :key="index"
        :class="[
          'message',
          message.isSystem
            ? 'message-system'
            : message.isMe
            ? 'message-me'
            : 'message-other',
        ]"
      >
        <div v-if="!message.isSystem" class="message-avatar">
          <div class="avatar-circle">
            <img
              :src="message.avatar"
              alt="avatar"
              style="width: 40px; height: 40px; border-radius: 20px"
            />
          </div>
        </div>
        <div class="message-content">
          <div v-if="!message.isSystem" class="message-sender">
            {{ message.sender }}
          </div>
          <div class="message-bubble">{{ message.content }}</div>
          <div class="message-time">{{ message.time }}</div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <input
        type="text"
        v-model="newMessage"
        @keyup.enter="sendChatMessage"
        placeholder="输入消息..."
        class="message-input"
      />
      <div class="send-button" @click="sendChatMessage">
        <i class="fa-solid fa-paper-plane"></i>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, computed } from "vue";
import loginImage from "@/assets/login.png";
import { socketState } from "@/stores/socket";
import { useCanvasStore } from "@/stores/canvasStore";
import { useCharacterStore } from "@/stores/character";
import { nextTick } from "vue";

const canvasStore = useCanvasStore();
const characterStore = useCharacterStore();

const collectContextData = () => {
  return {
    chat: socketState.messages.map((msg) => ({
      user: msg.username || msg.sender,
      text: msg.content,
      time: msg.time,
    })),

    // 画布节点数据（场景、氛围、线索等）
    canvasNodes: canvasStore.nodes.map((node) => ({
      id: node.id,
      type: node.type,
      title: node.data?.title || "未命名",
      // 根据节点类型提取关键信息
      summary: getNodeSummary(node),
    })),

    // 画布连接关系
    canvasEdges: canvasStore.edges.map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      type: edge.type,
      relationship: edge.data?.relationship || edge.data?.label || "连接",
      description: edge.data?.description || "",
    })),

    // 角色节点数据
    characterNodes: characterStore.nodes.map((node) => ({
      id: node.id,
      type: node.type,
      name: node.data?.name || "未命名角色",
      occupation: node.data?.occupation || "",
      background: node.data?.background?.substring(0, 100) || "",
      personality: Array.isArray(node.data?.personality)
        ? node.data.personality.join(", ")
        : "",
    })),

    // 角色关系
    characterEdges: characterStore.edges.map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      relationship: edge.data?.relationship || "关系",
      description: edge.data?.description || "",
      strength: edge.data?.strength || "",
    })),

    // 房间信息
    room: {
      id: socketState.roomId,
      members: socketState.members.length,
      stage: "第二阶段协作设计",
    },
  };
};

const getNodeSummary = (node) => {
  switch (node.type) {
    case "custom": // 场景节点
      return `场景: ${node.data?.sceneDescription || ""}, 时间: ${
        node.data?.timeLabel || ""
      }, 涉及角色: ${node.data?.characters || ""}`;

    case "character": // 角色节点
      return `角色: ${node.data?.name || ""}, 职业: ${
        node.data?.occupation || ""
      }, 性格: ${
        Array.isArray(node.data?.personality)
          ? node.data.personality.join(", ")
          : ""
      }`;

    case "atmosphere": // 氛围节点
      return `氛围: ${node.data?.mood || ""}, 灯光: ${
        node.data?.lighting || ""
      }, 音乐: ${node.data?.music || ""}`;

    case "clue": // 线索节点
      return `线索: ${node.data?.detail || ""}, 相关事件: ${
        node.data?.relatedEvent || ""
      }`;

    case "inference": // 推理节点
      return `推理: ${node.data?.summary || ""}, 证据: ${
        node.data?.evidence || ""
      }`;

    case "person": // 人物节点
      return `人物: ${node.data?.name || ""}, 简介: ${node.data?.bio || ""}`;

    default:
      return JSON.stringify(node.data).substring(0, 100);
  }
};
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
  } else {
    // console.error("WebSocket连接未就绪");
  }

  newMessage.value = "";
};

// 滚动到最底部
const scrollToBottom = () => {
  nextTick(() => {
    // 确保 messagesContainer 已加载
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

// 监听 props 更新
watch(
  () => props.initialMessages,
  (newVal) => {
    messages.value = newVal;
  }
);

// Refs
const messagesContainer = ref(null);
</script>

<style>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  overflow: hidden;
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
  background: transparent;
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

/* hover 效果 */
.send-button:hover {
  background-color: #2563eb;
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.4);
}

/* 关键点：按钮 hover 时图标变色 */
.send-button:hover i {
  color: white;
}
</style>
