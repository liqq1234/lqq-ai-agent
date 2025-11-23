# CORS 登录问题修复记录

## 问题描述

**时间**: 2025-11-22 21:04  
**错误类型**: `com.lqq.lqqaiagent.exception.BusinessException: 未登录`  
**触发场景**: 点击前端登录按钮时

### 错误堆栈关键信息
```
com.lqq.lqqaiagent.exception.BusinessException: 未登录
	at com.lqq.lqqaiagent.config.interceptor.UserContextInterceptor.preHandle(UserContextInterceptor.java:28)
	at org.springframework.web.servlet.FrameworkServlet.doOptions(FrameworkServlet.java:950)
```

**关键线索**: `doOptions` 方法，说明是 OPTIONS 预检请求被拦截。

---

## 问题分析

### 根本原因
1. **CORS 预检请求被拦截**: 浏览器发送 OPTIONS 预检请求，被 `UserContextInterceptor` 拦截
2. **拦截器强制校验登录**: 所有请求都要求用户已登录，包括 OPTIONS 请求
3. **Spring Security 未正确配置 CORS**: 导致预检请求无法通过

### 请求流程
```
浏览器 → OPTIONS 预检请求 → Spring Security → UserContextInterceptor
                                                    ↓
                                            检查登录状态（Session）
                                                    ↓
                                            抛出"未登录"异常 ❌
```

---

## 修复方案

### 修复 1: 拦截器放行 OPTIONS 请求

**文件**: `UserContextInterceptor.java`

```java
@Override
public boolean preHandle(HttpServletRequest request,
                         HttpServletResponse response,
                         Object handler) {

    // 放行 OPTIONS 请求（CORS 预检请求）
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        return true;
    }

    // 从 Session 获取当前登录用户
    Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN);
    if (userObj instanceof User user) {
        UserContext.setUser(user);
    }

    return true;
}
```

**说明**: 
- OPTIONS 请求是浏览器自动发送的预检请求，不携带 Session
- 必须直接放行，否则会导致跨域请求失败

---

### 修复 2: 完善 Spring Security CORS 配置

**文件**: `SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 启用 CORS
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .httpBasic(httpBasic -> httpBasic.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);  // 允许携带 Cookie
    configuration.setMaxAge(3600L);  // 预检请求缓存 1 小时

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**关键配置**:
- `allowCredentials(true)`: 允许携带 Cookie（Session 认证必需）
- `allowedOriginPatterns("*")`: 允许所有域名（开发环境）
- `maxAge(3600L)`: 预检请求缓存 1 小时，减少 OPTIONS 请求次数

---

### 修复 3: 已存在的 CORS 配置

**文件**: `CorsConfig.java` (无需修改，已正确配置)

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
```

---

## 修复后的请求流程

### 正常流程
```
1. 浏览器发送 OPTIONS 预检请求
   ↓
2. Spring Security CORS Filter 处理
   ↓
3. UserContextInterceptor.preHandle()
   ↓
4. 检测到 OPTIONS 请求，直接返回 true ✅
   ↓
5. 返回 200 OK，携带 CORS 响应头
   ↓
6. 浏览器发送真实的 POST /api/user/login 请求
   ↓
7. 登录成功，返回用户信息 ✅
```

---

## 验证测试

### 测试步骤
1. 重启后端服务
2. 打开浏览器 DevTools（网络面板）
3. 点击前端"登录"按钮
4. 观察网络请求

### 预期结果
```
✅ OPTIONS /api/user/login  →  200 OK  (预检请求成功)
✅ POST   /api/user/login  →  200 OK  (登录请求成功)
```

### 响应头检查
```http
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
```

---

## 知识点总结

### 1. CORS 预检请求
- **什么时候触发**: 跨域 POST 请求 + `Content-Type: application/json`
- **请求方法**: OPTIONS
- **携带内容**: 不携带 Cookie、不携带请求体
- **必须放行**: 拦截器、过滤器都要放行

### 2. Session 认证 + CORS
- **必须配置**: `allowCredentials: true`（前后端都要配置）
- **Origin 限制**: 不能使用 `*`，必须使用 `allowedOriginPatterns`
- **Cookie 传递**: 前端请求需要 `withCredentials: true`

