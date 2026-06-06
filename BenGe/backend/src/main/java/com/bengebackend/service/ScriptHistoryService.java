package com.bengebackend.service;

import com.bengebackend.model.ScriptHistory;

import java.util.List;

/**
 * 剧本历史服务接口
 */
public interface ScriptHistoryService {
    
    /**
     * 添加历史记录
     */
    void addHistory(ScriptHistory history);
    
    /**
     * 根据剧本ID获取历史记录
     */
    List<ScriptHistory> getHistoryByScript(Integer scriptId);
}
