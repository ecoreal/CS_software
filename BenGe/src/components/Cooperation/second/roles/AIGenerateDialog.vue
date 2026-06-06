<template>
  <div v-if="visible" class="ai-generate-overlay" @click.self="handleCancel">
    <div class="ai-generate-dialog">
      <!-- 对话框头部 -->
      <div class="dialog-header">
        <div class="header-info">
          <i :class="getDesignerIcon(designerType)" class="designer-icon"></i>
          <div class="header-text">
            <h3>{{ getDesignerTitle(designerType) }}</h3>
            <p>{{ getDesignerDescription(designerType) }}</p>
          </div>
        </div>
        <button class="close-btn" @click="handleCancel">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <!-- 对话框主体 -->
      <div class="dialog-body">
        <!-- 左侧：预设模板 -->
        <div class="templates-section">
          <h4>快速模板</h4>
          <div class="template-categories">
            <div 
              v-for="category in templateCategories" 
              :key="category.name"
              class="template-category"
            >
              <h5>{{ category.name }}</h5>
              <div class="template-list">
                <div 
                  v-for="template in category.templates" 
                  :key="template.id"
                  class="template-item"
                  @click="selectTemplate(template)"
                  :class="{ active: selectedTemplate?.id === template.id }"
                >
                  <i :class="template.icon" class="template-icon"></i>
                  <div class="template-info">
                    <span class="template-name">{{ template.name }}</span>
                    <span class="template-desc">{{ template.description }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间：输入区域 -->
        <div class="input-section">
          <h4>详细描述</h4>
          <div class="input-container">
            <textarea 
              v-model="userInput"
              :placeholder="getInputPlaceholder(designerType)"
              class="input-textarea"
              rows="8"
              @input="adjustTextareaHeight"
              ref="textareaRef"
            ></textarea>
          </div>
          
          <!-- 输入提示 -->
          <div class="input-hints">
            <div class="hint-item">
              <i class="fas fa-lightbulb"></i>
              <span>提示：描述越详细，AI生成的内容越精准</span>
            </div>
            <div class="hint-item">
              <i class="fas fa-magic"></i>
              <span>可以指定数量、风格、关联关系等</span>
            </div>
          </div>
        </div>

        <!-- 右侧：上下文信息 -->
        <div class="context-section">
          <h4>当前上下文</h4>
          <div class="context-info">
            <div class="context-item">
              <i class="fas fa-comments"></i>
              <div class="context-details">
                <span class="context-label">聊天记录</span>
                <span class="context-value">{{ contextData.chatCount || 0 }} 条消息</span>
              </div>
            </div>
            <div class="context-item">
              <i class="fas fa-project-diagram"></i>
              <div class="context-details">
                <span class="context-label">画布节点</span>
                <span class="context-value">{{ contextData.nodeCount || 0 }} 个节点</span>
              </div>
            </div>
            <div class="context-item">
              <i class="fas fa-users"></i>
              <div class="context-details">
                <span class="context-label">角色信息</span>
                <span class="context-value">{{ contextData.characterCount || 0 }} 个角色</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 结果显示区域 -->
      <div v-if="showResult" class="result-section">
        <div class="result-content">
          <div v-if="generateResult" class="result-success">
            <i class="fas fa-check-circle"></i>
            <span>{{ generateResult.message || '生成成功！' }}</span>
          </div>
          <div v-if="generateError" class="result-error">
            <i class="fas fa-exclamation-circle"></i>
            <span>{{ generateError }}</span>
          </div>
        </div>
      </div>

      <!-- 对话框底部 -->
      <div class="dialog-footer">
        <div class="footer-info" v-if="!showResult">
          <i class="fas fa-info-circle"></i>
          <span>AI将基于当前上下文和您的描述生成相关节点</span>
        </div>
        <div class="footer-info" v-else>
          <i class="fas fa-clock"></i>
          <span>{{ autoCloseCountdown > 0 ? `${autoCloseCountdown}秒后自动关闭` : '正在关闭...' }}</span>
        </div>
        <div class="footer-actions">
          <button class="btn-cancel" @click="handleCancel" :disabled="isGenerating">
            <i class="fas fa-times"></i>
            取消
          </button>
          <button
            class="btn-generate"
            @click="handleGenerate"
            :disabled="!userInput.trim() || isGenerating"
            v-if="!showResult"
          >
            <i v-if="isGenerating" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-magic"></i>
            {{ isGenerating ? '生成中...' : '开始生成' }}
          </button>
          <button
            class="btn-close"
            @click="handleClose"
            v-if="showResult"
          >
            <i class="fas fa-check"></i>
            确定
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  designerType: {
    type: String,
    required: true,
    validator: value => ['narrative', 'character', 'clue', 'atmosphere'].includes(value)
  },
  contextData: {
    type: Object,
    default: () => ({})
  },
  generateResult: {
    type: Object,
    default: null
  },
  generateError: {
    type: String,
    default: null
  }
})

