<template>
  <div class="cooperation-container">
    <div class="cooperation-bg"></div>
    <div class="room-list-container">
      <div class="room-back-house"></div>
      <!-- 页面标题 -->
      <div class="page-title">
        <i class="fa-solid fa-house"></i>
        <h1>房间列表</h1>
        <h1>房间列表</h1>
      </div>

      <!-- 创建房间区域 -->
      <div v-if="showCreatePage" class="create-room-card room-section">
        <div class="header">
          <div class="header-left">
            <i class="fas fa-home icon"></i>
          </div>
          <div class="header-right">
            <h2 class="title">创建新房间</h2>
            <p class="subtitle">请输入基本信息</p>
          </div>
        </div>

        <div class="form-group">
          <i class="fas fa-pencil-alt icon"></i>
          <input id="room-name" type="text" v-model="newRoom.name" placeholder="Enter room name" />
        </div>

        <div class="form-group">
          <i class="fas fa-list icon"></i>
          <input id="room-description" v-model="newRoom.description" placeholder="可选：房间描述" rows="2" />
        </div>

        <div class="password-toggle-wrapper">
          <!-- 左侧锁和文字 -->
          <label for="pwd-check" class="password-toggle-label">
            <i class="fas fa-lock icon"></i>
            <span>设置密码</span>
          </label>

          <!-- 复选框控制展开 -->
          <input id="pwd-check" type="checkbox" v-model="newRoom.havePwd" class="hidden-checkbox" />

          <!-- 密码输入框：右侧展开 -->
          <transition name="expand-slide">
            <input v-if="newRoom.havePwd" type="password" class="password-slide-input" v-model="newRoom.password"
              placeholder="请输入密码" />
          </transition>
        </div>


        <!-- 创建和取消按钮 -->
        <div class="button-group">
          <button class="cancel-btn" @click="showCreatePage = false">
            <i class="fas fa-times"></i> 取消
            <i class="fas fa-times"></i> 取消
          </button>
          <button class="create-btn" @click="submitRoom">
            <i class="fas fa-plus"></i> 创建
            <i class="fas fa-plus"></i> 创建
          </button>
        </div>
      </div>

      <div v-else class="join-room room-section">
        <!-- 顶部搜索和创建 -->
        <div class="custom-header-bar">
          <div class="custom-input-wrapper">
            <i class="fa fa-search search-icon"></i>
            <input type="text" v-model="searchKeyword" class="custom-input" placeholder="搜索房间..." />
            <button v-if="searchKeyword" class="clear-btn" @click="searchKeyword = ''">
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>

          <button class="custom-create-btn" @click="create">
            <i class="fa fa-plus"></i>
            <span>创建房间</span>
          </button>
        </div>

        <!-- 房间卡片轮播 -->
        <div class="room-carousel" v-loading="loading">
          <div v-if="!loading && filteredRooms.length === 0" class="empty-state">
            <el-empty description="暂无房间数据">
              <el-button type="primary" @click="create">
                <el-icon>
                  <Plus />
                </el-icon>
                创建第一个房间
              </el-button>
            </el-empty>
          </div>
          <div v-else class="room-card-wrapper" :style="{ transform: wrapperTransform }">
            <div v-for="room in filteredRooms" :key="room.roomId" class="room-card" :class="{
              'password-room': room.havePwd,
              'no-password-room': !room.havePwd
            }" @click="handleCardClick(room)">
              <!-- 无密码房间卡片结构 -->
              <template v-if="!room.havePwd">
                <i class="fa-solid fa-circle-check top-right-icon"></i>
                <div class="room-header no-pwd-header">
                  <div class="room-icon-circle">
                    <i class="fa-solid fa-house-signal"></i>
                  </div>
                  <div class="room-basic-info">
                    <h3>{{ room.name }}</h3>
                    <p class="room-desc">{{ truncateDescription(room.description) }}</p>
                    <p class="room-members">
                      <el-icon>
                        <User />
                      </el-icon>
                      {{ room.currentMembers }} 人
                    </p>
                  </div>
                </div>
              </template>

              <!-- 有密码房间卡片结构 -->
              <template v-else>
                <div class="room-header with-pwd-header">
                  <div class="room-icon-circle">
                    <i class="fa-solid fa-key"></i>
                  </div>
                  <div class="room-basic-info">
                    <h3>{{ room.name }}</h3>
                    <p class="room-desc">{{ truncateDescription(room.description) }}</p>
                  </div>
                </div>
                <div class="room-footer">
                  <button class="enter-btn" :disabled="joiningRoomId !== null" @click.stop="enterRoom(room)">
                    <span v-if="joiningRoomId === room.roomId">加入中...</span>
                    <span v-else>加入房间</span>
                  </button>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部导航 -->
      <transition name="fade-slide">
        <div v-if="!showCreatePage" class="bottom-nav">
          <div class="nav-left">

            <!-- 当前房间状态 -->
            <div class="current-room-info-native">
              <div class="info-banner">
                <div class="info-icon">
                  <i class="fa-solid fa-circle-info"></i>
                </div>
                <div v-if="currentRoom && currentRoom.name" class="info-text">
                  <div class="info-title">当前房间: {{ currentRoom.name }}</div>
                  <div class="room-actions">
                    <button class="leave-button" @click.stop="enterRoom({ ...currentRoom, roomId: currentRoom.id })">
                      回到房间
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="nav-right">
            <!-- 刷新按钮 -->
            <i class="fa-solid fa-rotate-right refresh-icon" @click="fetchRooms"></i>

            <!-- 当前索引 -->
            <span>{{ filteredRooms.length > 0 ? currentIndex + 1 : 0 }} / {{ filteredRooms.length }}</span>

            <!-- 左箭头 -->
            <i class="fa-solid fa-arrow-left nav-arrow" :class="{ disabled: currentIndex === 0 }" @click="prevRoom"></i>

            <!-- 右箭头 -->
            <i class="fa-solid fa-arrow-right nav-arrow"
              :class="{ disabled: currentIndex === filteredRooms.length - 1 }" @click="nextRoom"></i>
          </div>
        </div>
      </transition>
    </div>
    <BackRoomDialog :visible="showDialog" v-bind="dialogOptions" @confirm="handleDialogConfirm"
      @cancel="handleDialogCancel" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useRouter } from 'vue-router';
