// src/api/room.js
import request from "./request";

//获取房间列表
export function getRoomList(page = 1, limit = 10) {
  return request.post("/room/list", {
    page,
    limit,
  });
}

// 创建房间
export function createRoom(data) {
  const roomData = {
    ...data,
    havePwd: Boolean(data.havePwd),
  };
  // console.log("我的测试：", roomData);
  return request.post("/room", roomData);
}

//申请加入房间
export function joinRoom(roomId, password = "", applyReason = "") {
  return request.post("/room/application", {
    roomId,
    password,
    applyReason,
  });
}

// 获取用户当前所在房间
export function getCurrentRoom() {
  return request.post("/room/current");
}

// 退出房间
export function leaveRoom(roomId) {
  return request.post(`/room/leave/${roomId}`);
}

// 获取用户拥有的房间
export function getOwnedRooms() {
  return request.post("/room/owned");
}

// 房主进入自己的房间
export function enterOwnedRoom(roomId) {
  return request.post(`/room/enter-owned/${roomId}`);
}

// 判断用户是不是房主
export function isOwner(roomId) {
  return request.post(`/room/is-owner/${roomId}`);
}

// AI整合方向
export function AIIntegrateDirection(data) {
  return request.post("/room/generate-ai-content", data);
}
