// Vue Flow 终极导出工具 - 背景+节点完美结合
import html2canvas from 'html2canvas'

export class UltimateVueFlowExporter {
  
  /**
   * 使用画布合成的方式导出 - 背景和内容分别处理再合并
   */
  static async exportVueFlowWithBackground(format = 'png', filename = 'canvas') {
    try {
      // 第一步：创建背景画布
      const backgroundCanvas = await this.createBackgroundCanvas();
      
      // 第二步：创建内容画布（节点和边）
      const contentCanvas = await this.createContentCanvas();
      
      // 第三步：合并两个画布
      const finalCanvas = this.mergeCanvases(backgroundCanvas, contentCanvas);
      
      // 第四步：下载合并后的图片
      return this.downloadCanvas(finalCanvas, format, filename);
      
    } catch (error) {
      // console.error('导出失败:', error);
      // 降级方案
      return this.fallbackExport(format, filename);
    }
  }
  
  /**
   * 创建背景画布
   */
  static async createBackgroundCanvas() {
    return new Promise((resolve) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      
      // 获取 Vue Flow 容器尺寸
      const vueFlowElement = document.querySelector('.vue-flow');
      const width = vueFlowElement ? vueFlowElement.offsetWidth : 1200;
      const height = vueFlowElement ? vueFlowElement.offsetHeight : 800;
      
      canvas.width = width;
      canvas.height = height;
      
      // 设置白色背景
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, width, height);
      
      // 先尝试从实际元素获取背景图片
      const actualBgImage = this.getBackgroundImageFromElement();
      
      if (actualBgImage) {
        // console.log('找到实际背景图片:', actualBgImage);
        const img = new Image();
        img.crossOrigin = 'anonymous';
        
        img.onload = () => {
          // console.log('背景图片加载成功');
          const pattern = ctx.createPattern(img, 'repeat');
          ctx.fillStyle = pattern;
          ctx.fillRect(0, 0, width, height);
          resolve(canvas);
        };
        
        img.onerror = () => {
          // console.warn('背景图片加载失败，使用白色背景');
          resolve(canvas);
        };
        
        img.src = actualBgImage;
        
        // 超时保护
        setTimeout(() => {
          // console.warn('背景图片加载超时');
          resolve(canvas);
        }, 5000);
      } else {
        // console.warn('无法获取背景图片，使用白色背景');
        resolve(canvas);
      }
    });
  }
  
  /**
   * 从实际元素获取背景图片URL
   */
  static getBackgroundImageFromElement() {
    const vueFlowElement = document.querySelector('.vue-flow');
    if (!vueFlowElement) return null;
    
    const computedStyle = window.getComputedStyle(vueFlowElement);
    const backgroundImage = computedStyle.backgroundImage;
    
    // console.log('元素背景图片样式:', backgroundImage);
    
    if (backgroundImage && backgroundImage !== 'none') {
      // 提取URL
      const match = backgroundImage.match(/url\(["']?([^"')]+)["']?\)/);
      if (match && match[1]) {
        let imageUrl = match[1];
        
        // 处理相对路径
        if (imageUrl.startsWith('./') || imageUrl.startsWith('../')) {
          imageUrl = new URL(imageUrl, window.location.href).href;
        } else if (imageUrl.startsWith('/')) {
          imageUrl = window.location.origin + imageUrl;
        }
        
        return imageUrl;
      }
    }
    
    return null;
  }
  
  /**
   * 创建内容画布（只有节点和边，透明背景）
   */
  static async createContentCanvas() {
    const vueFlowElement = document.querySelector('.vue-flow');
    if (!vueFlowElement) {
      throw new Error('找不到 Vue Flow 元素');
    }
    
    // 临时隐藏背景，只截取内容
    const originalBgImage = vueFlowElement.style.backgroundImage;
    const originalBgColor = vueFlowElement.style.backgroundColor;
    
    vueFlowElement.style.backgroundImage = 'none';
    vueFlowElement.style.backgroundColor = 'transparent';
    
    const options = {
      useCORS: true,
      allowTaint: true,
      scale: 1,
      backgroundColor: null, // 透明背景
      width: vueFlowElement.offsetWidth,
      height: vueFlowElement.offsetHeight,
      onclone: (clonedDoc) => {
        // 确保节点可见
        const nodes = clonedDoc.querySelectorAll('[data-id], .vue-flow__node, .vue-flow__edge');
        nodes.forEach(node => {
          node.style.visibility = 'visible';
          node.style.opacity = '1';
        });
        
        // 移除背景
        const clonedVueFlow = clonedDoc.querySelector('.vue-flow');
        if (clonedVueFlow) {
          clonedVueFlow.style.backgroundImage = 'none';
          clonedVueFlow.style.backgroundColor = 'transparent';
        }
      }
    };
    
    try {
      const canvas = await html2canvas(vueFlowElement, options);
      
      // 恢复原始背景
      vueFlowElement.style.backgroundImage = originalBgImage;
      vueFlowElement.style.backgroundColor = originalBgColor;
      
      return canvas;
    } catch (error) {
      // 恢复原始背景
      vueFlowElement.style.backgroundImage = originalBgImage;
      vueFlowElement.style.backgroundColor = originalBgColor;
      throw error;
    }
  }
  
  /**
   * 合并背景画布和内容画布
   */
  static mergeCanvases(backgroundCanvas, contentCanvas) {
    const finalCanvas = document.createElement('canvas');
    const ctx = finalCanvas.getContext('2d');
    
    // 设置最终画布尺寸
    finalCanvas.width = Math.max(backgroundCanvas.width, contentCanvas.width);
    finalCanvas.height = Math.max(backgroundCanvas.height, contentCanvas.height);
    
    // 先绘制背景
    ctx.drawImage(backgroundCanvas, 0, 0);
    
    // 再绘制内容（节点和边）
    ctx.drawImage(contentCanvas, 0, 0);
    
    return finalCanvas;
  }
  
  /**
   * 下载画布
   */
  static downloadCanvas(canvas, format, filename) {
    const mimeType = format === 'jpg' ? 'image/jpeg' : 'image/png';
    const quality = format === 'jpg' ? 0.9 : undefined;
    const imgData = canvas.toDataURL(mimeType, quality);
    
    const link = document.createElement('a');
    link.href = imgData;
    link.download = `${filename}.${format}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    return { success: true, data: imgData };
  }
  
  /**
   * 降级方案
   */
  static async fallbackExport(format, filename) {
    const vueFlowElement = document.querySelector('.vue-flow');
    
    const options = {
      useCORS: true,
      allowTaint: true,
      scale: 1,
      backgroundColor: '#ffffff'
    };
    
    const canvas = await html2canvas(vueFlowElement || document.body, options);
    return this.downloadCanvas(canvas, format, filename);
  }
  
  /**
   * PNG 导出
   */
  static async exportAsPNG(filename = 'canvas') {
    return this.exportVueFlowWithBackground('png', filename);
  }
  
  /**
   * JPG 导出
   */
  static async exportAsJPG(filename = 'canvas') {
    return this.exportVueFlowWithBackground('jpg', filename);
  }
}