import { ElMessage } from "element-plus";
import { User, Plus } from '@element-plus/icons-vue';
import { createRoom, getRoomList, joinRoom, getCurrentRoom, leaveRoom, getOwnedRooms, enterOwnedRoom } from "../api/room";
import { socketState, setupWebSocket } from "@/stores/socket";
import BackRoomDialog from "@/components/Cooperation/BackRoomDialog.vue";

const router = useRouter();

const showCreatePage = ref(false);
const newRoom = ref({
  name: "",
  description: "",
  havePwd: false,
  password: "",
});

const searchKeyword = ref("");
const debouncedSearchKeyword = ref("");
const roomList = ref([]);
const loading = ref(false);
const joiningRoomId = ref(null);
const currentRoom = ref(null);
const ownedRooms = ref([]);
const currentIndex = ref(0); // 当前显示的房间索引
const wrapperWidth = ref(0);
const cardGapPx = 16; // 卡片右间距
// 控制回到房间的弹窗
const showDialog = ref(false)
const dialogOptions = ref({
  title: '',
  message: '',
  showInput: false,
  inputPlaceholder: '',
  inputType: 'text',
})
const dialogResolve = ref(null)
const dialogReject = ref(null)


onMounted(() => {
  const wrapper = document.querySelector('.room-carousel')
  if (wrapper) {
    wrapperWidth.value = wrapper.offsetWidth
  }
})

function truncateDescription(desc) {
  if (!desc) return '';
  return desc.length > 10 ? desc.slice(0, 10) + '...' : desc;
}

const filteredRooms = computed(() => {
  return roomList.value.filter((room) =>
    room.name.includes(debouncedSearchKeyword.value)
  );
});

