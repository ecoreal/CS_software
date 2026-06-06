<template>
  <teleport to="body">
  <div class="overlay">
    <div class="selector-dialog">
      <button class="close-btn" @click="$emit('cancel')" aria-label="关闭弹窗">×</button>

      <h3>选择关联类型</h3>

      <ul class="edge-types">
        <li v-for="type in edgeTypes" :key="type.value" :class="{ selected: selectedType === type.value }"
          @click="selectType(type.value)" @mouseenter="hoverType = type.value" @mouseleave="hoverType = null">
          <div class="line-preview" :style="type.style"></div>
          <div class="info">
            <strong>{{ type.label }}</strong>
            <p>{{ type.description }}</p>
          </div>
        </li>
      </ul>

      <label class="label-input">
        边标签（如条件）：
        <input v-model="edgeLabel" type="text" placeholder="输入标签，如 [条件]" />
      </label>

      <div class="btn-group">
        <button v-if="showDelete" class="btn btn-delete" @click="handleDelete">
          删除
        </button>

        <div class="right-buttons">
          <button class="btn btn-cancel" @click="$emit('cancel')">取消</button>
          <button class="btn btn-confirm" :disabled="!selectedType" @click="confirmSelection">
            确认
          </button>
        </div>
      </div>
    </div>
  </div>
  </teleport>
</template>

<script setup>
import { ref, defineEmits, defineProps, watch} from 'vue'

const emit = defineEmits(['confirm', 'cancel', 'delete-edge'])

const props = defineProps({
  initialType: {
    type: String,
    default: null
  },
  initialLabel: {
    type: String,
    default: ''
  },
  showDelete: {
    type: Boolean,
    default: false
  }
})

const edgeTypes = [
  {
    value: 'dependency',
    label: '顺序依赖（依赖）',
    description: '事件严格先后顺序，主线剧情发展',
    style: {
      borderBottom: '2px dashed #4A90E2',
      position: 'relative',
      height: '4px',
      marginBottom: '4px',
      background: 'none',
      '::after': {
        content: "'→'",
        position: 'absolute',
        right: '0',
        top: '-6px',
        color: '#4A90E2',
        fontWeight: 'bold',
      }
    }
  },
  {
    value: 'causal',
    label: '因果关联（关联）',
    description: '事件因果或相关关系，可能并行发生',
    style: {
      borderBottom: '2px solid #4A90E2',
      height: '4px',
      marginBottom: '4px',
      background: 'none'
    }
  },
  {
    value: 'containment',
    label: '包含关系（组合）',
    description: '整体与部分的关系，子事件归属于大事件',
    style: {
      borderBottom: '2px solid #4A90E2',
      height: '4px',
      marginBottom: '4px',
      position: 'relative',
      '::after': {
        content: "'◆'",
        position: 'absolute',
        right: '0',
        top: '-6px',
        color: '#4A90E2',
        fontWeight: 'bold',
      }
    }
  },
  {
    value: 'inheritance',
    label: '继承关系',
    description: '事件的特殊或变体，继承基本属性',
    style: {
      borderBottom: '2px solid transparent',
      borderLeft: '10px solid transparent',
      borderRight: '10px solid #4A90E2',
      height: '4px',
      marginBottom: '4px',
    }
  },
  {
    value: 'parallel',
    label: '并行关系',
    description: '事件同时发生或无先后顺序',
    style: {
      borderBottom: '2px solid #4A90E2',
      height: '4px',
      marginBottom: '4px',
      background: 'linear-gradient(to right, #4A90E2 50%, transparent 50%)',
      backgroundSize: '10px 4px',
      backgroundRepeat: 'repeat-x'
    }
  },
  {
    value: 'conflict',
    label: '冲突/互斥',
    description: '事件不可能同时发生，相互排斥',
    style: {
      borderBottom: '2px dashed #FF6B6B',
      height: '4px',
      marginBottom: '4px',
      position: 'relative'
    }
  }
]

const selectedType = ref(null)
const hoverType = ref(null)
const edgeLabel = ref('')

watch(() => props.initialType, (val) => {
  selectedType.value = (val) ? val : null
}, { immediate: true })

watch(() => props.initialLabel, (val) => {
  edgeLabel.value = (val) ? val : ''
}, { immediate: true })
const selectType = (type) => {
  selectedType.value = type
}

const confirmSelection = () => {
  if (!selectedType.value) return
  // 向父组件发送选中类型和标签
  // 例如 {type: 'dependency', label: '[条件]'}
  const label = edgeLabel.value.trim()
  // 这里label可为空，代表无标签
  // 触发confirm事件
  // 事件参数顺序按之前设定 (type, label)

  // 可能会有bug
  // console.log("选择的边类型和标签为", selectedType.value, label)
  emit('confirm', selectedType.value, label)
}

// 点击删除按钮
const handleDelete = () => {
  emit('delete-edge')
}
</script>

<style scoped>
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.25);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; /* ✨ 确保比其他 teleport 出来的组件高 */
}


.selector-dialog {
  background: #FFFFFF;
  border-radius: 8px;
  width: 360px;
  max-width: 90vw;
  padding: 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  position: relative;
  color: #333333;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
  color: #666666;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #4A90E2;
}

h3 {
  margin-bottom: 16px;
  font-weight: 600;
  font-size: 1.25rem;
  color: #333333;
}

.edge-types {
  list-style: none;
  padding: 0;
  margin: 0 0 16px 0;
  max-height: 220px;
  overflow-y: auto;
}

.edge-types li {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  border-radius: 6px;
  cursor: pointer;
  gap: 12px;
  transition: background-color 0.2s ease;
  user-select: none;
  border: 1px solid transparent;
}

.edge-types li:hover {
  background-color: #E0E7FF;
}

.edge-types li.selected {
  background-color: #D0DBFF;
  border-color: #4A90E2;
}

/* 简易线条预览 */
.line-preview {
  flex-shrink: 0;
  width: 50px;
  height: 4px;
  border-radius: 2px;
  background-color: #4A90E2;
}

/* 描述文本 */
.info {
  flex-grow: 1;
  font-size: 13px;
  color: #666666;
}

.label-input {
  display: flex;
  flex-direction: column;
  font-size: 14px;
  color: #333333;
  margin-bottom: 20px;
}

.label-input input {
  margin-top: 6px;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
}

.label-input input:focus {
  border-color: #4A90E2;
}

.btn-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
}

.btn-delete {
  flex-shrink: 0;
  /* 左边固定显示 */
}

.right-buttons {
  display: flex;
  gap: 12px;
  margin-left: auto; /* 自动左边距，推到右边 */
}


.btn {
  padding: 8px 18px;
  font-size: 14px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  user-select: none;
  border: none;
}

.btn-cancel {
  background-color: #eee;
  color: #666;
}

.btn-cancel:hover {
  background-color: #ddd;
}

.btn-confirm {
  background-color: #4A90E2;
  color: white;
}

.btn-confirm:disabled {
  background-color: #a0bfff;
  cursor: not-allowed;
}

.btn-confirm:not(:disabled):hover {
  background-color: #357ABD;
}
</style>