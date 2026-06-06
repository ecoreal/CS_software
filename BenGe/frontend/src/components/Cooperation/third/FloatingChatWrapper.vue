<template>
  <div class="floating-chat" :class="{ expanded: isExpanded }"
    :style="{ top: position.top + 'px', left: position.left + 'px' }">
    <!-- 头部：拖动和收缩按钮 -->
    <div class="chat-header-bar" @mousedown="startDrag">
      <div class="chat-header-bg"></div>
      <div class="chat-header-front">
        <div class="user-info">
          <img :src="avatar" class="user-avatar" />
          <span class="user-name">{{ userName }}</span>
        </div>
        <i class="fa-solid toggle-button" :class="isExpanded ? 'fa-chevron-down' : 'fa-chevron-up'"
          @click.stop="toggleExpand"></i>
      </div>
    </div>

    <!-- 聊天内容 -->
    <transition name="fade-slide">
      <div v-show="isExpanded" class="chat-inner-wrapper">
        <Chat :roomId="roomId" :userId="userId" :userName="userName" :avatar="avatar" :initialMessages="initialMessages"
          @membersUpdated="$emit('membersUpdated', $event)" />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import Chat from "../Chat.vue";
import loginImage from "@/assets/login.png";

defineProps({
  roomId: Number,
  userId: String,
  userName: String,
  avatar: {
    type: String,
    default: loginImage
  },
  initialMessages: {
    type: Array,
    default: () => []
  }
});

defineEmits(["membersUpdated"]);

const isExpanded = ref(true);
const position = reactive({ top: 300, left: 100 });

let dragging = false;
let offsetX = 0;
let offsetY = 0;

const startDrag = (e) => {
  // 只响应鼠标左键
  if (e.button !== 0) return;
  dragging = true;
  offsetX = e.clientX - position.left;
  offsetY = e.clientY - position.top;

  document.addEventListener("mousemove", onDrag);
  document.addEventListener("mouseup", stopDrag);
};

const onDrag = (e) => {
  if (!dragging) return;
  position.left = e.clientX - offsetX;
  position.top = e.clientY - offsetY;
};

const stopDrag = () => {
  dragging = false;
  document.removeEventListener("mousemove", onDrag);
  document.removeEventListener("mouseup", stopDrag);
};

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value;
};
</script>

<style scoped>
.floating-chat {
  position: fixed;
  width: 320px;
  background: #fff;
  border-radius: 16px;
  box-shadow:
    0 6px 20px rgba(0, 0, 0, 0.589),
    /* 主阴影：更高位移，更大模糊 */
    0 4px 10px rgba(0, 0, 0, 0.315);
  /* 次阴影：柔和光晕 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 999;
  transition: height 0.3s ease;
  transform-origin: top;
}

/* 展开高度 */
.floating-chat.expanded {
  height: 520px;
}

/* 收缩高度 */
.floating-chat:not(.expanded) {
  height: 40px;
}

.chat-header-bar {
  cursor: move;
  user-select: none;
  height: 40px;
  width: 100%;
  position: relative;
  z-index: 2;
  box-shadow:
    inset 0 -1px 0 rgba(0, 0, 0, 0.1),
    /* 原有下边框阴影 */
    inset 0 2px 4px rgba(255, 255, 255, 0.6);
  /* 顶部亮色高光 */
}

.chat-header-bg {
  width: 120%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  background-image: url('@/assets/chat-header-bg.png');
  background-repeat: no-repeat;
  background-size: cover;
  z-index: 0;
}

.chat-header-front {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  backdrop-filter: blur(2px);
  background: transparent;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 6px;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  color: #ECEDF0;
}

.toggle-button {
  font-size: 14px;
  color: #ECEDF0;
  cursor: pointer;
}

.chat-inner-wrapper {
  flex: 1;
  display: flex;
  height: 100%;
  background-image: url('@/assets/chat-main-bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 展开收缩过渡 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
}
</style>
