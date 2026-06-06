<template>
  <teleport to="body">
  <transition name="fade">
    <div 
      v-if="showSelector"
      class="scene-editor-overlay"
      @click.self="handleCancel"
    >
      <div class="scene-editor-modal">
        <div class="modal-header">
          <h3>设置角色场景参与</h3>
          <n-button
            circle
            size="small"
            @click="handleCancel"
          >
            <n-icon><close-icon /></n-icon>
          </n-button>
        </div>


        <div class="modal-body">
          <!-- 角色和场景显示 -->
          <div class="participation-info">
            <div class="character-info">
              <img 
                :src="characterNode.data?.avatar || defaultAvatar" 
                :alt="characterNode.data?.name || '角色'"
                class="character-avatar"
              />
              <span class="character-name">{{ characterNode.data?.name || '角色' }}</span>
            </div>
            
            <div class="participation-arrow">
              <n-icon size="24" color="#666">
                <arrow-forward-icon />
              </n-icon>
            </div>
            
            <div class="scene-info">
              <div class="scene-icon">🎬</div>
              <span class="scene-name">{{ sceneNode.data?.title || '场景' }}</span>
            </div>
          </div>

          <!-- 参与设置表单 -->
          <n-form
            ref="formRef"
            :model="formData"
            label-placement="top"
          >
            <n-form-item label="参与类型" path="participationType">
              <select
                v-model="formData.participationType"
                style="width: 100%; padding: 8px; border: 1px solid #d9d9d9; border-radius: 6px;"
              >
                <option value="">请选择参与类型</option>
                <option value="protagonist">主角</option>
                <option value="supporting">配角</option>
                <option value="antagonist">反派</option>
                <option value="witness">见证者</option>
                <option value="victim">受害者</option>
                <option value="helper">帮助者</option>
                <option value="obstacle">阻碍者</option>
                <option value="other">其他</option>
              </select>
            </n-form-item>

            <n-form-item label="重要性" path="importance">
              <select
                v-model="formData.importance"
                style="width: 100%; padding: 8px; border: 1px solid #d9d9d9; border-radius: 6px;"
              >
                <option value="">请选择重要性</option>
                <option value="critical">关键</option>
                <option value="high">高</option>
                <option value="normal">普通</option>
                <option value="low">低</option>
              </select>
            </n-form-item>

            <n-form-item label="参与描述" path="description">
              <n-input
                v-model:value="formData.description"
                type="textarea"
                placeholder="描述角色在此场景中的具体参与情况..."
                :autosize="{ minRows: 3, maxRows: 5 }"
              />
            </n-form-item>

            <n-form-item label="显示标签" path="label">
              <n-input
                v-model:value="formData.label"
                placeholder="边上显示的标签文字"
              />
            </n-form-item>

            <n-form-item label="边样式" path="style">
              <n-radio-group v-model:value="formData.style">
                <n-radio value="solid">实线</n-radio>
                <n-radio value="dashed">虚线</n-radio>
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
            删除关联
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
  NInput,
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
  initialParticipationType: {
    type: String,
    default: 'protagonist'
  },
  initialImportance: {
    type: String,
    default: 'normal'
  },
  initialDescription: {
    type: String,
    default: ''
  },
  initialLabel: {
    type: String,
    default: ''
  },
  initialStyle: {
    type: String,
    default: 'solid'
  },
  showDelete: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['confirm', 'cancel', 'delete-edge']);

// 默认头像
const defaultAvatar = require('@/assets/avatar/1.jpg');

// 显示状态
const showSelector = computed(() => true);

// 确定哪个是角色节点，哪个是场景节点
const characterNode = computed(() => {
  return props.source?.type === 'character' ? props.source : props.target;
});

const sceneNode = computed(() => {
  return props.source?.type === 'custom' ? props.source : props.target;
});



// 表单数据
const formData = ref({
  participationType: 'protagonist',
  importance: 'normal',
  description: '',
  label: '',
  style: 'solid'
});

// 监听初始值变化
watch([
  () => props.initialParticipationType,
  () => props.initialImportance,
  () => props.initialDescription,
  () => props.initialLabel,
  () => props.initialStyle
], ([participationType, importance, description, label, style]) => {

  formData.value = {
    participationType: participationType || 'protagonist',
    importance: importance || 'normal',
    description: description || '',
    label: label || '',
    style: style || 'solid'
  };
}, { immediate: true });

// 确认参与设置
const handleConfirm = () => {
  if (!formData.value.participationType) {
    // 可以添加提示：请选择参与类型
    return;
  }

  emit('confirm', {
    participationType: formData.value.participationType,
    importance: formData.value.importance,
    description: formData.value.description,
    label: formData.value.label || getDefaultLabel(),
    style: formData.value.style
  });
};

// 获取默认标签
const getDefaultLabel = () => {
  const typeMap = {
    'protagonist': '主角',
    'supporting': '配角',
    'antagonist': '反派',
    'witness': '见证者',
    'victim': '受害者',
    'helper': '帮助者',
    'obstacle': '阻碍者',
    'other': '参与'
  };
  return typeMap[formData.value.participationType] || '参与';
};

// 取消
const handleCancel = () => {
  emit('cancel');
};

// 删除
const handleDelete = () => {
  emit('delete-edge');
};
</script>

<style scoped>
.scene-editor-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.scene-editor-modal {
  background: white;
  border-radius: 12px;
  padding: 0;
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: visible;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.modal-body {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
}

.participation-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.character-info, .scene-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.character-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.scene-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: #e3f2fd;
  border-radius: 50%;
}

.character-name, .scene-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  text-align: center;
}

.participation-arrow {
  margin: 0 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  flex-shrink: 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
