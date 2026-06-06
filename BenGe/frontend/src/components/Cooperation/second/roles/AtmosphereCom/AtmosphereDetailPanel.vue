<template>
  <portal to="atmosphere-detail-panel">
    <transition name="slide-fade">
      <div
        v-if="visible"
        class="detail-panel"
        @click.self="handleClose"
      >
        <div class="panel-content">
          <!-- 头部 -->
          <div class="panel-header">
            <h3>{{ formData.title || '氛围详情' }}</h3>
            <n-button
              circle
              size="small"
              @click="handleClose"
            >
              <n-icon><close-icon /></n-icon>
            </n-button>
          </div>

          <!-- 表单主体 -->
          <div class="panel-body">
            <n-form
              ref="formRef"
              :model="formData"
              label-placement="top"
            >
              <!-- 基本信息 -->
              <div class="form-section">
                <h4 class="section-title">基本信息</h4>

                <!-- 节点标题 -->
                <n-form-item label="节点标题" path="title">
                  <n-input
                    v-model:value="formData.title"
                    placeholder="输入氛围节点标题"
                  />
                </n-form-item>

                <!-- 时间标签 -->
                <n-form-item label="时间标签" path="timeLabel">
                  <n-input
                    v-model:value="formData.timeLabel"
                    placeholder="例：DAY1 09:00"
                  />
                </n-form-item>
              </div>

              <!-- 氛围设定 -->
              <div class="form-section">
                <h4 class="section-title">氛围设定</h4>

                <!-- 情绪氛围 -->
                <n-form-item label="情绪氛围" path="mood">
                  <n-input
                    v-model:value="formData.mood"
                    placeholder="如：紧张、温馨、神秘、恐怖"
                  />
                </n-form-item>

                <!-- 灯光设定 -->
                <n-form-item label="灯光设定" path="lighting">
                  <n-input
                    v-model:value="formData.lighting"
                    placeholder="如：昏暗、暖黄、刺眼、忽明忽暗"
                  />
                </n-form-item>

                <!-- 音效/BGM -->
                <n-form-item label="音效/BGM" path="music">
                  <n-input
                    v-model:value="formData.music"
                    placeholder="如：雨声、钢琴曲、脚步声"
                  />
                </n-form-item>

                <!-- 天气环境 -->
                <n-form-item label="天气环境" path="weather">
                  <n-input
                    v-model:value="formData.weather"
                    placeholder="如：暴雨、多云、雾气、晴朗"
                  />
                </n-form-item>
              </div>

              <!-- 额外备注 -->
              <div class="form-section">
                <h4 class="section-title">额外备注</h4>
                <n-form-item label="氛围描述" path="notes">
                  <n-input
                    v-model:value="formData.notes"
                    type="textarea"
                    placeholder="详细描述场景氛围和注意事项..."
                    :autosize="{ minRows: 3, maxRows: 6 }"
                  />
                </n-form-item>
              </div>
            </n-form>
          </div>

          <!-- 操作按钮 -->
          <div class="panel-footer">
            <n-button @click="handleClose">取消</n-button>
            <n-button
              type="primary"
              @click="handleSave"
            >
            保存
            </n-button>
          </div>
        </div>
      </div>
    </transition>
  </portal>
</template>

<script setup>
import { ref, watch, defineEmits, defineProps } from 'vue';
import { NForm, NFormItem, NInput, NButton, NIcon } from 'naive-ui';
import { Close as CloseIcon } from '@vicons/ionicons5';
import {Portal} from "portal-vue";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  nodeData: {
    type: Object,
    default: () => ({
      id: '',
      data: {
        title: '',
        timeLabel: '',
        mood: '',
        lighting: '',
        music: '',
        weather: '',
        notes: ''
      }
    })
  }
});

const emit = defineEmits(['close', 'save']);

// 表单数据
const formData = ref({
  title: '',
  timeLabel: '',
  mood: '',
  lighting: '',
  music: '',
  weather: '',
  notes: ''
});

// 初始化表单数据
const initFormData = () => {
  const data = props.nodeData?.data || props.nodeData?.node?.data || {};
  formData.value = {
    title: data.title || '',
    timeLabel: data.timeLabel || '',
    mood: data.mood || '',
    lighting: data.lighting || '',
    music: data.music || '',
    weather: data.weather || '',
    notes: data.notes || ''
  };
  // console.log('氛围面板初始化表单数据:', formData.value);
};

// 监听节点数据变化
watch(() => props.nodeData, (newNode) => {
  if (newNode) {
    // console.log('氛围面板接收到节点数据:', props.nodeData);
    initFormData();
  }
}, { deep: true, immediate: true });

// 保存处理
const handleSave = () => {
  // console.log('保存氛围表单数据', formData.value);
  if (!props.nodeData?.id) return;

  emit('save', {
    id: props.nodeData?.id,
    data: formData.value
  });
  handleClose();
};

// 关闭处理
const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
.detail-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 35%;
  max-width: 400px;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2500;
  display: flex;
  justify-content: flex-end;
}

.panel-content {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  backdrop-filter: blur(10px);
  padding: 20px;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #34495e;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding-bottom: 8px;
}

.panel-footer {
  margin-top: auto;
  padding-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

/* 表单样式覆盖 */
:deep(.n-form-item-label) {
  color: #495057 !important;
  font-weight: 500;
}

:deep(.n-input) {
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(0, 0, 0, 0.15) !important;
  border-radius: 6px !important;
}

:deep(.n-input:hover) {
  border-color: rgba(0, 0, 0, 0.25) !important;
}

:deep(.n-input:focus-within) {
  border-color: #74b9ff !important;
  box-shadow: 0 0 0 2px rgba(116, 185, 255, 0.2) !important;
}

:deep(.n-input .n-input__input-el) {
  color: #2c3e50 !important;
}

:deep(.n-input .n-input__input-el::placeholder) {
  color: rgba(0, 0, 0, 0.4) !important;
}

/* 动画过渡 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.slide-fade-enter-to {
  opacity: 1;
  transform: translateX(0);
}

.slide-fade-leave-from {
  opacity: 1;
  transform: translateX(0);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style> 