package com.bengebackend.dto;

import com.bengebackend.model.Script;
import com.bengebackend.model.ScriptAnalysis;
import com.bengebackend.model.ScriptHistory;
import com.bengebackend.model.VisualElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptDetailDto {
    private Script script;

    private List<ScriptHistory> history;

    private ScriptAnalysis analysis;

    private List<VisualElement> visualElements;
}
