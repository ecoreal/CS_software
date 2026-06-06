package com.bengebackend.service;

import com.bengebackend.model.ScriptAnalysis;

/**
 * 剧本分析服务接口
 */
public interface ScriptAnalysisService {
    
    /**
     * 分析剧本
     */
    ScriptAnalysis analyzeScript(Integer scriptId, String scriptContent);
    
    /**
     * 根据剧本ID获取分析结果
     */
    ScriptAnalysis getByScriptId(Integer scriptId);
    
    /**
     * 保存分析结果
     */
    void saveAnalysis(ScriptAnalysis analysis);
}
