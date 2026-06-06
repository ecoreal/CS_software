<template>
  <BaseEdge 
    :id="id"
    :path="edgePath"
    :style="edgeStyle"
    :marker-end="markerEnd"
  >
  </BaseEdge>
  
  <!-- 使用EdgeLabelRenderer来渲染标签 -->
  <EdgeLabelRenderer>
    <div
      :style="{
        position: 'absolute',
        transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`,
        pointerEvents: 'all',
        zIndex: 1000
      }"
      class="scene-participation-label-container"
    >
      <div
        class="scene-participation-label"
        :style="{
          backgroundColor: participationColor,
          color: '#ffffff',
          padding: '2px 8px',
          borderRadius: '12px',
          fontSize: '12px',
          fontWeight: 'bold',
          whiteSpace: 'nowrap'
        }"
      >
        {{ participationLabel }}
      </div>
    </div>
    
    <!-- 重要性指示器 -->
    <div
      v-for="(dot, index) in importanceDots"
      :key="index"
      :style="{
        position: 'absolute',
        transform: `translate(-50%, -50%) translate(${dot.x}px, ${dot.y}px)`,
        width: '4px',
        height: '4px',
        borderRadius: '50%',
        backgroundColor: importanceColor,
        opacity: 0.8,
        pointerEvents: 'none'
      }"
    />
  </EdgeLabelRenderer>
</template>

<script setup>
import { computed, defineProps, toRef } from 'vue'
import { BaseEdge, EdgeLabelRenderer, getBezierPath } from '@vue-flow/core'

const props = defineProps({
  id: String,
  sourceX: Number,
  sourceY: Number,
  targetX: Number,
  targetY: Number,
  sourcePosition: String,
  targetPosition: String,
  sourceHandle: String,
  targetHandle: String,
  data: {
    type: Object,
    default: () => ({})
  }
})

// 使用toRef创建响应式引用
const data = toRef(props, 'data')

// 计算边路径
const edgePath = computed(() => {
  return getBezierPath({
    sourceX: props.sourceX,
    sourceY: props.sourceY,
    sourcePosition: props.sourcePosition,
    targetX: props.targetX,
    targetY: props.targetY,
    targetPosition: props.targetPosition,
  })[0]
})

// 场景参与数据
const participationData = computed(() => data.value || {})

// 参与类型标签
const participationLabel = computed(() => {
  const type = participationData.value.participationType || 'protagonist'
  const typeMap = {
    'protagonist': '主角',
    'supporting': '配角',
    'antagonist': '反派',
    'witness': '见证者',
    'victim': '受害者',
    'helper': '帮助者',
    'obstacle': '阻碍者',
    'other': '其他'
  }
  return typeMap[type] || participationData.value.label || '参与'
})

// 根据参与类型确定颜色
const participationColor = computed(() => {
  const type = participationData.value.participationType || 'protagonist'
  const colorMap = {
    'protagonist': '#4a90e2',    // 主角 - 蓝色
    'supporting': '#26de81',     // 配角 - 绿色
    'antagonist': '#ff4757',     // 反派 - 红色
    'witness': '#ffa726',        // 见证者 - 橙色
    'victim': '#ff6b9d',         // 受害者 - 粉色
    'helper': '#00b894',         // 帮助者 - 青色
    'obstacle': '#ff7675',       // 阻碍者 - 浅红色
    'other': '#95a5a6'           // 其他 - 灰色
  }
  return colorMap[type] || '#95a5a6'
})

// 重要性颜色
const importanceColor = computed(() => {
  const importance = participationData.value.importance || 'normal'
  const colorMap = {
    'critical': '#e74c3c',  // 关键 - 红色
    'high': '#f39c12',      // 高 - 橙色
    'normal': '#3498db',    // 普通 - 蓝色
    'low': '#95a5a6'        // 低 - 灰色
  }
  return colorMap[importance] || '#3498db'
})

// 边样式
const edgeStyle = computed(() => {
  const importance = participationData.value.importance || 'normal'
  const widthMap = {
    'critical': 4,
    'high': 3,
    'normal': 2,
    'low': 1
  }
  
  return {
    stroke: participationColor.value,
    strokeWidth: widthMap[importance] || 2,
    strokeDasharray: participationData.value.style === 'dashed' ? '5,5' : 'none'
  }
})

// 标签位置
const labelX = computed(() => (props.sourceX + props.targetX) / 2)
const labelY = computed(() => (props.sourceY + props.targetY) / 2)

// 箭头标记
const markerEnd = computed(() => {
  return 'url(#arrow)'
})

// 重要性指示点
const importanceDots = computed(() => {
  const importance = participationData.value.importance || 'normal'
  const dotCountMap = {
    'critical': 3,
    'high': 2,
    'normal': 1,
    'low': 0
  }
  
  const numDots = dotCountMap[importance] || 1
  const dots = []
  
  for (let i = 0; i < numDots; i++) {
    const progress = (i + 1) / (numDots + 1)
    dots.push({
      x: props.sourceX + (props.targetX - props.sourceX) * progress,
      y: props.sourceY + (props.targetY - props.sourceY) * progress
    })
  }
  
  return dots
})
</script>

<style scoped>
.scene-participation-label {
  pointer-events: none;
  user-select: none;
}
</style>
