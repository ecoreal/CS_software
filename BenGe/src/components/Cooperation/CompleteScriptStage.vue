<template>
  <div class="back-bg"></div>
  <div class="front-bg"></div>
  <div class="container">
    <div class="header">
      <div class="left-menu">
        <div class="stage-title">
          <i class="fa-solid fa-bars-staggered title-icon"></i>
          <span class="title">共同编辑</span>
        </div>
      </div>
      <div class="right-menu">
        <div class="back-image"></div>
        <div class="menu-front">
          <i class="fa-solid fa-scroll logo"></i>
          <div class="menu-group">
            <div class="menu-item">BenGe.Vision</div>
            <i class="fa-solid fa-circle-info menu-icon"></i>
            <img src="../../assets/login.png" alt="avatar" class="avatar" />
          </div>
        </div>
      </div>
    </div>
    <div class="main-area">
      <div class="toolbar-container">
        <button @click="format('header', 1)" class="ql-custom">
          <i class="fas fa-heading"></i><span>Heading 1</span>
        </button>
        <button @click="format('header', 2)" class="ql-custom">
          <i class="fas fa-heading"></i><span>Heading 2</span>
        </button>
        <button @click="format('header', 3)" class="ql-custom">
          <i class="fas fa-heading"></i><span>Heading 3</span>
        </button>
        <button @click="format('header', false)" class="ql-custom">
          <i class="fas fa-paragraph"></i><span>Normal</span>
        </button>
        <button @click="format('bold')" class="ql-custom">
          <i class="fas fa-bold"></i><span>Bold</span>
        </button>
        <button @click="format('italic')" class="ql-custom">
          <i class="fas fa-italic"></i><span>Italic</span>
        </button>
        <button @click="format('underline')" class="ql-custom">
          <i class="fas fa-underline"></i><span>Underline</span>
        </button>
        <button @click="format('strike')" class="ql-custom">
          <i class="fas fa-strikethrough"></i><span>Strike</span>
        </button>
        <button @click="format('list', 'ordered')" class="ql-custom">
          <i class="fas fa-list-ol"></i><span>Ordered</span>
        </button>
        <button @click="format('list', 'bullet')" class="ql-custom">
          <i class="fas fa-list"></i><span>Bullet</span>
        </button>
        <button @click="format('link', prompt('Enter URL'))" class="ql-custom">
          <i class="fas fa-link"></i><span>Link</span>
        </button>
        <button @click="format('code-block')" class="ql-custom">
          <i class="fas fa-code"></i><span>Code</span>
        </button>
        <button @click="format('clean')" class="ql-custom">
          <i class="fas fa-eraser"></i><span>Clear</span>
        </button>
      </div>

      <div class="edit-area">
        <div class="edit-header">
          <h2>编辑区</h2>
          <div style="
              margin-left: 20px;
              display: flex;
              align-items: center;
              gap: 10px;
            ">
            <el-tag :type="isConnected ? 'success' : 'danger'" size="small" style="font-size: 12px">
              {{ connectionStatus }}
            </el-tag>
            <span style="font-size: 12px; color: #666">
              房间ID: {{ roomId }}
            </span>
          </div>
          <div style="margin-left: auto; display: flex; gap: 10px">
            <el-button class="button" @click="saveToServer" :disabled="!isConnected">
              <i class="fas fa-save fa-fw save-icon"></i>
              保存剧本
            </el-button>
            <el-button class="button print" @click="output">
              <img src="../../assets/output.png" style="width: 16px; height: 16px; margin-right: 2px" />导出剧本</el-button>
          </div>
        </div>
        <div id="editor" style="height: calc(100% - 100px)"></div>
      </div>

      <!-- 成员区 -->
      <div class="member-area" :class="{ collapsed: !isMemberOpen }">
        <div class="member-header">
          <button class="toggle-btn" @click="toggleMemberArea">
            <img v-if="isMemberOpen" style="object-fit: cover; width: 24px; height: 24px"
              src="../../assets/third/close.png" />
            <img v-if="!isMemberOpen" style="object-fit: cover; width: 24px; height: 24px"
              src="../../assets/third/open.png" />
          </button>
          <span v-show="isMemberOpen" class="member-titlr">在线成员 ({{ socketState.members.length }})</span>
        </div>
        <div class="member-list">
          <div class="member-item" v-for="member in socketState.members" :key="member.id">
            <div class="member-avatar-container">
              <img :src="member.avatar" alt="avatar" class="member-avatar" />
              <div class="online-indicator"></div>
            </div>
            <span v-show="isMemberOpen" class="member-name">{{
              member.username
            }}</span>
          </div>
          <div v-if="socketState.members.length === 0" class="no-members">
            暂无其他成员在线
          </div>
        </div>
      </div>
    </div>
    <FloatingChatWrapper :room-id="roomId" :user-id="currentUser.id" :user-name="currentUser.name"
      :avatar="currentUser.avatar" @membersUpdated="updateMembers" />
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, computed } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import Quill from "quill";
import QuillCursors from "quill-cursors";
import "quill/dist/quill.snow.css";
import * as Y from "yjs";
import { QuillBinding } from "y-quill";
import { WebsocketProvider } from "y-websocket";
import { marked } from "marked";
import TurndownService from "turndown";
import axios from "axios";
import FloatingChatWrapper from "./third/FloatingChatWrapper.vue";
import loginImage from "../../assets/login.png";
import { socketState } from "@/stores/socket";

