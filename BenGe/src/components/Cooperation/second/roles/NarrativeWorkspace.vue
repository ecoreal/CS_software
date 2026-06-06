<template>
  <div class="workspace-container">
    <!-- 悬浮工具球 -->
    <FloatingToolball v-if="socketState.userRole == 0" @add-node="canvasStore.handleAddNode"
      @add-edge="canvasStore.handleCreateEdgeClick" @export-canvas="handleExportAs" @ai-generate="handleAiGenerate"
      @ai-integrate="handleAiIntegrate" />
    <CharacterToolbar v-if="socketState.userRole == 1" @add-character="characterStore.handleAddNode"
      @add-relationship="characterStore.handleCreateEdgeClick" @export-canvas="handleExportAs"
      @ai-generate="handleCharacterAiGenerate" @ai-integrate="handleAiIntegrate" />
    <ClueToolbar v-if="socketState.userRole == 2" @add-clue="clueStore.handleAddClueNode"
      @add-inference="clueStore.handleAddInferenceNode" @add-person="clueStore.handleAddPersonNode"
      @add-relationship="clueStore.handleCreateEdgeClick" @export-canvas="handleExportAs"
      @ai-generate="handleClueAiGenerate" @ai-integrate="handleAiIntegrate" />
    <AtmosphereToolbar v-if="socketState.userRole == 3" @add-node="atmosphereStore.handleAddNode"
      @atmo-palette="handleAtmospherePalette" @link-scene="handleLinkScene" @export-canvas="handleExportAs"
      @ai-generate="handleAtmosphereAiGenerate" @ai-integrate="handleAiIntegrate" />

    <AiIntegrate v-if="ShowAiIntegrate && !loadingStore.loading3" :AIcontent="AIContent"
      @close="ShowAiIntegrate = false" @select="$emit('select')" />

    <!-- 主画布 -->
    <CanvasArea ref="canvasRef" v-if="nodes.length > 0" :nodes="nodes" :edges="edges" @delete-node="handleDeleteNode"
      @node-select="handleNodeClick" @edge-select="handleEdgeSelect" @node-position-change="handleNodePositionChange"
      @connect-node="handleConnectNode" />

    <!-- 节点详情抽屉 -->
    <div style="height: 100%; width: 100%">
      <NodeDetailDrawer :visible="canvasStore.selectedNode" :nodeData="canvasStore.selectedNode"
        @save="handleDetailSave" @close="canvasStore.selectedNode = null" />

      <EdgeTypeSelector v-if="canvasStore.showEdgeSelector" :source="canvasStore.selectedNodesForEdge[0]"
        :target="canvasStore.selectedNodesForEdge[1]" @confirm="canvasStore.handleEdgeConfirm"
        @cancel="canvasStore.handleEdgeCancel" />

      <!-- 人物-场景边编辑器 -->
      <CharacterSceneEdgeEditor
        v-if="characterStore.showEdgeSelector && characterStore.editingEdgeId && isCharacterSceneEdge()"
        :source="getCharacterEdgeSourceNode()" :target="getCharacterEdgeTargetNode()"
        :initialParticipationType="getCurrentCharacterEdge()?.data?.participationType || 'protagonist'"
        :initialImportance="getCurrentCharacterEdge()?.data?.importance || 'normal'"
        :initialDescription="getCurrentCharacterEdge()?.data?.description || ''"
        :initialLabel="getCurrentCharacterEdge()?.data?.label || ''"
        :initialStyle="getCurrentCharacterEdge()?.data?.style || 'solid'" :showDelete="true"
        @confirm="handleCharacterSceneEdgeEditConfirm" @cancel="characterStore.handleEdgeCancel"
        @delete-edge="characterStore.handleDeleteEdge" />

      <!-- 普通边编辑器 -->
      <EdgeTypeSelector v-if="canvasStore.showEdgeSelector && canvasStore.editingEdgeId && !isCharacterSceneEdge()"
        :initialType="edges?.find((e) => e.id === canvasStore.editingEdgeId)?.data?.type ||
          null
          " :initialLabel="edges?.find((e) => e.id === canvasStore.editingEdgeId)?.data?.label ||
          ''
          " :showDelete="true" @confirm="canvasStore.handleEdgeEditConfirm" @cancel="canvasStore.handleEdgeCancel"
        @delete-edge="canvasStore.handleDeleteEdge" />

      // 角色设计师
      <CharacterDetailPanel :visible="characterStore.selectedNode" :nodeData="characterStore.selectedNode"
        @save="handleDetailSave" @close="characterStore.selectedNode = null" />

      <!-- 氛围设计师 -->
      <AtmosphereDetailPanel :visible="atmosphereStore.selectedNode" :nodeData="atmosphereStore.selectedNode"
        @save="handleDetailSave" @close="atmosphereStore.selectedNode = null" />

      <!-- 角色关系编辑器 -->
      <CharacterRelationEditor
        v-if="characterStore.showEdgeSelector && !characterStore.editingEdgeId && characterStore.edgeType === 'relationship'"
        :source="characterStore.selectedNodesForEdge[0]" :target="characterStore.selectedNodesForEdge[1]"
        @confirm="characterStore.handleEdgeConfirm" @cancel="characterStore.handleEdgeCancel" />

      <!-- 角色-场景关系编辑器 -->
      <CharacterSceneEdgeEditor
        v-if="characterStore.showEdgeSelector && !characterStore.editingEdgeId && characterStore.edgeType === 'character-scene'"
        :source="characterStore.selectedNodesForEdge[0]" :target="characterStore.selectedNodesForEdge[1]"
        @confirm="characterStore.handleCharacterSceneEdgeConfirm" @cancel="characterStore.handleEdgeCancel" />

      <!-- 编辑现有关系 -->
      <CharacterRelationEditor v-if="characterStore.showEdgeSelector && characterStore.editingEdgeId"
        :source="getCharacterEdgeSourceNode()" :target="getCharacterEdgeTargetNode()"
        :initialType="getCurrentCharacterEdge()?.data?.type || ''"
        :initialDescription="getCurrentCharacterEdge()?.data?.description || ''"
        :initialStrength="getCurrentCharacterEdge()?.data?.strength || 5"
        :initialStatus="getCurrentCharacterEdge()?.data?.status || 'active'" :showDelete="true"
        @confirm="characterStore.handleEdgeEditConfirm" @cancel="characterStore.handleEdgeCancel"
        @delete-relation="characterStore.handleDeleteEdge" />

      // 线索设计师
      <ClueDetailPanel :visible="clueStore.selectedNode?.type == 'clue' || false" :clueData="clueStore.selectedNode"
        @save="handleDetailSave" @close="clueStore.selectedNode = null" />
      <InferenceDetailPanel :visible="clueStore.selectedNode?.type == 'inference' || false"
        :nodeData="clueStore.selectedNode" @save="handleDetailSave" @close="clueStore.selectedNode = null" />
      <PersonDetailPanel :visible="clueStore.selectedNode?.type == 'person' || false" :nodeData="clueStore.selectedNode"
        @save="handleDetailSave" @close="clueStore.selectedNode = null" />

      <ClueEdgeSelector v-if="clueStore.showEdgeSelector" :source="clueStore.selectedNodesForEdge[0]"
        :target="clueStore.selectedNodesForEdge[1]" @confirm="clueStore.handleEdgeConfirm"
        @cancel="clueStore.handleEdgeCancel" />

      <ClueEdgeSelector v-if="clueStore.showEdgeSelector && clueStore.editingEdgeId" :initialType="clueStore.edges.find((e) => e.id === clueStore.editingEdgeId)?.data
          ?.type || null
        " :initialLabel="clueStore.edges.find((e) => e.id === clueStore.editingEdgeId)?.data
            ?.label || ''
          " :showDelete="true" @confirm="clueStore.handleEdgeEditConfirm" @cancel="clueStore.handleEdgeCancel"
        @delete-edge="clueStore.handleDeleteEdge" />

      // 氛围设计师
      <AtmosphereDetailPanel :visible="atmosphereStore.selectedNode" :nodeData="atmosphereStore.selectedNode"
        @save="handleDetailSave" @close="atmosphereStore.selectedNode = null" />
      <!-- 氛围调色板 -->
      <AtmospherePalette :visible="showPalette" @close="showPalette = false" @select="handleAtmosphereSelect" />

      <!-- 简单的状态指示 -->
      <div v-if="atmosphereStore.isLinkingMode" class="linking-status">
        <span v-if="!atmosphereStore.selectedAtmosphereNode">🔗 点击氛围节点</span>
        <span v-else>🎯 点击场景节点建立关联</span>
      </div>
    </div>

    <!-- AI生成对话框 -->
    <AIGenerateDialog :visible="showAIDialog" :designer-type="currentDesignerType" :context-data="aiContextData"
      :generate-result="aiGenerateResult" :generate-error="aiGenerateError" @cancel="handleAIDialogCancel"
      @generate="handleAIDialogGenerate" @close="handleAIDialogClose" />
  </div>
