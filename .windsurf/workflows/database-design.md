---
description: 数据库设计工作流程 - 全局视角下的系统化设计方法
---

# 数据库设计工作流程

本工作流程用于指导从业务需求到数据库实现的完整设计过程，强调全局视角和表间关系的系统性设计。

## 第 1 步：业务域分析和核心场景梳理

### 1.1 识别业务子域
- 列出系统的所有业务子域
- 为每个子域定义职责边界
- 识别子域间的交互关系

### 1.2 核心使用场景分析
- 为每个业务子域列出 3-5 个核心使用场景
- 描述每个场景的数据流转过程
- 识别高频查询和写入操作

**示例（lqq-ai-agent）：**
```
用户与权限域：
- 用户注册/登录/权限校验
- 用户信息管理和状态控制

应用/智能体域：
- 创建应用/上架/列表查询/详情查看/删除
- 应用配置管理和部署

会话与消息域：
- 按应用查看会话列表/打开历史会话/清空会话
- 翻页查看某会话消息/搜索历史消息

知识库域：
- 为某应用绑定知识库/在对话中检索知识库
- 知识库文档管理和版本控制
```

## 第 2 步：实体识别和关系建模（ER 概念层）

### 2.1 实体识别
- 从业务场景中抽取核心实体
- 定义每个实体的职责和边界
- 区分主实体和关联实体

### 2.2 关系建模
- 识别实体间的关系类型（1:1, 1:N, N:N）
- 绘制实体关系图（文字或图形）
- 确定关系的约束条件

**示例关系设计：**
```
User (1) ——— (N) App
  用户可以创建多个应用

App (1) ——— (N) Conversation  
  每个应用可以有多个会话

Conversation (1) ——— (N) ConversationMessage
  每个会话包含多条消息

App (N) ——— (N) KnowledgeBase
  应用与知识库多对多关系（通过 app_kb_rel）

KnowledgeBase (1) ——— (N) KnowledgeDocument
  知识库包含多个文档
```

## 第 3 步：统一设计规范

### 3.1 主键设计
- 统一主键类型：`id BIGINT AUTO_INCREMENT`
- 为业务标识单独设计字段（如 deployKey）

### 3.2 外键命名规范
- 统一外键命名：`userId`, `appId`, `conversationId`, `kbId`
- 保持命名一致性和可读性

### 3.3 字段分层设计
```
核心业务字段：描述实体本质的字段
- appName, initPrompt, deployKey
- conversationTitle, type
- role, content, extra

状态控制字段：
- status (enabled/disabled)
- priority, role (user/admin)

审计字段（所有主表统一）：
- createTime DATETIME DEFAULT CURRENT_TIMESTAMP
- updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  
- editTime DATETIME DEFAULT CURRENT_TIMESTAMP
- isDelete TINYINT DEFAULT 0 (逻辑删除)
```

## 第 4 步：关联表设计

### 4.1 N:N 关系处理
- 为每个多对多关系创建关联表
- 关联表命名规范：`主表_从表_rel`
- 关联表字段设计原则

### 4.2 关联表结构
```sql
-- 示例：应用知识库关联表
CREATE TABLE app_kb_rel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appId BIGINT NOT NULL COMMENT '应用ID',
    kbId BIGINT NOT NULL COMMENT '知识库ID', 
    userId BIGINT NOT NULL COMMENT '创建用户ID',
    bindTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    isDelete TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_app_kb (appId, kbId)
);
```

## 第 5 步：查询场景驱动的索引设计

### 5.1 高频查询分析
- 列出所有高频查询场景
- 写出对应的伪SQL语句
- 分析查询的WHERE条件和ORDER BY子句

### 5.2 索引设计策略
```sql
-- 示例索引设计思路：

-- 查某用户创建的应用
SELECT * FROM app WHERE userId = ? ORDER BY createTime DESC LIMIT ?,?
-- 需要索引：idx_userId_createTime

-- 查某应用下会话列表  
SELECT * FROM conversation WHERE appId = ? AND userId = ? ORDER BY updateTime DESC
-- 需要索引：idx_appId_userId_updateTime

-- 查某会话消息列表
SELECT * FROM conversation_message WHERE conversationId = ? ORDER BY createTime ASC  
-- 需要索引：idx_conversationId_createTime
```

## 第 6 步：数据安全和多租户设计

### 6.1 租户隔离策略
- 明确租户边界（通常是 userId）
- 所有用户数据表必须包含 userId
- 设计数据访问权限控制

### 6.2 数据隔离检查清单
- [ ] 所有用户拥有的数据表都有 userId 字段
- [ ] 关联表中适当冗余 userId 便于权限控制
- [ ] 查询时始终带上租户条件

## 第 7 步：端到端业务流程验证

### 7.1 完整业务链路测试
选择一条完整业务流程，验证数据库设计：
```
用户登录 → 选择应用 → 打开/创建会话 → 发送消息 → 知识库检索 → 返回回答
```

### 7.2 验证检查点
- [ ] 每个步骤涉及的表和字段是否完整
- [ ] 查询性能是否满足要求（索引覆盖）
- [ ] 数据一致性约束是否正确
- [ ] 权限控制是否到位

## 第 8 步：文档和版本管理

### 8.1 设计文档维护
- 更新 `docs/db-schema.md` 包含所有表结构
- 维护 `docs/sql/` 目录下的建表脚本
- 记录设计决策和变更历史

### 8.2 表演进流程
```
新需求 → 修改ER设计 → 更新设计文档 → 编写SQL脚本 → 执行到数据库 → 更新代码实体类
```

## 第 9 步：性能和扩展性考虑

### 9.1 性能优化检查
- [ ] 大表是否有合适的分区策略
- [ ] 是否有不必要的冗余数据
- [ ] 索引是否过多影响写入性能

### 9.2 扩展性设计
- [ ] 表结构是否支持未来功能扩展
- [ ] 是否预留了扩展字段（如 extra JSON字段）
- [ ] 数据迁移和版本升级策略

## 使用建议

1. **严格按步骤执行**：不要跳过任何步骤，每个步骤都有其必要性
2. **文档先行**：先完善设计文档，再编写SQL和代码
3. **迭代优化**：设计完成后，根据实际使用情况持续优化
4. **团队协作**：重要设计决策需要团队review和确认

## 常见陷阱

❌ **避免的错误做法：**
- 直接开始写建表SQL，没有全局规划
- 忽略表间关系，导致数据冗余或不一致
- 索引设计不当，影响查询性能
- 缺乏租户隔离，存在数据安全风险
- 文档与实际实现不同步

✅ **推荐的做法：**
- 始终从业务需求出发进行设计
- 保持表结构和命名的一致性
- 重视索引设计和查询性能
- 做好数据安全和权限控制
- 维护完整的设计文档
