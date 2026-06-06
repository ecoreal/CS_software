<template>
    <div v-if="loadingStore.loading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </template>
  
  <script setup>
  import { userLoadingStore } from '@/stores/userLoadingStore.js'
  const loadingStore = userLoadingStore()
  </script>
  
  <style scoped>
  .loading-overlay {
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background-color: #fff;
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .spinner {
    width: 60px;
    height: 60px;
    border: 6px solid #BDBDCE;
    border-top: 6px solid transparent;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  @keyframes spin {
    100% { transform: rotate(360deg); }
  }
  </style>
  