package com.bengebackend.service.serviceImpl;

import com.bengebackend.dto.RoomCreateDto;
import com.bengebackend.dto.RoomDto;
import com.bengebackend.entity.room.applyRoomEntity;
import com.bengebackend.entity.room.createRoomEntity;
import com.bengebackend.entity.room.getAllRoomEntity;
import com.bengebackend.mapper.RoomMapper;
import com.bengebackend.model.Room;
import com.bengebackend.service.RoomService;
import com.bengebackend.util.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public List<RoomDto> getAllRooms(getAllRoomEntity getAllRoomsEntity) {
        // 计算分页偏移量
        int offset = (getAllRoomsEntity.getPage() - 1) * getAllRoomsEntity.getLimit();
        // 查询房间列表
        List<RoomDto> roomDtos = roomMapper.getAllRooms(offset, getAllRoomsEntity.getLimit()).stream().filter(room->room.getCurrentMembers()<40).collect(Collectors.toList());

        return roomDtos;
    }

    @Override
    public RoomCreateDto createRoom(createRoomEntity createRoomEntity) {
        int currentId= Context.getCurrentUserId();

        // 检查用户是否在其他房间中，如果是则先退出
        Room currentRoom = roomMapper.getUserCurrentRoom(currentId);
        if (currentRoom != null) {
            // 退出当前房间
            roomMapper.removeRoomMember(currentRoom.getId().intValue(), currentId);
            roomMapper.updateRoomMemberCount(currentRoom.getId().intValue(), -1);
        }

        Room room = new Room();
        room.setName(createRoomEntity.getName());
        room.setDescription(createRoomEntity.getDescription());
        room.setHavePwd(createRoomEntity.isHavePwd());
        room.setPassword(createRoomEntity.getPassword());
        room.setOwnerId((long)currentId);
        room.setCurrentMembers(0); // 初始化参与人数为0，房主加入后会变为1
        roomMapper.createRoom(room);

        RoomCreateDto roomCreateDto = new RoomCreateDto();
        roomCreateDto.setRoomId(room.getId().intValue());
        roomCreateDto.setName(room.getName());
        roomCreateDto.setDescription(room.getDescription());
        roomCreateDto.setOwnerId(currentId);
        roomCreateDto.setCreatedAt(LocalDateTime.now());

        // 添加房主为成员，角色设为房主(2)
        roomMapper.addRoomMember(room.getId().intValue(), currentId, 2);
        roomMapper.updateRoomMemberCount(room.getId().intValue(), 1);

        return roomCreateDto;
    }

    @Override
    public String applyRoom(applyRoomEntity applyRoomEntity) {
        // 1. 验证房间是否存在
        Room room = roomMapper.getRoomById(applyRoomEntity.getRoomId());
        if (room == null) {
            return "房间不存在";
        }

        // 2. 验证密码（如果房间有密码）
        if (room.getHavePwd() && !room.getPassword().equals(applyRoomEntity.getPassword())) {
            return "房间密码错误";
        }

        int currentUserId = Context.getCurrentUserId();

        // 3. 检查用户是否已经在目标房间中
        if (roomMapper.isUserInRoom(applyRoomEntity.getRoomId(), currentUserId)) {
            return "您已经在房间中";
        }

        // 4. 检查用户是否在其他房间中
        Room currentRoom = roomMapper.getUserCurrentRoom(currentUserId);
        if (currentRoom != null) {
            return "您当前在房间\"" + currentRoom.getName() + "\"中，请先退出当前房间";
        }

        // 5. 加入房间
        roomMapper.addRoomMember(applyRoomEntity.getRoomId(), currentUserId, 0);
        roomMapper.updateRoomMemberCount(applyRoomEntity.getRoomId(), 1);

        return "success";
    }

    @Override
    public String leaveRoom(int roomId) {
        int currentUserId = Context.getCurrentUserId();

        // 1. 验证房间是否存在
        Room room = roomMapper.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }

        // 2. 检查用户是否在房间中
        if (!roomMapper.isUserInRoom(roomId, currentUserId)) {
            return "您不在该房间中";
        }

        // 3. 退出房间
        roomMapper.removeRoomMember(roomId, currentUserId);
        roomMapper.updateRoomMemberCount(roomId, -1); // 所有退出都减少成员数

        return "success";
    }



    @Override
    public Room getUserCurrentRoom() {
        int currentUserId = Context.getCurrentUserId();
        return roomMapper.getUserCurrentRoom(currentUserId);
    }

    @Override
    public List<RoomDto> getUserOwnedRooms() {
        int currentUserId = Context.getCurrentUserId();
        return roomMapper.getUserOwnedRooms(currentUserId);
    }

    @Override
    public String enterOwnedRoom(int roomId) {
        int currentUserId = Context.getCurrentUserId();

        // 1. 验证房间是否存在
        Room room = roomMapper.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }

        // 2. 验证是否为房主
        if (!room.getOwnerId().equals((long)currentUserId)) {
            return "您不是该房间的房主";
        }

        // 3. 检查是否已经在房间中
        if (roomMapper.isUserInRoom(roomId, currentUserId)) {
            return "success"; // 已经在房间中，直接返回成功
        }

        // 4. 先退出当前房间（如果有的话）
        Room currentRoom = roomMapper.getUserCurrentRoom(currentUserId);
        if (currentRoom != null) {
            roomMapper.removeRoomMember(currentRoom.getId().intValue(), currentUserId);
            roomMapper.updateRoomMemberCount(currentRoom.getId().intValue(), -1);
        }

        // 5. 进入目标房间
        roomMapper.addRoomMember(roomId, currentUserId, 2); // 房主角色
        roomMapper.updateRoomMemberCount(roomId, 1); // 房主进入房间增加成员数

        return "success";
    }

    @Override
    public boolean isOwner(Integer roomId) {
        // 获取当前用户ID
        int userId = Context.getCurrentUserId();  // 假设这是获取当前用户ID的方式
        // 调用 Mapper 检查是否为房主
        Boolean result =  roomMapper.checkIfOwner(roomId, userId);
        return Boolean.TRUE.equals(result);
    }
}
