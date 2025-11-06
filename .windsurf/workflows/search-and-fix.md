---
description: 搜索网络解决方案并自动修复错误的通用工作流程
---

# 🔍 搜索并修复错误工作流程

这是一个通用的错误解决工作流程，会自动搜索网络上的解决方案并应用修复。

## 📋 使用方法

### 命令格式
```bash
/search-and-fix "错误信息或问题描述"
```

### 示例用法
```bash
/search-and-fix "Java syntax error constructor name"
/search-and-fix "Spring AI FunctionCallback deprecated"
/search-and-fix "The method is undefined for the type"
```

## 🚀 执行流程

### 阶段 1：错误分析
// turbo
1. **提取错误信息**
   - 分析错误类型和严重程度
   - 识别相关技术栈和框架
   - 提取关键错误关键词

// turbo
2. **构建搜索查询**
   - 组合错误信息和技术栈
   - 添加版本信息和上下文
   - 优化搜索关键词

### 阶段 2：网络搜索
// turbo
3. **多源搜索**
   - Stack Overflow 问答搜索
   - GitHub Issues 搜索
   - 官方文档搜索
   - 技术博客搜索

// turbo
4. **结果分析**
   - 评估解决方案的相关性
   - 检查解决方案的完整性
   - 验证适用性和时效性

### 阶段 3：解决方案应用
// turbo
5. **制定修复计划**
   - 选择最佳解决方案
   - 评估修复风险
   - 准备回滚方案

// turbo
6. **执行修复**
   - 应用代码修改
   - 更新配置文件
   - 添加必要依赖

// turbo
7. **验证修复**
   - 编译检查
   - 功能测试
   - 回归测试

### 阶段 4：文档记录
8. **记录解决过程**
   - 保存错误信息和解决方案
   - 更新知识库
   - 生成修复报告

## 🔧 搜索策略

### 搜索源优先级
1. **Stack Overflow** (最高优先级)
   - 社区验证的解决方案
   - 详细的问题描述和答案
   - 投票和采纳机制

2. **GitHub Issues** (高优先级)
   - 官方项目的问题跟踪
   - 开发者直接回复
   - 版本特定的解决方案

3. **官方文档** (高优先级)
   - 权威的使用指南
   - API 变更说明
   - 最佳实践建议

4. **技术博客** (中优先级)
   - 深入的技术分析
   - 实际案例研究
   - 专家经验分享

### 搜索查询优化
```
基础查询：错误信息 + 技术栈
示例：Java constructor name mismatch Spring Boot

增强查询：错误信息 + 技术栈 + 版本 + 解决方案关键词
示例：Java constructor name mismatch Spring Boot 2.7 fix solution

特定查询：具体错误码 + 框架版本
示例：Spring AI FunctionCallback deprecated ToolCallback migration
```

## 🛠️ 修复策略

### 代码修复类型

#### 1. 语法错误修复
```java
// 常见问题：构造方法名称不匹配
// 搜索：Java constructor name must match class name
// 修复：确保构造方法名与类名完全一致

public class MyClass {
    // 错误
    public MyClasss() { }
    
    // 正确
    public MyClass() { }
}
```

#### 2. API 弃用修复
```java
// 常见问题：使用已弃用的 API
// 搜索：Spring AI FunctionCallback deprecated alternative
// 修复：使用新的 ToolCallback API

// 旧版本
FunctionCallback.builder()
    .function("name", instance)
    .build()

// 新版本
ChatClient.builder(model)
    .tools(instance)
    .build()
```

#### 3. 方法调用错误修复
```java
// 常见问题：调用不存在的方法
// 搜索：method undefined for type Java Spring
// 修复：使用正确的方法名或添加缺失的方法

// 错误
obj.nonExistentMethod()

// 正确
obj.existingMethod()
```

#### 4. 依赖问题修复
```xml
<!-- 常见问题：缺少依赖 -->
<!-- 搜索：dependency cannot be resolved Maven -->
<!-- 修复：添加正确的依赖 -->

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 📊 成功指标

### 搜索效果
- 相关结果命中率：> 80%
- 解决方案适用率：> 70%
- 搜索响应时间：< 30 秒

### 修复效果
- 一次修复成功率：> 75%
- 编译通过率：> 95%
- 功能正常率：> 90%

### 效率指标
- 平均解决时间：< 15 分钟
- 自动化程度：> 60%
- 用户满意度：> 85%

## 🎯 最佳实践

### 搜索技巧
1. **使用英文关键词**：技术搜索以英文为主
2. **包含版本信息**：指定框架和库的版本
3. **添加上下文**：包含相关的技术栈信息
4. **使用专业术语**：使用准确的技术术语

### 修复原则
1. **最小化修改**：只修改必要的部分
2. **保持兼容性**：确保修改不破坏现有功能
3. **遵循规范**：符合编码规范和最佳实践
4. **充分测试**：验证修复的有效性

### 风险控制
1. **备份代码**：修改前备份重要文件
2. **分步执行**：逐步应用修复方案
3. **回滚准备**：准备回滚方案
4. **测试验证**：充分测试修复结果

## 🔄 持续改进

### 学习机制
- 记录成功的解决方案
- 分析失败的修复尝试
- 更新搜索策略
- 优化修复模板

### 知识积累
- 建立错误解决方案库
- 维护常见问题 FAQ
- 更新最佳实践指南
- 分享经验和教训

---

## 💡 使用提示

1. **详细描述问题**：提供完整的错误信息和上下文
2. **指定技术栈**：明确使用的框架和版本
3. **验证修复结果**：确保修复不引入新问题
4. **记录解决过程**：为将来类似问题做准备

这个工作流程设计为智能化和自动化的错误解决方案，通过网络搜索和知识积累，不断提高解决问题的效率和准确性。
