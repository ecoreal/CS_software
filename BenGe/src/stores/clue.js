import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { socketState } from "./socket";
import { debounce } from "lodash";

export const useClueStore = defineStore("clueStore", () => {
  // 生成结点的ID
  const generateNodeId = () =>
    "node-" + Date.now() + "-" + Math.floor(Math.random() * 1000);

  // 所有线索节点数据
  const nodes = ref([


  ]);

  // 所有连线
  const edges = reactive([

  ]);

  // 当前选择结点
  const selectedNode = ref(null);

  // 当前选择边的ID
  const editingEdgeId = ref(null);

  // 是否在创建边
  const isCreatingEdge = ref(false);

  // 通过工具栏创建边时，存放的两个结点
  const selectedNodesForEdge = ref([]);

  // 是否展示边选择器
  const showEdgeSelector = ref(false);

  // 用户点击创建边按钮时，调用此方法
  const handleCreateEdgeClick = () => {
    // console.log("点击创建边按钮");
    isCreatingEdge.value = true;
    selectedNodesForEdge.value = [];
    showEdgeSelector.value = false;
  };

  // 用户点击边时，进入边选择器
  const handleEdgeSelect = (edgeId) => {
    // console.log("点击的边的Id", edgeId);
    editingEdgeId.value = edgeId;
    showEdgeSelector.value = true;
  };

  // 用户在关系选择器中，确认创建线索关系
  const handleEdgeConfirm = (edgeType, edgeLabel) => {
    if (selectedNodesForEdge.value.length === 2) {
      const [sourceNode, targetNode] = selectedNodesForEdge.value;

      // 创建线索关系边
      const newEdge = reactive({
        id: `clue-edge-${sourceNode.id}-${targetNode.id}-${Date.now()}`,
        source: sourceNode.id,
        target: targetNode.id,
        sourceHandle: "right-source",
        targetHandle: "left",
        type: "clue-edge",
        data: reactive({
          type: edgeType,
          label: edgeLabel || edgeType,
        }),
      });

      edges.push(newEdge);
      broadcast();
    }

    selectedNodesForEdge.value = [];
    isCreatingEdge.value = false;
    showEdgeSelector.value = false;
  };

  // 用户在关系选择器中修改关系，确认
  const handleEdgeEditConfirm = (edgeType, edgeLabel) => {
    if (!editingEdgeId.value) return;

    const edge = edges.find((e) => e.id === editingEdgeId.value);
    if (edge) {
      // 更新线索边数据
      edge.data.type = edgeType;
      edge.data.label = edgeLabel || edgeType;

      editingEdgeId.value = null;
      showEdgeSelector.value = false;
    }
    broadcast();
  };

  // 用户在边选择器中，取消创建边
  const handleEdgeCancel = () => {
    selectedNodesForEdge.value = [];
    isCreatingEdge.value = false;
    showEdgeSelector.value = false;
    editingEdgeId.value = null; // 重置编辑状态
  };

  // 在边选择器中，删除边
  const handleDeleteEdge = () => {
    const index = edges.findIndex((e) => e.id === editingEdgeId.value);
    if (index !== -1) {
      edges.splice(index, 1);
    }

    // forceUpdateNode(payload.nodeId, payload.nodeData);
    editingEdgeId.value = null;
    showEdgeSelector.value = false;

    broadcast();
  };

  // 连接结点
  const handleConnectNode = (newEdge) => {
    edges.push(newEdge);
    broadcast();
  };

  // 点击结点，进入结点的信息编辑界面或者是在创建边
  const handleNodeClick = (node) => {
    const actualNode = node.node || node; // 处理两种可能的情况

    if (isCreatingEdge.value) {
      if (!selectedNodesForEdge.value.find((n) => n.id === actualNode.id)) {
        selectedNodesForEdge.value.push(actualNode);
        broadcast();
      }

      if (selectedNodesForEdge.value.length === 2) {
        showEdgeSelector.value = true;
      }
    } else {
      selectedNode.value = { ...actualNode };
    }
  };

  // 修改结点信息的保存
  const handleDetailSave = (updatedData) => {
    // console.log("保存的节点数据：", updatedData);

    if (!updatedData || !updatedData.id || !updatedData.data) {
      // console.warn("[handleDetailSave] 无效参数：", updatedData);
      return -1;
    }

    let index = -1;

    // 如果当前有选中节点，才尝试查找并更新
    if (selectedNode.value) {
      index = nodes.value.findIndex((n) => n.id === updatedData.id);

      if (index !== -1) {
        nodes.value[index].data = {
          ...nodes.value[index].data,
          ...updatedData.data,
        };

        // ✅ 强制触发响应式更新
        nodes.value[index] = { ...nodes.value[index] };

        // console.log("更新后的节点数据：", nodes.value[index]);
      } else {
        // console.warn("未找到对应的节点 ID:", updatedData.id);
        return -1;
      }
    } else {
      // console.warn("无选中节点，可能是编辑逻辑未正确触发");
      return -1;
    }

    // 最后收起面板
    selectedNode.value = null;

    broadcast();
    return index;
  };

  const handleAddClueNode = (event) => {
    const rect = event?.target?.getBoundingClientRect();
    const x = rect ? event.clientX - rect.left : Math.random() * 300 + 100;
    const y = rect ? event.clientY - rect.top : Math.random() * 300 + 100;

    const newNode = {
      id: generateNodeId(),
      type: "clue", // 关键点：类型改为 clue
      position: { x, y },
      data: {
        title: "新线索",
        relatedEvent: "", // 关联的事件
        detail: "", // 线索内容
        logic: "", // 推理逻辑
        tags: "", // 标签（逗号分隔）
        notes: "", // 备注
      },
    };

    nodes.value.push(newNode);

    broadcast();
  };
  const handleAddInferenceNode = (event) => {
    const rect = event?.target?.getBoundingClientRect();
    const x = rect ? event.clientX - rect.left : Math.random() * 300 + 100;
    const y = rect ? event.clientY - rect.top : Math.random() * 300 + 100;

    const newNode = {
      id: generateNodeId(),
      type: "inference",
      position: { x, y },
      data: {
        title: "推导结论1",
        summary: "总结",
        evidence: "证据",
        tags: "",
        notes: "",
      },
    };

    nodes.value.push(newNode);

    broadcast();
  };
  const handleAddPersonNode = (event) => {
    const rect = event?.target?.getBoundingClientRect();
    const x = rect ? event.clientX - rect.left : Math.random() * 300 + 100;
    const y = rect ? event.clientY - rect.top : Math.random() * 300 + 100;

    const newNode = {
      id: generateNodeId(),
      type: "person", // 使用你在 VueFlow 中注册的人物节点组件类型
      position: { x, y },
      data: {
        name: "新人物",
        bio: "人物背景简介",
        clues: [], // 与线索有关的ID或标题
        tags: ["可疑"], // 人物特点标签
        notes: "", // 附加备注
      },
    };

    nodes.value.push(newNode);

    broadcast(); // 如果你在同步节点给其他协作者，这一行保留
  };

  // 结点的删除
  const handleDeleteNode = (nodeId) => {
    const index = nodes.value.findIndex((n) => n.id === nodeId);
    if (index !== -1) {
      nodes.value.splice(index, 1);

      if (selectedNode.value?.id === nodeId) {
        selectedNode.value = null;
      }

      for (let i = edges.length - 1; i >= 0; i--) {
        if (edges[i].source === nodeId || edges[i].target === nodeId) {
          edges.splice(i, 1);
        }
      }
    }
    broadcast();
  };

  // 处理结点的位置变化
  const handlePositionChange = (payload) => {
    const { id, position } = payload;
    const nodeIndex = nodes.value.findIndex((n) => n.id === id);
    if (nodeIndex !== -1) {
      nodes.value[nodeIndex].position = position;
      nodes.value[nodeIndex] = { ...nodes.value[nodeIndex] }; // ✅ 强制 Vue 感知变化
      // // console.log(`[DEBUG] 节点 ${id} 位置已更新为：`, nodes.value);
    }
    broadcast();
  };
  // 广播节点和边的信息
  const broadcast = debounce(() => {
    if (
      socketState?.socket &&
      socketState.socket.readyState === WebSocket.OPEN
    ) {
      const clueNodes = nodes.value.filter((n) => n.type === "clue");
      const inferenceNodes = nodes.value.filter((n) => n.type === "inference");
      const personNodes = nodes.value.filter((n) => n.type === "person");

      const message = {
        type: "clue",
        clueNodes,
        inferenceNodes,
        personNodes,
        clueEdges: edges,
      };

      socketState.socket.send(JSON.stringify(message));
      // console.log("广播的节点信息：", message);
    } else {
      // console.error("未连接websocket");
    }
  }, 300); // 300ms 内重复调用只执行一次

  return {
    nodes,
    edges,
    selectedNode,
    editingEdgeId,
    isCreatingEdge,
    selectedNodesForEdge,
    showEdgeSelector,
    handleCreateEdgeClick,
    handleEdgeSelect,
    handleEdgeConfirm,
    handleEdgeEditConfirm,
    handleEdgeCancel,
    handleDeleteEdge,
    handleConnectNode,
    handleNodeClick,
    handleDetailSave,
    handleAddClueNode,
    handleAddInferenceNode,
    handleAddPersonNode,
    handleDeleteNode,
    handlePositionChange,
  };
});