// Emits
const emit = defineEmits(['cancel', 'generate', 'close'])

// 响应式数据
const userInput = ref('')
const selectedTemplate = ref(null)
const isGenerating = ref(false)
const textareaRef = ref(null)
const showResult = ref(false)
const autoCloseCountdown = ref(0)
let autoCloseTimer = null

// 设计师配置
const designerConfig = {
  narrative: {
    title: 'AI场景生成',
    description: '基于剧情需求智能生成场景节点',
    icon: 'fas fa-theater-masks',
    placeholder: '请描述您想要生成的场景，例如：\n- 生成3个现代都市背景的场景\n- 创建一个紧张的审讯室场景\n- 设计开场、发展、高潮三个关键场景'
  },
  character: {
    title: 'AI角色生成', 
    description: '根据剧情设定智能创建角色',
    icon: 'fas fa-user-friends',
    placeholder: '请描述您想要生成的角色，例如：\n- 生成3个现代都市角色，包括主角、配角\n- 创建一个神秘的反派角色\n- 设计具有复杂背景的侦探角色'
  },
  clue: {
    title: 'AI线索生成',
    description: '智能生成推理线索和证据',
    icon: 'fas fa-search',
    placeholder: '请描述您想要生成的线索，例如：\n- 生成与剧情相关的物证和证言\n- 创建复杂的推理链条\n- 设计误导性线索和关键证据'
  },
  atmosphere: {
    title: 'AI氛围生成',
    description: '创建场景氛围和环境设定',
    icon: 'fas fa-cloud-sun',
    placeholder: '请描述您想要生成的氛围，例如：\n- 生成与场景匹配的氛围节点\n- 创建紧张悬疑的环境氛围\n- 设计灯光、音乐、天气等氛围元素'
  }
}

// 计算属性
const templateCategories = computed(() => {
  return getTemplatesByDesigner(props.designerType)
})

// 方法
const getDesignerTitle = (type) => designerConfig[type]?.title || 'AI生成'
const getDesignerDescription = (type) => designerConfig[type]?.description || ''
const getDesignerIcon = (type) => designerConfig[type]?.icon || 'fas fa-magic'
const getInputPlaceholder = (type) => designerConfig[type]?.placeholder || '请描述您的需求...'



const selectTemplate = (template) => {
  selectedTemplate.value = template
  userInput.value = template.prompt
  nextTick(() => {
    adjustTextareaHeight()
  })
}

const adjustTextareaHeight = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = textareaRef.value.scrollHeight + 'px'
  }
}

const handleCancel = () => {
  if (isGenerating.value) return // 生成中不允许取消
  resetDialog()
  emit('cancel')
}

const handleGenerate = () => {
  if (!userInput.value.trim()) return

  isGenerating.value = true
  showResult.value = false
  emit('generate', {
    userInput: userInput.value,
    template: selectedTemplate.value
  })
}

