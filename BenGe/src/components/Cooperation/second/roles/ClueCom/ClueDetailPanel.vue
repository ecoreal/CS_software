<template>
  <portal to="clue-detail-panel">
    <transition name="slide-fade">
      <div v-if="visible" class="detail-drawer" @click.self="handleClose">
        <div class="drawer-content">
          <!-- Header -->
          <div class="drawer-header">
            <h3>{{ formData.title || '线索详情' }}</h3>
            <n-button circle size="small" @click="handleClose">
              <n-icon><CloseIcon /></n-icon>
            </n-button>
          </div>

          <!-- Form -->
          <n-form ref="formRef" :model="formData" label-placement="top">
            <!-- Title -->
            <n-form-item label="线索标题" path="title">
              <n-input v-model:value="formData.title" placeholder="请输入线索标题" />
            </n-form-item>

            <!-- 关联事件 -->
            <n-form-item label="关联事件" path="relatedEvent">
              <n-input v-model:value="formData.relatedEvent" placeholder="如：命案发生前" />
            </n-form-item>

            <!-- 线索内容 -->
            <n-form-item label="线索内容" path="detail">
              <n-input
                v-model:value="formData.detail"
                type="textarea"
                placeholder="线索描述..."
                :autosize="{ minRows: 3 }"
              />
            </n-form-item>

            <!-- 推理逻辑 -->
            <n-form-item label="推理逻辑" path="logic">
              <n-input
                v-model:value="formData.logic"
                type="textarea"
                placeholder="如：钥匙消失 → 门锁异常"
                :autosize="{ minRows: 3 }"
              />
            </n-form-item>

            <!-- 标签 -->
            <n-form-item label="标签 (可选)" path="tags">
              <n-input
                v-model:value="formData.tags"
                placeholder="使用逗号分隔，如：物证,目击"
              />
            </n-form-item>

            <!-- 备注 -->
            <n-form-item label="备注" path="notes">
              <n-input
                v-model:value="formData.notes"
                type="textarea"
                placeholder="其他信息..."
                :autosize="{ minRows: 2 }"
              />
            </n-form-item>
          </n-form>

          <!-- Footer -->
          <div class="drawer-footer">
            <n-button @click="handleClose">取消</n-button>
            <n-button type="primary" @click="handleSave">保存</n-button>
          </div>
        </div>
      </div>
    </transition>
  </portal>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { NForm, NFormItem, NInput, NButton, NIcon } from 'naive-ui';
import { Close as CloseIcon } from '@vicons/ionicons5';
import {Portal, PortalTarget} from "portal-vue";

const props = defineProps({
  visible: Boolean,
  clueData: {
    type: Object,
    default: () => ({
      id: '',
      data: {
        title: "测试线索",
        timeLabel: "",
        characters: "",
        clues: "线索A",
        sceneDescription: "",
        nodeConnections: "",
        notes: "",
      },
    })
  }
});

const emit = defineEmits(['close', 'save']);

const formData = ref({
  title: '',
  relatedEvent: '',
  detail: '',
  logic: '',
  tags: '',
  notes: ''
});

const initFormData = () => {
  const data = props.clueData?.data || {};
  formData.value = {
    title: data.title || '',
    relatedEvent: data.relatedEvent || '',
    detail: data.detail || '',
    logic: data.logic || '',
    tags: data.tags || '', // 直接使用字符串，与store定义一致
    notes: data.notes || ''
  };
};

watch(() => props.clueData, initFormData, { immediate: true, deep: true });

const handleSave = () => {
  emit('save', {
    id: props.clueData?.id,
    data: {
      ...formData.value,
      tags: formData.value.tags // 保持字符串格式，与store定义一致
    }
  });
  handleClose();
};

const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
.detail-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 40%;
  max-width: 300px;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2500;
  display: flex;
  justify-content: flex-end;
}

.drawer-content {
  width: 100%;
  height: 100%;
  background: white;
  padding: 20px;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-footer {
  margin-top: auto;
  padding-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

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
