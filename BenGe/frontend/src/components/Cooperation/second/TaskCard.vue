<template>
  <div :class="['task-card-wrapper', { 'slide-in': isSliding }]">
    <div class="task-card">
      <!-- 上层卡片 -->
      <div class="task-card-top">
        <div class="task-card-head">
          <img style="object-fit: cover; width: 30px; height: 30px;" src="../../../assets/second/wink.png" />
          <h3 style="margin-left: 4px;">任务卡片</h3>
          <div class="task-card-toggle" @click="toggleDrawer">
            <i :class="['fas', isSliding ? 'fa-caret-left' : 'fa-caret-right']"></i>
          </div>
        </div>

        <div class="task-card-content-fixed">
          <transition name="fade" mode="out-in">
            <div :key="currentIndex" class="task-item">
              {{ contentList[currentIndex] }}
            </div>
          </transition>
        </div>

        <div class="task-card-controls">
          <i class="fas fa-chevron-left" @click="prevItem"></i>
          <span>{{ currentIndex + 1 }}/{{ contentList.length }}</span>
          <i class="fas fa-chevron-right" @click="nextItem"></i>
        </div>
      </div>

      <!-- 背景卡片 -->
      <div class="task-card-bottom"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  content: {
    type: [String, Array],
    required: true
  }
})

const contentList = Array.isArray(props.content) ? props.content : [props.content]
const currentIndex = ref(0)

const nextItem = () => {
  currentIndex.value = (currentIndex.value + 1) % contentList.length
}
const prevItem = () => {
  currentIndex.value = (currentIndex.value - 1 + contentList.length) % contentList.length
}

let intervalId = null
onMounted(() => {
  intervalId = setInterval(nextItem, 4000)
})
onUnmounted(() => {
  clearInterval(intervalId)
})

const isDrawerOpen = ref(true)
const isSliding = ref(true)
const toggleDrawer = () => {
  isDrawerOpen.value = !isDrawerOpen.value
  setTimeout(() => {
    isSliding.value = isDrawerOpen.value
  }, 100)
}
</script>

<style scoped>
.task-card-wrapper {
  position: fixed;
  top: 100px;
  left: -250px;
  z-index: 9000;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.5s ease;
}

.task-card-wrapper.slide-in {
  transform: translateX(100%);
}

.task-card {
  position: relative;
  width: 300px;
  height: 200px;
  transform-origin: center;
}

.task-card-top {
  position: absolute;
  width: 100%;
  height: 94%;
  border-radius: 20px;
  background-image: url('../../../assets/second/cardback.png');
  background-size: cover;
  background-color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 10px;
  z-index: 2;
}

.task-card-head {
  margin: 10px;
  width: 100%;
  height: 30px;
  display: flex;
  align-items: center;
  position: relative;
}

.task-card-toggle {
  position: absolute;
  right: 10px;
  font-size: 24px;
  cursor: pointer;
  z-index: 3;
}

/* ✅ 保持内容区域高度固定，避免跳动 */
.task-card-content-fixed {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  overflow: hidden;
  padding: 0 16px;
}

.task-item {
  text-align: center;
  font-size: 16px;
  line-height: 1.4;
  width: 250px;
  white-space: normal;
  word-break: break-word;
}

.task-card-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #333;
}
.task-card-controls i {
  cursor: pointer;
}

/* ✅ 背景卡片：保留你的设计样式 */
.task-card-bottom {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  background-color: white;
  background-image: url('../../../assets/second/cardback.png');
  transition: transform 0.5s ease;
}

.slide-in .task-card-bottom {
  top: 4%;
  left: 8%;
  transform: rotate(8deg);
}

/* ✅ 平滑淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
