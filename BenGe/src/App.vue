<template>
  <div id="app">
    <router-view />
    <AIChatWidget v-if="loadingStore.aiAssistantLoading" />
    <transition name="slide-fade" mode="out-in">
      <FullScreenLoading v-if="loadingStore.loading" />
    </transition>
    <transition name="slide-fade" mode="out-in">
      <FullScreenLoading2 v-if="loadingStore.loading2" />
    </transition>
    <transition name="slide-fade" mode="out-in">
      <FullScreenLoading3 v-if="loadingStore.loading3" />
    </transition>
  </div>
</template>

<script setup>
import FullScreenLoading from "./components/FullScreenLoading.vue";
import FullScreenLoading2 from "./components/Cooperation/first/FullScreenLoading2.vue";
import FullScreenLoading3 from "./components/Cooperation/second/FullScreenLoading3.vue";
import { userLoadingStore } from "./stores/userLoadingStore";
import AIChatWidget from "./components/AIChatWidget.vue";

const loadingStore = userLoadingStore();
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html,
body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

#app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
}
/* 动画名要和 transition 组件的 name 属性一致 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.6s ease;
  position: fixed; /* 避免滑动时占位影响页面结构 */
  width: 100%;
  z-index: 9999;
}

.slide-fade-enter-from {
  transform: translateY(100%); /* 从下方进入 */
  opacity: 0;
}

.slide-fade-enter-to {
  transform: translateY(0%);
  opacity: 1;
}

.slide-fade-leave-from {
  transform: translateY(0%);
  opacity: 1;
}

.slide-fade-leave-to {
  transform: translateY(-100%); /* 向上退出 */
  opacity: 0;
}
</style>