</template>

<script setup>
import FloatingToolball from "./NarrativeCom/FloatingToolball.vue";
import CanvasArea from "./NarrativeCom/CanvasArea.vue";
import NodeDetailDrawer from "./NarrativeCom/NodeDetailDrawer.vue";
import EdgeTypeSelector from "./NarrativeCom/EdgeTypeSelector.vue";
import CharacterToolbar from "./CharacterCom/CharacterToolbar.vue";
import CharacterDetailPanel from "./CharacterCom/CharacterDetailPanel.vue";
import CharacterRelationEditor from "./CharacterCom/CharacterRelationEditor.vue";
import CharacterSceneEdgeEditor from "./CharacterCom/CharacterSceneEdgeEditor.vue";
//线索设计师组件导入
import ClueToolbar from "./ClueCom/ClueToolbar.vue";
import ClueDetailPanel from "./ClueCom/ClueDetailPanel.vue";
import InferenceDetailPanel from "./ClueCom/InferenceDetailPanel.vue";
import PersonDetailPanel from "./ClueCom/PersonDetailPanel.vue";
//氛围设计师组件导入
import AtmosphereToolbar from "./AtmosphereCom/AtmosphereToolbar.vue";
import AtmosphereDetailPanel from "./AtmosphereCom/AtmosphereDetailPanel.vue";
import AtmospherePalette from "./AtmosphereCom/AtmospherePalette.vue";
import AIGenerateDialog from './AIGenerateDialog.vue'

