/**
 * 统一的上下文数据收集工具
 * 用于为AI功能提供完整、一致的上下文信息
 */

import { useCanvasStore } from '@/stores/canvasStore'
import { useCharacterStore } from '@/stores/character'
import { useClueStore } from '@/stores/clue'
import { useAtmosphereStore } from '@/stores/atmosphere'
import { socketState } from '@/stores/socket'

/**
 * 节点类型映射
 */
const NODE_TYPES = {
  SCENE: 'custom',
  CHARACTER: 'character', 
  CLUE: 'clue',
  INFERENCE: 'inference',
  PERSON: 'person',
  ATMOSPHERE: 'atmosphere'
}

const getNodeSummary = (node) => {
  if (!node || !node.data) return '无数据'
  
  switch(node.type) {
    case NODE_TYPES.SCENE: // 场景节点
      return `场景: ${node.data.sceneDescription || node.data.title || ''}, 时间: ${node.data.timeLabel || ''}, 涉及角色: ${node.data.characters || ''}`
      
    case NODE_TYPES.CHARACTER: // 角色节点
      const personality = Array.isArray(node.data.personality) ? 
        node.data.personality.join(', ') : (node.data.personality || '')
      return `角色: ${node.data.name || ''}, 职业: ${node.data.occupation || ''}, 性格: ${personality}`
      
    case NODE_TYPES.ATMOSPHERE: // 氛围节点
      return `氛围: ${node.data.mood || ''}, 灯光: ${node.data.lighting || ''}, 音乐: ${node.data.music || ''}, 天气: ${node.data.weather || ''}`
      
    case NODE_TYPES.CLUE: // 线索节点
      return `线索: ${node.data.detail || node.data.title || ''}, 相关事件: ${node.data.relatedEvent || ''}`
      
    case NODE_TYPES.INFERENCE: // 推理节点
      return `推理: ${node.data.summary || node.data.title || ''}, 证据: ${node.data.evidence || ''}, 逻辑: ${node.data.logic || ''}`
      
    case NODE_TYPES.PERSON: // 人物节点
      return `人物: ${node.data.name || node.data.title || ''}, 简介: ${node.data.bio || node.data.background || ''}`
      
    default:
      // 对于未知类型，尝试提取通用字段
      const title = node.data.title || node.data.name || '未命名'
      const desc = node.data.description || node.data.detail || node.data.background || ''
      return `${title}: ${desc.substring(0, 100)}`
  }
}

const filterChatMessages = (messages) => {
  if (!Array.isArray(messages)) return []

  return messages
    .filter(msg => {
      // 只过滤掉系统加入/离开消息，保留所有其他消息（包括AI消息和用户对话）
      if (msg.isSystem) {
        const content = msg.content || ''
        // 过滤加入/离开消息
        if (content.includes('加入了房间') || content.includes('离开了房间')) {
          return false
        }
      }
      return true
    })
    .map(msg => ({
      user: msg.username || msg.sender || '匿名用户',
      text: msg.content || '',
      time: msg.time || '',
      isAI: msg.isAI || false,
      isSystem: msg.isSystem || false
    }))
}


