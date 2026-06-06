package com.bengebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {
    private int roomId;

    private String name;

    private String description;

    private String ownerName;

    private int currentMembers;

    private Boolean havePwd;

}
