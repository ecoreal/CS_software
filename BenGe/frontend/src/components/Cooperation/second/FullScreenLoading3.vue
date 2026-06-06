<template>
  <div v-if="loadingStore.loading3" class="loading-overlay">
    <div class="title">框架确定，正在进入第三阶段...</div>
    <video
      class="loading-video"
      autoplay
      muted
      loop
      playsinline
      src="@/assets/first/book.mp4"
    ></video>
  </div>
</template>

<script setup>
import { userLoadingStore } from "@/stores/userLoadingStore.js";
import { socketState } from "@/stores/socket";
const loadingStore = userLoadingStore();
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #FCF9FC;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.loading-video {
  width: 200px; /* 你可以改成 100% 或 cover 效果 */
  height: 200px;
  object-fit: contain;
}

.title{
  font-size: 24px;
  font-weight: bold;
}

.description{
  font-size: 20px;
  width: 400px;

  margin-top: 20px;
}
</style>
