<template>
  <div class="search-panel" @keydown="handleKeydown">
    <img class="search-icon-left" src="@/assets/second/search1.png" alt="search icon" />
    <input type="text" placeholder="Search..." v-model="query" @keyup.enter="onSearch"/>
    <button class="search-button" @click="onSearch">
      <img class="search-icon-right" src="@/assets/second/search1.png" alt="search icon" />
    </button>

    <!-- 搜索结果展示区域 -->
    <transition name="fade">
      <div v-if="search.length > 0 && query && query.length > 0" class="search-results">
        <ul>
          <li
              v-for="(node, index) in search"
              :key="index"
              class="search-result-item"
              :class="{ 'selected': selectedItemIndex === index }"
              @click="handleNodeClick(node)"
              @mouseover="selectedItemIndex = index"
              @mouseout="selectedItemIndex = -1"
          >
            <p>{{ node.data.title || node.data.name }}</p>
          </li>
        </ul>
      </div>
      <!-- 如果没有搜索结果，显示提示 -->
      <div v-else-if="query && query.length > 0 && searching.value" class="search-results">
        <p>No results found for "{{ query }}"</p>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useCanvasStore } from '@/stores/canvasStore'
import { useCharacterStore } from '@/stores/character'
import { useClueStore } from '@/stores/clue'
import { useAtmosphereStore } from '@/stores/atmosphere'
import { useSearchStore } from '@/stores/searchStore'

const canvasStore = useCanvasStore()
const characterStore = useCharacterStore()
const clueStore = useClueStore()
const atmosphereStore = useAtmosphereStore()
const searchStore = useSearchStore()

const query = ref('')
// 搜索结果
const search = ref([])
const searching = ref(false)
// 当前选中的搜索结果项索引
const selectedItemIndex = ref(-1)

// 触发搜索逻辑
function onSearch() {
  updateSearchResults()
}

// 更新搜索结果
function updateSearchResults() {
  // 过滤出符合搜索条件的节点
  search.value = nodes.value.filter(node => {
    return (node.data.title && node.data.title.includes(query.value)) ||
        (node.data.name && node.data.name.includes(query.value))
  })

  // 输出搜索结果，方便调试
  // console.log('Search results:', search.value)
  searching.value = true
  selectedItemIndex.value = -1 // 重置选中的项
}

const handleNodeClick = async (node) => {
  if (!node?.id) return

  // console.log('Attempting to focus node:', node.id)
  // console.log('Current searchStore API:', searchStore.vueFlowApi)

  const success = await searchStore.focusNode(node.id)
  if (!success) {
    // console.error('Failed to focus node after first attempt, retrying...')
    await new Promise(resolve => setTimeout(resolve, 300))
    await searchStore.focusNode(node.id)
  }
}

// 直接使用 store 中的数据驱动画布
const nodes = computed(() => [
  ...(canvasStore.nodes || []),
  ...(characterStore.nodes || []),
  ...(clueStore.nodes || []),
  ...(atmosphereStore.nodes || []),
])

const edges = computed(() => [
  ...(canvasStore.edges || []),
  ...(characterStore.edges || []),
  ...(clueStore.edges || []),
  ...(atmosphereStore.edges || []),
])

// 监听 query 的变化并更新搜索结果
watch(query, (newQuery) => {
  if (newQuery.length > 0) {
    searching.value = false
    updateSearchResults()
  } else {
    search.value = []
    searching.value = false
    selectedItemIndex.value = -1 // 重置选中的项
  }
})

// 处理键盘事件
function handleKeydown(event) {
  if (search.value.length === 0) return

  switch (event.key) {
    case 'ArrowUp':
      if (selectedItemIndex.value > 0) {
        selectedItemIndex.value--
      } else {
        selectedItemIndex.value = search.value.length - 1
      }
      break
    case 'ArrowDown':
      if (selectedItemIndex.value < search.value.length - 1) {
        selectedItemIndex.value++
      } else {
        selectedItemIndex.value = 0
      }
      break
    case 'Enter':
      if (selectedItemIndex.value >= 0 && selectedItemIndex.value < search.value.length) {
        handleNodeClick(search.value[selectedItemIndex.value])
      }
      break
  }
}
</script>

<style scoped>
.search-panel {
  position: fixed;
  top: 50px; /* margin-top 50px */
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  height: 50px;
  background: #E7F0F9;
  border-radius: 24px;
  box-shadow: 0 2px 6px rgb(0 0 0 / 0.1);

  display: flex;
  align-items: center;
  padding: 0 10px;
  gap: 10px;
  box-sizing: border-box;
  z-index: 1001;
}

.search-icon-left {
  width: 24px;
  height: 24px;
}

input {
  flex: 1;
  height: 32px;
  border: none;
  outline: none;
  font-size: 16px;

  background-color: #E7F0F9;
}

.search-button {
  width: 40px;
  height: 40px;
  background-color: #3b82f6; /* 蓝色背景 */
  border: none;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-icon-right {
  width: 20px;
  height: 20px;
  filter: invert(100%); /* 白色图标，如果你的图标是黑色可以用这个反转 */
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;

  margin-top: 10px;
  background-color: white;
  border: 1px solid #ddd;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;

  border-radius: 20px;
}

.search-result-item {
  padding: 10px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.search-result-item:hover,
.search-result-item.selected {
  background-color: #D2D6F4;
}

.no-results {
  margin-top: 10px;
  color: #888;
  font-size: 14px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
.fade-enter-to, .fade-leave-from {
  opacity: 1;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: white;
  border: 1px solid #ddd;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  padding: 10px;
}
</style>