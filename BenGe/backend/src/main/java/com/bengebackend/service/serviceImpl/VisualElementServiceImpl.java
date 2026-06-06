package com.bengebackend.service.serviceImpl;

import com.bengebackend.mapper.VisualElementMapper;
import com.bengebackend.model.VisualElement;
import com.bengebackend.service.VisualElementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 可视化元素服务实现类
 */
@Service
public class VisualElementServiceImpl implements VisualElementService {

    @Autowired
    private VisualElementMapper visualElementMapper;

    @Override
    public List<VisualElement> getAllElements(Integer scriptId) {
        return visualElementMapper.selectByScriptId(scriptId);
    }

    @Override
    public void updateVisualElements(Integer scriptId, List<VisualElement> newElements) {
        // 删除旧元素
        visualElementMapper.deleteByScriptId(scriptId);
        // 插入新元素
        for (VisualElement element : newElements) {
            element.setScriptId(scriptId);
            visualElementMapper.insert(element);
        }
    }

    @Override
    public VisualElement getElementById(Integer elementId) {
        return visualElementMapper.selectById(elementId);
    }

    @Override
    public void updateElementUrl(Integer elementId, String url) {
        VisualElement element = visualElementMapper.selectById(elementId);
        if (element != null) {
            element.setImageUrl(url);
            element.setImageGeneratedAt(java.time.LocalDateTime.now());
            visualElementMapper.update(element);
        }
    }
}
