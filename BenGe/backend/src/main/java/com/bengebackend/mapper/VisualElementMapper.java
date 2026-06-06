package com.bengebackend.mapper;

import com.bengebackend.model.VisualElement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 可视化元素数据访问层
 */
public interface VisualElementMapper {
    
    /**
     * 根据剧本ID查询可视化元素
     */
    List<VisualElement> selectByScriptId(Integer scriptId);
    
    /**
     * 根据ID查询可视化元素
     */
    VisualElement selectById(Integer id);
    
    /**
     * 插入可视化元素
     */
    int insert(VisualElement visualElement);
    
    /**
     * 更新可视化元素
     */
    int update(VisualElement visualElement);
    
    /**
     * 删除可视化元素
     */
    int deleteById(Integer id);
    
    /**
     * 根据剧本ID删除所有可视化元素
     */
    int deleteByScriptId(Integer scriptId);
}
