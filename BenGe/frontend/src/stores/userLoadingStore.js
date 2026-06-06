import { defineStore } from "pinia";
import { ref } from "vue";

export const userLoadingStore = defineStore("loading", () => {
  const loading = ref(false);
  const aiAssistantLoading = ref(false);
  const show = () => {
    loading.value = true;
  };

  const hide = () => {
    loading.value = false;
  };

  const showAiAssistantLoading = () => {
    aiAssistantLoading.value = true;
  };

  const hideAiAssistantLoading = () => {
    aiAssistantLoading.value = false;
  };

  // 第二阶段的加载动画
  const loading2 = ref(false);

  const show2 = () => {
    loading2.value = true;
  };

  const hide2 = () => {
    loading2.value = false;
  };

  // 第三阶段的加载动画
  const loading3 = ref(false);

  const show3 = () => {
    loading3.value = true;
  };

  const hide3 = () => {
    loading3.value = false;
  };

  return {
    loading,
    aiAssistantLoading,
    show,
    hide,
    showAiAssistantLoading,
    hideAiAssistantLoading,

    // 第二阶段
    loading2,
    show2,
    hide2,

    // 第三阶段
    loading3,
    show3,
    hide3,
  };
});
