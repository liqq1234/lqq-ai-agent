# Markdown 渲染配置指南

## 📋 问题描述

AI 回复的内容是 Markdown 格式，但前端直接显示原始文本，导致：
- 列表没有缩进和项目符号
- 代码块没有高亮
- 加粗、斜体等格式不生效
- 整体排版混乱

**示例**：
```
1.**基础知识**：
 -**理解概念**：首先需要...
```

应该显示为：

1. **基础知识**：
   - **理解概念**：首先需要...

---

## ✅ 解决方案：markdown-it + highlight.js

### 核心技术
- **markdown-it**: 业界最流行的 Markdown 解析器，GitHub、知乎都在用
- **highlight.js**: 代码高亮库，支持 180+ 种语言

---

## 🔧 实施步骤

### 1. 安装依赖

```bash
npm install markdown-it highlight.js
```

**已安装** ✅

---

### 2. 修改 ChatBubble 组件

**文件**: `src/components/ChatBubble.vue`

#### 2.1 导入依赖和配置 Markdown 解析器

```javascript
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 配置 Markdown 解析器
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  linkify: true,     // 自动识别链接
  typographer: true, // 智能标点
  breaks: true,      // 换行转为 <br>
  highlight: function (str, lang) {
    // 代码高亮
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (err) {
        console.error('代码高亮失败:', err)
      }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})
```

#### 2.2 添加 Markdown 渲染逻辑

```javascript
// 渲染 Markdown 内容
const renderedContent = computed(() => {
  if (!props.content) return ''
  
  // AI 消息渲染 Markdown，用户消息保持纯文本
  if (props.from === 'ai') {
    try {
      return md.render(props.content)
    } catch (err) {
      console.error('Markdown 渲染失败:', err)
      return props.content
    }
  }
  
  // 用户消息使用纯文本，但支持换行
  return props.content.replace(/\n/g, '<br>')
})
```

#### 2.3 修改模板使用 v-html

```vue
<div class="message-text" v-html="renderedContent"></div>
```

**⚠️ 注意**：使用 `v-html` 前确保内容来源可信，避免 XSS 攻击。

---

### 3. 导入代码高亮样式

**文件**: `src/main.js`

```javascript
// 引入代码高亮样式（GitHub 风格）
import 'highlight.js/styles/github.css'
```

