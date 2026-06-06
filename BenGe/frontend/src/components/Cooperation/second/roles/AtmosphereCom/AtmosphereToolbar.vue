<template>
  <div class="toolball-wrapper">
    <div
      class="atmosphere-toolball"
      :style="toolballStyle"
      @mouseenter="isExpanded = true"
      @mousedown="startDrag"
    >
      <!-- 主按钮 -->
      <button class="main-button">
        <img
            src="../../../../../assets/second/role4.png"
            alt="氛围工具"
            class="icon-main"
        />
      </button>

      <!-- 展开面板 -->
      <transition name="panel-expand">
        <div
            v-if="isExpanded"
            class="tool-panel"
            :class="[panelDirection === 'left' ? 'panel-left' : 'panel-right']"
            @mousemove="handlePanelEnter"
            @mouseleave="handlePanelLeave"
            :style="panelHoverStyle"
        >
          <button
            v-for="(btn, i) in buttons"
            :key="btn.action"
            class="tool-button"
            :style="{ '--delay-index': i }"
            @click="$emit(btn.action)"
            @mouseenter="btn.hover = true"
            @mouseleave="btn.hover = false"
          >
            <img
              :src="btn.icon"
              :alt="btn.tooltip"
              class="icon-tool"
              :style="{
                transform: btn.hover ? 'scale(1.15)' : 'none',
                filter: btn.hover
                  ? 'drop-shadow(0 0 8px ' + btn.color + ')'
                  : 'none',
              }"
            />
            <span class="tooltip">{{ btn.tooltip }}</span>
          </button>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, watch} from "vue";

// 氛围设计师专用按钮配置
const buttons = ref([
  {
    icon: require("@/assets/icons/plus-atmosphere.svg"),
    action: "add-node",
    tooltip: "添加氛围节点",
    color: "rgba(135, 206, 235, 0.7)",
    hover: false,
  },
  {
    icon: require("@/assets/icons/link.svg"),
    action: "link-scene",
    tooltip: "关联场景",
    color: "rgba(221, 160, 221, 0.7)",
    hover: false,
  },
  {
    icon: require("@/assets/icons/export.svg"),
    action: "export-canvas",
    tooltip: "导出画板",
    color: "rgba(144, 238, 144, 0.7)",
    hover: false,
  },
  {
    icon: require("@/assets/icons/magic-wand.svg"),
    action: "ai-generate",
    tooltip: "AI生成氛围",
    color: "rgba(255, 215, 0, 0.7)",
    hover: false,
  },
]);
// 判断是否房主
import { isOwner } from "@/api/room";
import { socketState } from "@/stores/socket";
const addAiIntegrateButton = () => {
  if (isOwner(socketState.roomId)) {
    buttons.value.push({
      icon: require("@/assets/icons/AI.svg"),
      action: "ai-integrate",
      tooltip: "AI整合",
      color: "rgba(255, 215, 0, 0.7)",
      hover: false,
    });
  }
};

addAiIntegrateButton();
// 交互状态
const isExpanded = ref(false);
const isDragging = ref(false);
const position = ref({ x: 40, y: 40 });
const dragStartPos = ref(null);
const panelHoverPos = ref({ x: 0, y: 0 });

const panelDirection = ref('right');

const closeTimeout = ref(null);
const isPanelHovered = ref(false);


const handlePanelEnter = () => {
  isPanelHovered.value = true;
  if (closeTimeout.value) {
    clearTimeout(closeTimeout.value);
    closeTimeout.value = null;
  }
};

const handlePanelLeave = () => {
  isPanelHovered.value = false;
  closeTimeout.value = setTimeout(() => {
    isExpanded.value = false;
  }, 200);
};

const updatePanelDirection = () => {
  const viewportWidth = window.innerWidth;
  const ballCenterX = position.value.x + 28;
  panelDirection.value = ballCenterX > viewportWidth / 2 ? 'left' : 'right';
};

watch(position, updatePanelDirection, {immediate: true});

// 动态样式
const toolballStyle = computed(() => ({
  left: `${position.value.x}px`,
  top: `${position.value.y}px`,
  "--panel-width": `${buttons.value.length * 56 + 16}px`,
}));

const panelHoverStyle = computed(() => ({
  backgroundImage: `radial-gradient(200px circle at ${panelHoverPos.value.x}px ${panelHoverPos.value.y}px, rgba(176, 227, 255, 0.35), transparent 80%)`,
}));

const handlePanelMove = (e) => {
  const rect = e.currentTarget.getBoundingClientRect();
  panelHoverPos.value = {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top,
  };
};

