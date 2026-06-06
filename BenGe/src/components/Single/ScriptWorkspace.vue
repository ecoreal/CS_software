<template>
  <div class="script-workspace">
    <!-- 当没有选中剧本时显示提示卡片 -->
    <div v-if="scriptStore.selectScriptId === -1" class="empty-workspace">
      <div class="empty-workspace-icon">📝</div>
      <h3>开始您的剧本创作</h3>
      <p>请在左侧列表中选择一个剧本或点击"新建剧本"按钮创建一个新的剧本</p>
      <button class="create-script-btn" @click="handleCreateScript">新建剧本</button>
    </div>
    
    <!-- 有选中剧本时显示工作区 -->
    <template v-else>
      <!-- 工作区头部 -->
      <div class="workspace-header">
        <h2 class="workspace-title">{{ scriptStore.scriptTitle === '新剧本' ? '对话创作工作区' : scriptStore.scriptTitle }}</h2>
        
        <!-- 阶段指示器 -->
        <div class="stage-indicator">
          <div 
            v-for="(stage, index) in scriptStages" 
            :key="stage.value"
            :class="['stage-item', { 'active': scriptStore.scriptStage === stage.value, 'completed': scriptStore.scriptStage > stage.value }]"
          >
            <div class="stage-number">{{ index + 1 }}</div>
            <div class="stage-name">{{ stage.name }}</div>
          </div>
        </div>
      </div>
      <div class="workspace-body">
      <!-- 主工作区 - 根据当前阶段显示不同组件 -->
      <component 
        :is="currentStageComponent" 
        :scriptId="scriptStore.selectScriptId"
        :userInfo="prop.userInfo"
      />
    </div>

    </template>
  </div>
</template>

<script setup>
import { computed, defineProps, onMounted } from 'vue';
import { usescriptStore } from '@/stores/scriptStore';

// 导入各阶段组件
import DirectionSelectionStage from './stages/DirectionSelectionStage.vue';
import FrameworkDesignStage from './stages/FrameworkDesignStage.vue';
import CompleteScriptStage from './stages/CompleteScriptStage.vue';

const scriptStore = usescriptStore();

// 用户信息
const prop = defineProps(['userInfo']); // 传入用户名

// 阶段定义
const scriptStages = [
  { value: 1, name: '方向选择' },
  { value: 2, name: '框架设计' },
  { value: 3, name: '完整剧本' }
];

// 根据当前阶段返回对应的组件
const currentStageComponent = computed(() => {
  switch (scriptStore.scriptStage) {
    case 1:
      return DirectionSelectionStage;
    case 2:
      return FrameworkDesignStage;
    case 3:
      return CompleteScriptStage;
    default:
      return DirectionSelectionStage;
  }
});

// 新建剧本
const handleCreateScript = () => {
  scriptStore.createNewScript();
};

// 手动调整阶段
onMounted(() => {
  scriptStore.scriptStage = 2;
})

</script>

<style scoped>
.script-workspace {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f8f8f8;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 空工作区样式 */
.empty-workspace {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 30px;
}

.empty-workspace-icon {
  font-size: 64px;
  margin-bottom: 20px;
  color: #6366F1;
}

.empty-workspace h3 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.empty-workspace p {
  font-size: 16px;
  color: #666;
  max-width: 400px;
  margin-bottom: 30px;
  line-height: 1.6;
}

.create-script-btn {
  background-color: #6366F1;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.create-script-btn:hover {
  background-color: #4F46E5;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #eaeaea;
  z-index: 8;
}

.workspace-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 阶段指示器样式 */
.stage-indicator {
  display: flex;
  align-items: center;
}

.stage-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  opacity: 0.6;
  transition: all 0.3s;
}

.stage-item.active {
  opacity: 1;
  font-weight: 600;
}

.stage-item.completed {
  opacity: 0.8;
  color: #4CAF50;
}

.stage-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: #eaeaea;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
  font-size: 14px;
  font-weight: 600;
}

.stage-item.active .stage-number {
  background-color: #6366F1;
  color: white;
}

.stage-item.completed .stage-number {
  background-color: #4CAF50;
  color: white;
}

.workspace-body{
  flex: 1;
  overflow-y: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}
</style>