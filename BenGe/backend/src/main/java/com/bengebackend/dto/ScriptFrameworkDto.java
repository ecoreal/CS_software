package com.bengebackend.dto;

import com.bengebackend.model.Script;
import com.bengebackend.model.ScriptHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptFrameworkDto {

    private Script script;

    private List<ScriptHistory> dialogHistory;
}
