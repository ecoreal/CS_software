#!/usr/bin/env node

/**
 * Y.js WebSocket 服务器启动脚本
 * 用于支持多人实时协同编辑功能
 */

const http = require('http');
const WebSocket = require('ws');
const { setupWSConnection } = require('y-websocket/bin/utils');

const host = process.env.HOST || 'localhost';
const port = process.env.PORT || 1234;


const server = http.createServer((request, response) => {
  response.writeHead(200, { 'Content-Type': 'text/plain' });
  response.end('Y.js WebSocket 服务器正在运行\n');
});

const wss = new WebSocket.Server({ server });

wss.on('connection', (ws, req) => {
  const url = new URL(req.url, `http://${req.headers.host}`);
  const roomname = url.pathname.slice(1); 

  // console.log(`👥 新用户加入房间: ${roomname}`);

  setupWSConnection(ws, req, {
    docName: roomname,
    gc: true 
  });

  ws.on('close', () => {
    // console.log(`👋 用户离开房间: ${roomname}`);
  });
});

server.listen(port, host, () => {
  // console.log('💡 使用说明:');
  // console.log('  - 每个房间对应一个独立的文档');
  // console.log('  - 房间名格式: room-{roomId}');
  // console.log('  - 支持多用户实时协同编辑');
  // console.log('  - 自动同步和冲突解决');
});

process.on('SIGINT', () => {
  server.close(() => {
    process.exit(0);
  });
});

process.on('SIGTERM', () => {
  server.close(() => {
    process.exit(0);
  });
});
