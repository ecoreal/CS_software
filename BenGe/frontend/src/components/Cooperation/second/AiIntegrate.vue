<template>
  <teleport to="body">
    <div class="AiIntegrate-overlay">
      <div
        v-if="showAI"
        class="AiIntegrate-panel"
        style="display: flex; justify-content: center; align-items: center"
      >
        <video
          class="loading-video"
          autoplay
          muted
          loop
          playsinline
          src="@/assets/first/book.mp4"
        ></video>
        <h2>AI正在生成完整剧本</h2>
      </div>

      <div v-else class="AiIntegrate-panel" @click.stop>
        <div class="AiIntegrate-header">
          <h3>AI生成完整剧本</h3>
          <button class="close-btn" @click="$emit('close')">×</button>
        </div>

        <div class="AiIntegrate-content">
          <v-md-editor
            v-model="editorContent"
            height="600px"
            :mode="'editable'"
          />
          <div style="text-align: right; margin-top: 16px">
            <button @click="emitContent" class="confirm-btn">确认使用</button>
          </div>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import VMdEditor from "@kangc/v-md-editor";
import "@kangc/v-md-editor/lib/style/base-editor.css";
import githubTheme from "@kangc/v-md-editor/lib/theme/github.js";
import "@kangc/v-md-editor/lib/theme/style/github.css";
import Prism from "prismjs";
import { socketState } from "@/stores/socket";

VMdEditor.use(githubTheme, {
  Prism,
});

const props = defineProps({
  AIcontent: { type: String, default: "# 测试文本" },
});
const emit = defineEmits(["close", "select"]);

const editorContent = ref(props.AIcontent);

// 监听 props 变化时同步到本地
watch(
  () => props.AIcontent,
  (newVal) => {
    editorContent.value = newVal;
  }
);
const showAI = ref(true);

onMounted(() => {
  AiIntegrate();
});

const handleOverlayClick = () => {
  emit("close");
};

import { enterThirdStage } from "@/api/script";
const emitContent = async () => {
  socketState.CompleteScriptContent = editorContent.value;
  // console.log("点击确认：", socketState.CompleteScriptContent);

  await enterThirdStage(socketState.roomId, socketState.CompleteScriptContent);
};

// 产生AI整合的内容
import { collectContextData } from "@/utils/contextCollector";
const contextData = collectContextData();
import { generateCooperateFramework } from "@/api/script";
const AiIntegrate = async () => {
  // console.log("contextData:", contextData);
  const data = {
    contextData: JSON.stringify(contextData),
    roomId: socketState.roomId,
  };
  if (socketState.AICompleteScriptContent.length == 0) {
    const res = await generateCooperateFramework(data);
    // console.log("res:", res);
  }
  showAI.value = false;
};
</script>

<style scoped>
.AiIntegrate-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.AiIntegrate-panel {
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  width: 960px;
  height: 800px;
  margin: 20px;
}

.AiIntegrate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.AiIntegrate-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #95a5a6;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f8f9fa;
  color: #2c3e50;
}

.AiIntegrate-content {
  padding: 24px;

  width: 100%;
  height: 90%;
  background-color: white;
}
.confirm-btn {
  padding: 8px 16px;
  background-color: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.confirm-btn:hover {
  background-color: #1e40af;
}

.loading-video {
  width: 200px; /* 你可以改成 100% 或 cover 效果 */
  height: 200px;
  object-fit: contain;
}

.title {
  font-size: 24px;
  font-weight: bold;
}
</style>
