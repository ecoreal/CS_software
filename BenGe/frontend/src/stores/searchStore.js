import { defineStore } from "pinia";
import { ref } from "vue";

export const useSearchStore = defineStore("search", () => {
    const focusedNodeId = ref(null);
    const vueFlowApi = ref(null);

    function setFocusedNodeId(nodeId) {
        focusedNodeId.value = nodeId;
    }

    function setVueFlowApi(api) {
        if (!api || typeof api.getNodes !== 'function') {
            // console.error('Invalid API received:', api);
            return false;
        }

        vueFlowApi.value = {
            getNodes: () => api.getNodes(), // 确保总是返回最新节点
            viewport: api.viewport,
            dimensions: api.dimensions,
            setViewport: api.setViewport,
            fitView: api.fitView
        };

        // console.log('VueFlow API successfully set:', vueFlowApi.value);
        return true;
    }

    async function focusNode(nodeId) {
        if (!vueFlowApi.value) {
            // console.warn('VueFlow API not initialized');
            return false;
        }

        try {
            // 确保获取的是数组
            const nodes = vueFlowApi.value.getNodes();
            // console.log('Available nodes:', nodes); // 调试日志

            if (!Array.isArray(nodes)) {
                throw new Error('getNodes() did not return an array');
            }

            const node = nodes.find(n => n.id === nodeId);
            if (!node) {
                // console.warn(`Node ${nodeId} not found`);
                return false;
            }

            const dimensions = vueFlowApi.value.dimensions();
            const defaultZoom = 1; // 默认缩放值

            await vueFlowApi.value.setViewport({
                x: -node.position.x + dimensions.width / 2 / defaultZoom,
                y: -node.position.y + dimensions.height / 2 / defaultZoom,
                zoom: defaultZoom
            }, { duration: 800 });

            // console.log('Node focused successfully:', nodeId);
            return true;

        } catch (error) {
            // console.error('Focus node failed:', error);
            return false;
        }
    }

    return {
        focusedNodeId,
        setFocusedNodeId,
        setVueFlowApi,
        focusNode,
        vueFlowApi,
    };
});
