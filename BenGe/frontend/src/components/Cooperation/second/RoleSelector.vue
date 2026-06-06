<template>
  <div class="choose-area">
    <div class="choose-header">
      <h1 class="select-role-title">请选择你的角色</h1>
      <button
          class="button"
          style="margin-left: auto"
          @click="confirmSelection"

      ><!--:disabled="!allRolesSelected"
          :title="!allRolesSelected ? '请等待所有成员选择角色' : ''"-->
        选择完毕
      </button>
    </div>
    <div class="role-list">
      <div v-for="(role, index) in roles"
           :key="index"
           class="role-wrapper"
           :class="{'blur-others': hoverIndex !== null && hoverIndex !== index}"
           @mouseenter="hoverIndex = index"
           @mouseleave="hoverIndex = null"
      >
        <div class="card-container">
          <!-- 已选择角色打勾 -->
          <div v-if="socketState.roleSelections[roles[index].name]" class="selected-check">
            <i class="el-icon--check"></i>
          </div>
          <!-- 显示已选择的角色用户名 -->
          <div v-if="socketState.roleSelections[roles[index].name]" class="role-username">
            {{ socketState.roleSelections[roles[index].name] }}
          </div>

          <div class="image-container">
            <!-- 模糊背景框 -->
            <div class="blur-background"></div>

            <!-- 鼠标未悬浮展示角色图片1 -->
            <img
                :src="getRoleImage(index,1)"
                alt="角色图片"
                class="role-image"
                :class="{ 'active': hoverIndex !== index }"
            />
            <!-- 鼠标悬浮展示角色图片2 -->
            <img
                :src="getRoleImage(index,2)"
                alt="角色图片"
                class="role-image"
                :class="{'active': hoverIndex === index }"
                @click="selectRole(index,2)"
            />
          </div>
        </div>
      </div>

        <!-- 角色信息展示面板 -->
        <div class="role-info-panel " v-for="(role, index) in roles" :key="index" :class="{'active':hoverIndex === index }">
          <!-- 角色名称 -->
          <div class="role-text">{{ role.name }}</div>
          <!-- 角色描述 -->
          <div class="role-description">{{ role.description }}</div>
        </div>

    </div>
  </div>
</template>

<script setup>
import {ref, defineProps, defineEmits, computed, watch} from 'vue';
import { socketState } from '@/stores/socket';
import { ElMessage } from 'element-plus';

// 定义 props
const props = defineProps({
  roles: {
    type: Array,
    required: true
  }
});

// 定义 emits
const emit = defineEmits(['selected', 'confirm']);

// 响应式数据
const selectedRole = ref(null);  // 当前成员选择的角色

// 当前悬停的角色索引
const hoverIndex = ref(null);

// 获取角色图片（type控制悬停时展示对应角色第二张图片）
function getRoleImage(index, imgType) {
  return require(`@/assets/second/role${index + 1}-${imgType}.png`);
}

// 选择角色
function selectRole(index) {
  const roleName = props.roles[index].name;
  socketState.userRole = index;

  // 如果该角色已被其他成员选择，则弹出提示
  if (socketState.roleSelections[roleName] && socketState.roleSelections[roleName] != socketState.username) {
    ElMessage({
      message: `${socketState.roleSelections[roleName]} 已选择该角色！`,
      type: 'warning',
    });
    return;
  }

  // 如果是当前用户选择角色
  selectedRole.value = props.roles[index];
  emitRoleSelection(roleName);  // 通过 WebSocket 发送角色选择
  
  // 发送选择事件给父组件
  emit('selected', index);
}

// 发送角色选择到服务器
function emitRoleSelection(roleName) {
  socketState.socket.send(
    JSON.stringify({
      type: "role",
      roleName,
      username: socketState.currentUsername,
    })
  );
  // console.log("发送了选择角色消息：", {
  //   type: "role",
  //   roleName,
  //   username: socketState.currentUsername,
  // });
}
/*
const allPlayersSelected = computed(() => {
  // 获取已选择的角色数量
  const selectedCount = Object.values(socketState.roleSelections).filter(role => role !== "").length;

  // 获取房间中的玩家总数（来自成员列表）
  const totalPlayers = socketState.members.length;

  // 确保有玩家存在
  if (totalPlayers === 0) return false;

  return selectedCount >= totalPlayers;
});

// 监听角色选择变化（用于调试）
watch(() => socketState.roleSelections, (newSelections) => {
  // console.log("角色选择变化:", newSelections);
  // console.log("所有玩家是否已选择:", allPlayersSelected.value);
}, { deep: true });

// 监听成员变化（用于调试）
watch(() => socketState.members, (newMembers) => {
  // console.log("成员变化:", newMembers.length);
  // console.log("所有玩家是否已选择:", allPlayersSelected.value);
});
*/
// 确认角色选择
function confirmSelection() {
  emit('confirm', selectedRole.value);
}
</script>