const route = useRoute();
const isMemberOpen = ref(true);
const isConnected = ref(false);
const connectionStatus = ref("连接中...");

const toggleMemberArea = () => {
  isMemberOpen.value = !isMemberOpen.value;
};

// 注册光标模块
Quill.register("modules/cursors", QuillCursors);

// 从路由参数获取房间ID，如果没有则使用默认值1
const roomId = computed(() => {
  return parseInt(route.params.roomId) || 1;
});

// 从localStorage获取用户信息
const currentUser = computed(() => {
  const username = localStorage.getItem("username") || "匿名用户";
  const userId = `user_${username}_${Date.now()}`;
  return {
    id: userId,
    name: username,
    color: getRandomColor(),
    avatar: loginImage,
  };
});

const members = ref([]);

let quill, ydoc, provider, cursorsModule;

onMounted(async () => {
  try {
    connectionStatus.value = "初始化编辑器...";

    // 初始化 Yjs 文档
    ydoc = new Y.Doc();
    const ytext = ydoc.getText("quill");

    // 初始化 Quill 编辑器
    quill = new Quill("#editor", {
      theme: "snow",
      modules: {
        cursors: {
          transformOnTextChange: true,
        },
        toolbar: false,
      },
    });

    // 获取 cursors 模块实例
    cursorsModule = quill.getModule("cursors");

    connectionStatus.value = "连接协作服务器...";

    // 初始化 WebSocket Provider - 使用动态房间ID
    provider = new WebsocketProvider(
      "ws://1.15.138.177/yjs",
      `room-${roomId.value}`,
      ydoc
    );

    // 设置本地用户信息
    provider.awareness.setLocalStateField("user", {
      id: currentUser.value.id,
      name: currentUser.value.name,
      color: currentUser.value.color,
      avatar: currentUser.value.avatar,
    });

    // 绑定 Yjs 与 Quill
    new QuillBinding(ytext, quill, provider.awareness);

    // 监听连接状态
    provider.on("status", (event) => {
      // console.log("Y.js连接状态:", event.status);
      if (event.status === "connected") {
        isConnected.value = true;
        connectionStatus.value = "已连接";
        ElMessage.success("协作服务器连接成功");
      } else if (event.status === "disconnected") {
        isConnected.value = false;
        connectionStatus.value = "连接断开";
        ElMessage.warning("协作服务器连接断开");
      }
    });

    // 监听远程用户状态更新（用于光标显示）
    provider.awareness.on("update", () => {
      const states = Array.from(provider.awareness.getStates().entries());
      cursorsModule.clearCursors();

      for (const [clientID, state] of states) {
        if (clientID === provider.awareness.clientID) continue;
        const user = state.user;
        const selection = state.selection;
        if (user && selection) {
          cursorsModule.createCursor(
            clientID.toString(),
            user.name,
            user.color
          );
          cursorsModule.moveCursor(clientID.toString(), selection);
        }
      }
    });

    // 本地用户光标变化广播
    quill.on("selection-change", (range) => {
      provider.awareness.setLocalStateField("selection", range ?? null);
    });

    // 首次加载内容
    // let hasInitialized = false;
    // if (hasInitialized) return;
    // hasInitialized = true;

    // const quillContents = quill.getContents();
    // const isQuillEmpty =
    //   quillContents.ops.length === 1 && quillContents.ops[0].insert === "\n";

    // // console.log("quill.getLength:", quill.getText());
    // // console.log("Quill:", quill);
    // quill.setText(socketState.CompleteScriptContent);
    // if (!quill.getLength() && socketState.CompleteScriptContent) {
    //   const markdown = socketState.CompleteScriptContent;
    //   const html = marked.parse(markdown);
    //   quill.setContents([], "api");
    //   quill.clipboard.dangerouslyPasteHTML(0, html, "api");
    // }

    provider.once("synced", async () => {
  const ytext = ydoc.getText("quill");

  // 如果 ytext 是空的，说明是首次进入或首次写入
  if (ytext.length === 0 && socketState.CompleteScriptContent) {
    const markdown = socketState.CompleteScriptContent;
    const html = marked.parse(markdown);

    // 用 Quill 插入 HTML，触发绑定到 ytext 的变更
    quill.setContents([], 'api'); // 清空旧内容
    quill.clipboard.dangerouslyPasteHTML(0, html, 'api');

    // console.log("✏️ 初次插入内容成功！");
  } else {
    // console.log("✅ 已有内容，跳过初始化。");
  }
});

  } catch (error) {
    // console.error("初始化编辑器失败:", error);
    ElMessage.error("初始化编辑器失败，请刷新页面重试");
    connectionStatus.value = "连接失败";
  }
});

