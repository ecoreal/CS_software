<template>
    <div class="loginPage-container">
        <div ref="loginPageTitleRef" class="loginPage-title-container" :style="titleStyle">
          <span class="loginPage-title">BenGe.vision</span>
        </div>
        <div class="loginPage-background">
            <!-- Logo -->
            <div class="logo" :style="{ '--logo-position': signIn ? '5%' : '85%' }">
                <span>本格视界</span>
                <svg width="40" height="10" viewBox="0 0 40 10" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M0 5 C0 7.76142 2.23858 10 5 10 H35 C37.7614 10 40 7.76142 40 5 V5 C40 2.23858 37.7614 0 35 0 H5 C2.23858 0 0 2.23858 0 5 Z" fill="#1A1A1A"/>
</svg>
            </div>
            
            <!-- 登录表单 -->
            <div class="login-form" :class="signIn ? '' : 'move-right'">
                <transition name="fade" mode="out-in">
                    <div v-if="signIn" key="login">
                        <div class="welcome">Welcome back</div>
                        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
                        <form @submit.prevent="handleLogin">
                            <div class="input-group">
                                <input type="text" v-model="form.username" placeholder="username" required :disabled="loading" />
                                <img class="icon-username" src="../assets/username.png">
                            </div>
                            <div class="input-group">
                                <input type="password" v-model="form.password" placeholder="password" required :disabled="loading" />
                                <img class="icon-password" src="../assets/password.png">
                            </div>
                            <div class="actions">
                                <button type="submit" :disabled="loading">
                                  {{ loading ? '登录中...' : 'Sign in' }}
                                </button>
                                <span class="toggleSpan" @click="toggleSignInMode" :class="{ 'disabled': loading }">sign up</span>
                            </div>
                        </form>
                    </div>
                    <div v-else key="register">
                        <div class="welcome">Register now</div>
                        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
                        <form @submit.prevent="handleRegister">
                            <div class="input-group">
                                <input type="text" v-model="form.username" placeholder="username" required :disabled="loading" />
                                <img class="icon-username" src="../assets/username.png">
                            </div>
                            <div class="input-group">
                                <input type="password" v-model="form.password" placeholder="password" required :disabled="loading" />
                                <img class="icon-password" src="../assets/password.png">
                            </div>
                            <div class="actions">
                                <button type="submit" :disabled="loading">
                                  {{ loading ? '注册中...' : 'Register' }}
                                </button>
                                <span class="toggleSpan" @click="toggleSignInMode" :class="{ 'disabled': loading }">sign in</span>
                            </div>
                        </form>
                    </div>
                </transition>
            </div>

            <!-- 右侧六边形背景图 -->
            <div class="login-image" :class="signIn ? '' : 'move-left'">
                <img src = "../assets/login.png" alt="Login Image" />
                <div class="description">
                    <span>{{ showText }}</span>
                    <span>|</span>
                </div>
            </div>
        </div>
    </div>
  </template>
  
  <script setup>
  import { reactive, ref, onMounted, onUnmounted, computed } from 'vue';
  import { login, register } from '../api/user';
  import { useRouter } from 'vue-router';
  import { userLoadingStore } from '@/stores/userLoadingStore';
  
  const router = useRouter();

  // 标题倾斜模块
  const loginPageTitleRef = ref(null);
  const mousePosition = ref({x:0,y:0});
  const elementPosition = ref(null);

  // 获取元素的位置
  onMounted(() => {
    if(loginPageTitleRef.value){
      const rect = loginPageTitleRef.value.getBoundingClientRect();
      elementPosition.value = {
        currentX: rect.left + rect.width / 2,
        currentY: rect.top + rect.height / 2
      }
    }
  })

  // 监听鼠标移动
  const handleMouseMove = (e) => {
    mousePosition.value = {
      x: e.clientX,
      y: e.clientY
    }
  }

  // 计算倾斜角度
  const titleStyle = computed(() => {
    if(!elementPosition.value) return '';
    const  offsetX = (mousePosition.value.x - elementPosition.value.currentX) / 100;
    const  offsetY = (mousePosition.value.y - elementPosition.value.currentY) / 100;

    return `transform: perspective(500px) rotateY(${offsetX}deg) rotateX(${offsetY}deg);`
  })

  onMounted(() =>{
    window.addEventListener('mousemove', handleMouseMove);
  })

  onUnmounted(() => {
    window.removeEventListener('mousemove', handleMouseMove);
  })
  
  // 登录和注册模块
  const form = reactive({
    username: '',
    password: ''
  });
  
  // 加载和错误提示状态
  const loading = ref(false);
  const signIn = ref(true);
  const errorMessage = ref('');
  const loadingStore = userLoadingStore();
  
  const handleLogin = async () => {
    loadingStore.show();

    setTimeout(async () =>{
      loading.value = true;
      errorMessage.value = '';
    
    try {
      const response = await login(form.username, form.password);

      // 如果登录成功，后端返回status为200的Ok结果，带有token数据
      // console.log('登录响应:', response); // 添加日志，查看具体响应

      if (response && response.token) {
        // 保存token到localStorage
        localStorage.setItem('token', response.token);
        localStorage.setItem('username', response.username);
        // 跳转到首页或其他页面
        router.push('/home');
        loadingStore.hide();
      } else {
        loadingStore.hide();
        errorMessage.value = '登录失败，请检查用户名和密码';
      }
    } catch (error) {
      setTimeout(() => {
        loadingStore.hide();
      }, 1000)

      // console.error('登录出错:', error);

      // 如果有详细错误信息，显示它
      if (error.response && error.response.data) {
        errorMessage.value = error.response.data;
      } else if (typeof error === 'string') {
        errorMessage.value = error;
      } else {
        errorMessage.value = '登录失败，请稍后重试';
      }
    } finally {
      loading.value = false;
    }
    }, 1000);
    
  };

  const handleRegister = async () => {
    loading.value = true;
    errorMessage.value = '';

    try {
      const response = await register(form.username, form.password);

      // 后端注册成功时返回字符串 "注册成功"
      if (response === "注册成功") {
        // 注册成功，切换到登录状态
        signIn.value = true;
        // 清空错误信息
        errorMessage.value = '';
        form.username = '';
        form.password = '';
        // 可以显示成功消息
        // console.log('注册成功，请登录');
      } else {
        errorMessage.value = response || '注册失败，请稍后重试';
      }
    } catch (error) {
      // console.error('注册出错:', error);

      // 处理错误响应
      if (error.response && error.response.data) {
        errorMessage.value = error.response.data;
      } else if (typeof error === 'string') {
        errorMessage.value = error;
      } else {
        errorMessage.value = '注册失败，请稍后重试';
      }
    } finally {
      loading.value = false;
    }
  };

  const toggleSignInMode = () => {
    signIn.value = !signIn.value;
    // 切换模式时清空错误信息
    errorMessage.value = '';
    form.username = '';
    form.password = '';
  };

  // 页面效果模块
  //制作一个打字机效果
  let typeText = "A cat is sleeping on a snowball"
  let index = 0
  let typeTimerout = null
  const showText = ref("")

  const typing = () => {
    if(index == typeText.length){
        clearTimeout(typeTimerout)
        typeTimerout = setTimeout(() => {
            showText.value = ""
            index = 0;
            typing()
        }, 2000)
    }else{
        typeTimerout = setTimeout(() => {
            showText.value += typeText[index++];
            typing()
        }, 450)
    }
  }

  onMounted(() => {
    typing()
  })

  onUnmounted(() => {
    clearTimeout(typeTimerout)
  })

