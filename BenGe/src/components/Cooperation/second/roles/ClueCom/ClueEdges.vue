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
    cause: '因果',
    imply: '指向',
    exclude: '互斥',
    evidence: '证据链',
  }
  return map[data?.value.type] || '线索关联'
})

const labelVisible = computed(() => !!data?.value.label || !!data?.value.type)

const edgeStyle = computed(() => {
  const type = data?.value.type
  switch (type) {
    case 'cause':
      return { stroke: '#4A90E2', strokeWidth: 2 }
    case 'imply':
      return { stroke: '#00B894', strokeWidth: 2, strokeDasharray: '4,2' }
    case 'exclude':
      return { stroke: '#FF6B6B', strokeWidth: 2, strokeDasharray: '5,4' }
    case 'evidence':
      return { stroke: '#9B59B6', strokeWidth: 2, strokeDasharray: '3,2' }
    default:
      return { stroke: '#95a5a6', strokeWidth: 2 }
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
