package com.bengebackend.service;

import com.bengebackend.dto.ScriptDetailDto;
import com.bengebackend.dto.ScriptFrameworkDto;
import com.bengebackend.entity.ScriptReplyRequestEntity;
import com.bengebackend.model.Script;
import com.bengebackend.model.ScriptAnalysis;
import com.bengebackend.model.ScriptHistory;

import java.util.List;

/**
 * 剧本服务接口
 */
public interface ScriptService {

  /**
   * 根据ID获取剧本详情
   */
  ScriptDetailDto getScriptByIdAsync(Integer id);

  /**
   * 获取用户的剧本列表
   */
  List<Script> getUserScriptsAsync(Integer userId);

  /**
   * 创建剧本
   */
  Script createScriptAsync(Script script);

  /**
   * 更新剧本
   */
  void updateScriptAsync(Integer scriptId, String title, String content, Integer stage);

  /**
   * 删除剧本
   */
  void deleteScriptAsync(Integer id);

  /**
   * 初始化新剧本
   */
  ScriptDetailDto initializeScriptAsync(Integer userId);

  /**
   * 生成剧本框架
   */
  ScriptFrameworkDto genFrame(ScriptReplyRequestEntity request, List<ScriptHistory> history, String scriptContent);

  /**
   * 分析剧本内容
   */
  ScriptAnalysis analyzeScriptContent(String scriptContent, Integer scriptId);

  /**
   * 获取完整剧本和描述
   * 注意：方法名保持与C#一致，包含拼写错误
   */
  ScriptDetailDto getCompSctiptAndDesc(Script script);

  /**
   * 可视化剧本元素
   */
  String visualizeScriptAsync(Integer scriptId, Integer elementId);
}
