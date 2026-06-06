// socket.js
import { reactive } from "vue";
import { useCanvasStore } from "./canvasStore";
import { useCharacterStore } from "./character";
import { useClueStore } from "./clue";
import { useAtmosphereStore } from "./atmosphere";

// 不要在顶层调用 useStore，先定义变量
let canvasStore = null;
let characterStore = null;
let clueStore = null;
let atmosphereStore = null;

const socketState = reactive({
  socket: null,
  currentUserId: null,
  currentUsername: null,
  messages: [],
  members: [],
  isConnected: false,
  isConnecting: false, // 👈 添加标记防重复连接
  roomId: null,
  avatar: null,
  newMessage: "",

  roleSelections: {}, // 存储每个角色的选择状态，key是角色索引，value是已选择的用户名

  //   节点和边的信息
  nodes: {},
  edges: {},

  userRole: -1,
  roles: [
    {
      name: "剧情设计师",
      description:
        "擅长构建故事主线与反转，通过精妙布局勾勒出跌宕起伏的剧情，掌控节奏与情感张力，引导玩家沉浸在虚构与现实交织的世界中。",
      task: [
        "亲爱的剧情大师，准备好画出故事的脉络了吗？",
        "你的工作是搭建整个故事的时间线和关键事件，像导演一样统筹全局。",
        "先来画几个重要时间节点吧！这些节点是整个剧本的骨架。",
        "事件之间的关系也要梳理清楚，保证故事流畅又紧凑。",
        "当你确定节点，别忘了告诉角色设计师和线索架构师，让他们准备配合哦！",
        "需要灵感？找协作助理AI聊聊，给你意想不到的剧情闪光点！",
        "加油，让我们一起把这场悬疑故事推向高潮！",
      ],
    },
    {
      name: "角色设计师",
      description:
        "负责塑造人物性格与关系网络，为每一个角色赋予鲜明动机与成长轨迹，让玩家在扮演中感受真实的情感与冲突。",
      task: [
        "嘿，角色塑造达人！让人物跳出纸面吧！",
        "你的使命是创造活灵活现的人物和他们之间复杂微妙的关系。",
        "根据剧情设计师的时间节点，安排角色何时出现、他们的背景和秘密。",
        "构建角色关系网，谁是朋友，谁是敌人？让人物之间产生火花！",
        "随时和线索架构师沟通，确保人物动机和线索逻辑一致。",
        "记得用标签和注释帮团队快速理解每个角色。",
        "角色鲜活了，故事才精彩！",
      ],
    },
    {
      name: "线索设计师",
      description:
        "精于埋设线索与误导，通过巧妙布局隐藏真相，引导推理节奏，确保玩家在抽丝剥茧中感受层层惊喜与挑战。",
      task: [
        "侦探专家上线，线索全盘掌控！",
        "你的任务是设计推理的蛛丝马迹，让玩家循着线索步步深入真相。",
        "按照时间线节点，布置关键线索和证据，设计合理的推理链条。",
        "绑定线索与事件、角色，保证逻辑清晰，别让推理跑偏！",
        "常和剧情设计师和角色设计师交流，确认线索和故事同步推进。",
        "利用冲突检测AI，找出可能的逻辑漏洞。",
        "把握好线索节奏，玩家才能越推越带劲！",
      ],
    },
    {
      name: "氛围设计师",
      description:
        "以视觉、音效与文本语言营造沉浸式体验，塑造紧张或诡秘的氛围，让每一处场景都充满戏剧张力，增强整体代入感。",
      task: [
        "场景魔法师，来营造氛围啦！",
        "你是这场故事的“滤镜”和“布景师”，让每个场景生动立体。",
        "根据剧情设计师的事件时间点，设计符合事件气氛的场景风格。",
        "利用AI帮忙快速生成视觉素材或背景描述。",
        "时刻关注剧情节奏，调整氛围色调，打造悬疑、紧张或神秘的感觉。",
        "跟剧情设计师保持沟通，确保氛围与故事情绪一致。",
        "场景有了氛围，故事才有“灵魂”！",
      ],
    },
  ],
  // 第一阶段存储的方向
  options: [],
  direction: {
    title: "",
    description: "",
  },
  AICompleteScriptContent: "",
  CompleteScriptContent: "",
});

// 在这里初始化 store，确保只初始化一次
async function ensureStores() {
  if (!canvasStore) {
    const { useCanvasStore } = await import("./canvasStore");
    const { useCharacterStore } = await import("./character");
    const { useClueStore } = await import("./clue");
    const { useAtmosphereStore } = await import("./atmosphere");

    canvasStore = useCanvasStore();
    characterStore = useCharacterStore();
    clueStore = useClueStore();
    atmosphereStore = useAtmosphereStore();
  }
}

