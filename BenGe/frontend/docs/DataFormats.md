# 数据格式规范文档

本文档定义了应用程序与AI接口交互时使用的数据格式规范。遵循这些格式可以确保前端组件能够正确解析和展示数据。

## 目录

1. [聊天消息格式](#聊天消息格式)
2. [剧本格式](#剧本格式)
3. [角色数据格式](#角色数据格式)
4. [道具数据格式](#道具数据格式)
5. [场景数据格式](#场景数据格式)
6. [剧本分析结果格式](#剧本分析结果格式)
7. [图像生成结果格式](#图像生成结果格式)
8. [AI接口函数和绑定](#AI接口函数和绑定)

## 聊天消息格式

聊天消息用于用户与AI助手之间的对话交流。

```javascript
{
  sender: string,       // 消息发送者，'user'或'ai'
  content: string,      // 消息内容
  timestamp: Date,      // 消息时间戳
  isTyping?: boolean    // 可选，AI是否正在输入中
}
```

### 示例

```javascript
{
  sender: 'ai',
  content: '你好！我是你的剧本创作助手。告诉我你想要创作的剧本类型或主题，我可以帮你打造一个精彩的故事。',
  timestamp: new Date()
}
```

## 剧本格式

剧本是用户创建的完整剧本杀内容，包含标题、内容和元数据。

```javascript
{
  id: string,               // 剧本唯一标识符
  title: string,            // 剧本标题
  content: string,          // 剧本内容（Markdown格式）
  createdAt: Date,          // 创建时间
  updatedAt?: Date,         // 更新时间（可选）
  tags?: string[],          // 标签数组（可选）
  coverImage?: string       // 封面图片URL（可选）
}
```

### 示例

```javascript
{
  id: '1',
  title: '神秘庄园谋杀案',
  content: `# 神秘庄园谋杀案\n\n## 角色\n\n- 威廉·莫里斯: 庄园主人，近日神秘死亡\n- 艾米丽·布莱克: 年轻的女继承人\n- 亨利·李: 忠诚的老管家\n- 苏菲娅·兰: 突然出现的神秘女子\n\n## 场景\n\n1. 古堡大厅: 家族聚会的场所\n2. 密室书房: 死者最后出现的地方\n3. 地下酒窖: 藏有家族秘密的场所\n4. 花园迷宫: 错综复杂，暗藏机关\n\n## 剧情概要\n\n在一个雷雨交加的夜晚，庄园主人威廉·莫里斯被发现死在自己的书房中，现场没有挣扎痕迹，但死者表情惊恐。家族成员聚集在庄园中，每个人都有不可告人的秘密和杀人动机...`,
  createdAt: new Date('2023-05-15'),
  tags: ['悬疑', '推理', '密室']
}
```

## 角色数据格式

角色是剧本中的人物，包含名称、描述和特征等信息。

```javascript
{
  name: string,             // 角色名称
  description: string,      // 角色描述
  traits: string[],         // 角色特征/标签
  image?: string,           // 角色图片URL（可选）
  background?: string,      // 角色背景故事（可选）
  motivation?: string,      // 角色动机（可选）
  relationships?: {         // 与其他角色的关系（可选）
    characterName: string,  // 关系对象的角色名
    description: string     // 关系描述
  }[]
}
```

### 示例

```javascript
{
  name: "威廉·莫里斯", 
  traits: ["精明", "多疑", "野心家"],
  description: "古老家族的继承人，表面温文尔雅，内心机关算尽。对家族财产有着强烈的占有欲，同时隐藏着不为人知的秘密。",
  image: "../assets/character-placeholder.jpg",
  background: "威廉出生于莫里斯家族，从小接受精英教育。父亲去世后，他接管了家族企业，但近年来企业业绩下滑，财务状况不佳。",
  motivation: "保住家族财产和地位，隐藏自己挪用家族基金的秘密"
}
```

## 道具数据格式

道具是剧本中的物品，可能是关键线索或重要物证。

```javascript
{
  icon: string,             // 道具图标（emoji或图标代码）
  name: string,             // 道具名称
  description: string,      // 道具描述
  importance: string,       // 重要性标记（例如"关键道具"、"次要线索"）
  image?: string,           // 道具图片URL（可选）
  location?: string,        // 道具初始位置（可选）
  clue?: string             // 道具所含线索（可选）
}
```

### 示例

```javascript
{
  icon: "🗝️", 
  name: "古老的铜钥匙", 
  description: "一把雕刻精美的铜钥匙，可以打开庄园中的某扇隐藏门，据说与莫里斯家族的秘密宝藏有关。",
  importance: "关键道具",
  location: "老管家的房间",
  clue: "钥匙背面刻有一组数字，是密室保险箱的密码"
}
```

## 场景数据格式

场景是剧本中的环境和地点。

```javascript
{
  name: string,             // 场景名称
  description: string,      // 场景描述
  color: string,            // 场景色调（HEX颜色代码）
  time: string,             // 场景时间（例如"夜晚"、"黄昏"）
  image?: string,           // 场景图片URL（可选）
  characters: string[],     // 场景中出现的角色名称
  props?: string[],         // 场景中的道具（可选）
  events?: string[]         // 场景中发生的事件（可选）
}
```

### 示例

```javascript
{
  name: "古堡大厅",
  color: "#8B4513",
  time: "夜晚",
  description: "莫里斯家族的古堡大厅，天花板高耸，墙上挂满历代家族成员的肖像画，正中央是一架精致的古董钢琴。夜晚时分，壁炉中的火光为整个大厅增添了几分神秘氛围。",
  characters: ["威廉·莫里斯", "亨利·李", "所有宾客"],
  props: ["家族画像", "古董钢琴"],
  events: ["家族晚宴", "威廉宣布遗产分配计划"]
}
```

## 剧本分析结果格式

剧本分析结果包含AI对剧本的分析数据，包括角色、道具和场景的提取和分析。

```javascript
{
  characters: [角色数据对象],     // 角色数组
  props: [道具数据对象],         // 道具数组
  scenes: [场景数据对象],        // 场景数组
  plotAnalysis?: {             // 剧情分析（可选）
    structure: {               // 剧情结构
      setup: string,           // 设定/开场
      conflict: string,        // 冲突
      resolution: string       // 解决
    },
    themes: string[],          // 主题
    strengths: string[],       // 优点
    improvements: string[]     // 改进建议
  }
}
```

### 示例

```javascript
{
  "characters": [
    {
      "name": "威廉·莫里斯", 
      "traits": ["精明", "多疑", "野心家"],
      "description": "古老家族的继承人，表面温文尔雅，内心机关算尽。"
    },
    {
      "name": "艾米丽·布莱克", 
      "traits": ["敏锐", "善良", "正义感强"],
      "description": "前律师，现私家侦探。受邀调查庄园中的怪事。"
    }
  ],
  "props": [
    {
      "icon": "🗝️", 
      "name": "古老的铜钥匙", 
      "description": "一把雕刻精美的铜钥匙，可以打开庄园中的某扇隐藏门。",
      "importance": "关键道具"
    }
  ],
  "scenes": [
    {
      "name": "古堡大厅",
      "color": "#8B4513",
      "time": "夜晚",
      "description": "莫里斯家族的古堡大厅，天花板高耸，墙上挂满历代家族成员的肖像画。",
      "characters": ["威廉·莫里斯", "亨利·李"]
    }
  ],
  "plotAnalysis": {
    "structure": {
      "setup": "富有的莫里斯家族在古老庄园中集结，庄园主人威廉·莫里斯被发现死亡。",
      "conflict": "每个家族成员都有杀人动机，艾米丽作为侦探开始调查，但发现事情比表面更复杂。",
      "resolution": "最终揭露真相，管家亨利为报复家族多年前的不公而策划了这一切。"
    },
    "themes": ["复仇", "贪婪", "家族秘密"],
    "strengths": ["角色动机明确", "场景描写生动", "线索设置巧妙"],
    "improvements": ["可以增加更多反转", "部分角色关系可以更复杂"]
  }
}
```

## 图像生成结果格式

图像生成结果包含AI根据文本描述生成的图像数据。

```javascript
{
  image: string,               // 图像URL或Base64数据
  prompt: string,              // 生成所用的提示词
  type: string,                // 图像类型（'character', 'prop', 'scene'）
  name: string,                // 对象名称
  variations?: string[],       // 其他变体图像URL（可选）
  generationParams?: {         // 生成参数（可选）
    style: string,             // 风格
    resolution: string,        // 分辨率
    quality: string            // 质量
  }
}
```

### 示例

```javascript
{
  "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "prompt": "神秘古老的欧洲庄园大厅，高耸的天花板，墙上挂满家族画像，中央有古董钢琴，壁炉中的火光照亮了暗沉的室内，夜晚，哥特风格",
  "type": "scene",
  "name": "古堡大厅",
  "generationParams": {
    "style": "写实",
    "resolution": "1024x1024",
    "quality": "高"
  }
}
```

## AI接口函数和绑定

以下是应用程序中需要实现与AI服务交互的关键函数以及数据绑定点。这些函数目前在代码中已有基本实现，但需要替换为实际的AI接口调用。

### 1. 聊天交互函数

**函数：** `handleSendMessage`
**代码位置：** `src/view/homePage.vue`
**调用时机：** 用户在聊天界面发送消息时
**需绑定数据：** `chatHistory`

```javascript
// 实现示例
const handleSendMessage = (message) => {
  // 1. 添加用户消息到聊天历史
  chatHistory.value.push({
    sender: 'user',
    content: message,
    timestamp: new Date()
  });
  
  // 2. 调用AI接口获取回复
  aiService.sendMessage({
    message: message,
    chatHistory: chatHistory.value
  }).then(response => {
    // 3. 将AI回复添加到聊天历史
    chatHistory.value.push(response);
  });
};
```

### 2. 剧本生成函数

**函数：** `handleGenerateScript`
**代码位置：** `src/view/homePage.vue`
**调用时机：** 用户请求生成剧本时
**需绑定数据：** `currentScript`, `scripts`

```javascript
// 实现示例
const handleGenerateScript = (prompt) => {
  // 1. 调用AI接口生成剧本
  aiService.generateScript({
    prompt: prompt,
    chatHistory: chatHistory.value
  }).then(response => {
    // 2. 更新当前剧本
    currentScript.value = response.script;
    
    // 3. 保存到剧本列表
    saveGeneratedScript();
    
    // 4. 提取剧本元素
    extractScriptElements();
  });
};
```

### 3. 剧本分析函数

**函数：** `handleAnalyzeScript`
**代码位置：** `src/view/homePage.vue`
**调用时机：** 用户请求分析剧本时
**需绑定数据：** `extractedElements`

```javascript
// 实现示例
const handleAnalyzeScript = () => {
  // 1. 调用AI接口分析剧本
  aiService.analyzeScript({
    scriptContent: currentScript.value.content,
    scriptTitle: currentScript.value.title
  }).then(response => {
    // 2. 更新提取的元素
    extractedElements.value = response.analysis;
    
    // 3. 显示元素画布
    showElementsCanvas.value = true;
  });
};
```

### 4. 剧本元素提取函数

**函数：** `extractScriptElements`
**代码位置：** `src/view/homePage.vue`
**调用时机：** 选择或生成剧本后自动调用
**需绑定数据：** `visualElements`

```javascript
// 实现示例
const extractScriptElements = () => {
  // 1. 调用AI接口提取剧本元素
  aiService.analyzeScript({
    scriptContent: currentScript.value.content,
    scriptTitle: currentScript.value.title
  }).then(response => {
    // 2. 更新可视化元素
    visualElements.value = response.analysis;
  });
};
```

### 5. 图像生成函数

**函数：** `handleGenerateImage`
**代码位置：** `src/view/homePage.vue`
**调用时机：** 用户在可视化工作区请求生成图像时
**需绑定数据：** 更新选中的场景、角色或道具的image属性

```javascript
// 实现示例
const handleGenerateImage = (type, index) => {
  const targetElement = visualElements.value[type + 's'][index];
  const prompt = generatePromptForElement(type, targetElement);
  
  // 1. 调用AI接口生成图像
  aiService.generateImage({
    prompt: prompt,
    type: type,
    name: targetElement.name
  }).then(response => {
    // 2. 更新元素的图像
    visualElements.value[type + 's'][index].image = response.result.image;
  });
};
```

### AI服务接口建议实现

建议创建一个专门的服务模块来处理与AI的所有交互：

```javascript
// src/services/aiService.js
import axios from 'axios';

// AI服务基础URL
const AI_API_URL = 'https://your-ai-api-endpoint.com/api';

// 聊天接口
export const sendMessage = async (params) => {
  const response = await axios.post(`${AI_API_URL}/chat`, params);
  return {
    sender: 'ai',
    content: response.data.content,
    timestamp: new Date()
  };
};

// 剧本生成接口
export const generateScript = async (params) => {
  const response = await axios.post(`${AI_API_URL}/generate-script`, params);
  return {
    script: {
      id: Date.now().toString(),
      title: response.data.title || '新剧本',
      content: response.data.content,
      createdAt: new Date()
    }
  };
};

// 剧本分析接口
export const analyzeScript = async (params) => {
  const response = await axios.post(`${AI_API_URL}/analyze-script`, params);
  return {
    analysis: {
      characters: response.data.characters || [],
      props: response.data.props || [],
      scenes: response.data.scenes || [],
      plotAnalysis: response.data.plotAnalysis
    }
  };
};

// 图像生成接口
export const generateImage = async (params) => {
  const response = await axios.post(`${AI_API_URL}/generate-image`, params);
  return {
    result: response.data
  };
};
```

### 数据持久化建议

为了提高用户体验，建议实现以下数据持久化功能：

1. 将剧本列表保存到本地存储(localStorage)，以便用户刷新页面后仍能看到之前的剧本
2. 将聊天历史暂存，以便用户可以继续之前的对话
3. 将生成的图像缓存，减少重复生成请求

```javascript
// 示例：保存剧本到本地存储
const saveScriptsToLocalStorage = () => {
  localStorage.setItem('scripts', JSON.stringify(scripts.value));
};

// 示例：从本地存储加载剧本
const loadScriptsFromLocalStorage = () => {
  const savedScripts = localStorage.getItem('scripts');
  if (savedScripts) {
    scripts.value = JSON.parse(savedScripts);
  }
};
```

### 调试和测试建议

在实际AI接口完成前，可以使用模拟数据进行测试：

1. 创建模拟AI响应函数，返回符合上述数据格式的模拟数据
2. 添加随机延迟，模拟真实网络请求延迟
3. 添加加载状态处理，提高用户体验

```javascript
// 示例：模拟AI聊天响应
const mockAiResponse = (message) => {
  return new Promise(resolve => {
    // 添加随机延迟
    setTimeout(() => {
      resolve({
        sender: 'ai',
        content: `这是对"${message}"的模拟回复。实际AI接口完成后，这里将返回真实的AI生成内容。`,
        timestamp: new Date()
      });
    }, 1000 + Math.random() * 2000);
  });
};
``` 