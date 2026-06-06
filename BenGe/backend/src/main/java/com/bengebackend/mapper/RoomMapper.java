package com.bengebackend.mapper;

import com.bengebackend.dto.RoomDto;
import com.bengebackend.model.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMapper {

    int createRoom(Room room);

    List<RoomDto> getAllRooms(@Param("offset") int offset, @Param("limit") int limit);

    Room getRoomById(@Param("roomId") int roomId);

    boolean isUserInRoom(@Param("roomId") int roomId, @Param("userId") int userId);

    int addRoomMember(@Param("roomId") int roomId, @Param("userId") int userId, @Param("role") int role);

    int updateRoomMemberCount(@Param("roomId") int roomId, @Param("increment") int increment);

    int removeRoomMember(@Param("roomId") int roomId, @Param("userId") int userId);



    Room getUserCurrentRoom(@Param("userId") int userId);

    List<RoomDto> getUserOwnedRooms(@Param("userId") int userId);

    Boolean checkIfOwner(@Param("roomId") Integer roomId, @Param("userId") Integer userId);
}
