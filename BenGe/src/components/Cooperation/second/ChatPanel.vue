<template>
  <div class="floating-wrapper">
    <!-- 控制按钮，浮在最外层 -->
    <div
      @click="toggleCollapse"
      class="toggle-btn"
      :class="{ 'collapsed-btn': isCollapsed }"
    >
      <transition name="fade">
        <img class="img" v-if="isCollapsed" :src="chatIcon" />
      </transition>
      <transition name="fade">
        <img class="img" v-if="!isCollapsed" :src="collapseIcon" />
      </transition>
    </div>

    <!-- 主对话容器 -->
    <div class="container" :class="{ collapsed: isCollapsed }">
      <Chat v-show="!isCollapsed" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import chatIcon from "@/assets/second/chat.png";
import collapseIcon from "@/assets/second/collapse.png";
import Chat from "../Chat.vue";

const isCollapsed = ref(false);

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  // console.log("test:", isCollapsed.value);
};

const currentIcon = computed(() =>
  isCollapsed.value ? chatIcon : collapseIcon
);
</script>

<style scoped>
.floating-wrapper {
  position: fixed;
  bottom: 80px;
  right: 80px;
  z-index: 2000;
  pointer-events: none; /* 保证只有子元素响应点击 */
}

.container {
  width: 300px;
  height: 500px;
  background-color: white;
  transition: all 0.5s ease-in-out;
  border-radius: 16px;
  overflow: hidden;
  padding: 4px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
  background-image: url('@/assets/second/chatbackground.png');
  object-fit: cover;
}

.container.collapsed {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  padding: 0;
  overflow: hidden;
}

/* 按钮基础样式 */
.toggle-btn {
  position: absolute;
  width: 30px;
  height: 30px;
  top: 20px;
  left: 20px;
  z-index: 9000;
  cursor: pointer;
  opacity: 1;
  transition: all 0.5s ease;

  pointer-events: auto;
}

/* 折叠状态下的按钮样式 */
.toggle-btn.collapsed-btn {
  width: 120px;
  height: 90px;
  top: -10px;
  left: -30px;
}

/* 图片样式 */
.img {
  width: 100%; /* 让图片宽度适应父容器 */
  height: 100%; /* 让图片高度适应父容器 */
  object-fit: cover; /* 保持比例，并裁剪超出的部分 */
  position: absolute;
  pointer-events: none; /* 不响应鼠标事件 */
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.fade-enter-to,
.fade-leave-from {
  opacity: 1;
}
</style>
