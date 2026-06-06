// ws-server.js 用来测试聊天区的消息转发功能
const WebSocket = require('ws');
const wss = new WebSocket.Server({host: '0.0.0.0', port: 8084 });

const rooms = new Map();

wss.on('connection', (ws) => {
  let currentRoom = null;

  ws.on('message', (message) => {
    const msg = JSON.parse(message);

    if (msg.type === 'join') {
      currentRoom = msg.roomId;
      if (!rooms.has(currentRoom)) rooms.set(currentRoom, new Set());
      rooms.get(currentRoom).add(ws);
    }

    if (msg.type === 'chat') {
      rooms.get(currentRoom)?.forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
          client.send(JSON.stringify(msg));
        }
      });
    }
  });

  ws.on('close', () => {
    if (currentRoom) {
      rooms.get(currentRoom)?.delete(ws);
    }
  });
});
