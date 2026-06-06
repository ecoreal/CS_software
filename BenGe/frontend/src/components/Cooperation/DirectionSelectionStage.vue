<template>
  <div class="direction-selection-stage">
    <div class="main-content">
      <!-- 左侧区域 -->

      <div class="left-panel">
        <div class="left-panel-header">
          <div style="font-size: 24px; font-weight: bold;margin-left: 50px;">BenGe.Vision</div>
          <button class="confirm-button" @click="$emit('updateStage', 1)">
            下一阶段
          </button>
        </div>
        <DirectionSelect
          v-if="!showVoteStage"
          @confirm="handleDirectionConfirm"
          class="direction-select-component"
        />
        <div v-else-if="AIGenerate"></div>
        <VoteStage
          v-else
          :room-id="roomId"
          :members="members"
          :all-directions="allDirections"
          @submitVote="handleVoteSubmit"
          @regenerateSuggestion="handleRegenerateRequest"
          @next-stage="$emit('updateStage', 1)"
          class="vote-stage-component"
        />
      </div>

      <!-- 右侧区域 -->
      <div class="right-panel">
        <!-- 成员区 -->
        <MemberList members="members" />

        <!-- 聊天区 -->
        <ChatPanel
          :room-id="String(roomId)"
          :user-id="currentUser.id"
          :user-name="currentUser.name"
          :avatar="currentUser.avatar"
          @membersUpdated="updateMembers"
          class="chat-component"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRoute } from "vue-router";
import DirectionSelect from "@/components/Cooperation/first/DirectionSelect.vue";
import VoteStage from "@/components/Cooperation/first/VoteStage.vue";
import ChatPanel from "./first/ChatPanel.vue";
import loginImage from "../../assets/login.png";
import { socketState } from "@/stores/socket";
import MemberList from "@/components/Cooperation/first/MemberList.vue";
import { ElButton } from "element-plus";
import AIGenerateDialog from "./second/roles/AIGenerateDialog.vue";

const route = useRoute();
const isMemberOpen = ref(true);
const showVoteStage = ref(false);
const allDirections = ref([]); // 存储所有成员选择的方向

// 获取房间ID
const roomId = computed(() => {
  return parseInt(route.params.roomId) || 1;
});

// 从localStorage获取用户信息
const currentUser = computed(() => {
  const username = localStorage.getItem("username") || "匿名用户";
  return {
    id: socketState.userId || `user_${username}_${Date.now()}`,
    name: username,
    avatar: loginImage,
  };
});

const toggleMemberArea = () => {
  isMemberOpen.value = !isMemberOpen.value;
};

const updateMembers = (membersList) => {
  if (!Array.isArray(membersList)) return;

  const currentUserId = String(currentUser.value.id);

  // 更新socketState中的成员数据
  socketState.members = membersList
    .filter(
      (member) =>
        member &&
        member.id != null &&
        String(member.id) !== currentUserId &&
        member.roomId === roomId.value
    )
    .map((m) => ({
      id: m.id,
      username: m.name || m.username || "匿名用户",
      avatar: m.avatar || loginImage,
      selectedDirections: m.selectedDirections || [],
    }));
};

const handleDirectionConfirm = (selectedDirections) => {
  // json格式发送到后端并收集所有成员的选择
  showVoteStage.value = true;
};

// 投票
const handleVoteSubmit = (voteData) => {
  // socketState.socket.send(
  //   JSON.stringify({
  //     type: "submit_vote",
  //     roomId: roomId.value,
  //     directions: voteData.directions,
  //   })
  // );
};

const handleRegenerateRequest = (requestData) => {};

// 监听所有方向数据
watch(
  () => socketState.directionDate,
  (newData) => {
    if (newData && newData.roomId === roomId.value) {
      allDirections.value = newData.allDirections;
      showVoteStage.value = true;
    }
  },
  { deep: true }
);

defineExpose({
  roomId,
  currentUser,
  isMemberOpen,
  toggleMemberArea,
  updateMembers,
  handleDirectionConfirm,
  showVoteStage,
  allDirections,
});
</script>

<style scoped>
.left-panel-header {
  width: 100%;
  height: 96px;

  display: flex;
  justify-content: space-between;
}
.direction-selection-stage {
  width: 100%;
  height: 100vh;

  padding: 120px;
  display: flex;
  flex-direction: column;
  background-image: url("@/assets/first/back.png");
  background-size: cover;
  background-repeat: no-repeat;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
  height: 65px;
  padding: 0 0;
  background: transparent;
  border-color: transparent;
  border-radius: 20px;
}

.left-menu {
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom-color: transparent;
}

.stage-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.title-icon {
  font-size: large;
}

.title {
  font-weight: 800;
  font-family: Arial, Helvetica, sans-serif;
  letter-spacing: 2px;
}

.right-menu {
  flex: 1;
  display: flex;
  align-items: center;
  position: relative;
  height: 100%;
}

.back-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 113%;
  background-image: url("../../assets/header-back.png");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  z-index: 1;
  border-bottom-left-radius: 30px;
  border-top-right-radius: 20px;
  border-top-left-radius: 4px;
}

.menu-front {
  width: 100%;
  height: 113%;
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  justify-content: space-between;
  border-bottom-left-radius: 30px;
  border-top-right-radius: 20px;
  border-top-left-radius: 4px;
  background: transparent;
  backdrop-filter: blur(10px);
  align-items: center;
  z-index: 2;
  padding-left: 20px;
}

.logo {
  font-size: 22px;
  font-weight: bold;
  color: white;
}

.menu-group {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;
  gap: 20px;
  margin-right: 10px;
}

.menu-item {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.menu-icon {
  cursor: pointer;
  font-size: 30px;
  background-image: linear-gradient(
    45deg,
    #e6e6ed 0%,
    #c6c3df 30%,
    #b5c4e1 70%,
    #b5bddf 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.avatar {
  padding: 2px;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background-color: #fcfefe;
  z-index: 2;
}

.main-content {
  display: flex;
  flex: 1;
  height: calc(100% - 85px);

  border-radius: 48px;

  /* 亚克力风格核心 */
  background: rgba(255, 255, 255, 0.15); /* 半透明白色 */
  backdrop-filter: blur(12px); /* 毛玻璃模糊效果 */
  -webkit-backdrop-filter: blur(12px); /* Safari 支持 */

  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.2); /* 柔和阴影 */
  border-left: 1px solid rgba(255, 255, 255, 0.2); /* 细边界线 */
}

.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: transparent;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);

  position: relative;
}

.left-panel .stage-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.direction-select-component {
  flex: 1;
  height: 0; /* 让DirectionSelect组件填充剩余空间 */
}

.right-panel {
  width: 300px;
  display: flex;
  flex-direction: column;

  border-radius: 36px;

  background: rgba(255, 255, 255, 0.5);
}

.chat-component {
  flex: 1;
  border-radius: 15px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, max-height 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  max-height: 0;
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  max-height: 200px;
}
.confirm-button {
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
</style>