</script>
  
  <style scoped>
  .loginPage-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-image: url("../assets/loginback.jpg");
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center;
    min-width: 800px;
    min-height: 600px;
  }

  .loginPage-title-container {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 100px;
    will-change: transform;
  }

  .loginPage-title {
    font-size: 100px;
    font-style: italic;
    letter-spacing: 5px;
    color: #fff;
    font-weight: bold;
  }



  .loginPage-background {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255,255,255,0.9);
    width: 60%;
    height: 55%;
    min-height: max-content;
    min-width: max-content;
    border-radius: 20px;
    box-shadow: rgba(240, 237, 237, 0.1) 0px 4px 16px;
    position: relative;
    overflow: hidden;
  }
  
  .login-form {
    width: 50%;
    padding: 40px;
    background-color: rgba(255,255,255,0.3);
    border-radius: 8px;
    text-align: center;
    position: absolute;
    transition: transform 0.6s cubic-bezier(0.68, -0.55, 0.27, 1.55);
    left: 0;
    z-index: 2;
    backdrop-filter: blur(10px);
  }

  /* 控制表单的左右移动 */
  .move-left {
    transform: translateX(-100%);
  }

  .move-right {
    transform: translateX(100%);
  }
  
  .logo {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    position: absolute;
    gap: 5px;
    z-index: 10;
    top: 5%;
    left: var(--logo-position, 5%);
    transition: all 0.6s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  }
  
  .welcome {
    font-size: 24px;
    margin-bottom: 30px;
    transition: opacity 0.3s ease;
  }
  
  .input-group {
    position: relative;
    margin-bottom: 20px;
    transition: opacity 0.3s ease;
  }
  
  .input-group input {
    width: 100%;
    padding: 10px 10px 10px 30px;
    border: 1.5px solid #ccc;
    border-radius: 25px;
    font-size: 14px;
    transition: border 0.2s ease-in-out;
  }

  .input-group input:focus {
    outline: none;
    border: 1.5px solid #B3B2C7;
  }
  
  .input-group .icon-username,
  .input-group .icon-password {
    position: absolute;
    top: 50%;
    left: 12px;
    transform: translateY(-50%);
    pointer-events: none;
    width: 12px;
    height: 12px;
  }
  
  .actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    transition: opacity 0.3s ease;
  }
  
  .actions button {
    padding: 10px 20px;
    border: none;
    border-radius: 25px;
    background-color: #1a1a1a;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }

  .actions button:hover{
    background-color: transparent;
    color: #fff;
  }

  .actions button::before{
    content:'';
    position:absolute;
    border-radius: 25px;
    top:0;
    left: 0;
    width: 0;
    height:100%;
    background-color: rgb(179, 178, 199);
    transition: width 0.3s ease-in-out;
    z-index:-1;
  }

  .actions button:hover::before{
    width: 100%;
  }
  
  .actions .toggleSpan {
    font-size: 12px;
    color: #1a1a1a;
    text-decoration: none;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .actions .toggleSpan:hover{
    color: rgb(179, 178, 199);
    transform: scale(1.1);
  }
  
  .register {
    font-size: 12px;
    color: #666;
  }
  
  .register .social-icon {
    display: inline-block;
    width: 24px;
    height: 24px;
    margin-left: 10px;
    background-color: #ccc;
    border-radius: 50%;
    text-align: center;
    line-height: 24px;
    color: #fff;
    text-decoration: none;
  }
  
  .login-image {
    width: 50%;
    padding: 40px;
    padding-top: 20px;
    padding-bottom: 20px;
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: rgb(179, 178, 199,0.8);
    position: absolute;
    right: 20px;
    transition: transform 0.6s cubic-bezier(0.68, -0.55, 0.27, 1.55);
    z-index: 2;
    backdrop-filter: blur(10px);
  }
  
  .login-image img {
    width: 100%;
    border-radius: 16px;
  }

  .description {
    font-size: 14px;
    font-weight: 500;
    opacity: 0.4;
    margin-top: 10px;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s;
  }
  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }

  .error-message {
    color: #ff4d4f;
    margin-bottom: 15px;
    font-size: 14px;
    background-color: rgba(255, 77, 79, 0.1);
    border-radius: 4px;
    padding: 8px;
    text-align: left;
  }
  
  button:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
  
  button:disabled:hover {
    background-color: #cccccc;
    color: #fff;
  }
  
  button:disabled:hover::before {
    width: 0;
  }
  
  .disabled {
    color: #cccccc !important;
    cursor: not-allowed !important;
    pointer-events: none;
  }
  </style>