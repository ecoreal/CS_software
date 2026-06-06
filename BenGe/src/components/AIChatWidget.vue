<template>
  <div>
    <!-- 悬浮球 -->
    <div v-if="!isChatOpen" class="floating-ball" :class="{ hidden: isHidden, 'semi-hidden': isSemiHidden }"
      @mousedown="startDrag" @click="handleClick" :style="{ top: `${position.top}px`, left: `${position.left}px` }">
      💬
    </div>

    <!-- 聊天窗口 -->
    <div v-if="isChatOpen" class="chat-widget"
      :style="{ top: `${adjustedPosition.top}px`, left: `${adjustedPosition.left}px` }">
      <div class="chat-header">
        AI 助手
        <button class="close-button" @click="toggleChat">×</button>
      </div>
      <div class="chat-messages">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.type]">
          {{ msg.content }}
        </div>
        <div v-if="loading" class="loading">AI 正在输入...</div>
      </div>
      <div class="chat-input">
        <textarea v-model="userInput" placeholder="请输入内容" @keydown.enter.prevent="handleEnter"></textarea>
        <button @click="sendMessage" :disabled="loading || !userInput.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive, ref, onBeforeUnmount } from "vue";

export default defineComponent({
  name: "AIChatWidget",
  props: {
    autoHideTime: {
      type: Number,
      default: 3000, // 默认 5 秒后隐藏
    },
    autoEdgingTime: {
      type: Number,
      default: 600, // 默认贴边动画时间 300ms
    },
  },
  setup(props) {
    const isChatOpen = ref(false); // 控制聊天窗口是否展开
    const messages = reactive([]);
    const userInput = ref("");
    const loading = ref(false);

    const position = reactive({ top: 20, left: 20 }); // 悬浮球位置
    const adjustedPosition = reactive({ top: 20, left: 20 }); // 聊天窗口调整后的位置
    const isDragging = ref(false);
    const wasDragged = ref(false); // 用于判断是否发生了拖拽
    const isHidden = ref(false); // 控制悬浮球是否完全隐藏
    const isSemiHidden = ref(false); // 控制悬浮球是否半隐藏
    let hideTimeout = null;

    const toggleChat = () => {
      if (!isChatOpen.value) {
        adjustChatPosition(); // 打开聊天窗口时调整位置
      }
      isChatOpen.value = !isChatOpen.value;
      resetHideTimer(); // 重置隐藏计时器
    };

    const adjustChatPosition = () => {
      const chatWidth = 350; // 聊天窗口宽度
      const chatHeight = 400; // 聊天窗口高度
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;

      // 计算聊天窗口的位置
      adjustedPosition.top = Math.min(
        Math.max(position.top + 70, 0),
        windowHeight - chatHeight
      );
      adjustedPosition.left = Math.min(
        Math.max(position.left, 0),
        windowWidth - chatWidth
      );
    };

    const startDrag = (event) => {
      isDragging.value = true;
      wasDragged.value = false; // 重置拖拽标志
      const dragOffset = {
        x: event.clientX - position.left,
        y: event.clientY - position.top,
      };

      const onDrag = (event) => {
        if (isDragging.value) {
          position.left = event.clientX - dragOffset.x;
          position.top = event.clientY - dragOffset.y;
          wasDragged.value = true;
        }
      };

      const stopDrag = () => {
        isDragging.value = false;
        document.removeEventListener("mousemove", onDrag);
        document.removeEventListener("mouseup", stopDrag);
        if (wasDragged.value) {
          autoEdge(); // 自动贴边
        }
      };

      document.addEventListener("mousemove", onDrag);
      document.addEventListener("mouseup", stopDrag);
    };

    const handleClick = () => {
      // 只有在未发生拖拽时才切换聊天窗口
      if (!wasDragged.value) {
        toggleChat();
      }
    };

    const autoEdge = () => {
      const windowWidth = window.innerWidth;
      const ballWidth = 60; // 悬浮球宽度
      const targetLeft =
        position.left + ballWidth / 2 < windowWidth / 2 ? 0 : windowWidth - ballWidth;

      const startTime = performance.now();
      const startLeft = position.left;

      const animate = (time) => {
        const elapsed = time - startTime;
        const progress = Math.min(elapsed / props.autoEdgingTime, 1);
        position.left = startLeft + (targetLeft - startLeft) * progress;

        if (progress < 1) {
          requestAnimationFrame(animate);
        }
      };

      requestAnimationFrame(animate);
    };

    const resetHideTimer = () => {
      clearTimeout(hideTimeout);
      isHidden.value = false;
      isSemiHidden.value = false;
      hideTimeout = setTimeout(() => {
        isSemiHidden.value = true;
        adjustSemiHiddenPosition();
      }, props.autoHideTime);
    };

    const adjustSemiHiddenPosition = () => {
      const windowWidth = window.innerWidth;
      const ballWidth = 60; // 悬浮球宽度

      // 如果悬浮球在左侧，则部分隐藏在左侧；否则部分隐藏在右侧
      if (position.left < windowWidth / 2) {
        position.left = -ballWidth / 2;
      } else {
        position.left = windowWidth - ballWidth / 2;
      }
    };

    const sendMessage = async () => {
      if (!userInput.value.trim()) return;

      messages.push({ type: "user", content: userInput.value });
      const history = messages.map((msg) => msg.content);
      const requestBody = {
        message: [
          { role: "user", content: userInput.value }
        ],
        history: history.slice(0, -1)
      };

      userInput.value = "";
      loading.value = true;

      try {
        const response = await fetch("/api/ai/chat/stream", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
          },
          body: JSON.stringify(requestBody),
        });

        if (!response.body) throw new Error("No response body");

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let done = false;

        while (!done) {
          const { value, done: readerDone } = await reader.read();
          done = readerDone;
          if (value) {
            const chunk = decoder.decode(value, { stream: true });
            const parsedChunk = parseSSEChunk(chunk);
            updateAIMessage(parsedChunk);
          }
        }
      } catch (error) {
        // console.error("Error fetching AI response:", error);
        messages.push({ type: "ai", content: "AI 回复失败，请稍后重试。" });
      } finally {
        loading.value = false;
      }
    };

    const handleEnter = () => {
      if (!loading.value && userInput.value.trim()) {
        sendMessage();
      }
    };

    const parseSSEChunk = (chunk) => {
      const lines = chunk.split("data:");
      const dataLines = lines
        .filter((line) => {return line.trim() !== "" && !line.startsWith("event:");})
        .map((line) => line.replace("event:message", "").trim())
        .filter((line) => line !== "");
      return dataLines; // 将所有 `data:` 内容拼接成完整字符串
    };

    const updateAIMessage = (chunk) => {
      const lastMessage = messages[messages.length - 1];
      // 如果最后一条消息是 AI 的，则逐字更新
      if (lastMessage && lastMessage.type === "ai") {
        let currentContent = lastMessage.content || "";

        const displayNextChar = () => {
          if (chunk.length === 0) return;
            try{
              const data = JSON.parse(chunk[0]);
              if(data.choices?.[0]?.delta?.content){
                currentContent += data.choices[0].delta.content;
                lastMessage.content = currentContent; // 更新最后一条消息内容
                scrollToBottom(); // 滚动到底部
                chunk.shift();
                if(chunk.length > 0){
                  setTimeout(displayNextChar, 50);
                }
              }
            }catch(e){
              chunk.shift(); // 即使解析失败，也移除当前 chunk
              if (chunk.length > 0) {
                setTimeout(displayNextChar, 50);
              }
            }
        };

        displayNextChar();
      } else {
        // 如果没有 AI 消息，则添加一条新的消息
        messages.push({ type: "ai", content: "" });
        updateAIMessage(chunk); // 递归调用以逐字显示
      }
    };

    const scrollToBottom = () => {
      const chatMessages = document.querySelector(".chat-messages");
      if (chatMessages) {
        setTimeout(() => {
          chatMessages.scrollTop = chatMessages.scrollHeight;
        }, 0); // 使用 `setTimeout` 确保 DOM 更新完成后滚动
      }
    };

    onBeforeUnmount(() => {
      clearTimeout(hideTimeout);
    });

    return {
      isChatOpen,
      messages,
      userInput,
      loading,
      position,
      adjustedPosition,
      isHidden,
      isSemiHidden,
      toggleChat,
      sendMessage,
      handleEnter,
      startDrag,
      handleClick,
      resetHideTimer,
    };
  },
});
</script>