const collectAllNodes = () => {
  const canvasStore = useCanvasStore()
  const characterStore = useCharacterStore()
  const clueStore = useClueStore()
  const atmosphereStore = useAtmosphereStore()
  
  const nodesByType = {
    scene: [],
    character: [],
    clue: [],
    inference: [],
    person: [],
    atmosphere: []
  }
  
  // 收集画布节点（场景节点）
  if (canvasStore.nodes) {
    canvasStore.nodes.forEach(node => {
      const nodeData = {
        id: node.id,
        type: node.type,
        position: node.position,
        title: node.data?.title || node.data?.name || '未命名',
        summary: getNodeSummary(node),
        data: { ...node.data }
      }
      
      if (node.type === NODE_TYPES.SCENE) {
        nodesByType.scene.push(nodeData)
      } else if (nodesByType[node.type]) {
        nodesByType[node.type].push(nodeData)
      }
    })
  }
  
  // 收集角色节点
  if (characterStore.nodes) {
    characterStore.nodes.forEach(node => {
      nodesByType.character.push({
        id: node.id,
        type: node.type,
        position: node.position,
        title: node.data?.name || '未命名角色',
        summary: getNodeSummary(node),
        data: { ...node.data }
      })
    })
  }
  
  // 收集线索相关节点
  if (clueStore.nodes) {
    clueStore.nodes.forEach(node => {
      const nodeData = {
        id: node.id,
        type: node.type,
        position: node.position,
        title: node.data?.title || node.data?.name || '未命名',
        summary: getNodeSummary(node),
        data: { ...node.data }
      }
      
      switch(node.type) {
        case NODE_TYPES.CLUE:
          nodesByType.clue.push(nodeData)
          break
        case NODE_TYPES.INFERENCE:
          nodesByType.inference.push(nodeData)
          break
        case NODE_TYPES.PERSON:
          nodesByType.person.push(nodeData)
          break
        default:
          // 未知类型添加到线索分类
          nodesByType.clue.push(nodeData)
      }
    })
  }
  
  // 收集氛围节点
  if (atmosphereStore.nodes) {
    atmosphereStore.nodes.forEach(node => {
      nodesByType.atmosphere.push({
        id: node.id,
        type: node.type,
        position: node.position,
        title: node.data?.title || '未命名氛围',
        summary: getNodeSummary(node),
        data: { ...node.data }
      })
    })
  }
  
  return nodesByType
}

/**
 * 收集所有边数据，按类型分组
 * @returns {Object} 按类型分组的边数据
 */
const collectAllEdges = () => {
  const canvasStore = useCanvasStore()
  const characterStore = useCharacterStore()
  const clueStore = useClueStore()
  const atmosphereStore = useAtmosphereStore()
  
  const edgesByType = {
    scene: [],
    character: [],
    clue: [],
    atmosphere: []
  }
  
  // 收集画布边（场景边）
  if (canvasStore.edges) {
    canvasStore.edges.forEach(edge => {
      const edgeData = {
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.type,
        relationship: edge.data?.relationship || edge.data?.label || '连接',
        description: edge.data?.description || '',
        data: { ...edge.data }
      }
      
      edgesByType.scene.push(edgeData)
    })
  }
  
  // 收集角色关系边
  if (characterStore.edges) {
    characterStore.edges.forEach(edge => {
      // 为角色-场景关系生成更清晰的relationship描述
      let relationship = edge.data?.relationship || edge.data?.label || '关系'
      if (edge.type === 'character-scene' && edge.data?.participationType) {
        const typeMap = {
          'protagonist': '主角参与',
          'supporting': '配角参与',
          'antagonist': '反派参与',
          'witness': '见证者',
          'victim': '受害者',
          'helper': '帮助者',
          'obstacle': '阻碍者',
          'other': '其他参与'
        }
        relationship = typeMap[edge.data.participationType] || '参与'
      }
      
      edgesByType.character.push({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.type,
        relationship: relationship,
        description: edge.data?.description || '',
        strength: edge.data?.strength || '',
        data: { ...edge.data }
      })
    })
  }
  
  // 收集线索边
  if (clueStore.edges) {
    clueStore.edges.forEach(edge => {
      edgesByType.clue.push({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.type,
        relationship: edge.data?.relationship || edge.data?.label || '关联',
        description: edge.data?.description || '',
        data: { ...edge.data }
      })
    })
  }
  
  // 收集氛围边
  if (atmosphereStore.edges) {
    atmosphereStore.edges.forEach(edge => {
      edgesByType.atmosphere.push({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.type,
        relationship: edge.data?.relationship || edge.data?.label || '氛围影响',
        description: edge.data?.description || '',
        data: { ...edge.data }
      })
    })
  }
  
  return edgesByType
}

