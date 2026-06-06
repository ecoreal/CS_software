<template>
  <div class="direction-select">
    <div class="title">
      <h1
        style="
          font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI',
            Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue',
            sans-serif;
        "
      >
        Script Direction Selection
      </h1>
    </div>

    <div class="selection-container">
      <!-- 目标区域 (已选择) -->
      <div class="target-area">
        <h3>已选择的方向主题 (最多3个)</h3>
        <div
          class="card-container"
          @drop="onDrop($event, 'selected')"
          @dragover.prevent
          @dragenter.prevent
        >
          <div
            v-for="(item, index) in selectedDirections"
            :key="'selected-' + index"
            class="direction-card"
            draggable="true"
            @dragstart="startDrag($event, item, 'selected')"
            :title="item"
          >
            <span class="card-content">{{ item }}</span>
            <span class="remove-btn" @click="removeDirection(index, 'selected')"
              >×</span
            >
          </div>
          <div v-if="selectedDirections.length === 0" class="empty-placeholder">
            请从右侧拖入方向主题
          </div>
        </div>
      </div>

      <!-- 存放区域 (未选择) -->
      <div class="storage-area">
        <h3>可选的方向主题</h3>
        <div
          class="card-container"
          @drop="onDrop($event, 'unselected')"
          @dragover.prevent
          @dragenter.prevent
        >
          <div
            v-for="(item, index) in unselectedDirections"
            :key="'unselected-' + index"
            class="direction-card"
            draggable="true"
            @dragstart="startDrag($event, item, 'unselected')"
            :title="item"
            :style="getCardStyle(index)"
          >
            <span class="card-content">{{ item }}</span>
            <span class="remove-btn" @click="deleteDirection(index)">×</span>
          </div>
        </div>
      </div>
    </div>

    <div
      style="display: flex; align-items: center; justify-content: space-between"
    >
      <div class="add-direction">
        <input
          v-model="newDirection"
          type="text"
          placeholder="输入新的方向主题"
          @keyup.enter="addNewDirection"
          style="width: 300px; height: 36px; border-radius: 18px"
        />
        <button
          @click="addNewDirection"
          class="confirm-btn"
          style="width: 84px"
        >
          添加
        </button>
      </div>

      <button
        class="confirm-btn"
        :disabled="selectedDirections.length === 0"
        @click="confirmSelection"
      >
        确认选择
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { ElMessage } from "element-plus"; // 如果你用的是 Element Plus
import { socketState } from "@/stores/socket";
import { isOwner } from "@/api/room";

// 判断成员是否全部选择了
const allMembersChosen = computed(
  () =>
    socketState.members.length > 0 &&
    socketState.members.every((member) => member.hasChosen === true)
);
watch(allMembersChosen, (newVal) => {
  if (newVal) {
    emit("confirm", selectedDirections.value);
  }
});

const isOwnerResult = ref(false);

onMounted(async () => {
  isOwnerResult.value = !(await isOwner(socketState.roomId));
});

const selectedDirections = ref([]);
const unselectedDirections = ref([
  "悬疑推理",
  "浪漫爱情",
  "科幻未来",
  "历史传奇故事",
  "青春校园",
  "恐怖惊悚",
]);

const newDirection = ref("");
const dragItem = ref(null);
const dragSource = ref(null);

const emit = defineEmits(["confirm"]);

// 开始拖拽
function startDrag(event, item, source) {
  dragItem.value = item;
  dragSource.value = source;
  event.dataTransfer.effectAllowed = "move";
}

// 放置处理
function onDrop(event, targetArea) {
  if (!dragItem.value) return;

  if (dragSource.value === targetArea) return;

  const item = dragItem.value;

  // 从原区域移除
  if (dragSource.value === "selected") {
    const index = selectedDirections.value.indexOf(item);
    if (index !== -1) selectedDirections.value.splice(index, 1);
  } else {
    const index = unselectedDirections.value.indexOf(item);
    if (index !== -1) unselectedDirections.value.splice(index, 1);
  }

  // 添加到目标区域
  if (targetArea === "selected") {
    if (selectedDirections.value.length >= 3) {
      ElMessage.warning("最多只能选择3个方向主题");

      // 放回原区域
      if (dragSource.value === "selected") {
        selectedDirections.value.push(item);
      } else {
        unselectedDirections.value.push(item);
      }

      return;
    }

    selectedDirections.value.push(item);
  } else {
    unselectedDirections.value.push(item);
  }

  // console.log("选择了方向：", selectedDirections.value);
  socketState.socket.send(
    JSON.stringify({ type: "vote", key: selectedDirections.value })
  );

  dragItem.value = null;
  dragSource.value = null;
}

// 从“已选”区域移除
function removeDirection(index) {
  const [removed] = selectedDirections.value.splice(index, 1);
  unselectedDirections.value.push(removed);

  socketState.socket.send(
    JSON.stringify({ type: "vote", key: selectedDirections.value })
  );
}

