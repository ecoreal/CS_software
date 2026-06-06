package com.bengebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamDelta {
    private String role;
    private String content;
    private String reasoning_content;
    private StreamSecuritySuggest security_suggest;
}