async function fetchCurrentRoom() {
  try {
    const res = await getCurrentRoom();
    currentRoom.value = res;
  } catch (err) {
    currentRoom.value = null;
  }
}

async function fetchOwnedRooms() {
  try {
    const res = await getOwnedRooms();
    ownedRooms.value = Array.isArray(res) ? res : [];
  } catch (err) {
    ownedRooms.value = [];
  }
}

async function fetchRooms() {
  loading.value = true;
  try {
    const [roomListRes] = await Promise.all([
      getRoomList(1, 100),
      fetchCurrentRoom(),
      fetchOwnedRooms()
    ]);
    if (Array.isArray(roomListRes)) {
      roomList.value = roomListRes;
      const mineIndex = roomList.value.findIndex(room => room.name === 'Mine');
      if (mineIndex !== -1) {
        currentIndex.value = mineIndex;
      } else {
        currentIndex.value = 0;
      }
    } else {
      roomList.value = [];
      ElMessage.error('获取房间列表失败：响应格式不正确');
    }
  } catch (err) {
    ElMessage.error('获取房间列表失败：' + (err.response?.data || err.message));
    roomList.value = [];
  } finally {
    loading.value = false;
  }
}

const cardWidthPx = computed(() => {
  return (wrapperWidth.value - cardGapPx * 3) / 4
})

const wrapperTransform = computed(() => {
  const slideWidth = cardWidthPx.value + cardGapPx
  return `translateX(-${currentIndex.value * slideWidth}px)`
})

function handleCardClick(room) {
  enterRoom(room);
}

function openDialog(options) {
  dialogOptions.value = options
  showDialog.value = true
  return new Promise((resolve, reject) => {
    dialogResolve.value = resolve
    dialogReject.value = reject
  })
}

function handleDialogConfirm(val) {
  showDialog.value = false
  dialogResolve.value(val)
}

function handleDialogCancel() {
  showDialog.value = false
  dialogReject.value('cancel')
}

function nextRoom() {
  const maxVisible = Math.max(0, filteredRooms.value.length - 4)
  if (currentIndex.value < maxVisible) currentIndex.value++
}

function prevRoom() {
  if (currentIndex.value > 0) currentIndex.value--
}

function create() {
  showCreatePage.value = true;
}

