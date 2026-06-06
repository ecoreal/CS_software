<template>
    <div v-if="visible" class="custom-dialog-mask">
      <div class="custom-dialog-box">
        <div class="dialog-header">
          <i class="fa-solid fa-circle-info dialog-icon"></i>
          <h3 class="dialog-title">{{ title }}</h3>
        </div>
  
        <p class="dialog-message">{{ message }}</p>
  
        <input
          v-if="showInput"
          v-model="inputValue"
          :type="inputType"
          :placeholder="inputPlaceholder"
          class="dialog-input"
        />
  
        <div class="dialog-actions">
          <button class="dialog-btn cancel-btn" @click="$emit('cancel')">取消</button>
          <button class="dialog-btn confirm-btn" @click="$emit('confirm', inputValue)">确认</button>
        </div>
      </div>
    </div>
  </template>
  
  
  <script setup>
  import { ref, watch, defineEmits, defineProps } from 'vue'
  
  const emit = defineEmits(['confirm', 'cancel'])
  
  const props = defineProps({
    visible: Boolean,
    title: String,
    message: String,
    showInput: Boolean,
    inputPlaceholder: String,
    inputType: {
      type: String,
      default: 'text',
    },
  })
  
  const inputValue = ref('')
  
  const handleCancel = () => {
    emit('cancel')
  }
  
  const handleConfirm = () => {
    emit('confirm', inputValue.value)
  }
  </script>
  
  <style scoped>
  .custom-dialog-mask {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.25);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  }
  
  .custom-dialog-box {
    width: 420px;
    background: linear-gradient(135deg, #FFFFFF 0%, #F5F9FF 100%);
    border-radius: 24px;
    box-shadow:
      0 8px 24px rgba(30, 60, 120, 0.3),
      0 4px 12px rgba(0, 0, 0, 0.05);
    padding: 30px;
    text-align: center;
    animation: fadeInUp 0.3s ease-out;
  }
  
  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .dialog-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .dialog-icon {
    font-size: 32px;
    color: #5C7BFE;
    margin-bottom: 10px;
  }
  
  .dialog-title {
    font-size: 22px;
    font-weight: 700;
    color: #333;
  }
  
  .dialog-message {
    font-size: 16px;
    color: #666;
    margin-bottom: 24px;
    line-height: 1.5;
  }
  
  .dialog-input {
    width: 80%;
    padding: 10px 16px;
    font-size: 16px;
    border: 1px solid #ddd;
    border-radius: 12px;
    outline: none;
    margin-bottom: 24px;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.06);
  }
  
  .dialog-input:focus {
    border-color: #5C7BFE;
    box-shadow: 0 0 0 3px rgba(92, 123, 254, 0.15);
  }
  
  .dialog-actions {
    display: flex;
    justify-content: space-around;
    gap: 20px;
  }
  
  .dialog-btn {
    flex: 1;
    padding: 12px 0;
    font-size: 16px;
    border-radius: 40px;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }
  
  .cancel-btn {
    background-color: #f5f5f5;
    color: #333;
    border: none;
  }
  
  .cancel-btn:hover {
    background-color: #e0e0e0;
  }
  
  .confirm-btn {
    background-color: #5C7BFE;
    color: #fff;
    border: none;
  }
  
  .confirm-btn:hover {
    background-color: #3f62ea;
  }

  
</style>  