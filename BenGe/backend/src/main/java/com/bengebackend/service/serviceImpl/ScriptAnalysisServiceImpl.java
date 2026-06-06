package com.bengebackend.service.serviceImpl;

import com.bengebackend.mapper.ScriptAnalysisMapper;
import com.bengebackend.model.ScriptAnalysis;
import com.bengebackend.service.ScriptAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 剧本分析服务实现类
 */
@Service
public class ScriptAnalysisServiceImpl implements ScriptAnalysisService {
    
    @Autowired
    private ScriptAnalysisMapper scriptAnalysisMapper;
    
    @Override
    public ScriptAnalysis analyzeScript(Integer scriptId, String scriptContent) {
        // 模拟分析逻辑
        ScriptAnalysis analysis = new ScriptAnalysis();
        analysis.setScriptId(scriptId);
        analysis.setAnalysisResult("分析结果：" + scriptContent);
        analysis.setAnalyzedAt(java.time.LocalDateTime.now());
        
        scriptAnalysisMapper.insert(analysis);
        return analysis;
    }

    @Override
    public ScriptAnalysis getByScriptId(Integer scriptId) {
        return scriptAnalysisMapper.selectByScriptId(scriptId);
    }
    
    @Override
    public void saveAnalysis(ScriptAnalysis analysis) {
        if (analysis.getId() == null) {
            scriptAnalysisMapper.insert(analysis);
        } else {
            scriptAnalysisMapper.updateByScriptId(analysis);
        }
    }
}
