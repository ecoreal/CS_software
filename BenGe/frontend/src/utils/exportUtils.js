// 画板导出工具类
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

export class CanvasExporter {
  
  /**
   * 导出画板为PNG图片
   * @param {HTMLElement} canvasElement - 画板DOM元素
   * @param {string} filename - 文件名
   * @param {object} options - 导出选项
   */
  static async exportAsPNG(canvasElement, filename = 'canvas', options = {}) {
    // 等待所有节点渲染完成
    await this.waitForNodesRender();

    const defaultOptions = {
      useCORS: true,
      allowTaint: true,
      scale: 2,
      backgroundColor: '#ffffff',
      width: canvasElement.scrollWidth || 1200,
      height: canvasElement.scrollHeight || 800,
      logging: true,
      ignoreElements: (element) => {
        // 忽略某些不需要的元素
        return element.classList.contains('vue-flow__controls') ||
               element.classList.contains('vue-flow__minimap') ||
               element.classList.contains('vue-flow__panel');
      },
      onclone: (clonedDoc) => {
        // 确保克隆的文档包含所有样式
        const styles = document.querySelectorAll('style, link[rel="stylesheet"]');
        styles.forEach(style => {
          clonedDoc.head.appendChild(style.cloneNode(true));
        });
      },
      ...options
    }

    try {
      // console.log('开始PNG导出，画板元素:', canvasElement);
      // console.log('画板子元素数量:', canvasElement.children.length);
      
      const canvas = await html2canvas(canvasElement, defaultOptions)
      const imgData = canvas.toDataURL('image/png')
      
      const link = document.createElement('a')
      link.href = imgData
      link.download = `${filename}.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      return { success: true, data: imgData }
    } catch (error) {
      // console.error('PNG导出失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 等待节点渲染完成
   */
  static async waitForNodesRender() {
    return new Promise(resolve => {
      // 等待所有图片和样式加载完成
      const images = document.querySelectorAll('.vue-flow img');
      const promises = Array.from(images).map(img => {
        if (img.complete) return Promise.resolve();
        return new Promise(resolve => {
          img.onload = resolve;
          img.onerror = resolve; // 即使加载失败也继续
        });
      });
      
      Promise.all(promises).then(() => {
        // 额外等待一点时间确保Vue组件完全渲染
        setTimeout(resolve, 300);
      });
    });
  }

  /**
   * 导出画板为JPG图片
   * @param {HTMLElement} canvasElement - 画板DOM元素
   * @param {string} filename - 文件名
   * @param {object} options - 导出选项
   */
  static async exportAsJPG(canvasElement, filename = 'canvas', options = {}) {
    // 等待所有节点渲染完成
    await this.waitForNodesRender();

    const defaultOptions = {
      useCORS: true,
      allowTaint: true,
      scale: 2,
      backgroundColor: '#ffffff',
      width: canvasElement.scrollWidth || 1200,
      height: canvasElement.scrollHeight || 800,
      logging: true,
      ignoreElements: (element) => {
        return element.classList.contains('vue-flow__controls') ||
               element.classList.contains('vue-flow__minimap') ||
               element.classList.contains('vue-flow__panel');
      },
      onclone: (clonedDoc, element) => {
        const styles = document.querySelectorAll('style, link[rel="stylesheet"]');
        styles.forEach(style => {
          clonedDoc.head.appendChild(style.cloneNode(true));
        });
      },
      ...options
    }

    try {
      // console.log('开始JPG导出，画板元素:', canvasElement);
      
      const canvas = await html2canvas(canvasElement, defaultOptions)
      const imgData = canvas.toDataURL('image/jpeg', 0.9)
      
      const link = document.createElement('a')
      link.href = imgData
      link.download = `${filename}.jpg`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      return { success: true, data: imgData }
    } catch (error) {
      // console.error('JPG导出失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 导出画板数据为JSON
   * @param {object} canvasData - 画板数据
   * @param {string} filename - 文件名
   */
  static exportAsJSON(canvasData, filename = 'canvas-data') {
    try {
      const jsonData = JSON.stringify(canvasData, null, 2)
      
      const blob = new Blob([jsonData], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      
      const link = document.createElement('a')
      link.href = url
      link.download = `${filename}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      URL.revokeObjectURL(url)
      return { success: true, data: jsonData }
    } catch (error) {
      // console.error('JSON导出失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 导出画板为PDF
   * @param {HTMLElement} canvasElement - 画板DOM元素
   * @param {object} canvasData - 画板数据
   * @param {string} filename - 文件名
   */
  static async exportAsPDF(canvasElement, canvasData, filename = 'canvas') {
    try {
      // 首先获取画板截图
      const canvas = await html2canvas(canvasElement, {
        useCORS: true,
        scale: 2,
        backgroundColor: '#ffffff',
        width: canvasElement.scrollWidth,
        height: canvasElement.scrollHeight,
        logging: false
      })
      
      const imgData = canvas.toDataURL('image/png')
      
      // 创建PDF文档
      const pdf = new jsPDF({
        orientation: 'landscape',
        unit: 'mm',
        format: 'a4'
      })
      
      // 添加画板图片
      const imgWidth = 280
      const imgHeight = (canvas.height * imgWidth) / canvas.width
      pdf.addImage(imgData, 'PNG', 10, 10, imgWidth, imgHeight)
      
      // 添加数据页面
      pdf.addPage()
      pdf.setFontSize(16)
      pdf.text('画板数据统计', 10, 20)
      
      // 统计节点数量
      const nodeStats = this.getNodeStatistics(canvasData)
      let yPos = 40
      
      pdf.setFontSize(12)
      for (const [type, count] of Object.entries(nodeStats)) {
        pdf.text(`${type}: ${count}个`, 10, yPos)
        yPos += 10
      }
      
      // 保存PDF
      pdf.save(`${filename}.pdf`)
      
      return { success: true, data: pdf }
    } catch (error) {
      // console.error('PDF导出失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 导出画板为Markdown
   * @param {object} canvasData - 画板数据
   * @param {string} filename - 文件名
   */
  static exportAsMarkdown(canvasData, filename = 'canvas') {
    try {
      let markdown = `# 画板内容导出\n\n`
      markdown += `导出时间: ${new Date().toLocaleString()}\n\n`
      
      // 统计信息
      const nodeStats = this.getNodeStatistics(canvasData)
      markdown += `## 统计信息\n\n`
      for (const [type, count] of Object.entries(nodeStats)) {
        markdown += `- ${type}: ${count}个\n`
      }
      markdown += '\n'
      
      // 节点详情
      if (canvasData.nodes && canvasData.nodes.length > 0) {
        markdown += `## 节点详情\n\n`
        
        canvasData.nodes.forEach((node, index) => {
          markdown += `### ${index + 1}. ${node.data.title || node.data.name || `节点${node.id}`}\n\n`
          markdown += `- **类型**: ${this.getNodeTypeName(node.type)}\n`
          markdown += `- **位置**: (${node.position.x}, ${node.position.y})\n`
          
          // 根据节点类型添加详细信息
          this.addNodeDetailsToMarkdown(node, markdown)
          
          markdown += '\n'
        })
      }
      
      // 连线信息
      if (canvasData.edges && canvasData.edges.length > 0) {
        markdown += `## 连线关系\n\n`
        canvasData.edges.forEach((edge, index) => {
          const sourceNode = canvasData.nodes.find(n => n.id === edge.source)
          const targetNode = canvasData.nodes.find(n => n.id === edge.target)
          
          markdown += `${index + 1}. ${sourceNode?.data.title || sourceNode?.data.name || edge.source} → ${targetNode?.data.title || targetNode?.data.name || edge.target}\n`
          if (edge.data?.label) {
            markdown += `   - 关系: ${edge.data.label}\n`
          }
        })
      }
      
      // 保存Markdown文件
      const blob = new Blob([markdown], { type: 'text/markdown' })
      const url = URL.createObjectURL(blob)
      
      const link = document.createElement('a')
      link.href = url
      link.download = `${filename}.md`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      URL.revokeObjectURL(url)
      return { success: true, data: markdown }
    } catch (error) {
      // console.error('Markdown导出失败:', error)
      return { success: false, error }
    }
  }

  /**
   * 获取节点统计信息
   * @param {object} canvasData - 画板数据
   * @returns {object} 节点统计
   */
  static getNodeStatistics(canvasData) {
    const stats = {}
    
    if (canvasData.nodes) {
      canvasData.nodes.forEach(node => {
        const typeName = this.getNodeTypeName(node.type)
        stats[typeName] = (stats[typeName] || 0) + 1
      })
    }
    
    return stats
  }

  /**
   * 获取节点类型中文名称
   * @param {string} type - 节点类型
   * @returns {string} 中文名称
   */
  static getNodeTypeName(type) {
    const typeMap = {
      'custom': '剧情节点',
      'character': '角色节点',
      'clue': '线索节点',
      'inference': '推理节点',
      'person': '人物节点',
      'atmosphere': '氛围节点'
    }
    return typeMap[type] || type
  }

  /**
   * 为Markdown添加节点详细信息
   * @param {object} node - 节点对象
   * @param {string} markdown - Markdown内容
   */
  static addNodeDetailsToMarkdown(node, markdown) {
    switch (node.type) {
      case 'custom':
        if (node.data.timeLabel) markdown += `- **时间**: ${node.data.timeLabel}\n`
        if (node.data.sceneDescription) markdown += `- **场景**: ${node.data.sceneDescription}\n`
        if (node.data.characters) markdown += `- **角色**: ${node.data.characters}\n`
        if (node.data.clues) markdown += `- **线索**: ${node.data.clues}\n`
        break
      case 'character':
        if (node.data.age) markdown += `- **年龄**: ${node.data.age}\n`
        if (node.data.occupation) markdown += `- **职业**: ${node.data.occupation}\n`
        if (node.data.personality) markdown += `- **性格**: ${node.data.personality.join(', ')}\n`
        break
      case 'clue':
        if (node.data.relatedEvent) markdown += `- **相关事件**: ${node.data.relatedEvent}\n`
        if (node.data.detail) markdown += `- **详情**: ${node.data.detail}\n`
        break
    }
    
    if (node.data.notes) {
      markdown += `- **备注**: ${node.data.notes}\n`
    }
  }
}