package com.bengebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamResponse {
    private int code;
    private String message;
    private String sid;
    private String id;
    private long created;
    private List<StreamChoice> choices;
    private StreamUsage usage;
}
