package com.bengebackend.service;

import com.bengebackend.model.VisualElement;

import java.util.List;

/**
 * 可视化元素服务接口
 */
public interface VisualElementService {
    
    /**
     * 获取剧本的所有可视化元素
     */
    List<VisualElement> getAllElements(Integer scriptId);
    
    /**
     * 更新可视化元素
     */
    void updateVisualElements(Integer scriptId, List<VisualElement> newElements);
    
    /**
     * 根据ID获取可视化元素
     */
    VisualElement getElementById(Integer elementId);
    
    /**
     * 更新元素的图片URL
     */
    void updateElementUrl(Integer elementId, String url);
}