const handleClose = () => {
  resetDialog()
  emit('close')
}

const resetDialog = () => {
  userInput.value = ''
  selectedTemplate.value = null
  isGenerating.value = false
  showResult.value = false
  autoCloseCountdown.value = 0
  if (autoCloseTimer) {
    clearInterval(autoCloseTimer)
    autoCloseTimer = null
  }
}

const startAutoClose = () => {
  autoCloseCountdown.value = 3
  autoCloseTimer = setInterval(() => {
    autoCloseCountdown.value--
    if (autoCloseCountdown.value <= 0) {
      clearInterval(autoCloseTimer)
      autoCloseTimer = null
      handleClose()
    }
  }, 1000)
}

// 获取模板数据
const getTemplatesByDesigner = (designerType) => {
  // 这里返回预设模板，后续可以从外部文件导入
  const templates = {
    narrative: [
      {
        name: '场景类型',
        templates: [
          { id: 'scene-opening', name: '开场场景', description: '故事开始的场景', icon: 'fas fa-play', prompt: '生成一个引人入胜的开场场景，设定故事背景和初始氛围' },
          { id: 'scene-climax', name: '高潮场景', description: '剧情高潮部分', icon: 'fas fa-fire', prompt: '创建一个紧张刺激的高潮场景，包含关键冲突和转折点' },
          { id: 'scene-ending', name: '结局场景', description: '故事结尾场景', icon: 'fas fa-flag-checkered', prompt: '设计一个令人满意的结局场景，解决主要矛盾并给出结论' }
        ]
      }
    ],
    character: [
      {
        name: '角色类型',
        templates: [
          { id: 'char-protagonist', name: '主角', description: '故事主人公', icon: 'fas fa-star', prompt: '创建一个有深度的主角角色，包含背景故事、动机和性格特点' },
          { id: 'char-antagonist', name: '反派', description: '对立角色', icon: 'fas fa-mask', prompt: '设计一个复杂的反派角色，有合理的动机和行为逻辑' },
          { id: 'char-supporting', name: '配角', description: '支持性角色', icon: 'fas fa-users', prompt: '生成几个重要的配角，每个都有独特的作用和特点' }
        ]
      }
    ],
    clue: [
      {
        name: '线索类型',
        templates: [
          { id: 'clue-evidence', name: '物证', description: '实物证据', icon: 'fas fa-fingerprint', prompt: '生成关键的物理证据，包含发现地点、特征和推理价值' },
          { id: 'clue-testimony', name: '证言', description: '人物证言', icon: 'fas fa-comment', prompt: '创建重要的证人证言，包含真实信息和可能的误导' },
          { id: 'clue-deduction', name: '推理', description: '逻辑推理', icon: 'fas fa-brain', prompt: '设计复杂的推理链条，连接各个线索形成完整逻辑' }
        ]
      }
    ],
    atmosphere: [
      {
        name: '氛围类型',
        templates: [
          { id: 'atmo-tense', name: '紧张氛围', description: '营造紧张感', icon: 'fas fa-bolt', prompt: '创建紧张悬疑的氛围，包含灯光、音效、环境描述' },
          { id: 'atmo-mysterious', name: '神秘氛围', description: '神秘诡异感', icon: 'fas fa-eye', prompt: '设计神秘莫测的环境氛围，增强悬疑效果' },
          { id: 'atmo-dramatic', name: '戏剧氛围', description: '戏剧化效果', icon: 'fas fa-theater-masks', prompt: '营造戏剧性的场景氛围，突出情感冲突' }
        ]
      }
    ]
  }
  
  return templates[designerType] || []
}

// 监听visible变化，重置状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    nextTick(() => {
      adjustTextareaHeight()
    })
  } else {
    resetDialog()
  }
})

// 监听生成结果
watch(() => props.generateResult, (newResult) => {
  if (newResult) {
    isGenerating.value = false
    showResult.value = true
    startAutoClose()
  }
})