async function enterRoom(room) {
  if (!room.roomId) {
    ElMessage.warning('RoomId 不存在，无法进入房间');
    return;
  }

  socketState.roomId = room.roomId;
  socketState.avatar = require(`@/assets/avatar/${Math.floor(Math.random() * 5 + 1)}.jpg`);
  setupWebSocket();

  if (joiningRoomId.value !== null) return;
  joiningRoomId.value = room.roomId;

  if (currentRoom.value && currentRoom.value.id !== room.roomId) {
    try {
      await openDialog({
        title: '切换房间',
        message: `您当前在房间 "${currentRoom.value.name}" 中，是否要退出并加入 "${room.name}"？`,
        showInput: false,
      });
      await leaveRoom(currentRoom.value.id);
      ElMessage.success('已退出当前房间');
      currentRoom.value = null;
      await fetchCurrentRoom();
    } catch (error) {
      if (error === 'cancel') {
        joiningRoomId.value = null;
        return;
      }
      ElMessage.error('切换房间失败：' + (error.message || '未知错误'));
      return;
    }
  }

  const tryJoinRoom = async (password = '') => {
    try {
      const result = await joinRoom(room.roomId, password);
      if (result === 'success') {
        ElMessage.success('成功加入房间！');
        router.push(`/room/${room.roomId}`);
        await fetchCurrentRoom();
        return true;
      } else if (result === '房间密码错误') {
        return 'needPassword';
      } else if (result === '您已经在房间中') {
        router.push(`/room/${room.roomId}`);
        return true;
      } else {
        ElMessage.error(result || '加入房间失败');
        return false;
      }
    } catch (error) {
      throw error;
    }
  };

  try {
    const result = await tryJoinRoom();
    if (result === 'needPassword') {
      try {
        const password = await openDialog({
          title: '加入房间',
          message: `房间 "${room.name}" 需要密码`,
          showInput: true,
          inputType: 'password',
          inputPlaceholder: '请输入房间密码',
        });
        const retryResult = await tryJoinRoom(password);
        if (retryResult === 'needPassword') {
          ElMessage.error('密码错误，请重试');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('密码输入失败：' + (error.message || '未知错误'));
        }
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('加入房间失败：' + (error.response?.data || error.message));
    }
  } finally {
    joiningRoomId.value = null;
  }
}



async function submitRoom() {
  const { name, description, havePwd, password } = newRoom.value;
  if (!name || name.trim() === "") {
    ElMessage.error("房间名称不能为空！");
    return;
  }
  if (havePwd && (!password || password.trim() === "")) {
    ElMessage.error("请填写房间密码！");
    return;
  }
  const payload = {
    name: name.trim(),
    description: description?.trim() || '',
    havePwd,
    password: havePwd ? password.trim() : ''
  };
  try {
    const res = await createRoom(payload);
    if (res && res.roomId) {
      ElMessage.success('房间创建成功！');
      showCreatePage.value = false;
      newRoom.value = { name: "", description: "", havePwd: false, password: "" };
      await fetchRooms();

      // enterRoom({ ...currentRoom, roomId: currentRoom.id })
      // router.push(`/room/${res.roomId}`);
    } else {
      ElMessage.error('创建失败：响应格式不正确');
    }
  } catch (err) {
    ElMessage.error('请求失败：' + (err.response?.data || err.message));
  }
}

let searchTimer = null;
watch(searchKeyword, (newVal) => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    debouncedSearchKeyword.value = newVal;
    currentIndex.value = 0;
  }, 300);
});

onMounted(() => {
  fetchRooms();
  debouncedSearchKeyword.value = searchKeyword.value;
});
</script>

<style scoped>
.cooperation-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  background-image: url(@/assets/chat-main-bg.png);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 1;
}

.cooperation-bg {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  background-color: transparent;
  backdrop-filter: blur(4px);
}

.room-list-container {
  width: 80%;
  height: 90%;
  margin-top: 40px;
  padding: 20px 20px 0 20px;
  background:
    radial-gradient(circle at top right, hsla(209, 84%, 54%, 0.231) 0%, #459adc4a 30%, hsla(200, 24%, 93%, 0.086) 50%) no-repeat,
    linear-gradient(#F5F6FB 0%, #F6F7F9 35%, #F4F6F5 70%, #F5F6FA 100%);
  border-color: transparent;
  border-radius: 28px;
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.1),
    0 6px 18px rgba(30, 60, 120, 0.3);
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
  overflow: hidden;
  position: relative;
}

.room-back-house {
  position: absolute;
  top: 3%;
  right: 10%;
  width: 30%;
  height: 40%;
  background-image: url(../assets/room_house.png);
  opacity: 0.7;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: -1;
  transform-origin: center;
  transform: rotate(8deg);
}

.page-title {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  height: 60px;
  gap: 18px;
  padding-left: 20px;
}

.page-title i {
  font-size: 34px;
}

.page-title h1 {
  font-size: 36px;
  font-weight: 1000;
  color: #333;
  margin: 0;
}

.create-room-card {
  background: #fff;
  border-radius: 30px;
  padding: 40px 60px;
  width: 60%;
  position: relative;
  top: -7%;
  max-height: fit-content;
  min-height: fit-content;
  min-width: max-content;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.52, 0.51, 0.42, 0.92);
  font-family: "Helvetica Neue", sans-serif;
  color: #333;
  align-items: flex-start;
}

.header {
  text-align: center;
  margin-bottom: 24px;
  display: flex;
  padding-left: 10px;
  padding-top: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  margin-right: 10px;
}