async function setupWebSocket() {
  await ensureStores();
  if (socketState.isConnected || socketState.isConnecting) {
    // console.warn("WebSocket 已经连接");
    return;
  }
  socketState.isConnecting = true; // 正在连接

  const token = localStorage.getItem("token");
  if (!token) {
    // console.error("未找到 token，无法建立 WebSocket 连接");
    return;
  }
  // 穿透版本
  // socketState.socket = new WebSocket("ws://9cd1-2001-250-4001-5012-c1e1-eff4-a331-25f4.ngrok-free.app/ws");

  socketState.socket = new WebSocket(
    "ws://1.15.138.177/ws"
  );

  let pingInterval = null;

  socketState.socket.onopen = () => {
    if (socketState.socket.readyState === WebSocket.OPEN) {
      socketState.socket.send(
        JSON.stringify({
          type: "auth",
          token,
          roomId: socketState.roomId,
          avatar: socketState.avatar,
        })
      );
      socketState.isConnecting = false;
      socketState.isConnected = true;
    } else {
      // console.warn(
      //   "WebSocket 还未准备好，当前状态:",
      //   socketState.socket.readyState
      // );
    }

    // 启动心跳机制（每 20 秒发一个 ping）
    pingInterval = setInterval(() => {
      if (
        socketState.socket &&
        socketState.socket.readyState === WebSocket.OPEN
      ) {
        // socketState.socket.send(JSON.stringify({ type: "ping" }));
        // 可选：// console.log("发送 ping 心跳");
      }
    }, 20000);
  };

  socketState.socket.onmessage = (event) => {
    const msg = JSON.parse(event.data);

    // console.log("event:", msg);

    if (msg.type === "userInfo") {
      socketState.currentUserId = msg.userId;
      socketState.currentUsername = msg.username;
      // console.log(
      //   "接收到用户信息:",
      //   socketState.currentUserId,
      //   socketState.currentUsername
      // );
    } else if (msg.type === "chat") {
      const isAIMessage = msg.username === "AI助手" || msg.userId === -1;
      const newMessage = {
        ...msg,
        isMe: msg.userId === socketState.currentUserId,
        sender: msg.username,
        content: msg.content,
        time: msg.time,
        avatar: msg.avatar || socketState.avatar,
        isAI: isAIMessage,
      };

      socketState.messages.push(newMessage);
      // console.log("消息数组更新，当前长度:", socketState.messages.length);
    } else if (msg.type === "system") {
      socketState.messages.push({
        type: "system",
        content: msg.message,
        time: getCurrentTime(),
        isMe: false,
        isSystem: true,
      });
    } else if (msg.type === "members") {
      handleMembersUpdate(msg.members || []);
    } else if (msg.type === "error") {
      // console.error("WebSocket 错误:", msg.message);
      alert("错误: " + msg.message);
    } else if (msg.type === "role") {
      handleRoleSelection(msg.roleName, msg.username);
    } else if (
      msg.type === "canvas" ||
      msg.type === "character" ||
      msg.type === "clue" ||
      msg.type === "atmosphere"
    ) {
      handleCanvas(msg);
    } else if (msg.type === "vote") {
      handleVote(msg);
    } else if (msg.type == "enter-third-stage") {
      socketState.CompleteScriptContent = msg.content;
    } else if (msg.type == "enter-second-stage") {
      socketState.direction = JSON.parse(msg.content);
    }
  };

  socketState.socket.onclose = () => {
    // console.log("WebSocket 连接已关闭");
    socketState.isConnected = false;
    socketState.isConnecting = false;
    if (pingInterval) clearInterval(pingInterval);

    if(!socketState.isConnected)setupWebSocket();
    // 自动尝试重连
    // setTimeout(() => {
    //   // console.log("尝试重连 WebSocket...");
    // }, 5000); // 可配置：5 秒后重连
  };

  socketState.socket.onerror = (err) => {
    // console.error("WebSocket 连接错误:", err);
    socketState.isConnected = false;
    socketState.isConnecting = false;
    if (pingInterval) clearInterval(pingInterval);
  };
}

function handleMembersUpdate(incomingMembers) {
  const existing = socketState.members || [];

  const mergedMap = new Map();

  existing.forEach((member) => {
    mergedMap.set(member.id, member);
  });

  incomingMembers.forEach((member) => {
    mergedMap.set(member.id, member);
  });

  socketState.members = Array.from(mergedMap.values());
}

function sendMessage() {
  // console.log("sendMessage:", socketState.newMessage);
  if (socketState.isConnected && socketState.socket) {
    const messageData = {
      type: "chat",
      content: socketState.newMessage,
      time: getCurrentTime(),
      avatar: socketState.avatar,
    };
    socketState.socket.send(JSON.stringify(messageData));
    socketState.newMessage = "";
  } else {
    // console.error("WebSocket 连接未就绪");
  }
}

function getCurrentTime() {
  const now = new Date();
  return `${now.getHours()}:${now.getMinutes().toString().padStart(2, "0")}`;
}

function closeWebSocket() {
  if (socketState.socket) {
    socketState.socket.close();
  }
}