import AiIntegrate from "../AiIntegrate.vue";

import ClueEdgeSelector from "./ClueCom/ClueEdgeSelector.vue";
import { useCharacterStore } from "@/stores/character";
import { useCanvasStore } from "@/stores/canvasStore";
import { useClueStore } from "@/stores/clue";
import { useAtmosphereStore } from "@/stores/atmosphere";
import { ref, computed, onMounted, watchEffect } from "vue";
import { userLoadingStore } from "@/stores/userLoadingStore";
import { useSearchStore } from "@/stores/searchStore";

const loadingStore = userLoadingStore();

const ShowAiIntegrate = ref(false);
const showPalette = ref(false);
const searchStore = useSearchStore();

// 传入参数
import { defineProps } from 'vue'
import { socketState } from '@/stores/socket'
import request from '@/api/request';
import { collectContextData } from '@/utils/contextCollector';

// const effectiveNodes = computed(() => props.nodes || canvasStore.nodes)
// const effectiveEdges = computed(() => props.edges || canvasStore.edges)


const canvasStore = useCanvasStore();
const characterStore = useCharacterStore();
const clueStore = useClueStore();
const atmosphereStore = useAtmosphereStore();
const canvasRef = ref(null);

// AI对话框相关状态
const showAIDialog = ref(false)
const currentDesignerType = ref('narrative')
const aiGenerateResult = ref(null)
const aiGenerateError = ref(null)
const aiContextData = computed(() => {
  const contextData = collectContextData()
  return {
    chatCount: contextData.chatCount || 0,
    nodeCount: contextData.nodeCount || 0,
    characterCount: contextData.characterCount || 0,
    // 为了兼容现有UI，保留这些统计字段
    ...contextData.context?.statistics
  }
})

// 导出图片
const handleExport = () => {
  if (canvasRef.value && canvasRef.value?.exportCanvas) {
    // 调用CanvasArea组件暴露的导出方法
    canvasRef.value?.exportCanvas();
  } else {
    // console.warn('canvasRef or export method not found');
  }
};