.header .icon {
  color: #2563eb;
  font-size: 28px;
  margin-bottom: 10px;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.header .title {
  font-size: 28px;
  letter-spacing: 2px;
  word-spacing: 10px;
  font-size: 28px;
  font-weight: 800;
  font-family: serif;
  margin: 0;
}

.header .subtitle {
  font-size: 14px;
  color: #888;
  margin-top: 6px;
}

.form-group {
  position: relative;
  margin-bottom: 20px;
  border-radius: 15px;
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.25), 0 2px 4px rgba(64, 158, 255, 0.1);
}

.form-group .icon {
  position: absolute;
  transition: translateY(-50%);
  left: 10px;
  color: #2563eb;
  font-size: 14px;
}

.form-group input {
  border-color: transparent;
  width: 100%;
  padding: 15px 16px 15px 36px;
  font-size: 14px;
  border-radius: 15px;
  transition: border-color 0.2s ease;
  outline: none;
  appearance: none;
  /* 去掉原生外观样式 */
  -webkit-appearance: none;
  -moz-appearance: none;
}

.form-group input:focus {
  border-color: #409eff;
}

.password-toggle-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  height: 48px;
  /* 固定高度防止变形 */
  position: relative;
  overflow: hidden;
  margin-bottom: 20px;
  padding: 0 16px;
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.25), 0 2px 4px rgba(64, 158, 255, 0.1);
}

/* 隐藏 checkbox */
.hidden-checkbox {
  display: none;
}

/* 左侧图标+文字 */
.password-toggle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #2563eb;
  white-space: nowrap;
  cursor: pointer;
}

.password-toggle-label .icon {
  font-size: 14px;
}

.password-slide-input {
  width: 180px;
  height: 95%;
  padding: 12px 16px;
  border-radius: 5px;
  border-left: none;
  border-right: none;
  border-top: none;
  background-color: #ffffff;
  /* 统一白色背景 */
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.25),
    0 2px 10px rgba(64, 158, 255, 0.1);
  /* 同输入框风格 */
  font-size: 14px;
  color: #333;
  outline: none;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

/* Focus 状态保持高亮蓝色边框 */
.password-slide-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}


/* 动画效果 */
.expand-slide-enter-active,
.expand-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.5, 0.2, 0.2, 1);
}

.expand-slide-enter-from,
.expand-slide-leave-to {
  transform: scaleX(0.6);
  opacity: 0;
  transform-origin: left;
}



/* 按钮组（你已有，可保留）再次贴一下供参考 */
.button-group {
  align-self: flex-end;
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;
}

.cancel-btn,
.create-btn {
  border: none;
  border-radius: 20px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: #e5e7eb;
  color: #333;
}

.cancel-btn:hover {
  background: #d1d5db;
  transform: scale(1.05);
}

.create-btn {
  background: #2563eb;
  color: white;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.create-btn:hover {
  transform: scale(1.05);
  background: #1e40af;
}


.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.room-section {
  display: flex;
  flex-direction: column;
}

.join-room {
  width: 100%;
  max-height: 80%;
  align-items: center;
  flex: 1;
}

.custom-header-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 20%;
  padding: 0 20px;
  margin-bottom: 20px;
}

.custom-input-wrapper {
  position: relative;
  flex: 1;
  max-width: 80%;
}

.custom-input {
  width: 100%;
  height: 70px;
  padding: 12px 40px 12px 36px;
  font-size: 16px;
  border-radius: 40px;
  border-color: transparent;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.25), 0 2px 4px rgba(64, 158, 255, 0.1);
  outline: none;
  transition: border-color 0.3s;
}

.custom-input:focus {
  border-color: #409eff;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
  color: #888;
}

.clear-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #666;
  padding: 4px;
  margin-left: -50px;
  z-index: 10;
  position: relative;
  transition: color 0.2s ease;
}

.clear-btn:hover {
  color: #333;
}


.custom-create-btn {
  margin-left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 70px;
  padding: 10px 20px;
  border: none;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 600;
  background-color: #5C7BFE;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.custom-create-btn:hover {
  background-color: #337ecc;
}


