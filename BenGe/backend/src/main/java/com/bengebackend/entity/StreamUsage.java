package com.bengebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamUsage {
    private int prompt_tokens;
    private int completion_tokens;
    private int search_prompt_tokens;
    private int total_tokens;
}
