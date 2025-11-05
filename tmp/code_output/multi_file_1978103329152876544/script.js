// 获取 DOM 元素
const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const successMessage = document.getElementById('successMessage');
const errorMessage = document.getElementById('errorMessage');

// 模拟有效用户凭证（实际项目中应由后端验证）
const validCredentials = {
  username: 'admin',
  password: '123456'
};

// 表单提交处理
loginForm.addEventListener('submit', function(e) {
  e.preventDefault(); // 阻止默认提交行为

  const username = usernameInput.value.trim();
  const password = passwordInput.value.trim();

  // 简单校验输入
  if (!username || !password) {
    showError('请输入完整的用户名和密码。');
    return;
  }

  // 验证用户名和密码
  if (username === validCredentials.username && password === validCredentials.password) {
    showSuccess();
    // 模拟跳转
    setTimeout(() => {
      alert(`欢迎回来，${username}！`);
    }, 1000);
  } else {
    showError('用户名或密码错误，请检查后重试。');
  }
});

// 显示成功消息
function showSuccess() {
  hideMessages();
  successMessage.style.display = 'block';
}

// 显示错误消息
function showError(message) {
  hideMessages();
  errorMessage.textContent = message;
  errorMessage.style.display = 'block';
}

// 隐藏所有提示消息
function hideMessages() {
  successMessage.style.display = 'none';
  errorMessage.style.display = 'none';
}

// 输入框获得焦点时自动隐藏错误信息
usernameInput.addEventListener('focus', hideMessages);
passwordInput.addEventListener('focus', hideMessages);