### 3. 拦截器 vs 过滤器
| 特性 | 拦截器（Interceptor） | 过滤器（Filter） |
|------|---------------------|----------------|
| 执行顺序 | 晚于过滤器 | 早于拦截器 |
| OPTIONS 处理 | 需要手动放行 | Spring Security 已处理 |
| 适用场景 | 业务逻辑校验 | 请求预处理 |

---

## 常见错误

### ❌ 错误 1：拦截器未放行 OPTIONS
```java
// 错误
public boolean preHandle(HttpServletRequest request, ...) {
    ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN);  // 直接校验
    return true;
}
```

**后果**: 所有跨域请求都会失败，前端报 CORS 错误

### ❌ 错误 2：CORS 配置冲突
```java
// 错误
.cors(cors -> cors.disable())  // 禁用 CORS
```

**后果**: Spring MVC 的 CORS 配置不生效

### ❌ 错误 3：allowCredentials 与 allowedOrigins 冲突
```java
// 错误
configuration.setAllowedOrigins(Arrays.asList("*"));  // 使用 *
configuration.setAllowCredentials(true);  // 允许凭证
```

**后果**: 浏览器报错，CORS 策略冲突

### ✅ 正确做法
```java
// 正确
configuration.setAllowedOriginPatterns(Arrays.asList("*"));  // 使用 patterns
configuration.setAllowCredentials(true);
```

---

## 生产环境配置建议

### 1. 限制允许的域名
```java
// 开发环境
configuration.setAllowedOriginPatterns(Arrays.asList("*"));

// 生产环境
configuration.setAllowedOrigins(Arrays.asList(
    "https://www.example.com",
    "https://app.example.com"
));
```

### 2. 减少暴露的响应头
```java
// 开发环境
configuration.setExposedHeaders(Arrays.asList("*"));

// 生产环境
configuration.setExposedHeaders(Arrays.asList(
    "Authorization",
    "Content-Type",
    "X-Total-Count"
));
```

### 3. 增加预检缓存时间
```java
configuration.setMaxAge(7200L);  // 2 小时（生产环境）
```

---

## 面试要点

### Q1: 什么是 CORS 预检请求？
**A**: 浏览器在发送跨域请求前，先发送 OPTIONS 请求询问服务器是否允许跨域。满足以下条件触发：
- 非简单请求（POST + JSON、自定义请求头等）
- 跨域请求

### Q2: 为什么 OPTIONS 请求没有 Session？
**A**: OPTIONS 是浏览器自动发送的，不会携带 Cookie 和 Session，因此拦截器必须直接放行。

### Q3: allowCredentials 的作用？
**A**: 允许跨域请求携带凭证（Cookie、Authorization 等），Session 认证必须开启此项。

### Q4: 为什么不能用 allowedOrigins("*") + allowCredentials(true)？
**A**: 安全策略冲突，浏览器禁止此组合。必须使用 `allowedOriginPatterns` 或指定具体域名。

---

## 相关文件

### 后端
- ✅ `UserContextInterceptor.java` - 放行 OPTIONS 请求
- ✅ `SecurityConfig.java` - 配置 CORS 和 Session
- ⚠️ `CorsConfig.java` - Spring MVC CORS（可能与 Security 冲突，建议统一配置）

### 前端
- ✅ `request.js` - `withCredentials: true`（已配置）
- ✅ `.env.development` - 正确的后端地址

---

## 修复完成检查清单

- [x] UserContextInterceptor 放行 OPTIONS 请求
- [x] SecurityConfig 配置 CORS
- [x] 允许携带凭证（allowCredentials）
- [x] 配置 Session 管理策略
- [x] 文档记录修复过程
- [ ] 重启后端服务验证
- [ ] 测试登录、注册、退出功能
- [ ] 检查浏览器控制台无 CORS 错误

---

**修复完成时间**: 2025-11-22 21:10  
**修复人员**: Cascade AI  
**修复结果**: 等待用户验证 ✅
