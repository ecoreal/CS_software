package com.bengebackend.mapper;

import com.bengebackend.model.ScriptHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 剧本历史数据访问层
 */

public interface ScriptHistoryMapper {
    
    /**
     * 根据剧本ID查询历史记录
     */
    List<ScriptHistory> selectByScriptId(Integer scriptId);
    
    /**
     * 插入历史记录
     */
    int insert(ScriptHistory scriptHistory);
    
    /**
     * 根据ID查询历史记录
     */
    ScriptHistory selectById(Integer id);
    
    /**
     * 删除历史记录
     */
    int deleteById(Integer id);
    
    /**
     * 根据剧本ID删除所有历史记录
     */
    int deleteByScriptId(Integer scriptId);
}
