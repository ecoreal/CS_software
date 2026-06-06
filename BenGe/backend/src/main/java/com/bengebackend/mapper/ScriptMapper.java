package com.bengebackend.mapper;

import com.bengebackend.model.Script;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 剧本数据访问层
 */

public interface ScriptMapper {
    
    /**
     * 根据ID查询剧本
     */
    Script selectById(Integer id);
    
    /**
     * 根据用户ID查询剧本列表
     */
    List<Script> selectByUserId(Integer userId);
    
    /**
     * 插入新剧本
     */
    int insert(Script script);
    
    /**
     * 更新剧本
     */
    int update(@Param("id") Integer id, @Param("title") String title, 
               @Param("content") String content, @Param("stage") Integer stage,
               @Param("lastUpdated") LocalDateTime lastUpdated);
    
    /**
     * 软删除剧本
     */
    int deleteById(Integer id);
}
