<template>
  <div class="main-page">
    <!-- 液态玻璃容器 -->
    <div class="glass-container">

      <!-- 顶部栏 -->
      <div class="top-bar">
        <div class="logo">
          <img src="../assets/logo.png" alt="本格视界" class="logo-img" />
        </div>
        <div class="title">BenGe.vision</div>
        <div class="user">
          <i class="fa-solid fa-door-open quit-icon"></i>
          <div class="quit-login" @click="logout()">退出登录</div>
        </div>
      </div>

      <h1 class="hub-title">创作空间</h1>
      <div class="mode-options">
        <!-- 单人创作卡片 -->
        <div class="mode-card" @click="navigateTo('single')">
          <i class="fa-solid fa-user-pen fa-icon"></i>
          <h3>单人创作</h3>
          <p>独立构建完整剧本</p>
        </div>

        <!-- 团队协作卡片 -->
        <div class="mode-card" @click="navigateTo('cooperate')">
          <i class="fa-solid fa-users fa-icon"></i>
          <h3>团队协作</h3>
          <p>实时协同创作剧本</p>
        </div>

      </div>

    </div>
    <!-- 页面底部背景 -->
    <div class="bottom-bg"></div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { userLoadingStore } from '@/stores/userLoadingStore';
import { onMounted } from 'vue';

const router = useRouter();
const loadingStore = userLoadingStore();


// 退出登录函数
const logout = () => {
  localStorage.removeItem('token');
  router.push('/');
};

// 导航到指定模式的页面
function navigateTo(mode) {
  router.push(`/${mode}`);
}

onMounted(() => {
  if (loadingStore.loading) {
    setTimeout(() => {
      loadingStore.hide();
    }, 1000);
  }
});

</script>

<style scoped>
.main-page {
  position: relative;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: url('../assets/main-bg1.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

/* 液态玻璃居中容器 */
.glass-container {
  border-radius: 20px;
  text-align: center;
  overflow: visible;
  width: 80%;
  height: 80%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  z-index: 3;
  background: rgba(247, 250, 255, 0.5);
  /* 淡蓝白半透明 */
  backdrop-filter: blur(14px);
  border: 1px solid rgba(64, 158, 255, 0.3);
  box-shadow:
    0 8px 36px rgba(64, 158, 255, 0.15),
    0 2px 10px rgba(64, 158, 255, 0.1);
}

/* 顶部栏 */
.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  border-radius: 20px 20px 0 0;
  height: 10%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #333;
  z-index: 3;
  background-color: rgba(255, 255, 255, 0.692);
  border-color: transparent;
  padding: 20px 20px;
}

.logo {
  font-size: 20px;
}

.logo-img {
  height: 36px;
  width: 40px;
  object-fit: contain;
  transform-origin: center;
  transform: scale(3.5);
  position: relative;
  left: 120%;
}

.title {
  font-size: 16px;
  opacity: 0.6;
}

.user {
  display: flex;
  align-items: center;
}

.quit-icon {
  font-size: 24px;
  color: #30393C
}

.quit-login {
  font-size: 12px;
  opacity: 0.6;
  cursor: pointer;
  background: #eff6ff;
  padding: 10px 30px;
  border-radius: 30px;
  color: #1f2937;
}

.quit-login:hover {
  background: #dbeafe;
}

.hub-title {
  font-size: 60px;
  letter-spacing: 5px;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 60px;
  transform: scale(1.5);
}

/* 模式卡片 */
.mode-options {
  display: flex;
  gap: 80px;
  justify-content: center;
  margin-bottom: 20px;
  width: 100%;
}

.mode-card {
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(8px);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.2);
  border: 1px solid rgba(64, 158, 255, 0.25);
  border-radius: 16px;
  padding: 35px 30px;
  width: 25%;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;
}

.mode-card:hover {
  transform: translateY(-6px) scale(1.03);
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.3);
  background: rgba(255, 255, 255, 0.6);
}

.fa-icon {
  font-size: 36px;
  color: #637CFF;
  margin-bottom: 12px;
  transition: transform 0.3s ease, color 0.3s ease;
}

.mode-card h3 {
  font-size: 16px;
  margin-bottom: 6px;
  color: #1f2937;
}

.mode-card p {
  font-size: 12px;
  color: #6b7280;
}

.bottom-bg {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  background: linear-gradient(to bottom,
      rgba(215, 230, 255, 0.4),
      rgba(34, 39, 48, 0.5));
  backdrop-filter: blur(4px);
}

/* 隐藏滚动条但保留滚动功能 */
.recent-scroll::-webkit-scrollbar {
  display: none;
  /* 隐藏 Webkit 浏览器的滚动条 */
}

.recent-scroll {
  -ms-overflow-style: none;
  /* 隐藏 IE/Edge 滚动条 */
  scrollbar-width: none;
  /* 隐藏 Firefox 滚动条 */
}
</style>