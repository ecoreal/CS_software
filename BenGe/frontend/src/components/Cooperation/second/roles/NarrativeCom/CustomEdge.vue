<template>
  <g :data-id="id">
    <!-- 把事件放到 path 上 -->
    <path
      class="custom-edge-path"
      :d="edgePath"
      fill="none"
      :style="edgeStyle"
      :marker-end="markerEnd"
      @mouseenter="hovering = true"
      @mouseleave="hovering = false"
    />
    <EdgeLabelRenderer v-if="labelVisible">
      <div
        :style="{
          ...labelStyle,
          left: `${labelX}px`,
          top: `${labelY}px`,
          pointerEvents: 'auto'  // 确保按钮可点击
        }"
        class="edge-label"
      >
        {{ edgeLabel || edgeTypeName }}
      </div>
    </EdgeLabelRenderer>
  </g>
</template>

<script setup>
import { computed, defineProps, toRef} from 'vue'
import { getBezierPath, EdgeLabelRenderer } from '@vue-flow/core'

const props = defineProps({
  id: String,
  sourceX: Number,
  sourceY: Number,
  targetX: Number,
  targetY: Number,
  sourceHandle: String,
  targetHandle: String,
  data: {
    type: Object,
    default: () => ({}),
  },
})

const data = toRef(props, 'data')

const edgeLabel = computed(() => data?.value.label || '')
const labelX = computed(() => (props.sourceX + props.targetX) / 2)
const labelY = computed(() => (props.sourceY + props.targetY) / 2)

const inferPositionFromHandle = (handleId) => {
  if (!handleId) return 'bottom'
  if (handleId.includes('left')) return 'left'
  if (handleId.includes('right')) return 'right'
  if (handleId.includes('top')) return 'top'
  if (handleId.includes('bottom')) return 'bottom'
  return 'bottom'
}

const sourcePos = computed(() => inferPositionFromHandle(props.sourceHandle))
const targetPos = computed(() => inferPositionFromHandle(props.targetHandle))

const edgePath = computed(() => {
  const [path] = getBezierPath({
    sourceX: props.sourceX,
    sourceY: props.sourceY,
    targetX: props.targetX,
    targetY: props.targetY,
    sourcePosition: sourcePos.value,
    targetPosition: targetPos.value,
  })
  return path
})

const edgeTypeName = computed(() => {
  const map = {
    causal: '因果',
    dependency: '依赖',
    composition: '包含',
    containment: '包含',
    inherit: '继承',
    inheritance: '继承',
    parallel: '并行',
    conflict: '冲突',
    conditional: '条件',
  }
  return map[data?.value.type] || '关联'
})

const labelVisible = computed(() => !!data?.value.label || !!data?.value.type)

const edgeStyle = computed(() => {
  const type = data?.value.type
  switch (type) {
    case 'dependency':
      return {
        stroke: '#4A90E2',
        strokeWidth: 2,
        strokeDasharray: '5,5',
      }
    case 'causal':
      return {
        stroke: '#4A90E2',
        strokeWidth: 2,
      }
    case 'containment':
      return {
        stroke: '#4A90E2',
        strokeWidth: 2,
      }
    case 'inheritance':
      return {
        stroke: '#4A90E2',
        strokeWidth: 2,
        strokeDasharray: '3,3',
      }
    case 'parallel':
      return {
        stroke: '#4A90E2',
        strokeWidth: 2,
        strokeDasharray: '5,3,2,3',
      }
    case 'conflict':
      return {
        stroke: '#FF6B6B',
        strokeWidth: 2,
        strokeDasharray: '6,4',
      }
    default:
      return {
        stroke: '#7f8c8d',
        strokeWidth: 2,
      }
  }
})

const markerEnd = computed(() => 'url(#arrow)')

const labelStyle = {
  position: 'absolute',
  transform: 'translate(-50%, -50%)',
  backgroundColor: '#fff',
  padding: '2px 6px',
  borderRadius: '4px',
  fontSize: '12px',
  color: '#333',
  border: '1px solid #E0E7FF',
  whiteSpace: 'nowrap',
}
</script>

<style scoped>
.edge-label {
  /* 去掉 pointer-events: none; */
  pointer-events: auto;
}
</style>
