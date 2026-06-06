<template>
  <portal to="person-detail-panel">
    <transition name="slide-fade">
      <div v-if="visible" class="detail-drawer" @click.self="handleClose">
        <div class="drawer-content">
          <!-- Header -->
          <div class="drawer-header">
            <h3>{{ formData.name || '人物详情' }}</h3>
            <n-button circle size="small" @click="handleClose">
              <n-icon><CloseIcon /></n-icon>
            </n-button>
          </div>

          <!-- Form -->
          <n-form ref="formRef" :model="formData" label-placement="top">
            <!-- 人物名 -->
            <n-form-item label="姓名" path="name">
              <n-input v-model:value="formData.name" placeholder="请输入人物姓名" />
            </n-form-item>

            <!-- 简介 -->
            <n-form-item label="人物简介" path="bio">
              <n-input
                v-model:value="formData.bio"
                type="textarea"
                placeholder="请输入人物背景介绍..."
                :autosize="{ minRows: 2 }"
              />
            </n-form-item>

            <!-- 相关线索 -->
            <n-form-item label="涉及线索" path="clues">
              <n-input
                v-model:value="formData.clues"
                placeholder="使用逗号分隔，如：遗物A, 动机B"
              />
            </n-form-item>

            <!-- 特点/标签 -->
            <n-form-item label="人物标签" path="tags">
              <n-input
                v-model:value="formData.tags"
                placeholder="使用逗号分隔，如：冷静, 怀疑人"
              />
            </n-form-item>

            <!-- 备注 -->
            <n-form-item label="备注" path="notes">
              <n-input
                v-model:value="formData.notes"
                type="textarea"
                placeholder="其他补充信息..."
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
import { ref, watch, defineProps, defineEmits } from 'vue'
import { NForm, NFormItem, NInput, NButton, NIcon } from 'naive-ui'
import { Close as CloseIcon } from '@vicons/ionicons5'

const props = defineProps({
  visible: Boolean,
  nodeData: {
    type: Object,
    default: () => ({ id: '', data: {} })
  }
})

const emit = defineEmits(['close', 'save'])

const formData = ref({
  name: '',
  bio: '',
  clues: '',
  tags: '',
  notes: ''
})

const initFormData = () => {
  const data = props.nodeData?.data || {}
  formData.value = {
    name: data.name || '',
    bio: data.bio || '',
    clues: Array.isArray(data.clues) ? data.clues.join(', ') : (data.clues || ''),
    tags: Array.isArray(data.tags) ? data.tags.join(', ') : (data.tags || ''),
    notes: data.notes || ''
  }
}

watch(() => props.nodeData, initFormData, { immediate: true, deep: true })

const handleSave = () => {
  emit('save', {
    id: props.nodeData?.id,
    data: {
      name: formData.value.name,
      bio: formData.value.bio,
      clues: formData.value.clues
        .split(',')
        .map(c => c.trim())
        .filter(Boolean),
      tags: formData.value.tags
        .split(',')
        .map(t => t.trim())
        .filter(Boolean),
      notes: formData.value.notes
    }
  })
  handleClose()
}

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.detail-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 40%;
  max-width: 320px;
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
