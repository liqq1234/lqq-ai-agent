# lqq-ai-agent 数据库结构

> 本文档用于记录当前项目使用的核心数据表结构，便于对照代码和后续演进。

---

## 1. app 表（应用 / 智能体模板）

```sql
-- TODO: 在数据库客户端中执行 `SHOW CREATE TABLE app\G` 或使用图形工具复制 DDL，
-- 然后把生成的 CREATE TABLE app ... 语句粘贴到这里。
```

---

## 2. ai_chat_memory 表（会话记忆 / 旧版）

```sql
-- TODO: 在数据库客户端中执行 `SHOW CREATE TABLE ai_chat_memory\G`，
-- 或使用图形工具复制 DDL，粘贴到这里。
```

---

## 3. users 表（用户）

```sql
-- TODO: 在数据库客户端中执行 `SHOW CREATE TABLE users\G`，
-- 或使用图形工具复制 DDL，粘贴到这里。
```

---

## 4. conversation 表（会话）

```sql
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `appid` bigint NOT NULL COMMENT '关联应用 id',
  `userid` bigint NOT NULL COMMENT '会话所属用户 id',
  `title` varchar(255) DEFAULT NULL COMMENT '会话标题',
  `createtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isdelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_user_app` (`userid`, `appid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话';
```

---

## 5. conversation_message 表（会话消息）

```sql
CREATE TABLE IF NOT EXISTS `conversation_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `conversationid` bigint NOT NULL COMMENT '所属会话 id',
  `role` varchar(32) NOT NULL COMMENT '角色 user/assistant/system',
  `content` text NOT NULL COMMENT '消息内容',
  `extra` text DEFAULT NULL COMMENT '额外信息(JSON)',
  `createtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `isdelete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_message_conversation` (`conversationid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话消息';
```

---

> 后续可以在每个表下面补充“字段说明”“索引说明”等文字说明，慢慢完善。