// 导出为指定格式（显示选择菜单）
const handleExportAs = () => {
  // 创建格式选择弹窗
  const formats = [
    { key: 'png', label: 'PNG图片', desc: '高清画板截图' },
    { key: 'jpg', label: 'JPG图片', desc: '压缩画板截图' },
    { key: 'json', label: 'JSON数据', desc: '结构化数据文件' },
    { key: 'pdf', label: 'PDF文档', desc: '包含图片和数据的文档' },
    { key: 'markdown', label: 'Markdown文档', desc: '文本格式的内容清单' }
  ];

  // 使用Element Plus的消息选择器
  import('element-plus').then(({ ElMessageBox }) => {
    const formatOptions = formats.map(f =>
      `<label style="display: block; margin: 8px 0; cursor: pointer;">
        <input type="radio" name="exportFormat" value="${f.key}" style="margin-right: 8px;">
        <strong>${f.label}</strong> - ${f.desc}
      </label>`
    ).join('');

    ElMessageBox.confirm(
      `<div style="text-align: left;">
        <p style="margin-bottom: 16px;">选择导出格式:</p>
        ${formatOptions}
      </div>`,
      '导出画板',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '导出',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            const selectedFormat = document.querySelector('input[name="exportFormat"]:checked')?.value;
            if (selectedFormat) {
              if (canvasRef.value && canvasRef.value?.exportCanvasAs) {
                canvasRef.value?.exportCanvasAs(selectedFormat);
              }
              done();
            } else {
              ElMessage.warning('请选择导出格式');
              return false;
            }
          } else {
            done();
          }
        }
      }
    ).catch(() => {
      // 用户取消
    });
  });
};

// 直接使用 store 中的数据驱动画布
const nodes = computed(() => [
  ...(canvasStore.nodes || []),
  ...(characterStore.nodes || []),
  ...(clueStore.nodes || []),
  ...(atmosphereStore.nodes || []),

]);
const edges = computed(() => [
  ...(canvasStore.edges || []),
  ...(characterStore.edges || []),
  ...(clueStore.edges || []),
  ...(atmosphereStore.edges || []),
]);

// const edges = computed(() => canvasStore.edges)
const handleDetailSave = (updatedData) => {
  // console.log("当前节点数据", updatedData);

  const index = nodes.value.findIndex((node) => node.id === updatedData.id);
  if (index === -1) {
    // console.warn("未找到对应节点", updatedData.id);
    return;
  }

  const targetNode = nodes.value[index];
  let newIndex = -1;

  if (targetNode.type === "custom") {
    newIndex = canvasStore.handleDetailSave(updatedData);
  } else if (targetNode.type === "character") {
    newIndex = characterStore.handleDetailSave(updatedData);
  } else if (targetNode.type === "clue" || targetNode.type === "inference" || targetNode.type === "person") {
    newIndex = clueStore.handleDetailSave(updatedData);
  } else {
    newIndex = atmosphereStore.handleDetailSave(updatedData);
  }

  // console.log("接受到的索引", newIndex);
  // 直接使用更新后的数据进行强制更新
  if (newIndex !== -1) {
    canvasRef.value?.forceUpdateNode(updatedData.id, updatedData.data);
  }
};

const handleDeleteNode = (id) => {
  const node = nodes.value.find((n) => n.id === id);
  if (!node) return;

  if (node.type === "custom") {
    canvasStore.handleDeleteNode(id);
  } else if (node.type === "character") {
    characterStore.handleDeleteNode(id);
  } else if (node.type === "atmosphere") {
    atmosphereStore.handleDeleteNode(id);
  } else {
    clueStore.handleDeleteNode(id);
  }
};

const handleNodeClick = (data) => {
  // 根据用户角色直接分发到对应的store
  if (data.node.type === "custom") {
    // 叙事设计师
    canvasStore.handleNodeClick(data);
  } else if (data.node.type === "character") {
    // 角色设计师
    characterStore.handleNodeClick(data);
  } else if (data.node.type === "clue") {
    // 线索设计师
    clueStore.handleNodeClick(data);
  } else if (data.node.type === "atmosphere") {
    // 氛围设计师
    atmosphereStore.handleNodeClick(data);
  }
};
const handleEdgeSelect = (edgeId) => {
  // 检查edges是否可用
  if (!edges.value || !Array.isArray(edges.value)) {
    // console.warn('edges数据不可用:', edges.value);
    return;
  }

  // 根据边ID查找边对象
  const edge = edges.value.find(e => e.id === edgeId);
  if (!edge) {
    // console.warn('未找到边对象:', edgeId);
    return;
  }

  // 根据边的类型分发到对应的store
  if (edge.type === 'character-scene') {
    // 人物-场景边由characterStore处理
    characterStore.handleEdgeSelect(edgeId);
  } else if (edge.type === 'relationship') {
    // 角色关系边由characterStore处理
    characterStore.handleEdgeSelect(edgeId);
  } else if (edge.type === 'custom') {
    // 检查是否是线索相关的边
    if (edge.data?.type?.includes('clue') || edge.data?.type?.includes('inference')) {
      clueStore.handleEdgeSelect(edgeId);
    } else {
      canvasStore.handleEdgeSelect(edgeId);
    }
  } else {
    // 默认由canvasStore处理
    canvasStore.handleEdgeSelect(edgeId);
  }
};

