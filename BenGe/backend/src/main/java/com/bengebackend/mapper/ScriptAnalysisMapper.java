package com.bengebackend.mapper;

import com.bengebackend.model.ScriptAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 剧本分析数据访问层
 */

public interface ScriptAnalysisMapper {
    
    /**
     * 根据剧本ID查询分析结果
     */
    ScriptAnalysis selectByScriptId(Integer scriptId);
    
    /**
     * 插入分析结果
     */
    int insert(ScriptAnalysis scriptAnalysis);
    
    /**
     * 更新分析结果
     */
    int updateByScriptId(ScriptAnalysis scriptAnalysis);
    
    /**
     * 根据ID删除分析结果
     */
    int deleteById(Integer id);
    
    /**
     * 根据剧本ID删除分析结果
     */
    int deleteByScriptId(Integer scriptId);
}
