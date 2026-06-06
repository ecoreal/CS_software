package com.bengebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateDto {
    private int roomId;

    private String name;

    private String description;

    private int ownerId;

    private LocalDateTime createdAt;
}