// 使用watchEffect自动响应式处理
watchEffect(() => {
  if (canvasRef.value?.vueFlowApi?.getNodes) {
    // console.log('Detected valid VueFlow API:', canvasRef.value.vueFlowApi)
    searchStore.setVueFlowApi(canvasRef.value.vueFlowApi)
  }
})

onMounted(() => {
  const timer = setInterval(() => {
    if (canvasRef.value?.vueFlowApi?.getNodes) {
      clearInterval(timer)
      searchStore.setVueFlowApi(canvasRef.value.vueFlowApi)
      // console.log('API confirmed via interval check')
    }
  }, 100)
})

const handleNodePositionChange = (payload) => {
  const { id, position } = payload;
  const node = nodes.value.find((n) => n.id === id);
  if (!node) return;

  if (node.type === "custom") {
    canvasStore.handlePositionChange(payload);
  } else if (node.type === "character") {
    characterStore.handlePositionChange(payload);
  } else if (node.type === "atmosphere") {
    atmosphereStore.handlePositionChange(payload);
  } else {
    clueStore.handlePositionChange(payload);
  }
};
const handleConnectNode = (connection) => {
  // connection: { source, target, sourceHandle, targetHandle, type, data }

  // 查找源节点和目标节点
  const sourceNode = nodes.value.find(n => n.id === connection.source);
  const targetNode = nodes.value.find(n => n.id === connection.target);

  if (!sourceNode || !targetNode) {
    // console.warn('无法找到连接的节点');
    return;
  }

  // 定义线索相关的节点类型
  const clueNodeTypes = ['clue', 'inference', 'person'];

  // 检查连接类型
  const isClueConnection = clueNodeTypes.includes(sourceNode.type) || clueNodeTypes.includes(targetNode.type);
  const isCharacterRelationship = connection.type === 'relationship';

  if (isClueConnection) {
    // 线索相关连接 - 交给线索设计器处理
    const newEdge = {
      id: connection.id || `edge-${connection.source}-${connection.target}-${Date.now()}`,
      source: connection.source,
      target: connection.target,
      sourceHandle: connection.sourceHandle,
      targetHandle: connection.targetHandle,
      type: 'clue-edge',
      data: {
        type: 'clue-relation',
        label: '线索关联'
      }
    };
    clueStore.handleConnectNode(newEdge);
  } else if (isCharacterRelationship) {
    // 角色间关系 - 交给角色设计器处理
    characterStore.handleConnectNode(connection);
  } else {
    // 其他连接（包括人物-场景连接）- 交给画布设计器处理
    canvasStore.handleConnectNode(connection);
  }
};

// 处理AI生成场景
const handleAiGenerate = async () => {
  currentDesignerType.value = 'narrative'
  showAIDialog.value = true
}

// AI对话框处理方法
const handleAIDialogCancel = () => {
  showAIDialog.value = false
  aiGenerateResult.value = null
  aiGenerateError.value = null
}

const handleAIDialogClose = () => {
  showAIDialog.value = false
  aiGenerateResult.value = null
  aiGenerateError.value = null
}

