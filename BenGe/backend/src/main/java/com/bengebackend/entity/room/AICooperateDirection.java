package com.bengebackend.entity.room;

import lombok.Data;

import java.util.List;

@Data
public class AICooperateDirection {
    private List<String> keyWords;

    private String roomId;
}
