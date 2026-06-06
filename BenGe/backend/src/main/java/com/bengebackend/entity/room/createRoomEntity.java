package com.bengebackend.entity.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class createRoomEntity {
    private String name;

    private String description;

    private boolean havePwd;

    private String password;
}
