import request from "./request";

// 获取所有的列表，在用户点击我的剧本列表时使用
export function getAllScript() {
  return request.get("script/user");
}

// 获取用户点击的剧本，返回详细的信息
export function getSelectScript(scriptId) {
  return request.get(`/script/${scriptId}`);
}

// 删除剧本
export function deleteScriptApi(scriptId) {
  return request.delete(`/script/${scriptId}`);
}

// 新建剧本
export function createScript() {
  return request.post("/script/create");
}

// 向AI发送请求
export function sendMessageToAI(message, scriptId) {
  return request.put("script/reply", {
    scriptId: scriptId,
    message: message,
  });
}

// 获取剧本方向选项
export function getScriptDirections(prompt, scriptId) {
  return request.post("/ai/slogan/stream", {
    prompt: prompt,
    scriptId: scriptId,
  });
}

// 更新第一阶段后剧本的内容
export function updateScriptContent(scriptId, content, stage) {
  return request.put("script/update", {
    scriptId: scriptId,
    content: content,
    stage: stage,
  });
}

// 第二阶段对话更新剧本
export function updateScriptContent2(scriptId, content) {
  return request.post("script/reply2nd", {
    scriptId: scriptId,
    message: content,
  });
}

// 第二阶段剧本分析
export function analyzeScript(scriptId, content) {
  return request.put("script/analyze", {
    scriptId: scriptId,
    message: content,
  });
}

// 第三阶段生成完整的剧本
export function generateCompleteScript(scriptId, content) {
  return request.post("script/reply3rd", {
    scriptId: scriptId,
    message: content,
  });
}

// 生成图片url
export function generateElementImage(scriptId, elementId) {
  return request.put("script/visualize", {
    scriptId: scriptId,
    elementId: elementId,
  });
}

// 第二阶段交给AI整合（房主才能调用）
export function generateCooperateFramework(data) {
  return request.post("room/collaboration/generate", data);
}

// 确认进入第三阶段
// ✅ 修复后
export function enterThirdStage(roomId, data) {
  return request.post("room/enter-third-stage", {
    roomId,
    data,
  });
}

