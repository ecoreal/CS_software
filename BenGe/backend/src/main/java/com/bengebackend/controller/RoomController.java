package com.bengebackend.controller;

import com.bengebackend.dto.CollaborationScriptDto;
import com.bengebackend.dto.RoomCreateDto;
import com.bengebackend.dto.RoomDto;
import com.bengebackend.entity.CollaborationScriptRequestEntity;
import com.bengebackend.entity.room.AICooperateDirection;
import com.bengebackend.entity.room.applyRoomEntity;
import com.bengebackend.entity.room.createRoomEntity;
import com.bengebackend.entity.room.getAllRoomEntity;
import com.bengebackend.model.Room;
import com.bengebackend.service.CoopAIService;
import com.bengebackend.service.RoomService;
import com.bengebackend.websocket.message.WebSocketMessage;
import com.bengebackend.websocket.session.RoomManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/room")
@Slf4j
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private final RedissonClient redissonClient;

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private CoopAIService aiService;

    public RoomController(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 创建房间
     *
     * @param createRoomEntity 包含房间信息的实体类
     * @return 创建结果的响应实体
     */
    @PostMapping
    public ResponseEntity<RoomCreateDto> createRoom(@RequestBody createRoomEntity createRoomEntity) {
        log.debug("createRoomEntity: {}", createRoomEntity);
        RoomCreateDto roomDto = roomService.createRoom(createRoomEntity);
        return ResponseEntity.ok(roomDto);
    }

    /**
     * 获取所有房间
     *
     * @param getAllRoomsEntity 包含分页信息的实体类
     * @return 房间列表的响应实体
     */
    @PostMapping("/list")
    public ResponseEntity<List<RoomDto>> getAllRooms(@RequestBody getAllRoomEntity getAllRoomsEntity) {
        log.debug("getAllRoomsEntity: {}", getAllRoomsEntity);
        List<RoomDto> roomDtos = roomService.getAllRooms(getAllRoomsEntity);
        return ResponseEntity.ok(roomDtos);
    }

    @PostMapping("/application")
    public ResponseEntity<String> applyRoom(@RequestBody applyRoomEntity applyRoomEntity) {
        log.debug("applyRoomEntity: {}", applyRoomEntity);
        String result = roomService.applyRoom(applyRoomEntity);
        return ResponseEntity.ok(result);
    }

    /**
     * 退出房间
     *
     * @param roomId 房间ID
     * @return 退出结果的响应实体
     */
    @PostMapping("/leave/{roomId}")
    public ResponseEntity<String> leaveRoom(@PathVariable int roomId) {
        log.debug("退出房间，房间ID: {}", roomId);
        String result = roomService.leaveRoom(roomId);
        return ResponseEntity.ok(result);
    }



    /**
     * 获取用户当前所在房间
     *
     * @return 当前房间信息
     */
    @PostMapping("/current")
    public ResponseEntity<Room> getCurrentRoom() {
        Room currentRoom = roomService.getUserCurrentRoom();
        return ResponseEntity.ok(currentRoom);
    }

    /**
     * 获取用户拥有的房间列表
     *
     * @return 用户拥有的房间列表
     */
    @PostMapping("/owned")
    public ResponseEntity<List<RoomDto>> getOwnedRooms() {
        List<RoomDto> ownedRooms = roomService.getUserOwnedRooms();
        return ResponseEntity.ok(ownedRooms);
    }

    @PostMapping("/enter-owned/{roomId}")
    public ResponseEntity<String> enterOwnedRoom(@PathVariable int roomId) {
        try {
            String result = roomService.enterOwnedRoom(roomId);
            if ("success".equals(result)) {
                return ResponseEntity.ok("进入房间成功");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("进入房间失败");
        }
    }

    @PostMapping("/is-owner/{roomId}")
    public ResponseEntity<Boolean> isOwner(@PathVariable Integer roomId) {
        // 调用 Service 获取当前用户是否是房主
        boolean isOwner = roomService.isOwner(roomId);
        return ResponseEntity.ok(isOwner);
    }

    /**
     * 广播AI产生的方向
     */
    @PostMapping("/generate-ai-content")
    public ResponseEntity<?> generateDirection(@RequestBody AICooperateDirection aiCooperateDirection){
        String roomId = aiCooperateDirection.getRoomId();
        RLock lock = redissonClient.getLock("room:" + roomId);
        log.info("testtest");

        boolean locked = false;
        try {
            locked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!locked) {
                return ResponseEntity.status(429).body("正在处理中，请稍候");
            }

            // 调用AI接口，返回 List<Map<String, String>>
            List<Map<String, String>> result = aiService.getCoopDirection(aiCooperateDirection.getKeyWords());

            log.info("AI结果：" + result);
            // 广播：
            WebSocketMessage msg = new WebSocketMessage();
            msg.setType("vote");
            msg.setRoomId(roomId);
            msg.setContent(objectMapper.writeValueAsString(result));
            roomManager.broadcastToRoom(roomId, objectMapper.writeValueAsString(msg));

            // 设置标志，10分钟过期
            redisTemplate.opsForValue().set("ai_done:" + roomId, true, 10, TimeUnit.MINUTES);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("生成剧本方向失败", e);
            return ResponseEntity.status(500).body("生成失败");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 第二阶段到第三阶段，整合内容生成剧本
     * @param request
     * @return
     */
    @PostMapping("/collaboration/generate")
    public ResponseEntity<CollaborationScriptDto> generateCollaborationScript(@RequestBody CollaborationScriptRequestEntity request) {
        log.info(request.toString());

        try {
            Integer roomId = request.getRoomId();

            String contextData = request.getContextData();


            String generatedScript = aiService.getCompleteScript(contextData);
//            String generatedScript = "# 这是AI成功返回的测试文本，恭喜你成功了!";

            log.info(generatedScript);

            if (generatedScript != null && !generatedScript.trim().isEmpty()) {
                // 广播：
                WebSocketMessage msg = new WebSocketMessage();
                msg.setType("canvas");
                msg.setRoomId(String.valueOf(roomId));
                msg.setContent(generatedScript);
                roomManager.broadcastToRoom(String.valueOf(roomId), objectMapper.writeValueAsString(msg));

                String title = "房间" + request.getRoomId() + "协作剧本";
                return ResponseEntity.ok(new CollaborationScriptDto(title, generatedScript, "剧本生成成功", true));
            } else {
                return ResponseEntity.status(500).body(new CollaborationScriptDto(null, null, "AI生成剧本失败", false));
            }
        } catch (Exception e) {
            System.err.println("生成协作剧本失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new CollaborationScriptDto(null, null, "服务器错误: " + e.getMessage(), false));
        }
    }

    /**
     * 房主确认进入第三阶段
     */
    @PostMapping("/enter-third-stage")
    public void enterThirdStage(Integer roomId,String data){
        // 广播：
        try {
            WebSocketMessage msg = new WebSocketMessage();
            msg.setType("enter-third-stage");
            msg.setRoomId(String.valueOf(roomId));
            msg.setContent(data);
            roomManager.broadcastToRoom(String.valueOf(roomId), objectMapper.writeValueAsString(msg));
        }catch (Exception error){
            error.printStackTrace();
        }
    }

    /**
     * AI生成结点
     */
    @PostMapping("/generate-nodes")
    public ResponseEntity<Map<String, Object>> generateNodes(@RequestBody Map<String, Object> request) {
        log.info("原始request:{}", request);
        try {
            String userInput = (String) request.get("userInput");
            String designerType = (String) request.get("designerType");
            String contextData = (String) request.get("contextData");

            log.info("AI生成请求: 类型={}, 输入={}, 上下文={}", designerType, userInput, contextData);

            List<Map<String, Object>> nodes = aiService.generateNodes(userInput, designerType, null, contextData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("nodes", nodes);
            response.put("message", "成功生成" + nodes.size() + "个节点");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("AI生成失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "生成失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}
