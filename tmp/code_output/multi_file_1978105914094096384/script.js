// 获取 DOM 元素
const redeemForm = document.getElementById('redeemForm');
const codeInput = document.getElementById('code');
const successMessage = document.getElementById('successMessage');
const errorMessage = document.getElementById('errorMessage');
const redeemList = document.getElementById('redeemList');
const emptyHistory = document.getElementById('emptyHistory');

// 模拟有效的兑换码列表（实际项目中应由后端验证）
const validCodes = ['ABC123', 'XZY987', 'VIP2025', 'FREE2024'];

// 从 localStorage 加载兑换历史
function loadHistory() {
  const history = JSON.parse(localStorage.getItem('redeemHistory')) || [];
  renderHistory(history);
}

// 渲染历史记录
function renderHistory(history) {
  redeemList.innerHTML = '';
  if (history.length === 0) {
    emptyHistory.style.display = 'block';
  } else {
    emptyHistory.style.display = 'none';
    history.forEach(item => {
      const li = document.createElement('li');
      li.className = 'redeem-item';
      li.innerHTML = `
        <span class="redeem-code">${item.code}</span>
        <div class="date">兑换时间：${new Date(item.timestamp).toLocaleString()}</div>
      `;
      redeemList.appendChild(li);
    });
  }
}

// 保存新记录到历史
function saveToHistory(code) {
  const history = JSON.parse(localStorage.getItem('redeemHistory')) || [];
  const newRecord = {
    code: code.trim().toUpperCase(),
    timestamp: new Date().getTime()
  };
  history.unshift(newRecord); // 最新的在最前面
  localStorage.setItem('redeemHistory', JSON.stringify(history));
  renderHistory(history);
}

// 表单提交处理
redeemForm.addEventListener('submit', function(e) {
  e.preventDefault();

  const code = codeInput.value.trim().toUpperCase();

  // 验证是否为空
  if (!code) {
    showError('请输入兑换码。');
    return;
  }

  // 检查是否已兑换过（防止重复）
  const history = JSON.parse(localStorage.getItem('redeemHistory')) || [];
  const alreadyUsed = history.some(item => item.code === code);
  if (alreadyUsed) {
    showError('该兑换码已被使用过。');
    return;
  }

  // 验证兑换码有效性
  if (validCodes.includes(code)) {
    showSuccess();
    saveToHistory(code);
    redeemForm.reset();
  } else {
    showError('兑换码无效，请核对后重试。');
  }
});

// 显示成功消息
function showSuccess() {
  hideMessages();
  successMessage.style.display = 'block';
  setTimeout(() => {
    successMessage.style.display = 'none';
  }, 3000);
}

// 显示错误消息
function showError(message) {
  hideMessages();
  errorMessage.textContent = message;
  errorMessage.style.display = 'block';
  setTimeout(() => {
    errorMessage.style.display = 'none';
  }, 3000);
}

// 隐藏所有消息
function hideMessages() {
  successMessage.style.display = 'none';
  errorMessage.style.display = 'none';
}

// 输入框聚焦时隐藏提示
codeInput.addEventListener('focus', hideMessages);

// 页面加载完成后初始化
window.addEventListener('DOMContentLoaded', () => {
  loadHistory();
});