<style scoped>
.button {
  padding: 12px 30px;
  background-color: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

button[disabled] {
  position: relative;
}

button[disabled]:hover::after {
  content: attr(title);
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  white-space: nowrap;
  z-index: 100;
  font-size: 14px;
}

.choose-area {
  display: flex;
  flex-direction: column;
  width: 100%;
  padding: 0;
  margin: 0 auto;
  max-width: 1400px;
}

.choose-header {
  display: flex;
  margin: 0px 20px 20px;
  justify-content: space-between;
  width: 100%;
  align-items: center;
}

.select-role-title {
  color: white;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 24px;
  font-weight: bold;
  text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.5);
}

.role-list {
  display: flex;
  width: 100%;
  margin: auto;
  min-width: auto;
  justify-content: center;
  gap: clamp(20px, 3vw, 40px);
  padding: 20px;
  perspective: 1000px;
}

.role-wrapper {
  width: clamp(180px, 18vw, 220px);
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  position: relative;
  cursor: pointer;
  padding: 0 clamp(10px, 1.5vw, 20px);
  margin-bottom: 100px;
  margin-top: -50px;
}

/* 有卡片被悬停时，其他卡片 */
.role-wrapper.blur-others {
  opacity: 0.6;
  filter: blur(1px);
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

/* 左侧卡片向左下方偏移 */
.role-wrapper.blur-others:has(~ .role-wrapper:hover) {
  transform:
      scale(0.9)
      translateX(-40px)
      translateY(5px)
      rotateY(20deg); /* 3D倾斜效果 */
}

/* 右侧卡片向右下方偏移 */
.role-wrapper:hover ~ .role-wrapper.blur-others {
  transform:
      scale(0.9)
      translateX(40px)
      translateY(5px)
      rotateY(-20deg); /* 反向倾斜 */
}

/* 被悬停的卡片保持高亮 */
.role-wrapper:hover {
  width: 100%;
  height: 90%;
  top: 0;
  opacity: 1;
  filter: none;
  z-index: 10; /* 确保悬停卡片在最上层 */
}

/* 扩大被悬停的卡片的图片容器的显示区域 */
.role-wrapper:hover .card-container {
  transform: scale(1.1) translateY(100px);
  z-index: 10;
  height: 140%;
  margin-top: -20%;
}

/* 选中状态的样式 */
.role-wrapper.selected {
  opacity: 0.7;
}

.selected-check {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 30px;
  height: 30px;
  background-color: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  z-index: 2;
}

.role-username {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 8px;
  font-size: 16px;
  border-radius: 3px;
  z-index: 2;
}

.card-container {
  width: 100%;
  height: clamp(240px, 40vh, 400px);
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);
  position: relative;
  transform-style: preserve-3d;
  margin-top: 0;
}

.image-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  transition: all 0.6s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

/* 模糊背景框样式 */
.blur-background {
  position: absolute;
  background: #B3B2C7;
  width: 100%;
  height: 100%;
  z-index: -1;
  opacity: 0.8;
  transition: opacity 0.3s ease;
  border-radius: 10px;
  border: 2px solid white;
}

/* 悬停时显示模糊背景 */
.role-wrapper:hover .blur-background {
  width: 100%;
  height: 60%;
  top: 5%;
  left: 0;
}

.role-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
  opacity: 0;
}

.role-image.active {
  opacity: 1;
  transform: scale(1.05);
}

.role-image:nth-child(1) {
  opacity: 1;
}

/* hover 状态下使用 contain */
.role-wrapper:hover .role-image {
  object-fit: contain;
}

/* 右下方信息面板 */
.role-info-panel {
  position: absolute;
  width: clamp(200px, 28vh, 500px);
  opacity: 0;
  transition: all 0.3s ease;
  z-index: 3;
  pointer-events: none;
  /* 默认位置 */
  top: calc(100% + 5px);
  left: 90%;
  transform: translateX(-50%) translateY(10px);
}

.role-info-panel.active {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.role-text {
  font-size: 24px;
  color: #000000;
  font-weight: bold;
  margin-bottom: 8px;
}

.role-description {
  font-size: 18px;
  color: #000000;
  line-height: 1.5;
}

</style>
