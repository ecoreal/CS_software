package com.bengebackend.service;

import com.bengebackend.dto.RoomCreateDto;
import com.bengebackend.dto.RoomDto;
import com.bengebackend.entity.room.applyRoomEntity;
import com.bengebackend.entity.room.createRoomEntity;
import com.bengebackend.entity.room.getAllRoomEntity;
import com.bengebackend.model.Room;

import java.util.List;

public interface RoomService {
    RoomCreateDto createRoom(createRoomEntity createRoomEntity);

    List<RoomDto> getAllRooms(getAllRoomEntity getAllRoomsEntity);

    String applyRoom(applyRoomEntity applyRoomEntity);

    String leaveRoom(int roomId);



    Room getUserCurrentRoom();

    List<RoomDto> getUserOwnedRooms();

    String enterOwnedRoom(int roomId);

    boolean isOwner(Integer roomId);
}
