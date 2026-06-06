<template>
  <div class="canvas-container">
    <VueFlow ref="vueFlowRef"
             class="vue-flow"
             :style="{
    backgroundSize: computedBackgroundSize,
    backgroundPosition: `${backgroundX}px ${backgroundY}px`
  }"
             :nodes="props.nodes"
             :edges="props.edges"
             :node-types="nodeTypes"
             :edge-types="edgeTypes"
             :connectable="true"
             :snap-to-grid="true"
             :snap-grid="[15, 15]"
             :fit-view="true"
             @node-click="handleNodeClick"
             @edge-click="handleEdgeClick"
             @node-drag="handleNodeDrag"
             @node-drag-stop="handleNodeDragStop"
             @connect="handleConnect"
             @edge-update="handleEdgeUpdate"
             @move="handleMove"

    >
      <!-- <Background
        variant="lines"
        gap="20"
        size="0.6"
        patternColor="#6e28e6"
        bgColor="#fafafa"
        width="100"
        height="100"
      /> -->
      <!-- 自定义结点 -->
      <template #node-custom="{ id, type, data, position }">
        <CustomNode
            :key="id"
            :id="id"
            :type="type"
            :data="data"
            :position="position"
            @delete="handleDeleteNode"
        />
      </template>

      <!-- 角色卡片节点 -->
      <template #node-character="{ id, type, data, position }">
        <CharacterCard
            :key="id"
            :id="id"
            :type="type"
            :data="data"
            :position="position"
            @delete="handleDeleteNode"
        />
      </template>

      <!-- 氛围节点 -->
      <template #node-atmosphere="{ id, type, data, position, selected }">
        <AtmosphereNode
            :key="id"
            :id="id"
            :type="type"
            :data="data"
            :position="position"
            :selected="selected"
            @delete="handleDeleteNode"
        />
      </template>

      <!-- 线索节点 -->
      <template #node-clue="{ id, type, data, position }">
        <ClueNode
            :key="id"
            :id="id"
            :type="type"
            :data="data"
            :position="position"
            @delete="handleDeleteNode"
        />
      </template>

      <!-- 结论节点 -->
      <template #node-inference="{ id, type, data, position }">
        <InferenceNode
            :key="id"
            :id="id"
            :type="type"
            :data="data"
            :position="position"
            @delete="handleDeleteNode"
        />
      </template>

      <!-- 事件相关人物节点 -->
      <template #node-person="{ id, type, data, position }">
        <PersonNode
            :key="id"
            :id="id"
            :type="type"
            :data="data"
            :position="position"
            @delete="handleDeleteNode"
        />
      </template>

      <!-- 自定义边 -->
      <template #edge-custom="edgeProps">
        <CustomEdge v-bind="edgeProps"/>
      </template>

      <!-- 角色关系边 -->
      <template #edge-relationship="edgeProps">
        <CharacterRelationEdge v-bind="edgeProps"/>
      </template>

      <!-- 人物场景关联边 -->
      <template #edge-character-scene="edgeProps">
        <CharacterSceneEdge v-bind="edgeProps"/>
      </template>

      <!-- 线索、结论边 -->
      <template #edge-clue="edgeProps">
        <ClueEdges v-bind="edgeProps"/>
      </template>
    </VueFlow>
  </div>
</template>

<script setup>
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { CanvasExporter } from '@/utils/exportUtils'
import { UltimateVueFlowExporter } from '@/utils/ultimateVueFlowExporter'
import { defineProps, defineEmits, defineExpose, markRaw, watch, onMounted } from 'vue'
import CustomNode from './CustomNode.vue'
import CustomEdge from './CustomEdge.vue'
import CharacterCard from '../CharacterCom/CharacterCard.vue'
import CharacterRelationEdge from '../CharacterCom/CharacterRelationEdge.vue'
import ClueNode from '../ClueCom/ClueNode.vue'
import ClueEdges from '../ClueCom/ClueEdges.vue'
import InferenceNode from '../ClueCom/InferenceNode.vue'
import PersonNode from '../ClueCom/PersonNode.vue'
import AtmosphereNode from '../AtmosphereCom/AtmosphereNode.vue'
import CharacterSceneEdge from '../CharacterCom/CharacterSceneEdge.vue'
import { useSearchStore } from '@/stores/searchStore'
import html2canvas from 'html2canvas';

import { ref, computed } from 'vue'

const vueFlowRef = ref(null)
const searchStore = useSearchStore()

const { getNodes, viewport, dimensions, setViewport, fitView } = useVueFlow()

// 定义vueFlowApi
const vueFlowApi = computed(() => ({
  getNodes: () => getNodes.value, // 直接返回节点数组
  viewport: () => viewport.value,
  dimensions: () => dimensions.value,
  setViewport,
  fitView
}))


