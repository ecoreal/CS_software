import { defineStore } from 'pinia'
import { computed, ref , watch} from 'vue'
import { analyzeScript, generateCompleteScript, getSelectScript, updateScriptContent2 } from '@/api/script';
import { getAllScript, createScript, deleteScriptApi, updateScriptContent, generateElementImage } from '@/api/script';
import { filter } from 'lodash';
import { f } from 'html2pdf.js';

export const usescriptStore = defineStore('script', () => {
    const scripts = ref([]);
    const chatHistory = ref([]);
    const analysis = ref([]);
    const filteredScripts = ref([]);
    const visualElements = ref([]);
    const scriptSortOption = ref('data-desc');
    const scriptListSearchQuery = ref('');

    const scriptStage = ref(1); // 默认为第一阶段
    const scriptDirections = ref([]);
    const selectedDirection = ref(null);

    const selectScriptId = ref(-1);

    const streamingState = ref({
      isStreaming: false,
      originalContent: '',
      accumulatedContent: '',
      controller: null, // 用于取消流的 AbortController
      currentAIResponse: '' // 当前正在接收的AI响应
    });

    const scriptTitle = computed(() => {
      const script = filteredScripts.value.find(s => s.id === selectScriptId.value);
      return script?.title || "空标题";
    });
    
    const scriptContent = computed(() => {
        const script = filteredScripts.value.find(s => s.id === selectScriptId.value);
        return script?.content || "当前剧本内容为空";
    });

    const loadScripts = async () => {
      try {  
        const response = await getAllScript();
        
        // console.log("获取用户对应的剧本列表成功", response);

        if(response){
          scripts.value = response;
          filteredScripts.value = response;
        }
      } catch (error) {
        // console.error("获取用户对应的剧本列表出错");

        // if(error.response && error.response.data)
          // console.error("报错信息", error.response.data);
      }
    }

    const filterScripts = () => {
      let filtered = [...scripts.value];
      
      // 应用搜索过滤
      if (scriptListSearchQuery.value.trim()) {
        const query = scriptListSearchQuery.value.toLowerCase();
        filtered = filtered.filter(script => 
          (script.title && script.title.toLowerCase().includes(query)) || 
          (script.content && script.content.toLowerCase().includes(query))
        );
      }
      
      // 应用排序
      switch(scriptSortOption.value) {
        case 'date-desc':
          filtered.sort((a, b) => new Date(b.lastUpdated) - new Date(a.lastUpdated));
          break;
        case 'date-asc':
          filtered.sort((a, b) => new Date(a.lastUpdated) - new Date(b.lastUpdated));
          break;
        case 'title-asc':
          filtered.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
          break;
        case 'title-desc':
          filtered.sort((a, b) => (b.title || '').localeCompare(a.title || ''));
          break;
      }
      
      filteredScripts.value = filtered;
    };
      
    const convertScriptHistories = (histories) =>{
      if(!Array.isArray(histories)) return [];

      const convertedHistories = [];

      for(const history of histories){
        if(history.message && history.message.trim() !== ''){
          convertedHistories.push({
            sender: 'user',
            content: history.message,
            timestamp: new Date(history.createdAt)
          });
        }
        else if(history.response && history.response.trim() !== ''){
          convertedHistories.push({
            sender: 'ai',
            content: history.response,
            timestamp: new Date(history.createdAt)
          });
        }
      }

      return convertedHistories.sort((a, b) => a.timestamp - b.timestamp);
    }
      // 剧本操作相关
    const handleSelectScript = async (id) => {
      if(id !== selectScriptId.value){
        selectScriptId.value = id;

        // 提取剧本元素
        try {
            const response = await getSelectScript(id);

            // console.log("获取剧本详情成功", response);

            if (response){
                if(response.history){
                    // console.log("获取剧本历史记录成功", response.history);
                    chatHistory.value = convertScriptHistories(response.history);
                }
                if(response.visualElements)
                    visualElements.value = response.visualElements;
                  
                if(response.analysis && response.analysis.analysisResult)
                    analysis.value = response.analysis.analysisResult;
                else
                    analysis.value = [];
                // 设置剧本阶段
                if(response.script && response.script.stage) {
                    scriptStage.value = response.script.stage;
                } else {
                    // 如果没有阶段信息，默认为第一阶段
                    scriptStage.value = 1;
                }
                
                // 清空方向选择
                scriptDirections.value = [];
                selectedDirection.value = null;
            }
        } catch (error){
            // console.error("获取选择的剧本元素出错");
            // if(error.response && error.response.data)
                // console.error("报错信息", error.response.data);
        }
      }
    };
      
    const createNewScript = async () => {
      // 创建新剧本
      try{
        const response = await createScript();

        // console.log("创建新剧本成功", response);

        if(response){
          const responseScript = response.script;
          const newScript = {
            id: responseScript.id,
            title: responseScript.title,
            content: responseScript.content,
            lastUpdated: responseScript.lastUpdated,
            stage: responseScript.stage || 1 // 默认为第一阶段
          }
          scripts.value.push(newScript);
          filteredScripts.value.push(newScript);
          
          // 重置方向和阶段
          scriptDirections.value = [];
          selectedDirection.value = null;
          scriptStage.value = 1;
          
          // 选中新创建的剧本
          handleSelectScript(newScript.id);
        }
      } catch (error){
        // console.error("创建新剧本出错");


      }
    };
      
    // 删除剧本
    const deleteScript = async (id) => {
      try{
        // 发送删除请求
        const response = await deleteScriptApi(id);
        // console.log("删除剧本成功", response);

        // 从原始剧本和过滤列表中删除（通过id）
        scripts.value = scripts.value.filter(script => script.id !== id);
        filteredScripts.value = filteredScripts.value.filter(script => script.id !== id);

        if(id === selectScriptId.value){
          // 如果删除的是当前选中的剧本，则选择第一个剧本或清空当前选择
          if (filteredScripts.value.length > 0) {
            selectScriptId.value = filteredScripts.value[0].id;
            handleSelectScript(filteredScripts.value[0].id);
          } else {
            // 没有剩余剧本，重置状态
            selectScriptId.value = -1;
            chatHistory.value = [];
            visualElements.value = [];
            scriptDirections.value = [];
            selectedDirection.value = null;
            scriptStage.value = 1;
          }
        }
      } catch (error){

      }
    };
      
    // 搜索和排序
    const handleScriptSearch = (query) => {
      scriptListSearchQuery.value = query;
      filterScripts();
    };
    
    // 设置方向选项（添加selected字段）
    const setDirections = (directions) => {
      // console.log("设置方向选项", directions);
      if(Array.isArray(directions)){
        const formattedDirections = directions.map((slogan, index) => {
          const contentLines = slogan.content.split('\n');
          const content = contentLines.join('\n');
          const coreIdeaLines = slogan.coreIdea.split('\n');
          const coreIdea = coreIdeaLines.join('\n');

          return {
            id: index + 1,
            content : content || '',
            coreIdea: coreIdea || '',
            selected: false
          }
        });
      scriptDirections.value = formattedDirections;
      }
    }
    
    // 本地选择方向（不调用API）
    const selectDirectionLocally = (directionId) => {
      const direction = scriptDirections.value.find(dir => dir.id === directionId);
      if (!direction) {
        // console.error(`找不到ID为${directionId}的方向`);
        return false;
      }
      
      // 清除所有方向的selected状态
      scriptDirections.value.forEach(dir => {
        dir.selected = false;
      });
      
      // 设置选中的方向
      direction.selected = true;
      selectedDirection.value = direction;
      
      // console.log(`已在store中选择方向: ${direction.id}`);
      return true;
    };
    
    // 确认选择方向并提交到后端
    const confirmDirection = async () => {
      if (!selectedDirection.value) return false;
      
      try {
        const content = selectedDirection.value.content;
        const coreIdea = selectedDirection.value.coreIdea;

        // // console.log("确认方向", selectedDirection.value);
        // 调用API更新剧本内容和阶段
        const response = await updateScriptContent(selectScriptId.value, "# 核心谜题\n\n" + content + "\n\n---\n\n# 核心创意\n\n" + coreIdea, 2);
        
        if (response && response.script) {
          // 更新剧本内容和阶段
          const updatedScript = response.script;
          
          // 更新本地剧本数据
          const scriptIndex = scripts.value.findIndex(s => s.id === selectScriptId.value);
          if (scriptIndex !== -1) {
            scripts.value[scriptIndex] = updatedScript;
          }
          
          // 更新过滤后的剧本数据
          const filteredIndex = filteredScripts.value.findIndex(s => s.id === selectScriptId.value);
          if (filteredIndex !== -1) {
            filteredScripts.value[filteredIndex] = updatedScript;
          }
          
          // 更新当前阶段
          scriptStage.value = updatedScript.stage || 2;
          
          return true;
        }
        
        return false;
      } catch (error) {
        // console.error("确认方向失败", error);
        if (error.response && error.response.data)
          // console.error("报错信息", error.response.data);
        return false;
      }
    };

    // 更新剧本内容（第二阶段（非流式））
    const UpdateFramework = async (messageInput) => {
      if(messageInput.trim() === '') return false;
      try{
        const response = await updateScriptContent2(selectScriptId.value, messageInput);

        if(response && response.script){
          const updatedScript = response.script;

          // 更新本地剧本数据
          const scriptIndex = scripts.value.findIndex(s => s.id === selectScriptId.value);
          if (scriptIndex!== -1) {
            scripts.value[scriptIndex] = updatedScript;
          }

          // 更新过滤后的剧本数据
          const filteredIndex = filteredScripts.value.findIndex(s => s.id === selectScriptId.value);
          if (filteredIndex !== -1) {
            filteredScripts.value[filteredIndex] = updatedScript;
          }

          chatHistory.value = convertScriptHistories(response.dialogHistory);
          return true;
        }
      } catch (error){

      }
    }

    // 更新剧本内容（第二阶段（流式））
    const streamUpdateFramework = async (messageInput) => {
      if (messageInput.trim() === '') return false;
      if (selectScriptId.value === -1) return false;
      
      // 如果已有流在运行，先取消
      if (streamingState.value.isStreaming) {
        CloseStreaming();
      }

      // 准备流式状态
      const scriptId = selectScriptId.value;
      const scriptIndex = scripts.value.findIndex(s => s.id === scriptId);
      if (scriptIndex === -1) {
        console.error(`找不到ID为${scriptId}的剧本  in scriptStore.StreamUpdateFramework`);
        return false;
      }
      let scriptTemp = scripts.value[scriptIndex] || {};
      const filteredIndex = filteredScripts.value.findIndex(s => s.id === scriptId);
      if (filteredIndex === -1) {
        console.error(`找不到ID为${filteredIndex}的剧本 in scriptStore.StreamUpdateFramework`);
        return false;
      }
      let filteredScriptTemp = filteredScripts.value[filteredIndex] || {};

      streamingState.value = {
        isStreaming: true,
        originalContent: filteredScriptTemp.content,
        accumulatedContent: '',
        controller: new AbortController(),
        currentAIResponse: ''
      };


      try {
        // 创建新的流请求
        const response = await fetch('/api/ai/genframework/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
            'Connection': 'keep-alive'
          },
          body: JSON.stringify({
            scriptId,
            message: messageInput
          }),
          signal: streamingState.value.controller.signal
        });
        
        if (!response.ok) {
          throw new Error(`API请求失败: ${response.status}`);
        }
        if (!response.body) {
          throw new Error('响应体不可读');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let AIresponseOver = false;
        let TitleGenOver = false;
          while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            
            // 处理SSE消息
            const messages = buffer.split('data:');
            buffer = messages.pop() || '';
            
            for (const msg of messages) {
              try {
                const eventData = parseSSEMessage(msg);
                if (!eventData) continue;
                //console.log(eventData); // 明确标记输出内容
                // 处理流数据
                if (eventData.choices?.[0]?.delta) {
                  const delta = eventData.choices[0].delta;

                  if (delta.content) {
                    streamingState.value.accumulatedContent += delta.content;
                    
                    // 判断“标题”内容是否完成生成
                    if(!TitleGenOver){
                      let tempIndex = streamingState.value.accumulatedContent.indexOf("## 背景");
                      if(tempIndex !== -1){
                        TitleGenOver = true;
                        streamingState.value.currentAIResponse = streamingState.value.accumulatedContent.slice(0, tempIndex);
                        streamingState.value.accumulatedContent = streamingState.value.accumulatedContent.slice(tempIndex);
                        tempIndex = streamingState.value.currentAIResponse.indexOf("》》》");
                        // 更新本地剧本标题和AIResponseForUser
                        let titleTemp = streamingState.value.currentAIResponse.slice(tempIndex + 3).trim();
                        titleTemp = titleTemp.replace(/---/g, ''); // 去除标题中的---
                        titleTemp = titleTemp.replace(/#/g, '').trim(); // 去除标题中的#
                        if(tempIndex !== -1){
                          filteredScriptTemp = {
                            ...filteredScriptTemp,
                            title: titleTemp
                          };
                          scriptTemp = {
                            ...scriptTemp,
                            title: titleTemp
                          };
                          //console.log("script:",  filteredScriptTemp);
                          streamingState.value.currentAIResponse = streamingState.value.currentAIResponse.slice(0, tempIndex);
                        }
                      }
                    }

                    // 更新本地剧本数据
                    scriptTemp = {
                      ...scriptTemp,
                      content: streamingState.value.accumulatedContent
                    };
                    scripts.value[scriptIndex] = scriptTemp;

                    // 更新过滤后的剧本数据
                    filteredScriptTemp = {
                      ...filteredScriptTemp,
                      content: streamingState.value.accumulatedContent
                    };
                    filteredScripts.value[filteredIndex] = filteredScriptTemp;
                  }
                }
              } catch (e) {
                console.error('解析SSE数据失败:', e, '原始数据:', msg);
              }
            }
          }
          reader.releaseLock();
      } catch (error) {
        if (error.name !== 'AbortError') {
          console.error('流式更新出错:', error.message);
        }
        // 恢复剧本内容
        filteredScriptTemp = {
          ...filteredScriptTemp,
          content: streamingState.value.originalContent
        };
        return false;
      } finally {

        // 流结束后，更新对话记录，添加新消息
        chatHistory.value.push({
          sender: 'user', 
          content: messageInput,
          timestamp: new Date().toString()
        });

        chatHistory.value.push({
          sender: 'ai',
          content: streamingState.value.currentAIResponse || "已按照您的要求修改剧本",
          timestamp: new Date().toString()
        });

        // 重置流状态
        streamingState.value = {
          isStreaming: false,
          originalContent: '',
          accumulatedContent: '',
          controller: null,
          currentAIResponse: ''
        };
        return true;
      }
    };

    // 辅助函数：解析SSE消息
    function parseSSEMessage(msg) {
      if (!msg.trim()) return null;
      if(msg.startsWith('event')) return null; 
      let temp = msg;
      if(temp.startsWith('data:')) {
        temp = temp.substring(5).trim();
      }
      temp = temp.slice(0,temp.indexOf('retry')).trim();
      try {
        if(temp.startsWith('{')){
          return JSON.parse(temp);
        }
        else{
          return null;
        }
      } catch (e) {
        console.error('解析JSON失败:', e);
        return null;
      }
    }

    // 辅助函数：取消流式传输
    const CloseStreaming = () => {
      if (streamingState.value.isStreaming && streamingState.value.controller) {
        streamingState.value.controller.abort();
        streamingState.value.controller = null;
        streamingState.value.isStreaming = false;
      }
    };

    // 更新剧本分析
    const UpdateAnalysis = async () => {
          if(selectScriptId.value === -1) return;
          if(scriptContent.value === '') return;

          try{
            const response = await analyzeScript(selectScriptId.value, scriptContent.value);

            if(response && response.analysisResult){
              analysis.value = response.analysisResult;
            }
          } catch (error){

          }
        }

    // 生成完整内容
    const GenerateScript = async () => {
      // console.log("生成完整内容");

      if(selectScriptId.value === -1) return;
      if(scriptContent.value === '') return;

      try{
        const response = await generateCompleteScript(selectScriptId.value, scriptContent.value);
        // console.log("生成完整内容成功", response);
        if(response && response.script){
          // 用返回的script更新本地的scripts和filteredScripts
          const updatedScript = response.script;
          const scriptIndex = scripts.value.findIndex(s => s.id === selectScriptId.value);
          if (scriptIndex!== -1) {
            scripts.value[scriptIndex] = updatedScript;
          }

          const filteredIndex = filteredScripts.value.findIndex(s => s.id === selectScriptId.value);
          if (filteredIndex!== -1) {
            filteredScripts.value[filteredIndex] = updatedScript;
          }

          // 更新剧本阶段
          scriptStage.value = updatedScript.stage || 3;

          //console.log("test here:",response.visualElements);
          if(response.visualElements){
            visualElements.value = response.visualElements; 
            //console.log("test3 here:" , visualElements);
          }
        }
      } catch (error){

      }
    }
    // 用于流式添加单个剧本方向
    const addDirection = (direction) => {
      // 确保direction有id属性
      if (!direction.id) {
        direction.id = scriptDirections.value.length + 1;
      }
      scriptDirections.value.push(direction);
    };

    // 清空所有方向
    const clearDirections = () => {
      scriptDirections.value = [];
    }

    // 生成可视化的照片
    const generateVisualElementImage = async (visualElementId) => {
      if(selectScriptId.value === -1) return;
      if(visualElementId === -1) return;

      try{
        const element = visualElements.value.find(el => el.id === visualElementId);
        if(element){
          const response = await generateElementImage(selectScriptId.value, visualElementId);
          if(response){
            const index = visualElements.value.findIndex(el => el.id === visualElementId);
            if(index !== -1){
              visualElements.value[index] = {
                ...visualElements.value[index],
                imageUrl: response,
                imageGeneratedAt: new Date()
              };
            }
          }
          return response;
        }
        return null;
      } catch (error){
        console.error("生成可视化元素图片失败", error);
        if(error.response && error.response.data)
          // console.error("报错信息", error.response.data); 
        return null;
      }
    }

    const getVisualElementsByType = (type) => {
      return visualElements.value.filter(el => el.type === type);
    }

    const getVisualElementById = (id) => {
      return visualElements.value.find(el => el.id === id);
    }

    return {
      scripts,
      filteredScripts,
      scriptStage,
      scriptDirections,
      selectedDirection,
      selectScriptId,
      scriptTitle,
      scriptContent,
      chatHistory,
      analysis,
      visualElements,
      scriptSortOption,
      scriptListSearchQuery,
      loadScripts,
      filterScripts,
      handleSelectScript,
      createNewScript,
      deleteScript,
      handleScriptSearch,
      setDirections,
      selectDirectionLocally,
      confirmDirection,
      UpdateFramework,
      streamUpdateFramework,
      UpdateAnalysis,
      GenerateScript,
      addDirection, 
      clearDirections,
      generateVisualElementImage,
      getVisualElementsByType, 
      getVisualElementById
    };
});

watch(() => usescriptStore().scriptSortOption, () => usescriptStore().filterScripts());
