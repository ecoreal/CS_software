<template>
  <div class="member-list-container">
    <div class="member-area">
      <div class="member-header">
        <h4>成员列表</h4>
        <button @click="toggleMemberListVisibility" class="toggle-btn">
          <i
            :class="
              isMemberOpen
                ? 'fa-solid fa-chevron-up'
                : 'fa-solid fa-chevron-down'
            "
          ></i>
        </button>
      </div>

      <transition name="collapse">
        <div v-if="isMemberOpen" class="member-list">
          <div
            class="member-item"
            v-for="(member, index) in socketState.members"
            :key="index"
            @click="$emit('clickMember', { member, index })"
          >
            <div style="display: flex;flex-direction: column;justify-content: center;">
              <div class="avatar-wrapper">
                <img :src="member.avatar" alt="avatar" class="member-avatar" />
                <!-- 绿色角标 -->
                <span v-if="member.hasChosen" class="voted-badge">已选好</span>
              </div>
              <span class="member-name">{{ member.username }}</span>
            </div>
            <div class="member-tags">
              <template
                v-if="Array.isArray(member.key) && member.key.length > 0"
              >
                <span class="tag" v-for="(tag, i) in member.key" :key="i">
                  #{{ tag }}
                </span>
              </template>
              <template v-else>
                <span class="tag no-selection">#无选择方向</span>
              </template>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { socketState } from "@/stores/socket";

const props = defineProps({
  members: {
    type: Array,
    required: true,
  },
});

// 响应式变量
const isMemberOpen = ref(true);

// 切换成员列表显示
const toggleMemberListVisibility = () => {
  isMemberOpen.value = !isMemberOpen.value;
  // console.log("成员列表：", socketState.members);
};
</script>

<style scoped>
.member-list-container {
  width: 100%;
  margin-bottom: 20px;

  background: transparent;
}

.member-area {
  background: transparent;
  border-radius: 15px;
  padding: 15px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.member-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.toggle-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  padding: 5px;
}

.member-list {
  display: flex;
  flex-direction: column;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 4px;
  overflow-y: auto;
  padding-right: 5px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.avatar-wrapper {
  position: relative;

  overflow: visible;
  display: flex;

  align-items: flex-end;
  justify-content: center;

  height: 48px;
  width: 64px;
}

.member-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.voted-badge {
  position: absolute;
  top: 0px;
  right: -10px;
  background-color: #4caf50;
  color: white;
  font-size: 10px;
  border-radius: 8px;
  font-weight: bold;
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.3);
  z-index: 1;

  padding: 2px;
}

.member-name {
  font-size: 12px;
  color: #666;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 滚动条样式 */
.member-list::-webkit-scrollbar {
  width: 4px;
}

.member-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.member-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.member-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 折叠/展开动画 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
}

.collapse-enter-to,
.collapse-leave-from {
  max-height: 200px;
  opacity: 1;
}

.member-tags {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag {
  background-color: #dceeff;
  color: #007bff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 6px;
}

.no-selection {
  background-color: #f0f0f0;
  color: #999;
}
</style>
