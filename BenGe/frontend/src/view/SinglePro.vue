<template>
  <div class="home-container">

    <div class="top-bar">
      <div class="logo-area">
        <div class="logo">
          <span>本格视界</span>
          <svg width="40" height="10" viewBox="0 0 40 10" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 5 C0 7.76142 2.23858 10 5 10 H35 C37.7614 10 40 7.76142 40 5 V5 C40 2.23858 37.7614 0 35 0 H5 C2.23858 0 0 2.23858 0 5 Z" fill="#1A1A1A"/>
          </svg>
        </div>
      </div>
      <div class="title">BenGe.vision</div>

      <div class="nav-actions">
        <div class="user-avatar" @click="toggleUserMenu" ref="userAvatarRef">
          <div class="avatar-text">{{ username?.charAt(0) }}</div>
          <div class="avatar-ripple" v-if="rippleShow"></div>
          <div class="avatar-pulse" v-if="showUserMenu"></div>
        </div>
      </div>

      <!-- 用户菜单 -->
      <transition name="curtain">
        <div v-if="showUserMenu" class="user-menu-container">
          <div class="user-menu">
            <div class="menu-header">
              <div class="menu-avatar">{{ username?.charAt(0) }}</div>
              <div class="menu-user-info">
                <div class="menu-username">{{ username }}</div>
              </div>
            </div>
            <div class="menu-divider"></div>
            <div class="menu-item" @click="toggleScriptList">
              <img src="../assets/user-menu-script.png" alt="我的剧本" class="menu-icon-img">
              <span>我的剧本</span>
            </div>
            <div class="menu-item" @click="logout">
              <img src='../assets/user-menu-quit.png' alt="回到首页" class="menu-icon-img">
              <span>回到首页</span>
            </div>
          </div>
          <div class="curtain-title">
            <span>BenGe.vision</span>
          </div>
        </div>
      </transition>

    </div>

    <div class="main-content">
      <!-- 左侧剧本列表 - 可收缩 -->
      <div class="scripts-list-section" :class="{ 'show-list': showScriptList }">
        <transition name="fade">
          <ScriptsList
            v-if="showScriptList"
          />
        </transition>

        <transition name="fade">
          <div class="collapsed-title" v-if="!showScriptList">
            <span @click="toggleScriptList">我的剧本</span>
          </div>
        </transition>
        
        <div
          class="toggle-button"
          @click="toggleScriptList"
          v-show="showScriptList"
        >
          <span class="toggle-icon">◀</span>
        </div>
      </div>
      
      <!-- 右侧工作区 -->
      <div class="workspace-container" :class="{ 'full-width': !showScriptList }">
        <!-- 剧本创作工作区 -->
        <ScriptWorkspace
          :userInfo = "username"
        />

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref , onMounted , onUnmounted } from 'vue'
import { useRouter } from 'vue-router';
import { userLoadingStore } from '@/stores/userLoadingStore';
import { usescriptStore } from '@/stores/scriptStore';
import ScriptsList from '@/components/Single/ScriptsList.vue';
import ScriptWorkspace from '@/components/Single/ScriptWorkspace.vue';

const router = useRouter();

// 子组件的控制显示的变量
const showUserMenu = ref(false);  // 用户菜单是否显示
const showScriptList = ref(false);  // 脚本列表是否显示


// 渲染数据的获取
const loadingStore = userLoadingStore();
const scriptStore = usescriptStore();
const username = ref('');


// 用户菜单相关部分
const rippleShow = ref(false);  // 涟漪效果是否显示
const userAvatarRef = ref(null);

const toggleRepple = () => {
  rippleShow.value = !rippleShow.value;
  // 涟漪展示一次
  setTimeout(() => {
    rippleShow.value = false;
  }, 800);
}

const handleOutsideClick = (e) => {
  if(showUserMenu.value){
    if(userAvatarRef.value && userAvatarRef.value.contains(e.target)){
      return;
    }

    const menuElement = document.querySelector('.user-menu');
    const menuContainerElement = document.querySelector('.user-menu-container');

    if(menuContainerElement && menuContainerElement.contains(e.target)){
      if(menuElement && !menuElement.contains(e.target)){
        showUserMenu.value = false;
      }
    }
  }
}
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
  toggleRepple();
}

const logout = () => {
  router.push('/home');
}
 
// 页面加载逻辑
onMounted(() => {
  const storeUsername = localStorage.getItem('username');
  if(storeUsername){
    username.value = storeUsername;
  }

  // 如果原来显示加载页面，过一会关闭
  if(loadingStore.loading){
    setTimeout(() => {
      loadingStore.hide();
    }, 1000)
  }

  scriptStore.loadScripts();

  // 控制AI助手的显示
  loadingStore.showAiAssistantLoading();

  // 添加点击外部关闭菜单的事件监听
  document.addEventListener('click', handleOutsideClick);
  
})

// 页面卸载逻辑
onUnmounted(() => {
  // 移除事件监听
  document.removeEventListener('click', handleOutsideClick);
  loadingStore.hideAiAssistantLoading();
})

const toggleScriptList = () => {
  showScriptList.value = !showScriptList.value;
  showUserMenu.value = false;
}

</script>

