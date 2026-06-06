package com.bengebackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private Long id;

    private String name;

    private String description;

    private Boolean havePwd;

    private String password;

    private Long ownerId;

    private Integer currentMembers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