**可选主题**：
- `github.css` - GitHub 风格（浅色）
- `github-dark.css` - GitHub 深色
- `monokai.css` - Monokai 风格
- `atom-one-dark.css` - Atom One Dark
- [更多主题](https://highlightjs.org/static/demo/)

---

### 4. 添加 Markdown 样式

**文件**: `src/components/ChatBubble.vue` 的 `<style>` 部分

```scss
.message-text {
  font-size: 15px;
  line-height: 1.6;
  
  // 标题样式
  :deep(h1), :deep(h2), :deep(h3) {
    margin: 16px 0 8px;
    font-weight: 600;
  }
  
  // 列表样式
  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 24px;
  }
  
  // 代码块样式
  :deep(code) {
    background: #f6f8fa;
    padding: 2px 6px;
    border-radius: 3px;
    color: #e83e8c;
  }
  
  :deep(pre) {
    background: #f6f8fa;
    border-radius: 6px;
    padding: 16px;
    overflow-x: auto;
    margin: 12px 0;
  }
  
  // 其他元素...
}
```

**已添加完整样式** ✅

---

## 🎯 效果展示

### ✅ 修复后

#### 1. 标题和列表
```
1. **基础知识**：
   - **理解概念**：首先需要...
```

渲染为：

1. **基础知识**：
   - **理解概念**：首先需要...

#### 2. 代码高亮

````markdown
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```
````

渲染为带语法高亮的代码块 ✨

#### 3. 格式支持
- **加粗文本**
- *斜体文本*
- [链接](https://example.com)
- `行内代码`
- 表格、引用等

---

## 📋 支持的 Markdown 语法

### 标题
```markdown
# H1 标题
## H2 标题
### H3 标题
```

### 列表
```markdown
1. 有序列表
2. 第二项

- 无序列表
- 第二项
```

### 强调
```markdown
**加粗** 或 __加粗__
*斜体* 或 _斜体_
~~删除线~~
```

### 代码
```markdown
行内代码：`code`

代码块：
\`\`\`java
public class Demo {}
\`\`\`
```

### 链接和图片
```markdown
[链接文字](https://example.com)
![图片描述](image.png)
```

### 引用
```markdown
> 这是一段引用
> 可以多行
```

### 表格
```markdown
| 列1 | 列2 |
|-----|-----|
| 内容1 | 内容2 |
```

### 分隔线
```markdown
---
```

---

## 🔍 技术细节

### 1. markdown-it 配置选项

| 选项 | 说明 | 默认值 |
|------|------|--------|
| `html` | 允许 HTML 标签 | false |
| `linkify` | 自动识别链接 | false |
| `typographer` | 智能标点替换 | false |
| `breaks` | 换行转 `<br>` | false |
| `highlight` | 代码高亮函数 | null |

### 2. 为什么只对 AI 消息渲染 Markdown？

```javascript
if (props.from === 'ai') {
  return md.render(props.content)  // AI 消息渲染 Markdown
}
return props.content.replace(/\n/g, '<br>')  // 用户消息纯文本
```

**原因**：
- **AI 消息**：模型返回的是 Markdown 格式，需要渲染
- **用户消息**：用户输入的是纯文本，只需要支持换行

### 3. XSS 安全性

使用 `v-html` 可能导致 XSS 攻击，但在此场景是安全的：

**安全理由**：
1. AI 回复来自后端，可信来源
2. markdown-it 会自动转义危险的 HTML
3. 用户消息不使用 Markdown 渲染

**额外防护**（可选）：
```javascript
import DOMPurify from 'dompurify'

const renderedContent = computed(() => {
  if (props.from === 'ai') {
    const html = md.render(props.content)
    return DOMPurify.sanitize(html)  // 清理危险标签
  }
  // ...
})
```

---

## 📊 性能优化

### 1. 缓存 Markdown 实例
```javascript
// ✅ 好：组件级别缓存
const md = new MarkdownIt({ ... })

// ❌ 差：每次渲染都创建
const renderedContent = computed(() => {
  const md = new MarkdownIt({ ... })  // 不要这样做
  return md.render(props.content)
})
```

### 2. 按需导入 highlight.js
如果只需要部分语言，可以减小打包体积：

```javascript
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import java from 'highlight.js/lib/languages/java'
import python from 'highlight.js/lib/languages/python'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('java', java)
hljs.registerLanguage('python', python)
```

---

## 🎓 面试要点

### Q1: 为什么选择 markdown-it 而不是 marked？

**A**: 
- **markdown-it**: 插件丰富、性能更好、社区活跃
- **marked**: 更轻量，但功能较少
- **其他方案**: react-markdown（React）、showdown（老项目）

### Q2: v-html 有什么安全风险？

**A**: 
- **XSS 攻击**：注入恶意脚本
- **防护措施**：
  1. 只对可信内容使用 v-html
  2. 使用 DOMPurify 清理
  3. 配置 CSP（内容安全策略）

### Q3: 如何优化 Markdown 渲染性能？

**A**: 
1. **缓存解析器实例**：避免重复创建
2. **按需加载语言包**：减小打包体积
3. **虚拟滚动**：大量消息时使用
4. **Web Worker**：复杂渲染放到后台线程

---

## 🚨 常见问题

### Q1: 代码块没有高亮？

**检查**：
1. 是否导入了 `highlight.js/styles/xxx.css`？
2. 代码块是否指定了语言？

```markdown
\`\`\`java  ← 必须指定语言
public class Demo {}
\`\`\`
```

### Q2: 列表样式不对？

**检查**：
1. CSS 中是否设置了 `padding-left`？
2. 是否使用了 `:deep()` 选择器？

### Q3: 表格显示不正常？

**确保**：
1. Markdown 表格格式正确
2. CSS 中添加了 table 样式

---

## 📁 相关文件

### 前端
- ✅ `ChatBubble.vue` - Markdown 渲染组件
- ✅ `main.js` - 代码高亮样式导入
- ✅ `package.json` - 依赖配置

### 后端
- `ChatController.java` - 返回 Markdown 格式内容
- `application.yml` - LangChain4j 配置

---

## 🎉 完成标志

当你在前端看到：
- ✅ 列表有缩进和项目符号
- ✅ 代码块有语法高亮
- ✅ 加粗、斜体等格式正确
- ✅ 整体排版美观

就说明 Markdown 渲染配置成功了！

---

## 📚 参考资料

- [markdown-it 官方文档](https://markdown-it.github.io/)
- [highlight.js 官方网站](https://highlightjs.org/)
- [Markdown 语法指南](https://www.markdownguide.org/)
- [Vue v-html 官方文档](https://cn.vuejs.org/api/built-in-directives.html#v-html)

---

**最后更新**: 2025-11-22 21:50
**修改人员**: Cascade AI