// 从“可选”区域删除
function deleteDirection(index) {
  unselectedDirections.value.splice(index, 1);
}

// 添加新方向
function addNewDirection() {
  const value = newDirection.value.trim();
  if (!value) {
    ElMessage.warning("请输入有效方向主题");
    return;
  }

  if (unselectedDirections.value.includes(value)) {
    ElMessage.warning("该方向主题已存在");
    return;
  }

  unselectedDirections.value.push(value);
  newDirection.value = "";
}

// 确认选择
function confirmSelection() {
  if (selectedDirections.value.length === 0) {
    ElMessage.warning("请至少选择一个方向主题");
    return;
  }
  // console.log("最终选择方向：", true);
  socketState.socket.send(JSON.stringify({ type: "vote", hasChosen: true }));

  // emit("confirm", selectedDirections.value);
}
// 缓存颜色，确保每个卡片只计算一次颜色
const cardStyles = computed(() => {
  return unselectedDirections.value.map(() => {
    const isBlack = Math.random() > 0.5; // 随机决定是否为黑色背景
    return {
      backgroundColor: isBlack ? "#000" : "#fff",
      color: isBlack ? "#fff" : "#000",
    };
  });
});

// 根据index返回每个卡片的颜色
function getCardStyle(index) {
  return cardStyles.value[index];
}
</script>

<style scoped>
.direction-select {
  padding: 20px;
  font-family: "Arial", sans-serif;
  display: flex;
  flex-direction: column;
  height: 100%;

  justify-content: center;
}

.title {
  color: #333;
  margin-bottom: 20px;
  flex-shrink: 0;
  display: flex;
  margin-top: 20px;
  margin-left: auto;
  margin-right: auto;

  justify-content: flex-start;
  width: 90%;
}
.titleh1::before {
  content: "<";
}
.titleh1::after {
  content: ">";
}

.selection-container {
  margin-left: auto;
  margin-right: auto;
  display: flex;
  justify-content: space-between;
  margin-bottom: 100px;
  gap: 20px;
  flex: 1;
  min-height: 0;
  width: 90%;
}

.target-area {
  width: 48%; /* 固定宽度 */
  height: 100%;
  border: 3px solid #ccc;
  border-radius: 24px;
  padding: 15px;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.storage-area {
  width: 48%; /* 固定宽度 */
  height: 100%;
  border: 3px solid #ccc;
  border-radius: 24px;
  padding: 15px;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.target-area {
  border-color: #64d0a5;
  background-color: rgba(103, 194, 58, 0.05);
}

.storage-area {
  border-color: #409eff;
  background-color: rgba(64, 158, 255, 0.05);
}

h3 {
  margin-top: 0;
  color: #555;
  text-align: center;
  flex-shrink: 0;
}

.card-container {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap; /* 允许换行 */
  gap: 16px;
  overflow-y: auto; /* 纵向滚动 */
  overflow-x: hidden; /* 禁止横向滚动 */

  margin-top: 20px;
  min-height: 49px;
}

.direction-card {
  display: flex; /* 保持你卡片内容水平排列 */
  align-items: center;
  padding: 12px 15px;
  background-color: white;
  border-radius: 27px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  cursor: move;
  position: relative;
  transition: all 0.3s;

  /* 控制宽度，不固定，也不占满一整行 */
  flex: 0 1 auto; /* 不放大，可以缩小，自适应宽度 */
  max-width: 200px; /* 或你希望的最大宽度 */

  font-size: 20px;
  font-weight: bold;
}

.direction-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-content {
  flex: 1;
  word-wrap: break-word;
  white-space: normal;
  overflow: hidden;
  padding-right: 20px;
}

.remove-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #f56c6c;
  font-size: 18px;
  line-height: 1;
  flex-shrink: 0;
}

.empty-placeholder {
  text-align: center;
  color: #999;
  padding: 20px;
  font-style: italic;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-direction {
  display: flex;
  gap: 10px;
  flex-shrink: 0;

  align-items: center;
}

.add-direction input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.add-direction button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.add-direction button:hover {
  background-color: #66b1ff;
}

.confirm-btn {
  width: 120px;
  height: 36px;
  padding: 12px;
  background-color: #337df2;
  color: white;
  border: none;
  border-radius: 18px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-btn:hover {
  background-color: #005cf0;
}

.confirm-btn:disabled {
  background-color: #77767a;
  cursor: not-allowed;
}

.owner-btn {
  position: relative;
  right: -200px;
  display: block;
  width: 96px;
  padding: 12px;
  background-color: #337df2;
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  flex-shrink: 0;
}

.owner-btn:disabled {
  background-color: #77767a;
  cursor: not-allowed;
}
</style>
