<template>
  <teleport to="body">
  <transition name="fade">
    <div 
      v-if="showSelector"
      class="relation-editor-overlay"
      @click.self="handleCancel"
    >
      <div class="relation-editor-modal">
        <div class="modal-header">
          <h3>设置角色关系</h3>
          <n-button 
            circle
            size="small"
            @click="handleCancel"
          >
            <n-icon><close-icon /></n-icon>
          </n-button>
        </div>

        <div class="modal-body">
          <!-- 关系角色显示 -->
          <div class="relation-characters">
            <div class="character-info">
              <img 
                :src="sourceCharacter.avatar || defaultAvatar" 
                :alt="sourceCharacter.name"
                class="character-avatar"
              />
              <span class="character-name">{{ sourceCharacter.name || '角色A' }}</span>
            </div>
            
            <div class="relation-arrow">
              <n-icon size="24" color="#666">
                <arrow-forward-icon />
              </n-icon>
            </div>
            
            <div class="character-info">
              <img 
                :src="targetCharacter.avatar || defaultAvatar" 
                :alt="targetCharacter.name"
                class="character-avatar"
              />
              <span class="character-name">{{ targetCharacter.name || '角色B' }}</span>
            </div>
          </div>

          <!-- 关系类型选择 -->
          <n-form
            ref="formRef"
            :model="formData"
            label-placement="top"
          >
            <n-form-item label="关系类型" path="type">
              <n-select
                v-model:value="formData.type"
                :options="relationTypeOptions"
                placeholder="请选择关系类型"
                clearable
              />

            </n-form-item>

            <n-form-item label="关系描述" path="description">
              <n-input
                v-model:value="formData.description"
                type="textarea"
                placeholder="描述两个角色之间的具体关系..."
                :autosize="{ minRows: 3, maxRows: 5 }"
              />
            </n-form-item>

            <n-form-item label="关系强度" path="strength">
              <n-slider
                v-model:value="formData.strength"
                :min="1"
                :max="10"
                :marks="strengthMarks"
                :step="1"
              />
            </n-form-item>

            <n-form-item label="关系状态" path="status">
              <n-radio-group v-model:value="formData.status">
                <n-radio value="active">活跃</n-radio>
                <n-radio value="passive">潜在</n-radio>
                <n-radio value="conflict">冲突</n-radio>
              </n-radio-group>
            </n-form-item>
          </n-form>
        </div>

        <div class="modal-footer">
          <n-button @click="handleCancel">取消</n-button>
          <n-button 
            v-if="showDelete"
            type="error"
            @click="handleDelete"
          >
            删除关系
          </n-button>
          <n-button 
            type="primary"
            @click="handleConfirm"
          >
            确认
          </n-button>
        </div>
      </div>
    </div>
  </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits } from 'vue';
import { 
  NButton, 
  NIcon, 
  NForm, 
  NFormItem, 
  NSelect, 
  NInput, 
  NSlider, 
  NRadioGroup, 
  NRadio 
} from 'naive-ui';
import { Close as CloseIcon, ArrowForward as ArrowForwardIcon } from '@vicons/ionicons5';

const props = defineProps({
  source: {
    type: Object,
    default: () => ({})
  },
  target: {
    type: Object,
    default: () => ({})
  },
  initialType: {
    type: String,
    default: ''
  },
  initialDescription: {
    type: String,
    default: ''
  },
  initialStrength: {
    type: Number,
    default: 5
  },
  initialStatus: {
    type: String,
    default: 'active'
  },
  showDelete: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['confirm', 'cancel', 'delete-relation']);

// 默认头像
const defaultAvatar = '/src/assets/avatar/1.jpg';

// 显示状态
const showSelector = computed(() => props.source && props.target);

// 角色信息
const sourceCharacter = computed(() => props.source?.data || {});
const targetCharacter = computed(() => props.target?.data || {});

// 关系类型选项 - 使用常量确保在teleport后也能正常工作
const relationTypeOptions = [
  { label: '朋友', value: 'friend' },
  { label: '恋人', value: 'lover' },
  { label: '家人', value: 'family' },
  { label: '同事', value: 'colleague' },
  { label: '师生', value: 'mentor' },
  { label: '敌人', value: 'enemy' },
  { label: '竞争对手', value: 'rival' },
  { label: '合作伙伴', value: 'partner' },
  { label: '上下级', value: 'superior' },
  { label: '其他', value: 'other' }
];

// 关系强度标记
const strengthMarks = {
  1: '很弱',
  3: '较弱',
  5: '中等',
  7: '较强',
  10: '很强'
};

// 表单数据
const formData = ref({
  type: '',
  description: '',
  strength: 5,
  status: 'active'
});

// 监听初始值变化
watch([
  () => props.initialType,
  () => props.initialDescription,
  () => props.initialStrength,
  () => props.initialStatus
], ([type, description, strength, status]) => {
  formData.value = {
    type: type || '',
    description: description || '',
    strength: strength || 5,
    status: status || 'active'
  };
}, { immediate: true });

// 确认关系
const handleConfirm = () => {
  if (!formData.value.type) {
    // 可以添加提示：请选择关系类型
    return;
  }

  emit('confirm', {
    type: formData.value.type,
    description: formData.value.description,
    strength: formData.value.strength,
    status: formData.value.status
  });
};

// 取消
const handleCancel = () => {
  emit('cancel');
};

// 删除关系
const handleDelete = () => {
  emit('delete-relation');
};
</script>

<style scoped>
.relation-editor-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.relation-editor-modal {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.modal-body {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
}

.relation-characters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.character-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.character-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #4a90e2;
}

.character-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  text-align: center;
}

.relation-arrow {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .relation-editor-modal,
.fade-leave-active .relation-editor-modal {
  transition: transform 0.3s ease;
}

.fade-enter-from .relation-editor-modal,
.fade-leave-to .relation-editor-modal {
  transform: scale(0.9) translateY(-20px);
}
</style> 