const handleAIDialogGenerate = async ({ userInput, template }) => {
  // 清除之前的结果
  aiGenerateResult.value = null
  aiGenerateError.value = null

  try {
    // 使用统一的上下文收集工具，收集完整的上下文数据
    const contextData = collectContextData()

    const result = await request.post('/room/generate-nodes', {
      userInput: userInput,
      designerType: currentDesignerType.value,
      contextData: JSON.stringify(contextData)
    })

    if (result.success && result.nodes) {
      // 根据设计师类型添加节点到相应的store
      result.nodes.forEach((nodeData, index) => {
        // 对不同类型节点数据进行格式转换
        let processedData = nodeData
        if (currentDesignerType.value === 'character') {
          processedData = processCharacterNodeData(nodeData)
        } else if (currentDesignerType.value === 'clue') {
          processedData = processClueNodeData(nodeData)
        } else if (currentDesignerType.value === 'atmosphere') {
          processedData = processAtmosphereNodeData(nodeData)
        }

        const newNode = {
          id: `ai-${currentDesignerType.value}-${Date.now()}-${index}`,
          type: getNodeTypeByDesigner(currentDesignerType.value),
          position: {
            x: 400 + index * 250,
            y: 300 + index * 120
          },
          data: processedData
        }

        if (currentDesignerType.value === 'character') {
          characterStore.nodes.push(newNode)
          characterStore.broadcast && characterStore.broadcast()
        } else if (currentDesignerType.value == 'clue') {
          clueStore.nodes.push(newNode)
          clueStore.broadcast && clueStore.broadcast()
        } else if (currentDesignerType.value == 'atmosphere') {
          canvasStore.nodes.push(newNode)
          canvasStore.broadcast && canvasStore.broadcast()
        } else {
          canvasStore.nodes.push(newNode)
          canvasStore.broadcast && canvasStore.broadcast()
        }
      }
      )
      // 设置成功结果，让对话框显示
      aiGenerateResult.value = {
        success: true,
        message: `成功生成${result.nodes.length}个${getDesignerName(currentDesignerType.value)}节点！`,
        nodeCount: result.nodes.length
      }
    } else {
      // 设置失败结果
      aiGenerateError.value = '生成失败：' + (result.message || '未知错误')
    }
  } catch (error) {
    // console.error('AI生成失败:', error)
    // 设置错误结果
    aiGenerateError.value = '生成失败，请检查网络连接'
  }
}

// 辅助方法
const getNodeTypeByDesigner = (designerType) => {
  const typeMap = {
    narrative: 'custom',
    character: 'character',
    clue: 'clue',
    atmosphere: 'atmosphere'
  }
  return typeMap[designerType] || 'custom'
}

const getDesignerName = (designerType) => {
  const nameMap = {
    narrative: '场景',
    character: '角色',
    clue: '线索',
    atmosphere: '氛围'
  }
  return nameMap[designerType] || '节点'
}

// 处理导出功能


// 处理人物设计师AI生成
const handleCharacterAiGenerate = async () => {
  currentDesignerType.value = 'character'
  showAIDialog.value = true
}

// 处理线索设计师AI生成
const handleClueAiGenerate = async () => {
  currentDesignerType.value = 'clue'
  showAIDialog.value = true
}

// 处理角色节点数据格式转换
const processCharacterNodeData = (nodeData) => {
  const processed = { ...nodeData }

  // 处理personality字段：如果是字符串，转换为数组
  if (processed.personality && typeof processed.personality === 'string') {
    // 按照常见分隔符分割字符串
    processed.personality = processed.personality
      .split(/[,，、；;]/)
      .map(item => item.trim())
      .filter(item => item.length > 0)
  }

  // 处理skills字段：如果是字符串，转换为数组
  if (processed.skills && typeof processed.skills === 'string') {
    processed.skills = processed.skills
      .split(/[,，、；;]/)
      .map(item => item.trim())
      .filter(item => item.length > 0)
  }

  // 确保必要字段存在
  if (!processed.personality) processed.personality = []
  if (!processed.skills) processed.skills = []
  if (!processed.relationships) processed.relationships = []
  if (!processed.avatar) processed.avatar = require('@/assets/avatar/1.jpg')

  return processed
}

// 处理线索节点数据格式转换
const processClueNodeData = (nodeData) => {
  const processed = { ...nodeData }

  // 安全处理relatedCharacters字段
  let relatedEvent = ''
  if (processed.relatedCharacters) {
    if (Array.isArray(processed.relatedCharacters)) {
      relatedEvent = processed.relatedCharacters.join(', ')
    } else {
      relatedEvent = String(processed.relatedCharacters)
    }
  }

  // 将AI返回的字段映射到前端期望的字段
  return {
    title: processed.title || '新线索',
    relatedEvent: relatedEvent,
    detail: processed.description || '',
    logic: processed.hiddenInfo || '',
    tags: processed.type || '',
    notes: processed.notes || ''
  }
}