<style scoped>
/* 页面容器 */
.home-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-image: url("../assets/loginback.jpg");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  color: #333;
}

/* 顶部栏样式 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
  border-radius: 0 0 12px 12px;
}
.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}
.title {
  font-size: 24px;
  font-weight: bold;
  font-style: italic;
  letter-spacing: 1px;
}
.nav-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}
.user-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #1a1a1a;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.3s;
  /* 11 是为了后续可以通过点击用户来关闭面板 */
  z-index: 11;
  /* hidden 是为了后续的涟漪效果只会在圆圈内部显示 */
  overflow: hidden;
}
.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(179, 178, 199, 0.8);
}
.avatar-text {
  font-size: 16px;
  text-transform: uppercase;
  z-index: 2;
}
.avatar-ripple {
  position: absolute;
  /* 设置在父元素内居中 */
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  /* 刚开始是看不到的 */
  width: 0;
  height: 0;
  border-radius: 50%;
  background-color: rgba(179, 178, 199, 0.2);
  z-index: 1;
  /* 设置无限循环是为了可以在任何时刻显示涟漪效果 */
  animation: ripple 2s infinite;
}
.avatar-pulse{
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(179, 178, 199, 0.2);
  border-radius: 50%;
  z-index: 1;
  animation: pulse-animation 2s infinite;
}
@keyframes ripple {
  0% {
    width: 0;
    height: 0;
    opacity: 0.8;
  }
  100% {
    width: 160%;
    height: 160%;
    opacity: 0;
  }
}
@keyframes pulse-animation {
  0% {
    box-shadow: 0 0 0 0 rgba(179, 178, 199, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(179, 178, 199, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(179, 178, 199, 0);
  }
}
/* 覆盖在页面上面的灰色区域，但是z-index设置成10，为了可以点击用户来关闭 */
.user-menu-container{
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: flex-end;
  z-index: 10;
}
.user-menu {
  position: absolute;
  top: 60px;
  right: 25px;
  width: 200px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  z-index: 100;
  color: #333;
  transform-origin: top right;
}
.menu-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  background-color: rgba(26, 26, 26, 0.05);
}
.menu-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #1a1a1a;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  margin-right: 12px;
  font-size: 16px;
}

.menu-user-info {
  flex: 1;
}

.menu-username {
  font-weight: 500;
  font-size: 15px;
}

.menu-divider {
  height: 1px;
  background-color: rgba(0, 0, 0, 0.1);
  margin: 0;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  font-size: 14px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.menu-item:hover {
  background-color: rgba(179, 178, 199, 0.3);
}

.menu-icon-img {
  margin-right: 50px;
  width: 20px;
  height: 20px;
  object-fit: contain;
  vertical-align: middle;
}
.curtain-title{
  font-size: 100px;
  font-weight: bold;
  font-style: italic;
  position: absolute;
  letter-spacing: 10px;
  color: #fff;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
/* 窗帘下拉动画 */
.curtain-enter-active,
.curtain-leave-active {
  transition: opacity 0.3s;
}

.curtain-enter-from,
.curtain-leave-to {
  opacity: 0;
}

.curtain-enter-active .user-menu,
.curtain-leave-active .user-menu {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.3s;
}

.curtain-enter-from .user-menu,
.curtain-leave-to .user-menu {
  transform: translateY(-20px) scale(0.95);
  opacity: 0;
}

/* 主内容区域 */
.main-content {
  display: flex;
  flex: 1;
  padding: 20px;
  gap: 20px;
  position: relative;
  height: calc(100vh - 70px); /* 减去顶部栏的高度 */
  overflow: hidden;
}

/* 剧本列表区域 */
.scripts-list-section {
  width: 0;
  min-width: 30px;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  /* 添加过渡动画 */
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.scripts-list-section.show-list {
  width: 320px;
}

/* 收缩状态下的竖排标题 */
.collapsed-title {
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px 0;
  position: relative;
  overflow: hidden;
}

.collapsed-title span {
  writing-mode: vertical-lr;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  letter-spacing: 4px;
  white-space: nowrap;
  text-align: center;
  padding: 20px 0;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.collapsed-title span:hover {
  color: #1a1a1a;
  letter-spacing: 6px;
  text-shadow: 0 0 5px rgba(179, 178, 199, 0.5);
}

.collapsed-title span::after {
  content: "";
  position: absolute;
  bottom: 10px;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #1a1a1a;
  transform: translateX(-50%);
  transition: width 0.3s ease;
}

.collapsed-title span:hover::after {
  width: 70%;
}

/* 透明度过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

/* 切换按钮样式 */
.toggle-button {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  background-color: rgba(26, 26, 26, 0.9);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
}

/* 添加这个规则，列表收起时调整按钮位置 */
.scripts-list-section:not(.show-list) .toggle-button {
  right: 12px; /* 当列表收起时，按钮向右移动，确保可见 */
}

.toggle-button:hover {
  background-color: #3A384D;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.toggle-icon {
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 工作区容器 */
.workspace-container {
  flex: 1;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

.workspace-content {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.workspace-header {
  margin-bottom: 20px;
}

.workspace-header h1 {
  font-size: 24px;
  margin: 0;
  color: #333;
}
</style>