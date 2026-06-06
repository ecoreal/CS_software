package com.bengebackend.entity.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class applyRoomEntity {
    private int roomId;

    private String applyReason;

    private String password;
}