// 处理氛围节点数据格式转换
const processAtmosphereNodeData = (nodeData) => {
  const processed = { ...nodeData }

  // 将AI返回的字段映射到前端期望的字段
  return {
    title: processed.title || '氛围节点',
    timeLabel: processed.timeLabel || '',
    mood: processed.mood || processed.type || '平静',
    lighting: processed.lighting || processed.description || '',
    music: processed.music || '',
    weather: processed.weather || '',
    notes: processed.notes || ''
  }
}

// 处理氛围设计师AI生成
const handleAtmosphereAiGenerate = async () => {
  currentDesignerType.value = 'atmosphere'
  showAIDialog.value = true
}

// 处理氛围调色板
const handleAtmospherePalette = () => {
  handleExport()
}

// 处理关联场景
const handleLinkScene = () => {
  if (atmosphereStore.isLinkingMode) {
    atmosphereStore.exitLinkingMode()
    // console.log('退出氛围与场景关联模式')
  } else {
    atmosphereStore.handleLinkSceneClick()
    // console.log('进入氛围与场景关联模式')
    // console.log('请先点击氛围节点，再点击场景节点建立关联')
  }
}

// 处理氛围选择
const handleAtmosphereSelect = (atmosphereData) => {
  // console.log('选择氛围:', atmosphereData)
  // 这里可以添加氛围选择的处理逻辑
  showPalette.value = false
}

// 边编辑相关辅助方法
const isCharacterSceneEdge = () => {
  if (!characterStore.editingEdgeId) return false
  if (!edges.value || !Array.isArray(edges.value)) return false
  const edge = edges.value.find(e => e.id === characterStore.editingEdgeId)
  return edge?.type === 'character-scene'
}

const getCurrentEdge = (edgeId) => {
  const id = edgeId || canvasStore.editingEdgeId
  if (!id) return null
  if (!edges.value || !Array.isArray(edges.value)) return null
  return edges.value.find(e => e.id === id)
}

const getEdgeSourceNode = (edgeId) => {
  const edge = getCurrentEdge(edgeId)
  if (!edge) return null
  return nodes.value.find(n => n.id === edge.source)
}

const getEdgeTargetNode = (edgeId) => {
  const edge = getCurrentEdge(edgeId)
  if (!edge) return null
  return nodes.value.find(n => n.id === edge.target)
}

// 角色边相关辅助方法
const getCurrentCharacterEdge = () => {
  if (!characterStore.editingEdgeId) return null
  return characterStore.edges.find(e => e.id === characterStore.editingEdgeId)
}

const getCharacterEdgeSourceNode = () => {
  const edge = getCurrentCharacterEdge()
  if (!edge) return null
  return characterStore.nodes.find(n => n.id === edge.source)
}

const getCharacterEdgeTargetNode = () => {
  const edge = getCurrentCharacterEdge()
  if (!edge) return null
  return characterStore.nodes.find(n => n.id === edge.target)
}

// 处理人物-场景边编辑确认
const handleCharacterSceneEdgeEditConfirm = (edgeData) => {
  characterStore.handleCharacterSceneEdgeEditConfirm(edgeData)
}

const handleAiIntegrate = () => {
  const contextData = collectContextData();
  // console.log("调用AI整合功能:", contextData);

  ShowAiIntegrate.value = true;
}

import { watch } from "vue";
const AIContent = ref("");
// 监听AI返回整合剧本的内容
watch(
  () => socketState.AICompleteScriptContent,
  (newData) => {
    if (newData) {
      ShowAiIntegrate.value = true;
      AIContent.value = newData;
      // console.log("newAi:", AIContent.value);
    }
  },
  { deep: true }
);
</script>

<style scoped>
.workspace-container {
  position: relative;
  width: 100%;
  height: 100%;
  max-height: 960px;
  overflow: hidden;

  padding: 40px;
  border-radius: 50px;
}

/* 简单的关联状态指示 */
.linking-status {
  position: fixed;
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  z-index: 100;
  backdrop-filter: blur(4px);
}
</style>