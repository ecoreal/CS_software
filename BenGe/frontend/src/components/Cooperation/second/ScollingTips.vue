<template>
  <div
    class="scrolling-tips"
    :style="{ width: containerWidth }"
  >
  <img style="object-fit: cover;width: 20px;height: 20px;" src="../../../assets/second/tips.png"/>
    <transition
      name="slide-fade"
      mode="out-in"
      @before-leave="onBeforeLeave"
    >
      <p ref="tipEl" style="margin-left: 10px; color:#8BC0FA;" :key="currentTip">{{ currentTip }}</p>
    </transition>

    <!-- 隐藏的预测文本，用于提前测量下一条宽度 -->
    <p ref="nextTipEl" class="hidden-tip">{{ currentTip }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';

const tips = [
  '设计你的剧本主线，确保起承转合清晰。',
  '尝试为角色赋予独特的动机和冲突，增强故事的张力。',
  '在设计线索时，考虑不同人物之间的信息差。',
  '构建剧情反转时，确保伏笔的合理性，避免突兀。',
  '思考每个场景的情感变化和高潮时刻。',
  '记得在适当的时刻加入悬念，让观众保持兴趣。',
  '角色之间的互动要有深度，避免单一对话。',
  '设计结局时，考虑是否能给观众留下深刻印象。',
  '为每个角色设计一个独特的背景故事，使其更加丰满。',
  '思考每个节点的作用，确保每一环节都能推动故事发展。',
  '根据人物设定，设计他们的语言风格和行为模式。',
  '构思场景时，考虑不同的环境和氛围对剧情的影响。',
  '在设计复杂剧情时，确保人物的动机一致且合理。',
  '设计节奏时，适当分配高潮与平缓的部分。',
  '不忘设计出彩的细节，这些细节会增加剧本的层次感。',
  '思考剧情中的冲突与解决方案，给角色安排挑战。'
];


const currentIndex = ref(0);
const currentTip = ref(tips[0]);
const nextTip = ref(tips[1]);

const tipEl = ref(null);
const nextTipEl = ref(null);
const containerWidth = ref('auto');

// 提前计算下一个提示的宽度
function updateNextWidth() {
  if (nextTipEl.value) {
    const width = nextTipEl.value.scrollWidth + 60;
    containerWidth.value = `${width}px`;
  }
}

function changeTip() {
  currentIndex.value = (currentIndex.value + 1) % tips.length;
  currentTip.value = tips[currentIndex.value];
  nextTip.value = tips[(currentIndex.value + 1) % tips.length];
  nextTick(() => updateNextWidth());
}

// 触发切换时提前更新宽度
function onBeforeLeave() {
  updateNextWidth();
}

onMounted(() => {
  updateNextWidth();
  setInterval(changeTip, 8000);
});
</script>

<style scoped>
.scrolling-tips {
  position: fixed;
  bottom: 20px;
  left: 20px;
  font-size: 18px;
  color: black;
  background-color: #F5F8FD;
  padding: 8px 8px;
  border-radius: 20px;
  box-shadow: 0 12px 15px rgba(0, 0, 0, 0.2);
  z-index: 9000;
  overflow: hidden;
  height: 50px;
  display: inline-block;
  /* line-height: 50px; */
  align-items: center;
  transition: width 0.5s ease;
  white-space: nowrap;
}

.scrolling-tips p {
  margin: 0;
  display: inline-block;
  white-space: nowrap;
}

/* 隐藏预测文本 */
.hidden-tip {
  visibility: hidden;
  position: absolute;
  left: -9999px;
  top: -9999px;
  white-space: nowrap;
}

/* 动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: transform 0.5s ease, opacity 0.5s ease;
}

.slide-fade-enter-from {
  transform: translateY(100%);
  opacity: 0;
  will-change: transform, opacity;
}

.slide-fade-enter-to {
  transform: translateY(0);
  opacity: 1;
  will-change: transform, opacity;
}

.slide-fade-leave-from {
  transform: translateY(0);
  opacity: 1;
  will-change: transform, opacity;
}

.slide-fade-leave-to {
  transform: translateY(-100%);
  opacity: 0;
  will-change: transform, opacity;
}

/* 避免影响布局，使用绝对定位 */
.scrolling-tips p {
  position: absolute; 
}

</style>
