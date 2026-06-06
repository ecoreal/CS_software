<template>
  <div class="scripts-list-container">
    <div class="scripts-list">
      <div class="scripts-list-header">
        <h3 class="scripts-list-title">我的剧本</h3>
        <div class="list-actions">
          <div class="list-search">
            <input 
              type="text" 
              v-model="scriptStore.scriptListSearchQuery" 
              placeholder="搜索剧本..." 
              @input="scriptStore.handleScriptSearch(scriptStore.scriptListSearchQuery)"
            />
          </div>
          <div class="sort-btn">
            <select v-model="scriptStore.scriptSortOption">
              <option value="date-desc">最近创建</option>
              <option value="date-asc">最早创建</option>
              <option value="name-asc">按名称 A-Z</option>
              <option value="name-desc">按名称 Z-A</option>
            </select>
          </div>
        </div>
      </div>
      
      <div class="scripts-list-content">
        <div
          v-for="script in scriptStore.filteredScripts"
          :key="script.id"
          :class="['script-item', { 'selected': script.id === scriptStore.selectScriptId }]"
          @click="scriptStore.handleSelectScript(script.id)"
        >
          <div class="script-icon user-script-icon">
            <img src="../../assets/user-menu-script.png" alt="Script" class="script-icon"/>
          </div>
          <div class="script-info">
            <div class="script-title">{{ script.title || '未命名剧本' }}</div>
            <div class="script-date">{{ formatTimestamp(script.lastUpdated) }}</div>
          </div>
          <div class="script-actions">
            <button class="script-action-btn" @click.stop="() => scriptStore.deleteScript(script.id)" title="删除">
              <img src="../../assets/delete.png" alt="Delete" class="delete-icon"/>
            </button>
          </div>
        </div>
        
        <div v-if="scriptStore.filteredScripts.length === 0" class="empty-scripts">
          <div class="empty-scripts-icon">
            <img src="../../assets/user-menu-script.png" alt="Script" class="empty-scripts-icon">
          </div>
          <div class="empty-scripts-text">没有找到剧本</div>
        </div>
      </div>

      <div class="script-item create-new-script-item" @click="scriptStore.createNewScript">
          <div class="script-icon user-script-icon">
            <img src="../../assets/create-script.png" alt="Add" class="script-icon"/>
          </div>
          <div class="script-info">
            <div class="script-title">创建新剧本</div>
          </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { usescriptStore  } from '@/stores/scriptStore';

const scriptStore = usescriptStore();

function formatTimestamp(timestamp) {
  if (!timestamp) return '';

  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0'); // 月份0开始
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

onMounted(() => {
  scriptStore.scriptSortOption = 'date-desc';
});

</script>

<style scoped>
.scripts-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scripts-list {
  border-radius: 10px;
  overflow: hidden;
  background-color: #fff;
  border: 1px solid #e0e0e0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.scripts-list-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eaeaea;
}

.scripts-list-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 15px 0;
  text-align: center;
}

.list-actions {
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 12px;
}

.list-search {
  position: relative;
  width: 100%;
}

.list-search input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
  background-color: white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.list-search input:focus {
  border-color: #aaa;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.sort-btn {
  position: relative;
  width: 100%;
}

.sort-btn select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  background-color: #fff;
  cursor: pointer;
  outline: none;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  padding-right: 35px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.sort-btn select:focus {
  border-color: #aaa;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.scripts-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.script-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  margin: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: #fff;
  border: 1px solid transparent;
}

.script-item:hover {
  background-color: #f9f9f9;
  border-color: #e0e0e0;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.script-item.selected {
  background-color: rgba(179, 178, 199, 0.15);
  border-color: rgba(179, 178, 199, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.script-icon {
    width: 20px;
    height: 20px;
    object-fit: contain;
    vertical-align: middle;
    margin-bottom: 3px;
}

.script-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.script-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.script-date {
  font-size: 13px;
  color: #888;
}

.script-actions {
  display: flex;
  opacity: 0;
  transition: opacity 0.2s;
}

.script-item:hover .script-actions {
  opacity: 1;
}

.script-action-btn {
  background: none;
  border: none;
  padding: 6px 10px;
  font-size: 16px;
  cursor: pointer;
  color: #888;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.script-action-btn:hover {
  color: #333;
  background-color: rgba(0, 0, 0, 0.05);
}

.empty-scripts {
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.empty-scripts-icon {
  width: 50px;
  height: 50px;
  color: #ccc;
  margin-bottom: 15px;
}

.empty-scripts-text {
  font-size: 16px;
  color: #999;
  margin-bottom: 20px;
}

.create-new-script-item {
  justify-content: center;
  color: #666;
  font-weight: 500;
  background-color: #f0f0f0;
  border: 1px dashed #ccc;
}

.create-new-script-item:hover {
  background-color: #e0e0e0;
  border-color: #999;
  transform: translateY(-2px);
}

.user-script-icon {
 margin-right: 20px; 
}

.delete-icon {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    object-fit: contain;
    vertical-align: middle;
  }

/* 适应收缩状态的样式 */
@media (max-width: 768px) {
  .scripts-list-header {
    padding: 12px 15px;
  }
  
  .scripts-list-title {
    font-size: 16px;
    margin-bottom: 10px;
  }
  
  .list-search input,
  .sort-btn select {
    padding: 8px 12px;
  }
  
  .script-item {
    padding: 12px 15px;
    margin: 4px 8px;
  }
  
  .script-title {
    font-size: 14px;
  }
  
  .script-date {
    font-size: 12px;
  }
  
  .script-icon {
    font-size: 20px;
    margin-right: 10px;
  }
}

/* 极度收缩状态下的样式 */
@media (max-width: 480px) {
  .scripts-list-section:not(.show-list) .scripts-list-header {
    padding: 10px 5px;
  }
  
  .scripts-list-section:not(.show-list) .scripts-list-title {
    writing-mode: vertical-lr;
    transform: rotate(180deg);
    letter-spacing: 2px;
    text-align: center;
    margin: 0;
    height: auto;
    line-height: 1.2;
    font-size: 16px;
    white-space: nowrap;
  }
  
  .scripts-list-section:not(.show-list) .list-actions {
    display: none;
  }
  
  .scripts-list-section:not(.show-list) .scripts-list-content {
    display: none;
  }
  
  .list-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .script-item {
    padding: 10px;
    margin: 4px;
    justify-content: center;
  }
  
  .script-info {
    text-align: center;
  }
  
  .script-date {
    display: none;
  }
  
  .script-icon {
    margin-right: 8px;
  }

  .delete-icon {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    object-fit: contain;
    vertical-align: middle;
  }
}
</style> 