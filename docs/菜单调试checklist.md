# 菜单显示问题调试清单

## 🔍 快速检查步骤

### 1. 检查菜单是否在 DOM 中

打开浏览器开发者工具（F12），在 Elements 标签中：

1. 点击三个点按钮
2. 在 Elements 中搜索 `dropdown-menu`
3. 检查是否能找到 `.dropdown-menu` 元素

**如果找不到**：
- 检查 `openMenuChatId` 的值
- 在 Console 中执行：`document.querySelectorAll('.chat-actions')`

---

### 2. 检查菜单的显示状态

如果菜单在 DOM 中，选中 `.dropdown-menu` 元素，查看 Computed 样式：

检查项：
```
✓ display: 不应该是 none
✓ opacity: 应该是 1
✓ visibility: 应该是 visible
✓ z-index: 应该是 9999
```

---

### 3. 检查父容器的 overflow

在 Elements 中，依次检查以下元素的 overflow 属性：

1. `.history-item` - 应该没有 overflow 限制
2. `.history-list` - overflow: visible
3. `.chat-history` - overflow-x: visible, overflow-y: auto
4. `.sidebar` - overflow: visible

**检查方法**：
选中元素 → Computed 标签 → 搜索 "overflow"

---

### 4. 检查 z-index 层级

在 Console 中运行：

```javascript
// 检查菜单的 z-index
const menu = document.querySelector('.dropdown-menu')
if (menu) {
  console.log('菜单 z-index:', window.getComputedStyle(menu).zIndex)
  console.log('菜单位置:', menu.getBoundingClientRect())
}

// 检查按钮容器的 z-index
const actions = document.querySelector('.chat-actions')
if (actions) {
  console.log('按钮 z-index:', window.getComputedStyle(actions).zIndex)
}
```

---

### 5. 检查菜单位置

在 Console 中运行：

```javascript
const menu = document.querySelector('.dropdown-menu')
if (menu) {
  const rect = menu.getBoundingClientRect()
  console.log('菜单位置:', {
    top: rect.top,
    left: rect.left,
    right: rect.right,
    bottom: rect.bottom,
    width: rect.width,
    height: rect.height
  })
  
  // 检查是否在视口内
  if (rect.left < 0 || rect.top < 0 || 
      rect.right > window.innerWidth || 
      rect.bottom > window.innerHeight) {
    console.log('⚠️ 菜单在视口外！')
  }
}
```

---

### 6. 强制显示菜单（测试）

在 Console 中运行：

```javascript
// 强制显示菜单，用于测试
const menu = document.querySelector('.dropdown-menu')
if (menu) {
  menu.style.display = 'block'
  menu.style.opacity = '1'
  menu.style.visibility = 'visible'
  menu.style.zIndex = '99999'
  menu.style.background = 'red'  // 红色背景方便看到
  console.log('✓ 菜单已强制显示')
}
```

---

## 🐛 常见问题和解决方案

### 问题 1: 菜单在 DOM 中但看不见

**可能原因**：
- 被其他元素遮挡
- opacity 为 0
- 菜单在视口外

**解决方案**：
```javascript
// 检查遮挡元素
const menu = document.querySelector('.dropdown-menu')
const rect = menu.getBoundingClientRect()
const topElement = document.elementFromPoint(rect.left + 10, rect.top + 10)
console.log('菜单位置上的元素:', topElement)
```

---

### 问题 2: 点击按钮菜单不出现

**可能原因**：
- `openMenuChatId` 没有更新
- Vue 响应式问题

**解决方案**：
```javascript
// 查看 Vue DevTools
// 或在 Console 中手动触发
const chatLayout = document.querySelector('.chat-layout').__vueParentComponent
console.log('openMenuChatId:', chatLayout.ctx.openMenuChatId)
```

---

### 问题 3: 菜单被父容器裁剪

**可能原因**：
- 父容器有 overflow: hidden

**解决方案**：
```javascript
// 检查所有父元素的 overflow
let el = document.querySelector('.dropdown-menu')
while (el) {
  const style = window.getComputedStyle(el)
  if (style.overflow !== 'visible' && style.overflow !== 'auto') {
    console.log('发现 overflow 限制:', el.className, style.overflow)
  }
  el = el.parentElement
}
```

---

## 📋 当前配置检查

### 应该有的CSS

```scss
.sidebar {
  overflow: visible;  // ✓
}

.chat-history {
  overflow-x: visible;  // ✓
  overflow-y: auto;     // ✓
}

.history-list {
  overflow: visible;  // ✓
}

.chat-actions {
  z-index: 1001;  // ✓
}

.dropdown-menu {
  z-index: 9999;  // ✓
  position: absolute;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}
```

---

## 🔧 临时修复方案

如果以上都检查了还是不行，可以尝试：

### 方案 A: 使用 Fixed 定位

```scss
.dropdown-menu {
  position: fixed !important;
  // 需要 JS 动态计算位置
}
```

### 方案 B: 使用 Portal/Teleport

```vue
<teleport to="body">
  <div v-if="openMenuChatId === chat.id" class="dropdown-menu">
    <!-- 菜单内容 -->
  </div>
</teleport>
```

---

## 📸 截图分享

如果以上方法都不行，请截图以下内容：

1. Elements 标签中的 `.dropdown-menu` 元素
2. Computed 样式中的 overflow 和 z-index
3. Console 中运行检查脚本的输出

---

**最后更新**: 2025-11-23 15:22