// 拖拽逻辑
const startDrag = (e) => {
  e.preventDefault();
  e.stopPropagation();

  isDragging.value = true;

  // 添加安全检查，避免悬浮球移动到边界时出现报错问题
  let clientX, clientY;
  if (e.clientX !== undefined) {
    clientX = e.clientX;
    clientY = e.clientY;
  } else if (e.touches && e.touches.length > 0) {
    clientX = e.touches[0].clientX;
    clientY = e.touches[0].clientY;
  } else {
    clientX = position.value.x;
    clientY = position.value.y;
  }

  dragStartPos.value = {
    x: clientX - position.value.x,
    y: clientY - position.value.y,
  };

  const moveHandler = (moveEvent) => {
    if (!isDragging.value) return;

    let moveClientX, moveClientY;
    if (moveEvent.clientX !== undefined) {
      moveClientX = moveEvent.clientX;
      moveClientY = moveEvent.clientY;
    } else if (moveEvent.touches && moveEvent.touches.length > 0) {
      moveClientX = moveEvent.touches[0].clientX;
      moveClientY = moveEvent.touches[0].clientY;
    } else {
      return;
    }

    // 计算新位置并限制边界
    const toolballSize = 56;
    const newX = Math.max(0,
        Math.min(moveClientX - dragStartPos.value.x,
            window.innerWidth - toolballSize));
    const newY = Math.max(0,
        Math.min(moveClientY - dragStartPos.value.y,
            window.innerHeight - toolballSize));

    position.value = {
      x: newX,
      y: newY
    };

    updatePanelDirection();
  };


  const endHandler = () => {
    window.removeEventListener("mousemove", moveHandler);
    window.removeEventListener("touchmove", moveHandler);
    window.removeEventListener("mouseup", endHandler);
    window.removeEventListener("touchend", endHandler);
    isDragging.value = false;
  };

  window.addEventListener("mousemove", moveHandler);
  window.addEventListener("touchmove", moveHandler);
  window.addEventListener("mouseup", endHandler);
  window.addEventListener("touchend", endHandler);
};

// 发出事件
defineEmits([
  'add-node',
  'atmo-palette',
  'link-scene',
  'export-canvas',
  'ai-generate',
  'ai-integrate'
]);
</script>

<style scoped>
.toolball-wrapper {
  position: fixed;
  z-index: 1000;
}

.atmosphere-toolball {
  position: relative;
  z-index: 1000;
  display: flex;
  align-items: center;
  cursor: grab;
  user-select: none;
  -webkit-user-drag: none;
}

.atmosphere-toolball::after {
  content: '';
  position: absolute;
  width: calc(var(--panel-width), 300px);
  height: 56px;
  top: 0;
}

.atmosphere-toolball .panel-right:hover::after {
  left: 100%;
  width: var(--panel-width);
}

.tmosphere-toolball .panel-left:hover::after {
  right: 100%;
  width: var(--panel-width);
}


.atmosphere-toolball:active {
  cursor: grabbing;
}

.main-button {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15), 
              0 0 0 1px rgba(255, 255, 255, 0.1),
              0 0 20px 2px rgba(100, 200, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  display: grid;
  place-items: center;
  overflow: hidden;
}

.main-button:hover {
  transform: scale(1.05);
  background: linear-gradient(to top, #dce1ff, #ffffff);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2), 
              0 0 0 1px rgba(255, 255, 255, 0.2),
              0 0 30px 4px rgba(135, 206, 235, 0.7);
}

/* 图标样式 */
.icon-main {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.icon-tool {
  width: 22px;
  height: 22px;
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  border-radius: 50%;
  object-fit: cover;
}

.tool-panel {
  display: flex;
  padding: 0 12px;
  height: 56px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1),
  inset 0 0 0 1px rgba(255, 255, 255, 0.3);
  margin-left: 12px;
  position: absolute;
  align-items: center;
}

/* 面板左、右展开 */
.tool-panel.panel-right {
  left: calc(100% + 12px);
}
.tool-panel.panel-left {
  right: calc(100% + 12px);
  flex-direction: row-reverse;
}

.tool-button {
  position: relative;
  z-index: 1;
  width: 44px;
  height: 44px;
  margin: 6px;
  border-radius: 16px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.35);
  transition: all 0.3s ease calc(var(--delay-index, 0) * 50ms);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.4);
}

.tool-button:hover {
  background: linear-gradient(135deg, #b0e3ff, #ffffff);
  border: 1px solid #9faeff;
  box-shadow: 0 0 6px rgba(176, 227, 255, 0.6),
  0 0 10px rgba(159, 174, 255, 0.4);
  transform: scale(1.15);
}

.tool-button:hover .icon-tool {
  filter: drop-shadow(0 0 6px rgba(150, 200, 255, 0.6)) saturate(140%);
  transform: scale(1.1);
}

.tooltip {
  position: absolute;
  top: 50px;
  left: 50%;
  transform: translate(-50%, 8px);
  white-space: nowrap;
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  opacity: 0;
  transition: all 0.3s ease;
}

.tool-button:hover .tooltip {
  opacity: 1;
  transform: translate(-50%, 0);
}

/* 动画效果 */
.panel-expand-enter-active,
.panel-expand-leave-active {
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

.panel-expand-enter-from,
.panel-expand-leave-to {
  opacity: 0;
}

.panel-expand-enter-from.panel-right,
.panel-expand-leave-to.panel-right {
  transform: translateX(-20px);
}

.panel-expand-enter-from.panel-left,
.panel-expand-leave-to.panel-left {
  transform: translateX(20px);
}

.panel-expand-enter-to,
.panel-expand-leave-from {
  opacity: 1;
  transform: translateX(0);
}
</style> 