<template>
  <portal to="character-detail-panel">
    <transition name="slide-fade">
      <div
        v-if="visible"
        class="detail-panel"
        @click.self="handleClose"
      >
        <div class="panel-content">
          <!-- 头部 -->
          <div class="panel-header">
            <h3>{{ formData.name || '角色详情' }}</h3>
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

                <!-- 角色姓名 -->
                <n-form-item label="角色姓名" path="name">
                  <n-input
                    v-model:value="formData.name"
                    placeholder="输入角色姓名"
                  />
                </n-form-item>

                <!-- 头像选择 -->
                <n-form-item label="角色头像" path="avatar">
                  <div class="avatar-selector">
                    <div class="current-avatar">
                      <img
                        :src="formData.avatar || defaultAvatar"
                        alt="当前头像"
                        @error="handleImageError"
                      />
                    </div>
                    <div class="avatar-options">
                      <div
                        v-for="avatar in avatarOptions"
                        :key="avatar.id"
                        class="avatar-option"
                        :class="{ active: formData.avatar === avatar.src }"
                        @click="selectAvatar(avatar.src)"
                      >
                        <img :src="avatar.src" :alt="avatar.name" />
                      </div>
                    </div>
                  </div>
                </n-form-item>

                <!-- 年龄 -->
                <n-form-item label="年龄" path="age">
                  <n-input-number
                    v-model:value="formData.age"
                    placeholder="输入年龄"
                    :min="1"
                    :max="150"
                  />
                </n-form-item>

                <!-- 职业 -->
                <n-form-item label="职业" path="occupation">
                  <n-input
                    v-model:value="formData.occupation"
                    placeholder="输入职业"
                  />
                </n-form-item>
              </div>

              <!-- 性格特征 -->
              <div class="form-section">
                <h4 class="section-title">性格特征</h4>
                <n-form-item label="性格标签" path="personality">
                  <n-dynamic-tags
                    v-model:value="formData.personality"
                    placeholder="添加性格标签"
                  />
                </n-form-item>
              </div>

              <!-- 背景故事 -->
              <div class="form-section">
                <h4 class="section-title">背景故事</h4>
                <n-form-item label="角色背景" path="background">
                  <n-input
                    v-model:value="formData.background"
                    type="textarea"
                    placeholder="输入角色的背景故事..."
                    :autosize="{ minRows: 3, maxRows: 6 }"
                  />
                </n-form-item>
              </div>

              <!-- 能力技能 -->
              <div class="form-section">
                <h4 class="section-title">能力技能</h4>
                <n-form-item label="技能特长" path="skills">
                  <n-dynamic-tags
                    v-model:value="formData.skills"
                    placeholder="添加技能标签"
                  />
                </n-form-item>
              </div>

              <!-- 重要物品 -->
              <div class="form-section">
                <h4 class="section-title">重要物品</h4>
                <n-form-item label="关键道具" path="items">
                  <n-input
                    v-model:value="formData.items"
                    type="textarea"
                    placeholder="输入角色拥有的重要物品..."
                    :autosize="{ minRows: 2, maxRows: 4 }"
                  />
                </n-form-item>
              </div>

              <!-- 角色备注 -->
              <div class="form-section">
                <h4 class="section-title">角色备注</h4>
                <n-form-item label="其他信息" path="notes">
                  <n-input
                    v-model:value="formData.notes"
                    type="textarea"
                    placeholder="输入其他重要信息..."
                    :autosize="{ minRows: 2, maxRows: 4 }"
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
import { NForm, NFormItem, NInput, NInputNumber, NButton, NIcon, NDynamicTags } from 'naive-ui';
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
        name: '',
        avatar: '',
        age: null,
        occupation: '',
        personality: [],
        background: '',
        skills: [],
        items: '',
        notes: ''
      }
    })
  }
});

const emit = defineEmits(['close', 'save']);

// 默认头像
const defaultAvatar = '/src/assets/avatar/1.jpg'

// 头像选项
const avatarOptions = ref([
  { id: 1, name: '头像1', src: '/src/assets/avatar/1.jpg' },
  { id: 2, name: '头像2', src: '/src/assets/avatar/2.jpg' },
  { id: 3, name: '头像3', src: '/src/assets/avatar/3.jpg' },
  { id: 4, name: '头像4', src: '/src/assets/avatar/4.jpg' },
  { id: 5, name: '头像5', src: '/src/assets/avatar/5.jpg' }
]);

// 表单数据
const formData = ref({
  name: '',
  avatar: '',
  age: null,
  occupation: '',
  personality: [],
  background: '',
  skills: [],
  items: '',
  notes: ''
});

// 初始化表单数据
const initFormData = () => {
  const data = props.nodeData?.node?.data || props.nodeData?.data || {};
  formData.value = {
    name: data.name || '',
    avatar: data.avatar || defaultAvatar,
    age: data.age || null,
    occupation: data.occupation || '',
    personality: data.personality || [],
    background: data.background || '',
    skills: data.skills || [],
    items: data.items || '',
    notes: data.notes || ''
  };
  // console.log("角色表单数据:", formData.value);
};

// 监听节点数据变化
watch(() => props.nodeData, (newNode) => {
  if (newNode) {
    // console.log('初始化角色表单数据', props.nodeData);
    initFormData();
  }
}, { deep: true, immediate: true });

// 选择头像
const selectAvatar = (avatarSrc) => {
  formData.value.avatar = avatarSrc;
};

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = defaultAvatar;
};

// 保存处理
const handleSave = () => {
  // console.log('保存角色数据', formData.value);
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
  width: 45%;
  max-width: 400px;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2500;
  display: flex;
  justify-content: flex-end;
}

.panel-content {
  width: 100%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 15px rgba(0, 0, 0, 0.1);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.panel-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.panel-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #34495e;
  padding-bottom: 8px;
  border-bottom: 2px solid #e8f4f8;
}

.avatar-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.current-avatar {
  align-self: flex-start;
}

.current-avatar img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #4a90e2;
}

.avatar-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.avatar-option {
  cursor: pointer;
  border-radius: 50%;
  padding: 2px;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.avatar-option:hover {
  border-color: #74b9ff;
  transform: scale(1.05);
}

.avatar-option.active {
  border-color: #4a90e2;
  background: #e8f4f8;
}

.avatar-option img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/* 过渡动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style> 