// 导出画板为PNG（原有功能）
function exportCanvas() {
  const el = document.querySelector('.vue-flow');
  if (!el) {
    // console.warn('画布容器未找到');
    return;
  }

  html2canvas(el, {
    useCORS: true,
    scale: 2,
    backgroundColor: null,
    logging: true,
  }).then(canvas => {
    const imgData = canvas.toDataURL('image/png');
    const link = document.createElement('a');
    link.href = imgData;
    link.download = 'canvas.png';
    link.click();
  }).catch(err => {
    // console.error('截图失败：', err);
  });
}

// 导出画板为指定格式（新增功能）
function exportCanvasAs(format) {
  // console.log(`导出格式: ${format}`);

  // 获取完整的画板数据
  const canvasData = {
    nodes: props.nodes,
    edges: props.edges,
    exportTime: new Date().toISOString(),
    viewport: {
      x: 0,
      y: 0,
      zoom: 1
    }
  };

  const filename = `canvas-${Date.now()}`;

  switch (format) {
    case 'png':
      // 使用终极导出工具
      UltimateVueFlowExporter.exportAsPNG(filename);
      break;
    case 'jpg':
      // 使用终极导出工具
      UltimateVueFlowExporter.exportAsJPG(filename);
      break;
    case 'json':
      CanvasExporter.exportAsJSON(canvasData, filename);
      break;
    case 'pdf':
      // PDF 导出也使用改进的逻辑
      exportAsPDF(canvasData, filename);
      break;
    case 'markdown':
      CanvasExporter.exportAsMarkdown(canvasData, filename);
      break;
    default:
      // console.warn('不支持的导出格式:', format);
  }
}

// 改进的 PDF 导出
async function exportAsPDF(canvasData, filename) {
  try {
    // 使用终极导出工具获取高质量的画板截图
    const imageResult = await UltimateVueFlowExporter.exportVueFlowWithBackground('png', 'temp');
    
    if (imageResult.success) {
      // 使用获取到的图片数据创建 PDF
      const { jsPDF } = await import('jspdf');
      
      const pdf = new jsPDF({
        orientation: 'landscape',
        unit: 'mm',
        format: 'a4'
      });
      
      // 添加画板图片
      const imgWidth = 280;
      const imgHeight = 200;
      pdf.addImage(imageResult.data, 'PNG', 10, 10, imgWidth, imgHeight);
      
      // 添加数据页面
      pdf.addPage();
      pdf.setFontSize(16);
      pdf.text('画板数据统计', 10, 20);
      
      // 统计节点数量
      const nodeStats = CanvasExporter.getNodeStatistics(canvasData);
      let yPos = 40;
      
      pdf.setFontSize(12);
      for (const [type, count] of Object.entries(nodeStats)) {
        pdf.text(`${type}: ${count}个`, 10, yPos);
        yPos += 10;
      }
      
      // 保存PDF
      pdf.save(`${filename}.pdf`);
    } else {
      // console.error('PDF导出失败：无法获取画板截图');
    }
  } catch (error) {
    // console.error('PDF导出失败:', error);
  }
}

onMounted(() => {
  searchStore.setVueFlowApi(vueFlowApi.value)
  // console.log('VueFlow API set with nodes:', getNodes.value)
});

// 监听聚焦节点变化
watch(() => searchStore.focusedNodeId, async (newId) => {
  if (newId) {
    await searchStore.focusNode(newId);
    searchStore.setFocusedNodeId(null);
  }
});

const props = defineProps({
  nodes: {
    type: Array,
    required: true,
    validator: nodes => nodes.every(n => n.position && typeof n.position.x === 'number')
  },
  edges: {
    type: Array,
    default: () => []
  }
})

// // 监听数据变化
// watch(
//   () => props.nodes.map(node => node.data?.title),
//   (newTitles) => {
//     // // console.log('节点标题变化:', newTitles);
//   },
//   { deep: false }
// )

// watch(() => props.nodes, (newVal, oldVal) => {
//   // // console.log('[DEBUG] nodes发生变化')
//   // console.log('新节点列表：', newVal)
//   // console.log('旧节点列表：', oldVal)
// }, { deep: true })

const emit = defineEmits(['node-select', "edge-select" , 'delete-node' , 'node-position-change', "connect-node"])

const zoom = ref(1)
const backgroundX = ref(0)
const backgroundY = ref(0)

const computedBackgroundSize = computed(() => {
  // 假设原始背景图大小为 100%，可以根据需要调整为 auto、cover 等
  return `${zoom.value * 800}%`
})