// 角色选择广播
function handleRoleSelection(roleName, username) {
  // 遍历所有角色，查找是否有该用户已选择了其他角色
  for (let existingRole in socketState.roleSelections) {
    // console.log("打印：", existingRole);
    // 如果当前角色是其他角色且该角色已经被用户名选择
    if (
      socketState.roleSelections[existingRole] === username &&
      existingRole !== roleName
    ) {
      // 清空原来选择的角色
      // console.log(`${username} 已选择了 ${existingRole}，正在清空该角色的选择`);
      socketState.roleSelections[existingRole] = ""; // 清空原选择
    }
  }

  // 更新当前角色的选择
  socketState.roleSelections[roleName] = username;


  // 更新成员列表
  updateMembers(roleName, username);
  // console.log("更新后的成员信息:", socketState.members);
}

// 同步画布
function handleCanvas(msg) {
  // console.log("接收到canvas：", msg);
  if (msg.content) {
    // console.log("进入：", socketState.AICompleteScriptContent);
    socketState.AICompleteScriptContent = msg.content;
    // console.log("OKOK:", socketState.AICompleteScriptContent);
    return;
  }

  if (msg.type == "canvas") {
    // 更新叙事设计师的节点
    canvasStore.nodes = msg.nodes || [];

    // 合并边：保留其他设计师的边，更新叙事设计师的边
    const incomingEdges = msg.edges || [];
    const existingEdges = canvasStore.edges || [];

    // 移除现有的叙事设计师边（type为'custom'且不是线索相关的边）
    const filteredEdges = existingEdges.filter((edge) => {
      // 保留人物设计师的边（character-scene, relationship）
      if (edge.type === "character-scene" || edge.type === "relationship") {
        return true;
      }
      // 保留线索设计师的边（custom类型但data.type包含clue相关）
      if (edge.type === "custom" && edge.data?.type?.includes("clue")) {
        return true;
      }
      // 保留氛围设计师的边
      if (edge.data?.type === "atmosphere-scene") {
        return true;
      }
      // 移除叙事设计师的边
      return false;
    });

    // 合并边数组
    canvasStore.edges = [...filteredEdges, ...incomingEdges];
  } else if (msg.type == "character") {
    // 更新人物设计师的节点和边
    characterStore.nodes = msg.characterNodes || [];
    characterStore.edges = msg.characterEdges || [];

    // 同时更新canvasStore中的人物设计师边
    const incomingCharacterEdges = msg.characterEdges || [];
    const existingCanvasEdges = canvasStore.edges || [];

    // 移除现有的人物设计师边
    const filteredCanvasEdges = existingCanvasEdges.filter(
      (edge) => edge.type !== "character-scene" && edge.type !== "relationship"
    );

    // 合并边数组
    canvasStore.edges = [...filteredCanvasEdges, ...incomingCharacterEdges];
  } else if (msg.type == "clue") {
    // 更新线索设计师的节点
    clueStore.nodes = [
      ...(msg.clueNodes || []),
      ...(msg.inferenceNodes || []),
      ...(msg.personNodes || []),
    ];
    // 后端返回的是edges字段，不是clueEdges
    clueStore.edges = msg.edges || msg.clueEdges || [];

    // 同时更新canvasStore中的线索设计师边
    const incomingClueEdges = msg.edges || msg.clueEdges || [];
    const existingCanvasEdges = canvasStore.edges || [];

    // 移除现有的线索设计师边
    const filteredCanvasEdges = existingCanvasEdges.filter((edge) => {
      // 保留非线索相关的边
      return !(
        edge.type === "clue-edge" ||
        (edge.type === "custom" && edge.data?.type?.includes("clue"))
      );
    });

    // 合并边数组
    canvasStore.edges = [...filteredCanvasEdges, ...incomingClueEdges];
  } else if (msg.type == "atmosphere") {
    // 更新氛围设计师的节点和边
    atmosphereStore.nodes = msg.atmosphereNodes || [];
    atmosphereStore.edges = msg.atmosphereEdges || [];
  }
}

// 更新成员的角色选择状态
function updateMembers(roleName, username) {
  socketState.members.forEach((member) => {
    if (member.username === username) {
      member.selectedRole = roleName;
    }
  });
}

// 同步投票部分
function handleVote(msg) {
  if (msg.key && msg.username) {
    const member = socketState.members.find((m) => m.username === msg.username);
    if (member) {
      member.key = msg.key;
    }
  }
  if (msg.hasChosen) {
    const member = socketState.members.find((m) => m.username === msg.username);
    if (member) {
      member.hasChosen = msg.hasChosen;
    }
  }
  if (msg.vote) {
    const member = socketState.members.find((m) => m.username === msg.username);
    if (member) {
      member.vote = msg.vote;
    }
  }
  if (msg.hasVoted) {
    const member = socketState.members.find((m) => m.username === msg.username);
    if (member) {
      member.hasVoted = msg.hasVoted;
    }
  }
  if (msg.content) {
    socketState.options = JSON.parse(msg.content);
    // console.log("socketState.options:", socketState.options);
  }
}

//

export { socketState, setupWebSocket, sendMessage, closeWebSocket };
