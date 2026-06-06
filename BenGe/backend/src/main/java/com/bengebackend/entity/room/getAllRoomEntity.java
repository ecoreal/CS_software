package com.bengebackend.entity.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class getAllRoomEntity {
    private int page;
    private int limit;
}