const handleMove = (viewpoint) => {
  // Vue Flow 的 move 事件会返回 zoom 和 transform 信息
  // // console.log("handleMove：",viewpoint);

  zoom.value = viewpoint.flowTransform.zoom // 缩放比例
  backgroundX.value = viewpoint.flowTransform.x;
  backgroundY.value = viewpoint.flowTransform.y;
}


// 处理节点事件
const handleNodeClick = (node) => {
  emit('node-select', node)
}
// 处理边点击修改
const handleEdgeClick = (e) => {
  // 查看具体的数据格式
  // console.log("选择的边", e)
  emit("edge-select", e.edge.id)
}
// 删除结点
const handleDeleteNode = (nodeId) => {
  emit('delete-node', nodeId)
}
// 实现结点拖拽
const handleNodeDrag = (node) => {
  const payload = { id: node.node.id, position: node.node.position };
  // // console.log('emit payload:', payload)
  emit('node-position-change', payload)
}

const handleNodeDragStop = (node) => {
  // // console.log('拖拽停止:', node); // 调试日志
  if (!node?.node.position) return

  const payload = { id: node.node.id, position: node.node.position };
  emit('node-position-change', payload)
}

const nodeTypes = {
  custom: CustomNode,
  character: CharacterCard,
  atmosphere: AtmosphereNode,
  clue: markRaw(ClueNode),
  inference: markRaw(InferenceNode),
  person: markRaw(PersonNode)
}

const edgeTypes = {
  custom: markRaw(CustomEdge),
  relationship: markRaw(CharacterRelationEdge),
  'character-scene': markRaw(CharacterSceneEdge),
  'clue-edge': markRaw(ClueEdges)
}

// 强制刷新结点
const { updateNode } = useVueFlow();
const { updateEdge } = useVueFlow();
const forceUpdateNode = (id, newData) => {
  // console.log('forceUpdateNode', id, newData)
  updateNode(id, {
    data: { ...newData },
  })
}
const forceUpdateEdge = (id, newData) => {

  updateEdge(id, {
    data: { ...newData}
  })
};

defineExpose({
  forceUpdateNode,
  forceUpdateEdge,
  exportCanvas,
  exportCanvasAs,
  vueFlowApi:vueFlowApi.value,
})

// 边连接完成事件（拖动新边）
const handleConnect = (params) => {
  // 查找源节点和目标节点
  const sourceNode = props.nodes.find(n => n.id === params.source);
  const targetNode = props.nodes.find(n => n.id === params.target);

  if (!sourceNode || !targetNode) {
    // console.warn('无法找到连接的节点');
    return;
  }

  // 检查是否是人物-场景连接
  const isCharacterToScene = sourceNode.type === 'character' && targetNode.type === 'custom';
  const isSceneToCharacter = sourceNode.type === 'custom' && targetNode.type === 'character';
  const isCharacterToCharacter = sourceNode.type === 'character' && targetNode.type === 'character';

  let edgeType, edgeData;

  if (isCharacterToScene || isSceneToCharacter) {
    // 人物-场景连接
    edgeType = 'character-scene';
    edgeData = {
      participationType: 'protagonist', // 默认为主角参与
      importance: 'normal',
      description: '参与场景',
      label: '参与',
      showLabel: true
    };
  } else if (isCharacterToCharacter) {
    // 角色间关系连接
    edgeType = 'relationship';
    edgeData = {
      type: 'friend',
      label: '关系',
      strength: 5,
      status: 'active'
    };
  } else {
    // 其他类型连接
    edgeType = 'custom';
    edgeData = {
      type: 'dependency',
      label: '新边'
    };
  }

  const newEdge = {
    id: `edge-${params.source}-${params.target}-${Date.now()}`,
    source: params.source,
    target: params.target,
    sourceHandle: params.sourceHandle,
    targetHandle: params.targetHandle,
    type: edgeType,
    data: edgeData
  }

  emit('connect-node', newEdge)
}

// 边位置更新事件（编辑边连接点）
const handleEdgeUpdate = ({ edge, connection }) => {
  edge.source = connection.source
  edge.target = connection.target
  edge.sourceHandle = connection.sourceHandle
  edge.targetHandle = connection.targetHandle
}


</script>

<style>
.canvas-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;

  border-radius: 50px;
}

.vue-flow {
  background-image: url('../../../../../assets/second/background.png');
}

/* 覆盖VueFlow默认节点样式 */
.vue-flow__node {
  position: absolute !important;
  transform: none !important;
  width: auto !important;
  height: auto !important;
}

/* 确保自定义节点不受干扰 */
.custom-node-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

@import '../../../../../../node_modules/@vue-flow/core/dist/style.css';
@import '../../../../../../node_modules/@vue-flow/core/dist/theme-default.css';
</style>