/**
 * 收集上下文信息
 * @returns {Object} 上下文信息
 */
const collectContextInfo = () => {
  return {
    room: {
      id: socketState.roomId,
      members: socketState.members?.length || 0,
      stage: '第二阶段协作设计'
    },
    user: {
      id: socketState.currentUserId,
      name: socketState.currentUsername,
      role: socketState.userRole >= 0 ? socketState.roles[socketState.userRole]?.name : '未分配'
    }
  }
}

/**
 * 计算数据统计信息
 * @param {Object} nodes - 节点数据
 * @param {Object} edges - 边数据
 * @param {Array} chat - 聊天数据
 * @returns {Object} 统计信息
 */
const calculateStatistics = (nodes, edges, chat) => {
  const nodeCount = Object.values(nodes).reduce((sum, nodeArray) => sum + nodeArray.length, 0)
  const edgeCount = Object.values(edges).reduce((sum, edgeArray) => sum + edgeArray.length, 0)
  const chatCount = chat.length
  
  return {
    nodeCount,
    edgeCount,
    chatCount,
    nodesByType: Object.fromEntries(
      Object.entries(nodes).map(([type, nodeArray]) => [type, nodeArray.length])
    ),
    edgesByType: Object.fromEntries(
      Object.entries(edges).map(([type, edgeArray]) => [type, edgeArray.length])
    )
  }
}

/**
 * 主要的上下文数据收集函数
 * 收集所有完整的上下文信息，包括聊天记录、所有类型的节点和边数据
 * @returns {Object} 完整的上下文数据
 */
export const collectContextData = () => {
  try {
    // 收集完整的聊天数据
    const chat = filterChatMessages(socketState.messages)

    // 收集所有节点数据
    const nodes = collectAllNodes()

    // 收集所有边数据
    const edges = collectAllEdges()

    // 收集上下文信息
    const context = collectContextInfo()

    // 计算统计信息
    const statistics = calculateStatistics(nodes, edges, chat)
    context.statistics = statistics

    return {
      chat,
      nodes,
      edges,
      context,
      // 兼容旧版本的字段
      chatCount: statistics.chatCount,
      nodeCount: statistics.nodeCount,
      characterCount: statistics.nodesByType.character || 0
    }
  } catch (error) {
    // console.error('收集上下文数据时发生错误:', error)
    return {
      chat: [],
      nodes: {},
      edges: {},
      context: {
        room: { id: null, members: 0, stage: '未知' },
        user: { id: null, name: '未知', role: '未分配' },
        statistics: { nodeCount: 0, edgeCount: 0, chatCount: 0 }
      },
      chatCount: 0,
      nodeCount: 0,
      characterCount: 0
    }
  }
}



/**
 * 验证收集的数据完整性
 * @param {Object} contextData - 收集的上下文数据
 * @returns {Object} 验证结果
 */
export const validateContextData = (contextData) => {
  const issues = []
  const warnings = []
  
  if (!contextData) {
    issues.push('上下文数据为空')
    return { isValid: false, issues, warnings }
  }
  
  // 检查必要字段
  if (!contextData.chat) warnings.push('缺少聊天数据')
  if (!contextData.nodes) warnings.push('缺少节点数据')
  if (!contextData.edges) warnings.push('缺少边数据')
  if (!contextData.context) warnings.push('缺少上下文信息')
  
  // 检查数据质量
  if (contextData.chat && contextData.chat.length === 0) {
    warnings.push('聊天记录为空')
  }
  
  const totalNodes = Object.values(contextData.nodes || {}).reduce((sum, arr) => sum + arr.length, 0)
  if (totalNodes === 0) {
    warnings.push('没有收集到任何节点数据')
  }
  
  return {
    isValid: issues.length === 0,
    issues,
    warnings,
    statistics: contextData.context?.statistics
  }
}

export default {
  collectContextData,
  validateContextData,
  NODE_TYPES
}