function format(name, value = true) {
  if (quill) {
    if (name === "clean") {
      quill.removeFormat(quill.getSelection());
    } else {
      quill.format(name, value);
    }
  }
}

onBeforeUnmount(() => {
  provider?.destroy();
  ydoc?.destroy();
});

// 保存剧本到服务器
async function saveToServer() {
  try {
    if (!quill) {
      ElMessage.error("编辑器未初始化");
      return;
    }

    const turndownService = new TurndownService({
      headingStyle: "atx",
      bulletListMarker: "-",
      codeBlockStyle: "fenced",
    });

    // 添加规则处理 quill 中可能的 <div> 或 <span>
    turndownService.addRule("customBlock", {
      filter: ["div", "span"],
      replacement: function (content) {
        return content;
      },
    });

    const html = quill.root.innerHTML;
    const markdown = turndownService.turndown(html);

    // TODO: 需要后端实现 - 保存房间剧本内容的接口
    // POST /api/script/room/save
    // 参数: { roomId, content, title }
    try {
      const response = await axios.post("/api/script/room/save", {
        roomId: roomId.value,
        content: markdown,
        title: `房间${roomId.value}的剧本`,
      });

      if (response.data === "success" || response.status === 200) {
        ElMessage.success("剧本保存成功！");
        // console.log("✅ 保存成功，内容：\n", markdown);
      } else {
        ElMessage.error("保存失败：" + (response.data || "未知错误"));
      }
    } catch (apiError) {
      // 如果后端接口不存在，先在本地保存
      // console.warn("后端保存接口未实现，使用本地保存:", apiError.message);
      ElMessage.warning("后端接口未实现，内容已保存到控制台");
      // console.log("✅ 本地保存内容：\n", markdown);

      // 可以保存到 localStorage 作为临时方案
      localStorage.setItem(`room_${roomId.value}_script`, markdown);
      ElMessage.success("剧本已临时保存到本地");
    }
  } catch (error) {
    // console.error("保存失败:", error);
    ElMessage.error("保存失败：" + error.message);
  }
}

// 随机颜色
function getRandomColor() {
  const colors = ["#f44336", "#2196f3", "#4caf50", "#ff9800", "#9c27b0"];
  return colors[Math.floor(Math.random() * colors.length)];
}