<style scoped>
/* 悬浮球样式 */
.floating-ball {
  position: fixed;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #007bff, #00d4ff);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  cursor: grab;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.5s ease;
  opacity: 1;
}

.floating-ball.hidden {
  opacity: 0;
  pointer-events: none;
}

.floating-ball.semi-hidden {
  opacity: 0.5;
  pointer-events: auto;
}

.floating-ball:active {
  cursor: grabbing;
}

.floating-ball:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

/* 聊天窗口样式 */
.chat-widget {
  position: fixed;
  width: 350px;
  background: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.chat-header {
  background: linear-gradient(135deg, #007bff, #00d4ff);
  color: #fff;
  padding: 15px;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #ccc;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header .close-button {
  background: none;
  border: none;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.chat-header .close-button:hover {
  transform: scale(1.2);
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  max-height: 400px;
  background: #fff;
}

.message {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 10px;
  animation: fadeIn 0.3s forwards;
}

.message.user {
  background: #e0f7fa;
  align-self: flex-end;
  color: #007bff;
}

.message.ai {
  background: #f1f1f1;
  align-self: flex-start;
  color: #333;
}

.loading {
  font-size: 14px;
  color: #888;
  text-align: center;
}

.chat-input {
  display: flex;
  border-top: 1px solid #ddd;
  padding: 10px;
  background: #f9f9f9;
}

textarea {
  flex: 1;
  resize: none;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
}

textarea:focus {
  border-color: #007bff;
}

button {
  margin-left: 10px;
  padding: 10px 15px;
  background: linear-gradient(135deg, #007bff, #00d4ff);
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s ease, transform 0.2s ease;
}

button:hover {
  background: linear-gradient(135deg, #0056b3, #00a3cc);
  transform: scale(1.05);
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}
</style>