<template>
  <BaseEdge 
    :id="id"
    :path="edgePath"
    :style="edgeStyle"
    :marker-end="markerEnd"
  >
  </BaseEdge>
  
  <!-- 使用EdgeLabelRenderer来渲染标签和强度指示器 -->
  <EdgeLabelRenderer>
    <div
      :style="{
        position: 'absolute',
        transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)`,
        pointerEvents: 'all',
        zIndex: 1000
      }"
      class="relationship-label-container"
    >
      <div
        class="relationship-label"
        :style="{
          backgroundColor: relationshipColor,
          color: '#ffffff',
          padding: '2px 8px',
          borderRadius: '12px',
          fontSize: '12px',
          fontWeight: 'bold',
          whiteSpace: 'nowrap'
        }"
      >
        {{ relationshipLabel }}
      </div>
    </div>
    
    <!-- 关系强度指示器 -->
    <div
      v-for="(dot, index) in strengthDots"
      :key="index"
      :style="{
        position: 'absolute',
        transform: `translate(-50%, -50%) translate(${dot.x}px, ${dot.y}px)`,
        width: '6px',
        height: '6px',
        borderRadius: '50%',
        backgroundColor: strengthColor,
        opacity: 0.7,
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

// 关系数据
const relationData = computed(() => data.value || {})

// 关系标签
const relationshipLabel = computed(() => {
  const type = relationData.value.type || ''
  const typeMap = {
    'friend': '朋友',
    'lover': '恋人',
    'family': '家人',
    'colleague': '同事',
    'mentor': '师生',
    'enemy': '敌人',
    'rival': '竞争',
    'partner': '合作',
    'superior': '上下级',
    'other': '其他'
  }
  return typeMap[type] || type
})

// 根据关系类型确定颜色
const relationshipColor = computed(() => {
  const type = relationData.value.type || ''
  const status = relationData.value.status || 'active'
  
  if (status === 'conflict') {
    return '#ff4757' // 冲突关系 - 红色
  }
  
  const colorMap = {
    'friend': '#4a90e2',     // 朋友 - 蓝色
    'lover': '#ff6b9d',      // 恋人 - 粉色
    'family': '#26de81',     // 家人 - 绿色
    'colleague': '#7b68ee',  // 同事 - 紫色
    'mentor': '#ffa726',     // 师生 - 橙色
    'enemy': '#ff4757',      // 敌人 - 红色
    'rival': '#ff7675',      // 竞争 - 浅红色
    'partner': '#00b894',    // 合作 - 青色
    'superior': '#6c5ce7',   // 上下级 - 深紫色
    'other': '#95a5a6'       // 其他 - 灰色
  }
  
  if (status === 'passive') {
    return colorMap[type] || '#95a5a6' + '80' // 潜在关系 - 半透明
  }
  
  return colorMap[type] || '#95a5a6'
})

// 关系强度颜色
const strengthColor = computed(() => {
  const strength = relationData.value.strength || 5
  if (strength >= 8) return '#27ae60' // 强关系 - 绿色
  if (strength >= 5) return '#f39c12' // 中等关系 - 橙色
  return '#e74c3c' // 弱关系 - 红色
})

// 边样式
const edgeStyle = computed(() => ({
  stroke: relationshipColor.value,
  strokeWidth: Math.max(2, (relationData.value.strength || 5) / 2),
  strokeDasharray: relationData.value.status === 'passive' ? '5,5' : 'none'
}))

// 标签位置
const labelX = computed(() => (props.sourceX + props.targetX) / 2)
const labelY = computed(() => (props.sourceY + props.targetY) / 2)

// 箭头标记 - 简化版本
const markerEnd = computed(() => {
  return 'url(#arrow)'
})

// 关系强度点
const strengthDots = computed(() => {
  const strength = relationData.value.strength || 5
  const dots = []
  const numDots = Math.ceil(strength / 2) // 每2点强度显示一个点
  
  for (let i = 0; i < numDots && i < 5; i++) {
    const progress = (i + 1) / (numDots + 1)
    dots.push({
      x: props.sourceX + (props.targetX - props.sourceX) * progress,
      y: props.sourceY + (props.targetY - props.sourceY) * progress,
      radius: 2
    })
  }
  
  return dots
})
</script>

<style scoped>
.relationship-label {
  pointer-events: none;
  user-select: none;
}
</style> 