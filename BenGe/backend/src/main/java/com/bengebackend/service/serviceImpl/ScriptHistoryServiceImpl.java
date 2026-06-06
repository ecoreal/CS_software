package com.bengebackend.service.serviceImpl;

import com.bengebackend.mapper.ScriptHistoryMapper;
import com.bengebackend.model.ScriptHistory;
import com.bengebackend.service.ScriptHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 剧本历史服务实现类
 */
@Service
public class ScriptHistoryServiceImpl implements ScriptHistoryService {
    
    @Autowired
    private ScriptHistoryMapper scriptHistoryMapper;
    
    @Override
    public void addHistory(ScriptHistory history) {
        scriptHistoryMapper.insert(history);
    }

    @Override
    public List<ScriptHistory> getHistoryByScript(Integer scriptId) {
        return scriptHistoryMapper.selectByScriptId(scriptId);
    }
}
