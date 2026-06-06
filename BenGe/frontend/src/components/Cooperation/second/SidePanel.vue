<template>
  <div class="right-area">
    <!-- 成员区 -->
     <div style="background: transparent;padding: 20px 20px 0px 20px;height: 30%;">
    <div class="member-area" :style="{ background: backgroundColor, color: fontColor }"
:class="{ collapsed: !isMemberOpen }">
      <div class="member-header">
        <h2 style="min-width: 72px">成员区</h2>
      </div>
      <transition name="collapse">
        <div v-if="isMemberOpen" class="member-list">
          <div
            class="member-item"
            v-for="(member,index) in socketState.members"
            :key="index"
            @click="$emit('clickMember', {member,index})"
          >
            <img :src="member.avatar" alt="avatar" class="member-avatar" />
            <div class="member-info">
              <span class="member-name" :style="{ color: fontColor }">{{ member.name }}</span>
            </div>
          </div>
        </div>
      </transition>
    </div>
    </div>

    <!-- 聊天区 -->
    <div class="chat-area" :style="{ height: isMemberOpen ? '80%' : '95%' }">
      <div class="chat-wrapper">
        <Chat
          :roomId="roomId"
          :userId="userId"
          :avatar="avatar"
          @membersUpdated="$emit('membersUpdated', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import Chat from '../Chat.vue';
import { socketState } from '@/stores/socket';

// Props 直接通过 `defineProps` 获取
const props = defineProps({
  fontColor: {
    type: String,
    default: 'white',
  },
  backgroundColor: {
    type: String,
    default: 'black',
  },
  members: {
    type: Array,
    required: true,
  },
  userId: {
    type: String,
    required: true,
  },
  avatar: {
    type: String,
    required: true,
  },
  roomId: {
    type: [Number, String],
    required: true,
  },
  isMemberOpen: {
    type: Boolean,
    default: true,
  },
});

// 响应式变量
const isMemberListVisible = ref(props.isMemberOpen);

// 计算属性
const fontColor = computed(() => props.fontColor);
const backgroundColor = computed(() => props.backgroundColor);
const members = computed(() => props.members);
const userId = computed(() => props.userId);
const avatar = computed(() => props.avatar);
const roomId = computed(() => props.roomId);

// 切换成员列表显示
const toggleMemberListVisibility = () => {
  isMemberListVisible.value = !isMemberListVisible.value;
};
</script>


<style scoped>
.right-area{
    display: flex;
    flex-direction: column;
    width: 100%;

    justify-content: space-between;  /* 上下分布元素 */
}
.member-info {
  display: flex;
  align-items: flex-start;
  height: 100%;
  margin-left: 10px;
}

.chat-wrapper {
  width: 100%;
  height: 100%;
  margin-left: auto;
  margin-right: auto;
  position: relative;
  overflow: hidden;
}
/* 折叠/展开动画 */
.collapse-enter-active,
.collapse-leave-active {
  transition: max-height 0.3s ease, opacity 0.3s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
}

.collapse-enter-to,
.collapse-leave-from {
  max-height: 500px; /* 根据实际内容高度设大一点 */
  opacity: 1;
}

.member-header {
  display: flex;
  height: 20px;

  align-items: center;
}
.toggle-btn {
  height: 30px;
  width: 30px;

  border: none;
  background: transparent;
  color: white;
}
.member-area {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;

  color: white;

  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;

  /* ❌ 移除固定高度 */
  height: 100%;

  border-radius: 8px;
  padding: 20px;
}

.member-list {
  display: flex;
  flex-direction: column;
  margin-top: 10px;
  width: 100%;
  max-width: 100%;
  gap: 24px;

  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 4px;
}

.member-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #ccc;
}

.member-name {
  font-size: 12px;
  color: white;

  width: 54px;
  overflow: hidden; /* 隐藏溢出的内容 */
  text-overflow: ellipsis; /* 超出部分显示省略号 */
  white-space: nowrap; /* 不换行 */
}

.chat-area {
  height: 68%;
  max-height: 500px;
  display: flex;
  flex-direction: column;

  background: transparent;
  color: hsl(from color h s l);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}
</style>
