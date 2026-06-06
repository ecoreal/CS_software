package com.bengebackend.dto;

import com.bengebackend.entity.Slogan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SloganResponseDto {
    private List<Slogan> slogans;
}