// 监听生成错误
watch(() => props.generateError, (newError) => {
  if (newError) {
    isGenerating.value = false
    showResult.value = true
    startAutoClose()
  }
})
</script>

<style scoped>
.ai-generate-overlay {
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
  backdrop-filter: blur(4px);
}

.ai-generate-dialog {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 1200px;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: dialogAppear 0.3s ease-out;
}

@keyframes dialogAppear {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.designer-icon {
  font-size: 32px;
  opacity: 0.9;
}

.header-text h3 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.dialog-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.templates-section {
  width: 280px;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  padding: 20px;
  overflow-y: auto;
}

.templates-section h4 {
  margin: 0 0 20px 0;
  color: #1a202c;
  font-size: 18px;
  font-weight: 700;
  padding-bottom: 8px;
  border-bottom: 2px solid #667eea;
}

.template-category {
  margin-bottom: 20px;
}

.template-category h5 {
  margin: 0 0 12px 0;
  color: #667eea;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.template-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 6px;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.template-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #667eea;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.template-item:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
  transform: translateX(2px);
}

.template-item:hover::before {
  transform: scaleY(1);
}

.template-item.active {
  background: #f0f4ff;
  border-color: #667eea;
  border-width: 2px;
}

.template-item.active::before {
  transform: scaleY(1);
  background: #667eea;
  width: 4px;
}

.template-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
  color: #667eea;
  transition: color 0.3s ease;
}

.template-item.active .template-icon {
  color: #667eea;
}

.template-info {
  flex: 1;
  min-width: 0;
}

.template-name {
  display: block;
  font-weight: 600;
  font-size: 14px;
  color: #2d3748;
  line-height: 1.4;
  margin-bottom: 2px;
}

.template-desc {
  display: block;
  font-size: 12px;
  color: #718096;
  line-height: 1.3;
  opacity: 0.9;
}

.template-item.active .template-name {
  color: #1a202c;
  font-weight: 700;
}

.template-item.active .template-desc {
  color: #4a5568;
}

.input-section {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.input-section h4 {
  margin: 0 0 16px 0;
  color: #2d3748;
  font-size: 16px;
  font-weight: 600;
}

.input-container {
  flex: 1;
  margin-bottom: 16px;
}

.input-textarea {
  width: 100%;
  min-height: 200px;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  transition: border-color 0.3s ease;
  font-family: inherit;
}

.input-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-hints {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 13px;
}

.hint-item i {
  color: #667eea;
}

.context-section {
  width: 280px;
  background: #f8fafc;
  border-left: 1px solid #e2e8f0;
  padding: 24px;
  overflow-y: auto;
}

.context-section h4 {
  margin: 0 0 16px 0;
  color: #2d3748;
  font-size: 16px;
  font-weight: 600;
}

.context-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  margin-bottom: 12px;
  border: 1px solid #e2e8f0;
}

.context-item i {
  color: #667eea;
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.context-details {
  flex: 1;
}

.context-label {
  display: block;
  font-size: 13px;
  color: #4a5568;
  font-weight: 500;
}

.context-value {
  display: block;
  font-size: 14px;
  color: #2d3748;
  font-weight: 600;
  margin-top: 2px;
}



.result-section {
  padding: 20px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.result-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.result-success {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #22c55e;
  background: #f0fdf4;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #bbf7d0;
}

.result-success i {
  font-size: 16px;
}

.result-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ef4444;
  background: #fef2f2;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #fecaca;
}

.result-error i {
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 13px;
}

.footer-info i {
  color: #667eea;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.btn-cancel,
.btn-generate,
.btn-close {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-generate {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-generate:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-generate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-close {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
}

.btn-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(34, 197, 94, 0.3);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .ai-generate-dialog {
    width: 95%;
    max-height: 90vh;
  }
  
  .dialog-body {
    flex-direction: column;
  }
  
  .templates-section,
  .context-section {
    width: 100%;
    max-height: 200px;
  }
}
</style>