.room-carousel {
  width: 90%;
  flex: 1;
  overflow: hidden;
  position: relative;
}

.room-card-wrapper {
  display: flex;
  transition: transform 0.3s ease;
  width: 100%;
  margin-top: 20px;
}

.room-card {
  /* flex: 0 0 calc((99% - 3 * 16px) / 4); */
  width: calc((100% - 3 * 16px) / 4);
  min-height: 250px;
  box-sizing: border-box;
  margin-right: 16px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  flex-shrink: 0;
  position: relative;
  cursor: pointer;
  /* ⭐ 防止被压缩 */
  transform-style: preserve-3d;
  border-radius: 8px 16px 24px 12px;
  /* 左上、右上、右下、左下 */
  transform: perspective(1200px) rotateX(8deg) rotateY(-16deg) rotateZ(4deg);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.room-card:hover {
  transform: perspective(1200px) rotateX(8deg) rotateY(-16deg) rotateZ(4deg) translateY(-4px) scale(1.1);
}

.room-card.password-room {
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F9FF 100%);
  box-shadow:
    0 4px 12px rgba(94, 123, 254, 0.08),
    0 1px 2px rgba(0, 0, 0, 0.05);
}

.room-card.no-password-room {
  background: linear-gradient(0deg, #F0F5FF 0%, #C9D8FF 100%);
  box-shadow:
    0 6px 16px rgba(94, 123, 254, 0.15),
    inset 0 1px 2px rgba(255, 255, 255, 0.8);
}

.top-right-icon {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 18px;
  color: #409eff;
}

.room-header {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
}

.room-icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #F4F6F5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.room-icon-circle i {
  font-size: 25px;
}

.room-basic-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.room-basic-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px;
}

.room-desc {
  font-size: 14px;
  color: #888;
  margin: 0 0 4px;
}

.room-members {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0;
}

.room-footer {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.enter-btn {
  width: 90%;
  height: 40px;
  background-color: #5E7CFF;
  border-radius: 40px;
  color: #fff;
  font-weight: 600;
  border-color: transparent;
}

.bottom-nav {
  flex: 1;
  width: 100%;
  max-height: 100px;
  position: absolute;
  bottom: 0;
  left: 0;
  background-color: #FEFEFE;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  padding-bottom: 0px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  background: transparent;
  color: #606266;
}

.current-room-info-native {
  width: 100%;
  padding: 10px 16px;
  box-sizing: border-box;
  margin-bottom: 15px;
}

.info-banner {
  display: flex;
  align-items: flex-start;
  background-color: transparent;
  border-color: transparent;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 5px 4px#4586c880;
}

.info-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #409eff;
}

.info-text {
  flex: 1;
}

.info-title {
  font-weight: 600;
  font-size: 15px;
  color: #6e7fa0;
  margin-bottom: 8px;
}

.room-actions {
  display: flex;
  gap: 10px;
}

.leave-button {
  background-color: transparent;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  transition: color 0.2s ease;
}

.leave-button:hover {
  color: #6e7fa0;
}

.nav-right {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  /* 加大图标间距 */
  min-width: 240px;
  height: 100%;
  padding-bottom: 15px;
}

/* 所有图标的基础样式 */
.nav-right i {
  cursor: pointer;
  font-size: 22px;
  /* 图标更大 */
  color: #606266;
  width: 40px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  border-radius: 50%;
  background-color: #f2f2f2;
  /* 默认浅灰背景 */
  transition: background-color 0.3s ease, color 0.3s ease, transform 0.3s ease;
}

/* 鼠标悬浮时改变背景色和字体色 */
.nav-right i:hover {
  background-color: #1E1E26;
  /* 悬浮时更深的灰 */
  color: #FDFDFB;
  transform: scale(1.1);
}

/* 禁用状态 */
.nav-right i.disabled {
  background-color: #f5f5f5;
  color: #c0c4cc;
  cursor: not-allowed;
  pointer-events: none;
}

/* 动画过渡效果 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>