<template>
  <portal to="node-detail-drawer">
    <transition name="slide-fade">
      <div
        v-if="visible"
        class="detail-drawer"
        @click.self="handleClose"
      >
        <div class="drawer-content">
          <!-- 头部 -->
          <div class="drawer-header">
            <h3>{{ formData.title || '节点详情' }}</h3>
            <n-button
              circle
              size="small"
              @click="handleClose"
            >
              <n-icon><close-icon /></n-icon>
            </n-button>
          </div>

          <!-- 表单主体 -->
          <n-form
            ref="formRef"
            :model="formData"
            label-placement="top"
          >
            <!-- 节点标题 -->
            <n-form-item label="节点标题" path="title">
              <n-input
                v-model:value="formData.title"
                placeholder="输入节点标题"
              />
            </n-form-item>

            <!-- 节点时间 -->
            <n-form-item label="节点时间" path="timeLabel">
              <n-input
                v-model:value="formData.timeLabel"
                placeholder="例：DAY1 09:00"
              />
            </n-form-item>

            <!-- 涉及人物 -->
            <n-form-item label="涉及人物" path="characters">
              <n-input
                v-model:value="formData.characters"
                placeholder="输入涉及的人物"
              />
            </n-form-item>

            <!-- 涉及线索 -->
            <n-form-item label="涉及线索" path="clues">
              <n-input
                v-model:value="formData.clues"
                placeholder="输入涉及的线索"
              />
            </n-form-item>

            <!-- 场景描述 -->
            <n-form-item label="场景描述" path="sceneDescription">
              <n-input
                v-model:value="formData.sceneDescription"
                type="textarea"
                placeholder="输入场景的大致描述..."
                :autosize="{ minRows: 3 }"
              />
            </n-form-item>

            <!-- 其他节点联系 -->
            <n-form-item label="其他节点联系" path="nodeConnections">
              <n-input
                v-model:value="formData.nodeConnections"
                type="textarea"
                placeholder="输入与其他节点的联系说明..."
                :autosize="{ minRows: 3 }"
              />
            </n-form-item>

            <!-- 后续注意点 -->
            <n-form-item label="后续注意点" path="notes">
              <n-input
                v-model:value="formData.notes"
                type="textarea"
                placeholder="输入后续需要注意的事项..."
                :autosize="{ minRows: 3 }"
              />
            </n-form-item>
          </n-form>

          <!-- 操作按钮 -->
          <div class="drawer-footer">
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
import { ref, watch , defineEmits, defineProps } from 'vue';
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
        characters: '',
        clues: '',
        sceneDescription: '',
        nodeConnections: '',
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
  characters: '',
  clues: '',
  sceneDescription: '',
  nodeConnections: '',
  notes: ''
});

// 初始化表单数据
const initFormData = () => {
  // 修复数据路径：直接从nodeData.data获取，因为AI生成的节点数据直接存储在data字段中
  const data = props.nodeData?.data || {};
  formData.value = {
    title: data.title || '',
    timeLabel: data.timeLabel || '',
    characters: data.characters || '',
    clues: data.clues || '',
    sceneDescription: data.sceneDescription || '',
    nodeConnections: data.nodeConnections || '',
    notes: data.notes || ''
  };
  // console.log("AI生成节点formData数据:", formData.value);
  // console.log("原始nodeData:", props.nodeData);
};


// 监听节点数据变化
watch(() => props.nodeData, (newNode) => {
  if (newNode) {
    // console.log('初始化表单数据', props.nodeData);
    initFormData();
  }
}, { deep: true, immediate: true });

// 保存处理
const handleSave = () => {
  // console.log('保存表单数据', formData.value);
  // console.log("结点信息修改", props.nodeData)
  if (!props.nodeData?.id) return;
  // console.log("id", props.nodeData?.id)

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
.detail-drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 40%;
  max-width: 300px;
  height: 100%;
  /* max-width: 400px; */
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

.drawer-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.drawer-footer {
  margin-top: auto;
  padding-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画过渡时间和样式 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

/* === 进入动画 === */
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.slide-fade-enter-to {
  opacity: 1;
  transform: translateX(0);
}

/* === 离开动画 === */
.slide-fade-leave-from {
  opacity: 1;
  transform: translateX(0);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

</style>