// 导出剧本
function output() {
  try {
    if (!quill) {
      ElMessage.error("编辑器未初始化");
      return;
    }

    const turndownService = new TurndownService({
      headingStyle: "atx",
      bulletListMarker: "-",
      codeBlockStyle: "fenced",
    });

    // 添加规则处理 quill 中可能的 <div> 或 <span>
    turndownService.addRule("customBlock", {
      filter: ["div", "span"],
      replacement: function (content) {
        return content;
      },
    });

    const html = quill.root.innerHTML;
    const markdown = turndownService.turndown(html);

    // 创建下载链接
    const blob = new Blob([markdown], { type: "text/markdown;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `房间${roomId.value}_剧本_${new Date()
      .toISOString()
      .slice(0, 10)}.md`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    ElMessage.success("剧本导出成功！");
    // console.log("✅ 导出 Markdown：\n", markdown);
  } catch (error) {
    // console.error("导出失败:", error);
    ElMessage.error("导出失败：" + error.message);
  }
}

function updateMembers(membersList) {
  // 直接更新整个成员列表
  members.value = membersList.map((member) => ({
    id: member.id,
    name: member.username,
    avatar: member.avatar || loginImage,
  }));
  // console.log("成员列表已更新:", members.value);
}
</script>

<style scoped>
.back-bg {
  background-image: url("../../assets/chat-main-bg.png");
  background-size: cover;
  background-repeat: no-repeat;
  height: 100%;
  width: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.front-bg {
  z-index: 2;
  height: 100%;
  width: 100%;
  background: transparent;
  backdrop-filter: blur(4px);
}

.member-header {
  display: flex;
  height: 40px;

  align-items: center;
}

.member-title {
  font-size: 16px;
  font-weight: bold;
}

.toggle-btn {
  height: 30px;
  width: 30px;

  border: none;
  background-color: white;
}

.member-area {
  width: 240px;
  transition: width 0.3s ease;
  overflow: hidden;
  background-color: white;
  border-top-left-radius: 15px;
  border-bottom-left-radius: 15px;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  padding-left: 14px;
  padding-top: 20px;
}

/* 折叠状态下宽度变小，只保留 header */
.member-area.collapsed {
  width: 50px;
}

.member-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  /* 自动换行、每列最小120px */
  margin-top: 40px;
  justify-content: center;
  /* 整体居中 */
  width: 100%;
  max-width: 100%;
}

.member-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 2px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.member-item:hover {
  background-color: #f5f5f5;
}

.member-avatar-container {
  position: relative;
  margin-right: 8px;
}

.member-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #ccc;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background-color: #4caf50;
  border: 2px solid #fff;
  border-radius: 50%;
}

.member-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.no-members {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 20px;
}

.container {
  width: 95%;
  height: 92%;
  /* 使用视口高度，确保充满整个屏幕 */
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(90deg,
      #edeef2 0%,
      #ecedef 38%,
      #ecedf1 70%,
      #edeef3 100%);
  background-size: cover;
  background-repeat: no-repeat;
  z-index: 3;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
  height: 65px;
  padding: 0 0;
  background-color: transparent;
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
  font-size: 18px;
}

.right-menu {
  flex: 1;
  display: flex;
  align-items: center;
  margin-left: auto;
  gap: 30px;
  position: relative;
  height: 100%;
  border-bottom-left-radius: 30px;
  border-top-right-radius: 20px;
  border-top-left-radius: 4px;
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
  background-color: transparent;
  backdrop-filter: blur(2px);
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
  background-image: linear-gradient(45deg,
      #e6e6ed 0%,
      #c6c3df 30%,
      #b5c4e1 70%,
      #b5bddf 100%);
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

.main-area {
  flex: 1;
  display: flex;
  padding: 32px 0px;
  gap: 20px;
  /* 子区域间距 */
  width: calc(100% - 100px);
  min-width: 827px;
  width: 100%;
  background-color: transparent;
  height: calc(100% - 200px);
  min-height: 500px;
  box-sizing: border-box;
  /* 确保 padding 不超宽 */
  border-radius: 20px;
  border-color: transparent;
  z-index: 2;
}

/* 工具栏容器竖排 */
.toolbar-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  width: 220px;
  background: transparent;
  border-radius: 12px;
}

/* 自定义按钮样式 */
.ql-custom {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  width: 80%;
  background: transparent;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  color: #2d3748ae;
  font-weight: 500;
  font-size: 14px;
  overflow: hidden;
  z-index: 0;
  transition: color 0.2s ease;
}

.ql-custom i {
  font-size: 16px;
}

/* 波浪渐变背景效果 */
.ql-custom::before {
  content: "";
  position: absolute;
  top: 0;
  left: -50%;
  width: 200%;
  height: 100%;
  background: radial-gradient(circle at 50% 0%,
      #2541ec 0%,
      #3182ce 50%,
      transparent 100%);
  transform: translateY(-110%);
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: -1;
  pointer-events: none;
}

.ql-custom:hover::before {
  transform: translateY(0%);
}

.ql-custom:hover {
  color: white;
}

.ql-custom:hover span {
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 图标 + 文字 */
.ql-custom span {
  pointer-events: none;
}

.edit-area {
  /* width: 66%; */
  flex: 1;
  min-width: 442px;
  background-color: white;
  background: #f4f6f7;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  margin-left: -40px;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
  transition: width 0.3s ease;
}

.edit-header {
  display: flex;
  margin: 0px 20px 20px;
  align-items: center;
  padding: 10px;
}

/* .chat-area {
  height: 85%;
  display: flex;
  flex-direction: column;
  position: absolute;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
} */

.button {
  height: 40px;
  border-radius: 30px;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.save-icon {
  margin-right: 6px;
  font-size: 16px;
  color: hsla(231, 88%, 53%, 0.7);
  /* 控制颜色、透明度 */
}

.button.save {
  background-color: hsla(240, 100%, 100%, 0.435);
  color: hsla(231, 88%, 53%, 0.339);
}

.button.print {
  background-color: #1e3df0;
  color: #f5f6f9;
}

.button.save:hover i {
  color: white;
}

.button.print:hover {
  background-color: #1e0a9a;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(39, 18, 187, 0.3);
}

.button:disabled {
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

#editor {
  border-color: transparent;
  border-radius: 10px;
  background: linear-gradient(180deg,
      #e5e7f3 0%,
      #e3e7f4 25%,
      #e0e6f6 50%,
      #dfe4f7 75%,
      #dde3f4 100%);
  padding: 10px;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  flex: 1;
}
</style>